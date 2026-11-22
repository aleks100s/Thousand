import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createRollDurations, ROLL_DURATION_MS } from './roll-timing.js';

test('a single die rolls for the full duration', () => {
  assert.deepEqual(createRollDurations(1), [ROLL_DURATION_MS]);
});

test('dice stop at distinct times within the final 400 ms', () => {
  for (let count = 2; count <= 5; count++) {
    let nextRandom = .999;
    const durations = createRollDurations(count, () => {
      const value = nextRandom;
      nextRandom = nextRandom === 0 ? .999 : 0;
      return value;
    }).sort((first, second) => first - second);

    assert.equal(durations.length, count);
    assert.ok(durations[0] >= ROLL_DURATION_MS - 400);
    assert.equal(durations[count - 1], ROLL_DURATION_MS);
    for (let index = 1; index < count; index++) {
      assert.ok(durations[index] - durations[index - 1] >= 60);
    }
  }
});

test('randomness changes the timing and which die stops last', () => {
  const firstRoll = createRollDurations(5, () => 0);
  const secondRoll = createRollDurations(5, () => .999);

  assert.notEqual(firstRoll.indexOf(ROLL_DURATION_MS), secondRoll.indexOf(ROLL_DURATION_MS));
  assert.notEqual(Math.min(...firstRoll), Math.min(...secondRoll));
});
