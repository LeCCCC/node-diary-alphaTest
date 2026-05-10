<template>
  <div class="detail-page" v-loading="loading">
    <el-card v-if="detail">
      <template #header>
        <div class="header-line">
          <div>
            <div class="title">{{ detail.title }}</div>
            <div class="meta">作者：{{ detail.authorName || detail.nickname || '-' }}</div>
          </div>
          <div class="actions">
            <el-button @click="$router.push('/diaries')">返回列表</el-button>
            <el-button type="primary" @click="$router.push(`/diaries/${detail.id}/edit`)">编辑</el-button>
          </div>
        </div>
      </template>

      <img v-if="detail.coverImage" :src="withBaseUrl(detail.coverImage)" class="cover" alt="cover" />
      <div class="info-bar">
        <el-tag class="tag">{{ visibilityText(detail.visibility) }}</el-tag>
        <span>创建时间：{{ createdTime }}</span>
        <span>更新时间：{{ updatedTime }}</span>
      </div>
      <div class="rich-html" v-html="normalizedHtml"></div>
    </el-card>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router';
import { diaryApi } from '@/api/modules';
import { formatTime, pickDate, visibilityText, withBaseUrl } from '@/utils/common';
import { sanitizeHtml } from '@/utils/sanitize';

const route = useRoute();
const loading = ref(false);
const detail = ref(null);

const createdTime = computed(() => formatTime(pickDate(
  detail.value?.createAt,
  detail.value?.createdAt,
  detail.value?.createTime,
  detail.value?.created_time,
  detail.value?.gmtCreate
)));

const updatedTime = computed(() => formatTime(pickDate(
  detail.value?.updateAt,
  detail.value?.updatedAt,
  detail.value?.updateTime,
  detail.value?.updated_time,
  detail.value?.gmtModified,
  detail.value?.modifiedAt,
  detail.value?.createAt
)));

const normalizedHtml = computed(() => {
  const html = detail.value?.content || '';
  const target = import.meta.env.VITE_API_BASE_TARGET || window.location.origin;
  const normalized = html.replace(/src=["'](\/uploads\/[^"']+)["']/g, `src="${target}$1"`);
  return sanitizeHtml(normalized);
});

async function loadDetail() {
  loading.value = true;
  try {
    const res = await diaryApi.getDetail(route.params.id);
    detail.value = res.data;
  } finally {
    loading.value = false;
  }
}

onMounted(loadDetail);
</script>

<style scoped lang="scss">
.detail-page {
  .header-line {
    display: flex;
    justify-content: space-between;
    gap: 16px;
  }

  .title {
    font-family: var(--font-display);
    font-size: 30px;
    color: var(--ink);
    line-height: 1.35;
    letter-spacing: 0.02em;
  }

  .meta {
    margin-top: 8px;
    color: var(--text-muted);
    font-size: 14px;
  }

  .cover {
    width: 100%;
    max-height: 320px;
    object-fit: cover;
    border-radius: var(--radius-md);
    margin-bottom: 24px;
  }

  .info-bar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 14px;
    color: var(--text-muted);
    font-size: 13px;
    margin-bottom: 18px;
    padding: 12px 16px;
    background: var(--bg);
    border-radius: var(--radius-sm);
    border: 1px solid var(--border-light);
  }

  .actions {
    display: flex;
    gap: 12px;

    :deep(.el-button--default) {
      --el-button-text-color: var(--text-secondary);
      --el-button-border-color: var(--border);
      --el-button-hover-text-color: var(--ink);
      --el-button-hover-border-color: var(--ink-light);
      --el-button-bg-color: transparent;
      border-radius: var(--radius-sm);
      transition: var(--transition-fast);
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
  }

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

  :deep(.el-tag) {
    background: var(--amber-light);
    border: 1px solid var(--amber);
    color: var(--amber);
    border-radius: 8px;
    font-weight: 500;
  }

  .rich-html {
    color: var(--text);
    line-height: 1.9;
    font-size: 16px;

    :deep(p) {
      margin-bottom: 1em;
    }

    :deep(img) {
      max-width: 100%;
      height: auto;
      border-radius: var(--radius-sm);
      display: block;
      margin: 20px auto;
    }

    :deep(blockquote) {
      border-left: 3px solid var(--border);
      padding-left: 16px;
      color: var(--text-secondary);
      font-style: italic;
      margin: 16px 0;
    }

    :deep(h1),
    :deep(h2),
    :deep(h3),
    :deep(h4) {
      font-family: var(--font-display);
      color: var(--ink);
      margin-top: 1.5em;
      margin-bottom: 0.5em;
    }

    :deep(a) {
      color: var(--sage);
      text-decoration: underline;
      text-underline-offset: 2px;
    }

    :deep(a:hover) {
      color: var(--sage-deep);
    }

    :deep(ul),
    :deep(ol) {
      padding-left: 1.5em;
      margin-bottom: 1em;
    }

    :deep(li) {
      margin-bottom: 0.3em;
    }
  }
}

@media (max-width: 768px) {
  .detail-page {
    .header-line {
      flex-direction: column;
      align-items: flex-start;
    }

    .title {
      font-size: 24px;
    }

    .actions {
      width: 100%;
      display: grid;
      grid-template-columns: 1fr 1fr;

      :deep(.el-button) {
        width: 100%;
      }
    }
  }
}

@media (max-width: 480px) {
  .detail-page .actions {
    grid-template-columns: 1fr;
  }
}
</style>
