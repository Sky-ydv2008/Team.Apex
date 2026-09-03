/**
 * Apex Innovators — team.js (team.html)
 * Published team members. GET /team
 */

import { apiFetch, errorMessage } from "./api.js";
import { mountLoading, mountData, mountEmpty, mountError } from "./components.js";
import { memberCardHTML } from "./cards.js";

const grid = document.getElementById("team-grid");

async function load() {
  if (!grid) return;
  mountLoading(grid, "card", 6);
  try {
    const team = await apiFetch("/team");
    const items = Array.isArray(team) ? team : (team && team.content) || [];
    if (!items.length) {
      mountEmpty(grid, {
        title: "The roster is still forming",
        message: "Published member profiles will appear here — from core leads to active contributors.",
        actionLabel: "About the team",
        actionHref: "about.html",
        iconName: "users",
      });
      return;
    }
    mountData(grid, `<div class="grid g4">${items.map(memberCardHTML).join("")}</div>`);
  } catch (err) {
    mountError(grid, errorMessage(err, "Could not load the team."), load);
  }
}

load();
