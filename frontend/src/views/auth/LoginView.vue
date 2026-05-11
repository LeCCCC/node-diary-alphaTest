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
/* LoginView: auth-card is slightly narrower than RegisterView */
.auth-card {
  --auth-card-max-width: 440px;
}
</style>
