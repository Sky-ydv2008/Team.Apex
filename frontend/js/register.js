/**
 * Apex Innovators — register.js (register.html)
 * POST /auth/register {name,email,password} → persist session → home.
 */

import { register } from "./auth.js";
import { icon, esc, toast } from "./components.js";

const form = document.getElementById("register-form");
const alertHost = document.getElementById("register-alert");

function showError(text) {
  if (alertHost) {
    alertHost.innerHTML = `<div class="alert alert-error">${icon("alert")}<span>${esc(text)}</span></div>`;
  }
}

if (form) {
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (alertHost) alertHost.innerHTML = "";

    const name = form.elements.name.value.trim();
    const email = form.elements.email.value.trim();
    const password = form.elements.password.value;
    const terms = form.elements.terms ? form.elements.terms.checked : true;

    if (name.length < 2) return showError("Please enter your full name.");
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return showError("Enter a valid email address.");
    if (password.length < 8) return showError("Password must be at least 8 characters long.");
    if (!terms) return showError("Please accept the community guidelines to continue.");

    const submitBtn = form.querySelector("button[type=submit]");
    submitBtn.disabled = true;
    submitBtn.innerHTML = `${icon("clock")} Creating account…`;

    try {
      const user = await register(name, email, password);
      toast(`Welcome to Apex Innovators, ${user.name}!`, "success");
      window.location.assign("index.html");
    } catch (err) {
      const message = err && err.status === 409
        ? "An account with this email already exists — try logging in instead."
        : (err && err.message) || "Registration failed. Please try again.";
      showError(message);
      submitBtn.disabled = false;
      submitBtn.innerHTML = `${icon("check")} Create account`;
    }
  });
}
