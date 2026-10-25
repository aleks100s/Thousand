/** Shares the header's animation clock; no independent timers or render loop. */
export function createScoreTransfer(source: HTMLElement, result: HTMLElement) {
  let snapshot: { gameId: string; playerId: string; total: number; last: number; rect: DOMRect; font: string; color: string } | undefined;
  return {
    capture(gameId?: string, playerId?: string | null, total?: number) {
      if (!gameId || !playerId || total === undefined || total <= 0) return;
      const style = getComputedStyle(source);
      snapshot = { gameId, playerId, total, last: parseInt(source.textContent ?? '0', 10) || 0,
        rect: source.getBoundingClientRect(), font: style.font, color: style.color };
    },
    start(playerId: string, gameId: string) {
      const saved = snapshot;
      snapshot = undefined;
      if (!saved || saved.gameId !== gameId || saved.playerId !== playerId) return;
      const target = Array.from(document.querySelectorAll<HTMLElement>('[data-score-for]')).find(el => el.dataset.scoreFor === playerId);
      if (!target) return;
      // Keep the outgoing player's score visible, even when their header card was scrolled away.
      target.closest('.header-player')?.scrollIntoView({ block: 'nearest', inline: 'nearest', behavior: 'instant' });
      const destination = target.getBoundingClientRect();
      const text = document.createElement('div');
      text.className = 'score-transfer'; text.setAttribute('aria-hidden', 'true');
      Object.assign(text.style, { position: 'fixed', left: '0', top: '0', font: saved.font,
        color: saved.color, pointerEvents: 'none', zIndex: '20', whiteSpace: 'nowrap',
        textShadow: '0 2px 8px #0008', transformOrigin: 'center', willChange: 'transform, opacity' });
      document.body.append(text);
      const previousVisibility = result.style.visibility;
      result.style.visibility = 'hidden';
      let finished = false;
      const update = (progress: number) => {
        if (finished) return;
        if (progress >= 1) {
          finished = true; text.remove(); result.style.visibility = previousVisibility; return;
        }
        const sumProgress = Math.min(1, progress / .22);
        const points = Math.round((saved.last + (saved.total - saved.last) * sumProgress) / 5) * 5;
        text.textContent = `${points} очков`;
        const flight = Math.max(0, (progress - .18) / .82), eased = 1 - (1 - flight) ** 3;
        const x = saved.rect.x + saved.rect.width / 2 + (destination.x + destination.width / 2 - saved.rect.x - saved.rect.width / 2) * eased;
        const y = saved.rect.y + saved.rect.height / 2 + (destination.y + destination.height / 2 - saved.rect.y - saved.rect.height / 2) * eased;
        text.style.transform = `translate(${x - text.offsetWidth / 2}px, ${y - text.offsetHeight / 2}px) scale(${1 - .45 * eased})`;
        text.style.opacity = String(1 - Math.max(0, (progress - .78) / .22));
      };
      update(0);
      return update;
    },
  };
}
