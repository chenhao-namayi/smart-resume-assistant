#!/usr/bin/env python3
"""Generate all 9 thesis figures using matplotlib"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch, Circle, Polygon, Arc, Rectangle, FancyArrow
from matplotlib.lines import Line2D
import matplotlib.patches as mpatches
import numpy as np
import os

out_dir = r'c:\Users\ch269\Desktop\thesis_figures'
os.makedirs(out_dir, exist_ok=True)

plt.rcParams['font.sans-serif'] = ['SimHei', 'Microsoft YaHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

def fig1_1():
    """图1-1: 传统简历撰写流程 vs AI辅助简历撰写流程对比"""
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 7))
    fig.suptitle('图1-1 传统简历撰写流程与AI辅助简历撰写流程对比', fontsize=14, fontweight='bold', y=0.98)

    # Traditional flow
    ax1.set_xlim(0, 10); ax1.set_ylim(0, 10); ax1.axis('off')
    ax1.set_title('传统简历撰写流程', fontsize=13, fontweight='bold', pad=15)

    trad_steps = [
        (5, 9, '求职者自行\n回忆经历', '#FFE0E0'),
        (5, 7.2, '手动编写\n简历内容', '#FFE0E0'),
        (5, 5.4, '参考模板\n调整格式', '#FFE0E0'),
        (5, 3.6, '自我检查\n修改润色', '#FFE0E0'),
        (5, 1.8, '投递简历', '#FFE0E0'),
    ]
    for i, (x, y, text, color) in enumerate(trad_steps):
        rect = FancyBboxPatch((x-2,y-0.6), 4, 1.2, boxstyle="round,pad=0.1", facecolor=color, edgecolor='#cc0000', linewidth=1.5)
        ax1.add_patch(rect)
        ax1.text(x, y, text, ha='center', va='center', fontsize=10)
        if i < len(trad_steps)-1:
            ax1.annotate('', xy=(x, trad_steps[i+1][1]+0.6), xytext=(x, y-0.6),
                        arrowprops=dict(arrowstyle='->', lw=1.8, color='#666'))

    ax1.text(5, 0.3, '耗时：3-7天 | 质量依赖个人经验 | 无法针对性优化', ha='center', fontsize=9, color='#cc0000', style='italic')

    # AI-assisted flow
    ax2.set_xlim(0, 10); ax2.set_ylim(0, 10); ax2.axis('off')
    ax2.set_title('AI Resume Copilot 辅助流程', fontsize=13, fontweight='bold', pad=15)

    ai_steps = [
        (5, 9, '选择模板\n快速创建', '#E0FFE0'),
        (5, 7.5, 'AI智能优化\n各模块内容', '#E0FFE0'),
        (5, 6.0, '岗位匹配分析\n量化评估', '#E0FFE0'),
        (5, 4.5, 'AI模拟面试\n实战演练', '#E0FFE0'),
        (5, 3.0, '优化修改\n导出PDF', '#E0FFE0'),
        (5, 1.5, '投递简历', '#E0FFE0'),
    ]
    for i, (x, y, text, color) in enumerate(ai_steps):
        rect = FancyBboxPatch((x-2,y-0.55), 4, 1.1, boxstyle="round,pad=0.1", facecolor=color, edgecolor='#006600', linewidth=1.5)
        ax2.add_patch(rect)
        ax2.text(x, y, text, ha='center', va='center', fontsize=10)
        if i < len(ai_steps)-1:
            ax2.annotate('', xy=(x, ai_steps[i+1][1]+0.55), xytext=(x, y-0.55),
                        arrowprops=dict(arrowstyle='->', lw=1.8, color='#666'))

    # AI loop arrow on the side
    ax2.annotate('AI持续反馈优化', xy=(8.2, 5.5), fontsize=9, color='#006600', ha='center',
                bbox=dict(boxstyle='round', facecolor='#f0fff0', edgecolor='#006600'))
    ax2.annotate('', xy=(8, 3.5), xytext=(8, 7.5),
                arrowprops=dict(arrowstyle='<->', lw=1.2, color='#006600', linestyle='dashed'))

    ax2.text(5, 0.3, '耗时：1-2小时 | AI辅助提升质量 | 多维度优化', ha='center', fontsize=9, color='#006600', style='italic')

    plt.tight_layout()
    plt.savefig(os.path.join(out_dir, '图1-1_流程对比.png'), dpi=200, bbox_inches='tight')
    plt.close()
    print('图1-1 完成')

def fig3_1():
    """图3-1: 简历管理功能用例图"""
    fig, ax = plt.subplots(figsize=(11, 8))
    ax.set_xlim(0, 14); ax.set_ylim(0, 10); ax.axis('off')
    ax.set_title('图3-1 简历管理功能用例图', fontsize=14, fontweight='bold', pad=10)

    # Actor
    ax.plot(1.5, 5, 'o', markersize=12, color='#333')
    ax.plot([1.5, 1.5], [4.5, 2.5], 'k-', lw=2)
    ax.plot([0.8, 2.2], [3.5, 3.5], 'k-', lw=2)
    ax.plot([1.5, 0.8], [2.5, 1.5], 'k-', lw=2)
    ax.plot([1.5, 2.2], [2.5, 1.5], 'k-', lw=2)
    ax.text(1.5, 1.2, '普通用户', ha='center', fontsize=11, fontweight='bold')

    # System boundary
    rect = FancyBboxPatch((4, 1), 9, 8.5, boxstyle="round,pad=0.3", facecolor='#f8f8ff', edgecolor='#666', linewidth=2, linestyle='--')
    ax.add_patch(rect)
    ax.text(13.2, 9.0, '简历管理系统', fontsize=10, color='#666', ha='right')

    # Use cases
    cases = [
        (7, 8.5, '创建简历'), (7, 7.2, '编辑简历'), (7, 5.9, '查看简历列表'),
        (7, 4.6, '删除简历'), (11, 7.2, '创建版本'), (11, 5.9, '上传证件照'),
        (11, 4.6, '选择模板'), (11, 3.3, '导出PDF'),
    ]
    for x, y, text in cases:
        ellipse = FancyBboxPatch((x-2.2, y-0.45), 4.4, 0.9, boxstyle="round,pad=0.05", facecolor='#fff', edgecolor='#3366cc', linewidth=1.5)
        ax.add_patch(ellipse)
        ax.text(x, y, text, ha='center', va='center', fontsize=10)

    # Connection lines from actor to use cases
    for x, y, _ in cases[:4]:
        ax.plot([3.5, x-2.2], [5, y], 'k-', lw=1, alpha=0.5)

    # <<extend>> relationships for version creation
    ax.annotate('', xy=(8.8, 6.35), xytext=(10.2, 6.8), arrowprops=dict(arrowstyle='->', lw=1, linestyle='dashed', color='#666'))
    ax.text(9.5, 6.8, '<<extend>>', fontsize=8, color='#666', ha='center')

    plt.tight_layout()
    plt.savefig(os.path.join(out_dir, '图3-1_简历管理用例图.png'), dpi=200, bbox_inches='tight')
    plt.close()
    print('图3-1 完成')

def fig3_2():
    """图3-2: AI模拟面试功能用例图"""
    fig, ax = plt.subplots(figsize=(12, 8))
    ax.set_xlim(0, 14); ax.set_ylim(0, 10); ax.axis('off')
    ax.set_title('图3-2 AI模拟面试功能用例图', fontsize=14, fontweight='bold', pad=10)

    # Actor
    ax.plot(1.5, 5, 'o', markersize=12, color='#333')
    ax.plot([1.5, 1.5], [4.5, 2.5], 'k-', lw=2)
    ax.plot([0.8, 2.2], [3.5, 3.5], 'k-', lw=2)
    ax.plot([1.5, 0.8], [2.5, 1.5], 'k-', lw=2)
    ax.plot([1.5, 2.2], [2.5, 1.5], 'k-', lw=2)
    ax.text(1.5, 1.2, '普通用户', ha='center', fontsize=11, fontweight='bold')

    # System boundary
    rect = FancyBboxPatch((4, 0.8), 9.5, 8.7, boxstyle="round,pad=0.3", facecolor='#f8f8ff', edgecolor='#666', linewidth=2, linestyle='--')
    ax.add_patch(rect)
    ax.text(13.7, 9.0, 'AI模拟面试系统', fontsize=10, color='#666', ha='right')

    # Use cases
    cases = [
        (7, 8.8, '开始面试'), (7, 7.5, '文字回答'), (7, 6.2, '语音回答'),
        (7, 4.9, '结束面试'), (11, 8.2, '查看面试报告'),
        (11, 6.9, '查看面试历史'), (11, 5.6, '删除面试记录'),
    ]
    for x, y, text in cases:
        ellipse = FancyBboxPatch((x-2.2, y-0.4), 4.4, 0.8, boxstyle="round,pad=0.05", facecolor='#fff', edgecolor='#3366cc', linewidth=1.5)
        ax.add_patch(ellipse)
        ax.text(x, y, text, ha='center', va='center', fontsize=10)

    # Connections
    ax.plot([3.5, 4.8], [5, 8.8], 'k-', lw=1, alpha=0.5)
    ax.plot([3.5, 4.8], [5, 4.9], 'k-', lw=1, alpha=0.5)

    # <<include>> from start
    ax.annotate('<<include>>', xy=(7.5, 8.0), fontsize=8, color='#666')
    # <<extend>> from end to report
    ax.annotate('', xy=(8.8, 5.3), xytext=(9.5, 7.8), arrowprops=dict(arrowstyle='->', lw=1, linestyle='dashed', color='#666'))
    ax.text(9.5, 6.6, '<<extend>>', fontsize=8, color='#666', ha='center')

    plt.tight_layout()
    plt.savefig(os.path.join(out_dir, '图3-2_面试用例图.png'), dpi=200, bbox_inches='tight')
    plt.close()
    print('图3-2 完成')

def fig3_3():
    """图3-3: 管理后台功能用例图"""
    fig, ax = plt.subplots(figsize=(12, 8))
    ax.set_xlim(0, 14); ax.set_ylim(0, 10); ax.axis('off')
    ax.set_title('图3-3 管理后台功能用例图', fontsize=14, fontweight='bold', pad=10)

    # Actor
    ax.plot(1.5, 5, 'o', markersize=12, color='#333')
    ax.plot([1.5, 1.5], [4.5, 2.5], 'k-', lw=2)
    ax.plot([0.8, 2.2], [3.5, 3.5], 'k-', lw=2)
    ax.plot([1.5, 0.8], [2.5, 1.5], 'k-', lw=2)
    ax.plot([1.5, 2.2], [2.5, 1.5], 'k-', lw=2)
    ax.text(1.5, 1.2, '管理员', ha='center', fontsize=11, fontweight='bold')

    # System boundary
    rect = FancyBboxPatch((4, 0.8), 9.5, 8.7, boxstyle="round,pad=0.3", facecolor='#f8f8ff', edgecolor='#666', linewidth=2, linestyle='--')
    ax.add_patch(rect)
    ax.text(13.7, 9.0, '管理后台系统', fontsize=10, color='#666', ha='right')

    # Use cases - 2 columns
    cases = [
        (7, 8.8, '查看统计概览'), (7, 7.5, '管理用户'), (7, 6.2, '管理简历'),
        (7, 4.9, '管理模板'), (11, 8.2, '查看优化日志'),
        (11, 6.9, '查看匹配记录'), (11, 5.6, '查看面试记录'),
    ]
    for x, y, text in cases:
        ellipse = FancyBboxPatch((x-2.2, y-0.4), 4.4, 0.8, boxstyle="round,pad=0.05", facecolor='#fff', edgecolor='#cc3366', linewidth=1.5)
        ax.add_patch(ellipse)
        ax.text(x, y, text, ha='center', va='center', fontsize=10)

    # Sub-cases under 管理用户
    ax.plot([7, 7], [5.5, 7.1], 'k-', lw=0.8, alpha=0.4)
    sub_cases = [(6.5, 5.8, '编辑用户\n信息'), (7.5, 5.8, '修改用户\n角色')]
    for sx, sy, st in sub_cases:
        e = FancyBboxPatch((sx-1.2, sy-0.35), 2.4, 0.7, boxstyle="round,pad=0.03", facecolor='#fff0f0', edgecolor='#cc3366', linewidth=1)
        ax.add_patch(e)
        ax.text(sx, sy, st, ha='center', va='center', fontsize=8)

    # Connection
    ax.plot([2.8, 4.8], [5, 7], 'k-', lw=1, alpha=0.5)

    plt.tight_layout()
    plt.savefig(os.path.join(out_dir, '图3-3_管理后台用例图.png'), dpi=200, bbox_inches='tight')
    plt.close()
    print('图3-3 完成')

def fig3_4():
    """图3-4: 系统总体用例图"""
    fig, ax = plt.subplots(figsize=(13, 8))
    ax.set_xlim(0, 15); ax.set_ylim(0, 10); ax.axis('off')
    ax.set_title('图3-4 系统总体用例图', fontsize=14, fontweight='bold', pad=10)

    # Two actors
    for i, (x, label, role) in enumerate([(1.2, '普通用户\n(USER)', '#3366cc'), (1.2, '管理员\n(ADMIN)', '#cc3366')]):
        y = 7.5 - i*3
        ax.plot(x, y, 'o', markersize=10, color='#333')
        ax.plot([x, x], [y-0.3, y-1.2], 'k-', lw=2)
        ax.plot([x-0.5, x+0.5], [y-0.8, y-0.8], 'k-', lw=2)
        ax.plot([x, x-0.5], [y-1.2, y-2], 'k-', lw=2)
        ax.plot([x, x+0.5], [y-1.2, y-2], 'k-', lw=2)
        ax.text(x, y-2.5, label, ha='center', fontsize=10, fontweight='bold', color=role)

    # System boundary
    rect = FancyBboxPatch((4, 1), 10.5, 8.5, boxstyle="round,pad=0.3", facecolor='#f8f8ff', edgecolor='#333', linewidth=2.5)
    ax.add_patch(rect)
    ax.text(14.7, 9.0, 'AI Resume Copilot', fontsize=12, fontweight='bold', color='#333', ha='right')

    # Use case groups
    groups = [
        (6.5, 8.6, '用户认证', ['注册', '登录', 'JWT认证'], '#e8f4e8'),
        (10.5, 8.6, '简历管理', ['创建简历', '编辑简历', '版本管理', 'PDF导出'], '#e8f0ff'),
        (6.5, 5.8, 'AI核心功能', ['AI优化', '岗位匹配', '模拟面试'], '#fff8e8'),
        (10.5, 5.8, '管理后台', ['统计概览', '用户管理', '模板管理', '日志查看'], '#ffe8f0'),
    ]

    for gx, gy, gname, items, color in groups:
        # Group box
        box = FancyBboxPatch((gx-2.5, gy-1-0.5*len(items)), 5, 1+0.55*len(items),
                            boxstyle="round,pad=0.1", facecolor=color, edgecolor='#999', linewidth=1, alpha=0.6)
        ax.add_patch(box)
        ax.text(gx, gy+0.1, gname, ha='center', fontsize=10, fontweight='bold')
        for j, item in enumerate(items):
            ax.text(gx, gy-0.45-j*0.5, f'• {item}', ha='center', fontsize=9)

    # User connections
    ax.plot([2.5, 4], [7.5, 8.6], 'k-', lw=1, alpha=0.4)
    ax.plot([2.5, 4], [7.5, 5.8], 'k-', lw=1, alpha=0.4)
    # Admin connections
    ax.plot([2.5, 4], [4.5, 8.6], 'k-', lw=1, alpha=0.4)
    ax.plot([2.5, 4], [4.5, 5.8], 'k-', lw=1, alpha=0.4)
    ax.plot([2.5, 4], [4.5, 3.0], 'k-', lw=1, alpha=0.4)

    # Admin access to management
    ax.text(3.2, 3.0, '仅管理员', fontsize=8, color='#cc3366', ha='center')

    plt.tight_layout()
    plt.savefig(os.path.join(out_dir, '图3-4_系统总体用例图.png'), dpi=200, bbox_inches='tight')
    plt.close()
    print('图3-4 完成')

def fig4_1():
    """图4-1: 系统总体架构图"""
    fig, ax = plt.subplots(figsize=(14, 10))
    ax.set_xlim(0, 16); ax.set_ylim(0, 14); ax.axis('off')
    ax.set_title('图4-1 系统总体架构图', fontsize=14, fontweight='bold', pad=10)

    layers = [
        (12.5, '表示层 (Presentation Layer)', 'Vue 3 + Element Plus + Axios\n运行环境：用户浏览器 (Chrome/Edge/Firefox)\n单页面应用 (SPA)，负责界面渲染与用户交互', '#e3f2fd'),
        (10.0, '接口层 (API Layer)', 'Spring MVC RestController\nAuthController | ResumeController | AIController\nInterviewController | AdminController\n统一ApiResponse<T>响应格式 | @Valid参数校验', '#fff3e0'),
        (7.5, '业务逻辑层 (Business Logic Layer)', 'UserService | ResumeService | LLMService\nJobAnalysisService | InterviewService | AdminService\nPromptBuilder (提示词管理) | JwtUtil (令牌工具)\n@Transactional事务管理 | 业务规则与流程编排', '#e8f5e9'),
        (4.8, '数据访问层 (Data Access Layer)', 'Spring Data JPA (Hibernate ORM)\nUserRepository | ResumeRepository | TemplateRepository\nJobAnalysisRepository | OptimizationLogRepository\nInterviewSessionRepository | 分页查询支持', '#fce4ec'),
        (2.2, '数据存储层 (Data Storage)', 'MySQL 8.0 生产数据库 | H2 内存数据库(开发)\nHikariCP 连接池 | DDL Auto-Update\n数据持久化与事务管理', '#f3e5f5'),
    ]

    for y, title, content, color in layers:
        rect = FancyBboxPatch((1, y-0.9), 14, 2.0, boxstyle="round,pad=0.15", facecolor=color, edgecolor='#555', linewidth=2)
        ax.add_patch(rect)
        ax.text(8, y+0.7, title, ha='center', fontsize=11, fontweight='bold', color='#222')
        ax.text(8, y-0.4, content, ha='center', fontsize=9, color='#444', va='center')

    # External service box on the right
    ext_rect = FancyBboxPatch((13.0, 9.5), 2.5, 3.5, boxstyle="round,pad=0.15", facecolor='#fff9c4', edgecolor='#f57f17', linewidth=2, linestyle='--')
    ax.add_patch(ext_rect)
    ax.text(14.25, 12.4, '外部服务', ha='center', fontsize=10, fontweight='bold', color='#f57f17')
    ax.text(14.25, 11.4, 'DeepSeek-V4\n大语言模型\nAPI', ha='center', fontsize=9, color='#555')

    # Arrow from business layer to external
    ax.annotate('', xy=(13.0, 10.8), xytext=(13.5, 8.5),
               arrowprops=dict(arrowstyle='->', lw=2, color='#f57f17', connectionstyle='arc3,rad=-0.3'))
    ax.text(14.8, 9.8, 'HTTP\n调用', fontsize=8, color='#f57f17', ha='center')

    # Arrows between layers
    for i in range(len(layers)-1):
        y1, y2 = layers[i][0], layers[i+1][0]
        ax.annotate('', xy=(8, y2+1.1), xytext=(8, y1-0.9),
                   arrowprops=dict(arrowstyle='<->', lw=1, color='#888'))

    ax.text(8.5, 5.5, '依赖注入\n(DI)', fontsize=7, color='#888', ha='center', rotation=90)

    # Bottom: infrastructure
    infra_text = '基础设施：Spring Boot 3.2 | Maven 3.9 | JDK 21 | Windows 11 | Git 版本控制'
    ax.text(8, 0.3, infra_text, ha='center', fontsize=9, color='#666', style='italic')

    plt.tight_layout()
    plt.savefig(os.path.join(out_dir, '图4-1_系统架构图.png'), dpi=200, bbox_inches='tight')
    plt.close()
    print('图4-1 完成')

def fig4_2():
    """图4-2: 系统功能模块结构图"""
    fig, ax = plt.subplots(figsize=(14, 9))
    ax.set_xlim(0, 16); ax.set_ylim(0, 12); ax.axis('off')
    ax.set_title('图4-2 系统功能模块结构图', fontsize=14, fontweight='bold', pad=10)

    # Top node
    top = FancyBboxPatch((5.5, 10.5), 5, 1, boxstyle="round,pad=0.15", facecolor='#e3f2fd', edgecolor='#1565c0', linewidth=2)
    ax.add_patch(top)
    ax.text(8, 11, 'AI Resume Copilot 系统', ha='center', fontsize=12, fontweight='bold')

    # Two main branches
    for bx, by, bcolor, bname in [(1, 8.5, '#e8f5e9', '用户端功能模块'), (9, 8.5, '#fce4ec', '管理端功能模块')]:
        branch = FancyBboxPatch((bx, by), 6, 1, boxstyle="round,pad=0.1", facecolor=bcolor, edgecolor='#555', linewidth=1.5)
        ax.add_patch(branch)
        ax.text(bx+3, by+0.5, bname, ha='center', fontsize=11, fontweight='bold')

    ax.plot([8, 4], [10.5, 9.5], 'k-', lw=1.5)
    ax.plot([8, 12], [10.5, 9.5], 'k-', lw=1.5)

    # User modules
    user_mods = [
        (1.0, 7.0, '用户认证\n注册/登录/JWT'),
        (3.1, 7.0, '简历管理\nCRUD/版本/PDF'),
        (5.2, 7.0, '模板系统\n选择/预览'),
        (1.0, 5.2, 'AI智能优化\n分段/全文优化'),
        (3.1, 5.2, '岗位匹配\n多维分析'),
        (5.2, 5.2, '模拟面试\n五维面试/语音'),
    ]
    for x, y, text in user_mods:
        box = FancyBboxPatch((x, y), 2, 1.6, boxstyle="round,pad=0.05", facecolor='#fff', edgecolor='#2e7d32', linewidth=1.2)
        ax.add_patch(box)
        ax.text(x+1, y+0.8, text, ha='center', fontsize=8.5)

    # Admin modules
    admin_mods = [
        (9.0, 7.0, '统计概览\n六维指标'),
        (11.1, 7.0, '用户管理\n编辑/删/角色'),
        (13.2, 7.0, '简历管理\n查看/删除'),
        (9.0, 5.2, '模板管理\n增删改查'),
        (11.1, 5.2, '日志查看\n优化/匹配'),
        (13.2, 5.2, '面试记录\n查看记录'),
    ]
    for x, y, text in admin_mods:
        box = FancyBboxPatch((x, y), 2, 1.6, boxstyle="round,pad=0.05", facecolor='#fff', edgecolor='#c62828', linewidth=1.2)
        ax.add_patch(box)
        ax.text(x+1, y+0.8, text, ha='center', fontsize=8.5)

    # Connection lines from branches to leaves
    for mods in [user_mods, admin_mods]:
        for x, y, _ in mods:
            bx = x + 1
            by = 8.5 if x < 8 else 8.5
            ax.plot([bx, bx], [by, y+1.6], 'k-', lw=0.6, alpha=0.4)

    plt.tight_layout()
    plt.savefig(os.path.join(out_dir, '图4-2_功能模块结构图.png'), dpi=200, bbox_inches='tight')
    plt.close()
    print('图4-2 完成')

def fig4_3():
    """图4-3: 系统数据库E-R图"""
    fig, ax = plt.subplots(figsize=(14, 10))
    ax.set_xlim(0, 16); ax.set_ylim(0, 12); ax.axis('off')
    ax.set_title('图4-3 系统数据库E-R图', fontsize=14, fontweight='bold', pad=10)

    # Define entities with positions
    entities = [
        (8, 10.0, 'users\n用户表', 'PK  id\n  username\n  password\n  email\n  role\n  created_at', '#e3f2fd'),
        (3, 7.5, 'resumes\n简历表', 'PK  id\nFK  user_id\n  title\n  version\n  content_json\nFK  optimized_from\n  is_current\n  created_at\n  updated_at', '#e8f5e9'),
        (13, 7.5, 'templates\n模板表', 'PK  id\n  name\n  category\n  description\n  content_json\n  source_url\n  created_at', '#fff3e0'),
        (1, 4.0, 'optimization_logs\n优化日志表', 'PK  id\nFK  resume_id\n  prompt_used\n  llm_model\n  input_text\n  output_text\n  response_time_ms\n  section_type\n  created_at', '#fce4ec'),
        (6, 4.0, 'job_analyses\n岗位分析表', 'PK  id\nFK  user_id\nFK  resume_id\n  job_description\n  match_score\n  suggestions\n  created_at', '#f3e5f5'),
        (11, 4.0, 'interview_sessions\n面试会话表', 'PK  id\nFK  user_id\nFK  resume_id\n  position\n  messages\n  status\n  score\n  report\n  strengths\n  weaknesses\n  suggestions\n  created_at\n  completed_at', '#e0f7fa'),
    ]

    for x, y, title, fields, color in entities:
        # Calculate height based on field count
        n_fields = len([f for f in fields.split('\n') if f.strip()])
        h = 0.4 * n_fields + 0.6
        rect = FancyBboxPatch((x-2.2, y-h), 4.4, h, boxstyle="round,pad=0.1", facecolor=color, edgecolor='#333', linewidth=1.5)
        ax.add_patch(rect)
        ax.text(x, y+0.2, title, ha='center', fontsize=10, fontweight='bold')
        lines = fields.split('\n')
        for i, line in enumerate(lines):
            if line.strip():
                fs = 8
                is_pk = 'PK' in line
                ax.text(x, y-0.3-i*0.4, line.strip(), ha='center', fontsize=fs,
                       fontweight='bold' if is_pk else 'normal', color='#c62828' if is_pk else '#333')

    # Relationship lines with cardinality
    relationships = [
        (8, 9.6, 3, 8.5, '1', 'N', '创建/拥有'),
        (3, 7.1, 1, 5.0, '1', 'N', '产生优化日志'),
        (8, 9.6, 6, 5.0, '1', 'N', '执行岗位分析'),
        (8, 9.6, 11, 5.0, '1', 'N', '参加面试'),
        (3, 7.1, 6, 5.0, '1', 'N', '被分析'),
        (3, 7.1, 11, 5.0, '1', 'N', '用于面试'),
    ]

    for x1, y1, x2, y2, c1, c2, label in relationships:
        ax.plot([x1, x2], [y1, y2], 'k-', lw=1, alpha=0.5)
        mx, my = (x1+x2)/2, (y1+y2)/2
        ax.text(mx+0.3, my+0.2, label, fontsize=7, color='#666')
        ax.text(x1-0.3, y1-0.3, c1, fontsize=8, fontweight='bold', color='#1565c0')
        ax.text(x2+0.3, y2+0.2, c2, fontsize=8, fontweight='bold', color='#c62828')

    # Self-referencing relationship for resumes
    ax.annotate('自引用', xy=(5.5, 7.5), xytext=(5.5, 6.0), fontsize=8, color='#666',
               arrowprops=dict(arrowstyle='->', lw=1, color='#666', connectionstyle='arc3,rad=0.5'), ha='center')
    ax.text(5.5, 6.2, 'optimized_from\n(版本链)', ha='center', fontsize=7, color='#666')

    plt.tight_layout()
    plt.savefig(os.path.join(out_dir, '图4-3_数据库ER图.png'), dpi=200, bbox_inches='tight')
    plt.close()
    print('图4-3 完成')

def fig4_4():
    """图4-4: AI模拟面试流程图 (Activity Diagram)"""
    fig, ax = plt.subplots(figsize=(10, 14))
    ax.set_xlim(0, 12); ax.set_ylim(0, 18); ax.axis('off')
    ax.set_title('图4-4 AI模拟面试流程图', fontsize=14, fontweight='bold', pad=10)

    steps = [
        (6, 17.0, '用户选择简历', 'start'),
        (6, 15.5, '系统发送简历数据\n+面试System Prompt', 'process'),
        (6, 14.0, 'LLM生成开场白\n+第一个面试问题', 'llm'),
        (6, 12.5, '用户输入回答\n(文字/语音)', 'input'),
        (6, 11.0, '系统追加回答到\n对话历史messages', 'process'),
        (6, 9.5, '统计已提问数量\n检查是否≥5题?', 'decision'),
        (3, 8.0, '是 → 自动结束面试', 'process'),
        (9, 8.0, '否 → LLM根据历史\n选择未覆盖维度提问', 'llm'),
        (3, 6.5, 'LLM生成面试\n评估报告JSON', 'llm'),
        (6, 5.0, '系统解析报告JSON\n提取评分/优势/不足/建议', 'process'),
        (6, 3.5, '更新InterviewSession\nstatus=COMPLETED', 'process'),
        (6, 2.0, '前端渲染报告页面\n展示评分环+详情', 'end'),
    ]

    for x, y, text, stype in steps:
        if stype == 'start':
            circle = plt.Circle((x, y), 0.8, facecolor='#e8f5e9', edgecolor='#2e7d32', linewidth=2)
            ax.add_patch(circle)
            ax.text(x, y, text, ha='center', va='center', fontsize=9, fontweight='bold')
        elif stype == 'end':
            circle = plt.Circle((x, y), 0.8, facecolor='#fce4ec', edgecolor='#c62828', linewidth=2)
            ax.add_patch(circle)
            ax.text(x, y, text, ha='center', va='center', fontsize=9, fontweight='bold')
        elif stype == 'decision':
            diamond = Polygon([(x,y+0.7), (x+1.5,y), (x,y-0.7), (x-1.5,y)],
                            facecolor='#fff9c4', edgecolor='#f57f17', linewidth=1.5)
            ax.add_patch(diamond)
            ax.text(x, y, text, ha='center', va='center', fontsize=8)
        elif stype == 'llm':
            rect = FancyBboxPatch((x-2.2, y-0.55), 4.4, 1.1, boxstyle="round,pad=0.1",
                                 facecolor='#e0f7fa', edgecolor='#00838f', linewidth=1.5)
            ax.add_patch(rect)
            ax.text(x, y, text, ha='center', va='center', fontsize=9)
        elif stype == 'input':
            rect = FancyBboxPatch((x-2.2, y-0.45), 4.4, 0.9, boxstyle="round,pad=0.1",
                                 facecolor='#fff3e0', edgecolor='#e65100', linewidth=1.5)
            ax.add_patch(rect)
            ax.text(x, y, text, ha='center', va='center', fontsize=9)
        else:
            rect = FancyBboxPatch((x-2.2, y-0.55), 4.4, 1.1, boxstyle="round,pad=0.1",
                                 facecolor='#f5f5f5', edgecolor='#555', linewidth=1.5)
            ax.add_patch(rect)
            ax.text(x, y, text, ha='center', va='center', fontsize=9)

    # Arrows
    arrows = [(6,16.2,6,15.95), (6,14.95,6,14.55), (6,13.45,6,12.95), (6,11.95,6,11.55),
              (6,10.45,6,10.2), (6,8.55,3.55,8.0), (6,8.55,8.45,8.0),
              (3,7.45,3,7.05), (3,6.0,5.2,5.55), (9,7.45,9,5.55), (9,5.0,7.5,5.0),
              (6,4.45,6,3.95), (6,2.95,6,2.5)]
    for x1,y1,x2,y2 in arrows:
        ax.annotate('', xy=(x2, y2), xytext=(x1, y1), arrowprops=dict(arrowstyle='->', lw=1.2, color='#555'))

    # Loop back arrow from LLM to input
    ax.annotate('', xy=(6.5, 12.5), xytext=(10.5, 8.5),
               arrowprops=dict(arrowstyle='->', lw=1, color='#00838f', linestyle='dashed', connectionstyle='arc3,rad=0.4'))
    ax.text(11.2, 10.5, '循环\n(最多5轮)', fontsize=8, color='#00838f', ha='center')

    # Legend
    ax.text(2, 1.2, '图例：  ▭ 处理步骤    ◇ 判断分支    🔵 LLM调用    □ 用户输入    ○ 开始/结束',
           fontsize=8, color='#666')

    plt.tight_layout()
    plt.savefig(os.path.join(out_dir, '图4-4_面试流程图.png'), dpi=200, bbox_inches='tight')
    plt.close()
    print('图4-4 完成')

# ===== Generate all =====
for func in [fig1_1, fig3_1, fig3_2, fig3_3, fig3_4, fig4_1, fig4_2, fig4_3, fig4_4]:
    try:
        func()
    except Exception as e:
        print(f'Error in {func.__name__}: {e}')

print(f'\n全部图片已生成到: {out_dir}')
