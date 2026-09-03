/**
 * Apex Innovators — achievements.js (achievements.html)
 * Achievements grouped into a year timeline with pagination.
 * GET /achievements?page=&size=  (AchievementDto list)
 */

import { apiFetch, errorMessage } from "./api.js";
import {
  esc, icon, formatDate, typeChip,
  mountLoading, mountData, mountEmpty, mountError, renderPagination, pageInfo,
} from "./components.js";

const SIZE = 24;
let page = 0;

const listEl = document.getElementById("achievements-list");
const pagerEl = document.getElementById("achievements-pager");
const infoEl = document.getElementById("results-info");

const DOT_BY_TYPE = {
  AWARD: "tl-dot-amber",
  FINALIST: "tl-dot-violet",
  CERTIFICATE: "tl-dot-emerald",
  MENTION: "tl-dot-slate",
};

function yearOf(value) {
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? null : d.getFullYear();
}

function itemHTML(a) {
  const dot = DOT_BY_TYPE[a.type] || "tl-dot-slate";
  const date = a.awardDate ? formatDate(a.awardDate) : "";
  return `<div class="tl-item ${dot}">
    <div class="card tl-card">
      <div class="tl-head">
        <h3 class="tl-title">${esc(a.title || "Achievement")}</h3>
        ${typeChip(a.type)}
      </div>
      ${a.userName || a.issuer ? `<div class="tl-meta">
        ${a.userName ? `<span>${icon("user")} ${esc(a.userName)}</span>` : ""}
        ${a.issuer ? `<span>${icon("trophy")} ${esc(a.issuer)}</span>` : ""}
        ${date ? `<span>${icon("calendar")} ${esc(date)}</span>` : ""}
      </div>` : ""}
      ${a.description ? `<p class="tl-desc">${esc(a.description)}</p>` : ""}
      ${a.verifyUrl ? `<div><a class="verify-link" href="${esc(a.verifyUrl)}" target="_blank" rel="noopener noreferrer">${icon("external")} Verify this achievement</a></div>` : ""}
    </div>
  </div>`;
}

function render(items) {
  const groups = new Map();
  for (const a of items) {
    const y = yearOf(a.awardDate) ?? "Undated";
    if (!groups.has(y)) groups.set(y, []);
    groups.get(y).push(a);
  }

  const years = [...groups.keys()].sort((a, b) => {
    if (a === "Undated") return 1;
    if (b === "Undated") return -1;
    return b - a;
  });

  mountData(listEl, `<div class="timeline">${years.map((y) => `
    <div class="tl-group">
      <div class="tl-year"><span>${esc(String(y))}</span><span class="tl-line"></span></div>
      <div class="tl-items">${groups.get(y).map(itemHTML).join("")}</div>
    </div>`).join("")}</div>`);
}

async function load() {
  if (!listEl) return;
  mountLoading(listEl, "feed", 3);
  try {
    const data = await apiFetch("/achievements", { params: { page, size: SIZE } });
    const items = (data && data.content) || [];
    if (infoEl) infoEl.textContent = pageInfo(data);

    if (!items.length) {
      mountEmpty(listEl, {
        title: "No achievements recorded yet",
        message: "Awards, certificates, finalist placings and shout-outs land here as the team collects them.",
        iconName: "award",
      });
    } else {
      render(items);
    }
    renderPagination(pagerEl, data, (p) => { page = p; load(); });
  } catch (err) {
    mountError(listEl, errorMessage(err, "Could not load achievements."), load);
    if (pagerEl) pagerEl.innerHTML = "";
  }
}

load();
