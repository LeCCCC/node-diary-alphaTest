#!/usr/bin/env bash
set -euo pipefail

# ============================================
#  结日记 (Node Diary) 一键部署脚本
#  说明：本版本不依赖 rsync，改用 ssh + scp
# ============================================

# ---- 服务器配置（按你的实际情况修改） ----
SERVER_HOST="39.102.84.22"
SERVER_USER="root"
SSH_PORT="22"
BACKEND_DIR="/opt/nodediary"
FRONTEND_DIR="/var/www/nodediary"
SERVICE_NAME="nodediary"
JAR_NAME="node-diary-0.0.1-SNAPSHOT.jar"
KEEP_BACKUPS=3

# ---- 本地路径（一般不用改） ----
LOCAL_JAR="target/${JAR_NAME}"
LOCAL_DIST="frontend/dist/"

# ---- 颜色 ----
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# ---- 远程命令 ----
SSH="ssh -i ~/.ssh/nodediary_ed25519 -p ${SSH_PORT} -o ConnectTimeout=10 ${SERVER_USER}@${SERVER_HOST}"
SCP="scp -i ~/.ssh/nodediary_ed25519 -P ${SSH_PORT}"

echo_step()  { echo -e "\n${GREEN}[==]${NC} $1"; }
echo_ok()    { echo -e "${GREEN}[OK]${NC} $1"; }
echo_warn()  { echo -e "${YELLOW}[!!]${NC} $1"; }
echo_fail()  { echo -e "${RED}[XX]${NC} $1"; exit 1; }

# ---- 连接检查 ----
echo_step "检查服务器连接…"
if ! ${SSH} "echo ok" > /dev/null 2>&1; then
  echo_fail "无法连接到 ${SERVER_USER}@${SERVER_HOST}:${SSH_PORT}，请检查网络或 SSH 配置"
fi
echo_ok "服务器连接正常"

# ---- 服务器目录检查 / 创建 ----
echo_step "检查服务器部署目录…"
${SSH} << CHECK_DIR_EOF
  mkdir -p "${BACKEND_DIR}"
  mkdir -p "${FRONTEND_DIR}"
CHECK_DIR_EOF
echo_ok "服务器目录正常"

# ============================================
# Step 1: 构建后端
# ============================================
echo_step "Step 1/6: 构建后端 jar…"
mvn clean package -DskipTests -q

if [ ! -f "${LOCAL_JAR}" ]; then
  echo_fail "jar 构建失败，${LOCAL_JAR} 不存在。请确认 pom.xml 中的 artifactId/version 是否生成了这个文件名。"
fi

echo_ok "后端构建完成: ${LOCAL_JAR}"

# ============================================
# Step 2: 构建前端
# ============================================
echo_step "Step 2/6: 构建前端 dist…"
cd frontend
npm install --silent
npm run build
cd ..

if [ ! -d "frontend/dist" ]; then
  echo_fail "前端构建失败，frontend/dist 不存在"
fi

echo_ok "前端构建完成: frontend/dist"

# ============================================
# Step 3: 上传后端 jar
# ============================================
echo_step "Step 3/6: 上传后端 jar 到 ${SERVER_HOST}:${BACKEND_DIR}…"

# 服务器上备份旧 jar（保留最近 KEEP_BACKUPS 个）
${SSH} << BACKUP_EOF
  if [ -f "${BACKEND_DIR}/${JAR_NAME}" ]; then
    BACKUP_NAME="${JAR_NAME}.bak.\$(date +%Y%m%d-%H%M%S)"
    cp "${BACKEND_DIR}/${JAR_NAME}" "${BACKEND_DIR}/\${BACKUP_NAME}"
    echo "[备份] \${BACKUP_NAME}"

    cd "${BACKEND_DIR}"
    ls -t ${JAR_NAME}.bak.* 2>/dev/null | tail -n +$((KEEP_BACKUPS + 1)) | xargs -r rm -f
  fi
BACKUP_EOF

# 先上传为临时文件，上传成功后再替换正式 jar
${SCP} "${LOCAL_JAR}" "${SERVER_USER}@${SERVER_HOST}:${BACKEND_DIR}/${JAR_NAME}.tmp"
${SSH} "mv '${BACKEND_DIR}/${JAR_NAME}.tmp' '${BACKEND_DIR}/${JAR_NAME}'"

echo_ok "jar 上传完成"

# ============================================
# Step 4: 上传前端 dist
# ============================================
echo_step "Step 4/6: 上传前端 dist 到 ${SERVER_HOST}:${FRONTEND_DIR}…"

# 清空服务器旧前端文件，包括隐藏文件
${SSH} << CLEAN_FRONTEND_EOF
  mkdir -p "${FRONTEND_DIR}"
  find "${FRONTEND_DIR}" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
CLEAN_FRONTEND_EOF

# 上传 dist 目录内的内容，而不是上传 dist 文件夹本身
${SCP} -r "${LOCAL_DIST}." "${SERVER_USER}@${SERVER_HOST}:${FRONTEND_DIR}/"

echo_ok "dist 上传完成"

# ============================================
# Step 5: 重启后端服务
# ============================================
echo_step "Step 5/6: 重启 systemd 服务 ${SERVICE_NAME}…"

${SSH} "sudo systemctl restart ${SERVICE_NAME}"

# 等几秒确认服务起来
sleep 3

if ${SSH} "systemctl is-active --quiet ${SERVICE_NAME}"; then
  echo_ok "服务 ${SERVICE_NAME} 已成功重启"
else
  echo_fail "服务 ${SERVICE_NAME} 启动失败，请登录服务器执行：journalctl -u ${SERVICE_NAME} -n 50"
fi

# ============================================
# Step 6: Reload nginx
# ============================================
echo_step "Step 6/6: reload nginx…"

${SSH} "sudo nginx -t && sudo nginx -s reload"

echo_ok "nginx reload 完成"

# ============================================
# 完成
# ============================================
echo -e "\n${GREEN}============================================${NC}"
echo -e "${GREEN}  部署完成！${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo "  后端:  http://${SERVER_HOST}:8080"
echo "  前端:  http://${SERVER_HOST}"
echo "  状态:  ${SSH} systemctl status ${SERVICE_NAME}"
echo ""
