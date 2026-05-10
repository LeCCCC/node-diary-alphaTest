<template>
  <el-card>
    <template #header>
      <div class="section-head">
        <span>我的日记</span>
        <el-button type="primary" @click="$router.push('/diaries/create')">写日记</el-button>
      </div>
    </template>

    <el-empty v-if="!records.length" description="暂无日记" />
    <div v-else class="card-grid">
      <DiaryCard v-for="item in records" :key="item.id" :diary="item" @delete="handleDelete" />
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        background
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadData"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import DiaryCard from '@/components/DiaryCard.vue';
import { diaryApi } from '@/api/modules';

const query = reactive({
  pageNum: 1,
  pageSize: 9
});
const records = ref([]);
const total = ref(0);

async function loadData() {
  const res = await diaryApi.getMyList(query);
  const data = res.data || {};
  records.value = data.records || [];
  total.value = data.total || 0;
}

async function handleDelete(item) {
  await diaryApi.remove(item.id);
  ElMessage.success('删除成功');
  if (records.value.length === 1 && query.pageNum > 1) query.pageNum -= 1;
  loadData();
}

onMounted(loadData);
</script>

<style scoped>
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.section-head > span {
  font-family: var(--font-display);
  font-size: 22px;
  color: var(--ink);
  letter-spacing: 0.02em;
}

/* Root card - Ink & Paper Journal design */
.el-card {
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

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.pagination :deep(.el-pagination) {
  --el-pagination-button-color: var(--text-secondary);
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: var(--sage);
  --el-pagination-font-size: 14px;
  font-weight: 400;
}

.pagination :deep(.el-pager li) {
  border-radius: var(--radius-sm);
  min-width: 34px;
  height: 34px;
  line-height: 34px;
  transition: var(--transition-fast);
}

.pagination :deep(.el-pager li:hover) {
  color: var(--sage-deep);
  background: var(--sage-light);
}

.pagination :deep(.el-pager li.is-active) {
  color: #fff;
  background: var(--sage);
  font-weight: 600;
}

.pagination :deep(.el-pagination button:disabled) {
  color: var(--text-muted);
  background: transparent;
}

.pagination :deep(.btn-prev),
.pagination :deep(.btn-next) {
  border-radius: var(--radius-sm);
  min-width: 34px;
  height: 34px;
}

.pagination :deep(.el-pagination__total) {
  color: var(--text-muted);
  font-size: 13px;
}

@media (max-width: 640px) {
  .pagination {
    justify-content: center;
  }
}
</style>
