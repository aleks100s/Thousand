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

/** Screen-edge rails keep their thickness and mitered corners on every viewport. */
export function drawWoodenFrame(canvas: HTMLCanvasElement, width: number, height: number, pixelRatio: number) {
  canvas.width = Math.round(width * pixelRatio);
  canvas.height = Math.round(height * pixelRatio);
  const context = canvas.getContext('2d')!;
  context.scale(pixelRatio, pixelRatio);
  const rail = Math.min(18, Math.max(10, Math.min(width, height) * .032));
  // A recessed inner edge casts a soft shadow onto the cloth.
  context.strokeStyle = '#100b06';
  context.lineWidth = rail;
  context.shadowColor = 'rgba(0,0,0,.65)';
  context.shadowBlur = 12;
  context.strokeRect(rail / 2, rail / 2, width - rail, height - rail);
  context.shadowBlur = 0;
  function drawRail(length: number) {
    context.save();
    context.beginPath();
    context.moveTo(0, 0);
    context.lineTo(length, 0);
    context.lineTo(length - rail, rail);
    context.lineTo(rail, rail);
    context.closePath();
    context.clip();
    const bevel = context.createLinearGradient(0, 0, 0, rail);
    for (const [offset, color] of [[0, '#342010'], [.12, '#977044'], [.25, '#79512d'], [.74, '#624022'], [.88, '#a07947'], [1, '#35200f']] as const) {
      bevel.addColorStop(offset, color);
    }
    context.fillStyle = bevel;
    context.fillRect(0, 0, length, rail);
    const random = randomSequence(908);
    for (let strand = 0; strand < 65; strand++) {
      const y = random() * rail;
      const phase = random() * Math.PI * 2;
      context.strokeStyle = strand % 3 ? 'rgba(35,17,6,.18)' : 'rgba(224,182,113,.14)';
      context.lineWidth = .3 + random() * .7;
      context.beginPath();
      for (let x = 0; x <= length + 8; x += 8) {
        const dy = Math.sin(x * .017 + phase) * .6 + Math.sin(x * .043 + phase) * .2;
        if (x === 0) context.moveTo(x, y + dy);
        else context.lineTo(x, y + dy);
      }
      context.stroke();
    }
    context.restore();
  }
  drawRail(width);
  context.save(); context.translate(width, 0); context.rotate(Math.PI / 2); drawRail(height); context.restore();
  context.save(); context.translate(width, height); context.rotate(Math.PI); drawRail(width); context.restore();
  context.save(); context.translate(0, height); context.rotate(-Math.PI / 2); drawRail(height); context.restore();
}
