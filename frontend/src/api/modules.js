import request from '@/utils/request';

async function tryRequests(tasks) {
  let lastError;
  for (const task of tasks) {
    try {
      return await task();
    } catch (error) {
      const status = error?.response?.status || error?.code;
      if (status === 404 || status === 405) {
        lastError = error;
        continue;
      }
      throw error;
    }
  }
  throw lastError;
}

export const authApi = {
  register(data) {
    return request.post('/user/register', data);
  },
  login(data) {
    return request.post('/user/login', data);
  },
  getMe() {
    return request.get('/user/me');
  },
  updateMe(data) {
    return tryRequests([
      () => request.put('/user/me', data, { silent: true }),
      () => request.put('/user/profile', data, { silent: true }),
      () => request.put('/user/info', data, { silent: true }),
      () => request.patch('/user/me', data, { silent: true })
    ]);
  },
  getCaptcha() {
    return request.get('/user/captcha');
  },
  changePassword(data) {
    return request.put('/user/password', data);
  },
  logout() {
    return request.post('/user/logout');
  }
};

export const uploadApi = {
  uploadImage(file) {
    const formData = new FormData();
    formData.append('file', file);
    return request.post('/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
};

export const diaryApi = {
  create(data) {
    return request.post('/diary', data);
  },
  update(id, data) {
    return request.put(`/diary/${id}`, data);
  },
  remove(id) {
    return request.delete(`/diary/${id}`);
  },
  getMyList(params) {
    return request.get('/diary/my-list', { params });
  },
  getHomeFeed(params) {
    return tryRequests([
      () => request.get('/diary/home-feed', { params, silent: true }),
      () => request.get('/diary/my-list', { params, silent: true })
    ]);
  },
  getDetail(id) {
    return request.get(`/diary/${id}`);
  }
};

export const matchApi = {
  join() {
    return request.post('/match/join');
  },
  getCurrent() {
    return request.get('/match/current');
  },
  quitQueue() {
    return request.delete('/match/queue');
  },
  cancel() {
    return request.delete('/match/current');
  }
};

export const tagApi = {
  getTags() {
    return request.get('/user/tags');
  },
  saveUserTags(tagIds) {
    return request.put('/user/tags', tagIds);
  }
};

export const treeHoleApi = {
  getList(params) {
    return request.get('/tree-holes', { params });
  },
  create(data) {
    return request.post('/tree-holes', data);
  },
  getDetail(id) {
    return request.get(`/tree-holes/${id}`);
  },
  update(id, data) {
    return request.put(`/tree-holes/${id}`, data);
  },
  remove(id) {
    return request.delete(`/tree-holes/${id}`);
  },
  getComments(id, params) {
    return request.get(`/tree-holes/${id}/comments`, { params });
  },
  createComment(id, data) {
    return request.post(`/tree-holes/${id}/comments`, data);
  }
};
