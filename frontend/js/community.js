/**
 * Apex Innovators — community.js (community.html)
 * Published post feed (GET /posts?page=&size=&type=) with type chips,
 * login-gated compose + submit flow, likes and expandable comments.
 */

import { apiFetch, errorMessage } from "./api.js";
import { getUser } from "./auth.js";
import {
  esc, icon, avatar, humanize, typeChip, timeAgo,
  mountLoading, mountData, mountEmpty, mountError, renderPagination, pageInfo, toast,
} from "./components.js";

const POST_TYPES = ["DISCUSSION", "PROJECT", "ACHIEVEMENT", "HACKATHON", "RESOURCE", "QUESTION", "ANNOUNCEMENT"];

const SIZE = 10;
const state = { page: 0, type: "" };

const feedEl = document.getElementById("posts-feed");
const pagerEl = document.getElementById("posts-pager");
const infoEl = document.getElementById("feed-info");
const typeFiltersEl = document.getElementById("type-filters");
const composeArea = document.getElementById("compose-area");

/* ---------------- Feed ---------------- */
function postCardHTML(p) {
  return `<article class="card post-card" data-post="${p.id}">
    <div class="post-top">
      <div class="post-author">
        ${avatar(p.authorName, "", "avatar-sm avatar-round")}
        <span class="who"><b>${esc(p.authorName || "Apex member")}</b><time datetime="${esc(p.createdAt || "")}">${timeAgo(p.createdAt) || ""}</time></span>
      </div>
      ${typeChip(p.type)}
    </div>
    <h3 class="post-title">${esc(p.title || "Untitled post")}</h3>
    ${p.body ? `<div class="post-body">${esc(String(p.body))}</div>` : ""}
    <div class="post-foot">
      <button class="post-action${p.likedByMe ? " liked" : ""}" type="button" data-action="like" data-id="${p.id}" aria-pressed="${p.likedByMe ? "true" : "false"}">
        ${icon("heart")} <span data-like-count>${Number(p.likeCount || 0)}</span>
      </button>
      <button class="post-action" type="button" data-action="toggle-comments" data-id="${p.id}" aria-expanded="false">
        ${icon("message")} Comments <span data-comment-count>${Number(p.commentCount || 0)}</span>
      </button>
    </div>
    <div class="comments" data-comments hidden></div>
  </article>`;
}

async function load() {
  if (!feedEl) return;
  mountLoading(feedEl, "feed", 4);
  const params = { page: state.page, size: SIZE };
  if (state.type) params.type = state.type;
  try {
    const data = await apiFetch("/posts", { params });
    const items = (data && data.content) || [];
    if (infoEl) infoEl.textContent = pageInfo(data);

    if (!items.length) {
      const msg = state.type
        ? { title: "No posts in this category yet", message: "Be the first to start a conversation here.", iconName: "message" }
        : { title: "The feed is quiet… for now", message: "Start the first conversation — share a project, an insight or a question.", iconName: "message" };
      mountEmpty(feedEl, msg);
    } else {
      mountData(feedEl, items.map(postCardHTML).join(""));
    }
    renderPagination(pagerEl, data, (p) => { state.page = p; load(); });
  } catch (err) {
    mountError(feedEl, errorMessage(err, "Could not load community posts."), load);
    if (pagerEl) pagerEl.innerHTML = "";
  }
}

function renderTypeFilters() {
  if (!typeFiltersEl) return;
  const all = `<button class="chip chip-filter${!state.type ? " active" : ""}" type="button" data-type="">All</button>`;
  const chips = POST_TYPES.map((t) =>
    `<button class="chip chip-filter${state.type === t ? " active" : ""}" type="button" data-type="${t}">${esc(humanize(t))}</button>`
  ).join("");
  typeFiltersEl.innerHTML = all + chips;
  typeFiltersEl.querySelectorAll("[data-type]").forEach((btn) => {
    btn.addEventListener("click", () => {
      state.type = btn.dataset.type;
      state.page = 0;
      renderTypeFilters();
      load();
    });
  });
}

/* ---------------- Feed interactions (delegated) ---------------- */
async function toggleComments(card) {
  const box = card.querySelector("[data-comments]");
  const toggleBtn = card.querySelector('[data-action="toggle-comments"]');
  const id = card.dataset.post;
  if (!box) return;

  if (!box.hidden) {
    box.hidden = true;
    toggleBtn?.setAttribute("aria-expanded", "false");
    return;
  }

  box.hidden = false;
  toggleBtn?.setAttribute("aria-expanded", "true");
  if (box.dataset.loaded) return;
  await loadCommentsInto(box, id);
}

async function loadCommentsInto(box, id) {
  box.dataset.loaded = "1";
  box.innerHTML = `<p class="muted faint" style="font-size:0.85rem;">Loading comments…</p>`;

  try {
    const comments = await apiFetch(`/posts/${id}/comments`);
    const items = Array.isArray(comments) ? comments : (comments && comments.content) || [];
    box.innerHTML = renderComments(items, id);
    wireCommentForm(box, id);
  } catch (err) {
    box.dataset.loaded = "0";
    box.innerHTML = `<p class="state-text" style="font-size:0.85rem;">${esc(errorMessage(err, "Could not load comments."))}</p>`;
  }
}

function renderComments(items, postId) {
  const user = getUser();
  const list = items.length
    ? items.map((c) => `
        <div class="comment-item">
          ${avatar(c.authorName, "", "avatar-round")}
          <div class="comment-bubble">
            <span class="who"><b>${esc(c.authorName || "Member")}</b><time datetime="${esc(c.createdAt || "")}">${timeAgo(c.createdAt) || ""}</time></span>
            <p>${esc(c.body)}</p>
          </div>
        </div>`).join("")
    : `<p class="faint" style="font-size:0.85rem;">No comments yet. Start the discussion!</p>`;

  const form = user
    ? `<form class="comment-form" data-comment-form data-post="${postId}">
        <textarea class="input" rows="2" placeholder="Write a comment…" required minlength="1" maxlength="2000" aria-label="Comment text"></textarea>
        <div style="display:flex;justify-content:flex-end;margin-top:0.5rem;">
          <button class="btn btn-sm btn-primary" type="submit">${icon("send")} Comment</button>
        </div>
      </form>`
    : `<p class="faint" style="font-size:0.85rem;"><a href="login.html">Log in</a> to join the conversation.</p>`;

  return `<div style="display:grid;gap:0.9rem;">${list}${form}</div>`;
}

function wireCommentForm(box, postId) {
  const form = box.querySelector("[data-comment-form]");
  if (!form) return;
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const textarea = form.querySelector("textarea");
    const body = textarea.value.trim();
    if (!body) return;
    const btn = form.querySelector("button[type=submit]");
    btn.disabled = true;
    try {
      await apiFetch(`/posts/${postId}/comments`, { method: "POST", body: { body }, auth: true });
      textarea.value = "";
      toast("Comment posted", "success");
      const count = box.closest("[data-post]")?.querySelector("[data-comment-count]");
      if (count) count.textContent = String(Number(count.textContent) + 1);
      // Refresh the open thread so the new comment appears.
      await loadCommentsInto(box, postId);
    } catch (err) {
      toast(errorMessage(err, "Could not post the comment."), "error");
    } finally {
      btn.disabled = false;
    }
  });
}

async function handleLike(btn, id) {
  const user = getUser();
  if (!user || !localStorage.getItem("ai_token")) {
    toast("Log in to like posts", "info");
    return;
  }
  const wasLiked = btn.classList.contains("liked");
  btn.disabled = true;
  try {
    if (wasLiked) {
      await apiFetch(`/posts/${id}/like`, { method: "DELETE", auth: true });
    } else {
      await apiFetch(`/posts/${id}/like`, { method: "POST", auth: true });
    }
    btn.classList.toggle("liked", !wasLiked);
    btn.setAttribute("aria-pressed", String(!wasLiked));
    const counter = btn.querySelector("[data-like-count]");
    if (counter) counter.textContent = String(Math.max(0, Number(counter.textContent) + (wasLiked ? -1 : 1)));
  } catch (err) {
    toast(errorMessage(err, "Could not update the like."), "error");
  } finally {
    btn.disabled = false;
  }
}

function wireFeedEvents() {
  if (!feedEl) return;
  feedEl.addEventListener("click", async (e) => {
    const likeBtn = e.target.closest('[data-action="like"]');
    if (likeBtn) { handleLike(likeBtn, likeBtn.dataset.id); return; }

    const commentsBtn = e.target.closest('[data-action="toggle-comments"]');
    if (commentsBtn) {
      const card = commentsBtn.closest("[data-post]");
      if (card) await toggleComments(card);
    }
  });
}

/* ---------------- Compose (login-gated) ---------------- */
function wireCompose() {
  if (!composeArea) return;
  const user = getUser();

  if (!user || !localStorage.getItem("ai_token")) {
    composeArea.innerHTML = `<div class="notice">
      <span class="notice-icon">${icon("message")}</span>
      <div>
        <h3>Join the conversation</h3>
        <p>Share projects, achievements and questions with the community. Log in or create a free account to post.</p>
        <div style="display:flex;gap:0.7rem;flex-wrap:wrap;">
          <a class="btn btn-primary btn-sm" href="login.html">Log in</a>
          <a class="btn btn-outline btn-sm" href="register.html">Create account</a>
        </div>
      </div>
    </div>`;
    return;
  }

  composeArea.innerHTML = `
    <form id="post-form" class="card card-pad form" novalidate>
      <div class="post-top">
        <div class="post-author">
          ${avatar(user.name, "", "avatar-sm avatar-round")}
          <span class="who"><b>${esc(user.name)}</b><span class="muted faint" style="font-size:0.78rem;">Posting as a team member</span></span>
        </div>
        <span class="badge b-member">${esc(humanize(user.role || "member"))}</span>
      </div>
      <div class="form-grid">
        <div class="field">
          <label for="post-type">Type</label>
          <select class="input" id="post-type">
            ${POST_TYPES.map((t) => `<option value="${t}">${esc(humanize(t))}</option>`).join("")}
          </select>
        </div>
        <div class="field">
          <label for="post-title">Title <span class="req">*</span></label>
          <input class="input" id="post-title" type="text" maxlength="190" placeholder="Give your post a clear title" required>
        </div>
      </div>
      <div class="field">
        <label for="post-body">What do you want to share? <span class="req">*</span></label>
        <textarea class="input" id="post-body" rows="4" maxlength="10000" placeholder="Tell the story — what you built, learned or are asking about…" required></textarea>
      </div>
      <div class="form-actions">
        <p class="faint" style="font-size:0.82rem;">Posts are reviewed before they appear in the public feed.</p>
        <span class="spacer"></span>
        <button class="btn btn-primary" type="submit">${icon("send")} Publish post</button>
      </div>
      <div id="compose-alert"></div>
    </form>`;

  const form = document.getElementById("post-form");
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const title = document.getElementById("post-title").value.trim();
    const body = document.getElementById("post-body").value.trim();
    const type = document.getElementById("post-type").value;
    if (!title || !body) {
      toast("Title and message are required", "warning");
      return;
    }
    const submitBtn = form.querySelector("button[type=submit]");
    submitBtn.disabled = true;
    submitBtn.innerHTML = `${icon("clock")} Submitting…`;
    try {
      const created = await apiFetch("/posts", { method: "POST", body: { type, title, body }, auth: true });
      try {
        await apiFetch(`/posts/${created.id}/submit`, { method: "POST", auth: true });
        toast("Post sent for review — it appears in the feed once published.", "success");
      } catch (err) {
        toast("Post saved as a draft. It needs review before appearing publicly.", "info");
      }
      form.reset();
      state.page = 0;
      load();
    } catch (err) {
      const alertEl = document.getElementById("compose-alert");
      if (alertEl) alertEl.innerHTML = `<div class="alert alert-error">${icon("alert")}<span>${esc(errorMessage(err, "Could not publish the post."))}</span></div>`;
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = `${icon("send")} Publish post`;
    }
  });
}

/* ---------------- Boot ---------------- */
renderTypeFilters();
wireCompose();
wireFeedEvents();
load();
