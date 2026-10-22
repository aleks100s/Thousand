import { test } from 'node:test';
import assert from 'node:assert/strict';
import { Vector3 } from 'three';
import { faceNormals, orientationFor } from './orientation.js';
for (let value = 1; value <= 6; value++) {
  test(`face ${value} ends on top and its opposite faces down`, () => {
    const rotation = orientationFor(value);
    assert.ok(faceNormals[value - 1].clone().applyQuaternion(rotation).distanceTo(new Vector3(0, 1, 0)) < 1e-6);
    assert.ok(faceNormals[6 - value].clone().applyQuaternion(rotation).distanceTo(new Vector3(0, -1, 0)) < 1e-6);
  });
}
test('rejects invalid faces', () => {
  for (const value of [0, 7, 1.5, NaN]) assert.throws(() => orientationFor(value));
});
