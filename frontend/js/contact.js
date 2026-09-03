/**
 * Apex Innovators — contact.js (contact.html)
 * POST /api/contact with success / error alert states.
 */

import { apiFetch, errorMessage } from "./api.js";
import { icon, esc, toast } from "./components.js";

const form = document.getElementById("contact-form");
const alertHost = document.getElementById("contact-alert");

function showAlert(kind, text) {
  if (!alertHost) return;
  const icons = { success: "check", error: "alert", info: "info" };
  alertHost.innerHTML = `<div class="alert alert-${kind}">${icon(icons[kind] || "info")}<span>${esc(text)}</span></div>`;
  alertHost.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function clearAlert() {
  if (alertHost) alertHost.innerHTML = "";
}

if (form) {
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearAlert();

    const name = form.elements.name.value.trim();
    const email = form.elements.email.value.trim();
    const subject = form.elements.subject.value.trim();
    const message = form.elements.message.value.trim();

    if (!name || !email || !message) {
      showAlert("error", "Please fill in your name, email and message.");
      return;
    }

    const submitBtn = form.querySelector("button[type=submit]");
    submitBtn.disabled = true;
    submitBtn.innerHTML = `${icon("clock")} Sending…`;

    try {
      await apiFetch("/contact", {
        method: "POST",
        body: { name, email, subject, message },
      });
      showAlert("success", "Message sent — thank you! The team reads every message and will reply at the email you provided.");
      form.reset();
    } catch (err) {
      showAlert("error", errorMessage(err, "Your message could not be sent. Please try again in a moment."));
      toast(errorMessage(err, "Could not send the message."), "error");
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = `${icon("send")} Send message`;
    }
  });
}
