export const ROLL_DURATION_MS = 1200;
const SETTLE_SPREAD_MS = 400;

export function createRollDurations(diceCount: number, random = Math.random): number[] {
  if (diceCount === 1) return [ROLL_DURATION_MS];

  const spacing = SETTLE_SPREAD_MS / (diceCount - 1);
  const durations = Array.from({ length: diceCount - 1 }, (_, index) => {
    const jitter = random() * spacing * .4;
    return ROLL_DURATION_MS - SETTLE_SPREAD_MS + index * spacing + jitter;
  });
  durations.push(ROLL_DURATION_MS);

  for (let index = durations.length - 1; index > 0; index--) {
    const other = Math.floor(random() * (index + 1));
    [durations[index], durations[other]] = [durations[other], durations[index]];
  }
  return durations;
}
