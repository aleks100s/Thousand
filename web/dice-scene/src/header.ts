export type HeaderPlayer = {
  id: string; name: string; score: number; bot: boolean; winner: boolean; bolts: number;
  online?: boolean; reactionId?: string | null; reaction?: string | null;
};
export type HeaderState = {
  gameId: string; players: HeaderPlayer[]; activePlayerId: string | null;
  goal: number; barrelBoundaries: number[]; clickable: boolean;
};
export type HeaderEvent = { type: string; running?: boolean; playerId?: string };
const robot = `<svg viewBox="0 0 64 64" aria-hidden="true"><path d="M32 11V5m-4 0h8" stroke="#9beddc" stroke-width="3" stroke-linecap="round"/><rect x="9" y="16" width="46" height="37" rx="14" fill="#e9c18e"/><rect x="13" y="20" width="38" height="25" rx="10" fill="#15263d"/><circle cx="24" cy="32" r="5" fill="#8cebdc"/><circle cx="40" cy="32" r="5" fill="#8cebdc"/><circle cx="25" cy="30" r="1.5" fill="white"/><circle cx="41" cy="30" r="1.5" fill="white"/><path d="M27 49h10" stroke="#75588d" stroke-width="3" stroke-linecap="round"/><path d="M6 28v12m52-12v12" stroke="#b99bdb" stroke-width="5" stroke-linecap="round"/></svg>`;
const face = `<svg viewBox="0 0 64 64" aria-hidden="true"><path d="M10 64c0-18 44-18 44 0" fill="#f2b68b"/><path d="M24 48h16v12H24" fill="#d99062"/><ellipse cx="32" cy="31" rx="19" ry="23" fill="#f6c999"/><path d="M12 31C4 9 26 1 36 9 55 4 58 24 49 34l-3-15c-8 7-16 5-22 0l-8 16z" fill="#292c43"/><ellipse cx="24" cy="32" rx="2.3" ry="3" fill="#292c43"/><ellipse cx="40" cy="32" rx="2.3" ry="3" fill="#292c43"/><path d="M27 42q5 5 10 0" fill="none" stroke="#9d514c" stroke-width="2.4" stroke-linecap="round"/><path d="M11 64l13-8 8 8 8-8 13 8" fill="#87b7d0"/></svg>`;
const colors = ['#345c8a', '#634880', '#356b67', '#806044'];

export function createGameHeader(root: HTMLElement, emit: (event: HeaderEvent) => void) {
  const list = document.createElement('div'); list.className = 'players-list';
  const progressRow = document.createElement('div'); progressRow.className = 'progress-row';
  const track = document.createElement('div'); track.className = 'score-track'; track.setAttribute('role', 'progressbar');
  const fill = document.createElement('div'); fill.className = 'score-fill';
  const zones = document.createElement('div'); zones.className = 'barrel-zones'; zones.setAttribute('aria-hidden', 'true');
  const ticks = document.createElement('div'); ticks.className = 'barrel-ticks'; track.append(fill, zones, ticks);
  const goal = document.createElement('span'); goal.className = 'score-goal'; progressRow.append(track, goal); root.append(list, progressRow);
  let shown: HeaderState | null = null, latest: HeaderState | null = null;
  let pending: (() => void) | null = null, animation = 0, animating = false;
  const expired = new Set<string>(), reactionTimers = new Map<string, ReturnType<typeof setTimeout>>();
  function render(state: HeaderState) {
    const previousScroll = list.scrollLeft;
    list.replaceChildren();
    root.hidden = false;
    state.players.forEach((player, index) => {
      const card = document.createElement('button'); card.type = 'button'; card.className = 'header-player';
      card.dataset.playerId = player.id; card.dataset.active = String(player.id === state.activePlayerId);
      card.setAttribute('aria-label', `${player.name}, ${player.score} очков${player.id === state.activePlayerId ? ', ходит' : ''}${player.bolts ? `, болтов: ${player.bolts}` : ''}`);
      card.disabled = !state.clickable;
      card.addEventListener('click', () => emit({ type: 'playerSelected', playerId: player.id }));
      const avatar = document.createElement('span'); avatar.className = 'player-avatar'; avatar.style.backgroundColor = colors[index % colors.length];
      avatar.innerHTML = player.bot ? robot : face; // Only static, locally-authored SVG, never player text.
      if (player.online !== undefined) {
        const dot = document.createElement('i'); dot.className = `online-dot ${player.online ? 'online' : ''}`; avatar.append(dot);
      }
      if (player.reactionId && player.reaction && !expired.has(player.reactionId)) {
        const bubble = document.createElement('span'); bubble.className = 'avatar-reaction'; bubble.textContent = player.reaction; avatar.append(bubble);
        if (!reactionTimers.has(player.reactionId)) {
          const id = player.reactionId;
          reactionTimers.set(id, setTimeout(() => { expired.add(id); reactionTimers.delete(id); if (shown && !animating) render(shown); }, 10000));
        }
      }
      const meta = document.createElement('span'); meta.className = 'player-meta';
      const name = document.createElement('span'); name.className = 'player-name'; name.textContent = player.name + (player.winner ? ' 🏆' : '');
      const score = document.createElement('span'); score.className = 'player-score'; score.textContent = String(player.score); score.dataset.scoreFor = player.id;
      if (player.bolts) {
        const bolts = document.createElement('span'); bolts.className = 'player-bolts'; bolts.setAttribute('aria-hidden', 'true');
        for (let i = 0; i < player.bolts; i++) {
          const marker = document.createElement('span'); marker.className = 'bolt-card'; bolts.append(marker);
        }
        avatar.append(bolts);
      }
      meta.append(name, score); card.append(avatar, meta); list.append(card);
    });
    list.scrollLeft = previousScroll;
    ticks.replaceChildren();
    zones.replaceChildren();
    for (let i = 0; i + 1 < state.barrelBoundaries.length; i += 2) {
      const start = state.barrelBoundaries[i], end = state.barrelBoundaries[i + 1];
      const zone = document.createElement('span'); zone.className = 'barrel-zone';
      zone.style.left = `${start / state.goal * 100}%`;
      zone.style.width = `${(end - start) / state.goal * 100}%`;
      zones.append(zone);
    }
    state.barrelBoundaries.forEach(boundary => {
      const tick = document.createElement('span'); tick.className = 'barrel-tick'; tick.style.left = `${boundary / state.goal * 100}%`;
      tick.title = `${boundary} очков — граница бочки`; tick.setAttribute('aria-label', tick.title);
      const label = document.createElement('span'); label.textContent = String(boundary); tick.append(label); ticks.append(tick);
    });
    goal.textContent = String(state.goal);
    track.setAttribute('aria-valuemin', '0'); track.setAttribute('aria-valuemax', String(state.goal));
    const active = state.players.find(p => p.id === state.activePlayerId) ?? state.players.find(p => p.winner);
    setScore(active?.score ?? 0, state.goal, active?.name ?? 'Игрок');
    const activeCard = Array.from(list.children).find(el => (el as HTMLElement).dataset.playerId === state.activePlayerId) as HTMLElement | undefined;
    if (activeCard && state.activePlayerId !== shown?.activePlayerId) {
      const left = activeCard.offsetLeft - list.offsetLeft, right = left + activeCard.offsetWidth;
      if (left < list.scrollLeft || right > list.scrollLeft + list.clientWidth) list.scrollTo({ left: left - (list.clientWidth - activeCard.offsetWidth) / 2, behavior: 'smooth' });
    }
  }
  function setScore(score: number, max: number, name: string) {
    fill.style.width = `${Math.max(0, Math.min(100, score / max * 100))}%`;
    track.setAttribute('aria-valuenow', String(Math.round(score))); track.setAttribute('aria-label', `${name}: ${Math.round(score)} из ${max} очков`);
  }
  function finish() {
    cancelAnimationFrame(animation); animation = 0;
    if (latest) { render(latest); shown = latest; }
    else { root.hidden = true; shown = null; }
    const callback = pending; pending = null;
    if (animating) { animating = false; emit({ type: 'headerAnimation', running: false }); }
    callback?.();
  }
  return {
    update(next: HeaderState | null, active: boolean, applyScene: () => void) {
      latest = next; pending = applyScene;
      if (animating) { if (!active || next?.gameId !== shown?.gameId) finish(); return; }
      if (!next) { finish(); return; }
      const previousPlayer = shown?.players.find(p => p.id === shown?.activePlayerId);
      const updatedPlayer = next.players.find(p => p.id === previousPlayer?.id);
      if (shown?.gameId === next.gameId && previousPlayer && updatedPlayer && updatedPlayer.score > previousPlayer.score && active && !matchMedia('(prefers-reduced-motion: reduce)').matches) {
        animating = true;
        const outgoing = { ...next, activePlayerId: previousPlayer.id };
        render({ ...outgoing, players: outgoing.players.map(p => p.id === previousPlayer.id ? { ...p, score: previousPlayer.score } : p) });
        emit({ type: 'headerAnimation', running: true });
        const started = performance.now();
        const step = (now: number) => {
          const progress = Math.min(1, (now - started) / 850), eased = 1 - (1 - progress) ** 3;
          const value = previousPlayer.score + (updatedPlayer.score - previousPlayer.score) * eased;
          setScore(value, next.goal, previousPlayer.name);
          const label = Array.from(list.querySelectorAll<HTMLElement>('[data-score-for]')).find(el => el.dataset.scoreFor === previousPlayer.id);
          if (label) label.textContent = String(Math.round(value));
          if (progress < 1) animation = requestAnimationFrame(step); else finish();
        };
        animation = requestAnimationFrame(step);
      } else {
        if (JSON.stringify(next) !== JSON.stringify(shown)) render(next);
        shown = next; pending = null; applyScene();
      }
    },
    stop() { finish(); },
  };
}
