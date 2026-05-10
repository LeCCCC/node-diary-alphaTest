<template>
  <div class="auth-page">
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
          <el-input v-model="form.username" placeholder="请输入用户名" maxlength="20" show-word-limit />
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

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度建议 3 到 20 位', trigger: 'blur' }
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
