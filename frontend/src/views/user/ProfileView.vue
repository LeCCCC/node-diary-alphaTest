<template>
  <div class="profile-page">
    <el-card>
      <template #header>
        <div class="section-head">
          <span class="section-title">我的资料</span>
          <el-button type="primary" @click="toggleEditing">{{ editing ? '取消编辑' : '编辑资料' }}</el-button>
        </div>
      </template>

      <div class="profile-layout">
        <div class="avatar-column">
          <img :src="avatarPreview" class="avatar" alt="avatar" />
          <el-upload :show-file-list="false" :http-request="handlePickImage" accept="image/*">
            <el-button>上传头像</el-button>
          </el-upload>
        </div>

        <div class="info-column">
          <el-descriptions v-if="!editing" :column="1" border class="profile-table">
            <el-descriptions-item label="用户 ID">{{ profile?.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ profile?.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ profile?.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(profile?.createdAt) }}</el-descriptions-item>
          </el-descriptions>

          <template v-else>
            <el-form ref="formRef" :model="form" label-position="top" class="edit-form">
              <el-form-item label="用户名">
                <el-input v-model="form.username" disabled />
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saving" @click="handleSave">保存资料</el-button>
              </el-form-item>
            </el-form>

            <div class="password-section">
              <el-button v-if="!showPasswordChange" @click="showPasswordChange = true">修改密码</el-button>
              <template v-else>
                <el-divider>修改密码</el-divider>
                <el-form
                  ref="passwordFormRef"
                  :model="passwordForm"
                  :rules="passwordRules"
                  label-position="top"
                  class="edit-form"
                >
                  <el-form-item label="原密码" prop="oldPassword">
                    <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
                  </el-form-item>
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（6-20 位）" />
                  </el-form-item>
                  <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" :loading="changing" @click="handleChangePassword">保存新密码</el-button>
                    <el-button @click="showPasswordChange = false">取消</el-button>
                  </el-form-item>
                </el-form>
              </template>
            </div>
          </template>
        </div>
      </div>
    </el-card>

    <!-- Crop Dialog -->
    <el-dialog v-model="cropVisible" title="裁剪头像" width="680px" :close-on-click-modal="false" @closed="onCropClosed">
      <div class="crop-wrapper">
        <div class="crop-container">
          <img ref="cropImage" :src="cropSrc" alt="crop" class="crop-image" />
        </div>
      </div>
      <template #footer>
        <el-button @click="cropVisible = false" :disabled="cropSaving">取消</el-button>
        <el-button type="primary" :loading="cropSaving" @click="handleCropConfirm">确认裁剪</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { authApi, uploadApi } from '@/api/modules';
import { useUserStore } from '@/stores/user';
import { formatTime, getAvatar } from '@/utils/common';
import Cropper from 'cropperjs';

const userStore = useUserStore();
const profile = computed(() => userStore.userInfo);
const editing = ref(false);
const saving = ref(false);
const formRef = ref();
const form = reactive({ username: '', nickname: '', avatarUrl: '' });

const changing = ref(false);
const showPasswordChange = ref(false);
const passwordFormRef = ref();
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' });

// Crop state
const cropVisible = ref(false);
const cropSaving = ref(false);
const cropSrc = ref('');
const cropImage = ref(null);
let cropperInstance = null;

const validateNewPassword = (rule, value, callback) => {
  if (!value) callback(new Error('请输入新密码'));
  else if (value === passwordForm.oldPassword) callback(new Error('新密码不能与原密码相同'));
  else callback();
};

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) callback(new Error('请再次输入新密码'));
  else if (value !== passwordForm.newPassword) callback(new Error('两次输入的新密码不一致'));
  else callback();
};

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: 'blur' },
    { min: 6, max: 20, message: '长度建议 6 到 20 位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
};

const avatarPreview = computed(() => getAvatar(form.avatarUrl || profile.value?.avatarUrl));

function fillForm() {
  form.username = profile.value?.username || '';
  form.nickname = profile.value?.nickname || '';
  form.avatarUrl = profile.value?.avatarUrl || '';
}

function resetPasswordForm() {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  showPasswordChange.value = false;
  passwordFormRef.value?.clearValidate();
}

function toggleEditing() {
  editing.value = !editing.value;
  if (!editing.value) resetPasswordForm();
}

// Crop flow: pick → crop → upload → auto-save
async function handlePickImage({ file }) {
  const rawFile = file.raw || file;
  const src = URL.createObjectURL(rawFile);
  cropSrc.value = src;
  cropVisible.value = true;
  await nextTick();
  if (cropperInstance) cropperInstance.destroy();
  if (cropImage.value) {
    cropperInstance = new Cropper(cropImage.value, {
      aspectRatio: 1,
      viewMode: 1,
      dragMode: 'move',
      autoCropArea: 1,
      restore: false,
      guides: true,
      center: true,
      highlight: false,
      cropBoxMovable: true,
      cropBoxResizable: true,
    });
  }
}

async function handleCropConfirm() {
  if (!cropperInstance) return;
  const canvas = cropperInstance.getCroppedCanvas({ width: 320, height: 320 });
  if (!canvas) return;
  cropSaving.value = true;
  try {
    const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.9));
    const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' });
    const res = await uploadApi.uploadImage(file);
    form.avatarUrl = res.data?.url || '';
    await authApi.updateMe({ nickname: form.nickname || profile.value?.nickname || '', avatarUrl: form.avatarUrl });
    await userStore.fetchProfile();
    fillForm();
    ElMessage.success('头像已更新');
    cropVisible.value = false;
  } catch (error) {
    ElMessage.error('头像保存失败，请重试');
  } finally {
    cropSaving.value = false;
    if (cropperInstance) {
      cropperInstance.destroy();
      cropperInstance = null;
    }
    URL.revokeObjectURL(cropSrc.value);
  }
}

function onCropClosed() {
  if (cropperInstance) {
    cropperInstance.destroy();
    cropperInstance = null;
  }
  if (cropSrc.value) {
    URL.revokeObjectURL(cropSrc.value);
    cropSrc.value = '';
  }
}

async function handleSave() {
  saving.value = true;
  try {
    await authApi.updateMe({ nickname: form.nickname, avatarUrl: form.avatarUrl });
    await userStore.fetchProfile();
    fillForm();
    editing.value = false;
    resetPasswordForm();
    ElMessage.success('资料已更新');
  } catch (error) {
    ElMessage.error('资料更新失败，请确认后端资料更新接口路径');
  } finally {
    saving.value = false;
  }
}

async function handleChangePassword() {
  await passwordFormRef.value?.validate();
  changing.value = true;
  try {
    await authApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    });
    ElMessage.success('密码修改成功');
    resetPasswordForm();
  } finally {
    changing.value = false;
  }
}

onMounted(async () => {
  await userStore.fetchProfile();
  fillForm();
});
</script>

<style scoped lang="scss">
.profile-page .el-card {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  background: var(--bg-card);
}

.profile-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 28px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.section-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--ink);
}

.avatar-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.avatar {
  width: 160px;
  height: 160px;
  object-fit: cover;
  border-radius: 50%;
  border: 2px solid var(--border-light);
  box-shadow: var(--shadow-card);
}

/* Profile table styling */
.profile-table {
  :deep(.el-descriptions__table) {
    border-radius: var(--radius-lg);
    overflow: hidden;
    border: 1px solid var(--border-light);
  }

  :deep(.el-descriptions__label) {
    width: 96px;
    background: linear-gradient(135deg, #fdfcf8, #f9f6f0);
    color: var(--text-secondary);
    font-weight: 600;
    font-size: 13px;
    padding: 16px 20px;
    vertical-align: middle;
  }

  :deep(.el-descriptions__content) {
    padding: 16px 20px;
    color: var(--text);
    font-size: 14px;
    vertical-align: middle;
    background: var(--bg-card);
  }

  :deep(.el-descriptions__body .el-descriptions__row) {
    border-bottom: 1px solid var(--border-light);

    &:last-child {
      border-bottom: none;
    }
  }

  :deep(.el-descriptions__body .el-descriptions__row:first-child) {
    .el-descriptions__label {
      border-top-left-radius: var(--radius-lg);
    }
    .el-descriptions__content {
      border-top-right-radius: var(--radius-lg);
    }
  }

  :deep(.el-descriptions__body .el-descriptions__row:last-child) {
    .el-descriptions__label {
      border-bottom-left-radius: var(--radius-lg);
    }
    .el-descriptions__content {
      border-bottom-right-radius: var(--radius-lg);
    }
  }
}

.edit-form {
  max-width: 560px;
}

.password-section {
  margin-top: 6px;
}

.crop-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 380px;
  background: #f5f0e8;
  border-radius: var(--radius-md);
}

.crop-container {
  width: 100%;
  max-height: 500px;
}

.crop-container :deep(img) {
  max-width: 100%;
  max-height: 500px;
  display: block;
}

@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .avatar-column {
    align-items: flex-start;
  }
}

</style>

<style>
/* Cropper required styles */
.crop-container img {
  max-width: 100%;
}

.cropper-container {
  direction: ltr;
  font-size: 0;
  line-height: 0;
  position: relative;
  touch-action: none;
  user-select: none;
}

.cropper-container img {
  display: block;
  image-orientation: 0deg;
  max-width: none !important;
  max-height: none !important;
  min-width: 0 !important;
  min-height: 0 !important;
  width: 100%;
  height: 100%;
}

.cropper-wrap-box,
.cropper-canvas,
.cropper-drag-box,
.cropper-crop-box,
.cropper-modal {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
}

.cropper-wrap-box,
.cropper-canvas {
  overflow: hidden;
}

.cropper-drag-box {
  background-color: #fff;
  opacity: 0;
}

.cropper-modal {
  background-color: #000;
  opacity: 0.5;
}

.cropper-view-box {
  display: block;
  height: 100%;
  outline: 2px solid #5a7a5e;
  overflow: hidden;
  width: 100%;
}

.cropper-dashed {
  border: 0 dashed #eee;
  display: block;
  opacity: 0.5;
  position: absolute;
}

.cropper-dashed.dashed-h {
  border-bottom-width: 1px;
  border-top-width: 1px;
  height: calc(100% / 3);
  left: 0;
  top: calc(100% / 3);
  width: 100%;
}

.cropper-dashed.dashed-v {
  border-left-width: 1px;
  border-right-width: 1px;
  height: 100%;
  left: calc(100% / 3);
  top: 0;
  width: calc(100% / 3);
}

.cropper-center {
  display: block;
  height: 0;
  left: 50%;
  opacity: 0.75;
  position: absolute;
  top: 50%;
  width: 0;
}

.cropper-center::before,
.cropper-center::after {
  background-color: #eee;
  content: ' ';
  display: block;
  position: absolute;
}

.cropper-center::before {
  height: 1px;
  left: -3px;
  top: 0;
  width: 7px;
}

.cropper-center::after {
  height: 7px;
  left: 0;
  top: -3px;
  width: 1px;
}

.cropper-face,
.cropper-line,
.cropper-point {
  display: block;
  height: 100%;
  opacity: 0.1;
  position: absolute;
  width: 100%;
}

.cropper-face {
  background-color: #fff;
  left: 0;
  top: 0;
}

.cropper-line {
  background-color: #5a7a5e;
}

.cropper-line.line-e {
  cursor: ew-resize;
  right: -3px;
  top: 0;
  width: 5px;
}

.cropper-line.line-n {
  cursor: ns-resize;
  height: 5px;
  left: 0;
  top: -3px;
}

.cropper-line.line-w {
  cursor: ew-resize;
  left: -3px;
  top: 0;
  width: 5px;
}

.cropper-line.line-s {
  bottom: -3px;
  cursor: ns-resize;
  height: 5px;
  left: 0;
}

.cropper-point {
  background-color: #5a7a5e;
  height: 5px;
  opacity: 0.75;
  width: 5px;
}

.cropper-point.point-e {
  cursor: ew-resize;
  margin-top: -3px;
  right: -3px;
  top: 50%;
}

.cropper-point.point-n {
  cursor: ns-resize;
  left: 50%;
  margin-left: -3px;
  top: -3px;
}

.cropper-point.point-w {
  cursor: ew-resize;
  left: -3px;
  margin-top: -3px;
  top: 50%;
}

.cropper-point.point-s {
  bottom: -3px;
  cursor: s-resize;
  left: 50%;
  margin-left: -3px;
}

.cropper-point.point-ne {
  cursor: nesw-resize;
  right: -3px;
  top: -3px;
}

.cropper-point.point-nw {
  cursor: nwse-resize;
  left: -3px;
  top: -3px;
}

.cropper-point.point-sw {
  bottom: -3px;
  cursor: nesw-resize;
  left: -3px;
}

.cropper-point.point-se {
  bottom: -3px;
  cursor: nwse-resize;
  height: 20px;
  opacity: 1;
  right: -3px;
  width: 20px;
}

.cropper-point.point-se::before {
  background-color: #5a7a5e;
  bottom: -50%;
  content: ' ';
  display: block;
  height: 200%;
  opacity: 0;
  position: absolute;
  right: -50%;
  width: 200%;
}

.cropper-invisible {
  opacity: 0;
}

.cropper-bg {
  background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAAA3NCSVQICAjb4U/gAAAABlBMVEXMzMz////TjRV2AAAACXBIWXMAAArrAAAK6wGCiw1aAAAAHHRFWHRTb2Z0d2FyZQBBZG9iZSBGaXJld29ya3MgQ1M26LyyjAAAABFJREFUCJlj+M/AgBVhF/0PAH6/D/HkDxOGAAAAAElFTkSuQmCC');
}

.cropper-hide {
  display: block;
  height: 0;
  position: absolute;
  width: 0;
}

.cropper-hidden {
  display: none !important;
}

.cropper-move {
  cursor: move;
}

.cropper-crop {
  cursor: crosshair;
}

.cropper-disabled .cropper-drag-box,
.cropper-disabled .cropper-face,
.cropper-disabled .cropper-line,
.cropper-disabled .cropper-point {
  cursor: not-allowed;
}
</style>
