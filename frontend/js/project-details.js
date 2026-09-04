/**
 * Apex Innovators — project-details.js (project-details.html)
 * Single published project by slug. GET /projects/{slug}.
 * Robust handling of missing optional fields + not-found/error states.
 */

import { apiFetch, errorMessage } from "./api.js?v=2";
import {
  esc, icon, avatar, formatDate, humanize,
  mountLoading, mountData, mountEmpty, mountError,
} from "./components.js";
import { techChipsHTML } from "./cards.js";

const root = document.getElementById("project-detail");

function missing(text) {
  return `<p class="faint" style="font-size:0.92rem;">${esc(text)}</p>`;
}

function urlButton(label, url, iconName, primary) {
  if (!url) return "";
  return `<a class="btn ${primary ? "btn-primary" : "btn-outline"}" href="${esc(url)}" target="_blank" rel="noopener noreferrer">
    ${icon(iconName)} ${esc(label)}
  </a>`;
}

function render(project) {
  const techs = Array.isArray(project.technologies) ? project.technologies : [];
  const members = Array.isArray(project.members) ? project.members : [];
  const hackathons = Array.isArray(project.hackathons) ? project.hackathons : [];

  const memberList = members.length
    ? `<div class="member-list">${members.map((m) => `
        <div class="member-line">
          ${avatar(m.name, "", "avatar-sm avatar-round")}
          <div class="who">
            <b>${esc(m.name)}</b>
            <span>${esc(m.role || humanize("Member"))}</span>
          </div>
          ${m.contribution ? `<span class="contrib">${esc(m.contribution)}</span>` : ""}
        </div>`).join("")}</div>`
    : missing("No members linked to this project yet.");

  const hackList = hackathons.length
    ? `<div class="link-row">${hackathons.map((h) => `
        <a class="verify-link" href="hackathons.html">${icon("trophy")} ${esc(h.name || `Hackathon #${h.id}`)}</a>`).join("")}</div>
      <p class="faint" style="font-size:0.8rem;">Opens the hackathon archive.</p>`
    : missing("This project was not associated with a hackathon.");

  const buttons = [
    urlButton("Source code", project.githubUrl, "github", false),
    urlButton("Live demo", project.demoUrl, "external", true),
    urlButton("Documentation", project.docsUrl, "book", false),
  ].join("");

  mountData(root, `
    <nav class="crumbs" aria-label="Breadcrumb">
      <a href="index.html">Home</a><span class="sep">/</span>
      <a href="projects.html">Projects</a><span class="sep">/</span>
      <span class="current">${esc(project.title || "Project")}</span>
    </nav>

    <article class="card detail-hero" style="margin-bottom:1.5rem;">
      <div class="eyebrow">Project${project.year ? ` · ${esc(project.year)}` : ""}</div>
      <h1>${esc(project.title || "Untitled project")}</h1>
      ${project.tagline ? `<p class="section-sub">${esc(project.tagline)}</p>` : ""}
      ${techs.length ? techChipsHTML(techs) : ""}
      ${buttons ? `<div class="link-row" style="margin-top:0.5rem;">${buttons}</div>` : ""}
    </article>

    <div class="detail-grid">
      <div class="detail-main">
        <section class="card rich-block">
          <h2><span class="num">01</span>Overview</h2>
          ${project.description ? `<p>${esc(project.description)}</p>` : missing("No overview has been written for this project yet.")}
        </section>
        <section class="card rich-block">
          <h2><span class="num">02</span>The problem</h2>
          ${project.problem ? `<p>${esc(project.problem)}</p>` : missing("The problem statement has not been documented yet.")}
        </section>
        <section class="card rich-block">
          <h2><span class="num">03</span>Our solution</h2>
          ${project.solution ? `<p>${esc(project.solution)}</p>` : missing("The solution write-up has not been added yet.")}
        </section>
      </div>

      <aside class="detail-side">
        <div class="card side-card">
          <h3>Quick links</h3>
          ${project.githubUrl ? `<p class="card-meta">${icon("github")} <a href="${esc(project.githubUrl)}" target="_blank" rel="noopener noreferrer">GitHub repository</a></p>` : ""}
          ${project.demoUrl ? `<p class="card-meta">${icon("external")} <a href="${esc(project.demoUrl)}" target="_blank" rel="noopener noreferrer">Live demo</a></p>` : ""}
          ${project.docsUrl ? `<p class="card-meta">${icon("book")} <a href="${esc(project.docsUrl)}" target="_blank" rel="noopener noreferrer">Documentation</a></p>` : ""}
          ${!project.githubUrl && !project.demoUrl && !project.docsUrl ? `<p class="faint" style="font-size:0.86rem;">No external links published yet.</p>` : ""}
        </div>

        <div class="card side-card">
          <h3>Details</h3>
          <dl class="kv">
            <dt>Year</dt><dd>${project.year ? esc(project.year) : "—"}</dd>
            <dt>Status</dt><dd>${project.featured ? `<span class="badge b-featured">${icon("star")} Featured</span>` : "Published"}</dd>
            ${project.createdAt ? `<dt>Added</dt><dd>${esc(formatDate(project.createdAt))}</dd>` : ""}
          </dl>
        </div>

        <div class="card side-card">
          <h3>Team · ${members.length}</h3>
          ${memberList}
        </div>

        <div class="card side-card">
          <h3>Hackathon</h3>
          ${hackList}
        </div>
      </aside>
    </div>`);
}

async function load() {
  if (!root) return;
  const slug = new URLSearchParams(window.location.search).get("slug");
  if (!slug) {
    mountError(root, "Missing project slug. Open a project from the archive.", null);
    return;
  }
  mountLoading(root, "detail", 1);
  try {
    const project = await apiFetch(`/projects/${encodeURIComponent(slug)}`);
    render(project);
  } catch (err) {
    const status = err && err.status;
    if (status === 404 || status === 403) {
      mountEmpty(root, {
        title: "Project not found",
        message: "This project does not exist or is not publicly visible yet.",
        actionLabel: "Browse all projects",
        actionHref: "projects.html",
        iconName: "search",
      });
    } else {
      mountError(root, errorMessage(err, "Could not load this project."), load);
    }
  }
}

load();
