<template>
  <article class="tree-hole-card" @click="goDetail">
    <div class="card-top">
      <div class="badge-row">
        <span class="bubble-tag">匿名树洞</span>
        <span v-if="isMine" class="mine-tag">我的发布</span>
      </div>
      <span class="created-at">{{ createdTime }}</span>
    </div>

    <p class="summary">{{ summaryText }}</p>

    <div class="card-bottom">
      <div class="meta-group">
        <span>{{ commentCount }} 条评论</span>
        <span>更新于 {{ updatedTime }}</span>
      </div>
      <el-button link type="primary" @click.stop="goDetail">查看详情</el-button>
    </div>
  </article>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { extractId, formatTime, getTextSummary, getUserId, pickDate, resolveField } from '@/utils/common';

const props = defineProps({
  hole: { type: Object, required: true }
});

const router = useRouter();
const userStore = useUserStore();

const holeId = computed(() => extractId(props.hole, ['treeHoleId', 'id']));
const publisherId = computed(() => resolveField(props.hole, ['publisherId']));
const isMine = computed(() => String(publisherId.value ?? '') !== '' && String(publisherId.value) === String(getUserId(userStore.userInfo)));
const createdTime = computed(() => formatTime(pickDate(props.hole?.createTime, props.hole?.createdAt, props.hole?.gmtCreate), 'MM-DD HH:mm'));
const updatedTime = computed(() => formatTime(pickDate(props.hole?.updateTime, props.hole?.updatedAt, props.hole?.createTime), 'MM-DD HH:mm'));
const commentCount = computed(() => Number(resolveField(props.hole, ['commentCount']) || 0));
const summaryText = computed(() => getTextSummary(resolveField(props.hole, ['summary', 'content']), 120) || '这个树洞还没有内容。');

function goDetail() {
  if (!holeId.value) return;
  router.push(`/tree-holes/${holeId.value}`);
}
</script>

<style scoped lang="scss">
.tree-hole-card {
  background: linear-gradient(145deg, #fdfcf8, #f8f6f0);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 22px;
  cursor: pointer;
  transition: all var(--transition-base);
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: card-enter 0.5s cubic-bezier(0.4, 0, 0.2, 1) both;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 30px rgba(44, 58, 79, 0.1);
    border-color: var(--border);
  }
}

.card-top,
.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.badge-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.bubble-tag,
.mine-tag {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.bubble-tag {
  background: var(--slate-light);
  color: var(--slate);
}

.mine-tag {
  background: var(--sage-light);
  color: var(--sage-deep);
}

.created-at {
  color: var(--text-muted);
  font-size: 12.5px;
  white-space: nowrap;
}

.summary {
  flex: 1;
  margin: 0;
  color: var(--text);
  line-height: 1.85;
  min-height: 110px;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 14.5px;
}

.meta-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
  color: var(--text-muted);
  font-size: 12.5px;
}

@media (max-width: 640px) {
  .card-top,
  .card-bottom {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary {
    min-height: 80px;
  }
}
</style>
