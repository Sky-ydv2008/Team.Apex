/**
 * Apex Innovators — auth.js
 * Token + user persistence (localStorage ai_token / ai_refresh / ai_user),
 * login / register / logout, and the admin role guard.
 */

import { apiFetch, TOKEN_KEY, REFRESH_KEY, USER_KEY, redirectToLogin, ApiError } from "./api.js";

export { TOKEN_KEY, REFRESH_KEY, USER_KEY };

/** Current stored user object (sync read) or null. */
export function getUser() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    const user = JSON.parse(raw);
    return user && user.id ? user : null;
  } catch (err) {
    return null;
  }
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || null;
}

export function isAuthenticated() {
  return Boolean(getToken() && getUser());
}

/** Persist an AuthResponse-ish payload: { token, refreshToken, user }. */
export function saveSession({ token, refreshToken, user }) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken);
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_KEY);
  localStorage.removeItem(USER_KEY);
}

export async function login(email, password) {
  const data = await apiFetch("/auth/login", {
    method: "POST",
    body: { email, password },
  });
  saveSession(data);
  return data.user;
}

export async function register(name, email, password) {
  const data = await apiFetch("/auth/register", {
    method: "POST",
    body: { name, email, password },
  });
  saveSession(data);
  return data.user;
}

export async function logout() {
  clearSession();
  if (window.location.pathname.includes("/admin/")) {
    window.location.assign("../index.html");
  } else {
    window.location.assign(window.location.pathname);
  }
}

/** Home path depending on whether we are under /admin/. */
export function homePath() {
  return window.location.pathname.includes("/admin/") ? "../index.html" : "index.html";
}

/** Login path depending on whether we are under /admin/. */
export function loginPath() {
  return window.location.pathname.includes("/admin/") ? "../login.html" : "login.html";
}

/**
 * Admin guard for pages under /admin/. Verifies the Bearer session via
 * /api/auth/me and that role === 'ADMIN'.
 * - 401 → apiFetch clears the session and redirects to login.html.
 * - Non-admin → redirected to the public home page.
 * @returns {Promise<object|null>} the admin user, or null if redirected.
 */
export async function guardAdmin() {
  let user;
  try {
    user = await apiFetch("/auth/me", { auth: true });
  } catch (err) {
    // 401 already redirected to login.html via apiFetch.
    if (err instanceof ApiError && err.status === 401) return null;
    // Server unreachable: fall back to the stored session; guard still applies.
    user = getUser();
    if (!user) {
      window.location.assign(loginPath());
      return null;
    }
  }

  if (!user || user.role !== "ADMIN") {
    window.location.assign(homePath());
    return null;
  }
  return user;
}

/**
 * Moderator guard for content-moderation pages (admin/posts.html, admin/projects.html).
 * ADMIN and CORE_MEMBER pass; MEMBER/guests are redirected home.
 * @returns {Promise<object|null>} the moderator user, or null if redirected.
 */
export async function guardModerator() {
  let user;
  try {
    user = await apiFetch("/auth/me", { auth: true });
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) return null;
    user = getUser();
    if (!user) {
      window.location.assign(loginPath());
      return null;
    }
  }
  if (!user || (user.role !== "ADMIN" && user.role !== "CORE_MEMBER")) {
    window.location.assign(homePath());
    return null;
  }
  return user;
}
/**
 * Member guard for community-style actions. Returns the user or null and,
 * when not authenticated, redirects to login.html keeping a `next` target.
 */
export async function requireLogin() {
  if (!getToken()) {
    redirectToLogin(window.location.pathname.replace(/^\//, ""));
    return null;
  }
  if (cached) return cached;
  try {
    const user = await apiFetch("/auth/me", { auth: true });
    return user;
  } catch (err) {
    return null; // apiFetch redirected on 401
  }
}
