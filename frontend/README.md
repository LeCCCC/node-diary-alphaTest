# node-diary-frontend

一个可直接与当前 Spring Boot 后端联调和部署的 Vue 3 前端项目，覆盖以下接口：

- 用户注册 `POST /api/user/register`
- 用户登录 `POST /api/user/login`
- 获取当前用户 `GET /api/user/me`
- 上传图片 `POST /api/upload/image`
- 新建日记 `POST /api/diary`
- 我的日记列表 `GET /api/diary/my-list`
- 主页日记流 `GET /api/diary/home-feed`
- 日记详情 `GET /api/diary/{id}`
- 编辑日记 `PUT /api/diary/{id}`
- 删除日记 `DELETE /api/diary/{id}`
- 加入匹配 `POST /api/match/join`
- 当前匹配 `GET /api/match/current`
- 取消匹配 `DELETE /api/match/current`
- 树洞列表 `GET /api/tree-holes`
- 发布树洞 `POST /api/tree-holes`
- 树洞详情 `GET /api/tree-holes/{treeHoleId}`
- 编辑树洞 `PUT /api/tree-holes/{treeHoleId}`
- 删除树洞 `DELETE /api/tree-holes/{treeHoleId}`
- 树洞评论列表 `GET /api/tree-holes/{treeHoleId}/comments`
- 发表评论 `POST /api/tree-holes/{treeHoleId}/comments`

## 技术栈

- Vue 3
- Vite
- Vue Router
- Pinia
- Axios
- Element Plus
- Vue Quill

## 本地启动

Windows：

```bash
npm install
copy .env.example .env
npm run dev
```

macOS / Linux：

```bash
npm install
cp .env.example .env
npm run dev
```

默认会把 `/api` 和 `/uploads` 代理到 `http://localhost:8080`。

## 开发环境变量

```bash
VITE_API_BASE_URL=/api
VITE_API_BASE_TARGET=http://localhost:8080
```

## 生产部署

### 1. 构建

```bash
npm install
npm run build
```

构建产物会输出到 `dist/`。

### 2. 生产环境变量建议

前端已默认使用相对路径：

```bash
VITE_API_BASE_URL=/api
```

这样上线后只需要让 Web 服务器把：

- `/` 指向前端静态资源
- `/api` 反向代理到 Spring Boot
- `/uploads` 反向代理到 Spring Boot 或静态文件服务

### 3. Nginx 示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /var/www/node-diary-frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 已实现页面

- 登录
- 注册
- 主页
- 匹配
- 我的资料
- 我的日记列表
- 新建日记
- 编辑日记
- 日记详情
- 树洞广场
- 发布树洞
- 编辑树洞
- 树洞详情与匿名评论

## 与后端联调约定

### 1. 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 2. JWT

登录成功后会将 `token` 保存到本地，并在后续请求里自动带上：

```http
Authorization: Bearer {token}
```

### 3. 图片回显

- 如果后端返回 `/uploads/xxx.jpg`，前端会自动拼接当前服务域名显示图片。
- 如果后端直接返回完整 URL，也可正常显示。
- 编辑器正文图片已限制最大展示尺寸，避免图片过大影响阅读。

## 目录结构

```text
src
├─ api
├─ components
├─ layouts
├─ router
├─ stores
├─ utils
└─ views
```
