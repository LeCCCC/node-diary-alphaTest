<template>
  <div class="tree-hole-detail-page" v-loading="loading">
    <el-card v-if="detail" class="glass-card detail-card">
      <template #header>
        <div class="header-line">
          <div>
            <div class="title-row">
              <span class="page-title">树洞详情</span>
              <span class="bubble-tag">匿名树洞</span>
              <span v-if="isMine" class="mine-tag">我的发布</span>
            </div>
            <div class="meta-row">发布于 {{ createdTime }} · 最近更新 {{ updatedTime }}</div>
          </div>
          <div class="actions">
            <el-button @click="$router.push('/tree-holes')">返回列表</el-button>
            <template v-if="isMine">
              <el-button type="primary" @click="$router.push(`/tree-holes/${holeId}/edit`)">编辑</el-button>
              <el-popconfirm title="确定删除这条树洞吗？" @confirm="handleDelete">
                <template #reference>
                  <el-button type="danger" plain>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </div>
        </div>
      </template>

      <div class="content-shell">
        <div class="content-label">树洞正文</div>
        <div class="content-box">{{ detailContent }}</div>
      </div>
    </el-card>

    <div class="comment-grid">
      <el-card class="glass-card composer-card">
        <div class="composer-head">
          <div>
            <div class="section-title">匿名评论</div>
            <div class="section-desc">评论只展示内容和时间，不显示评论者身份。</div>
          </div>
          <el-tag type="info">{{ comments.length }} 条</el-tag>
        </div>

        <el-input
          v-model="commentForm.content"
          type="textarea"
          :rows="5"
          maxlength="1000"
          show-word-limit
          resize="vertical"
          placeholder="写下你的评论……"
        />
        <div class="composer-actions">
          <el-button type="primary" :loading="commentSubmitting" @click="handleSubmitComment">发表评论</el-button>
        </div>
      </el-card>

      <el-card class="glass-card comment-card">
        <template #header>
          <div class="comment-list-head">
            <div>
              <div class="section-title">全部评论</div>
              <div class="section-desc">按时间展示所有评论内容。</div>
            </div>
          </div>
        </template>

        <el-empty v-if="!comments.length" description="还没有评论，来留下第一条吧" />
        <div v-else class="comment-list">
          <div v-for="(item, index) in comments" :key="commentKey(item, index)" class="comment-item">
            <div class="comment-meta">
              <span>评论 {{ index + 1 }}</span>
              <span>{{ formatCommentTime(item) }}</span>
            </div>
            <div class="comment-content">{{ commentContent(item) }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { treeHoleApi } from '@/api/modules';
import {
  extractId,
  formatTime,
  getUserId,
  normalizeListData,
  pickDate,
  resolveField
} from '@/utils/common';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const commentSubmitting = ref(false);
const detail = ref(null);
const comments = ref([]);
const commentForm = reactive({
  content: ''
});

const holeId = computed(() => route.params.id);
const publisherId = computed(() => resolveField(detail.value, ['publisherId']));
const isMine = computed(() => String(publisherId.value ?? '') !== '' && String(publisherId.value) === String(getUserId(userStore.userInfo)));
const detailContent = computed(() => resolveField(detail.value, ['content']) || '暂无内容');
const createdTime = computed(() => formatTime(pickDate(
  detail.value?.createTime,
  detail.value?.createdAt,
  detail.value?.gmtCreate
)));
const updatedTime = computed(() => formatTime(pickDate(
  detail.value?.updateTime,
  detail.value?.updatedAt,
  detail.value?.createTime
)));

function commentContent(item) {
  return resolveField(item, ['content']) || '暂无评论内容';
}

function formatCommentTime(item) {
  return formatTime(pickDate(
    item?.createTime,
    item?.createdAt,
    item?.gmtCreate
  ));
}

function commentKey(item, index) {
  return extractId(item, ['commentId', 'id']) || `${index}-${formatCommentTime(item)}`;
}

async function loadComments() {
  const res = await treeHoleApi.getComments(holeId.value, { pageNum: 1, pageSize: 100 });
  comments.value = normalizeListData(res.data || {});
}

async function loadDetail() {
  loading.value = true;
  try {
    const [detailRes] = await Promise.all([
      treeHoleApi.getDetail(holeId.value),
      loadComments()
    ]);
    detail.value = detailRes.data || {};
  } finally {
    loading.value = false;
  }
}

async function handleSubmitComment() {
  const content = commentForm.content.trim();
  if (!content) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  commentSubmitting.value = true;
  try {
    await treeHoleApi.createComment(holeId.value, { content });
    commentForm.content = '';
    ElMessage.success('评论成功');
    await loadComments();
  } finally {
    commentSubmitting.value = false;
  }
}

async function handleDelete() {
  await treeHoleApi.remove(holeId.value);
  ElMessage.success('树洞已删除');
  router.replace('/tree-holes');
}

onMounted(loadDetail);
</script>

<style scoped lang="scss">
.tree-hole-detail-page {
  display: grid;
  gap: 20px;
}

.glass-card {
  border: 1px solid var(--border-light);
  background: linear-gradient(145deg, #fdfcf8, #f8f6f0);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

.header-line {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  font-family: var(--font-display);
  color: var(--ink);
}

.bubble-tag,
.mine-tag {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.bubble-tag {
  background: var(--slate-light);
  color: var(--slate);
}

.mine-tag {
  background: var(--amber-light);
  color: var(--amber);
}

.meta-row {
  margin-top: 10px;
  color: var(--text-secondary);
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.content-shell {
  display: grid;
  gap: 14px;
}

.content-label {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-light);
}

.content-box {
  padding: 20px 22px;
  border-radius: var(--radius-md);
  background: var(--bg);
  border: 1px solid var(--border-light);
  line-height: 2;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--text);
}

.comment-grid {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 20px;
}

.composer-head,
.comment-list-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 14px;
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
  line-height: 1.75;
}

.composer-actions {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.comment-list {
  display: grid;
  gap: 14px;
}

.comment-item {
  padding: 16px 18px;
  border-radius: var(--radius-md);
  background: var(--bg);
  border: 1px solid var(--border-light);
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--text-secondary);
  font-size: 13px;
}

.comment-content {
  margin-top: 10px;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--text);
}

@media (max-width: 960px) {
  .comment-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .page-title {
    font-size: 24px;
  }
}
</style>
