/**
 * Apex Innovators — projects.js (projects.html)
 * Published project archive with q / year / tech filters + pagination.
 * GET /projects?page=&size=&q=&year=&tech=
 */

import { apiFetch, errorMessage } from "./api.js";
import { mountLoading, mountData, mountEmpty, mountError, renderPagination, pageInfo, esc } from "./components.js";
import { projectCardHTML } from "./cards.js";
import { STATIC_TECH_NAMES } from "./static.js";

const SIZE = 12;
const state = { page: 0, q: "", year: "", tech: "" };

const listEl = document.getElementById("projects-list");
const pagerEl = document.getElementById("projects-pager");
const infoEl = document.getElementById("results-info");

function currentYear() { return new Date().getFullYear(); }

function initFilters() {
  const yearEl = document.getElementById("filter-year");
  const techEl = document.getElementById("filter-tech");
  if (yearEl) {
    const start = currentYear() - 7;
    let html = `<option value="">All years</option>`;
    for (let y = currentYear(); y >= start; y--) html += `<option value="${y}">${y}</option>`;
    yearEl.innerHTML = html;
  }
  if (techEl) {
    let html = `<option value="">All technologies</option>`;
    html += STATIC_TECH_NAMES.map((t) => `<option value="${esc(t)}">${esc(t)}</option>`).join("");
    techEl.innerHTML = html;
  }

  // Hydrate from the URL query, if present
  const qs = new URLSearchParams(window.location.search);
  const q = qs.get("q") || "";
  const year = qs.get("year") || "";
  const tech = qs.get("tech") || "";
  if (q) { state.q = q; const input = document.getElementById("filter-q"); if (input) input.value = q; }
  if (year) { state.year = year; if (yearEl) yearEl.value = year; }
  if (tech) { state.tech = tech; if (techEl) techEl.value = tech; }
}

async function load() {
  if (!listEl) return;
  mountLoading(listEl, "card", 6);

  const params = { page: state.page, size: SIZE, q: state.q || undefined, year: state.year || undefined, tech: state.tech || undefined };

  try {
    const data = await apiFetch("/projects", { params });
    const items = (data && data.content) || [];

    if (infoEl) infoEl.textContent = pageInfo(data);

    if (!items.length) {
      const hasFilters = Boolean(state.q || state.year || state.tech);
      mountEmpty(listEl, hasFilters ? {
        title: "No projects match your filters",
        message: "Try a different search term, year or technology.",
        actionLabel: "Clear filters",
        actionHref: "projects.html",
        iconName: "search",
      } : {
        title: "No projects published yet",
        message: "The team is still building. Follow the community feed for what ships next.",
        actionLabel: "Join the community",
        actionHref: "community.html",
        iconName: "layers",
      });
    } else {
      mountData(listEl, `<div class="grid g3">${items.map(projectCardHTML).join("")}</div>`);
    }

    renderPagination(pagerEl, data, (page) => { state.page = page; load(); });
  } catch (err) {
    mountError(listEl, errorMessage(err, "Could not load projects."), load);
    if (pagerEl) pagerEl.innerHTML = "";
  }
}

function syncUrl() {
  const qs = new URLSearchParams();
  if (state.q) qs.set("q", state.q);
  if (state.year) qs.set("year", state.year);
  if (state.tech) qs.set("tech", state.tech);
  const target = qs.toString() ? `?${qs.toString()}` : window.location.pathname;
  window.history.replaceState(null, "", target);
}

function wireEvents() {
  const qInput = document.getElementById("filter-q");
  const yearEl = document.getElementById("filter-year");
  const techEl = document.getElementById("filter-tech");

  let debounce = null;
  if (qInput) {
    qInput.addEventListener("input", () => {
      clearTimeout(debounce);
      debounce = setTimeout(() => {
        state.q = qInput.value.trim();
        state.page = 0;
        syncUrl();
        load();
      }, 320);
    });
  }
  if (yearEl) {
    yearEl.addEventListener("change", () => { state.year = yearEl.value; state.page = 0; syncUrl(); load(); });
  }
  if (techEl) {
    techEl.addEventListener("change", () => { state.tech = techEl.value; state.page = 0; syncUrl(); load(); });
  }
}

initFilters();
wireEvents();
load();
