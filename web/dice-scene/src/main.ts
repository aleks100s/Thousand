import * as THREE from 'three';
import { RoundedBoxGeometry } from 'three/addons/geometries/RoundedBoxGeometry.js';
import { faceNormals, orientationFor } from './orientation';
import { createFeltMaterial, drawWoodenFrame } from './table';

type Request = { presentationId: string; values: number[]; animate: boolean; scoringIndices?: number[]; scoreText?: string };
type Bounds = { left: number; top: number; right: number; bottom: number };
type Configuration = { request: Request | null; bounds: Bounds; background: string; active: boolean; promptText?: string; isOpponentTurn?: boolean };
type NativeWindow = Window & {
  DiceNative?: { postMessage(message: string): void };
  webkit?: { messageHandlers?: { diceScene?: { postMessage(message: string): void } } };
  DiceScene?: { receive(message: string): void };
};
const native = window as NativeWindow;
const send = (type: string, presentationId?: string) => {
  const message = JSON.stringify({ type, presentationId });
  if (native.DiceNative) native.DiceNative.postMessage(message);
  else if (native.webkit?.messageHandlers?.diceScene) native.webkit.messageHandlers.diceScene.postMessage(message);
  else window.dispatchEvent(new CustomEvent('diceSceneEvent', { detail: JSON.parse(message) }));
};
const DURATION = 1200;
let config: Configuration = {
  request: null, bounds: { left: .06, top: .2, right: .94, bottom: .7 },
  background: '#212622', active: true,
};
let currentId: string | undefined;
let frame = 0;
let startedAt: number | null = null;
let complete = true;
let failed = false;
const canvas = document.querySelector('canvas')!;
const woodenFrame = document.querySelector<HTMLCanvasElement>('#wooden-frame')!;
const result = document.querySelector<HTMLElement>('#roll-result')!;
const score = document.querySelector<HTMLElement>('#roll-score')!;
const prompt = document.querySelector<HTMLElement>('#roll-prompt')!;

try {
  const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: false });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = .9;
  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(37, 1, .1, 100);
  camera.position.set(0, 10, 6);
  camera.lookAt(0, 0, 0);
  scene.add(new THREE.HemisphereLight(0xffffff, 0x667069, 1.8));
  const light = new THREE.DirectionalLight(0xffffff, 2.0);
  light.position.set(-5, 9, 3);
  light.castShadow = true;
  light.shadow.mapSize.set(1024, 1024);
  Object.assign(light.shadow.camera, { left: -9, right: 9, top: 9, bottom: -9, near: .1, far: 25 });
  light.shadow.normalBias = .025;
  light.shadow.bias = -.0002;
  light.shadow.radius = 4;
  scene.add(light);
  const tableMaterial = createFeltMaterial(renderer);
  const table = new THREE.Mesh(new THREE.PlaneGeometry(200, 200), tableMaterial);
  table.rotation.x = -Math.PI / 2;
  table.receiveShadow = true;
  scene.add(table);
  const geometry = new RoundedBoxGeometry(1, 1, 1, 4, .09);
  const ivory = new THREE.MeshStandardMaterial({ color: '#fffdf4', roughness: .28, metalness: .02 });
  // Nested back-face shells create a soft silhouette without a bloom pass or an idle render loop.
  const haloMaterials = [.30, .16, .085, .045, .02].map(opacity => new THREE.MeshBasicMaterial({
    color: '#55e5dc', transparent: true, opacity, side: THREE.BackSide, depthWrite: false, toneMapped: false,
  }));
  const ink = new THREE.MeshStandardMaterial({ color: '#202625', roughness: .6 });
  const pipGeometry = new THREE.CircleGeometry(.075, 20);
  const patterns: [number, number][][] = [
    [[0, 0]], [[-.23, .23], [.23, -.23]],
    [[-.23, .23], [0, 0], [.23, -.23]],
    [[-.23, .23], [.23, .23], [-.23, -.23], [.23, -.23]],
    [[-.23, .23], [.23, .23], [0, 0], [-.23, -.23], [.23, -.23]],
    [[-.23, .25], [.23, .25], [-.23, 0], [.23, 0], [-.23, -.25], [.23, -.25]],
  ];
  let dice: THREE.Group[] = [];
  let halos: THREE.Group[] = [];
  let targets: THREE.Vector3[] = [];
  let scale = 1;
  const plane = new THREE.Plane(new THREE.Vector3(0, 1, 0), 0);
  const ray = new THREE.Raycaster();
  function worldPoint(x: number, y: number): THREE.Vector3 {
    ray.setFromCamera(new THREE.Vector2(x * 2 - 1, 1 - y * 2), camera);
    return ray.ray.intersectPlane(plane, new THREE.Vector3()) ?? new THREE.Vector3();
  }
  function layout() {
    const landscape = canvas.clientWidth > canvas.clientHeight;
    const left = landscape ? Math.max(.12, config.bounds.left) : config.bounds.left;
    const right = landscape ? Math.min(.88, config.bounds.right) : config.bounds.right;
    // Native overlays may leave almost no top inset. Keep the dice below the
    // curved head of the table, where its full width is available.
    // History occupies the upper-left corner, not the entire landscape row.
    // Its growing height must not shrink dice on every subsequent reroll.
    const top = landscape ? .16 : Math.max(config.bounds.top, Math.min(.23, config.bounds.bottom - .20));
    const resultSpace = landscape
      ? 76 / Math.max(canvas.clientHeight, 1)
      : Math.min(88 / Math.max(canvas.clientHeight, 1), (config.bounds.bottom - top) * .3);
    const bottom = config.bounds.bottom - resultSpace;
    const width = Math.max(.01, right - left), height = Math.max(.01, bottom - top);
    const columns = landscape ? dice.length : Math.min(dice.length, 3);
    const rows = Math.ceil(dice.length / Math.max(columns, 1));
    const cx = (left + right) / 2, cy = (top + bottom) / 2;
    const worldWidth = worldPoint(right, cy).distanceTo(worldPoint(left, cy));
    const worldHeight = worldPoint(cx, bottom).distanceTo(worldPoint(cx, top));
    scale = Math.min(landscape ? 1.8 : 1.15, worldWidth / Math.max(columns, 1) / 1.7, worldHeight / Math.max(rows, 1) / (landscape ? 1.35 : 2.1));
    targets = dice.map((_, i) => {
      const row = Math.floor(i / columns);
      const inRow = Math.min(columns, dice.length - row * columns);
      const x = cx + ((i % columns) - (inRow - 1) / 2) * width / columns * .82;
      const y = cy + (landscape ? height * .22 : 0) + (row - (rows - 1) / 2) * height / Math.max(rows, 1) * .9;
      const point = worldPoint(x, y);
      point.y = scale * .51;
      return point;
    });
    if (landscape && targets.length) {
      // Fit the projected cube volume, including its halo, rather than just the
      // table footprint. A single die must leave the same room for the score.
      const projectedRange = () => {
        let min = Infinity, max = -Infinity;
        targets.forEach(target => {
          for (const x of [-.6, .6]) for (const y of [-.6, .6]) for (const z of [-.6, .6]) {
            const point = new THREE.Vector3(target.x + x * scale, target.y + y * scale, target.z + z * scale).project(camera);
            const screenY = (1 - point.y) / 2;
            min = Math.min(min, screenY);
            max = Math.max(max, screenY);
          }
        });
        return { min, max };
      };
      const extent = projectedRange();
      scale *= Math.min(1, height * 1.04 / (extent.max - extent.min));
      scale *= config.isOpponentTurn ? .9 : 1.06;
      targets.forEach(target => { target.y = scale * .51; });
      const fitted = projectedRange();
      const desiredCenter = Math.min(.5, cy + .08);
      const offset = worldPoint(cx, cy + desiredCenter - (fitted.min + fitted.max) / 2).sub(worldPoint(cx, cy));
      targets.forEach(target => target.add(offset));
    } else if (targets.length) {
      const offset = worldPoint(cx, cy + .04).sub(worldPoint(cx, cy));
      targets.forEach(target => target.add(offset));
    }
    dice.forEach(die => die.scale.setScalar(scale));
  }
  function rebuild(request: Request | null) {
    dice.forEach(die => scene.remove(die));
    halos = [];
    dice = (request?.values ?? []).map((_, index) => {
      const die = new THREE.Group();
      const body = new THREE.Mesh(geometry, ivory);
      body.castShadow = true;
      body.receiveShadow = true;
      die.add(body);
      const halo = new THREE.Group();
      halo.visible = false;
      if (request?.scoringIndices?.includes(index)) {
        haloMaterials.forEach((material, layer) => {
          const shell = new THREE.Mesh(geometry, material);
          shell.scale.setScalar(1.04 + layer * .03);
          shell.renderOrder = 1;
          halo.add(shell);
        });
      }
      die.add(halo);
      halos.push(halo);
      faceNormals.forEach((normal, index) => {
        const rotation = new THREE.Quaternion().setFromUnitVectors(new THREE.Vector3(0, 0, 1), normal);
        patterns[index].forEach(([x, y]) => {
          const pip = new THREE.Mesh(pipGeometry, ink);
          pip.position.copy(new THREE.Vector3(x, y, .501).applyQuaternion(rotation));
          pip.quaternion.copy(rotation);
          die.add(pip);
        });
      });
      scene.add(die);
      return die;
    });
    layout();
  }
  function pose(progress: number) {
    const request = config.request;
    if (!request) return;
    const ease = 1 - Math.pow(1 - progress, 3);
    dice.forEach((die, i) => {
      const target = targets[i];
      const direction = i % 2 ? -1 : 1;
      die.position.copy(target);
      die.position.x += Math.sin(progress * Math.PI * 2 + i) * (1 - ease) * scale * .35;
      die.position.y += Math.abs(Math.sin(progress * Math.PI * 3)) * (1 - progress) * scale * .65;
      die.position.z += direction * (1 - ease) * scale * .4;
      const spin = new THREE.Quaternion().setFromEuler(new THREE.Euler(
        direction * (1 - ease) * Math.PI * 4,
        (1 - ease) * Math.PI * (3 + i),
        (1 - ease) * Math.PI * 2,
      ));
      die.quaternion.copy(orientationFor(request.values[i])).multiply(spin);
    });
  }
  function render() { renderer.render(scene, camera); }
  function positionResult() {
    if (!config.request || !dice.length) { result.hidden = true; return; }
    score.textContent = config.request.scoreText ?? '0 очков';
    prompt.textContent = config.promptText ?? 'продолжить или сохранить?';
    const height = canvas.clientHeight;
    const box = new THREE.Box3();
    let lowest = 0;
    dice.forEach(die => {
      die.updateWorldMatrix(true, true);
      box.setFromObject(die);
      for (const x of [box.min.x, box.max.x]) for (const y of [box.min.y, box.max.y]) for (const z of [box.min.z, box.max.z]) {
        const projected = new THREE.Vector3(x, y, z).project(camera);
        lowest = Math.max(lowest, (1 - projected.y) / 2 * height);
      }
    });
    result.style.top = `${Math.max(canvas.clientWidth > height ? 0 : config.bounds.top * height, lowest + 12)}px`;
    result.style.left = `${(config.bounds.left + config.bounds.right) / 2 * 100}%`;
    result.hidden = false;
  }
  function stopFrame() { cancelAnimationFrame(frame); frame = 0; }
  function finish() {
    stopFrame();
    pose(1);
    halos.forEach(halo => { halo.visible = true; });
    render();
    positionResult();
    if (!complete && currentId) {
      complete = true;
      send('rollFinished', currentId);
    }
  }
  function tick(now: number) {
    if (!config.active || failed) return;
    if (startedAt === null) startedAt = now;
    const progress = Math.min((now - startedAt) / DURATION, 1);
    pose(progress);
    render();
    if (progress < 1) frame = requestAnimationFrame(tick);
    else finish();
  }
  function resize() {
    const width = Math.max(canvas.clientWidth, 1), height = Math.max(canvas.clientHeight, 1);
    renderer.setSize(width, height, false);
    drawWoodenFrame(woodenFrame, width, height, renderer.getPixelRatio());
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
    layout();
    if (complete) { pose(1); positionResult(); }
    render();
  }
  native.DiceScene = { receive(message) {
    if (failed) return;
    try {
      const next = JSON.parse(message) as Configuration;
      if (next.request && (!next.request.values.length || next.request.values.length > 5 || next.request.values.some(v => !Number.isInteger(v) || v < 1 || v > 6))) throw new Error('Invalid dice');
      const isNew = next.request?.presentationId !== currentId;
      config = next;
      scene.background = new THREE.Color(config.background);
      if (!next.request) {
        stopFrame();
        currentId = undefined;
        startedAt = null;
        complete = true;
        result.hidden = true;
        rebuild(null);
        render();
        return;
      }
      if (isNew) {
        stopFrame();
        result.hidden = true;
        currentId = next.request?.presentationId;
        complete = false;
        startedAt = null;
        rebuild(next.request);
        if (next.request?.animate && config.active && !matchMedia('(prefers-reduced-motion: reduce)').matches) {
          send('rollStarted', currentId);
          frame = requestAnimationFrame(tick);
        } else finish();
      } else {
        layout();
        if (!config.active || !next.request?.animate) finish();
        else if (complete) { pose(1); render(); positionResult(); }
      }
    } catch (error) { console.error('Dice scene update failed', error); failed = true; stopFrame(); send('error', currentId); }
  } };
  canvas.addEventListener('webglcontextlost', event => {
    event.preventDefault(); failed = true; stopFrame(); send('error', currentId);
  });
  window.addEventListener('error', () => { failed = true; stopFrame(); send('error', currentId); });
  document.addEventListener('visibilitychange', () => { if (document.hidden) finish(); });
  new ResizeObserver(resize).observe(canvas);
  resize();
  send('ready');
  // The browser-only harness exercises the same bridge as the native app.
  if (!native.DiceNative && !native.webkit?.messageHandlers?.diceScene) {
    native.DiceScene.receive(JSON.stringify({ ...config, request: { presentationId: 'preview', values: [1, 2, 3, 4, 5], scoringIndices: [0, 4], scoreText: '15 очков', animate: false } }));
  }
} catch (error) {
  console.error('Dice scene initialization failed', error);
  failed = true;
  send('error', currentId);
}
