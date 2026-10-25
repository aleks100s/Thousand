export type SceneMessage = { id: string; text: string; kind: 'notice' | 'reply' | 'reaction'; durationMs: number };
type NotificationEvent = { type: 'messageDismissed'; messageId: string };
const drawings: Record<string, string> = {
  notice: '<path d="M12 3 22 12 12 21 2 12Z"/><path d="M12 8v5m0 3v.1"/>',
  reply: '<rect x="4" y="7" width="16" height="13" rx="4"/><path d="M12 3v4M1 11v5m22-5v5m-15-1h.1m7.9 0h.1M9 17h6"/>',
  reaction: '<path d="M20 11c0 5-4 9-9 9l-7 2 2-6a9 9 0 1 1 14-5Z"/><path d="M8 13q4 4 8 0M8 9h.1m7.9 0h.1"/>',
  advice: '<path d="M8 17c0-3-3-4-3-8a7 7 0 0 1 14 0c0 4-3 5-3 8ZM9 21h6m-3-9v5"/>',
};
function icon(kind: string) {
  const badge = document.createElement('span'); badge.className = 'bubble-icon'; badge.setAttribute('aria-hidden', 'true');
  badge.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${drawings[kind] ?? drawings.notice}</svg>`;
  return badge;
}
export function createNotifications(emit: (event: NotificationEvent) => void) {
  const root = document.createElement('aside'); root.id = 'game-notifications'; root.setAttribute('aria-label', 'Игровые сообщения');
  const stack = document.createElement('div'); stack.className = 'toast-stack'; root.append(stack);
  const advice = document.createElement('div'); advice.className = 'game-advice game-bubble'; advice.dataset.kind = 'advice'; advice.hidden = true;
  const adviceText = document.createElement('div'); adviceText.className = 'bubble-copy'; advice.append(icon('advice'), adviceText); root.append(advice); document.body.append(root);
  type Entry = { node: HTMLElement; timer: ReturnType<typeof setTimeout> | null; remaining: number; started: number; closing: boolean };
  const entries = new Map<string, Entry>(), seen = new Set<string>();
  let active = true;
  function drop(id: string) {
    const entry = entries.get(id); if (!entry) return;
    if (entry.timer) clearTimeout(entry.timer);
    entry.node.remove(); entries.delete(id);
  }
  function dismiss(id: string) {
    const entry = entries.get(id); if (!entry || entry.closing) return;
    entry.closing = true;
    if (entry.timer) clearTimeout(entry.timer);
    entry.node.classList.add('bubble-leaving');
    entry.timer = setTimeout(() => { drop(id); emit({ type: 'messageDismissed', messageId: id }); }, 180);
  }
  function schedule(id: string, entry: Entry) {
    if (!active || entry.closing || entry.timer) return;
    entry.started = performance.now(); entry.timer = setTimeout(() => dismiss(id), Math.max(0, entry.remaining));
  }
  return {
    update(messages: SceneMessage[], adviceContent: string | null, isActive: boolean, bottom: number) {
      active = isActive;
      const header = document.querySelector<HTMLElement>('#game-header');
      const headerHeight = header && document.body.classList.contains('has-header') ? parseFloat(getComputedStyle(header).height) : 0;
      const footer = Math.max(12, innerHeight * (1 - bottom));
      root.style.setProperty('--notifications-top', `${headerHeight + 12}px`);
      root.style.setProperty('--notifications-bottom', `${footer}px`);
      advice.hidden = !adviceContent; adviceText.textContent = adviceContent ?? '';
      const available = new Set(messages.map(m => m.id));
      for (const id of entries.keys()) if (!available.has(id)) drop(id);
      for (const message of messages) {
        if (seen.has(message.id)) continue;
        seen.add(message.id);
        const node = document.createElement('div'); node.className = 'game-toast game-bubble'; node.dataset.kind = message.kind; node.dataset.messageId = message.id;
        node.setAttribute('role', 'status'); node.setAttribute('aria-live', 'polite'); node.setAttribute('aria-atomic', 'true');
        const text = document.createElement('div'); text.className = 'bubble-copy'; text.textContent = message.text;
        const close = document.createElement('button'); close.className = 'bubble-close'; close.type = 'button'; close.textContent = '×'; close.setAttribute('aria-label', 'Закрыть сообщение'); close.onclick = () => dismiss(message.id);
        node.append(icon(message.kind), text, close); stack.append(node);
        entries.set(message.id, { node, timer: null, remaining: Math.max(0, message.durationMs - 180), started: 0, closing: false });
      }
      for (const [id, entry] of entries) {
        if (!active && entry.timer && !entry.closing) { clearTimeout(entry.timer); entry.timer = null; entry.remaining -= performance.now() - entry.started; }
        schedule(id, entry);
      }
    },
  };
}
