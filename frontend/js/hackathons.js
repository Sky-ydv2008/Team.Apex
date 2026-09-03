/**
 * Apex Innovators — hackathons.js (hackathons.html)
 * Hackathon archive with pagination. GET /hackathons?page=&size=
 */

import { apiFetch, errorMessage } from "./api.js";
import { mountLoading, mountData, mountEmpty, mountError, renderPagination, pageInfo } from "./components.js";
import { hackathonCardHTML } from "./cards.js";

const SIZE = 12;
let page = 0;

const listEl = document.getElementById("hackathons-list");
const pagerEl = document.getElementById("hackathons-pager");
const infoEl = document.getElementById("results-info");

async function load() {
  if (!listEl) return;
  mountLoading(listEl, "card", 6);
  try {
    const data = await apiFetch("/hackathons", { params: { page, size: SIZE } });
    const items = (data && data.content) || [];
    if (infoEl) infoEl.textContent = pageInfo(data);

    if (!items.length) {
      mountEmpty(listEl, {
        title: "No hackathons archived yet",
        message: "When the team competes, every event — challenge, solution and result — gets documented here.",
        iconName: "trophy",
      });
    } else {
      mountData(listEl, `<div class="grid g3">${items.map(hackathonCardHTML).join("")}</div>`);
    }
    renderPagination(pagerEl, data, (p) => { page = p; load(); });
  } catch (err) {
    mountError(listEl, errorMessage(err, "Could not load the hackathon archive."), load);
    if (pagerEl) pagerEl.innerHTML = "";
  }
}

load();
