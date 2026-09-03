/**
 * Apex Innovators — cards.js
 * Shared entity card renderers for public pages. Field names follow the
 * contract DTOs exactly (ProjectDto, MemberDto, HackathonDto, EventDto).
 */

import { esc, icon, avatar, formatDate, dateBlock, roleBadge } from "./components.js";

/* ---------- Technology chips ---------- */
export function techChipsHTML(technologies, max = 99) {
  const techs = Array.isArray(technologies) ? technologies : [];
  if (!techs.length) return "";
  const visible = techs.slice(0, max);
  let html = `<div class="chip-row">${visible.map((t) => {
    const name = (t && (t.name || (typeof t === "string" ? t : ""))) || "";
    return name ? `<span class="chip chip-tech">${esc(name)}</span>` : "";
  }).join("")}`;
  if (techs.length > max) html += `<span class="chip">+${techs.length - max} more</span>`;
  html += `</div>`;
  return html;
}

/* ---------- External URL button (returns "" when no URL) ---------- */
function urlBtn(label, url, iconName, aria) {
  if (!url) return "";
  return `<a class="btn btn-sm btn-outline btn-icon" href="${esc(url)}" target="_blank" rel="noopener noreferrer" aria-label="${esc(aria || label)}" title="${esc(label)}">${icon(iconName)}</a>`;
}

/* ---------- Project card (ProjectDto) ---------- */
export function projectCardHTML(p, { showFeatured = true } = {}) {
  const detailHref = `project-details.html?slug=${encodeURIComponent(p.slug || "")}`;
  const members = Array.isArray(p.members) ? p.members : [];
  const memberNames = members.slice(0, 3).map((m) => m.name).filter(Boolean).join(", ");

  return `<article class="card card-hover project-card">
    <div class="card-tag-row">
      ${p.year ? `<span class="chip chip-year">${esc(p.year)}</span>` : ""}
      ${p.featured && showFeatured ? `<span class="chip chip-featured">${icon("star")} Featured</span>` : ""}
    </div>
    <h3 class="card-title"><a href="${detailHref}">${esc(p.title || "Untitled project")}</a></h3>
    <p class="card-text">${esc(p.tagline || p.description || "No description added yet.")}</p>
    ${techChipsHTML(p.technologies, 4)}
    ${memberNames ? `<p class="card-meta">${icon("users")} ${esc(memberNames)}</p>` : ""}
    <div class="card-actions">
      ${urlBtn("Source code on GitHub", p.githubUrl, "github", `GitHub repository for ${p.title}`)}
      ${urlBtn("Live demo", p.demoUrl, "external", `Live demo of ${p.title}`)}
      ${urlBtn("Documentation", p.docsUrl, "book", `Documentation for ${p.title}`)}
      <a class="link-btn" style="margin-left:auto;" href="${detailHref}">Details ${icon("arrow-right")}</a>
    </div>
  </article>`;
}

/* ---------- Member card (MemberDto) ---------- */
export function memberCardHTML(m) {
  const links = [
    m.github ? `<a class="social-btn" href="${esc(m.github)}" target="_blank" rel="noopener noreferrer" aria-label="GitHub profile of ${esc(m.name)}">${icon("github")}</a>` : "",
    m.linkedin ? `<a class="social-btn" href="${esc(m.linkedin)}" target="_blank" rel="noopener noreferrer" aria-label="LinkedIn profile of ${esc(m.name)}">${icon("link")}</a>` : "",
  ].join("");

  return `<article class="card card-hover member-card">
    <div class="member-head">
      ${avatar(m.name, m.photoUrl, "avatar-round avatar-frame")}
      <div>
        <h3 class="member-name">${esc(m.name || "Apex member")}</h3>
        <div class="member-role">${roleBadge(m.role)}</div>
      </div>
    </div>
    ${m.headline ? `<p class="muted" style="font-size:0.88rem;">${esc(m.headline)}</p>` : ""}
    ${m.bio ? `<p class="member-bio">${esc(m.bio)}</p>` : `<p class="member-bio faint">This member has not written a bio yet.</p>`}
    ${links ? `<div class="social-row">${links}</div>` : ""}
  </article>`;
}

/* ---------- Hackathon card (HackathonDto) ---------- */
export function hackathonCardHTML(h) {
  const href = `hackathon-details.html?slug=${encodeURIComponent(h.slug || "")}`;
  const members = Array.isArray(h.members) ? h.members : [];
  const projects = Array.isArray(h.projects) ? h.projects : [];
  const desc = h.description || h.challenge || "No description added yet.";

  return `<article class="card card-hover project-card">
    <div class="card-tag-row">
      ${h.date ? `<span class="chip">${icon("calendar")} ${esc(formatDate(h.date))}</span>` : `<span class="chip">Date TBA</span>`}
      ${h.organizer ? `<span class="chip">${esc(h.organizer)}</span>` : ""}
    </div>
    <h3 class="card-title"><a href="${href}">${esc(h.name || "Untitled hackathon")}</a></h3>
    <p class="card-text">${esc(truncateText(desc, 220))}</p>
    <p class="card-meta">
      ${members.length ? `<span>${icon("users")} ${members.length} team ${members.length === 1 ? "member" : "members"}</span>` : ""}
      ${projects.length ? `<span>${icon("layers")} ${projects.length} ${projects.length === 1 ? "project" : "projects"}</span>` : ""}
    </p>
    <div class="card-actions">
      <a class="link-btn" href="${href}">View story ${icon("arrow-right")}</a>
    </div>
  </article>`;
}

function truncateText(text, max) {
  const t = String(text || "");
  return t.length > max ? `${t.slice(0, max).trimEnd()}…` : t;
}

/* ---------- Event card (EventDto) ---------- */
export function eventCardHTML(e) {
  const block = dateBlock(e.date);
  const time = e.date ? formatTime(e.date) : "";
  const modeLabel = String(e.mode || "ONLINE");
  const toneCls = ({ ONLINE: "tc-emerald", OFFLINE: "tc-violet", HYBRID: "tc-amber" })[modeLabel] || "tc-slate";

  return `<article class="card event-card">
    <div class="event-date" aria-hidden="true">
      <b>${esc(block.day)}</b>
      <span>${esc(block.mon)}</span>
    </div>
    <div class="event-main">
      <div style="display:flex;flex-wrap:wrap;gap:0.5rem;align-items:center;">
        <h3>${esc(e.title || "Untitled event")}</h3>
        <span class="type-chip ${toneCls}">${esc(modeLabel.toLowerCase())}</span>
      </div>
      <div class="event-meta">
        ${e.date ? `<span>${icon("calendar")} ${esc(formatDate(e.date))}${time ? ` at ${esc(time)}` : ""}</span>` : `<span>${icon("calendar")} Date TBA</span>`}
        ${modeLabel !== "ONLINE" && e.location ? `<span>${icon("pin")} ${esc(e.location)}</span>` : ""}
        ${modeLabel === "ONLINE" ? `<span>${icon("globe")} Online</span>` : ""}
      </div>
      ${e.description ? `<p class="event-desc">${esc(e.description)}</p>` : ""}
    </div>
  </article>`;
}

function formatTime(value) {
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return "";
  return d.toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" });
}

/* ---------- Recent achievement wall item (AchievementDto) ---------- */
export function achievementWallItem(a) {
  return `<article class="wall-item">
      ${icon("award")}
    <div style="min-width:0;">
      <h4>${esc(a.title || "Achievement")}</h4>
      <p>${a.userName ? esc(a.userName) : "Apex member"}${a.issuer ? ` · ${esc(a.issuer)}` : ""}</p>
      <time datetime="${esc(a.awardDate || "")}">${esc(formatDate(a.awardDate))}</time>
    </div>
  </article>`;
}
