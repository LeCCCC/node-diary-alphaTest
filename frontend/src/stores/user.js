import { defineStore } from 'pinia';
import { authApi } from '@/api/modules';
import { getToken, getUserCache, removeToken, removeUserCache, setToken, setUserCache } from '@/utils/auth';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getUserCache()
  }),
  getters: {
    isLogin: (state) => Boolean(state.token)
  },
  actions: {
    async login(form) {
      const res = await authApi.login(form);
      const { token, userInfo } = res.data || {};
      this.token = token || '';
      this.userInfo = userInfo || null;
      if (token) setToken(token);
      if (userInfo) setUserCache(userInfo);
      return res;
    },
    async fetchProfile() {
      if (!this.token) return null;
      const res = await authApi.getMe();
      this.userInfo = res.data;
      setUserCache(res.data);
      return res.data;
    },
    async logout() {
      try {
        if (this.token) await authApi.logout();
      } finally {
        this.clearLogin();
      }
    },
    clearLogin() {
      this.token = '';
      this.userInfo = null;
      removeToken();
      removeUserCache();
    }
  }
});
