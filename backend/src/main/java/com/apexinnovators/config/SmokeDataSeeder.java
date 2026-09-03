package com.apexinnovators.config;

import com.apexinnovators.entity.Achievement;
import com.apexinnovators.entity.AchievementType;
import com.apexinnovators.entity.ContactMessage;
import com.apexinnovators.entity.Hackathon;
import com.apexinnovators.entity.HackathonMember;
import com.apexinnovators.entity.HackathonProject;
import com.apexinnovators.entity.Post;
import com.apexinnovators.entity.PostType;
import com.apexinnovators.entity.Profile;
import com.apexinnovators.entity.Project;
import com.apexinnovators.entity.ProjectMember;
import com.apexinnovators.entity.ProjectStatus;
import com.apexinnovators.entity.ProjectTechnology;
import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.Technology;
import com.apexinnovators.entity.User;
import com.apexinnovators.entity.UserStatus;
import com.apexinnovators.repository.AchievementRepository;
import com.apexinnovators.repository.ContactMessageRepository;
import com.apexinnovators.repository.HackathonMemberRepository;
import com.apexinnovators.repository.HackathonProjectRepository;
import com.apexinnovators.repository.HackathonRepository;
import com.apexinnovators.repository.PostRepository;
import com.apexinnovators.repository.ProfileRepository;
import com.apexinnovators.repository.ProjectMemberRepository;
import com.apexinnovators.repository.ProjectRepository;
import com.apexinnovators.repository.ProjectTechnologyRepository;
import com.apexinnovators.repository.TechnologyRepository;
import com.apexinnovators.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Demo dataset for the "smoke" profile only (in-memory H2). Mirrors the
 * production seed (database/seed.sql): role accounts, the IntelliERP case
 * study, the Build With 2.0 hackathon, catalog technologies, one published
 * post and one contact message — enough to exercise every REST surface.
 */
@Component
@org.springframework.context.annotation.Profile({"smoke", "render"})
@RequiredArgsConstructor
public class SmokeDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TechnologyRepository technologyRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectTechnologyRepository projectTechnologyRepository;
    private final HackathonRepository hackathonRepository;
    private final HackathonMemberRepository hackathonMemberRepository;
    private final HackathonProjectRepository hackathonProjectRepository;
    private final AchievementRepository achievementRepository;
    private final PostRepository postRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // -- Accounts ---------------------------------------------------
        User admin = user("Shivam Yadav", "apex.innovator.team@gmail.com", "Apex@2000", Role.ADMIN);
        User core = user("Lipsarani Bisoyi", "core@apexinnovators.dev", "Core@123", Role.CORE_MEMBER);
        User member = user("Aryan Gupta", "member@apexinnovators.dev", "Member@123", Role.CORE_MEMBER);
        profile(admin, "Java backend developer on Apex Innovators — Spring Boot, REST APIs, JWT security and the platform core.", "Java Backend Developer");
        profile(core, "Frontend developer on Apex Innovators — crafting the interfaces, design system and user experience.", "Frontend Developer");
        profile(member, "Full stack developer on Apex Innovators — backend to browser, from database to deployed product.", "Full Stack Developer");

        // -- Technology catalog ------------------------------------------
        Technology java = tech("Java", "Language", "devicon-java");
        Technology spring = tech("Spring Boot", "Framework", "devicon-spring");
        Technology sec = tech("Spring Security", "Security", "devicon-spring");
        Technology jwt = tech("JWT", "Security", "shield");
        Technology mysql = tech("MySQL", "Database", "devicon-mysql");
        Technology jpa = tech("Hibernate / JPA", "Database", "database");
        Technology maven = tech("Maven", "Tooling", "devicon-maven");
        Technology js = tech("JavaScript", "Language", "devicon-javascript");
        Technology html = tech("HTML5", "Frontend", "devicon-html5");
        Technology css = tech("CSS3", "Frontend", "devicon-css3");
        Technology rest = tech("REST APIs", "Integration", "server");
        Technology swagger = tech("Swagger / OpenAPI", "Docs", "file-code");
        Technology git = tech("Git / GitHub", "Tooling", "devicon-github");

        // -- IntelliERP case study ---------------------------------------
        Project intellierp = new Project();
        intellierp.setTitle("IntelliERP");
        intellierp.setSlug("intellierp");
        intellierp.setTagline("AI-powered ERP & Business Intelligence for small businesses");
        intellierp.setDescription("IntelliERP pairs a Mini ERP Core with an Analytics Dashboard and an "
                + "AI Business Advisor. Built for the Build With 2.0 hackathon, it gives small businesses a "
                + "single pane of glass over products, inventory, purchases, sales, expenses and employees — "
                + "with rule-based predictive intelligence that runs without any external AI dependency.");
        intellierp.setProblem("Small businesses operate on spreadsheets and gut feeling. They lack actionable "
                + "visibility into inventory risk, supplier reliability and overall business health.");
        intellierp.setSolution("A role-based Mini ERP Core plus Analytics Dashboard and AI Business Advisor. "
                + "Stockout forecasting, supplier-loss detection and automatic product classification run as "
                + "transparent rule-based algorithms over transactional data — fully self-contained.");
        intellierp.setStatus(ProjectStatus.PUBLISHED);
        intellierp.setFeatured(true);
        intellierp.setGithubUrl("https://github.com/Sky-ydv2008/ai-powered-mini-erp");
        intellierp.setDemoUrl("https://sky-ydv2008.github.io/Team.Apex/");
        intellierp.setYear(2026);
        projectRepository.save(intellierp);

        memberOf(intellierp, admin, "Java Backend Developer", "Spring Boot REST APIs, Spring Security + JWT, predictive inventory logic");
        memberOf(intellierp, core, "Frontend Developer", "UI, design system and client-side integration");
        linkTech(intellierp, java, spring, sec, jwt, mysql, jpa, maven, js, html, css, rest, swagger);

        // -- Hackathon archive ---------------------------------------------
        Hackathon hackathon = new Hackathon();
        hackathon.setName("Build With 2.0");
        hackathon.setSlug("build-with-2.0");
        hackathon.setDescription("Hackathon journey of Apex Innovators for Build With 2.0, where the team "
                + "built IntelliERP — an AI-powered ERP & Business Intelligence solution.");
        hackathon.setChallenge("AI-Powered ERP & Business Intelligence: give small businesses real, actionable intelligence.");
        hackathon.setResult("Participation — result and certificate to be added from the official announcement.");
        hackathonRepository.save(hackathon);

        memberOf(hackathon, admin);
        memberOf(hackathon, core);
        linkProject(hackathon, intellierp);

        // -- Achievement -----------------------------------------------------
        Achievement achievement = new Achievement();
        achievement.setUserId(core.getId());
        achievement.setTitle("Build With 2.0 — IntelliERP");
        achievement.setType(AchievementType.CERTIFICATE);
        achievement.setIssuer("Build With 2.0 organizers");
        achievement.setAwardDate(LocalDate.of(2026, 9, 1));
        achievement.setDescription("Certified participation for the AI-Powered ERP & Business Intelligence problem statement.");
        achievementRepository.save(achievement);

        // -- Community post ----------------------------------------------------
        Post post = new Post();
        post.setAuthorId(core.getId());
        post.setType(PostType.ANNOUNCEMENT);
        post.setTitle("IntelliERP is live on the showcase");
        post.setBody("Our Build With 2.0 project is now published — full problem statement, solution walkthrough "
                + "and build story on the project page. Feedback welcome in the comments.");
        post.setStatus(ProjectStatus.PUBLISHED);
        postRepository.save(post);

        // -- Contact message -----------------------------------------------------
        ContactMessage message = new ContactMessage();
        message.setName("Visitor");
        message.setEmail("visitor@example.com");
        message.setSubject("Collaboration");
        message.setMessage("Hi Apex Innovators — would love to discuss a joint hackathon entry.");
        contactMessageRepository.save(message);
    }

    private User user(String name, String email, String rawPassword, Role role) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        return userRepository.save(u);
    }

    private void profile(User user, String bio, String headline) {
        Profile p = new Profile();
        p.setUserId(user.getId());
        p.setBio(bio);
        p.setHeadline(headline);
        profileRepository.save(p);
    }

    private Technology tech(String name, String category, String icon) {
        Technology t = new Technology();
        t.setName(name);
        t.setCategory(category);
        t.setIcon(icon);
        return technologyRepository.save(t);
    }

    private void memberOf(Project project, User user, String role, String contribution) {
        ProjectMember pm = new ProjectMember();
        pm.setProjectId(project.getId());
        pm.setUserId(user.getId());
        pm.setRole(role);
        pm.setContribution(contribution);
        projectMemberRepository.save(pm);
    }

    private void linkTech(Project project, Technology... technologies) {
        for (Technology t : technologies) {
            ProjectTechnology pt = new ProjectTechnology();
            pt.setProjectId(project.getId());
            pt.setTechnologyId(t.getId());
            projectTechnologyRepository.save(pt);
        }
    }

    private void memberOf(Hackathon hackathon, User user) {
        HackathonMember hm = new HackathonMember();
        hm.setHackathonId(hackathon.getId());
        hm.setUserId(user.getId());
        hackathonMemberRepository.save(hm);
    }

    private void linkProject(Hackathon hackathon, Project project) {
        HackathonProject hp = new HackathonProject();
        hp.setHackathonId(hackathon.getId());
        hp.setProjectId(project.getId());
        hackathonProjectRepository.save(hp);
    }
}
