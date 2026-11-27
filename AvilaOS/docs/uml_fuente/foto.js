const { chromium } = require('playwright');
const fs = require('fs');
(async () => {
  const [spec, out, dpr] = process.argv.slice(2);
  const html = `<!doctype html><html><head><meta charset="utf-8"><style>${fs.readFileSync(__dirname+'/base.css','utf8')}</style></head>
  <body><div id="lienzo"><svg id="flechas"></svg></div>
  <script>${fs.readFileSync(__dirname+'/'+spec,'utf8')}</script><script>${fs.readFileSync(__dirname+'/render.js','utf8')}</script>
  <script>dibujar(window.SPEC)</script></body></html>`;
  const b = await chromium.launch();
  const p = await b.newPage({ deviceScaleFactor: Number(dpr || 1), viewport: { width: 1300, height: 1400 } });
  p.on('pageerror', e => console.log('ERR', e.message));
  await p.setContent(html);
  const r = await p.evaluate(() => ({ av: window.__avisos, al: window.__alturas }));
  console.log(JSON.stringify(r));
  await p.locator('#lienzo').screenshot({ path: out });
  await b.close();
})();
