import dayjs from 'dayjs';
import { DEFAULT_AVATAR } from './constants';

const baseUrl = import.meta.env.VITE_API_BASE_TARGET || window.location.origin;

export function normalizeDateValue(value) {
  if (!value && value !== 0) return null;

  if (Array.isArray(value)) {
    const [year, month = 1, day = 1, hour = 0, minute = 0, second = 0] = value;
    if (!year) return null;
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`;
  }

  if (typeof value === 'object') {
    const year = value.year ?? value.years;
    const month = value.monthValue ?? value.month ?? 1;
    const day = value.dayOfMonth ?? value.day ?? 1;
    const hour = value.hour ?? 0;
    const minute = value.minute ?? 0;
    const second = value.second ?? 0;
    if (year) {
      return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`;
    }
  }

  return value;
}

export function pickDate(...values) {
  for (const value of values) {
    const normalized = normalizeDateValue(value);
    if (normalized) return normalized;
  }
  return null;
}

export function formatTime(time, pattern = 'YYYY-MM-DD HH:mm:ss') {
  const normalized = normalizeDateValue(time);
  return normalized ? dayjs(normalized).format(pattern) : '-';
}

export function withBaseUrl(url) {
  if (!url) return '';
  if (/^https?:\/\//.test(url)) return url;
  return `${baseUrl}${url}`;
}

export function getAvatar(url) {
  return url ? withBaseUrl(url) : DEFAULT_AVATAR;
}

export function visibilityText(value) {
  if (value === null || typeof value === 'undefined' || value === '') return '仅自己可见';
  const normalized = String(value).trim().toLowerCase();
  if (['0', 'self', 'private', 'only_self', 'onlyself'].includes(normalized)) return '仅自己可见';
  return '匹配对象可见';
}

export function diaryVisibilityLabel(diary, currentUserId) {
  // 列表接口有 authorType 字段直接判断
  const authorType = String(diary?.authorType || '').toUpperCase();
  if (authorType === 'MATCHED' || authorType === 'MATCH' || authorType === 'OTHER') {
    return { label: '匹配对象日记', type: 'matched' };
  }
  // 详情接口无 authorType，通过 userId 与当前用户比对
  if (!authorType && currentUserId != null && diary?.userId != null) {
    if (String(diary.userId) !== String(currentUserId)) {
      return { label: '匹配对象日记', type: 'matched' };
    }
  }
  const v = diary?.visibility;
  if (v === 0 || v === '0' || v === null || typeof v === 'undefined' || v === '') {
    return { label: '仅自己可见', type: 'private' };
  }
  return { label: '匹配对象可见', type: 'shared' };
}

export function stripHtml(html = '') {
  return html.replace(/<[^>]+>/g, '').trim();
}

export function resolveField(source, fields = []) {
  if (!source) return null;
  for (const field of fields) {
    const value = source?.[field];
    if (value !== null && typeof value !== 'undefined' && value !== '') {
      return value;
    }
  }
  return null;
}

export function extractId(source, fields = ['id']) {
  return resolveField(source, fields);
}

export function getUserId(userInfo) {
  return resolveField(userInfo, ['id', 'userId', 'user_id', 'uid']);
}

export function getTextSummary(content = '', maxLength = 110) {
  const plain = stripHtml(String(content || '')).replace(/\s+/g, ' ').trim();
  if (!plain) return '';
  if (plain.length <= maxLength) return plain;
  return `${plain.slice(0, maxLength)}…`;
}

export function normalizeListData(data) {
  if (Array.isArray(data)) return data;
  if (!data || typeof data !== 'object') return [];
  return data.records || data.list || data.items || data.content || [];
}

async function nativeHeicToJpeg(file) {
  const url = URL.createObjectURL(file);
  return new Promise((resolve) => {
    const img = new Image();
    img.onload = () => {
      const canvas = document.createElement('canvas');
      canvas.width = img.naturalWidth;
      canvas.height = img.naturalHeight;
      canvas.getContext('2d').drawImage(img, 0, 0);
      canvas.toBlob((blob) => {
        URL.revokeObjectURL(url);
        if (!blob) { resolve(null); return; }
        const name = file.name.replace(/\.\w+$/i, '.jpg');
        resolve(new File([blob], name, { type: 'image/jpeg' }));
      }, 'image/jpeg', 0.92);
    };
    img.onerror = () => { URL.revokeObjectURL(url); resolve(null); };
    img.src = url;
  });
}

export async function ensureBrowserImage(file) {
  if (!file) return file;
  const isHeic = /\.(heic|heif)$/i.test(file.name) ||
    file.type === 'image/heic' || file.type === 'image/heif';
  if (!isHeic) return file;

  // 先尝试浏览器原生解码（Safari / macOS Chrome）
  const native = await nativeHeicToJpeg(file);
  if (native) return native;

  // 原生解码失败，用 heic2any 兜底（所有浏览器通用）
  try {
    const { default: heic2any } = await import('heic2any');
    const blob = await heic2any({ blob: file, toType: 'image/jpeg', quality: 0.92 });
    const result = Array.isArray(blob) ? blob[0] : blob;
    const name = file.name.replace(/\.\w+$/i, '.jpg');
    return new File([result], name, { type: 'image/jpeg' });
  } catch {
    return file;
  }
}

export function normalizeTotal(data, fallback = 0) {
  if (typeof data === 'number') return data;
  if (!data || typeof data !== 'object') return fallback;
  return Number(data.total ?? data.count ?? data.totalCount ?? fallback) || 0;
}
