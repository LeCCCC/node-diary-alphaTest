import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { getToken, removeToken, removeUserCache } from '@/utils/auth';

function isTokenExpired(token) {
  if (!token) return true;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 < Date.now();
  } catch {
    return true;
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guestOnly: true, title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { guestOnly: true, title: '注册' }
    },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '/home',
          name: 'home',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '主页' }
        },
        {
          path: '/match',
          name: 'match',
          component: () => import('@/views/match/MatchView.vue'),
          meta: { title: '匹配' }
        },
        {
          path: '/tree-holes',
          name: 'tree-holes',
          component: () => import('@/views/treeHole/TreeHoleListView.vue'),
          meta: { title: '树洞' }
        },
        {
          path: '/tree-holes/create',
          name: 'tree-hole-create',
          component: () => import('@/views/treeHole/TreeHoleFormView.vue'),
          meta: { title: '发布树洞' }
        },
        {
          path: '/tree-holes/:id/edit',
          name: 'tree-hole-edit',
          component: () => import('@/views/treeHole/TreeHoleFormView.vue'),
          meta: { title: '编辑树洞' }
        },
        {
          path: '/tree-holes/:id',
          name: 'tree-hole-detail',
          component: () => import('@/views/treeHole/TreeHoleDetailView.vue'),
          meta: { title: '树洞详情' }
        },
        {
          path: '/profile',
          name: 'profile',
          component: () => import('@/views/user/ProfileView.vue'),
          meta: { title: '我的资料' }
        },
        {
          path: '/diaries',
          name: 'diaries',
          component: () => import('@/views/diary/DiaryListView.vue'),
          meta: { title: '我的日记' }
        },
        {
          path: '/diaries/create',
          name: 'diary-create',
          component: () => import('@/views/diary/DiaryFormView.vue'),
          meta: { title: '写日记' }
        },
        {
          path: '/diaries/:id/edit',
          name: 'diary-edit',
          component: () => import('@/views/diary/DiaryFormView.vue'),
          meta: { title: '编辑日记' }
        },
        {
          path: '/diaries/:id',
          name: 'diary-detail',
          component: () => import('@/views/diary/DiaryDetailView.vue'),
          meta: { title: '日记详情' }
        }
      ]
    }
  ]
});

router.beforeEach(async (to) => {
  const userStore = useUserStore();
  let token = getToken();

  // 清理过期 token，防止残留 JWT 导致 guestOnly 页面被误判为已登录
  if (token && isTokenExpired(token)) {
    userStore.clearLogin();
    token = '';
  }

  if (to.meta.title) {
    document.title = `${to.meta.title} - 结日记`;
  }

  // 需要登录，但本地没有有效 token
  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录');
    return '/login';
  }

  // 已登录状态访问登录/注册页，跳回首页
  if (to.meta.guestOnly && token) {
    return '/home';
  }

  // 有 token，但用户信息还没加载，尝试拉取
  if (token && !userStore.userInfo && to.meta.requiresAuth) {
    try {
      await userStore.fetchProfile();
    } catch (error) {
      userStore.clearLogin();
      return '/login';
    }
  }

  return true;
});

export default router;