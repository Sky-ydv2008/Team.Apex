/**
 * Apex Innovators — interactive-grid.js
 * Interactive Dot Grid Canvas Background (port of interactive-grid component).
 * Grid dots change brightness, hue, and draw vector lines to the cursor on hover.
 */

export function initInteractiveGrid(canvasId = "hero-interactive-grid", opts = {}) {
  if (typeof window === "undefined" || typeof document === "undefined") return;

  const canvas = typeof canvasId === "string" ? document.getElementById(canvasId) : canvasId;
  if (!canvas) return;

  const dotDistance = opts.dotDistance || 32;
  const dotRadius = opts.dotRadius || 1.8;
  const minProximity = opts.minProximity || 210;
  const minProxSq = minProximity * minProximity;

  let width = 0;
  let height = 0;
  let dots = [];
  let mouse = { x: -1000, y: -1000 };
  let hue = 190;
  let animId = null;

  function resize() {
    const parent = canvas.parentElement;
    width = canvas.width = parent ? parent.offsetWidth : window.innerWidth;
    height = canvas.height = parent ? parent.offsetHeight : window.innerHeight;
    createDots();
  }

  function createDots() {
    dots = [];
    for (let x = 0; x < width; x += dotDistance) {
      for (let y = 0; y < height; y += dotDistance) {
        dots.push({ x, y });
      }
    }
  }

  function onMouseMove(e) {
    const rect = canvas.getBoundingClientRect();
    mouse.x = e.clientX - rect.left;
    mouse.y = e.clientY - rect.top;
    hue = (((mouse.x / (width || 1)) + (mouse.y / (height || 1))) * 360) % 360;
  }

  function onMouseLeave() {
    mouse.x = -1000;
    mouse.y = -1000;
  }

  window.addEventListener("resize", resize);
  window.addEventListener("mousemove", onMouseMove);
  window.addEventListener("mouseleave", onMouseLeave);

  resize();

  const ctx = canvas.getContext("2d");
  if (!ctx) return;

  function render() {
    ctx.clearRect(0, 0, width, height);

    for (let i = 0; i < dots.length; i++) {
      const dot = dots[i];
      const dX = dot.x - mouse.x;
      const dY = dot.y - mouse.y;
      const distSq = dX * dX + dY * dY;

      if (distSq <= minProxSq) {
        const factor = 1 - distSq / minProxSq;
        const brightness = Math.max(12, Math.round(60 - (distSq / minProxSq) * 45));
        const color = `hsl(${hue}, 85%, ${brightness}%)`;

        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.arc(dot.x, dot.y, dotRadius * (1 + factor * 0.5), 0, Math.PI * 2);
        ctx.fill();

        ctx.strokeStyle = `hsla(${hue}, 85%, ${brightness}%, ${Math.max(0.15, factor * 0.6)})`;
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(dot.x, dot.y);
        ctx.lineTo(mouse.x, mouse.y);
        ctx.stroke();
      } else {
        ctx.fillStyle = "rgba(125, 211, 252, 0.12)";
        ctx.beginPath();
        ctx.arc(dot.x, dot.y, dotRadius, 0, Math.PI * 2);
        ctx.fill();
      }
    }

    animId = requestAnimationFrame(render);
  }

  render();

  return () => {
    window.removeEventListener("resize", resize);
    window.removeEventListener("mousemove", onMouseMove);
    window.removeEventListener("mouseleave", onMouseLeave);
    if (animId) cancelAnimationFrame(animId);
  };
}

if (typeof document !== "undefined") {
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () => initInteractiveGrid());
  } else {
    initInteractiveGrid();
  }
}
