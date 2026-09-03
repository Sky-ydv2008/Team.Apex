/**
 * Apex Innovators — demo-data.js
 * Built-in read-only dataset used when the site is served without the API
 * (GitHub Pages / file:// or ?demo=1). Mirrors database/seed.sql and the
 * public REST contract in docs/api.md so every public page renders real
 * content. Mutating/account endpoints resolve to errors explaining that the
 * demo preview has no accounts — the live backend (backend/ + MySQL) adds them.
 *
 * Router output contract: returns the payload, or { __demoError: { status, message } }.
 */

const T = {
  IntelliERP: {
    id: 1,
    title: "IntelliERP",
    slug: "intellierp",
    tagline: "AI-powered ERP & Business Intelligence for small businesses",
    description:
      "IntelliERP pairs a Mini ERP Core with an Analytics Dashboard and an AI Business Advisor. Built for the Build With 2.0 hackathon, it gives small businesses a single pane of glass over products, inventory, purchases, sales, expenses and employees — with rule-based predictive intelligence that runs without any external AI dependency.",
    problem:
      "Small businesses operate on spreadsheets and gut feeling. They lack actionable visibility into inventory risk, supplier reliability and overall business health, and affordable ERP tools are built for enterprises, not for them.",
    solution:
      "A role-based Mini ERP Core (products, inventory, purchases, sales, expenses, employees) plus Analytics Dashboard and AI Business Advisor. Stockout forecasting, supplier-loss detection and automatic product classification run as transparent rule-based algorithms over transactional data — fully self-contained.",
    status: "PUBLISHED",
    featured: true,
    githubUrl: "https://github.com/Sky-ydv2008/ai-powered-mini-erp",
    demoUrl: "https://sky-ydv2008.github.io/Team.Apex/",
    docsUrl: null,
    year: 2026,
    technologies: [
      { id: 1, name: "Java", category: "Language", icon: "devicon-java" },
      { id: 5, name: "Spring Boot", category: "Framework", icon: "devicon-spring" },
      { id: 6, name: "Spring Security", category: "Security", icon: "devicon-spring" },
      { id: 7, name: "JWT", category: "Security", icon: "shield" },
      { id: 8, name: "MySQL", category: "Database", icon: "devicon-mysql" },
      { id: 12, name: "Hibernate / JPA", category: "Database", icon: "database" },
      { id: 9, name: "Maven", category: "Tooling", icon: "devicon-maven" },
      { id: 2, name: "JavaScript", category: "Language", icon: "devicon-javascript" },
      { id: 3, name: "HTML5", category: "Frontend", icon: "devicon-html5" },
      { id: 4, name: "CSS3", category: "Frontend", icon: "devicon-css3" },
      { id: 10, name: "REST APIs", category: "Integration", icon: "server" },
      { id: 11, name: "Swagger / OpenAPI", category: "Docs", icon: "file-code" },
    ],
    members: [
      { userId: 1, name: "Shivam Yadav", role: "Java Backend Developer", contribution: "Spring Boot REST APIs, Spring Security + JWT, predictive inventory logic" },
      { userId: 2, name: "Lipsarani Bisoyi", role: "Frontend Developer", contribution: "UI, design system and client-side integration" },
    ],
    hackathons: [{ id: 1, name: "Build With 2.0" }],
    createdAt: "2026-09-01T09:00:00",
  },
  "Hack Night Bot": {
    id: 2,
    title: "Hack Night Bot",
    slug: "hack-night-bot",
    tagline: "Bot for hackathon teams",
    description: "A helper bot that keeps hackathon teams on track during long builds — reminders, standups and deploy pings.",
    problem: "Teams lose momentum and forget breaks during 24-hour hackathons.",
    solution: "A lightweight bot that runs structured standups, reminds the team to commit and celebrate, and nudges healthy breaks.",
    status: "PUBLISHED",
    featured: false,
    githubUrl: null,
    demoUrl: null,
    docsUrl: null,
    year: 2026,
    technologies: [
      { id: 2, name: "JavaScript", category: "Language", icon: "devicon-javascript" },
      { id: 10, name: "REST APIs", category: "Integration", icon: "server" },
    ],
    members: [{ userId: 3, name: "Aryan Gupta", role: "Full Stack Developer", contribution: "Bot logic, integrations and deployment" }],
    hackathons: [],
    createdAt: "2026-09-02T14:00:00",
  },
};

const HACKATHON = {
  id: 1,
  name: "Build With 2.0",
  slug: "build-with-2.0",
  organizer: null,
  date: null,
  description:
    "Hackathon journey of Apex Innovators for Build With 2.0, where the team built IntelliERP — an AI-powered ERP & Business Intelligence solution.",
  challenge: "AI-Powered ERP & Business Intelligence: build a product that gives small businesses real, actionable intelligence.",
  result: "Team member for Build With 2.0 — result and certificate details to be added from the official announcement.",
  certificateUrl: null,
  presentationUrl: null,
  members: [
    { userId: 1, name: "Shivam Yadav" },
    { userId: 2, name: "Lipsarani Bisoyi" },
    { userId: 3, name: "Aryan Gupta" },
  ],
  projects: [{ id: 1, title: "IntelliERP", slug: "intellierp" }],
  createdAt: "2026-09-01T08:00:00",
};

const TEAM = [
  { id: 1, name: "Shivam Yadav", role: "ADMIN", headline: "Java Backend Developer", bio: "Java backend developer on Apex Innovators — Spring Boot, REST APIs, JWT security and the platform core.", photoUrl: null, github: "Sky-ydv2008", linkedin: null },
  { id: 2, name: "Lipsarani Bisoyi", role: "CORE_MEMBER", headline: "Frontend Developer", bio: "Frontend developer on Apex Innovators — crafting the interfaces, design system and user experience.", photoUrl: null, github: null, linkedin: null },
  { id: 3, name: "Aryan Gupta", role: "CORE_MEMBER", headline: "Full Stack Developer", bio: "Full stack developer on Apex Innovators — backend to browser, from database to deployed product.", photoUrl: null, github: null, linkedin: null },
];

const ACHIEVEMENTS = [
  {
    id: 1, userId: 1, userName: "Shivam Yadav", title: "Build With 2.0 — IntelliERP", type: "CERTIFICATE",
    issuer: "Build With 2.0 organizers", awardDate: "2026-09-01",
    description: "Certified participation for the AI-Powered ERP & Business Intelligence problem statement.", verifyUrl: null,
  },
];

const TECHNOLOGIES = [
  { id: 1, name: "Java", category: "Language", icon: "devicon-java" },
  { id: 2, name: "JavaScript", category: "Language", icon: "devicon-javascript" },
  { id: 3, name: "HTML5", category: "Frontend", icon: "devicon-html5" },
  { id: 4, name: "CSS3", category: "Frontend", icon: "devicon-css3" },
  { id: 5, name: "Spring Boot", category: "Framework", icon: "devicon-spring" },
  { id: 6, name: "Spring Security", category: "Security", icon: "devicon-spring" },
  { id: 7, name: "JWT", category: "Security", icon: "shield" },
  { id: 8, name: "MySQL", category: "Database", icon: "devicon-mysql" },
  { id: 9, name: "Maven", category: "Tooling", icon: "devicon-maven" },
  { id: 10, name: "REST APIs", category: "Integration", icon: "server" },
  { id: 11, name: "Swagger / OpenAPI", category: "Docs", icon: "file-code" },
  { id: 12, name: "Hibernate / JPA", category: "Database", icon: "database" },
  { id: 13, name: "Git / GitHub", category: "Tooling", icon: "devicon-github" },
];

const POSTS = [
  {
    id: 1, authorId: 1, authorName: "Shivam Yadav", type: "ANNOUNCEMENT", title: "IntelliERP is live on the showcase",
    body: "Our Build With 2.0 project is now published — full problem statement, solution walkthrough and build story on the project page. Feedback welcome in the comments.",
    status: "PUBLISHED", createdAt: "2026-09-02T10:00:00", commentCount: 2, likeCount: 4, likedByMe: false,
  },
  {
    id: 2, authorId: 3, authorName: "Aryan Gupta", type: "DISCUSSION", title: "How do you structure a 48-hour build?",
    body: "Splitting work across a hackathon weekend is half the battle — what works for your team?",
    status: "PUBLISHED", createdAt: "2026-09-02T18:30:00", commentCount: 1, likeCount: 2, likedByMe: false,
  },
];

const COMMENTS = {
  1: [
    { id: 1, authorId: 2, authorName: "Lipsarani Bisoyi", body: "Great write-up — the stockout logic is a clever touch.", createdAt: "2026-09-02T11:00:00" },
    { id: 2, authorId: 3, authorName: "Aryan Gupta", body: "Would love to see the dashboard screenshots soon.", createdAt: "2026-09-02T12:15:00" },
  ],
  2: [
    { id: 3, authorId: 1, authorName: "Shivam Yadav", body: "Plan the DB schema first, then split features by module.", createdAt: "2026-09-02T19:00:00" },
  ],
};

const STATS = { projects: 2, hackathons: 1, members: 3, technologies: 13, achievements: 1 };

const DEMO_NOTE = "This is the GitHub Pages demo preview with built-in sample data. Log in, posting and the admin panel need the live backend (see the repo README).";

function err(status, message) {
  return { __demoError: { status, message } };
}

function page(items, page, size) {
  const p = Number(page || 0);
  const s = Number(size || 12);
  const start = p * s;
  const content = items.slice(start, start + s);
  return {
    content,
    page: p,
    size: s,
    totalElements: items.length,
    totalPages: Math.max(1, Math.ceil(items.length / s)),
  };
}

function qMatch(needle, ...fields) {
  const q = (needle || "").trim().toLowerCase();
  if (!q) return true;
  return fields.some((f) => String(f || "").toLowerCase().includes(q));
}

export function demoFetch(method, path, params = {}, body = {}) {
  const seg = path.split("/").filter(Boolean); // ["projects", "intellierp"] or ["public","stats"]
  const base = seg[0];

  // Public reads -----------------------------------------------------
  if (method === "GET" && base === "public" && seg[1] === "stats") return STATS;
  if (method === "GET" && base === "technologies") return TECHNOLOGIES;

  if (method === "GET" && base === "projects") {
    if (seg.length === 1) {
      let items = Object.values(T).filter((p) => p.status === "PUBLISHED");
      if (params.featured === "true" || params.featured === true) items = items.filter((p) => p.featured);
      if (params.q) items = items.filter((p) => qMatch(params.q, p.title, p.tagline, p.description));
      if (params.tech) items = items.filter((p) => p.technologies.some((t) => String(t.name).toLowerCase() === String(params.tech).toLowerCase()));
      if (params.year) items = items.filter((p) => String(p.year) === String(params.year));
      return page(items, params.page, params.size);
    }
    const hit = Object.values(T).find((p) => String(p.id) === seg[1] || p.slug === seg[1]);
    return hit || err(404, "Project not found");
  }

  if (method === "GET" && base === "hackathons") {
    if (seg.length === 1) return page([HACKATHON], params.page, params.size);
    return (String(HACKATHON.id) === seg[1] || HACKATHON.slug === seg[1]) ? HACKATHON : err(404, "Hackathon not found");
  }

  if (method === "GET" && base === "team") return TEAM;

  if (method === "GET" && base === "achievements") return page(ACHIEVEMENTS, params.page, params.size);

  if (method === "GET" && base === "posts") {
    if (seg.length === 1) {
      let items = POSTS;
      if (params.type) items = items.filter((p) => p.type === params.type);
      return page(items, params.page, params.size);
    }
    if (seg.length === 2) {
      const post = POSTS.find((p) => String(p.id) === seg[1]);
      return post || err(404, "Post not found");
    }
    if (seg[2] === "comments") {
      return COMMENTS[seg[1]] || err(404, "Post not found");
    }
  }

  if (method === "GET" && base === "events") return page([], params.page, params.size);

  if (method === "GET" && base === "search") {
    const q = String(params.q || "").toLowerCase();
    const filter = (items, fields) => items.filter((it) => qMatch(q, ...fields));
    return {
      projects: filter(Object.values(T), "title", "tagline", "description").slice(0, 10),
      hackathons: qMatch(q, HACKATHON.name, HACKATHON.description) ? [HACKATHON] : [],
      posts: filter(POSTS, "title", "body").slice(0, 10),
    };
  }

  // Contact form — pretend it was stored (demo has no inbox) ----------
  if (method === "POST" && base === "contact") {
    const b = body || {};
    return { id: 1, name: b.name || "Guest", email: b.email || "", subject: b.subject || "", message: b.message || "", status: "NEW", createdAt: new Date().toISOString() };
  }

  // Account / admin surfaces need the real backend ---------------------
  if (base === "auth" || base === "admin") {
    return err(401, DEMO_NOTE);
  }
  if (method !== "GET") {
    return err(403, DEMO_NOTE);
  }
  return err(404, "Not found in the demo dataset");
}

/** True when served without a backend (GitHub Pages, file://, or ?demo=1). */
export function demoActive() {
  try {
    return window.location.protocol === "file:"
      || window.location.hostname.endsWith("github.io")
      || new URLSearchParams(window.location.search).has("demo");
  } catch (e) {
    return false;
  }
}
