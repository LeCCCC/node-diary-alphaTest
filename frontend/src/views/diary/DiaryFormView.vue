<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="section-head">
        <span>{{ isEdit ? '编辑日记' : '写日记' }}</span>
        <el-button @click="$router.push('/diaries')">返回列表</el-button>
      </div>
    </template>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入标题" maxlength="50" show-word-limit />
      </el-form-item>

      <el-form-item label="封面图">
        <div class="upload-line">
          <el-upload :show-file-list="false" :http-request="handleUploadCover" accept="image/*">
            <el-button>上传封面</el-button>
          </el-upload>
          <img v-if="form.coverImage" :src="withBaseUrl(form.coverImage)" class="cover-preview" alt="cover" />
        </div>
      </el-form-item>

      <el-form-item label="可见性" prop="visibility">
        <el-radio-group v-model="form.visibility">
          <el-radio :value="0">仅自己可见</el-radio>
          <el-radio :value="1">匹配对象可见</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="正文" prop="content">
        <div class="editor-tip">点击下方大编辑区即可开始输入，支持插入图片。</div>
        <div class="editor-actions">
          <el-upload :show-file-list="false" :http-request="handleInsertImage" accept="image/*">
            <el-button>上传正文图片</el-button>
          </el-upload>
        </div>
        <div class="editor-shell">
          <QuillEditor
            ref="editorRef"
            v-model:content="form.content"
            content-type="html"
            theme="snow"
            toolbar="full"
            placeholder="从这里开始写今天的故事……"
            class="editor"
          />
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ isEdit ? '保存修改' : '发布日记' }}</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { QuillEditor } from '@vueup/vue-quill';
import { useRoute, useRouter } from 'vue-router';
import { diaryApi, uploadApi } from '@/api/modules';
import { withBaseUrl } from '@/utils/common';
import { sanitizeHtml } from '@/utils/sanitize';

const route = useRoute();
const router = useRouter();
const formRef = ref();
const editorRef = ref();
const loading = ref(false);
const submitting = ref(false);

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
  const res = await uploadApi.uploadImage(file.raw || file);
  form.coverImage = res.data?.url || '';
  ElMessage.success('封面上传成功');
}

async function handleInsertImage({ file }) {
  const res = await uploadApi.uploadImage(file.raw || file);
  const url = res.data?.url;
  const quill = editorRef.value?.getQuill();
  if (quill && url) {
    const index = quill.getSelection()?.index ?? quill.getLength();
    quill.insertEmbed(index, 'image', withBaseUrl(url));
    quill.setSelection(index + 1);

    requestAnimationFrame(() => {
      const images = quill.root.querySelectorAll('img');
      const currentImage = images[images.length - 1];
      if (currentImage) {
        currentImage.style.maxWidth = '560px';
        currentImage.style.maxHeight = '420px';
        currentImage.style.width = 'auto';
        currentImage.style.height = 'auto';
        currentImage.style.display = 'block';
        currentImage.style.margin = '16px auto';
      }
    });
  }
  ElMessage.success('图片上传成功');
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
  await formRef.value?.validate();
  submitting.value = true;
  const payload = {
    ...form,
    content: sanitizeHtml(form.content)
  };
  try {
    if (isEdit.value) {
      await diaryApi.update(route.params.id, payload);
      ElMessage.success('更新成功');
      router.push(`/diaries/${route.params.id}`);
    } else {
      const res = await diaryApi.create(payload);
      ElMessage.success('创建成功');
      router.push(`/diaries/${res.data?.id}`);
    }
  } finally {
    submitting.value = false;
  }
}

onMounted(loadDetail);
</script>

<style scoped lang="scss">
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-head > span {
  font-family: var(--font-display);
  font-size: 22px;
  color: var(--ink);
  letter-spacing: 0.02em;
}

/* Root card - Ink & Paper Journal design */
:deep(.el-card) {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--border-light);
  padding: 20px 24px;
}

:deep(.el-card__body) {
  padding: 24px;
}

/* Buttons */
:deep(.el-button--primary) {
  --el-button-bg-color: var(--sage);
  --el-button-border-color: var(--sage);
  --el-button-hover-bg-color: var(--sage-deep);
  --el-button-hover-border-color: var(--sage-deep);
  --el-button-active-bg-color: var(--sage-deep);
  --el-button-active-border-color: var(--sage-deep);
  border-radius: var(--radius-sm);
  font-weight: 500;
  transition: var(--transition-fast);
}

:deep(.el-button--primary.is-plain) {
  --el-button-plain-bg-color: transparent;
  --el-button-border-color: var(--sage);
  --el-button-plain-text-color: var(--sage);
  --el-button-hover-text-color: var(--sage-deep);
  --el-button-hover-border-color: var(--sage-deep);
  --el-button-hover-bg-color: var(--sage-light);
  --el-button-active-bg-color: var(--sage-light);
  border-radius: var(--radius-sm);
  transition: var(--transition-fast);
}

:deep(.el-button--default) {
  --el-button-text-color: var(--text-secondary);
  --el-button-border-color: var(--border);
  --el-button-hover-text-color: var(--ink);
  --el-button-hover-border-color: var(--ink-light);
  --el-button-bg-color: transparent;
  border-radius: var(--radius-sm);
  transition: var(--transition-fast);
}

/* Form items */
:deep(.el-form-item__label) {
  color: var(--text);
  font-weight: 600;
  font-size: 14px;
  padding-bottom: 6px;
}

/* Input fields */
:deep(.el-input) {
  --el-input-border-color: var(--border);
  --el-input-hover-border-color: var(--sage);
  --el-input-focus-border-color: var(--sage);
  --el-input-text-color: var(--text);
  --el-input-placeholder-color: var(--text-muted);
  --el-input-bg-color: var(--bg-card);
  --el-input-border-radius: var(--radius-sm);
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--border) inset;
  border-radius: var(--radius-sm);
  transition: var(--transition-fast);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--sage) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--sage) inset;
}

:deep(.el-input .el-input__count) {
  color: var(--text-muted);
  font-size: 12px;
}

/* Radio group */
:deep(.el-radio) {
  color: var(--text-secondary);
}

:deep(.el-radio.is-checked) {
  color: var(--ink);
}

:deep(.el-radio__inner) {
  border-color: var(--border);
  background: var(--bg-card);
}

:deep(.el-radio.is-checked .el-radio__inner) {
  border-color: var(--sage);
  background: var(--sage);
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: var(--ink);
}

.upload-line {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cover-preview {
  width: 160px;
  height: 100px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
}

.editor-tip {
  margin-bottom: 10px;
  color: var(--text-muted);
  font-size: 14px;
}

.editor-actions {
  margin-bottom: 12px;
}

.editor-shell {
  width: 100%;
}

.editor {
  width: 100%;
  max-width: 100%;
  border: 2px solid var(--border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: border-color var(--transition-base), box-shadow var(--transition-base);
  box-shadow: var(--shadow-card);
}

.editor:focus-within {
  border-color: var(--sage);
  box-shadow: 0 0 0 4px rgba(122, 154, 126, 0.12);
}

.editor :deep(.ql-toolbar),
.editor :deep(.ql-container) {
  width: 100%;
}

.editor :deep(.ql-toolbar) {
  background: #fdfcf8;
  border-bottom: 1px solid var(--border-light);
  padding: 10px 16px;
  font-family: var(--font-body);
  position: sticky;
  top: 0;
  z-index: 2;
}

.editor :deep(.ql-container) {
  background: var(--bg-card);
}

.editor :deep(.ql-editor) {
  min-height: 560px;
  font-size: 16px;
  line-height: 2;
  color: var(--text);
  font-family: var(--font-body);
  padding: 28px 32px;
}

.editor :deep(.ql-editor.ql-blank::before) {
  color: var(--text-muted);
  font-style: normal;
  left: 24px;
}

.editor :deep(.ql-editor img) {
  display: block;
  width: auto;
  max-width: min(100%, 560px);
  max-height: 420px;
  object-fit: contain;
  margin: 16px auto;
  border-radius: var(--radius-sm);
}

/* Quill toolbar icon colors */
.editor :deep(.ql-formats button) {
  color: var(--text-secondary);
  transition: var(--transition-fast);
}

.editor :deep(.ql-formats button:hover) {
  color: var(--ink);
}

.editor :deep(.ql-formats .ql-active) {
  color: var(--sage);
}

.editor :deep(.ql-picker) {
  color: var(--text-secondary);
}

.editor :deep(.ql-picker:hover) {
  color: var(--ink);
}

.editor :deep(.ql-picker.ql-expanded) {
  color: var(--sage);
}

@media (max-width: 768px) {
  .section-head {
    gap: 10px;
  }

  .upload-line {
    align-items: flex-start;
    flex-direction: column;
  }

  .cover-preview {
    width: 100%;
    max-width: 240px;
    height: auto;
  }

  .editor :deep(.ql-editor) {
    min-height: 320px;
    padding: 16px;
  }
}
</style>
