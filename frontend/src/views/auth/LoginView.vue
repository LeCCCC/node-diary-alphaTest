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
          <div class="project-desc">欢迎回来，继续记录今天的心情</div>
        </div>
      </div>

      <div class="auth-intro">
        在这里，日常被认真珍藏，情绪被温柔安放。
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" />
        </el-form-item>
        <el-button type="primary" class="full-btn" :loading="loading" size="large" @click="handleLogin">登录</el-button>
      </el-form>

      <div class="bottom-link">
        还没有账号？<router-link class="link" to="/register">去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { APP_LOGO } from '@/utils/constants';

const router = useRouter();
const userStore = useUserStore();
const formRef = ref();
const loading = ref(false);

const form = reactive({ username: '', password: '' });

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

async function handleLogin() {
  await formRef.value?.validate();
  loading.value = true;
  try {
    await userStore.login(form);
    ElMessage.success('登录成功');
    router.replace('/home');
  } finally {
    loading.value = false;
  }
}
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
  width: min(440px, 100%);
  padding: 36px 32px;
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

@media (max-height: 760px) {
  .auth-page {
    place-items: start center;
    padding-top: 24px;
    padding-bottom: 24px;
  }
}
</style>
