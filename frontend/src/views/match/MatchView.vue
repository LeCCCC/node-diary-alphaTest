<template>
  <div class="match-page" v-loading="loading">
    <!-- 标签卡片 -->
    <el-card class="tag-card">
      <template #header>
        <div class="section-head">
          <div>
            <div class="title">我的标签</div>
            <div class="desc">选择你感兴趣的标签（最多 10 个），系统会优先匹配标签重合度高的用户。</div>
          </div>
        </div>
      </template>

      <!-- 折叠态：横幅 + 选择标签按钮 -->
      <div v-if="!pickerOpen" class="tag-banner">
        <div class="banner-tags">
          <template v-if="selectedTags.length > 0">
            <span
              v-for="tag in selectedTags"
              :key="tag.id"
              class="banner-chip"
            >
              {{ tag.name }}
            </span>
          </template>
          <span v-else class="banner-empty">还没有选择兴趣标签</span>
        </div>
        <div class="banner-actions">
          <span class="tag-count" :class="{ full: selectedTagIds.length >= 10 }">
            已选 {{ selectedTagIds.length }} / 10
          </span>
          <el-button type="primary" @click="pickerOpen = true">选择标签</el-button>
        </div>
      </div>

      <!-- 展开态：完整标签选择面板 -->
      <Transition name="picker-slide">
        <div v-if="pickerOpen" class="tag-picker">
          <!-- 已选标签区 -->
          <div class="picker-selected">
            <span class="picker-selected-label">已选择</span>
            <div class="picker-selected-tags">
              <template v-if="selectedTags.length > 0">
                <span
                  v-for="tag in selectedTags"
                  :key="tag.id"
                  class="picker-chip picker-chip--remove"
                  @click="removeTag(tag.id)"
                >
                  {{ tag.name }}
                  <span class="picker-chip-x">&times;</span>
                </span>
              </template>
              <span v-else class="picker-empty">暂未选择任何标签</span>
            </div>
          </div>

          <!-- 搜索框 -->
          <el-input
            v-model="searchQuery"
            placeholder="搜索标签..."
            :prefix-icon="SearchIcon"
            clearable
            class="tag-search"
          />

          <!-- 分类标签组 -->
          <div class="picker-categories">
            <div v-for="group in filteredTagGroups" :key="group.category" class="tag-category">
              <div class="category-name">{{ group.category }}</div>
              <div class="tag-chip-list">
                <button
                  v-for="tag in group.tags"
                  :key="tag.id"
                  class="tag-chip"
                  :class="{ selected: isSelected(tag.id), disabled: isTagDisabled(tag.id) }"
                  :disabled="isTagDisabled(tag.id)"
                  @click="toggleTag(tag.id)"
                >
                  <span class="tag-chip-check" v-if="isSelected(tag.id)">&#10003;</span>
                  {{ tag.name }}
                </button>
              </div>
            </div>
            <el-empty
              v-if="filteredTagGroups.length === 0 && searchQuery"
              description="没有匹配的标签"
              :image-size="60"
            />
          </div>

          <!-- 底部操作栏 -->
          <div class="picker-actions">
            <span class="tag-count" :class="{ full: selectedTagIds.length >= 10 }">
              已选 {{ selectedTagIds.length }} / 10
            </span>
            <el-button @click="handleCancelPicker">取消</el-button>
            <el-button type="primary" :loading="savingTags" @click="handleSaveTags">保存标签</el-button>
          </div>
        </div>
      </Transition>
    </el-card>

    <!-- 匹配卡片：以下与原逻辑一致，未修改 -->
    <el-card class="status-card">
      <template #header>
        <div class="section-head">
          <div>
            <div class="title">随机匹配</div>
            <div class="desc">加入匹配后，可以和当前匹配对象分享"匹配对象可见"的日记。</div>
          </div>
          <div class="actions">
            <el-button @click="loadMatch">刷新状态</el-button>
            <el-button v-if="!isMatched && !isQueueing" type="primary" :loading="joining" @click="handleJoin">加入匹配</el-button>
            <el-popconfirm
              v-else
              :title="isMatched ? '确定解除当前匹配吗？' : '确定退出匹配队列吗？'"
              @confirm="handleCancel"
            >
              <template #reference>
                <el-button type="danger" :loading="canceling">{{ isMatched ? '解除匹配' : '退出队列' }}</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </template>

      <div v-if="isMatched" class="matched-box">
        <div class="profile-row">
          <el-avatar :size="84" :src="avatarUrl" class="avatar">
            <img :src="fallbackAvatar" alt="default-avatar" />
          </el-avatar>
          <div>
            <div class="nickname">{{ detail.matchedNickname || '未命名用户' }}</div>
            <div class="created">建立时间：{{ formatTime(detail.createdAt) }}</div>
          </div>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">匹配对象 ID</span>
            <span class="value">{{ detail.matchedUserId || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">共享说明</span>
            <span class="value small">对方可查看你设置为"匹配对象可见"的日记</span>
          </div>
        </div>
        <div v-if="detail.matchedUserTags && detail.matchedUserTags.length > 0" class="matched-tags">
          <span class="matched-tags-label">对方的兴趣标签</span>
          <div class="matched-tags-list">
            <span v-for="tag in detail.matchedUserTags" :key="tag" class="matched-tag-chip">{{ tag }}</span>
          </div>
        </div>
      </div>

      <div v-else-if="isQueueing" class="queue-box">
        <div class="queue-badge">正在匹配中</div>
        <div class="queue-title">你已经加入匹配队列</div>
        <div class="queue-desc">系统正在为你寻找合适的匹配对象。你可以保留当前页面，也可以稍后回来刷新查看结果。</div>
        <div class="queue-dots" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>

      <el-empty v-else description="当前还没有匹配对象，点击右上角加入匹配即可开始。" />
    </el-card>
  </div>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { matchApi, tagApi } from '@/api/modules';
import { formatTime, getAvatar } from '@/utils/common';
import { DEFAULT_AVATAR } from '@/utils/constants';

const SearchIcon = Search;
const CATEGORY_ORDER = ['影音娱乐', '运动户外', '生活休闲', '学习技能', '社交个性'];

const loading = ref(false);
const joining = ref(false);
const canceling = ref(false);
const savingTags = ref(false);
const allTags = ref([]);
const selectedTagIds = ref([]);
const pickerOpen = ref(false);
const searchQuery = ref('');
const detail = ref({
  inQueue: false,
  matched: false,
  matchedUserId: null,
  matchedNickname: null,
  avatarUrl: null,
  createdAt: null
});

const isMatched = computed(() => Boolean(detail.value?.matched));
const isQueueing = computed(() => Boolean(detail.value?.inQueue) && !isMatched.value);
const avatarUrl = computed(() => getAvatar(
  detail.value?.avatarUrl || detail.value?.matchedAvatarUrl || detail.value?.userAvatarUrl
));
const fallbackAvatar = DEFAULT_AVATAR;

const tagGroups = computed(() => {
  const grouped = {};
  for (const cat of CATEGORY_ORDER) {
    grouped[cat] = [];
  }
  for (const tag of allTags.value) {
    const cat = tag.category;
    if (grouped[cat]) {
      grouped[cat].push(tag);
    } else {
      grouped[cat] = [tag];
    }
  }
  return CATEGORY_ORDER.filter(cat => grouped[cat].length > 0).map(cat => ({
    category: cat,
    tags: grouped[cat]
  }));
});

const selectedTags = computed(() =>
  allTags.value.filter(t => selectedTagIds.value.includes(t.id))
);

const filteredTagGroups = computed(() => {
  const q = searchQuery.value.trim().toLowerCase();
  if (!q) return tagGroups.value;
  return tagGroups.value
    .map(group => ({
      category: group.category,
      tags: group.tags.filter(t => t.name.toLowerCase().includes(q))
    }))
    .filter(group => group.tags.length > 0);
});

function isSelected(id) {
  return selectedTagIds.value.includes(id);
}

function isTagDisabled(tagId) {
  return selectedTagIds.value.length >= 10 && !selectedTagIds.value.includes(tagId);
}

function toggleTag(tagId) {
  const idx = selectedTagIds.value.indexOf(tagId);
  if (idx >= 0) {
    selectedTagIds.value.splice(idx, 1);
  } else if (selectedTagIds.value.length >= 10) {
    ElMessage.warning('最多只能选择 10 个标签');
  } else {
    selectedTagIds.value.push(tagId);
  }
}

function removeTag(tagId) {
  const idx = selectedTagIds.value.indexOf(tagId);
  if (idx >= 0) selectedTagIds.value.splice(idx, 1);
}

async function loadTags() {
  try {
    const res = await tagApi.getTags();
    const data = res.data || {};
    allTags.value = data.tags || [];
    selectedTagIds.value = data.userTagIds || [];
  } catch {
    // 标签加载失败不影响匹配功能
  }
}

function handleCancelPicker() {
  pickerOpen.value = false;
  searchQuery.value = '';
  loadTags(); // 恢复已保存的选中状态
}

async function handleSaveTags() {
  savingTags.value = true;
  try {
    await tagApi.saveUserTags(selectedTagIds.value);
    ElMessage.success('标签保存成功');
    pickerOpen.value = false;
    searchQuery.value = '';
  } finally {
    savingTags.value = false;
  }
}

async function loadMatch() {
  loading.value = true;
  try {
    const res = await matchApi.getCurrent();
    const data = res.data || {};
    detail.value = {
      inQueue: false,
      matched: false,
      matchedUserId: null,
      matchedNickname: null,
      avatarUrl: null,
      createdAt: null,
      ...data,
      avatarUrl: data.avatarUrl || data.matchedAvatarUrl || data.userAvatarUrl || null
    };
  } finally {
    loading.value = false;
  }
}

async function handleJoin() {
  joining.value = true;
  try {
    await matchApi.join();
    ElMessage.success('已加入匹配队列');
    await loadMatch();
  } finally {
    joining.value = false;
  }
}

async function handleCancel() {
  canceling.value = true;
  try {
    if (isMatched.value) {
      await matchApi.cancel();
      ElMessage.success('解除匹配成功');
    } else if (isQueueing.value) {
      await matchApi.quitQueue();
      ElMessage.success('已退出匹配队列');
    }
    await loadMatch();
  } finally {
    canceling.value = false;
  }
}

onMounted(() => {
  loadTags();
  loadMatch();
});
</script>

<style scoped lang="scss">
.match-page {
  display: grid;
  gap: 20px;
}

.match-page .el-card {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  background: var(--bg-card);
}

/* ---- 通用 ---- */
.tag-count {
  font-size: 14px;
  font-weight: 600;
  color: var(--sage);
  white-space: nowrap;

  &.full {
    color: var(--clay);
  }
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.title {
  font-size: 22px;
  font-weight: 700;
  font-family: var(--font-display);
  color: var(--ink);
}

.desc {
  margin-top: 6px;
  color: var(--text-secondary);
}

/* ======== 折叠态横幅 ======== */
.tag-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.banner-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.banner-empty {
  color: var(--text-muted);
  font-size: 14px;
}

.banner-chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  background: var(--sage-light);
  color: var(--sage-deep);
  font-size: 13px;
  font-weight: 600;
  border: 1px solid transparent;
  line-height: 1.6;
}


.banner-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* ======== 展开态面板 ======== */
.tag-picker {
  display: grid;
  gap: 20px;
}

/* 已选标签区 */
.picker-selected {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #fdfcf8, #f8f5ee);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.picker-selected-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-muted);
  white-space: nowrap;
  padding-top: 4px;
}

.picker-selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.picker-empty {
  color: var(--text-muted);
  font-size: 13px;
}

.picker-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 14px;
  border-radius: var(--radius-pill);
  background: var(--sage-light);
  color: var(--sage-deep);
  font-size: 13px;
  font-weight: 600;
  border: 1px solid transparent;
  transition: all var(--transition-fast);
  line-height: 1.5;

  &--remove {
    cursor: pointer;

    &:hover {
      background: #fde8e4;
      color: var(--clay);
      border-color: var(--clay);
      transform: translateY(-1px);
    }
  }
}

.picker-chip-x {
  font-size: 15px;
  line-height: 1;
  opacity: 0.6;
}

/* 搜索框 */
.tag-search {
  :deep(.el-input__wrapper) {
    border-radius: var(--radius-pill) !important;
    background: var(--bg) !important;
  }
}

/* 分类列表 */
.picker-categories {
  display: grid;
  gap: 6px;
  max-height: 360px;
  overflow-y: auto;
  padding-right: 4px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--border);
    border-radius: 2px;
  }
}

.tag-category {
  padding: 4px 0;

  &:not(:last-child) {
    padding-bottom: 12px;
    border-bottom: 1px dashed var(--border-light);
  }
}

.category-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
  margin-bottom: 10px;
  padding-left: 2px;
  letter-spacing: 0.03em;
}

.tag-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 10px;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  border-radius: var(--radius-pill);
  border: 1.5px solid var(--border);
  background: var(--bg);
  color: var(--text);
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
  outline: none;
  line-height: 1.5;
  user-select: none;

  &:hover:not(.disabled) {
    border-color: var(--sage);
    background: var(--sage-light);
    color: var(--sage-deep);
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(122, 154, 126, 0.15);
  }

  &:active:not(.disabled) {
    transform: scale(0.97);
  }

  &.selected {
    background: var(--sage-light);
    border-color: var(--sage);
    color: var(--sage-deep);
    font-weight: 600;
    box-shadow: 0 1px 4px rgba(122, 154, 126, 0.18);

    &:hover {
      background: #dce8d8;
      border-color: var(--sage-deep);
    }
  }

  &.disabled {
    opacity: 0.38;
    cursor: not-allowed;
    filter: grayscale(0.3);
  }
}

.tag-chip-check {
  font-size: 11px;
  font-weight: 700;
  opacity: 0.8;
}

/* 底部操作栏 */
.picker-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 8px;
  border-top: 1px solid var(--border-light);
}

/* ---- 面板展开/收起动画 ---- */
.picker-slide-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.picker-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.picker-slide-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}

.picker-slide-leave-to {
  opacity: 0;
}

/* ======== 匹配卡片（保持不变） ======== */
.status-card .actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.matched-box {
  display: grid;
  gap: 18px;
}

.profile-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  border: 1px solid var(--border-light);
  flex-shrink: 0;
}

.nickname {
  font-size: 22px;
  font-weight: 700;
  font-family: var(--font-display);
  color: var(--ink);
}

.created {
  margin-top: 8px;
  color: var(--text-secondary);
}

.queue-box {
  padding: 28px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--sage-light), rgba(122, 154, 126, 0.06));
  border: 1px solid rgba(122, 154, 126, 0.22);
  text-align: center;
}

.queue-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: var(--sage);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.queue-title {
  margin-top: 18px;
  font-size: 26px;
  font-weight: 700;
  font-family: var(--font-display);
  color: var(--ink);
}

.queue-desc {
  max-width: 560px;
  margin: 12px auto 0;
  color: var(--text-secondary);
  line-height: 1.8;
}

.queue-dots {
  margin-top: 18px;
  display: inline-flex;
  gap: 8px;

  span {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: var(--sage);
    animation: pulse 1.2s infinite ease-in-out;
  }

  span:nth-child(2) {
    animation-delay: 0.15s;
  }

  span:nth-child(3) {
    animation-delay: 0.3s;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.info-item {
  padding: 16px;
  background: var(--bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);

  .label {
    display: block;
    color: var(--text-secondary);
    margin-bottom: 8px;
  }

  .value {
    font-size: 18px;
    font-weight: 700;
    word-break: break-all;
    color: var(--ink);

    &.small {
      font-size: 15px;
      font-weight: 500;
      line-height: 1.8;
    }
  }
}

.matched-tags {
  padding-top: 4px;
}

.matched-tags-label {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 600;
  margin-bottom: 10px;
}

.matched-tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.matched-tag-chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  background: var(--amber-light);
  color: var(--amber-deep);
  font-size: 13px;
  font-weight: 600;
  border: 1px solid rgba(196, 149, 74, 0.2);
}

@keyframes pulse {
  0%, 80%, 100% {
    transform: scale(0.75);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@media (max-width: 720px) {
  .section-head {
    flex-direction: column;
  }

  .tag-banner {
    flex-direction: column;
    align-items: flex-start;
  }

  .banner-actions {
    align-self: flex-end;
  }

  .profile-row,
  .info-grid {
    grid-template-columns: 1fr;
  }

  .profile-row {
    align-items: flex-start;
  }

  .queue-title {
    font-size: 22px;
  }
}
</style>
