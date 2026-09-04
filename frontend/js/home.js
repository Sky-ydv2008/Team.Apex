/**
 * Apex Innovators — home.js (index.html)
 * Dynamic blocks: hero stats (/public/stats), featured projects
 * (/projects?featured=true&size=3), tech ecosystem (/technologies with
 * curated fallback), achievements wall, team preview (/team).
 */

import { apiFetch, errorMessage } from "./api.js?v=2";
import { esc, mountLoading, mountData, mountEmpty, mountError } from "./components.js";
import { projectCardHTML, memberCardHTML, achievementWallItem } from "./cards.js";
import { STATIC_TECHNOLOGIES } from "./static.js";

/* ---------- 1. Hero stats ---------- */
async function loadStats() {
  const host = document.getElementById("hero-stats");
  if (!host) return;
  host.innerHTML = Array.from({ length: 5 }, () =>
    `<div class="hero-stat"><div class="sk sk-line w50"></div><div class="sk sk-line w70" style="height:0.6rem;"></div></div>`
  ).join("");

  const STATS = [
    { key: "projects", label: "Projects" },
    { key: "hackathons", label: "Hackathons" },
    { key: "members", label: "Members" },
    { key: "technologies", label: "Technologies" },
    { key: "achievements", label: "Achievements" },
  ];

  try {
    const stats = await apiFetch("/public/stats");
    host.innerHTML = STATS.map((s) => {
      const value = stats && stats[s.key] !== undefined && stats[s.key] !== null ? stats[s.key] : "–";
      return `<div class="hero-stat">
        <strong class="stat-num">${typeof value === "number" ? value.toLocaleString("en-US") : esc(String(value))}</strong>
        <span class="stat-label">${esc(s.label)}</span>
      </div>`;
    }).join("");
  } catch (err) {
    host.innerHTML = STATS.map((s) =>
      `<div class="hero-stat"><strong class="stat-num">–</strong><span class="stat-label">${esc(s.label)}</span></div>`
    ).join("");
  }
}

/* ---------- 2. Featured projects ---------- */
async function loadFeatured() {
  const host = document.getElementById("featured-projects");
  if (!host) return;
  mountLoading(host, "card", 3);

  const render = (content) => {
    if (!content.length) {
      mountEmpty(host, {
        title: "No featured projects yet",
        message: "The team is still shipping. Check the full project archive.",
        actionLabel: "Browse projects",
        actionHref: "projects.html",
        iconName: "layers",
      });
      return;
    }
    mountData(host, `<div class="grid g3">${content.slice(0, 3).map(projectCardHTML).join("")}</div>`);
  };

  const tryLoad = async () => {
    try {
      mountLoading(host, "card", 3);
      const page = await apiFetch("/projects", { params: { featured: "true", size: 3, page: 0 } });
      const items = (page && page.content) || [];
      if (items.length) {
        render(items);
      } else {
        const fallback = await apiFetch("/projects", { params: { size: 3, page: 0 } });
        render((fallback && fallback.content) || []);
      }
    } catch (err) {
      mountError(host, errorMessage(err, "Could not load featured projects."), loadFeatured);
    }
  };

  await tryLoad();
}

/* ---------- 3. Tech ecosystem ---------- */
const FALLBACK_TECH = STATIC_TECHNOLOGIES;

async function loadTech() {
  const host = document.getElementById("tech-cloud");
  if (!host) return;
  mountLoading(host, "card", 1);

  let list = null;
  try {
    const res = await apiFetch("/technologies");
    if (Array.isArray(res)) list = res;
    else if (res && Array.isArray(res.content)) list = res.content;
    else if (res && Array.isArray(res.technologies)) list = res.technologies;
  } catch (err) {
    list = null; // endpoint is admin-protected or missing → curated fallback
  }

  const chips = (list || FALLBACK_TECH)
    .filter((t) => t && t.name)
    .map((t) => {
      const cat = t.category ? `<span class="t-cat">${esc(t.category)}</span>` : "";
      return `<span class="tech-chip">${esc(t.name)}${cat}</span>`;
    })
    .join("");

  mountData(host, chips || `<p class="muted center">Our technology stack is still taking shape — check back soon.</p>`);
}

/* ---------- 4. Achievements wall ---------- */
async function loadWall() {
  const host = document.getElementById("ach-wall");
  if (!host) return;
  mountLoading(host, "card", 6);

  try {
    const page = await apiFetch("/achievements", { params: { page: 0, size: 6 } });
    const items = (page && page.content) || [];
    if (!items.length) {
      mountEmpty(host, {
        title: "No achievements yet",
        message: "Awards and milestones will appear here as we collect them.",
        iconName: "award",
      });
      return;
    }
    mountData(host, `<div class="wall-grid">${items.slice(0, 6).map(achievementWallItem).join("")}</div>`);
  } catch (err) {
    mountError(host, errorMessage(err, "Could not load achievements."), loadWall);
  }
}

/* ---------- 5. Team preview ---------- */
async function loadTeam() {
  const host = document.getElementById("team-preview");
  if (!host) return;
  mountLoading(host, "card", 4);

  try {
    const team = await apiFetch("/team");
    const items = Array.isArray(team) ? team : (team && team.content) || [];
    if (!items.length) {
      mountEmpty(host, {
        title: "Meet the team soon",
        message: "Member profiles are published as they join the roster.",
        actionLabel: "About the team",
        actionHref: "about.html",
        iconName: "users",
      });
      return;
    }
    mountData(host, `<div class="grid g4">${items.slice(0, 4).map(memberCardHTML).join("")}</div>`);
  } catch (err) {
    mountError(host, errorMessage(err, "Could not load the team."), loadTeam);
  }
}

/* ---------- Boot ---------- */
async function boot() {
  loadStats();
  await loadFeatured();
  await loadTech();
  await loadWall();
  await loadTeam();
}

boot();
