import * as THREE from 'three';

function randomSequence(seed: number) {
  return () => {
    seed = (Math.imul(seed, 1664525) + 1013904223) >>> 0;
    return seed / 4294967296;
  };
}

/** Small seamless woven tiles, generated once and shared by the whole playing surface. */
export function createFeltMaterial(renderer: THREE.WebGLRenderer) {
  const cloth = document.createElement('canvas');
  const relief = document.createElement('canvas');
  cloth.width = cloth.height = relief.width = relief.height = 256;
  const context = cloth.getContext('2d')!;
  const bumpContext = relief.getContext('2d')!;
  const color = context.createImageData(256, 256);
  const bump = bumpContext.createImageData(256, 256);
  const random = randomSequence(713);
  for (let y = 0; y < 256; y++) {
    for (let x = 0; x < 256; x++) {
      const i = (y * 256 + x) * 4;
      const weave = Math.sin(x * Math.PI / 2) * Math.cos(y * Math.PI / 2);
      const nap = (random() - .5) * 9 + weave * 2;
      color.data.set([48 + nap, 72 + nap, 32 + nap * .6, 255], i);
      const height = 128 + weave * 20 + (random() - .5) * 42;
      bump.data.set([height, height, height, 255], i);
    }
  }
  context.putImageData(color, 0, 0);
  bumpContext.putImageData(bump, 0, 0);
  const map = new THREE.CanvasTexture(cloth);
  map.colorSpace = THREE.SRGBColorSpace;
  const bumpMap = new THREE.CanvasTexture(relief);
  for (const texture of [map, bumpMap]) {
    texture.wrapS = texture.wrapT = THREE.RepeatWrapping;
    texture.repeat.set(160, 160);
    texture.anisotropy = Math.min(renderer.capabilities.getMaxAnisotropy(), 4);
  }
  return new THREE.MeshStandardMaterial({ map, bumpMap, bumpScale: .018, roughness: .98, metalness: 0 });
}

/** Rounded rails and their shadows are rasterized only when the viewport changes. */
export function drawWoodenFrame(canvas: HTMLCanvasElement, width: number, height: number, pixelRatio: number) {
  canvas.width = Math.round(width * pixelRatio);
  canvas.height = Math.round(height * pixelRatio);
  const context = canvas.getContext('2d')!;
  context.scale(pixelRatio, pixelRatio);
  const margin = Math.min(24, Math.max(12, Math.min(width, height) * .04));
  const rail = Math.min(18, Math.max(11, Math.min(width, height) * .035));
  const radius = Math.min(width - margin * 2, height - margin * 2) / 2;
  const contour = (inset: number) => {
    const path = new Path2D();
    path.roundRect(margin + inset, margin + inset, width - 2 * (margin + inset), height - 2 * (margin + inset), Math.max(1, radius - inset));
    return path;
  };
  const outer = contour(0), inner = contour(rail);
  context.fillStyle = '#18201b';
  context.fillRect(0, 0, width, height);
  // The table's drop shadow falls to the right, away from the light on the left.
  context.shadowColor = 'rgba(0,0,0,.85)';
  context.shadowBlur = 18;
  context.shadowOffsetX = 9;
  context.shadowOffsetY = 5;
  context.fillStyle = '#52361e';
  context.fill(outer);
  context.shadowBlur = context.shadowOffsetX = context.shadowOffsetY = 0;
  context.save();
  context.clip(outer);
  const wood = context.createLinearGradient(0, 0, width, height * .15);
  wood.addColorStop(0, '#a67c48');
  wood.addColorStop(.32, '#80552f');
  wood.addColorStop(.7, '#654123');
  wood.addColorStop(1, '#432b19');
  context.fillStyle = wood;
  context.fillRect(0, 0, width, height);
  const random = randomSequence(908);
  for (let strand = 0; strand < 90; strand++) {
    const inset = random() * rail;
    context.strokeStyle = strand % 3 ? 'rgba(38,18,6,.16)' : 'rgba(236,192,125,.18)';
    context.lineWidth = .25 + random() * .6;
    context.stroke(contour(inset));
  }
  context.restore();
  // Punch out the playing surface, retaining the WebGL cloth and dice below.
  context.save();
  context.globalCompositeOperation = 'destination-out';
  context.fill(inner);
  context.restore();
  const edgeLight = context.createLinearGradient(0, 0, width, 0);
  edgeLight.addColorStop(0, 'rgba(255,220,158,.7)');
  edgeLight.addColorStop(.55, 'rgba(182,132,75,.25)');
  edgeLight.addColorStop(1, 'rgba(15,9,4,.8)');
  context.strokeStyle = edgeLight;
  context.lineWidth = 1.5;
  context.stroke(contour(1));
  context.strokeStyle = 'rgba(24,13,5,.65)';
  context.lineWidth = 2;
  context.stroke(contour(rail - 1));
  // The raised left rail casts a soft, directional shadow across the felt.
  context.save();
  context.clip(inner);
  context.shadowColor = 'rgba(0,0,0,.7)';
  context.shadowBlur = 9;
  context.shadowOffsetX = 7;
  context.shadowOffsetY = 3;
  context.strokeStyle = 'rgba(20,12,5,.55)';
  context.lineWidth = 4;
  context.stroke(inner);
  context.restore();
}
