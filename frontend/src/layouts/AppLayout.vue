<template>
  <div class="app-shell">
    <aside class="sidebar" :class="{ open: menuOpen }">
      <div class="brand" @click="$router.push('/home')">
        <img :src="APP_LOGO" alt="logo" class="logo" />
        <div>
          <div class="name">结日记</div>
          <div class="desc">记录、分享与陪伴</div>
        </div>
      </div>

      <nav class="nav-list">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item) }"
          @click="menuOpen = false"
        >
          <span class="nav-icon" v-html="item.icon"></span>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="footer-rule"></div>
        <div class="footer-text">把今天好好收起来</div>
      </div>
    </aside>

    <div v-if="menuOpen" class="mobile-mask" @click="menuOpen = false"></div>

    <section class="main-panel">
      <header class="topbar">
        <div class="topbar-left">
          <button class="menu-btn" @click="menuOpen = !menuOpen" aria-label="菜单">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M3 5h14M3 10h14M3 15h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
          <div>
            <div class="title">{{ currentTitle }}</div>
            <div class="subtitle">{{ currentSubtitle }}</div>
          </div>
        </div>
        <div class="user-box">
          <div class="profile-entry" @click="router.push('/profile')">
            <img :src="avatarUrl" alt="avatar" class="avatar" />
            <span>{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout">退出</button>
        </div>
      </header>
      <main class="content-area">
        <router-view v-slot="{ Component, route }">
          <transition name="content-fade-up" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </main>
    </section>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { APP_LOGO, APP_NAME } from '@/utils/constants';
import { getAvatar } from '@/utils/common';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const menuOpen = ref(false);

const navItems = [
  { path: '/home', label: '主页', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>' },
  { path: '/match', label: '匹配', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg>' },
  { path: '/tree-holes', label: '树洞', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>' },
  { path: '/diaries', label: '我的日记', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>' },
  { path: '/diaries/create', label: '写日记', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>' },
  { path: '/profile', label: '我的资料', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>' },
];

const subtitleMap = {
  home: '看看最近写下的内容，也顺手开启今天的新记录。',
  match: '查看当前匹配状态，管理你与对方的共享陪伴。',
  'tree-holes': '浏览大家匿名发布的树洞，也可以写下你的心事。',
  'tree-hole-create': '把想说的话匿名写下来，发布到树洞广场。',
  'tree-hole-edit': '修改这条树洞内容，让表达更贴近你现在的心情。',
  'tree-hole-detail': '查看树洞全文与评论区，支持匿名评论互动。',
  diaries: '按时间整理你的日记，把珍贵的片段都收好。',
  'diary-create': '把今天的心情、故事和照片写成一篇完整日记。',
  'diary-edit': '继续润色这篇日记，让它更贴近你想留下的样子。',
  'diary-detail': '翻看这篇日记的完整内容、封面和更新时间。',
  profile: '设置头像和昵称，让你的个人主页更完整。'
};

function isActive(item) {
  if (item.path === '/tree-holes') return route.path.startsWith('/tree-holes');
  if (item.path === '/diaries/create') return route.path === '/diaries/create';
  if (item.path === '/diaries') return route.path.startsWith('/diaries') && route.path !== '/diaries/create';
  return route.path === item.path;
}

const currentTitle = computed(() => route.meta.title || APP_NAME);
const currentSubtitle = computed(() => subtitleMap[route.name] || '记录今天，珍藏每一天。');
const avatarUrl = computed(() => getAvatar(userStore.userInfo?.avatarUrl));

watch(() => route.fullPath, () => {
  menuOpen.value = false;
});

async function handleLogout() {
  await userStore.logout();
  ElMessage.success('已退出登录');
  router.replace('/login');
}
</script>

<style scoped lang="scss">
.app-shell {
  height: 100vh;
  height: 100dvh;
  display: flex;
  overflow: hidden;
}

/* ---- Sidebar ---- */
.sidebar {
  width: 244px;
  background: linear-gradient(180deg, #fffdf8 0%, #faf7f2 100%);
  border-right: 1px solid var(--border-light);
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  z-index: 30;
  box-shadow: 2px 0 20px rgba(44, 58, 79, 0.04);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 20px 24px;
  cursor: pointer;

  .logo {
    width: 42px;
    height: 42px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(44, 58, 79, 0.1);
  }

  .name {
    font-family: var(--font-display);
    font-size: 22px;
    font-weight: 700;
    color: var(--ink);
    letter-spacing: 0.04em;
  }

  .desc {
    color: var(--text-muted);
    font-size: 11px;
    margin-top: 2px;
    letter-spacing: 0.02em;
  }
}

.nav-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 12px;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 14px;
  border-radius: 12px;
  color: var(--text-secondary);
  font-size: 14.5px;
  font-weight: 500;
  text-decoration: none;
  transition: all var(--transition-fast);

  .nav-icon {
    display: flex;
    align-items: center;
    opacity: 0.6;
    transition: opacity var(--transition-fast);
  }

  &:hover {
    background: var(--sage-light);
    color: var(--ink);

    .nav-icon { opacity: 0.9; }
  }

  &.active {
    background: var(--sage-light);
    color: var(--sage-deep);
    font-weight: 700;

    .nav-icon { opacity: 1; }

    &::before {
      content: '';
      position: absolute;
      left: -12px;
      width: 3px;
      height: 20px;
      background: var(--sage-deep);
      border-radius: 0 3px 3px 0;
    }
  }
}

.sidebar-footer {
  padding: 20px 20px 8px;
}

.footer-rule {
  height: 1px;
  background: linear-gradient(90deg, var(--border), transparent);
  margin-bottom: 10px;
}

.footer-text {
  font-family: var(--font-display);
  font-size: 12px;
  color: var(--text-muted);
  font-style: italic;
  opacity: 0.7;
}

/* ---- Main Panel ---- */
.main-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

/* ---- Topbar ---- */
.topbar {
  background: rgba(255, 253, 248, 0.92);
  border-bottom: 1px solid var(--border-light);
  padding: 18px 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  z-index: 20;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);

  .title {
    font-family: var(--font-display);
    font-size: 20px;
    font-weight: 700;
    color: var(--ink);
    letter-spacing: 0.02em;
  }

  .subtitle {
    margin-top: 3px;
    color: var(--text-muted);
    font-size: 13px;
  }
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.menu-btn {
  display: none;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  background: var(--bg-card);
  color: var(--ink);
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;

  &:hover {
    background: var(--sage-light);
    border-color: var(--border);
  }
}

.content-area {
  padding: 24px 28px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.content-fade-up-enter-active,
.content-fade-up-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.content-fade-up-enter-from,
.content-fade-up-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

/* ---- User Box ---- */
.user-box {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.profile-entry {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 12px;
  border-radius: 999px;
  transition: background var(--transition-fast);

  &:hover {
    background: var(--sage-light);
  }

  span {
    font-size: 14px;
    font-weight: 500;
    color: var(--text);
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-light);
  flex-shrink: 0;
}

.logout-btn {
  padding: 7px 16px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--bg-card);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    background: #fdf1eb;
    border-color: var(--clay);
    color: var(--clay);
  }
}

/* ---- Mobile ---- */
.mobile-mask {
  display: none;
}

@media (max-width: 960px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    transform: translateX(-100%);
    transition: transform 0.28s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 4px 0 40px rgba(44, 58, 79, 0.18);

    &.open {
      transform: translateX(0);
    }
  }

  .mobile-mask {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(44, 58, 79, 0.3);
    z-index: 25;
    animation: mask-in 0.2s ease;
  }

  .menu-btn {
    display: flex;
  }

  .topbar,
  .content-area {
    padding-left: 18px;
    padding-right: 18px;
  }

  .topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-box {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 640px) {
  .brand .name {
    font-size: 20px;
  }

  .topbar .title {
    font-size: 18px;
  }
}

@keyframes mask-in {
  from { opacity: 0; }
  to   { opacity: 1; }
}
</style>
