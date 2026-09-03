/**
 * Apex Innovators — events.js (events.html)
 * Upcoming events with pagination. GET /events?page=&size=
 */

import { apiFetch, errorMessage } from "./api.js";
import { mountLoading, mountData, mountEmpty, mountError, renderPagination, pageInfo } from "./components.js";
import { eventCardHTML } from "./cards.js";

const SIZE = 12;
let page = 0;

const listEl = document.getElementById("events-list");
const pagerEl = document.getElementById("events-pager");
const infoEl = document.getElementById("results-info");

async function load() {
  if (!listEl) return;
  mountLoading(listEl, "feed", 4);
  try {
    const data = await apiFetch("/events", { params: { page, size: SIZE } });
    const items = (data && data.content) || [];
    if (infoEl) infoEl.textContent = pageInfo(data);

    if (!items.length) {
      mountEmpty(listEl, {
        title: "No upcoming events scheduled",
        message: "Workshops, meetups and demo days are announced here first. Keep an eye on the community feed.",
        actionLabel: "Check the community",
        actionHref: "community.html",
        iconName: "calendar",
      });
    } else {
      mountData(listEl, `<div class="grid g2" style="gap:1rem;">${items.map(eventCardHTML).join("")}</div>`);
    }
    renderPagination(pagerEl, data, (p) => { page = p; load(); });
  } catch (err) {
    mountError(listEl, errorMessage(err, "Could not load events."), load);
    if (pagerEl) pagerEl.innerHTML = "";
  }
}

load();
