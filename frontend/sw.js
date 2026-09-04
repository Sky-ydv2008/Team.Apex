/**
 * Apex Innovators — sw.js
 * Service Worker for Progressive Web App (PWA) offline caching & performance.
 */

const CACHE_NAME = "apex-cache-v1";
const STATIC_ASSETS = [
  "./",
  "./index.html",
  "./projects.html",
  "./team.html",
  "./login.html",
  "./css/style.css",
  "./css/components.css",
  "./css/responsive.css",
  "./css/human.css",
  "./js/components.js",
  "./js/demo-data.js",
  "./js/home.js",
  "./favicon.svg",
  "./manifest.json"
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(STATIC_ASSETS);
    })
  );
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
      );
    })
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;

  event.respondWith(
    fetch(event.request)
      .then((response) => {
        if (response && response.status === 200) {
          const clone = response.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, clone);
          });
        }
        return response;
      })
      .catch(() => caches.match(event.request))
  );
});
