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
        <el-tag class="tag" :class="`tag-${visInfo.type}`">{{ visInfo.label }}</el-tag>        <span>创建时间：{{ createdTime }}</span>
        <span>更新时间：{{ updatedTime }}</span>
      </div>
      <div class="rich-html" v-html="normalizedHtml"></div>
    </el-card>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router';
import { diaryApi } from '@/api/modules';
import { formatTime, pickDate, diaryVisibilityLabel, getUserId, withBaseUrl } from '@/utils/common';
import { sanitizeHtml } from '@/utils/sanitize';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const userStore = useUserStore();
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

const visInfo = computed(() => diaryVisibilityLabel(detail.value, getUserId(userStore.userInfo)));

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
      --el-button-bg-color: transparent;
      --el-button-hover-text-color: var(--sage-deep);
      --el-button-hover-border-color: var(--sage);
      --el-button-hover-bg-color: var(--sage-light);
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
    border-radius: 8px;
    font-weight: 500;
  }

  :deep(.tag-private) {
    background: #f5f3ef;
    border-color: #e8e0d4;
    color: #9a8f84;
  }

  :deep(.tag-shared) {
    background: var(--amber-light);
    border-color: var(--amber);
    color: var(--amber-deep);
  }

  :deep(.tag-matched) {
    background: var(--sage-light);
    border-color: var(--sage);
    color: var(--sage-deep);
  }

  .rich-html {
    --detail-line-height: 32px;
    --detail-line-color: rgba(120, 95, 70, 0.34);

    color: var(--text);
    line-height: var(--detail-line-height);
    font-size: 16px;

    /* 段落与列表：基础尺寸 + 整行横线 */
    :deep(p) {
      margin: 0;
      padding: 0;
      line-height: var(--detail-line-height);
      min-height: var(--detail-line-height);
    }

    :deep(p:not(.diary-image-line)) {
      background-image: repeating-linear-gradient(
        to bottom,
        transparent,
        transparent calc(var(--detail-line-height) - 1px),
        var(--detail-line-color) calc(var(--detail-line-height) - 1px),
        var(--detail-line-color) var(--detail-line-height)
      );
      background-size: 100% var(--detail-line-height);
      background-position: 0 -6px;
    }

    :deep(li) {
      margin: 0;
      padding: 0;
      line-height: var(--detail-line-height);
      min-height: var(--detail-line-height);
      background-image: repeating-linear-gradient(
        to bottom,
        transparent,
        transparent calc(var(--detail-line-height) - 1px),
        var(--detail-line-color) calc(var(--detail-line-height) - 1px),
        var(--detail-line-color) var(--detail-line-height)
      );
      background-size: 100% var(--detail-line-height);
      background-position: 0 -6px;
    }

    /* 图片行不画横线，居中展示 */
    :deep(.diary-image-line),
    :deep(p:has(> img)) {
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      margin: 12px 0 !important;
      padding: 8px 0 !important;
      line-height: 0 !important;
      min-height: 0 !important;
      background-image: none !important;
    }

    :deep(img),
    :deep(p img),
    :deep(.diary-inline-image) {
      display: block !important;
      max-width: min(100%, 520px) !important;
      max-height: 360px !important;
      width: auto !important;
      height: auto !important;
      object-fit: unset !important;
      border-radius: 12px !important;
      margin: 0 auto !important;
      box-shadow: 0 8px 24px -8px rgba(44, 58, 79, 0.18) !important;
    }

    /* 分割线 */
    :deep(hr.diary-divider) {
      border: 0;
      height: 32px;
      margin: 0 auto;
      background-image:
        radial-gradient(circle, var(--amber) 1px, transparent 1.2px),
        radial-gradient(circle, var(--amber) 1px, transparent 1.2px),
        radial-gradient(circle, var(--amber) 1px, transparent 1.2px);
      background-size: 8px 16px;
      background-position: calc(50% - 20px) 50%, 50% 50%, calc(50% + 20px) 50%;
      background-repeat: no-repeat;
      opacity: 0.55;
    }

    /* 标题不加横线 */
    :deep(h1),
    :deep(h2) {
      font-family: var(--font-display);
      color: var(--ink);
      letter-spacing: 0.02em;
      margin: 0;
      padding: 0;
      background-image: none;
    }

    :deep(h1) {
      font-size: 26px;
      line-height: 64px;
      min-height: 64px;
    }

    :deep(h2) {
      font-size: 22px;
      line-height: 32px;
      min-height: 32px;
    }

    :deep(h3),
    :deep(h4) {
      font-family: var(--font-display);
      color: var(--ink);
      margin: 0;
      padding: 0;
      background-image: none;
    }

    :deep(blockquote) {
      position: relative;
      margin: 0;
      padding: 0 18px 0 22px;
      border-left: 3px solid var(--sage);
      background: linear-gradient(90deg, rgba(122, 154, 126, 0.08), transparent 70%);
      color: var(--text-secondary);
      border-radius: 0 10px 10px 0;
      font-style: italic;
      line-height: var(--detail-line-height);
      min-height: var(--detail-line-height);
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
    }
  }
}

@media (max-width: 768px) {
  .detail-page {
    .header-line {
      flex-direction: column;
      align-items: stretch;
    }

    .title {
      font-size: 24px;
    }

    .actions {
      width: 100%;
      display: grid;
      grid-template-columns: 1fr 1fr;
      align-items: center;

      :deep(.el-button) {
        width: 100%;
        justify-content: center;
      }
    }

    .rich-html {
      --detail-line-height: 30px;
      --detail-line-color: rgba(120, 95, 70, 0.32);
      font-size: 15px;

      :deep(p:not(.diary-image-line)) {
        background-position: 0 -5px;
      }
      :deep(li) {
        background-position: 0 -5px;
      }
      :deep(img),
      :deep(p img),
      :deep(.diary-inline-image) {
        max-width: 100% !important;
        max-height: 270px !important;
      }
      :deep(.diary-image-line),
      :deep(p:has(> img)) {
        margin: 10px 0 !important;
        padding: 6px 0 !important;
      }
      :deep(h1) {
        line-height: 60px;
        min-height: 60px;
      }
      :deep(h2) {
        line-height: 30px;
        min-height: 30px;
      }
      :deep(blockquote) {
        line-height: 30px;
        min-height: 30px;
      }
      :deep(hr.diary-divider) {
        height: 30px;
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
