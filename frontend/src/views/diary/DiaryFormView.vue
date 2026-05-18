<template>
  <div class="diary-form" v-loading="loading">
    <header class="form-head">
      <div class="head-left">
        <span class="eyebrow">{{ isEdit ? 'REVISING' : 'TODAY' }}</span>
        <h1 class="head-title">{{ isEdit ? '编辑日记' : '写一页日记' }}</h1>
        <p class="head-sub">静下来，把今天写下来。</p>
      </div>
      <div class="head-right">
        <el-button link @click="$router.push('/diaries')">
          <span class="back-arrow">←</span>&nbsp;返回列表
        </el-button>
      </div>
    </header>

    <el-form ref="formRef" :model="form" :rules="rules" class="diary-form-body">
      <!-- 标题 -->
      <el-form-item prop="title" class="title-item">
        <el-input
          v-model="form.title"
          placeholder="给今天起一个标题"
          maxlength="50"
          show-word-limit
          class="title-input"
        />
      </el-form-item>

      <!-- Meta 行：封面 + 可见性 -->
      <div class="meta-row">
        <div class="meta-item cover">
          <span class="meta-label">封面</span>
          <el-upload :show-file-list="false" :http-request="handleUploadCover" accept="image/*">
            <div class="cover-slot" :class="{ 'has-image': !!form.coverImage }">
              <img v-if="form.coverImage" :src="withBaseUrl(form.coverImage)" alt="cover" />
              <div v-else class="cover-plus">
                <span>+</span>
                <small>添加封面</small>
              </div>
            </div>
          </el-upload>
          <el-button
            v-if="form.coverImage"
            link
            class="cover-remove"
            @click="form.coverImage = ''"
          >移除</el-button>
        </div>

        <div class="meta-item visibility">
          <span class="meta-label">可见性</span>
          <div class="vis-group">
            <button
              type="button"
              class="vis-btn"
              :class="{ active: form.visibility === 0 }"
              @click="form.visibility = 0"
            >
              <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="4" y="10" width="16" height="10" rx="2" />
                <path d="M8 10V7a4 4 0 0 1 8 0v3" />
              </svg>
              仅自己
            </button>
            <button
              type="button"
              class="vis-btn"
              :class="{ active: form.visibility === 1 }"
              @click="form.visibility = 1"
            >
              <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 3v4" />
                <circle cx="12" cy="12" r="3" />
                <path d="M4 20c1.5-4 4.5-6 8-6s6.5 2 8 6" />
              </svg>
              匹配对象
            </button>
          </div>
        </div>
      </div>

      <!-- 正文编辑器 -->
      <el-form-item prop="content" class="editor-item">
        <DiaryEditor
          v-model="form.content"
          :upload-image="handleUploadInlineImage"
          :saved-at="lastSavedAt"
          class="diary-editor-host"
        />
      </el-form-item>

      <!-- 操作区 -->
      <div class="actions">
        <el-button class="btn-cancel" @click="$router.push('/diaries')">取消</el-button>
        <el-button
          type="primary"
          class="btn-publish"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ isEdit ? '保存修改' : '发布日记' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { diaryApi, uploadApi } from '@/api/modules';
import { withBaseUrl, ensureBrowserImage } from '@/utils/common';
import { sanitizeHtml } from '@/utils/sanitize';
import DiaryEditor from '@/components/DiaryEditor.vue';

const route = useRoute();
const router = useRouter();
const formRef = ref();
const loading = ref(false);
const submitting = ref(false);
const lastSavedAt = ref(null);

const isEdit = computed(() => Boolean(route.params.id));

const form = reactive({
  title: '',
  content: '',
  coverImage: '',
  visibility: 0
});

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'change' }]
};

async function handleUploadCover({ file }) {
  const safe = await ensureBrowserImage(file.raw || file);
  const res = await uploadApi.uploadImage(safe);
  form.coverImage = res.data?.url || '';
  ElMessage.success('封面上传成功');
}

async function handleUploadInlineImage(file) {
  const safe = await ensureBrowserImage(file.raw || file);
  const res = await uploadApi.uploadImage(safe);
  const url = res.data?.url;
  if (url) ElMessage.success('图片已插入');
  return url ? withBaseUrl(url) : '';
}

async function loadDetail() {
  if (!isEdit.value) return;
  loading.value = true;
  try {
    const res = await diaryApi.getDetail(route.params.id);
    Object.assign(form, {
      title: res.data?.title || '',
      content: res.data?.content || '',
      coverImage: res.data?.coverImage || '',
      visibility: Number(res.data?.visibility ?? 0)
    });
  } finally {
    loading.value = false;
  }
}

async function handleSubmit() {
  if (!formRef.value) {
    ElMessage.error('表单未就绪，请刷新重试');
    return;
  }
  try {
    await formRef.value.validate();
  } catch {
    ElMessage.warning('请检查标题和正文是否填写');
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      ...form,
      content: sanitizeHtml(form.content)
    };
    if (isEdit.value) {
      await diaryApi.update(route.params.id, payload);
      lastSavedAt.value = Date.now();
      ElMessage.success('更新成功');
      router.push(`/diaries/${route.params.id}`);
    } else {
      const res = await diaryApi.create(payload);
      lastSavedAt.value = Date.now();
      ElMessage.success('创建成功');
      router.push(`/diaries/${res.data?.id}`);
    }
  } catch {
    // 错误已在 request 拦截器中统一提示
  } finally {
    submitting.value = false;
  }
}

onMounted(loadDetail);
</script>

<style scoped lang="scss">
.diary-form {
  max-width: 880px;
  margin: 0 auto;
  padding: 8px 4px 40px;
}

/* ---- Head ---- */
.form-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 4px 22px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 22px;
}

.eyebrow {
  display: inline-block;
  font-family: var(--font-body);
  font-size: 11px;
  letter-spacing: 0.35em;
  color: var(--amber-deep);
  opacity: 0.82;
  margin-bottom: 4px;
}

.head-title {
  font-family: var(--font-display);
  font-size: 34px;
  line-height: 1.2;
  color: var(--ink);
  margin: 0;
  letter-spacing: 0.02em;
}

.head-sub {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 13px;
  letter-spacing: 0.04em;
}

.back-arrow {
  display: inline-block;
  transform: translateY(-1px);
}

/* ---- Form body ---- */
.diary-form-body {
  display: block;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__error) {
  padding-top: 4px;
  color: var(--clay);
}

/* ---- Title input ---- */
.title-item :deep(.el-form-item__content) { width: 100%; }

.title-input :deep(.el-input__wrapper) {
  background: transparent !important;
  border: 0 !important;
  box-shadow: none !important;
  padding: 0 !important;
  height: auto;
}

.title-input :deep(.el-input__wrapper:hover),
.title-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: none !important;
}

.title-input :deep(.el-input__inner) {
  font-family: var(--font-display);
  font-size: 28px;
  line-height: 1.5;
  height: 56px;
  color: var(--ink);
  letter-spacing: 0.03em;
  padding: 4px 2px 10px;
  border-bottom: 1px dashed var(--border);
  transition: border-color var(--transition-base);
}

.title-input :deep(.el-input__inner::placeholder) {
  color: var(--text-muted);
  font-style: italic;
  opacity: 0.7;
}

.title-input :deep(.el-input__inner:focus) {
  border-bottom-color: var(--sage);
}

.title-input :deep(.el-input__count) {
  background: transparent;
  color: var(--text-muted);
  font-size: 11px;
  right: 2px;
}

/* ---- Meta row ---- */
.meta-row {
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 14px 2px 22px;
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.meta-label {
  font-size: 12px;
  letter-spacing: 0.2em;
  color: var(--text-muted);
  text-transform: uppercase;
}

.cover-slot {
  position: relative;
  width: 76px;
  height: 56px;
  border-radius: 10px;
  border: 1px dashed var(--border);
  background: rgba(255, 253, 248, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
  transition: border-color var(--transition-fast), transform var(--transition-fast);
}

.cover-slot:hover {
  border-color: var(--sage);
  transform: translateY(-1px);
}

.cover-slot.has-image {
  border-style: solid;
  border-color: var(--border);
}

.cover-slot img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-plus {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: var(--text-muted);
}

.cover-plus span {
  font-size: 18px;
  line-height: 1;
  font-family: var(--font-display);
}

.cover-plus small {
  font-size: 10px;
  letter-spacing: 0.12em;
}

.cover-remove {
  color: var(--text-muted) !important;
  font-size: 12px;
}

.cover-remove:hover {
  color: var(--clay) !important;
}

.vis-group {
  display: inline-flex;
  padding: 3px;
  border-radius: var(--radius-pill);
  background: rgba(245, 239, 228, 0.6);
  border: 1px solid var(--border-light);
  gap: 2px;
}

.vis-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: 0;
  background: transparent;
  color: var(--text-secondary);
  border-radius: var(--radius-pill);
  font-size: 13px;
  font-family: var(--font-body);
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast);
}

.vis-btn:hover { color: var(--ink); }

.vis-btn.active {
  background: var(--bg-card);
  color: var(--ink);
  box-shadow: 0 1px 3px rgba(44, 58, 79, 0.08);
}

.vis-btn.active svg { color: var(--sage-deep); }

/* ---- Editor item ---- */
.editor-item :deep(.el-form-item__content) { width: 100%; }

.diary-editor-host {
  width: 100%;
}

/* ---- Actions ---- */
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding: 16px 2px 0;
  border-top: 1px solid var(--border-light);
}

.btn-cancel {
  padding: 10px 22px;
}

.btn-publish {
  padding: 10px 26px;
  background: var(--sage) !important;
  border-color: var(--sage) !important;
  font-family: var(--font-display);
  letter-spacing: 0.08em;
}

.btn-publish:hover {
  background: var(--sage-deep) !important;
  border-color: var(--sage-deep) !important;
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .diary-form { padding: 4px 2px 28px; }

  .form-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    padding-bottom: 16px;
    margin-bottom: 16px;
  }

  .head-title { font-size: 26px; }

  .title-input :deep(.el-input__inner) {
    font-size: 22px;
    line-height: 1.5;
    height: 44px;
    padding: 2px 0 8px;
  }

  .meta-row {
    gap: 16px;
    padding: 10px 0 16px;
  }

  .actions {
    justify-content: stretch;
  }

  .actions .el-button {
    flex: 1;
  }
}
</style>
