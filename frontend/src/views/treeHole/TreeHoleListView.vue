<template>
  <div class="tree-hole-list-page">
    <el-card class="hero-card glass-card">
      <div class="hero-wrap">
        <div>
          <div class="hero-kicker">匿名树洞广场</div>
          <div class="hero-title">把那些不方便当面说出口的话，放在这里。</div>
          <div class="hero-desc">树洞广场会展示所有用户发布的树洞摘要。你可以匿名发布，也可以在详情页里留下匿名评论，让情绪被温柔接住。</div>
          <div class="hero-actions">
            <el-button type="primary" @click="$router.push('/tree-holes/create')">发布树洞</el-button>
            <el-button @click="loadData">刷新列表</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <div class="stats-grid">
      <el-card class="stat-card stat-card-blue">
        <div class="stat-title">当前页树洞</div>
        <div class="stat-value">{{ records.length }}</div>
      </el-card>
      <el-card class="stat-card stat-card-emerald">
        <div class="stat-title">我的发布</div>
        <div class="stat-value">{{ myCount }}</div>
      </el-card>
      <el-card class="stat-card stat-card-cyan">
        <div class="stat-title">最新评论数</div>
        <div class="stat-value">{{ commentTotal }}</div>
      </el-card>
    </div>

    <el-card class="glass-card list-card" v-loading="loading">
      <template #header>
        <div class="section-head">
          <div>
            <div class="section-title">树洞摘要</div>
            <div class="section-desc">支持本页关键词筛选，点击卡片可进入详情查看全文和评论。</div>
          </div>
          <div class="toolbar">
            <el-input
              v-model="keyword"
              clearable
              placeholder="搜索本页树洞内容"
              class="search-input"
            />
            <el-button type="primary" @click="$router.push('/tree-holes/create')">发布树洞</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!filteredRecords.length && !loading" :description="emptyDescription" />
      <div v-else class="card-grid">
        <TreeHoleCard
          v-for="item in filteredRecords"
          :key="extractId(item, ['treeHoleId', 'id'])"
          :hole="item"
        />
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
  </div>
</template>

<script setup>
import TreeHoleCard from '@/components/TreeHoleCard.vue';
import { useUserStore } from '@/stores/user';
import { treeHoleApi } from '@/api/modules';
import { extractId, getTextSummary, getUserId, normalizeListData, normalizeTotal, resolveField } from '@/utils/common';

const loading = ref(false);
const records = ref([]);
const total = ref(0);
const keyword = ref('');
const userStore = useUserStore();
const query = reactive({
  pageNum: 1,
  pageSize: 9
});

const filteredRecords = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  if (!text) return records.value;
  return records.value.filter((item) => {
    const content = getTextSummary(resolveField(item, ['summary', 'content']) || '', 200).toLowerCase();
    return content.includes(text);
  });
});
const myCount = computed(() => {
  const myId = String(getUserId(userStore.userInfo) ?? '');
  if (!myId) return 0;
  return records.value.filter((item) => String(resolveField(item, ['publisherId']) ?? '') === myId).length;
});
const commentTotal = computed(() => records.value.reduce((sum, item) => sum + Number(resolveField(item, ['commentCount']) || 0), 0));
const emptyDescription = computed(() => keyword.value ? '这一页没有匹配到相关树洞' : '还没有树洞，去发布第一条吧');

async function loadData() {
  loading.value = true;
  try {
    const res = await treeHoleApi.getList(query);
    const data = res.data || {};
    records.value = normalizeListData(data);
    total.value = normalizeTotal(data, records.value.length);
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>

<style scoped lang="scss">
.tree-hole-list-page {
  display: grid;
  gap: 20px;
}

.glass-card {
  border: 1px solid var(--border-light);
  background: linear-gradient(145deg, #fdfcf8, #f8f6f0);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

.hero-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.hero-kicker {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--amber-light);
  color: var(--amber);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.hero-title {
  margin-top: 14px;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.35;
  max-width: 760px;
  font-family: var(--font-display);
  color: var(--ink);
}

.hero-desc {
  margin-top: 10px;
  line-height: 1.9;
  color: var(--text-secondary);
  max-width: 760px;
}

.hero-actions {
  margin-top: 18px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  min-height: 132px;
  overflow: hidden;
  position: relative;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);

  &::after {
    content: '';
    position: absolute;
    right: -18px;
    top: -18px;
    width: 104px;
    height: 104px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
  }
}

.stat-card-blue {
  background: linear-gradient(145deg, rgba(90, 110, 122, 0.12), var(--bg-card));
}

.stat-card-emerald {
  background: linear-gradient(145deg, rgba(122, 154, 126, 0.15), var(--bg-card));
}

.stat-card-cyan {
  background: linear-gradient(145deg, rgba(196, 149, 74, 0.14), var(--bg-card));
}

.stat-title {
  color: var(--text-secondary);
}

.stat-value {
  margin-top: 10px;
  font-size: 30px;
  font-weight: 800;
  position: relative;
  z-index: 1;
  color: var(--ink);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  font-family: var(--font-display);
  color: var(--ink);
}

.section-desc {
  margin-top: 6px;
  color: var(--text-secondary);
  font-size: 14px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  width: 260px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .hero-wrap {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .hero-title {
    font-size: 24px;
  }

  .search-input {
    width: 100%;
  }

  .toolbar {
    width: 100%;
  }
}
</style>
