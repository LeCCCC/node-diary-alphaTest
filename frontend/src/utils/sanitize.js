import DOMPurify from 'dompurify';

const DEFAULT_SANITIZE_OPTIONS = {
  USE_PROFILES: { html: true },
  FORBID_TAGS: ['script', 'style', 'iframe', 'object', 'embed', 'form'],
  FORBID_ATTR: ['onerror', 'onload', 'onclick', 'onmouseover', 'onfocus'],
  ALLOW_UNKNOWN_PROTOCOLS: false
};

export function sanitizeHtml(html = '', options = {}) {
  const dirty = String(html || '');
  return DOMPurify.sanitize(dirty, {
    ...DEFAULT_SANITIZE_OPTIONS,
    ...options
  });
}
