<template>
  <div class="auth-page">
    <div class="auth-light-orbs" aria-hidden="true">
      <div class="orb orb--sage"></div>
      <div class="orb orb--amber"></div>
      <div class="orb orb--clay"></div>
      <div class="orb orb--mist"></div>
    </div>
    <div class="auth-bg-dot"></div>

    <div class="auth-card">
      <div class="brand-head">
        <img :src="APP_LOGO" alt="logo" class="logo" />
        <div>
          <div class="project-name">结日记</div>
          <div class="project-desc">创建账号，开始书写你的故事</div>
        </div>
      </div>

      <div class="auth-intro">
        给自己一个柔软又真实的角落，把生活的片段轻轻写下来。
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名，仅限英文字母和数字" maxlength="20" show-word-limit />
          <div class="input-hint">仅支持英文字母（a-z、A-Z）和数字（0-9），长度 3-20 位</div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称（可选）" />
        </el-form-item>
        <el-form-item label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="请输入图中的验证码" maxlength="6" class="captcha-input" />
            <img
              v-if="captchaImage"
              :src="captchaImage"
              alt="captcha"
              class="captcha-img"
              title="点击刷新"
              @click="loadCaptcha"
            />
            <el-button v-else :loading="captchaLoading" @click="loadCaptcha">获取验证码</el-button>
          </div>
        </el-form-item>
        <el-button type="primary" class="full-btn" :loading="loading" size="large" @click="handleRegister">注册</el-button>
      </el-form>

      <div class="bottom-link">
        已有账号？<router-link class="link" to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { authApi } from '@/api/modules';
import { APP_LOGO } from '@/utils/constants';

const router = useRouter();
const formRef = ref();
const loading = ref(false);
const captchaLoading = ref(false);
const captchaImage = ref('');

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  captchaId: '',
  captchaCode: ''
});

const validateConfirm = (rule, value, callback) => {
  if (!value) callback(new Error('请再次输入密码'));
  else if (value !== form.password) callback(new Error('两次输入的密码不一致'));
  else callback();
};

  const validateUsername = (rule, value, callback) => {
    if (!value) return callback(new Error('请输入用户名'));
    if (!/^[a-zA-Z0-9]+$/.test(value)) return callback(new Error('用户名只能包含英文字母和数字'));
    if (value.length < 3 || value.length > 20) return callback(new Error('用户名长度应为 3-20 位'));
    callback();
  };

const rules = {
    username: [
      { required: true, validator: validateUsername, trigger: 'blur' }
    ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度建议 6 到 20 位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
};

async function loadCaptcha() {
  captchaLoading.value = true;
  try {
    const res = await authApi.getCaptcha();
    form.captchaId = res.data?.captchaId || '';
    captchaImage.value = res.data?.image || '';
    form.captchaCode = '';
  } catch (e) {
    captchaImage.value = '';
  } finally {
    captchaLoading.value = false;
  }
}

async function handleRegister() {
  await formRef.value?.validate();
  if (!form.captchaId) {
    ElMessage.error('请先获取验证码');
    return;
  }
  loading.value = true;
  try {
    await authApi.register({
      username: form.username,
      password: form.password,
      confirmPassword: form.confirmPassword,
      nickname: form.nickname,
      captchaId: form.captchaId,
      captchaCode: form.captchaCode
    });
    ElMessage.success('注册成功，请登录');
    router.push('/login');
  } catch (e) {
    loadCaptcha();
  } finally {
    loading.value = false;
  }
}

onMounted(loadCaptcha);
</script>

<style scoped lang="scss">
.auth-page {
  --pad: clamp(12px, 4vw, 24px);
  position: relative;
  min-height: 100vh;
  min-height: 100dvh;
  display: grid;
  place-items: center;
  padding: var(--pad);
  overflow: hidden;
  background:
    radial-gradient(ellipse at 15% 20%, rgba(122, 154, 126, 0.07) 0%, transparent 50%),
    radial-gradient(ellipse at 85% 80%, rgba(196, 149, 74, 0.06) 0%, transparent 50%),
    var(--bg);
}

.auth-light-orbs {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  will-change: transform, opacity;
}

.orb--sage {
  width: 420px;
  height: 420px;
  background: radial-gradient(circle at 35% 35%, rgba(122, 154, 126, 0.48), rgba(122, 154, 126, 0) 65%);
  top: -12%;
  left: -10%;
  animation: orb-drift-sage 16s ease-in-out infinite, orb-breathe-sage 7s ease-in-out infinite;
}

.orb--amber {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle at 40% 60%, rgba(196, 149, 74, 0.42), rgba(196, 149, 74, 0) 65%);
  bottom: -15%;
  right: -8%;
  animation: orb-drift-amber 20s ease-in-out infinite, orb-breathe-amber 8s ease-in-out infinite 2s;
}

.orb--clay {
  width: 260px;
  height: 260px;
  background: radial-gradient(circle at 50% 40%, rgba(196, 122, 90, 0.35), rgba(196, 122, 90, 0) 65%);
  top: 40%;
  left: -10%;
  animation: orb-drift-clay 18s ease-in-out infinite 1s, orb-breathe-clay 6s ease-in-out infinite 4s;
}

.orb--mist {
  width: 480px;
  height: 480px;
  background: radial-gradient(circle at 55% 45%, rgba(122, 154, 126, 0.25), rgba(196, 149, 74, 0.15), transparent 65%);
  top: 25%;
  right: -15%;
  animation: orb-drift-mist 22s ease-in-out infinite 3s, orb-breathe-mist 9s ease-in-out infinite 1s;
}

.auth-bg-dot {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, var(--border) 1px, transparent 1px);
  background-size: 32px 32px;
  opacity: 0.3;
  pointer-events: none;
}

/* ---- 光晕漂浮动画 ---- */
@keyframes orb-drift-sage {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25%      { transform: translate(60px, 50px) scale(1.12); }
  50%      { transform: translate(-20px, 80px) scale(0.92); }
  75%      { transform: translate(-50px, -10px) scale(1.08); }
}

@keyframes orb-drift-amber {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33%      { transform: translate(-55px, -60px) scale(1.1); }
  66%      { transform: translate(25px, -45px) scale(0.9); }
}

@keyframes orb-drift-clay {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50%      { transform: translate(50px, -55px) scale(1.15); }
}

@keyframes orb-drift-mist {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25%      { transform: translate(-70px, -30px) scale(1.06); }
  50%      { transform: translate(-30px, -70px) scale(0.94); }
  75%      { transform: translate(40px, -20px) scale(1.1); }
}

@keyframes orb-breathe-sage {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0.5; }
}

@keyframes orb-breathe-amber {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0.45; }
}

@keyframes orb-breathe-clay {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0.4; }
}

@keyframes orb-breathe-mist {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0.55; }
}

.auth-card {
  position: relative;
  z-index: 1;
  width: min(460px, 100%);
  padding: 34px 30px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 24px;
  box-shadow: 0 2px 4px rgba(44,58,79,0.03), 0 12px 40px rgba(44,58,79,0.08);
  box-sizing: border-box;
}

.brand-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;

  .logo {
    width: 52px;
    height: 52px;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(44,58,79,0.1);
  }

  .project-name {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
    color: var(--ink);
    letter-spacing: 0.05em;
  }

  .project-desc {
    color: var(--text-muted);
    margin-top: 3px;
    font-size: 13px;
  }
}

.auth-intro {
  margin-bottom: 20px;
  padding: 14px 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(122, 154, 126, 0.08), rgba(196, 149, 74, 0.06));
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 14px;
}

.full-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.captcha-input {
  flex: 1;
}

.input-hint {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
}

.captcha-img {
  height: 40px;
  border-radius: 10px;
  border: 1px solid var(--border-light);
  cursor: pointer;
  user-select: none;
}

.bottom-link {
  margin-top: 20px;
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
}

.link {
  color: var(--sage-deep);
  font-weight: 700;
  text-decoration: none;
  transition: color var(--transition-fast);

  &:hover { color: var(--sage); }
}

@media (max-width: 640px) {
  .auth-card {
    padding: 28px 20px;
    border-radius: 20px;
  }

  .brand-head .project-name {
    font-size: 24px;
  }
}

@media (max-height: 820px) {
  .auth-page {
    place-items: start center;
    padding-top: 24px;
    padding-bottom: 24px;
  }
}
</style>
