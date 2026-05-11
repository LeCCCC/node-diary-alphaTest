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
</style>
