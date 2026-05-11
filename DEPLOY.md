# 结日记 (Node Diary) 部署文档

## 服务器目录结构

```
/opt/nodediary/
├── node-diary-0.0.1-SNAPSHOT.jar          # 后端 Spring Boot jar
├── node-diary-0.0.1-SNAPSHOT.jar.bak.*    # 历史备份（保留最近 3 个）
├── uploads/                                # 用户上传的图片（需手动创建）
└── logs/                                   # 应用日志（可选）

/var/www/nodediary/
├── index.html                              # 前端入口
├── assets/                                 # 打包后的 JS/CSS
└── favicon.ico
```

## systemd 服务配置

创建文件 `/etc/systemd/system/nodediary.service`：

```ini
[Unit]
Description=结日记 (Node Diary) 后端服务
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/nodediary
ExecStart=/usr/bin/java -jar /opt/nodediary/node-diary-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
Environment="JAVA_OPTS=-Xms128m -Xmx256m"

[Install]
WantedBy=multi-user.target
```

启用服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable nodediary
sudo systemctl start nodediary
```

常用管理命令：

```bash
sudo systemctl status nodediary       # 查看状态
sudo systemctl restart nodediary      # 重启
sudo systemctl stop nodediary         # 停止
journalctl -u nodediary -n 50 -f      # 查看最近 50 条日志并持续追踪
```

## nginx 配置示例

创建文件 `/etc/nginx/conf.d/nodediary.conf`：

```nginx
upstream nodediary_backend {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name _;

    # 前端静态文件
    root /var/www/nodediary;
    index index.html;

    # 上传的图片
    location /uploads/ {
        alias /opt/nodediary/uploads/;
        expires 7d;
        add_header Cache-Control "public, immutable";
    }

    # API 反向代理到 Spring Boot
    location /api/ {
        proxy_pass http://nodediary_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 上传文件大小限制
        client_max_body_size 20m;
    }

    # Vue Router history 模式：所有非静态请求回退到 index.html
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

启用并重载：

```bash
sudo nginx -t                        # 测试配置
sudo systemctl reload nginx          # 重载
```

## 首次部署步骤

### 1. 服务器环境准备

```bash
# 安装 JDK 17
sudo apt update && sudo apt install openjdk-17-jdk -y   # Ubuntu/Debian
# 或 sudo yum install java-17-openjdk -y                 # CentOS/RHEL

# 安装 nginx
sudo apt install nginx -y

# 确认安装
java -version
nginx -v
```

### 2. 创建目录

```bash
sudo mkdir -p /opt/nodediary/uploads
sudo mkdir -p /opt/nodediary/logs
sudo mkdir -p /var/www/nodediary
```

### 3. 上传 application.yml（生产环境配置）

将项目的 `src/main/resources/application.yml` 复制一份，修改生产环境配置（数据库、Redis 等），放到 `/opt/nodediary/` 目录。然后在 systemd 配置中添加：

```ini
Environment="SPRING_CONFIG_LOCATION=file:/opt/nodediary/application.yml"
```

或直接在 jar 同目录放置 `application.yml`，Spring Boot 会自动读取。

### 4. 配置 systemd 服务

按上文「systemd 服务配置」章节操作。

### 5. 配置 nginx

按上文「nginx 配置示例」章节操作。

### 6. 配置 SSH 免密登录（可选但推荐）

在本机执行：

```bash
ssh-copy-id -p 22 root@39.102.84.22
```

之后 `deploy.sh` 不再需要输入密码。

### 7. 首次上传

```bash
# 先手动上传一次 jar 和 dist
scp target/node-diary-0.0.1-SNAPSHOT.jar root@39.102.84.22:/opt/nodediary/
rsync -avz frontend/dist/ root@39.102.84.22:/var/www/nodediary/

# 启动服务
ssh root@39.102.84.22 "sudo systemctl start nodediary && sudo systemctl reload nginx"
```

## 日常一键部署

在项目根目录执行：

```bash
chmod +x deploy.sh
./deploy.sh
```

脚本会自动完成：

1. `mvn clean package -DskipTests` — 构建后端 jar
2. `cd frontend && npm install && npm run build` — 构建前端 dist
3. rsync jar → `/opt/nodediary/`
4. rsync dist → `/var/www/nodediary/`
5. `systemctl restart nodediary` — 重启后端
6. `nginx -s reload` — 重载前端

每次部署前自动备份旧 jar（保留最近 3 个 `.bak.*` 文件）。
