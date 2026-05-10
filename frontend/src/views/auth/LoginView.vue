<template>
  <div class="auth-page">
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

.auth-bg-dot {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, var(--border) 1px, transparent 1px);
  background-size: 32px 32px;
  opacity: 0.3;
  pointer-events: none;
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
