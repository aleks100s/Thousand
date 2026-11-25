import { playerAvatars } from './player-avatars';
import { designColors } from './design-tokens';
export type HeaderPlayer = {
  id: string; name: string; score: number; bot: boolean; winner: boolean; bolts: number;
  color?: string; avatar?: string; online?: boolean; reactionId?: string | null; reaction?: string | null;
};
export type HeaderState = {
  gameId: string; players: HeaderPlayer[]; activePlayerId: string | null;
  goal: number; barrelBoundaries: number[]; clickable: boolean;
};
export type HeaderEvent = { type: string; running?: boolean; playerId?: string };

type PlayerScoreView = {
  name: string;
  initial: string;
  score: number;
  label: HTMLElement;
  marker: HTMLElement;
};

const coincidentMarkerDistance = 1; // CSS pixel

export function createGameHeader(root: HTMLElement, emit: (event: HeaderEvent) => void,
  startScoreTransfer: (playerId: string, gameId: string) => ((progress: number) => void) | undefined = () => undefined,
) {
  const list = document.createElement('div'); list.className = 'players-list';
  const progressRow = document.createElement('div'); progressRow.className = 'progress-row';
  const track = document.createElement('div'); track.className = 'score-track'; track.setAttribute('role', 'group');
  const zones = document.createElement('div'); zones.className = 'barrel-zones'; zones.setAttribute('aria-hidden', 'true');
  const ticks = document.createElement('div'); ticks.className = 'barrel-ticks';
  const markers = document.createElement('div'); markers.className = 'score-markers';
  track.append(zones, ticks, markers);
  const goal = document.createElement('span'); goal.className = 'score-goal'; progressRow.append(track, goal); root.append(list, progressRow);
  let shown: HeaderState | null = null, latest: HeaderState | null = null;
  let pending: (() => void) | null = null, animation = 0, animating = false;
  let scoreTransfer: ((progress: number) => void) | undefined;
  let scoreGoal = 1;
  const scoreViews = new Map<string, PlayerScoreView>();
  const expired = new Set<string>(), reactionTimers = new Map<string, ReturnType<typeof setTimeout>>();

  function updateScore(view: PlayerScoreView, score: number) {
    view.score = score;
    view.label.textContent = String(Math.round(score));
  }

  function positionMarkers() {
    const width = track.clientWidth;
    if (!width) return;
    const positions = Array.from(scoreViews.values(), view => ({
      view,
      x: Math.max(0, Math.min(1, view.score / scoreGoal)) * width,
    })).sort((a, b) => a.x - b.x);

    for (let start = 0; start < positions.length;) {
      const { view: { marker, initial }, x } = positions[start];
      let end = start + 1;
      while (end < positions.length && positions[end].x - x <= coincidentMarkerDistance) {
        positions[end].view.marker.hidden = true;
        end++;
      }

      const count = end - start;
      marker.hidden = false;
      marker.classList.toggle('score-marker-group', count > 1);
      marker.style.transform = `translate(${x}px, -50%) translateX(-50%)`;
      const caption = count > 1 ? String(count) : initial;
      if (marker.textContent !== caption) marker.textContent = caption;

      const players = positions.slice(start, end)
        .map(({ view }) => `${view.name}, ${Math.round(view.score)} очков`).join('. ');
      const description = count > 1 ? `Игроки (${count}). ${players}` : players;
      if (marker.title !== description) {
        marker.title = description;
        marker.setAttribute('aria-label', description);
      }
      start = end;
    }
  }

  new ResizeObserver(positionMarkers).observe(track);

  function render(state: HeaderState) {
    const previousScroll = list.scrollLeft;
    list.replaceChildren();
    markers.replaceChildren();
    scoreViews.clear();
    scoreGoal = state.goal;
    root.hidden = false;
    state.players.forEach(player => {
      const color = /^#[0-9a-f]{6}$/i.test(player.color ?? '') ? player.color! : designColors.AvatarLocalBottom;
      const card = document.createElement('button'); card.type = 'button'; card.className = 'header-player';
      card.dataset.playerId = player.id; card.dataset.active = String(player.id === state.activePlayerId);
      card.setAttribute('aria-label', `${player.name}, ${player.score} очков${player.id === state.activePlayerId ? ', ходит' : ''}${player.bolts ? `, болтов: ${player.bolts}` : ''}`);
      card.disabled = !state.clickable;
      card.addEventListener('click', () => emit({ type: 'playerSelected', playerId: player.id }));
      const avatar = document.createElement('span'); avatar.className = 'player-avatar'; avatar.style.backgroundColor = color;
      avatar.innerHTML = player.bot ? playerAvatars.robot : playerAvatars[(player.avatar ?? 'Neutral').toLowerCase() as keyof typeof playerAvatars] ?? playerAvatars.neutral; // Only static, locally-authored SVG, never player text.
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
      const score = document.createElement('span'); score.className = 'player-score'; score.dataset.scoreFor = player.id;
      const marker = document.createElement('span');
      marker.className = 'score-marker';
      marker.setAttribute('role', 'img');
      marker.style.setProperty('--marker-color', color);
      if (color === designColors.AvatarLocalBottom) marker.style.setProperty('--marker-text-color', designColors.TextPrimary);
      const [initial] = player.name.trim();
      const scoreView = { name: player.name, initial: initial?.toLocaleUpperCase() ?? '?', score: player.score, label: score, marker };
      scoreViews.set(player.id, scoreView);
      updateScore(scoreView, player.score);
      markers.append(marker);
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
    track.setAttribute('aria-label', `Шкала очков от 0 до ${state.goal}`);
    positionMarkers();
    const activeCard = Array.from(list.children).find(el => (el as HTMLElement).dataset.playerId === state.activePlayerId) as HTMLElement | undefined;
    if (activeCard && state.activePlayerId !== shown?.activePlayerId) {
      const left = activeCard.offsetLeft - list.offsetLeft, right = left + activeCard.offsetWidth;
      if (left < list.scrollLeft || right > list.scrollLeft + list.clientWidth) list.scrollTo({ left: left - (list.clientWidth - activeCard.offsetWidth) / 2, behavior: 'smooth' });
    }
  }
  function finish() {
    cancelAnimationFrame(animation); animation = 0;
    scoreTransfer?.(1); scoreTransfer = undefined;
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
      const previous = shown?.gameId === next.gameId ? shown : null;
      const previousScores = new Map(previous?.players.map(player => [player.id, player.score]));
      const scoreChanges = next.players.flatMap(player => {
        const from = previousScores.get(player.id);
        return from === undefined || from === player.score ? [] : [{ id: player.id, from, to: player.score }];
      });
      if (previous && scoreChanges.length && active && !matchMedia('(prefers-reduced-motion: reduce)').matches) {
        animating = true;
        const outgoingId = previous.activePlayerId;
        const transfersScore = outgoingId !== null && scoreChanges.some(({ id, from, to }) => id === outgoingId && to > from);
        render({
          ...next,
          activePlayerId: transfersScore ? outgoingId : next.activePlayerId,
          players: next.players.map(player => ({ ...player, score: previousScores.get(player.id) ?? player.score })),
        });
        const animatedScores = scoreChanges.map(change => ({ ...change, view: scoreViews.get(change.id)! }));
        if (transfersScore) scoreTransfer = startScoreTransfer(outgoingId, next.gameId);
        emit({ type: 'headerAnimation', running: true });
        const started = performance.now();
        const duration = transfersScore ? 850 : 300;
        const step = (now: number) => {
          const progress = Math.min(1, (now - started) / duration), eased = 1 - (1 - progress) ** 3;
          scoreTransfer?.(progress);
          for (const { view, from, to } of animatedScores) updateScore(view, from + (to - from) * eased);
          positionMarkers();
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
