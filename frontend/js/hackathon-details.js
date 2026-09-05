/**
 * Apex Innovators — hackathon-details.js (hackathon-details.html)
 * Single hackathon story by slug. GET /hackathons/{slug}.
 */

import { apiFetch, errorMessage } from "./api.js";
import {
  esc, icon, avatar, formatDate,
  mountLoading, mountData, mountEmpty, mountError,
} from "./components.js";

const root = document.getElementById("hackathon-detail");

function missing(text) {
  return `<p class="faint" style="font-size:0.92rem;">${esc(text)}</p>`;
}

function updateHackathonSEO(h) {
  if (!h || !h.name) return;
  const pageTitle = `${h.name} — Apex Innovators Hackathon`;
  const desc = h.challenge || h.description || `Read about Apex Innovators competing in ${h.name}.`;
  document.title = pageTitle;

  const setMeta = (selector, attr, val) => {
    let el = document.querySelector(selector);
    if (!el) {
      el = document.createElement("meta");
      const parts = selector.split("=");
      const key = parts[0].replace("[", "");
      const prop = parts[1].replace("]", "").replace(/"/g, "");
      el.setAttribute(key, prop);
      document.head.appendChild(el);
    }
    el.setAttribute(attr, val);
  };

  setMeta('meta[name="description"]', "content", desc);
  setMeta('meta[property="og:title"]', "content", pageTitle);
  setMeta('meta[property="og:description"]', "content", desc);
  setMeta('meta[name="twitter:title"]', "content", pageTitle);
  setMeta('meta[name="twitter:description"]', "content", desc);

  let ld = document.getElementById("hackathon-schema-ld");
  if (!ld) {
    ld = document.createElement("script");
    ld.id = "hackathon-schema-ld";
    ld.type = "application/ld+json";
    document.head.appendChild(ld);
  }

  const schema = {
    "@context": "https://schema.org",
    "@type": "Event",
    "name": h.name,
    "description": desc,
    "organizer": {
      "@type": "Organization",
      "name": h.organizer || "Hackathon Organizer"
    },
    "performer": {
      "@type": "Organization",
      "name": "Apex Innovators",
      "url": "https://sky-ydv2008.github.io/Team.Apex/"
    }
  };
  if (h.date) schema.startDate = h.date;
  ld.textContent = JSON.stringify(schema);
}

function render(h) {
  updateHackathonSEO(h);
  const members = Array.isArray(h.members) ? h.members : [];
  const projects = Array.isArray(h.projects) ? h.projects : [];
  const date = h.date ? formatDate(h.date) : "";

  const memberList = members.length
    ? `<div class="member-list">${members.map((m) => `
        <div class="member-line">
          ${avatar(m.name, "", "avatar-sm avatar-round")}
          <div class="who"><b>${esc(m.name)}</b></div>
        </div>`).join("")}</div>`
    : missing("No team members linked to this entry yet.");

  const projectList = projects.length
    ? `<div class="link-row">${projects.map((p) => `
        <a class="verify-link" href="project-details.html?slug=${encodeURIComponent(p.slug || "")}">${icon("layers")} ${esc(p.title || `Project #${p.id}`)}</a>`).join("")}</div>`
    : missing("No projects linked from this hackathon yet.");

  mountData(root, `
    <nav class="crumbs" aria-label="Breadcrumb">
      <a href="index.html">Home</a><span class="sep">/</span>
      <a href="hackathons.html">Hackathons</a><span class="sep">/</span>
      <span class="current">${esc(h.name || "Hackathon")}</span>
    </nav>

    <article class="card detail-hero" style="margin-bottom:1.5rem;">
      <div class="eyebrow">Hackathon story</div>
      <h1>${esc(h.name || "Untitled hackathon")}</h1>
      ${h.organizer || date ? `<p class="section-sub" style="display:flex;gap:0.4rem;flex-wrap:wrap;">
        ${h.organizer ? `<span class="chip">${esc(h.organizer)}</span>` : ""}
        ${date ? `<span class="chip">${icon("calendar")} ${esc(date)}</span>` : ""}
        ${h.result ? `<span class="chip chip-featured">${esc(h.result)}</span>` : ""}
      </p>` : ""}
      ${h.certificateUrl || h.presentationUrl ? `<div class="link-row" style="margin-top:0.75rem;">
        ${h.certificateUrl ? `<a class="btn btn-primary" href="${esc(h.certificateUrl)}" target="_blank" rel="noopener noreferrer">${icon("award")} Certificate</a>` : ""}
        ${h.presentationUrl ? `<a class="btn btn-outline" href="${esc(h.presentationUrl)}" target="_blank" rel="noopener noreferrer">${icon("external")} Presentation</a>` : ""}
      </div>` : ""}
    </article>

    <div class="detail-grid">
      <div class="detail-main">
        <section class="card rich-block">
          <h2><span class="num">01</span>The challenge</h2>
          ${h.challenge ? `<p>${esc(h.challenge)}</p>` : missing("The challenge brief has not been documented.")}
        </section>
        <section class="card rich-block">
          <h2><span class="num">02</span>How it went</h2>
          ${h.description ? `<p>${esc(h.description)}</p>` : missing("The full story has not been written yet.")}
        </section>
      </div>

      <aside class="detail-side">
        <div class="card side-card">
          <h3>At a glance</h3>
          <dl class="kv">
            ${date ? `<dt>Date</dt><dd>${esc(date)}</dd>` : ""}
            ${h.organizer ? `<dt>Organizer</dt><dd>${esc(h.organizer)}</dd>` : ""}
            <dt>Result</dt><dd>${h.result ? esc(h.result) : "—"}</dd>
          </dl>
        </div>

        <div class="card side-card">
          <h3>Team · ${members.length}</h3>
          ${memberList}
        </div>

        <div class="card side-card">
          <h3>Projects · ${projects.length}</h3>
          ${projectList}
        </div>
      </aside>
    </div>`);
}

async function load() {
  if (!root) return;
  const slug = new URLSearchParams(window.location.search).get("slug");
  if (!slug) {
    mountError(root, "Missing hackathon slug. Open an entry from the archive.", null);
    return;
  }
  mountLoading(root, "detail", 1);
  try {
    const h = await apiFetch(`/hackathons/${encodeURIComponent(slug)}`);
    render(h);
  } catch (err) {
    if (err && (err.status === 404 || err.status === 403)) {
      mountEmpty(root, {
        title: "Hackathon entry not found",
        message: "This story does not exist or is not publicly visible yet.",
        actionLabel: "Browse the archive",
        actionHref: "hackathons.html",
        iconName: "search",
      });
    } else {
      mountError(root, errorMessage(err, "Could not load this hackathon story."), load);
    }
  }
}

load();
