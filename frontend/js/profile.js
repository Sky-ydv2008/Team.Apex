/**
 * Apex Innovators — profile.js (profile.html)
 * Signed-in members edit their own display profile (GET/PUT /api/auth/profile)
 * and change their password (PUT /api/auth/password). Requires login.
 */

import { apiFetch, errorMessage } from "./api.js";
import { requireLogin } from "./auth.js";
import {
  icon, esc, avatar, roleBadge, statusBadge, mountData, mountError, toast,
} from "./components.js";

const host = document.getElementById("profile-host");

const FIELD_LABELS = {
  name: "Display name",
  headline: "Headline",
  bio: "Bio",
  github: "GitHub",
  linkedin: "LinkedIn",
  photoUrl: "Photo URL",
};

function fieldHtml(id, label, value, opts = {}) {
  const { type = "text", placeholder = "", maxlength = "", rows = 0 } = opts;
  const control = rows > 0
    ? `<textarea class="input" id="pf-${id}" rows="${rows}" maxlength="${maxlength}" placeholder="${esc(placeholder)}">${esc(value || "")}</textarea>`
    : `<input class="input" id="pf-${id}" type="${type}" maxlength="${maxlength}" placeholder="${esc(placeholder)}" value="${esc(value || "")}">`;
  return `<div class="field">
    <label for="pf-${id}">${label}</label>
    ${control}
    ${opts.hint ? `<p class="hint">${opts.hint}</p>` : ""}
  </div>`;
}

function renderProfile(p) {
  const html = `
    <div class="card card-pad" style="margin-bottom:1.25rem;">
      <div style="display:flex;align-items:center;gap:1rem;flex-wrap:wrap;margin-bottom:1rem;">
        ${avatar(p.name, p.photoUrl, "avatar-round avatar-frame")}
        <div>
          <h2 style="margin-bottom:0.25rem;">${esc(p.name || "—")}</h2>
          <div style="display:flex;gap:0.4rem;flex-wrap:wrap;">${roleBadge(p.role)} ${statusBadge(p.status)}</div>
          <p class="muted" style="margin-top:0.4rem;">${esc(p.email || "")}</p>
        </div>
      </div>

      <form id="profile-form" class="form" novalidate>
        <div class="form-grid">
          ${fieldHtml("name", FIELD_LABELS.name, p.name, { maxlength: 120 })}
          ${fieldHtml("headline", FIELD_LABELS.headline, p.headline, { maxlength: 190, placeholder: "e.g. Backend Developer · Spring Boot" })}
        </div>
        ${fieldHtml("bio", FIELD_LABELS.bio, p.bio, { rows: 4, maxlength: 5000, placeholder: "Short profile shown on your team card and project pages." })}
        <div class="form-grid">
          ${fieldHtml("github", FIELD_LABELS.github, p.github, { maxlength: 190, placeholder: "username or profile URL" })}
          ${fieldHtml("linkedin", FIELD_LABELS.linkedin, p.linkedin, { maxlength: 190, placeholder: "profile URL" })}
        </div>
        ${fieldHtml("photoUrl", FIELD_LABELS.photoUrl, p.photoUrl, { maxlength: 500, placeholder: "https://… (square image works best)", hint: "Profile photos are plain image URLs — no uploads yet." })}
        <p class="form-error" id="profile-error" role="alert" hidden></p>
        <div style="display:flex;justify-content:flex-end;margin-top:0.5rem;">
          <button class="btn btn-primary" type="submit">Save profile</button>
        </div>
      </form>
    </div>

    <div class="card card-pad">
      <h2 style="margin-bottom:0.25rem;">Change password</h2>
      <p class="muted" style="margin-bottom:1rem;">You'll stay logged in; other sessions are invalidated on their next request.</p>
      <form id="password-form" class="form" novalidate>
        ${fieldHtml("current", "Current password", "", { type: "password", autocomplete: "current-password" })}
        <div class="form-grid">
          ${fieldHtml("next", "New password", "", { type: "password", maxlength: 72, autocomplete: "new-password", hint: "8–72 characters." })}
          ${fieldHtml("next2", "Repeat new password", "", { type: "password", maxlength: 72, autocomplete: "new-password" })}
        </div>
        <p class="form-error" id="password-error" role="alert" hidden></p>
        <div style="display:flex;justify-content:flex-end;margin-top:0.5rem;">
          <button class="btn btn-outline" type="submit">Update password</button>
        </div>
      </form>
    </div>`;
  mountData(host, html);

  document.getElementById("profile-form").addEventListener("submit", submitProfile);
  document.getElementById("password-form").addEventListener("submit", submitPassword);
}

function currentProfile() {
  return {
    name: document.getElementById("pf-name").value.trim(),
    headline: document.getElementById("pf-headline").value.trim(),
    bio: document.getElementById("pf-bio").value.trim(),
    github: document.getElementById("pf-github").value.trim(),
    linkedin: document.getElementById("pf-linkedin").value.trim(),
    photoUrl: document.getElementById("pf-photoUrl").value.trim(),
  };
}

async function submitProfile(e) {
  e.preventDefault();
  const errEl = document.getElementById("profile-error");
  const btn = e.target.querySelector("button[type=submit]");
  if (errEl) { errEl.textContent = ""; errEl.hidden = true; }
  btn.disabled = true;
  try {
    const updated = await apiFetch("/auth/profile", { method: "PUT", body: currentProfile(), auth: true });
    toast("Profile saved", "success");
    try {
      localStorage.setItem("ai_user", JSON.stringify({
        id: updated.id, name: updated.name, email: updated.email, role: updated.role,
      }));
    } catch (err) { /* storage full/unavailable — cosmetic only */ }
    // Refresh the nav (new display name) after the toast is visible.
    setTimeout(() => window.location.reload(), 700);
  } catch (err) {
    const msg = errorMessage(err, "Could not save the profile.");
    if (errEl) { errEl.textContent = msg; errEl.hidden = false; }
    else toast(msg, "error");
  } finally {
    btn.disabled = false;
  }
}

async function submitPassword(e) {
  e.preventDefault();
  const errEl = document.getElementById("password-error");
  const current = document.getElementById("pf-current").value;
  const next = document.getElementById("pf-next").value;
  const next2 = document.getElementById("pf-next2").value;
  const btn = e.target.querySelector("button[type=submit]");
  if (errEl) { errEl.textContent = ""; errEl.hidden = true; }
  if (next !== next2) {
    if (errEl) { errEl.textContent = "The new passwords do not match."; errEl.hidden = false; }
    return;
  }
  btn.disabled = true;
  try {
    await apiFetch("/auth/password", { method: "PUT", body: { currentPassword: current, newPassword: next }, auth: true });
    toast("Password updated", "success");
    e.target.reset();
  } catch (err) {
    const msg = errorMessage(err, "Could not update the password.");
    if (errEl) { errEl.textContent = msg; errEl.hidden = false; }
    else toast(msg, "error");
  } finally {
    btn.disabled = false;
  }
}

async function boot() {
  const user = await requireLogin();
  if (!user) { location.href = "login.html?next=profile.html"; return; }
  try {
    const p = await apiFetch("/auth/profile", { auth: true });
    renderProfile(p);
  } catch (err) {
    mountError(host, errorMessage(err, "Could not load your profile."), boot);
  }
}

boot();
