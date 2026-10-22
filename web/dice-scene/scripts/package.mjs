import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { resolve, dirname } from 'node:path';
// Vite emits one entry with no dynamic imports. Inline it so WKWebView needs no file access.
let html = await readFile('dist/index.html', 'utf8');
const script = html.match(/<script\b[^>]*src="([^"]+)"[^>]*><\/script>/);
if (!script) throw new Error('Expected a single Vite entry');
const js = await readFile(resolve('dist', script[1].replace(/^\//, '')), 'utf8');
html = html.replace(script[0], () => `<script type="module">${js.replace(/<\/script/gi, '<\\/script')}</script>`);
if (/<(?:script|link)\b[^>]*(?:src|href)=/.test(html)) throw new Error('Unbundled dependency');
html = html.replace('<head>', '<head><meta http-equiv="Content-Security-Policy" content="default-src \'none\'; script-src \'unsafe-inline\'; style-src \'unsafe-inline\'; connect-src \'none\'">');
const output = resolve('../../composeApp/src/commonMain/composeResources/files/dice-scene/index.html');
await mkdir(dirname(output), { recursive: true });
await writeFile(output, html.replace(/[ \t]+$/gm, ''));
console.log(`Packaged ${html.length} characters into ${output}`);
