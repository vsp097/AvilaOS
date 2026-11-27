// Dibuja un diagrama de clases estilo draw.io a partir de un spec (cajas + relaciones).
function dibujar(spec) {
  const lienzo = document.getElementById('lienzo');
  lienzo.style.width = spec.ancho + 'px';
  lienzo.style.height = spec.alto + 'px';
  const cajas = {};
  for (const c of spec.clases) {
    const d = document.createElement('div');
    d.className = 'clase ' + (c.tipo || 'clase');
    d.style.left = c.x + 'px';
    d.style.top = c.y + 'px';
    d.style.width = c.w + 'px';
    let h = '<div class="cab">';
    if (c.tipo === 'interfaz') h += '<div class="est">«interface»</div>';
    if (c.tipo === 'enum') h += '<div class="est">«enumeration»</div>';
    if (c.tipo === 'abstracta') h += '<div class="est">«thread»</div>';
    h += '<div class="nom">' + c.n + '</div>' + (c.sub ? '<div class="sub">' + c.sub + '</div>' : '') + '</div>';
    if (c.a && c.a.length) h += '<div class="sec">' + c.a.map(l => '<div>' + esc(l) + '</div>').join('') + '</div>';
    if (c.m && c.m.length) h += '<div class="sec">' + c.m.map(l => '<div>' + esc(l) + '</div>').join('') + '</div>';
    d.innerHTML = h;
    lienzo.appendChild(d);
    cajas[c.id] = d;
  }
  if (spec.titulo) {
    const t = document.createElement('div');
    t.className = 'titulo'; t.textContent = spec.titulo; lienzo.appendChild(t);
  }
  if (spec.leyenda) {
    const l = document.createElement('div');
    l.className = 'leyenda'; l.style.left = spec.leyenda.x + 'px'; l.style.top = spec.leyenda.y + 'px';
    l.innerHTML = spec.leyenda.html; lienzo.appendChild(l);
  }
  const svg = document.getElementById('flechas');
  svg.setAttribute('width', spec.ancho); svg.setAttribute('height', spec.alto);
  const base = lienzo.getBoundingClientRect();
  function caja(id) {
    const r = cajas[id].getBoundingClientRect();
    return { x: r.left - base.left, y: r.top - base.top, w: r.width, h: r.height };
  }
  function punto(b, lado, f) {
    f = f === undefined ? 0.5 : f;
    if (lado === 'T') return [b.x + b.w * f, b.y];
    if (lado === 'B') return [b.x + b.w * f, b.y + b.h];
    if (lado === 'L') return [b.x, b.y + b.h * f];
    return [b.x + b.w, b.y + b.h * f];
  }
  let html = '';
  for (const r of spec.rel) {
    const a = caja(r.de), b = caja(r.a);
    const p = punto(a, r.s, r.sf), q = punto(b, r.t, r.tf);
    if (r.recto) { if (r.s === 'L' || r.s === 'R') p[1] = q[1]; else p[0] = q[0]; }
    let pts = [p];
    const vert = s => s === 'T' || s === 'B';
    if (r.via === 'LAZO') { pts.push([p[0] + 38, p[1]], [p[0] + 38, q[1]]); }
    else if (r.via) { for (const v of r.via) pts.push(v); }
    else if ((vert(r.s) && vert(r.t) && Math.abs(p[0] - q[0]) < 0.5) || (!vert(r.s) && !vert(r.t) && Math.abs(p[1] - q[1]) < 0.5)) { }
    else if (vert(r.s) && vert(r.t)) {
      const my = r.my !== undefined ? r.my : (p[1] + q[1]) / 2;
      pts.push([p[0], my], [q[0], my]);
    } else if (!vert(r.s) && !vert(r.t)) {
      const mx = r.mx !== undefined ? r.mx : (p[0] + q[0]) / 2;
      pts.push([mx, p[1]], [mx, q[1]]);
    } else if (vert(r.s)) { pts.push([p[0], q[1]]); }
    else { pts.push([q[0], p[1]]); }
    pts.push(q);
    // quitar puntos repetidos
    pts = pts.filter((v, i) => i === 0 || Math.abs(v[0] - pts[i - 1][0]) > 0.5 || Math.abs(v[1] - pts[i - 1][1]) > 0.5);
    const d = 'M' + pts.map(v => v[0].toFixed(1) + ' ' + v[1].toFixed(1)).join(' L');
    const tipo = r.k;
    const dash = (tipo === 'impl' || tipo === 'dep') ? ' stroke-dasharray="7 5"' : '';
    const fin = { her: 'tri', impl: 'tri', asoc: 'flecha', dep: 'flecha', comp: 'flecha', agr: 'flecha' }[tipo];
    const ini = { comp: 'rombo', agr: 'romboh' }[tipo];
    html += '<path d="' + d + '" fill="none" stroke="#333" stroke-width="1.6"' + dash +
      (fin ? ' marker-end="url(#' + fin + ')"' : '') + (ini ? ' marker-start="url(#' + ini + ')"' : '') + '/>';
    if (r.l) {
      let i = 0, mejor = -1;
      for (let k = 0; k < pts.length - 1; k++) { const L = Math.abs(pts[k][0] - pts[k + 1][0]) + Math.abs(pts[k][1] - pts[k + 1][1]); if (L > mejor) { mejor = L; i = k; } }
      let lx = (pts[i][0] + pts[i + 1][0]) / 2, ly = (pts[i][1] + pts[i + 1][1]) / 2;
      if (Math.abs(pts[i][1] - pts[i + 1][1]) < 0.5) ly -= 10;
      if (r.lp) { lx = r.lp[0]; ly = r.lp[1]; }
      html += '<text x="' + lx.toFixed(1) + '" y="' + (ly + 4).toFixed(1) + '" text-anchor="middle" font-family="Helvetica,Arial,sans-serif" font-size="12" font-style="italic" fill="#222" stroke="#fff" stroke-width="4" paint-order="stroke">' + esc(r.l) + '</text>';
    }
  }
  svg.innerHTML = '<defs>' +
    '<marker id="tri" viewBox="0 0 14 14" refX="13" refY="7" markerWidth="14" markerHeight="14" markerUnits="userSpaceOnUse" orient="auto"><path d="M1 1 L13 7 L1 13 Z" fill="#fff" stroke="#333" stroke-width="1.5"/></marker>' +
    '<marker id="flecha" viewBox="0 0 12 12" refX="11" refY="6" markerWidth="12" markerHeight="12" markerUnits="userSpaceOnUse" orient="auto"><path d="M1 1 L11 6 L1 11" fill="none" stroke="#333" stroke-width="1.6"/></marker>' +
    '<marker id="rombo" viewBox="0 0 20 12" refX="1" refY="6" markerWidth="20" markerHeight="12" markerUnits="userSpaceOnUse" orient="auto"><path d="M1 6 L10 1 L19 6 L10 11 Z" fill="#333"/></marker>' +
    '<marker id="romboh" viewBox="0 0 20 12" refX="1" refY="6" markerWidth="20" markerHeight="12" markerUnits="userSpaceOnUse" orient="auto"><path d="M1 6 L10 1 L19 6 L10 11 Z" fill="#fff" stroke="#333" stroke-width="1.4"/></marker>' +
    '</defs>' + html;
  // avisar si una caja se sale del lienzo o se solapa con otra
  const ids = Object.keys(cajas), avisos = [];
  for (let i = 0; i < ids.length; i++) {
    const A = caja(ids[i]);
    if (A.x + A.w > spec.ancho || A.y + A.h > spec.alto) avisos.push('fuera: ' + ids[i]);
    for (let j = i + 1; j < ids.length; j++) {
      const B = caja(ids[j]);
      if (A.x < B.x + B.w && B.x < A.x + A.w && A.y < B.y + B.h && B.y < A.y + A.h) avisos.push('solapa: ' + ids[i] + ' / ' + ids[j]);
    }
  }
  const alturas = {};
  for (const id of ids) { const B = caja(id); alturas[id] = [Math.round(B.y), Math.round(B.y + B.h)]; }
  window.__avisos = avisos; window.__alturas = alturas;
}
function esc(s) { return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }
