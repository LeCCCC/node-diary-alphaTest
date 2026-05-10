<template>
  <div class="match-page" v-loading="loading">
    <el-card class="status-card">
      <template #header>
        <div class="section-head">
          <div>
            <div class="title">随机匹配</div>
            <div class="desc">加入匹配后，可以和当前匹配对象分享“匹配对象可见”的日记。</div>
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
            <span class="value small">对方可查看你设置为“匹配对象可见”的日记</span>
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
import { ElMessage } from 'element-plus';
import { matchApi } from '@/api/modules';
import { formatTime, getAvatar } from '@/utils/common';
import { DEFAULT_AVATAR } from '@/utils/constants';

const loading = ref(false);
const joining = ref(false);
const canceling = ref(false);
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

onMounted(loadMatch);
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

.actions {
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
