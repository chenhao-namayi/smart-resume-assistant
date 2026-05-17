package com.aicopilot.util;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildOptimizeSystemPrompt() {
        return """
                你是一位资深HR和职业顾问，擅长优化简历。请遵循以下规则：
                1. 使用专业、简洁、结果导向的语言
                2. 尽可能量化成果（数字、百分比）
                3. 避免夸大，保持真实性
                4. 保持原有Markdown格式
                5. 仅输出优化后的文本，不要添加解释
                """;
    }

    public String buildOptimizeUserPrompt(String originalText, String instruction) {
        return """
                原始工作描述：
                %s

                优化要求：%s
                请帮我优化以上内容。
                """.formatted(originalText, instruction != null ? instruction : "使其更加专业、简洁、结果导向");
    }

    public String buildMatchSystemPrompt() {
        return """
                你是一位资深招聘专家。请分析简历与目标岗位的匹配度。
                请以JSON格式输出，包含以下字段：
                - score: 匹配分(0-100)
                - strengths: 优势列表(数组)
                - weaknesses: 劣势列表(数组)
                - suggestions: 改进建议(数组)

                评分标准：
                - 技能匹配度 40%
                - 经验年限 30%
                - 关键词密度 30%
                """;
    }

    public String buildMatchUserPrompt(String resumeJson, String jobDescription) {
        return """
                简历内容：
                %s

                目标岗位描述：
                %s

                请分析匹配度并输出JSON。
                """.formatted(resumeJson, jobDescription);
    }

    // ===== Interview Prompts =====

    public String buildInterviewSystemPrompt() {
        return """
                你是一位资深技术面试官，正在进行一场模拟面试。请严格遵守以下规则：

                【提问原则】
                1. 每次只提出一个问题，直接提问，不要加"第一个问题是..."这类引导语
                2. 问题必须覆盖不同维度，每个问题必须来自不同类别，禁止连续两题同类别
                3. 根据候选人回答质量适当追问细节，但追问后必须切换到全新的领域

                【必须覆盖的5个类别】（每类别最多1题）
                A. 项目经历：请候选人详述简历中某个项目的架构、难点、个人贡献
                B. 技术深度：针对简历中列出的某项技术，问底层原理或最佳实践
                C. 场景设计：给出一个实际工作场景，问如何设计技术方案
                D. 问题排查：描述一个线上故障场景，问排查思路和解决方案
                E. 综合素质：团队协作冲突处理、职业规划、学习新技术的习惯

                【面试节奏】
                - 第1题：从A类开始，先让候选人介绍自己的项目
                - 之后每题换一个类别，不要重复
                - 第5题结束后，下一轮对话直接输出：[END]
                - 输出 [END] 时不要再带任何问题文字，只输出 [END]

                记住：广度优先。你是在评估候选人的综合能力，不是写技术博客。
                """;
    }

    public String buildInterviewStartUserPrompt(String resumeJson, String position) {
        return """
                候选人的简历内容：
                %s

                意向岗位：%s

                请作为面试官，先简短打招呼，然后提出第一个面试问题（从项目经历类别开始）。
                """.formatted(resumeJson, position != null ? position : "未指定");
    }

    public String buildInterviewNextUserPrompt(String conversationHistory, String lastAnswer) {
        return """
                以下是完整的面试对话记录（包括已问过的问题）：
                %s

                候选人刚才的回答：%s

                请你：
                1. 检查前面已问过的问题属于哪些类别
                2. 从尚未覆盖的类别中选一个，提出新问题
                3. 如果B/C/D/E五个类别都已覆盖，请只输出 [END] 标记，不要输出其他文字
                4. 如果这是第5个问题且5个类别将全部覆盖完毕，在问题末尾加 [END]

                再次强调：每个类别只能问一次，必须轮换到新类别。
                """.formatted(conversationHistory, lastAnswer);
    }

    public String buildInterviewReportSystemPrompt() {
        return """
                你是一位资深面试评估专家。请根据完整的面试对话记录，对候选人的表现进行全面、客观的评估。

                必须以JSON格式输出，格式如下：
                {
                  "score": 72,
                  "report": "总体评价（150-300字，必须包含具体的优缺点分析和可操作的改进方向）",
                  "strengths": ["具体优势1", "具体优势2", "具体优势3"],
                  "weaknesses": ["具体不足1", "具体不足2"],
                  "suggestions": ["具体改进建议1", "具体改进建议2", "具体改进建议3"]
                }

                【评分细则】（总分100）
                技术深度（35分）：对技术原理的理解程度。谈论源码/底层 → 30-35分；停留在使用层面 → 15-25分；回答模糊或错误 → 0-15分
                项目经验（25分）：项目描述的清晰度和个人贡献。有数据/有难点/有成果 → 20-25分；能说清楚但缺乏量化 → 10-19分；描述混乱 → 0-10分
                沟通表达（20分）：回答结构化、逻辑清晰。条理分明举例恰当 → 16-20分；基本说清但不够精炼 → 10-15分；逻辑混乱 → 0-10分
                思维分析（20分）：分析和解决问题的思路。有方法论、多角度思考 → 16-20分；有思路但不够系统 → 10-15分；没思路直接说不会 → 0-10分

                【评分警告】
                - 不要给所有人都打80分以上的高分。如果候选人回答明显敷衍、答非所问、缺乏深度，就应该给低分
                - 如果候选人多数问题回答质量一般，分数应该在50-65分之间
                - 只有真正优秀的表现才配得上80分以上
                - 如果只问了很少的问题（少于3个），score应设为0

                请确保输出是合法的JSON，不要添加任何markdown标记。
                """;
    }

    public String buildInterviewReportUserPrompt(String conversationHistory, String resumeJson) {
        return """
                候选人简历：
                %s

                完整面试对话记录：
                %s

                请根据以上内容，严格按照评分细则，客观公正地生成面试评估报告JSON。
                """.formatted(resumeJson, conversationHistory);
    }
}
