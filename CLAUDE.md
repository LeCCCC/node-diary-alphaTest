# CLAUDE.md — 结日记 (Node Diary)

## 项目概览

「结日记」是一站式日记应用，支持日记写作（富文本+图片）、匿名树洞、随机匹配、用户系统。前后端分离，单仓库。

## 目录结构

```
node-diary/
├── pom.xml                          # Maven 配置，Spring Boot 4.0.3
├── src/main/java/org/example/nodediary/
│   ├── controller/                  # REST 接口 (User/Diary/TreeHole/Match/Auth)
│   ├── service/ + service/Imp/      # 业务逻辑
│   ├── mapper/                      # MyBatis 接口 + XML
│   ├── pojo/                        # DTO/Entity/VO
│   ├── config/                      # WebConfig, Redis 等配置
│   ├── interceptor/                 # JWT 登录拦截器
│   ├── aspect/                      # AOP 切面
│   ├── exception/                   # 全局异常处理
│   ├── task/                        # 定时任务（匹配等）
│   └── utils/                       # 工具类
├── src/main/resources/
│   ├── application.yml              # 数据库/Redis/OSS/JWT 配置
│   └── mapper/                      # MyBatis XML
└── frontend/                        # 前端独立 Vite 项目
    ├── src/
    │   ├── views/                   # 页面 (auth/ diary/ treeHole/ match/ user/)
    │   ├── components/              # DiaryCard, TreeHoleCard
    │   ├── layouts/AppLayout.vue    # 主布局（侧栏+顶栏）
    │   ├── router/index.js          # 路由，含登录守卫
    │   ├── stores/user.js           # Pinia 用户状态
    │   ├── api/modules.js           # 所有后端 API 调用
    │   ├── utils/                   # auth, common, sanitize, constants, request
    │   └── assets/main.scss         # 全局主题（CSS 变量、Element Plus 覆盖）
    ├── vite.config.js               # 代理 /api → localhost:8080
    └── index.html                   # 入口，引入 Noto Serif SC + DM Serif Display 字体
```

## 后端技术栈

Spring Boot 4.0.3 / Java 17 / MyBatis / MySQL / Redis / JWT 0.12.5 / Argon2 密码加密 / Aliyun OSS 图片上传 / fastjson2 / hutool / AspectJ / Lombok

## 前端技术栈

Vue 3 (Composition API + `<script setup>`) / Vite 5 / Element Plus 2.8（全局中文 locale）/ Pinia / Vue Router 4 / Axios / Quill 富文本编辑器 / cropperjs 1.x / SCSS

## 常见修改入口

| 改什么 | 去哪改 |
|--------|--------|
| 页面 UI | `frontend/src/views/` 对应目录 |
| 卡片组件 | `frontend/src/components/DiaryCard.vue` / `TreeHoleCard.vue` |
| 全局样式/主题色 | `frontend/src/assets/main.scss`（CSS 变量在 `:root`） |
| 布局/导航 | `frontend/src/layouts/AppLayout.vue` |
| API 调用 | `frontend/src/api/modules.js` |
| 路由/守卫 | `frontend/src/router/index.js` |
| 后端接口 | `controller/` → `service/Imp/` → `mapper/` |
| 数据库配置 | `src/main/resources/application.yml` |

## 禁止读取目录

`target/` / `.git/` / `node_modules/` / `dist/` / `build/` / `logs/`

## 前后端联动

- Vite dev server 端口 5173，`/api` 和 `/uploads` 代理到 `localhost:8080`
- 登录后 JWT token 存 localStorage，axios 请求拦截器自动带 token
- Pinia `useUserStore` 管理登录态和用户信息
- 路由守卫：`requiresAuth` 页面未登录跳 `/login`，`guestOnly` 页面已登录跳 `/home`

## 重要注意事项

- **Redis** 用于验证码存储、匹配队列、首页日记流缓存
- **验证码**：注册页 `authApi.getCaptcha()` 获取，返回 `{ captchaId, image(base64) }`，注册时带 captchaId+captchaCode
- **密码**：后端 Argon2 加密，修改密码需传 oldPassword + newPassword + confirmPassword
- **Element Plus**：全局中文 locale 已设（`main.js`），分页自动显示「共 X 条」；全局 CSS 覆盖在 `main.scss` 中，不要另建主题文件
- **设计系统**：暖纸本风格，CSS 变量命名 `--ink`/`--sage`/`--amber`/`--bg`/`--bg-card`/`--border-light`，标题字体 `var(--font-display)` = Noto Serif SC

## Skill routing

When the user's request matches an available skill, invoke it via the Skill tool. When in doubt, invoke the skill.

Key routing rules:
- Product ideas/brainstorming → invoke /office-hours
- Strategy/scope → invoke /plan-ceo-review
- Architecture → invoke /plan-eng-review
- Design system/plan review → invoke /design-consultation or /plan-design-review
- Full review pipeline → invoke /autoplan
- Bugs/errors → invoke /investigate
- QA/testing site behavior → invoke /qa or /qa-only
- Code review/diff check → invoke /review
- Visual polish → invoke /design-review
- Ship/deploy/PR → invoke /ship or /land-and-deploy
- Save progress → invoke /context-save
- Resume context → invoke /context-restore
