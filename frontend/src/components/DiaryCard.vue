<template>
  <article class="diary-card" :class="cardClass" @click="$router.push(`/diaries/${diary.id}`)">
    <div class="card-header">
      <div class="title-row">
        <h3 class="title">{{ diary.title }}</h3>
        <el-tag v-if="isMatchedDiary" size="small" class="vis-tag vis-matched">匹配对象日记</el-tag>
      </div>
      <el-tag v-if="shouldShowVisibility" size="small" class="vis-tag" :class="`vis-${visType}`">{{ visibilityLabel }}</el-tag>
    </div>

    <div class="card-body">
      <img v-if="diary.coverImage" :src="withBaseUrl(diary.coverImage)" class="cover" alt="cover" loading="lazy" />
      <p class="summary">{{ diary.summary || '暂无摘要' }}</p>
    </div>

    <div class="card-footer">
      <span class="meta">{{ createdTime }}</span>
      <div class="actions" @click.stop>
        <el-button class="btn-detail" link type="primary" @click="$router.push(`/diaries/${diary.id}`)">详情</el-button>
        <template v-if="canEdit">
          <el-button class="btn-edit" link @click="$router.push(`/diaries/${diary.id}/edit`)">编辑</el-button>
          <el-popconfirm title="确定删除这篇日记吗？" @confirm="$emit('delete', diary)">
            <template #reference>
              <el-button class="btn-delete" link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue';
import { formatTime, pickDate, diaryVisibilityLabel, withBaseUrl } from '@/utils/common';

const props = defineProps({
  diary: { type: Object, required: true }
});

defineEmits(['delete']);

const isMatchedDiary = computed(() => {
  const t = String(props.diary?.authorType || '').toUpperCase();
  return t === 'MATCHED' || t === 'MATCH' || t === 'OTHER';
});
const isSelfDiary = computed(() => !isMatchedDiary.value);
const shouldShowVisibility = computed(() => isSelfDiary.value && props.diary?.visibility !== undefined && props.diary?.visibility !== null);
const canEdit = computed(() => isSelfDiary.value);
const cardClass = computed(() => ({ 'matched-card': isMatchedDiary.value }));
const createdTime = computed(() => formatTime(pickDate(props.diary?.createdAt, props.diary?.createAt, props.diary?.createTime, props.diary?.created_time)));
const visInfo = computed(() => diaryVisibilityLabel(props.diary));
const visibilityLabel = computed(() => visInfo.value.label);
const visType = computed(() => visInfo.value.type);
</script>

<style scoped lang="scss">
.diary-card {
  background: linear-gradient(145deg, #fdfcf8, #f8f6f0);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
  display: flex;
  flex-direction: column;
  animation: card-enter 0.5s cubic-bezier(0.4, 0, 0.2, 1) both;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 30px rgba(44, 58, 79, 0.1);
    border-color: var(--border);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  padding: 20px 20px 0;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: 0.01em;
}

.vis-tag {
  flex-shrink: 0;
  font-weight: 500;
  border-radius: 8px;
}

.vis-private {
  background: #f5f3ef;
  border-color: #e8e0d4;
  color: #9a8f84;
}

.vis-shared {
  background: var(--amber-light);
  border-color: var(--amber);
  color: var(--amber-deep);
}

.vis-matched {
  background: var(--sage-light);
  border-color: var(--sage);
  color: var(--sage-deep);
}

.card-body {
  padding: 14px 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(44, 58, 79, 0.06);
}

.summary {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
  min-height: 48px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px 16px;
  margin-top: auto;
}

.meta {
  color: var(--text-muted);
  font-size: 12.5px;
}

.actions {
  display: flex;
  gap: 2px;

  :deep(.el-button) {
    font-size: 13px;
  }

  :deep(.btn-detail) {
    color: var(--sage-deep);
  }

  :deep(.btn-edit) {
    color: var(--ink-light);
  }

  :deep(.btn-delete) {
    color: #d9534f !important;
  }
}

/* Matched diary variant */
.matched-card {
  border-left: 4px solid var(--amber);

  &:hover {
    border-left-color: var(--amber-deep);
  }
}
</style>
