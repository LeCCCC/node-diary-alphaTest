<template>
  <div class="dashboard">
    <!-- Hero -->
    <div class="hero">
      <div class="hero-content">
        <span class="hero-kicker">结 · 日记</span>
        <h1 class="hero-title">你好，{{ displayName }}</h1>
        <p class="hero-desc">欢迎回到主页。这里会汇总你最近的记录、更新时间和共享动态，你也可以一键开始写今天的新日记。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/diaries/create')">开始写日记</el-button>
          <el-button size="large" @click="$router.push('/match')">查看匹配状态</el-button>
        </div>
      </div>
      <div class="hero-emblem">
        <img :src="APP_LOGO" alt="logo" class="emblem-img" />
        <div class="emblem-ring"></div>
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-card stat-ink">
        <div class="stat-label">日记总数</div>
        <div class="stat-number">{{ total }}</div>
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
        </div>
      </div>
      <div class="stat-card stat-sage">
        <div class="stat-label">最近更新</div>
        <div class="stat-number small">{{ latestTime }}</div>
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        </div>
      </div>
      <div class="stat-card stat-amber">
        <div class="stat-label">我的日记</div>
        <div class="stat-number">{{ visibleCount }}</div>
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
        </div>
      </div>
    </div>

    <!-- Feed -->
    <el-card class="feed-card">
      <template #header>
        <div class="feed-header">
          <div>
            <span class="feed-title">最近日记</span>
            <span class="feed-subtitle">浏览最近的公开日记与匹配共享</span>
          </div>
          <el-button type="primary" @click="$router.push('/diaries/create')">写新日记</el-button>
        </div>
      </template>

      <el-empty v-if="!records.length" description="还没有日记，开始写第一篇吧" />
      <div v-else class="card-grid">
        <DiaryCard v-for="item in records" :key="item.id" :diary="item" @delete="handleDelete" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import DiaryCard from '@/components/DiaryCard.vue';
import { diaryApi } from '@/api/modules';
import { formatTime, pickDate } from '@/utils/common';
import { APP_LOGO } from '@/utils/constants';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const records = ref([]);
const total = ref(0);
const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '你好');
const latestTime = computed(() => {
  const latest = records.value[0];
  return formatTime(pickDate(latest?.updatedAt, latest?.updateTime, latest?.createdAt, latest?.createTime));
});
const visibleCount = computed(() => records.value.filter((item) => item.authorType === 'SELF' || !item.authorType).length);

async function loadData() {
  const res = await diaryApi.getHomeFeed({ pageNum: 1, pageSize: 6 });
  const data = res.data || {};
  records.value = data.records || [];
  total.value = data.total || records.value.length || 0;
}

async function handleDelete(item) {
  await diaryApi.remove(item.id);
  ElMessage.success('删除成功');
  loadData();
}

onMounted(loadData);
</script>

<style scoped lang="scss">
.dashboard {
  display: grid;
  gap: 22px;
}

/* ---- Hero ---- */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  padding: 32px 36px;
  background: linear-gradient(135deg, var(--bg-card) 0%, #fbf8f0 100%);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: -60px;
    right: -20px;
    width: 200px;
    height: 200px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(122, 154, 126, 0.08) 0%, transparent 70%);
    pointer-events: none;
  }
}

.hero-kicker {
  display: inline-flex;
  padding: 5px 14px;
  border-radius: 999px;
  background: var(--sage-light);
  color: var(--sage-deep);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.hero-title {
  margin: 16px 0 0;
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 700;
  color: var(--ink);
  letter-spacing: 0.02em;
  line-height: 1.2;
}

.hero-desc {
  margin: 10px 0 0;
  color: var(--text-secondary);
  line-height: 1.8;
  max-width: 600px;
}

.hero-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-emblem {
  position: relative;
  flex-shrink: 0;
}

.emblem-img {
  width: 108px;
  height: 108px;
  border-radius: 24px;
  box-shadow: 0 12px 32px rgba(44, 58, 79, 0.12);
  position: relative;
  z-index: 1;
  animation: float 5s ease-in-out infinite;
}

.emblem-ring {
  position: absolute;
  inset: -12px;
  border-radius: 32px;
  border: 2px dashed var(--border);
  opacity: 0.5;
  animation: breathe 6s ease-in-out infinite;
}

/* ---- Stats ---- */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  padding: 22px 24px;
  border-radius: var(--radius-lg);
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  position: relative;
  overflow: hidden;
  transition: all var(--transition-base);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-card-hover);
  }
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
}

.stat-number {
  margin-top: 8px;
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 700;
  color: var(--ink);

  &.small {
    font-size: 18px;
    line-height: 1.6;
  }
}

.stat-icon {
  position: absolute;
  right: 18px;
  bottom: 18px;
  opacity: 0.12;
}

.stat-ink  { background: linear-gradient(135deg, #f8f6f2, #fdfcf8); .stat-icon { color: var(--ink); } }
.stat-sage { background: linear-gradient(135deg, #f4f7f2, #fdfcf8); .stat-icon { color: var(--sage); } }
.stat-amber { background: linear-gradient(135deg, #fdf9f2, #fdfcf8); .stat-icon { color: var(--amber); } }

/* ---- Feed ---- */
.feed-card {
  border: 1px solid var(--border-light) !important;
  animation: card-enter 0.5s var(--transition-slow) both;
  animation-delay: 0.15s;
}

.feed-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.feed-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--ink);
}

.feed-subtitle {
  display: block;
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-muted);
  font-family: var(--font-body);
}

/* ---- Responsive ---- */
@media (max-width: 900px) {
  .hero {
    flex-direction: column-reverse;
    align-items: flex-start;
    padding: 24px;
  }

  .hero-emblem {
    align-self: center;
  }

  .emblem-img {
    width: 80px;
    height: 80px;
    border-radius: 20px;
  }

  .hero-title {
    font-size: 28px;
  }

  .stats-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 24px;
  }
}
</style>
