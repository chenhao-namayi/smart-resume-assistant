package com.aicopilot.service;

import com.aicopilot.entity.Template;
import com.aicopilot.repository.TemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateScraperService {

    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void scrapeOnStartup() {
        if (templateRepository.count() > 0) {
            log.info("Templates already exist, skipping scrape");
            return;
        }
        log.info("Starting template scraping...");
        int count = 0;
        count += scrapeJsonResume();
        count += scrapeJobHero();
        count += scrapeResumeExamples();
        if (count == 0) {
            saveFallbackTemplates();
            count = 6;
        }
        log.info("Scraped/saved {} templates", count);
    }

    private int scrapeJsonResume() {
        int count = 0;
        String[] urls = {
            "https://raw.githubusercontent.com/jsonresume/resume-schema/master/samples/all-fields.json",
            "https://raw.githubusercontent.com/jsonresume/resume-schema/master/samples/software-developer.json"
        };
        for (String url : urls) {
            try {
                Document doc = Jsoup.connect(url).ignoreContentType(true)
                        .timeout(10000).get();
                String json = doc.body().text();
                JsonNode root = objectMapper.readTree(json);

                String category = "技术开发";
                String name = extractName(root) + " - 简历模板";
                String contentJson = convertJsonResume(root);

                if (!templateRepository.existsBySourceUrl(url)) {
                    Template t = new Template();
                    t.setName(name);
                    t.setCategory(category);
                    String label = "专业";
                    if (root.has("basics") && root.get("basics").has("label")) {
                        label = root.get("basics").get("label").asText();
                    }
                    t.setDescription("来自 JSON Resume 的" + label + "简历模板");
                    t.setContentJson(contentJson);
                    t.setSourceUrl(url);
                    templateRepository.save(t);
                    count++;
                    log.info("Scraped template: {}", name);
                }
            } catch (Exception e) {
                log.warn("Failed to scrape {}: {}", url, e.getMessage());
            }
        }
        return count;
    }

    private int scrapeJobHero() {
        int count = 0;
        String[][] targets = {
            {"https://www.jobhero.com/resume/examples/software-engineer", "技术开发", "软件工程师"},
            {"https://www.jobhero.com/resume/examples/project-manager", "管理", "项目经理"},
            {"https://www.jobhero.com/resume/examples/marketing-manager", "市场运营", "市场经理"},
        };
        for (String[] target : targets) {
            String url = target[0];
            String category = target[1];
            String title = target[2];
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(15000).get();

                Elements sections = doc.select(".resume-example-section, .resume-content, article");
                if (sections.isEmpty()) {
                    sections = doc.select("body");
                }

                Map<String, String> extracted = extractResumeSections(sections.text());
                if (!extracted.isEmpty() && extracted.containsKey("summary")) {
                    String contentJson = toTemplateJson(extracted, title, category);
                    if (!templateRepository.existsBySourceUrl(url)) {
                        Template t = new Template();
                        t.setName(title + " 简历模板");
                        t.setCategory(category);
                        t.setDescription("来自 JobHero 的" + title + "简历示例");
                        t.setContentJson(contentJson);
                        t.setSourceUrl(url);
                        templateRepository.save(t);
                        count++;
                        log.info("Scraped template from JobHero: {}", title);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to scrape {}: {}", url, e.getMessage());
            }
        }
        return count;
    }

    private int scrapeResumeExamples() {
        int count = 0;
        try {
            Document doc = Jsoup.connect("https://www.livecareer.com/resume-examples")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000).get();

            Elements links = doc.select("a[href*=/resume-examples/]");
            Set<String> visited = new HashSet<>();
            int maxLinks = 8;
            for (Element link : links) {
                if (visited.size() >= maxLinks) break;
                String href = link.absUrl("href");
                if (href.isEmpty() || visited.contains(href)) continue;
                visited.add(href);
                try {
                    Document page = Jsoup.connect(href)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(10000).get();
                    String text = page.select("article, .resume-content, .example-content, main").text();
                    if (text.length() < 200) text = page.body().text();
                    Map<String, String> extracted = extractResumeSections(text);
                    if (extracted.containsKey("summary") || extracted.containsKey("skills")) {
                        String title = page.title().replace(" Resume Example | LiveCareer", "")
                                .replace(" | LiveCareer", "").trim();
                        if (title.length() > 80) title = title.substring(0, 77) + "...";
                        String cat = classifyCategory(extracted);
                        String contentJson = toTemplateJson(extracted, title.isEmpty() ? "专业简历" : title, cat);
                        if (!templateRepository.existsBySourceUrl(href)) {
                            Template t = new Template();
                            t.setName(title.isEmpty() ? "专业简历模板" : title + " 模板");
                            t.setCategory(cat);
                            t.setDescription("来自 LiveCareer 的简历示例");
                            t.setContentJson(contentJson);
                            t.setSourceUrl(href);
                            templateRepository.save(t);
                            count++;
                            log.info("Scraped template from LiveCareer: {}", title);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to scrape page {}: {}", href, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to scrape LiveCareer: {}", e.getMessage());
        }
        return count;
    }

    private void saveFallbackTemplates() {
        List<Object[]> templates = List.of(
            new Object[]{"Java 后端开发工程师", "技术开发",
                "适合 Java/Spring Boot 方向的后端开发岗位", buildTechResume("Java", "Spring Boot, MySQL, Redis, Docker")},
            new Object[]{"前端开发工程师", "技术开发",
                "适合 Vue/React 方向的前端开发岗位", buildTechResume("前端", "Vue.js, React, TypeScript, Webpack")},
            new Object[]{"产品经理", "产品设计",
                "适合互联网产品经理岗位，含项目成果描述", buildProductResume()},
            new Object[]{"市场营销经理", "市场运营",
                "适合市场营销方向岗位", buildMarketingResume()},
            new Object[]{"应届毕业生", "应届生",
                "适合应届毕业生的通用简历模板", buildFreshGraduateResume()},
            new Object[]{"项目经理", "管理",
                "适合 PMP/技术管理方向岗位", buildManagerResume()}
        );
        for (Object[] t : templates) {
            String name = (String) t[0];
            String category = (String) t[1];
            String desc = (String) t[2];
            String json = (String) t[3];
            Template tmpl = new Template();
            tmpl.setName(name + " 模板");
            tmpl.setCategory(category);
            tmpl.setDescription(desc);
            tmpl.setContentJson(json);
            tmpl.setSourceUrl("built-in");
            templateRepository.save(tmpl);
        }
    }

    // --- parsing helpers ---

    private String extractName(JsonNode root) {
        if (root.has("basics")) {
            JsonNode basics = root.get("basics");
            if (basics.has("name")) return basics.get("name").asText();
        }
        return "专业人才";
    }

    private String convertJsonResume(JsonNode root) {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, String> basicInfo = new LinkedHashMap<>();
        String name = "";
        if (root.has("basics")) {
            JsonNode b = root.get("basics");
            name = b.has("name") ? b.get("name").asText() : "";
            basicInfo.put("name", name);
            basicInfo.put("email", b.has("email") ? b.get("email").asText() : "");
            basicInfo.put("phone", b.has("phone") ? b.get("phone").asText() : "");
            basicInfo.put("position", b.has("label") ? b.get("label").asText() : "");
        }
        data.put("basicInfo", basicInfo);
        data.put("summary", root.has("basics") && root.get("basics").has("summary") ?
                root.get("basics").get("summary").asText() : "");
        // work
        List<Map<String, String>> work = new ArrayList<>();
        if (root.has("work")) {
            for (JsonNode w : root.get("work")) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("company", w.has("company") ? w.get("company").asText() : "");
                item.put("position", w.has("position") ? w.get("position").asText() : "");
                item.put("period", (w.has("startDate") ? w.get("startDate").asText() : "") + " - " +
                        (w.has("endDate") ? w.get("endDate").asText() : "至今"));
                item.put("description", w.has("summary") ? w.get("summary").asText() : "");
                work.add(item);
            }
        }
        data.put("workExperience", work);
        // education
        List<Map<String, String>> edu = new ArrayList<>();
        if (root.has("education")) {
            for (JsonNode e : root.get("education")) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("school", e.has("institution") ? e.get("institution").asText() : "");
                item.put("major", e.has("area") ? e.get("area").asText() : "");
                item.put("degree", e.has("studyType") ? e.get("studyType").asText() : "");
                item.put("period", (e.has("startDate") ? e.get("startDate").asText() : "") + " - " +
                        (e.has("endDate") ? e.get("endDate").asText() : ""));
                edu.add(item);
            }
        }
        data.put("education", edu);
        // skills
        StringBuilder skills = new StringBuilder();
        if (root.has("skills")) {
            for (JsonNode s : root.get("skills")) {
                if (skills.length() > 0) skills.append(", ");
                skills.append(s.has("name") ? s.get("name").asText() : "");
            }
        }
        data.put("skills", skills.toString());
        data.put("projects", new ArrayList<>());
        try { return objectMapper.writeValueAsString(data); } catch (Exception e) { return "{}"; }
    }

    private Map<String, String> extractResumeSections(String text) {
        Map<String, String> sections = new LinkedHashMap<>();
        String[] keywords = {"summary", "experience", "education", "skills", "project",
                "个人简介", "工作经历", "教育背景", "技能", "项目经历",
                "professional summary", "work experience", "professional experience"};
        // Extract meaningful chunks based on common section headers
        StringBuilder summary = new StringBuilder();
        StringBuilder experience = new StringBuilder();
        StringBuilder education = new StringBuilder();
        StringBuilder skills = new StringBuilder();
        StringBuilder projects = new StringBuilder();

        String[] lines = text.split("(?<=\\.)\\s+|\\n");
        StringBuilder current = summary;
        for (String line : lines) {
            String lower = line.toLowerCase().trim();
            if (lower.contains("summary") || lower.contains("objective") || lower.contains("profile")) {
                current = summary; continue;
            }
            if (lower.contains("experience") || lower.contains("employment") || lower.contains("工作经历")) {
                current = experience; continue;
            }
            if (lower.contains("education") || lower.contains("教育背景") || lower.contains("academic")) {
                current = education; continue;
            }
            if (lower.contains("skill") || lower.contains("技能") || lower.contains("technical")) {
                current = skills; continue;
            }
            if (lower.contains("project") || lower.contains("项目") || lower.contains("portfolio")) {
                current = projects; continue;
            }
            if (line.length() > 5) current.append(line).append(" ");
        }
        sections.put("summary", summary.toString().trim());
        sections.put("experience", experience.toString().trim());
        sections.put("education", education.toString().trim());
        sections.put("skills", skills.toString().trim());
        sections.put("projects", projects.toString().trim());
        return sections;
    }

    private String toTemplateJson(Map<String, String> extracted, String title, String category) {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, String> basicInfo = new LinkedHashMap<>();
        basicInfo.put("name", title.replace(" 简历模板", "").replace(" 简历", ""));
        basicInfo.put("email", "");
        basicInfo.put("phone", "");
        basicInfo.put("position", title);
        data.put("basicInfo", basicInfo);
        data.put("summary", extracted.getOrDefault("summary", ""));
        data.put("workExperience", new ArrayList<>());
        data.put("education", new ArrayList<>());
        data.put("skills", extracted.getOrDefault("skills", ""));
        data.put("projects", new ArrayList<>());
        try { return objectMapper.writeValueAsString(data); } catch (Exception e) { return "{}"; }
    }

    private String classifyCategory(Map<String, String> extracted) {
        String all = String.join(" ", extracted.values()).toLowerCase();
        if (all.contains("java") || all.contains("python") || all.contains("react") ||
                all.contains("vue") || all.contains("code") || all.contains("software") ||
                all.contains("engineer") || all.contains("开发")) return "技术开发";
        if (all.contains("product") || all.contains("design") || all.contains("ux") ||
                all.contains("ui") || all.contains("产品") || all.contains("设计")) return "产品设计";
        if (all.contains("market") || all.contains("sale") || all.contains("运营") ||
                all.contains("市场")) return "市场运营";
        if (all.contains("manager") || all.contains("director") || all.contains("管理")) return "管理";
        return "应届生";
    }

    // --- built-in templates ---

    private static String buildTechResume(String role, String skillsStr) {
        String position = role + "开发工程师";
        return """
        {"basicInfo":{"name":"","email":"","phone":"","position":"%s"},"summary":"具有 5 年以上 %s 开发经验，熟悉团队协作与敏捷开发流程。参与过多个核心业务系统的架构设计与性能优化，具备良好的编码习惯和问题解决能力。","workExperience":[{"company":"某科技公司","position":"高级%s","period":"2020-01 - 至今","description":"负责核心业务系统的架构设计与开发，主导技术方案评审，推动代码规范落地。优化数据库查询性能，将核心接口响应时间降低 40%%。参与微服务架构升级，完成 3 个核心服务的拆分与上线。"},{"company":"某互联网公司","position":"%s","period":"2017-07 - 2019-12","description":"参与电商平台订单系统开发，对接支付、物流等多个上下游服务。编写单元测试，保证代码覆盖率达到 85%% 以上。参与技术分享与 Code Review，帮助团队成员成长。"}],"education":[{"school":"某理工大学","major":"计算机科学与技术","degree":"本科","period":"2013-09 - 2017-06"}],"skills":"%s","projects":[{"name":"电商订单管理系统","role":"后端负责人","description":"基于 Spring Boot 构建微服务架构，实现订单全生命周期管理，支持高并发场景下的分布式事务处理。"},{"name":"API 网关平台","role":"核心开发","description":"搭建统一 API 网关，集成限流、熔断、认证等能力，日处理请求量超过 500 万次。"}]}
        """.formatted(position, role, position, position, skillsStr);
    }

    private static String buildProductResume() {
        return """
        {"basicInfo":{"name":"","email":"","phone":"","position":"产品经理"},"summary":"拥有 4 年互联网产品经理经验，擅长从 0 到 1 的产品规划与落地。具备优秀的用户需求分析和数据分析能力，主导过 DAU 百万级产品的迭代优化。","workExperience":[{"company":"某互联网平台","position":"高级产品经理","period":"2020-03 - 至今","description":"负责核心产品线的规划与迭代，通过数据驱动优化产品体验，用户留存率提升 25%%。主导跨部门协作项目，协调设计、开发、运营团队完成 3 个大版本迭代。输出高质量 PRD，善于用数据和用户反馈验证产品假设。"},{"company":"某科技公司","position":"产品经理","period":"2017-06 - 2020-02","description":"负责 B 端 SaaS 产品的需求分析与功能设计，对接 50+ 企业客户需求。推动数据看板功能上线，客户满意度从 72%% 提升至 91%%。"}],"education":[{"school":"某大学","major":"工商管理","degree":"硕士","period":"2015-09 - 2017-06"}],"skills":"Axure, Figma, JIRA, SQL, 数据分析, 用户研究, 敏捷项目管理","projects":[{"name":"智能推荐系统","role":"产品负责人","description":"从零搭建个性化推荐系统，通过协同过滤和内容推荐算法，将用户点击率提升 35%%。"}]}
        """;
    }

    private static String buildMarketingResume() {
        return """
        {"basicInfo":{"name":"","email":"","phone":"","position":"市场营销经理"},"summary":"拥有 6 年市场营销经验，深耕数字营销与品牌策略。擅长整合线上线下营销资源，操盘过年预算超 500 万的营销活动。","workExperience":[{"company":"某消费品牌","position":"市场营销经理","period":"2019-05 - 至今","description":"制定年度营销战略，管理 10 人营销团队，年度营销 ROI 达到 1：4.5。主导双十一整合营销活动，GMV 同比增长 60%%。搭建品牌私域流量体系，微信生态粉丝增长 30 万+。"},{"company":"某广告公司","position":"数字营销专员","period":"2016-08 - 2019-04","description":"负责客户社交媒体账号运营，策划并执行线上活动，平均互动率提升 50%%。管理 SEM/信息流广告投放，获客成本降低 30%%。"}],"education":[{"school":"某师范大学","major":"广告学","degree":"本科","period":"2012-09 - 2016-06"}],"skills":"品牌策划, 数字营销, SEO/SEM, 数据分析(GA/百度统计), 社交媒体运营, 内容营销","projects":[{"name":"品牌升级项目","role":"项目负责人","description":"主导品牌视觉和定位升级，统一线上线下触达体验，品牌知名度提升 40%%。"}]}
        """;
    }

    private static String buildFreshGraduateResume() {
        return """
        {"basicInfo":{"name":"","email":"","phone":"","position":"应届毕业生"},"summary":"2024 届应届毕业生，专业基础扎实，学习能力强。在校期间积极参与项目和实习，具备良好的团队协作能力和快速学习能力。期望在充满挑战的环境中成长。","workExperience":[{"company":"某科技有限公司","position":"实习生","period":"2023-06 - 2023-09","description":"参与日常开发工作，协助完成 2 个功能模块的编码与测试。学习并实践 Git 协作流程和敏捷开发方法，获得导师好评。"}],"education":[{"school":"某大学","major":"计算机科学与技术","degree":"本科","period":"2020-09 - 2024-06"}],"skills":"Java, Python, SQL, Git, 英语 CET-6, Office 办公套件","projects":[{"name":"校园二手交易平台","role":"项目负责人","description":"作为毕业设计项目，使用 Spring Boot + Vue 搭建校园二手交易微信小程序，用户量 500+，获得校级优秀毕业设计。"},{"name":"智能课表助手","role":"开发者","description":"参加 Hackathon 比赛项目，基于 AI 算法实现智能排课建议，获得二等奖。"}]}
        """;
    }

    private static String buildManagerResume() {
        return """
        {"basicInfo":{"name":"","email":"","phone":"","position":"项目经理"},"summary":"PMP 认证，10 年 IT 项目管理经验。成功交付过 15+ 大型项目，累计管理预算超过 2000 万。擅长跨部门沟通、风险管控和团队建设。","workExperience":[{"company":"某大型科技集团","position":"高级项目经理","period":"2018-01 - 至今","description":"同时管理 3 个重点项目，协调 50+ 人跨部门团队。建立项目风险管理体系，将项目延期率从 20%% 降低至 5%%。推动敏捷转型，团队交付效率提升 40%%。"},{"company":"某软件公司","position":"项目经理","period":"2013-03 - 2017-12","description":"负责政务信息化项目交付，管理客户期望与项目范围。成功交付 8 个项目，总金额超 1500 万，客户满意度 95%% 以上。"}],"education":[{"school":"某交通大学","major":"信息管理与信息系统","degree":"硕士","period":"2010-09 - 2013-03"}],"skills":"PMP, Scrum Master, JIRA, MS Project, 风险管理, 敏捷/瀑布, 需求分析","projects":[{"name":"智慧城市数据中台","role":"项目总监","description":"统筹 2000 万预算项目，带领 80+ 人团队，历时 18 个月成功交付，获评省级示范项目。"}]}
        """;
    }
}
