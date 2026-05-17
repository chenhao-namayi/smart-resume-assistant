#!/usr/bin/env python3
"""Write all PlantUML source files for thesis diagrams"""
import os

out = r'c:\Users\ch269\Desktop\puml'
os.makedirs(out, exist_ok=True)

# ===== 图3-1: 简历管理用例图 =====
with open(os.path.join(out, 'fig3_1_usecase.puml'), 'w', encoding='utf-8') as f:
    f.write("""@startuml
title 图3-1 简历管理功能用例图

left to right direction
actor "普通用户" as User

rectangle "简历管理系统" {
  usecase "创建简历" as UC1
  usecase "编辑简历" as UC2
  usecase "查看简历列表" as UC3
  usecase "删除简历" as UC4
  usecase "创建版本" as UC5
  usecase "上传证件照" as UC6
  usecase "选择模板" as UC7
  usecase "导出PDF" as UC8
}

User --> UC1
User --> UC2
User --> UC3
User --> UC4
User --> UC5
User --> UC6
User --> UC7
User --> UC8

UC5 ..> UC2 : <<extend>>
UC8 ..> UC2 : <<extend>>
UC7 ..> UC1 : <<include>>

@enduml
""")

# ===== 图3-2: AI模拟面试用例图 =====
with open(os.path.join(out, 'fig3_2_interview.puml'), 'w', encoding='utf-8') as f:
    f.write("""@startuml
title 图3-2 AI模拟面试功能用例图

left to right direction
actor "普通用户" as User

rectangle "AI模拟面试系统" {
  usecase "开始面试" as UC1
  usecase "文字回答" as UC2
  usecase "语音回答" as UC3
  usecase "结束面试" as UC4
  usecase "查看面试报告" as UC5
  usecase "查看面试历史" as UC6
  usecase "删除面试记录" as UC7
  usecase "大语言模型\n(DeepSeek-V4)" as LLM
}

User --> UC1
User --> UC2
User --> UC3
User --> UC4
User --> UC5
User --> UC6
User --> UC7

UC1 ..> LLM : <<use>>
UC2 ..> LLM : <<use>>
UC3 ..> LLM : <<use>>
UC4 ..> UC5 : <<extend>>
UC6 --> UC5 : <<include>>
UC6 --> UC7 : <<extend>>

@enduml
""")

# ===== 图3-3: 管理后台用例图 =====
with open(os.path.join(out, 'fig3_3_admin.puml'), 'w', encoding='utf-8') as f:
    f.write("""@startuml
title 图3-3 管理后台功能用例图

left to right direction
actor "管理员" as Admin

rectangle "管理后台系统" {
  usecase "查看统计概览" as UC1
  usecase "管理用户" as UC2
  usecase "编辑用户信息" as UC2a
  usecase "修改用户角色" as UC2b
  usecase "删除用户" as UC2c
  usecase "管理简历" as UC3
  usecase "删除用户简历" as UC3a
  usecase "管理模板" as UC4
  usecase "新增模板" as UC4a
  usecase "编辑模板" as UC4b
  usecase "删除模板" as UC4c
  usecase "查看优化日志" as UC5
  usecase "查看匹配记录" as UC6
  usecase "查看面试记录" as UC7
}

Admin --> UC1
Admin --> UC2
Admin --> UC3
Admin --> UC4
Admin --> UC5
Admin --> UC6
Admin --> UC7

UC2 <.. UC2a : <<extend>>
UC2 <.. UC2b : <<extend>>
UC2 <.. UC2c : <<extend>>
UC3 <.. UC3a : <<extend>>
UC4 <.. UC4a : <<extend>>
UC4 <.. UC4b : <<extend>>
UC4 <.. UC4c : <<extend>>

@enduml
""")

# ===== 图3-4: 系统总体用例图 =====
with open(os.path.join(out, 'fig3_4_overall.puml'), 'w', encoding='utf-8') as f:
    f.write("""@startuml
title 图3-4 系统总体用例图

left to right direction

actor "普通用户\\n(USER)" as User
actor "管理员\\n(ADMIN)" as Admin

rectangle "AI Resume Copilot 系统" {

  package "用户认证" {
    usecase "注册" as Auth1
    usecase "登录" as Auth2
    usecase "JWT令牌认证" as Auth3
  }

  package "简历管理" {
    usecase "创建简历" as R1
    usecase "编辑简历" as R2
    usecase "版本管理" as R3
    usecase "PDF导出" as R4
  }

  package "AI核心功能" {
    usecase "AI智能优化" as AI1
    usecase "岗位匹配分析" as AI2
    usecase "AI模拟面试" as AI3
  }

  package "管理后台" {
    usecase "统计概览" as M1
    usecase "用户管理" as M2
    usecase "简历管理" as M3
    usecase "模板管理" as M4
    usecase "日志查看" as M5
  }
}

User --> Auth1
User --> Auth2
User --> R1
User --> R2
User --> R3
User --> R4
User --> AI1
User --> AI2
User --> AI3

Admin --> Auth2
Admin -up-> M1
Admin -up-> M2
Admin -up-> M3
Admin -up-> M4
Admin -up-> M5

note right of Admin
  管理员拥有普通用户的
  全部权限，并额外拥有
  管理后台访问权限
end note

@enduml
""")

# ===== 图4-1: 系统总体架构图 (Deployment + Component) =====
with open(os.path.join(out, 'fig4_1_arch.puml'), 'w', encoding='utf-8') as f:
    f.write("""@startuml
title 图4-1 系统总体架构图

skinparam rectangle {
  BackgroundColor<<frontend>> #E3F2FD
  BackgroundColor<<api>> #FFF3E0
  BackgroundColor<<business>> #E8F5E9
  BackgroundColor<<data>> #FCE4EC
  BackgroundColor<<storage>> #F3E5F5
  BackgroundColor<<external>> #FFF9C4
  BorderColor #555555
}

rectangle "表示层 (Presentation Layer)\\n━━━━━━━━━━━━━━━━━\\nVue 3 + Element Plus + Axios\\n运行环境: 用户浏览器 (Chrome/Edge/Firefox)\\nSPA单页面应用, 负责界面渲染与用户交互" <<frontend>> as L1

rectangle "接口层 (API Layer)\\n━━━━━━━━━━━━━━━━━\\nSpring MVC RestController\\nAuthController | ResumeController | AIController\\nInterviewController | AdminController\\n统一ApiResponse<T>响应格式 | @Valid参数校验" <<api>> as L2

rectangle "业务逻辑层 (Business Logic Layer)\\n━━━━━━━━━━━━━━━━━\\nUserService | ResumeService | LLMService\\nJobAnalysisService | InterviewService | AdminService\\nPromptBuilder (提示词管理) | JwtUtil (令牌工具)\\n@Transactional事务管理 | 业务规则与流程编排" <<business>> as L3

rectangle "数据访问层 (Data Access Layer)\\n━━━━━━━━━━━━━━━━━\\nSpring Data JPA (Hibernate ORM)\\nUserRepository | ResumeRepository | TemplateRepository\\nJobAnalysisRepository | OptimizationLogRepository\\nInterviewSessionRepository | Pageable分页查询" <<data>> as L4

rectangle "数据存储层 (Data Storage)\\n━━━━━━━━━━━━━━━━━\\nMySQL 8.0 生产库 | H2 内存数据库(开发)\\nHikariCP 连接池 | DDL Auto-Update" <<storage>> as L5

rectangle "外部服务\\n━━━━━━━━\\nDeepSeek-V4\\n大语言模型 API" <<external>> as EXT

L1 -down-> L2 : HTTP/JSON\\n(Axios + JWT)
L2 -down-> L3 : 方法调用\\n(依赖注入)
L3 -down-> L4 : Repository接口\\n(Spring Data JPA)
L4 -down-> L5 : JDBC/SQL\\n(Hibernate ORM)
L3 -left-> EXT : HTTP POST\\n(WebClient调用)

note right of L1
  Vite Dev Server
  端口: 5173
  代理 /api → 8080
end note

note right of EXT
  API Endpoint
  /v1/messages
  Anthropic兼容格式
end note

footer 基础设施: Spring Boot 3.2 | Maven 3.9 | JDK 21 | Windows 11 | Git

@enduml
""")

# ===== 图4-2: 系统功能模块结构图 (Component Diagram) =====
with open(os.path.join(out, 'fig4_2_modules.puml'), 'w', encoding='utf-8') as f:
    f.write("""@startuml
title 图4-2 系统功能模块结构图

skinparam component {
  BackgroundColor<<user>> #E8F5E9
  BackgroundColor<<admin>> #FCE4EC
  BorderColor #555555
}

[AI Resume Copilot 系统] as ROOT

package "用户端功能模块" <<user>> {
  [用户认证\\n注册/登录/JWT令牌] as M1
  [简历管理\\nCRUD/版本控制/PDF导出] as M2
  [模板系统\\n模板选择与预览] as M3
  [AI智能优化\\n分段优化/全文优化] as M4
  [岗位匹配分析\\n多维评分/建议生成] as M5
  [AI模拟面试\\n五维度面试/语音输入] as M6
  [面试记录\\n历史查看/报告浏览] as M7
}

package "管理端功能模块" <<admin>> {
  [统计概览\\n六维核心指标] as A1
  [用户管理\\n编辑信息/角色/删除] as A2
  [简历管理\\n查看/删除用户简历] as A3
  [模板管理\\n增删改查模板] as A4
  [优化日志\\n分页查看AI调用记录] as A5
  [匹配记录\\n岗位分析历史] as A6
  [面试记录\\n查看所有面试会话] as A7
}

ROOT -down-> M1
ROOT -down-> M2
ROOT -down-> M3
ROOT -down-> M4
ROOT -down-> M5
ROOT -down-> M6
ROOT -down-> M7

ROOT -down-> A1
ROOT -down-> A2
ROOT -down-> A3
ROOT -down-> A4
ROOT -down-> A5
ROOT -down-> A6
ROOT -down-> A7

M2 --> M3 : <<include>>
M4 --> M2 : <<extend>>
M5 --> M2 : <<extend>>
M6 --> M2 : <<extend>>
M7 --> M6 : <<include>>

A2 --> A3 : <<include>>

@enduml
""")

# ===== 图4-3: 数据库E-R图 =====
with open(os.path.join(out, 'fig4_3_er.puml'), 'w', encoding='utf-8') as f:
    f.write("""@startuml
title 图4-3 系统数据库E-R图

!define table(x) class x << (T,#FFAAAA) >>
!define pk(x) <b><color:blue>x</color></b>
!define fk(x) <color:red>x</color>

entity "users (用户表)" as users {
  * pk(id) : BIGINT <<PK>>
  --
  * username : VARCHAR(50) <<UK>>
  * password : VARCHAR(255)
  email : VARCHAR(100)
  * role : VARCHAR(10) <<DEFAULT 'USER'>>
  * created_at : DATETIME
}

entity "resumes (简历表)" as resumes {
  * pk(id) : BIGINT <<PK>>
  --
  * fk(user_id) : BIGINT <<FK>>
  title : VARCHAR(100)
  * version : INT <<DEFAULT 1>>
  content_json : LONGTEXT
  fk(optimized_from) : BIGINT <<FK(SELF)>>
  is_current : BOOLEAN
  * created_at : DATETIME
  updated_at : DATETIME
}

entity "templates (模板表)" as templates {
  * pk(id) : BIGINT <<PK>>
  --
  * name : VARCHAR(100)
  category : VARCHAR(50)
  description : VARCHAR(500)
  * content_json : LONGTEXT
  source_url : VARCHAR(500)
  * created_at : DATETIME
}

entity "job_analyses (岗位分析表)" as job {
  * pk(id) : BIGINT <<PK>>
  --
  fk(user_id) : BIGINT <<FK>>
  fk(resume_id) : BIGINT <<FK>>
  job_description : TEXT
  match_score : DECIMAL(5,2)
  suggestions : TEXT
  * created_at : DATETIME
}

entity "optimization_logs (优化日志表)" as opt {
  * pk(id) : BIGINT <<PK>>
  --
  * fk(resume_id) : BIGINT <<FK>>
  prompt_used : TEXT
  llm_model : VARCHAR(50)
  input_text : LONGTEXT
  output_text : LONGTEXT
  response_time_ms : INT
  section_type : VARCHAR(50)
  * created_at : DATETIME
}

entity "interview_sessions (面试会话表)" as interview {
  * pk(id) : BIGINT <<PK>>
  --
  * fk(user_id) : BIGINT <<FK>>
  * fk(resume_id) : BIGINT <<FK>>
  position : VARCHAR(100)
  messages : LONGTEXT
  * status : VARCHAR(20) <<DEFAULT 'IN_PROGRESS'>>
  score : INT
  report : TEXT
  strengths : TEXT
  weaknesses : TEXT
  suggestions : TEXT
  * created_at : DATETIME
  completed_at : DATETIME
}

users ||--o{ resumes : "1 创建 N"
users ||--o{ job : "1 执行 N"
users ||--o{ interview : "1 参加 N"
resumes ||--o{ job : "1 被分析 N"
resumes ||--o{ opt : "1 产生 N"
resumes ||--o{ interview : "1 用于 N"
resumes ||--o| resumes : "optimized_from\\n(版本链)"

@enduml
""")

# ===== 图4-4: AI模拟面试活动图 =====
with open(os.path.join(out, 'fig4_4_activity.puml'), 'w', encoding='utf-8') as f:
    f.write("""@startuml
title 图4-4 AI模拟面试活动图

|用户|\\t系统\\t|LLM|
|前端界面|后端服务|DeepSeek-V4|

start

:用户选择简历\\n(可选填意向岗位);

:POST /interview/start\\n携带resumeId和position;

if (简历归属校验?) then (否)
  :返回404错误;
  stop
else (是)
endif

:发送System Prompt\\n+简历JSON + 岗位信息;

:LLM生成开场白\\n+第一个面试问题;

:创建InterviewSession\\nstatus=IN_PROGRESS;

:返回sessionId\\n+第一个问题;

:显示面试对话界面\\n等待用户输入;

repeat
  :用户输入回答\\n(文字/语音);

  :POST /interview/answer\\n携带sessionId和answer;

  :追加用户回答\\n到messages数组;

  :统计已提问数量\\ncountAiMessages();

  if (问题数 ≥ 6?) then (是)
    #FFCCCC:自动触发结束流程;
    break
  else (否)
  endif

  :拼接对话历史\\n构建续问Prompt;

  :LLM根据对话历史\\n选择未覆盖的维度\\n生成下一个问题;

  if (LLM返回[END]标记?) then (是)
    #FFCCCC:自动触发结束流程;
    break
  else (否)
  endif

  :追加AI新问题\\n到messages数组;

  :返回下一个问题\\n到前端展示;

repeat while (面试进行中?)

:系统拼接完整对话历史\\n构建报告评估Prompt;

:LLM生成评估报告JSON\\n(score/report/strengths/\\nweaknesses/suggestions);

:解析报告JSON\\n更新InterviewSession\\nstatus=COMPLETED;

:返回InterviewReportResponse;

:前端渲染报告页面\\n展示评分环+详情列表;

stop

@enduml
""")

# ===== 图1-1: 传统 vs AI 流程对比 (Activity + Swimlane) =====
with open(os.path.join(out, 'fig1_1_compare.puml'), 'w', encoding='utf-8') as f:
    f.write("""@startuml
title 图1-1 传统简历撰写流程与AI辅助简历撰写流程对比

|传统方式 (3-7天)|
start
:求职者自行回忆\\n工作经历和教育背景;
:手动编写简历内容\\n(Word/在线编辑器);
:参考网络模板\\n调整格式排版;
:自我检查修改\\n反复润色;
:投递简历;
if (收到面试邀请?) then (否)
  :自行分析原因\\n再次修改简历;
  note right: 缺乏专业反馈\\n效率低下
else (是)
  :进入面试;
endif
stop

|AI Resume Copilot (1-2小时)|
start
:选择简历模板\\n快速创建简历框架;
:AI智能优化\\n各模块内容;
:岗位匹配分析\\n量化评估匹配度;
:AI模拟面试\\n实战演练;
partition "AI持续反馈优化" {
  :根据分析结果\\n修改简历;
  :再次AI优化;
  :导出PDF简历;
}
:投递简历;
stop

@enduml
""")

print('All PlantUML source files written.')
for f in sorted(os.listdir(out)):
    print(f'  {f}')
