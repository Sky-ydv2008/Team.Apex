/**
 * Apex Innovators — static.js
 * Curated static data used when dynamic sources are unavailable or for
 * filter option lists. Never fabricates member identities.
 */

/**
 * Curated technology ecosystem (fallback). The public REST surface does
 * not guarantee a guest-accessible /technologies listing, so pages that
 * need an ecosystem/filter vocabulary fall back to this list.
 */
export const STATIC_TECHNOLOGIES = [
  { name: "Java", category: "Language" },
  { name: "JavaScript", category: "Language" },
  { name: "TypeScript", category: "Language" },
  { name: "Python", category: "Language" },
  { name: "SQL", category: "Language" },
  { name: "Spring Boot", category: "Backend" },
  { name: "Node.js", category: "Backend" },
  { name: "REST APIs", category: "Backend" },
  { name: "React", category: "Frontend" },
  { name: "HTML & CSS", category: "Frontend" },
  { name: "MySQL", category: "Data" },
  { name: "MongoDB", category: "Data" },
  { name: "Docker", category: "DevOps" },
  { name: "Git & GitHub", category: "DevOps" },
  { name: "CI/CD", category: "DevOps" },
];

/** Technology filter vocabulary for the projects page (fallback). */
export const STATIC_TECH_NAMES = STATIC_TECHNOLOGIES.map((t) => t.name);

/** Hackathon journey steps shown on the home page. */
export const JOURNEY_STEPS = [
  {
    num: "01",
    title: "Compete",
    text: "We enter national and university hackathons with a build-first mindset — turning raw ideas into working demos in 24 to 48 hours.",
    icon: "rocket",
  },
  {
    num: "02",
    title: "Build & archive",
    text: "Every hackathon output is documented here: the challenge, our solution and the code, so the learning is never lost.",
    icon: "layers",
  },
  {
    num: "03",
    title: "Grow together",
    text: "Prototypes graduate into longer projects, and lessons come back to the team as talks, guides and community posts.",
    icon: "sparkles",
  },
];
