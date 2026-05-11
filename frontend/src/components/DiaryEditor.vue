<template>
  <div class="diary-editor">
    <div class="paper">
      <div :id="toolbarId" class="editor-toolbar">
        <div class="tb-group">
          <button type="button" class="tb-plain" title="撤销" @click="exec('undo')">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 7v6h6" />
              <path d="M21 17a9 9 0 0 0-15-6.7L3 13" />
            </svg>
          </button>
          <button type="button" class="tb-plain" title="重做" @click="exec('redo')">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 7v6h-6" />
              <path d="M3 17a9 9 0 0 1 15-6.7l3 2.7" />
            </svg>
          </button>
        </div>

        <span class="tb-sep"></span>

        <div class="tb-group">
          <button type="button" class="ql-header" value="1" title="一级标题"></button>
          <button type="button" class="ql-header" value="2" title="二级标题"></button>
        </div>

        <span class="tb-sep"></span>

        <div class="tb-group">
          <button type="button" class="ql-bold" title="加粗"></button>
          <button type="button" class="ql-italic" title="斜体"></button>
          <button type="button" class="ql-underline" title="下划线"></button>
          <button type="button" class="ql-strike" title="删除线"></button>
        </div>

        <span class="tb-sep"></span>

        <div class="tb-group">
          <button type="button" class="ql-list" value="bullet" title="无序列表"></button>
          <button type="button" class="ql-list" value="ordered" title="有序列表"></button>
          <button type="button" class="ql-list" value="check" title="待办清单"></button>
          <button type="button" class="ql-blockquote" title="引用"></button>
        </div>

        <span class="tb-sep"></span>

        <div class="tb-group">
          <button type="button" class="ql-align" value="" title="左对齐"></button>
          <button type="button" class="ql-align" value="center" title="居中"></button>
          <button type="button" class="ql-align" value="right" title="右对齐"></button>
        </div>

        <span class="tb-sep"></span>

        <div class="tb-group">
          <button type="button" class="tb-plain" title="分割线" @click="insertDivider">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 12h16" />
              <path d="M6 6h12" opacity="0.4" />
              <path d="M6 18h12" opacity="0.4" />
            </svg>
          </button>
          <button type="button" class="tb-plain" title="插入图片" @click="triggerImage">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="16" rx="2" />
              <circle cx="9" cy="10" r="1.5" />
              <path d="m4 17 5-5 4 4 3-3 4 4" />
            </svg>
          </button>
          <button type="button" class="tb-plain" title="清除格式" @click="exec('clean')">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 4 8 20" />
              <path d="M19 4 11 20" opacity="0.45" />
              <path d="M5 20h14" />
            </svg>
          </button>
        </div>
      </div>

      <div class="paper-inner">
        <QuillEditor
          ref="editorRef"
          v-model:content="innerContent"
          content-type="html"
          theme="snow"
          :toolbar="toolbarSelector"
          :placeholder="placeholder"
          class="quill-host"
          @text-change="onTextChange"
          @ready="onReady"
        />
      </div>

      <div class="paper-footer">
        <div class="foot-left">
          <span class="date-chip">{{ todayLabel }}</span>
          <span v-if="statusText" class="status-dot" :class="statusClass">
            <i></i>{{ statusText }}
          </span>
        </div>
        <div class="foot-right">
          <span class="count-num">{{ wordCount }}</span>
          <span class="count-label">字</span>
        </div>
      </div>
    </div>

    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      hidden
      @change="onFileChange"
    />
  </div>
</template>

<script setup>
import { QuillEditor } from '@vueup/vue-quill';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '从这里开始写今天的故事……' },
  uploadImage: { type: Function, default: null },
  savedAt: { type: [String, Number, Date], default: null }
});

const emit = defineEmits(['update:modelValue']);

const editorRef = ref();
const fileInputRef = ref();
const innerContent = ref(props.modelValue || '');
const wordCount = ref(0);
const isTyping = ref(false);
let typingTimer = null;

const toolbarId = `diary-editor-toolbar-${Math.random().toString(36).slice(2, 8)}`;
const toolbarSelector = computed(() => `#${toolbarId}`);

const todayLabel = computed(() => dayjs().format('YYYY 年 M 月 D 日 · dddd'));

const statusText = computed(() => {
  if (isTyping.value) return '正在书写…';
  if (props.savedAt) return `已保存 · ${dayjs(props.savedAt).format('HH:mm')}`;
  return '';
});
const statusClass = computed(() => (isTyping.value ? 'is-typing' : 'is-saved'));

watch(
  () => props.modelValue,
  (val) => {
    if (val !== innerContent.value) {
      innerContent.value = val || '';
      nextTick(() => updateWordCount());
    }
  }
);

watch(innerContent, (val) => {
  emit('update:modelValue', val);
});

function getQuill() {
  return editorRef.value?.getQuill?.();
}

function exec(name) {
  const quill = getQuill();
  if (!quill) return;
  if (name === 'undo') return quill.history.undo();
  if (name === 'redo') return quill.history.redo();
  if (name === 'clean') {
    const range = quill.getSelection(true);
    if (range && range.length) {
      quill.removeFormat(range.index, range.length);
    } else {
      quill.removeFormat(0, quill.getLength());
    }
  }
}

function insertDivider() {
  const quill = getQuill();
  if (!quill) return;
  const range = quill.getSelection(true) || { index: quill.getLength(), length: 0 };
  quill.insertText(range.index, '\n', 'user');
  quill.insertEmbed(range.index + 1, 'divider', true, 'user');
  quill.insertText(range.index + 2, '\n', 'user');
  quill.setSelection(range.index + 3, 0, 'silent');
}

function triggerImage() {
  fileInputRef.value?.click();
}

async function onFileChange(e) {
  const file = e.target.files?.[0];
  e.target.value = '';
  if (!file) return;
  if (!props.uploadImage) {
    ElMessage.warning('未配置图片上传');
    return;
  }
  try {
    const url = await props.uploadImage(file);
    if (!url) return;
    const quill = getQuill();
    if (!quill) return;
    let pos = quill.getSelection()?.index ?? quill.getLength();
    // 插入图片前先换行，避免与文字行重叠
    if (pos > 0) {
      const [line, offset] = quill.getLine(pos);
      if (offset > 0) {
        quill.insertText(pos, '\n', 'user');
        pos += 1;
      }
    }
    quill.insertEmbed(pos, 'image', url, 'user');
    quill.insertText(pos + 1, '\n', 'user');
    quill.setSelection(pos + 2, 0, 'silent');
    nextTick(() => normalizeEditorImages());
  } catch (err) {
    ElMessage.error('图片上传失败');
  }
}

function onTextChange() {
  normalizeEditorImages();
  updateWordCount();
  isTyping.value = true;
  clearTimeout(typingTimer);
  typingTimer = setTimeout(() => { isTyping.value = false; }, 900);
}

function updateWordCount() {
  const quill = getQuill();
  if (!quill) { wordCount.value = 0; return; }
  const text = quill.getText().replace(/\s+/g, '');
  wordCount.value = text.length;
}

function normalizeEditorImages() {
  const quill = getQuill();
  const root = quill?.root;
  if (!root) return;

  // 先清理所有不含 img 的元素上的 diary-image-line，避免删除图片后残留占位
  root.querySelectorAll('.diary-image-line').forEach((el) => {
    if (!el.querySelector('img')) {
      el.classList.remove('diary-image-line');
    }
  });

  root.querySelectorAll('img').forEach((img) => {
    img.classList.add('diary-inline-image');

    const holder = img.closest('p, div, li');
    if (holder) {
      holder.classList.add('diary-image-line');
    }
  });
}

function registerDividerBlot(Quill) {
  if (!Quill || Quill.__diary_divider_registered) return;
  const BlockEmbed = Quill.import('blots/block/embed');
  class DividerBlot extends BlockEmbed {}
  DividerBlot.blotName = 'divider';
  DividerBlot.tagName = 'hr';
  DividerBlot.className = 'diary-divider';
  Quill.register(DividerBlot, true);
  Quill.__diary_divider_registered = true;
}

function onReady(quill) {
  const Quill = quill?.constructor;
  registerDividerBlot(Quill);
  nextTick(() => {
    normalizeEditorImages();
    updateWordCount();
  });
}

onBeforeUnmount(() => clearTimeout(typingTimer));
</script>

<style scoped lang="scss">
.diary-editor {
  width: 100%;
}

/* ---- Paper card ---- */
.paper {
  position: relative;
  background: #fffdf6;
  border: 0;
  border-radius: 16px;
  box-shadow:
    0 1px 3px rgba(44, 58, 79, 0.03),
    0 12px 38px -18px rgba(44, 58, 79, 0.15);
  overflow: hidden;
  isolation: isolate;
  transition: box-shadow var(--transition-base), transform var(--transition-base);
}

/* 用独立覆盖层画边框，避免 toolbar / Quill 子元素背景在圆角处把边框盖断 */
.paper::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 20;
  pointer-events: none;
  border: 1px solid var(--border-light);
  border-radius: inherit;
}

.paper::after {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  border-radius: inherit;
  background-image: url("data:image/svg+xml,%3Csvg width='240' height='240' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.03'/%3E%3C/svg%3E");
  opacity: 0.55;
}

.paper:focus-within {
  box-shadow:
    0 0 0 4px rgba(122, 154, 126, 0.1),
    0 20px 56px -20px rgba(44, 58, 79, 0.22);
}

.paper:focus-within::before {
  border-color: rgba(122, 154, 126, 0.7);
}

/* ---- Toolbar ---- */
.editor-toolbar {
  position: sticky;
  top: 0;
  z-index: 3;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 2px;
  padding: 10px 18px;
  background: linear-gradient(180deg, #fffdf8 0%, #fbf6ec 100%);
  border-bottom: 1px solid var(--border-light);
  border-radius: 16px 16px 0 0;
  backdrop-filter: saturate(1.1);
}

.tb-group {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px;
  border-radius: 10px;
  background: rgba(255, 253, 248, 0.4);
}

.tb-sep {
  width: 1px;
  height: 18px;
  margin: 0 6px;
  background: var(--border);
  opacity: 0.7;
}

/* Quill buttons in our toolbar — reset + restyle */
.editor-toolbar :deep(.ql-formats) { margin: 0 !important; }

.editor-toolbar button,
.editor-toolbar :deep(button.ql-bold),
.editor-toolbar :deep(button.ql-italic),
.editor-toolbar :deep(button.ql-underline),
.editor-toolbar :deep(button.ql-strike),
.editor-toolbar :deep(button.ql-blockquote),
.editor-toolbar :deep(button.ql-list),
.editor-toolbar :deep(button.ql-header),
.editor-toolbar :deep(button.ql-align) {
  width: 30px;
  height: 30px;
  padding: 0;
  border: 0;
  background: transparent;
  border-radius: 8px;
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast),
    transform var(--transition-fast);
}

.editor-toolbar button:hover,
.editor-toolbar :deep(button.ql-bold):hover,
.editor-toolbar :deep(button.ql-italic):hover,
.editor-toolbar :deep(button.ql-underline):hover,
.editor-toolbar :deep(button.ql-strike):hover,
.editor-toolbar :deep(button.ql-blockquote):hover,
.editor-toolbar :deep(button.ql-list):hover,
.editor-toolbar :deep(button.ql-header):hover,
.editor-toolbar :deep(button.ql-align):hover {
  background: rgba(122, 154, 126, 0.12);
  color: var(--ink);
}

.editor-toolbar button:active {
  transform: scale(0.94);
}

.editor-toolbar :deep(button.ql-active),
.editor-toolbar :deep(button.ql-active .ql-stroke) {
  color: var(--sage-deep) !important;
  background: var(--sage-light);
}

.editor-toolbar :deep(.ql-stroke) { stroke: currentColor; }
.editor-toolbar :deep(.ql-fill)   { fill: currentColor; }
.editor-toolbar :deep(.ql-picker) { color: var(--text-secondary); }

/* ---- Paper inner (Quill host) ---- */
.paper-inner {
  position: relative;
  z-index: 1;
  --diary-line-height: 32px;
  --diary-line-color: rgba(120, 95, 70, 0.34);
  --diary-underline-offset: 0.32em;
}

.quill-host :deep(.ql-toolbar) { display: none !important; }

.quill-host :deep(.ql-container) {
  border: 0 !important;
  background: transparent;
  font-family: var(--font-body);
  height: auto !important;
  min-height: 420px;
}

.quill-host :deep(.ql-editor) {
  height: auto !important;
  min-height: 420px;
  padding: 24px 32px;
  font-size: 16px;
  line-height: var(--diary-line-height);
  color: var(--text);
  letter-spacing: 0.01em;
  caret-color: var(--sage-deep);
  background-color: #fffdf7;
  background-image: none !important;
  box-sizing: border-box;
}

/* 使用背景渐变在每行渲染整行横线，替代 text-decoration。
   text-decoration 的线只覆盖文字宽度，而背景渐变的线横跨整行。 */
.quill-host :deep(.ql-editor p),
.quill-host :deep(.ql-editor li),
.paper-inner :deep(.ql-editor p),
.paper-inner :deep(.ql-editor li) {
  margin: 0;
  padding: 0;
  line-height: var(--diary-line-height);
  min-height: var(--diary-line-height);
}

.quill-host :deep(.ql-editor p:not(.diary-image-line)),
.quill-host :deep(.ql-editor li),
.paper-inner :deep(.ql-editor p:not(.diary-image-line)),
.paper-inner :deep(.ql-editor li) {
  background-image: repeating-linear-gradient(
    to bottom,
    transparent,
    transparent calc(var(--diary-line-height) - 1px),
    var(--diary-line-color) calc(var(--diary-line-height) - 1px),
    var(--diary-line-color) var(--diary-line-height)
  );
  background-size: 100% var(--diary-line-height);
  background-position: 0 -6px;
}


.paper-inner :deep(.ql-editor) {
  background-color: #fffdf7;
  background-image: none !important;
}

.quill-host :deep(.ql-editor.ql-blank::before) {
  left: 32px;
  right: 32px;
  top: 24px;
  font-style: normal;
  font-family: var(--font-display);
  font-size: 16px;
  color: var(--text-muted);
  opacity: 0.6;
  letter-spacing: 0.03em;
  line-height: 32px;
}

.quill-host :deep(.ql-editor h1),
.quill-host :deep(.ql-editor h2) {
  font-family: var(--font-display);
  color: var(--ink);
  letter-spacing: 0.02em;
  margin: 0;
  padding: 0;
}
.quill-host :deep(.ql-editor h1) {
  font-size: 26px;
  line-height: 64px;
  min-height: 64px;
}
.quill-host :deep(.ql-editor h2) {
  font-size: 22px;
  line-height: 32px;
  min-height: 32px;
}

/* Quill 的“下划线”格式和纸张辅助线区分开：格式下划线更深更粗 */
.quill-host :deep(.ql-editor u),
.paper-inner :deep(.ql-editor u) {
  text-decoration-color: var(--sage-deep) !important;
  text-decoration-thickness: 2px !important;
  text-underline-offset: 0.28em !important;
}

.quill-host :deep(.ql-editor blockquote) {
  position: relative;
  margin: 0;
  padding: 0 18px 0 22px;
  border-left: 3px solid var(--sage);
  background: linear-gradient(90deg, rgba(122, 154, 126, 0.08), transparent 70%);
  color: var(--text-secondary);
  border-radius: 0 10px 10px 0;
  font-style: italic;
  line-height: 32px;
  min-height: 32px;
}

.quill-host :deep(.ql-editor hr.diary-divider) {
  border: 0;
  height: 32px;
  margin: 0 auto;
  background-image:
    radial-gradient(circle, var(--amber) 1px, transparent 1.2px),
    radial-gradient(circle, var(--amber) 1px, transparent 1.2px),
    radial-gradient(circle, var(--amber) 1px, transparent 1.2px);
  background-size: 8px 16px;
  background-position: calc(50% - 20px) 50%, 50% 50%, calc(50% + 20px) 50%;
  background-repeat: no-repeat;
  opacity: 0.55;
}

.quill-host :deep(.ql-editor ul[data-checked=true] > li::before),
.quill-host :deep(.ql-editor ul[data-checked=false] > li::before) {
  color: var(--sage-deep);
}

/* 图片段落不参与文字横线；让图片自然成为日记插图块 */
.quill-host :deep(.ql-editor .diary-image-line),
.quill-host :deep(.ql-editor p:has(> img)),
.paper-inner :deep(.ql-editor .diary-image-line),
.paper-inner :deep(.ql-editor p:has(> img)) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  margin: 12px 0 !important;
  padding: 8px 0 !important;
  line-height: 0 !important;
  min-height: 0 !important;
  box-sizing: border-box !important;
  text-decoration: none !important;
}

/* 图片按自身比例显示，不再用 object-fit: contain 撑满固定框，
   否则画幅和 520x320 不一致时四周会留空，配合 border-radius 看起来像一圈透明边框 */
.paper-inner :deep(.ql-editor img),
.paper-inner :deep(.ql-editor p img),
.paper-inner :deep(.ql-editor .diary-inline-image),
.quill-host :deep(.ql-editor img),
.quill-host :deep(.ql-editor p img),
.quill-host :deep(.ql-editor .diary-inline-image) {
  display: block !important;
  width: auto !important;
  height: auto !important;
  max-width: min(100%, 520px) !important;
  max-height: 320px !important;
  object-fit: unset !important;
  margin: 0 auto !important;
  border-radius: 12px !important;
  box-shadow: 0 8px 24px -8px rgba(44, 58, 79, 0.18) !important;
}

/* ---- Footer ---- */
.paper-footer {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  border-top: 1px dashed var(--border);
  background: linear-gradient(180deg, transparent, rgba(245, 239, 228, 0.5));
  font-size: 13px;
  color: var(--text-muted);
}

.foot-left {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.date-chip {
  font-family: var(--font-display);
  font-size: 14px;
  letter-spacing: 0.04em;
  color: var(--ink-light);
}

.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
}

.status-dot i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--text-muted);
  display: inline-block;
}

.status-dot.is-typing i {
  background: var(--amber);
  animation: blink 1s ease-in-out infinite;
}

.status-dot.is-saved i {
  background: var(--sage);
}

.foot-right {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  font-family: var(--font-display);
}

.count-num {
  font-size: 22px;
  color: var(--ink);
  letter-spacing: 0.04em;
  font-variant-numeric: tabular-nums;
}

.count-label {
  font-size: 12px;
  color: var(--text-muted);
  letter-spacing: 0.1em;
}

@keyframes blink {
  0%, 100% { opacity: 0.3; }
  50%      { opacity: 1; }
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .editor-toolbar {
    padding: 8px 10px;
    gap: 2px;
  }

  .tb-sep { margin: 0 2px; }

  .quill-host :deep(.ql-container) {
    min-height: 360px;
  }

  .paper-inner {
    --diary-line-height: 30px;
    --diary-line-color: rgba(120, 95, 70, 0.32);
    --diary-underline-offset: 0.34em;
  }

  .quill-host :deep(.ql-editor) {
    min-height: 360px;
    padding: 20px 18px;
    font-size: 15px;
    line-height: var(--diary-line-height);
    background-image: none !important;
  }

  .quill-host :deep(.ql-editor p),
  .quill-host :deep(.ql-editor li),
  .paper-inner :deep(.ql-editor p),
  .paper-inner :deep(.ql-editor li) {
    line-height: var(--diary-line-height);
    min-height: var(--diary-line-height);
  }

  .quill-host :deep(.ql-editor p:not(.diary-image-line)),
  .quill-host :deep(.ql-editor li),
  .paper-inner :deep(.ql-editor p:not(.diary-image-line)),
  .paper-inner :deep(.ql-editor li) {
    background-position: 0 -5px;
  }

  .paper-inner :deep(.ql-editor) {
    background-image: none !important;
  }

  .quill-host :deep(.ql-editor.ql-blank::before) {
    left: 18px;
    right: 18px;
    top: 20px;
    font-size: 15px;
    line-height: 30px;
  }

  .quill-host :deep(.ql-editor .diary-image-line),
  .quill-host :deep(.ql-editor p:has(> img)),
  .paper-inner :deep(.ql-editor .diary-image-line),
  .paper-inner :deep(.ql-editor p:has(> img)) {
    margin: 10px 0 !important;
    padding: 6px 0 !important;
    min-height: 0 !important;
  }

  .paper-inner :deep(.ql-editor img),
  .paper-inner :deep(.ql-editor p img),
  .paper-inner :deep(.ql-editor .diary-inline-image),
  .quill-host :deep(.ql-editor img),
  .quill-host :deep(.ql-editor p img),
  .quill-host :deep(.ql-editor .diary-inline-image) {
    width: auto !important;
    height: auto !important;
    max-width: 100% !important;
    max-height: 270px !important;
  }

  .quill-host :deep(.ql-editor h1) {
    line-height: 60px;
    min-height: 60px;
  }
  .quill-host :deep(.ql-editor h2) {
    line-height: 30px;
    min-height: 30px;
  }

  .quill-host :deep(.ql-editor blockquote) {
    line-height: 30px;
    min-height: 30px;
  }

  .quill-host :deep(.ql-editor hr.diary-divider) {
    height: 30px;
  }

  .paper-footer {
    padding: 10px 16px;
  }
}
</style>



