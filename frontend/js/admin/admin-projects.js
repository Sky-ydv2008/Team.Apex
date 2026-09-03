/**
 * Apex Innovators — admin-projects.js (admin/projects.html)
 * Admin CRUD for projects: list with q/status filters, status PATCHes
 * (approve/reject/publish), create/edit modal, delete.
 * Guarded by guardAdmin() before any data loads.
 */

import { apiFetch, errorMessage } from "../api.js";
import { guardModerator } from "../auth.js";
import {
  icon, esc, formatDate, statusBadge, injectAdminShell,
  openDialog, closeDialog, confirmDialog, toast, renderPagination, pageInfo,
} from "../components.js";

const SIZE = 15;
const state = { page: 0, q: "", status: "" };

const tbody = document.getElementById("table-body");
const pagerEl = document.getElementById("table-pager");
const infoEl = document.getElementById("list-info");
const modal = document.getElementById("project-modal");
const form = document.getElementById("project-form");

const cache = new Map();

function rowSkeleton() {
  return `<tr><td colspan="8" style="padding:1rem;"><div class="sk-list">
    <div class="sk sk-row w100"></div><div class="sk sk-row w100"></div><div class="sk sk-row w100"></div>
  </div></td></tr>`;
}

function collectForm() {
  const val = (id) => document.getElementById(id)?.value.trim() ?? "";
  const opt = (v) => (v ? v : null);
  return {
    title: val("p-title"),
    tagline: opt(val("p-tagline")),
    year: val("p-year") ? Number(val("p-year")) : null,
    status: val("p-status"),
    featured: Boolean(document.getElementById("p-featured")?.checked),
    githubUrl: opt(val("p-github-url")),
    demoUrl: opt(val("p-demo-url")),
    docsUrl: opt(val("p-docs-url")),
    description: opt(val("p-description")),
    problem: opt(val("p-problem")),
    solution: opt(val("p-solution")),
  };
}

function actionsFor(p) {
  const pub = p.slug ? `<a class="btn btn-sm btn-outline btn-icon" href="../project-details.html?slug=${encodeURIComponent(p.slug)}" target="_blank" rel="noopener" title="View public page">${icon("external")}</a>` : "";
  const statusButtons = {
    PENDING_REVIEW: `<button class="btn btn-sm btn-success-soft" type="button" data-status="APPROVED" data-id="${p.id}">Approve</button>
      <button class="btn btn-sm btn-warn-soft" type="button" data-status="REJECTED" data-id="${p.id}">Reject</button>`,
    APPROVED: `<button class="btn btn-sm btn-success-soft" type="button" data-status="PUBLISHED" data-id="${p.id}">Publish</button>`,
    PUBLISHED: `<button class="btn btn-sm btn-outline" type="button" data-status="APPROVED" data-id="${p.id}">Unpublish</button>`,
    REJECTED: `<button class="btn btn-sm btn-success-soft" type="button" data-status="APPROVED" data-id="${p.id}">Approve</button>`,
    DRAFT: `<button class="btn btn-sm btn-outline" type="button" data-status="PENDING_REVIEW" data-id="${p.id}">Send to review</button>
      <button class="btn btn-sm btn-success-soft" type="button" data-status="PUBLISHED" data-id="${p.id}">Publish</button>`,
  }[p.status] || "";

  return `<div class="row-actions">
    ${statusButtons}
    ${pub}
    <button class="btn btn-sm btn-outline btn-icon" type="button" data-edit data-id="${p.id}" title="Edit project">${icon("edit")}</button>
    <button class="btn btn-sm btn-danger btn-icon" type="button" data-delete data-id="${p.id}" title="Delete project">${icon("trash")}</button>
  </div>`;
}

function render(items) {
  if (!items.length) {
    tbody.innerHTML = `<tr><td colspan="8" class="empty-row">No projects found${state.q || state.status ? " for the current filters" : ""}.</td></tr>`;
    return;
  }
  items.forEach((p) => cache.set(String(p.id), p));
  tbody.innerHTML = items.map((p) => `
    <tr>
      <td><span class="cell-stack"><span class="cell-main">${esc(p.title || "Untitled")}</span><span class="cell-sub">${p.slug ? esc(p.slug) : ""}</span></span></td>
      <td>${p.year ? esc(p.year) : "—"}</td>
      <td>${Array.isArray(p.technologies) ? p.technologies.length : 0}</td>
      <td>${p.featured ? `<span class="badge b-featured">Featured</span>` : "—"}</td>
      <td>${statusBadge(p.status)}</td>
      <td class="hide-md">${p.createdAt ? esc(formatDate(p.createdAt)) : "—"}</td>
      <td>${actionsFor(p)}</td>
    </tr>`).join("");
}

async function load() {
  if (!tbody) return;
  tbody.innerHTML = rowSkeleton();
  if (pagerEl) pagerEl.innerHTML = "";
  try {
    const data = await apiFetch("/admin/projects", {
      auth: true,
      params: { page: state.page, size: SIZE, q: state.q || undefined, status: state.status || undefined },
    });
    const items = (data && data.content) || [];
    if (infoEl) infoEl.textContent = pageInfo(data);
    render(items);
    if (pagerEl) renderPagination(pagerEl, data, (p) => { state.page = p; load(); });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8"><div class="state state-error" style="border:none;">
      <span class="state-icon">${icon("alert")}</span>
      <h3 class="state-title">Could not load projects</h3>
      <p class="state-text">${esc(errorMessage(err, "An error occurred while loading projects."))}</p>
      <button class="btn btn-outline btn-sm" type="button" id="retry-projects">${icon("arrow-right")} Retry</button>
    </div></td></tr>`;
    const retry = tbody.querySelector("#retry-projects");
    if (retry) retry.addEventListener("click", load);
  }
}

/* ---------- Status / delete ---------- */
async function setStatus(id, status) {
  const label = String(status).toLowerCase().replace(/_/g, " ");
  try {
    await apiFetch(`/admin/projects/${id}/status`, { method: "PATCH", body: { status }, auth: true });
    toast(`Project marked ${label}`, "success");
    load();
  } catch (err) {
    toast(errorMessage(err, "Could not update project status."), "error");
  }
}

async function removeProject(id, title) {
  const ok = await confirmDialog({
    title: "Delete project?",
    message: `"${title}" and its member/technology links will be permanently removed. This cannot be undone.`,
    confirmLabel: "Delete project",
  });
  if (!ok) return;
  try {
    await apiFetch(`/admin/projects/${id}`, { method: "DELETE", auth: true });
    toast("Project deleted", "success");
    load();
  } catch (err) {
    toast(errorMessage(err, "Could not delete the project."), "error");
  }
}

/* ---------- Create / edit modal ---------- */
function openCreate() {
  form.reset();
  document.getElementById("p-id").value = "";
  const statusSel = document.getElementById("p-status");
  if (statusSel) statusSel.value = "DRAFT";
  document.getElementById("project-modal-title").textContent = "New project";
  openDialog(modal);
}

function openEdit(p) {
  form.reset();
  document.getElementById("p-id").value = p.id;
  document.getElementById("p-title").value = p.title || "";
  document.getElementById("p-tagline").value = p.tagline || "";
  document.getElementById("p-year").value = p.year || "";
  document.getElementById("p-status").value = p.status || "DRAFT";
  document.getElementById("p-featured").checked = Boolean(p.featured);
  document.getElementById("p-github-url").value = p.githubUrl || "";
  document.getElementById("p-demo-url").value = p.demoUrl || "";
  document.getElementById("p-docs-url").value = p.docsUrl || "";
  document.getElementById("p-description").value = p.description || "";
  document.getElementById("p-problem").value = p.problem || "";
  document.getElementById("p-solution").value = p.solution || "";
  document.getElementById("project-modal-title").textContent = `Edit — ${p.title || "project"}`;
  openDialog(modal);
}

async function submit(e) {
  e.preventDefault();
  const payload = collectForm();
  if (!payload.title) {
    toast("Title is required", "warning");
    return;
  }
  const id = document.getElementById("p-id").value;
  const saveBtn = form.querySelector("button[type=submit]");
  saveBtn.disabled = true;
  try {
    if (id) {
      await apiFetch(`/admin/projects/${id}`, { method: "PUT", body: payload, auth: true });
      toast("Project updated", "success");
    } else {
      await apiFetch("/admin/projects", { method: "POST", body: payload, auth: true });
      toast("Project created", "success");
    }
    closeDialog(modal);
    state.page = 0;
    load();
  } catch (err) {
    toast(errorMessage(err, "Could not save the project."), "error");
  } finally {
    saveBtn.disabled = false;
  }
}

/* ---------- Events ---------- */
function wireEvents() {
  const qInput = document.getElementById("filter-q");
  const statusSel = document.getElementById("filter-status");
  const newBtn = document.getElementById("btn-new");

  let debounce = null;
  if (qInput) {
    qInput.addEventListener("input", () => {
      clearTimeout(debounce);
      debounce = setTimeout(() => { state.q = qInput.value.trim(); state.page = 0; load(); }, 300);
    });
  }
  if (statusSel) {
    statusSel.addEventListener("change", () => { state.status = statusSel.value; state.page = 0; load(); });
  }
  if (newBtn) newBtn.addEventListener("click", openCreate);
  if (form) form.addEventListener("submit", submit);

  if (tbody) {
    tbody.addEventListener("click", async (e) => {
      const statusBtn = e.target.closest("[data-status]");
      if (statusBtn) { setStatus(statusBtn.dataset.id, statusBtn.dataset.status); return; }
      const editBtn = e.target.closest("[data-edit]");
      if (editBtn) {
        const p = cache.get(String(editBtn.dataset.id));
        if (p) openEdit(p);
        return;
      }
      const delBtn = e.target.closest("[data-delete]");
      if (delBtn) {
        const row = delBtn.closest("tr");
        const title = row ? row.querySelector(".cell-main")?.textContent || "this project" : "this project";
        removeProject(delBtn.dataset.id, title);
      }
    });
  }
}

async function boot() {
  const user = await guardModerator();
  if (!user) return;
  injectAdminShell("projects", user);
  wireEvents();
  load();
}

boot();
