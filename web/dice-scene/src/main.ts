import * as THREE from 'three';
import { RoundedBoxGeometry } from 'three/addons/geometries/RoundedBoxGeometry.js';
import { faceNormals, orientationFor } from './orientation';
import { createFeltMaterial, drawWoodenFrame } from './table';

type Request = { presentationId: string; values: number[]; animate: boolean; scoringIndices?: number[] };
type Bounds = { left: number; top: number; right: number; bottom: number };
type Configuration = { request: Request | null; bounds: Bounds; background: string; active: boolean };
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
    const { left, top, right, bottom } = config.bounds;
    const width = Math.max(.01, right - left), height = Math.max(.01, bottom - top);
    const columns = Math.min(dice.length, 3);
    const rows = Math.ceil(dice.length / Math.max(columns, 1));
    const cx = (left + right) / 2, cy = (top + bottom) / 2;
    const worldWidth = worldPoint(right, cy).distanceTo(worldPoint(left, cy));
    const worldHeight = worldPoint(cx, bottom).distanceTo(worldPoint(cx, top));
    scale = Math.min(1.15, worldWidth / Math.max(columns, 1) / 1.7, worldHeight / Math.max(rows, 1) / 2.1);
    targets = dice.map((_, i) => {
      const row = Math.floor(i / columns);
      const inRow = Math.min(columns, dice.length - row * columns);
      const x = cx + ((i % columns) - (inRow - 1) / 2) * width / columns * .82;
      const y = cy + (row - (rows - 1) / 2) * height / Math.max(rows, 1) * .7;
      const point = worldPoint(x, y);
      point.y = scale * .51;
      return point;
    });
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
  function stopFrame() { cancelAnimationFrame(frame); frame = 0; }
  function finish() {
    stopFrame();
    pose(1);
    halos.forEach(halo => { halo.visible = true; });
    render();
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
    if (complete) pose(1);
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
      if (isNew) {
        stopFrame();
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
        else if (complete) { pose(1); render(); }
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
    native.DiceScene.receive(JSON.stringify({ ...config, request: { presentationId: 'preview', values: [1, 2, 3, 4, 5], scoringIndices: [0, 4], animate: false } }));
  }
} catch (error) {
  console.error('Dice scene initialization failed', error);
  failed = true;
  send('error', currentId);
}
