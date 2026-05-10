<template>
  <div class="tree-hole-form-page">
    <el-card class="glass-card intro-card">
      <div class="intro-wrap">
        <div>
          <div class="intro-kicker">{{ isEdit ? '编辑树洞' : '发布树洞' }}</div>
          <div class="intro-title">{{ isEdit ? '重新整理这段情绪，让它更贴近现在的你。' : '把想说的话留在这里，匿名地被看见。' }}</div>
          <div class="intro-desc">树洞在广场和评论区都会匿名展示。详情页内可匿名评论，不显示评论者；只有你自己发布的树洞才会显示编辑和删除按钮。</div>
        </div>
        <el-button @click="$router.push('/tree-holes')">返回树洞</el-button>
      </div>
    </el-card>

    <div class="form-grid">
      <el-card class="glass-card main-card" v-loading="loading">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="树洞内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="14"
              maxlength="3000"
              show-word-limit
              resize="vertical"
              placeholder="你可以写下心情、困惑、遗憾，或者任何今天想说的话……"
            />
          </el-form-item>

          <div class="actions">
            <el-button :disabled="submitting" @click="$router.push('/tree-holes')">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ isEdit ? '保存修改' : '发布树洞' }}</el-button>
          </div>
        </el-form>
      </el-card>

      <div class="side-column">
        <el-card class="glass-card tip-card">
          <div class="side-title">发布提示</div>
          <ul class="tip-list">
            <li>树洞列表展示摘要和时间，完整内容需进入详情页查看。</li>
            <li>评论默认匿名展示，详情页不会显示评论发布者信息。</li>
            <li>内容建议控制在 3000 字内，保持表达清晰。</li>
          </ul>
        </el-card>

        <el-card class="glass-card preview-card">
          <div class="side-title">实时预览</div>
          <div class="preview-content">{{ previewText }}</div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { treeHoleApi } from '@/api/modules';
import { getTextSummary, resolveField } from '@/utils/common';

const route = useRoute();
const router = useRouter();
const formRef = ref();
const loading = ref(false);
const submitting = ref(false);

const isEdit = computed(() => Boolean(route.params.id));
const previewText = computed(() => getTextSummary(form.content, 180) || '这里会显示你的树洞摘要预览。');

const form = reactive({
  content: ''
});

const rules = {
  content: [
    { required: true, message: '请输入树洞内容', trigger: 'blur' },
    { min: 1, max: 3000, message: '树洞内容长度需在 1 到 3000 个字符之间', trigger: 'blur' }
  ]
};

async function loadDetail() {
  if (!isEdit.value) return;
  loading.value = true;
  try {
    const res = await treeHoleApi.getDetail(route.params.id);
    const data = res.data || {};
    form.content = resolveField(data, ['content']) || '';
  } finally {
    loading.value = false;
  }
}

async function handleSubmit() {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    if (isEdit.value) {
      await treeHoleApi.update(route.params.id, { content: form.content });
      ElMessage.success('树洞已更新');
      router.push(`/tree-holes/${route.params.id}`);
    } else {
      const res = await treeHoleApi.create({ content: form.content });
      const createdId = resolveField(res.data, ['treeHoleId']);
      ElMessage.success('树洞发布成功');
      router.push(createdId ? `/tree-holes/${createdId}` : '/tree-holes');
    }
  } finally {
    submitting.value = false;
  }
}

onMounted(loadDetail);
</script>

<style scoped lang="scss">
.tree-hole-form-page {
  display: grid;
  gap: 20px;
}

.glass-card {
  border: 1px solid var(--border-light);
  background: linear-gradient(145deg, #fdfcf8, #f8f6f0);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

.intro-wrap {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.intro-kicker {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--sage-light);
  color: var(--sage-deep);
  font-size: 13px;
  font-weight: 700;
}

.intro-title {
  margin-top: 14px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.4;
  font-family: var(--font-display);
  color: var(--ink);
}

.intro-desc {
  margin-top: 10px;
  color: var(--text-secondary);
  line-height: 1.85;
  max-width: 760px;
}

.form-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) 320px;
  gap: 20px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.side-column {
  display: grid;
  gap: 20px;
  align-content: start;
}

.side-title {
  font-size: 18px;
  font-weight: 700;
  font-family: var(--font-display);
  color: var(--ink);
}

.tip-list {
  margin: 14px 0 0;
  padding-left: 18px;
  color: var(--text-secondary);
  line-height: 1.85;
}

.preview-content {
  margin-top: 14px;
  min-height: 180px;
  padding: 18px;
  border-radius: var(--radius-md);
  background: var(--bg);
  color: var(--text);
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 960px) {
  .intro-wrap {
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .intro-title {
    font-size: 24px;
  }
}
</style>
