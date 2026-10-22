import { Quaternion, Vector3 } from 'three';

// Opposite faces sum to seven. These normals also position the pip meshes.
export const faceNormals = [
  new Vector3(0, 1, 0), new Vector3(1, 0, 0), new Vector3(0, 0, 1),
  new Vector3(0, 0, -1), new Vector3(-1, 0, 0), new Vector3(0, -1, 0),
];
export function orientationFor(value: number): Quaternion {
  if (!Number.isInteger(value) || value < 1 || value > 6) throw new Error('Invalid die value');
  return new Quaternion().setFromUnitVectors(faceNormals[value - 1], new Vector3(0, 1, 0));
}
