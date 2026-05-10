import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { getToken, removeToken, removeUserCache } from './auth';

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
});

let redirecting = false;

function handleLoginExpired() {
  removeToken();
  removeUserCache();

  if (!redirecting && router.currentRoute.value.path !== '/login') {
    redirecting = true;
    ElMessage.error('登录已过期，请重新登录');
    router.replace('/login').finally(() => {
      redirecting = false;
    });
  }
}

service.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

service.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 如果不是你项目统一返回结构，直接返回原数据
    if (typeof res?.code === 'undefined' && typeof res?.message === 'undefined' && typeof res?.msg === 'undefined') {
      return res;
    }

    // 成功
    if (res.code === 200) {
      return res;
    }

    // 未登录 / token过期
    if (
      res.code === 401 ||
      res.code === '401' ||
      res.code === 'NOT_LOGIN' ||
      res.message === 'not_login' ||
      res.msg === 'not_login'
    ) {
      handleLoginExpired();
      return Promise.reject(res);
    }

    // 其他业务错误
    if (!response.config?.silent) {
      ElMessage.error(res.message || res.msg || '请求失败');
    }

    return Promise.reject(res);
  },
  (error) => {
    const status = error?.response?.status;
    const data = error?.response?.data || {};
    const message = data.message || data.msg;

    // HTTP层未登录
    if (
      status === 401 ||
      message === 'not_login'
    ) {
      handleLoginExpired();
      return Promise.reject(error);
    }

    if (!error?.config?.silent) {
      ElMessage.error(message || error.message || '网络异常');
    }

    return Promise.reject(error);
  }
);

export default service;