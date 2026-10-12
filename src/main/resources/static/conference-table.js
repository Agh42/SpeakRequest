/* Shared presentation for the server's stable seat roster. No seating history lives in the browser. */
(() => {
  const views = new WeakMap();
  const palette = ['#a6b8ee', '#e1a4b1', '#b9c68d', '#d9b68c', '#a99ed5', '#86bcc8', '#cf9bca', '#b0c9da', '#cfb1a1', '#8ebca8'];
  let tooltipId = 0;
  const torso = '<ellipse cx="24" cy="48" rx="19" ry="6" fill="#071020" opacity=".5"/><path d="M8 35Q8 23 24 23T40 35V44Q24 50 8 44Z" fill="currentColor"/><path d="M29 24Q40 26 40 35V44L32 46V33Z" fill="#071020" opacity=".2"/><circle cx="24" cy="13" r="10" fill="currentColor"/><path d="M10 44Q24 50 38 44" fill="none" stroke="#d7e5ff" stroke-opacity=".25" stroke-width="2"/>';
  const emptyChair = '<path d="M12 43V17Q12 12 17 12H31Q36 12 36 17V43M9 37Q24 32 39 37V43Q24 48 9 43ZM13 45V51M35 45V51" fill="none" stroke="currentColor" stroke-width="4" stroke-linejoin="round"/>';
  const arrow = '<svg viewBox="0 0 20 22" aria-hidden="true"><path d="M6 1H14V11H19L10 21L1 11H6Z" fill="currentColor" stroke="#072918" stroke-width="1.5" stroke-linejoin="round"/></svg>';

  function color(id) {
    let hash = 2166136261;
    for (const char of id) hash = Math.imul(hash ^ char.codePointAt(0), 16777619);
    return palette[(hash >>> 0) % palette.length];
  }

  function cleanName(value) {
    return DOMPurify.sanitize(String(value || ''), { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim().replace(/\s+/g, ' ');
  }

  function speakerIdentity(entry, state) {
    // Proxy queue entries get a new request ID each turn; their member ID is stable.
    if (entry.id === state.current?.entry.id) {
      const speaking = state.conferenceTable?.seats.find(seat => seat.speaking);
      if (speaking?.member) return speaking.member.id;
    }
    const members = state.members || [];
    const member = members.find(m => m.id === entry.id) || members.find(m =>
      entry.id.startsWith(m.sessionId + ':proxy:') && m.name.toLowerCase() === entry.name.toLowerCase());
    if (member) return member.id;
    const matches = members.filter(m => m.name.toLowerCase() === entry.name.toLowerCase());
    return matches.length === 1 ? matches[0].id : entry.id;
  }

  function avatar(entry, state) {
    const id = speakerIdentity(entry, state);
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('viewBox', '0 0 48 54');
    svg.setAttribute('aria-hidden', 'true');
    svg.classList.add('conference-table-avatar');
    svg.dataset.memberId = id;
    svg.style.color = color(id);
    svg.innerHTML = torso;
    return svg;
  }

  function speakerRow(entry, state, label, tag = 'div') {
    const row = document.createElement(tag);
    row.className = 'speaker-queue-row';
    const name = document.createElement('span');
    name.className = 'speaker-queue-name';
    name.textContent = cleanName(entry.name);
    name.title = name.textContent;
    const badge = document.createElement('span');
    badge.className = 'speaker-queue-badge';
    badge.textContent = label;
    row.append(avatar(entry, state), name, badge);
    return row;
  }

  function renderQueue(host, state) {
    const tag = host.matches('ul, ol') ? 'li' : 'div';
    const rows = state.queue.map((entry, index) => speakerRow(entry, state, `#${index + 1}`, tag));
    if (!rows.length) {
      const empty = document.createElement(tag);
      empty.className = 'speaker-queue-empty';
      empty.textContent = 'Queue is empty.';
      rows.push(empty);
    }
    host.replaceChildren(...rows);
  }

  function renderCurrent(host, state) {
    if (!state.current) {
      host.textContent = 'No active speaker.';
      return;
    }
    const label = document.createElement('div');
    label.className = 'speaker-current-label';
    label.textContent = 'Current speaker';
    const row = speakerRow(state.current.entry, state, state.current.running ? 'Live' : 'Paused');
    host.replaceChildren(label, row);
  }

  function shortName(name) {
    const chars = Array.from(name);
    return chars.length <= 8 ? name : chars.slice(0, 6).join('') + '…';
  }

  function positionTooltip(node, scroll) {
    const tip = node.querySelector('.conference-table-tooltip');
    tip.style.marginLeft = '0px';
    tip.style.top = '';
    tip.style.bottom = '';
    const bounds = scroll.getBoundingClientRect();
    const rect = tip.getBoundingClientRect();
    const shift = Math.max(bounds.left + 8 - rect.left, Math.min(0, bounds.right - 8 - rect.right));
    tip.style.marginLeft = `${shift}px`;
    if (rect.top < bounds.top + 8) {
      tip.style.bottom = 'auto';
      tip.style.top = 'calc(100% + 6px)';
    }
  }

  function layout(count, available) {
    // Equal angles on a circle, projected at 30 degrees into an isometric ellipse.
    // Include label and arrow clearance when sizing the scene for dense attendance.
    let minimum = 480;
    const points = width => Array.from({ length: count }, (_, i) => {
      const angle = -Math.PI / 2 + i * Math.PI * 2 / count;
      const rx = width / 2 - 60;
      return { x: rx * Math.cos(angle), y: rx * Math.sin(angle) / Math.sqrt(3) };
    });
    while (minimum < 1800) {
      const p = points(minimum);
      const collision = p.some((a, i) => p.slice(i + 1).some(b => Math.abs(a.x - b.x) < 70 && Math.abs(a.y - b.y) < 90));
      if (!collision) break;
      minimum += 20;
    }
    const width = Math.max(minimum, Math.min(available, Math.max(640, minimum)));
    const height = (width - 120) / Math.sqrt(3) + 190;
    return { width, height, points: points(width).map(p => ({ x: p.x + width / 2, y: p.y + height / 2 })) };
  }

  function draw(view) {
    const geometry = layout(view.seats.length, view.scroll.clientWidth);
    const { width, height, points } = geometry;
    view.shell.style.width = `${width}px`;
    view.shell.style.height = `${height}px`;
    const cx = width / 2, cy = height / 2, rx = width / 2 - 82, ry = (width / 2 - 60) / Math.sqrt(3) - 18;
    view.art.setAttribute('viewBox', `0 0 ${width} ${height}`);
    view.art.innerHTML = `<ellipse cx="${cx}" cy="${cy + 46}" rx="${rx}" ry="${ry}" fill="#060e1a" opacity=".55"/>
      <path d="M${cx - rx * .6} ${cy}v58m${rx * 1.2} -58v58" stroke="#263a4c" stroke-width="16"/>
      <ellipse cx="${cx}" cy="${cy + 18}" rx="${rx}" ry="${ry}" fill="#162d3c" stroke="#3a5264" stroke-width="2"/>
      <ellipse cx="${cx}" cy="${cy}" rx="${rx}" ry="${ry}" fill="#263e50" stroke="#617b8e" stroke-width="2"/>
      <ellipse cx="${cx}" cy="${cy}" rx="${rx - 10}" ry="${ry - 8}" fill="none" stroke="#9ebbd0" stroke-opacity=".16"/>`;
    points.forEach((p, i) => {
      const node = view.nodes.get(view.seats[i].member?.id || 'empty-chair');
      node.style.left = `${p.x}px`;
      node.style.top = `${p.y}px`;
      node.style.zIndex = String(Math.round(p.y));
    });
    if (view.sceneWidth !== width || view.viewportWidth !== view.scroll.clientWidth) {
      view.sceneWidth = width;
      view.viewportWidth = view.scroll.clientWidth;
      centerFloor(view);
    }
  }

  function centerFloor(view) {
    const node = view.nodes.get(view.floorId);
    if (node) view.scroll.scrollLeft = node.offsetLeft - view.scroll.clientWidth / 2;
  }

  function create(host) {
    const shell = host.closest('.conference-table-shell');
    const panel = host.closest('.conference-panel');
    const art = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    art.classList.add('conference-table-art');
    art.setAttribute('aria-hidden', 'true');
    shell.prepend(art);
    const view = { host, shell, art, scroll: shell.parentElement, panel, nodes: new Map(), seats: [] };
    view.observer = new ResizeObserver(() => { if (view.seats.length) draw(view); });
    view.observer.observe(view.scroll);
    views.set(host, view);
    return view;
  }

  function render(host, state) {
    if (!host || !state.conferenceTable) return;
    const view = views.get(host) || create(host);
    const seats = state.conferenceTable.seats;
    const retained = new Set();
    let floorName = 'Chair has the floor';
    seats.forEach(seat => {
      const id = seat.member?.id || 'empty-chair';
      retained.add(id);
      let node = view.nodes.get(id);
      if (!node) {
        node = document.createElement('button');
        node.type = 'button';
        node.className = 'conference-table-seat';
        node.dataset.memberId = id;
        // Only fixed artwork is inserted as HTML. Names are assigned through text/attribute APIs.
        node.innerHTML = `<span class="conference-table-arrow" hidden>${arrow}</span><svg class="conference-table-avatar" viewBox="0 0 48 54" aria-hidden="true">${seat.member ? torso : emptyChair}</svg><span class="conference-table-label"></span><span class="conference-table-tooltip" role="tooltip"></span>`;
        const tip = node.querySelector('.conference-table-tooltip');
        tip.id = `conference-person-${++tooltipId}`;
        node.setAttribute('aria-describedby', tip.id);
        node.addEventListener('mouseenter', () => positionTooltip(node, view.scroll));
        node.addEventListener('focus', () => positionTooltip(node, view.scroll));
        view.host.append(node);
        view.nodes.set(id, node);
      }
      const fullName = cleanName(seat.member?.name) || 'Chair (vacant)';
      const marked = seat.speaking || (!state.current && seat.chair);
      node.dataset.empty = String(!seat.member);
      node.dataset.queued = String(seat.queued && !seat.speaking);
      node.dataset.speaking = String(seat.speaking);
      node.dataset.chair = String(seat.chair);
      node.querySelector('.conference-table-avatar').style.color = color(id);
      node.querySelector('.conference-table-label').textContent = seat.chair ? 'Chair' : shortName(fullName);
      node.querySelector('.conference-table-tooltip').textContent = fullName;
      node.querySelector('.conference-table-arrow').hidden = !marked;
      node.title = fullName;
      node.setAttribute('aria-label', `${fullName}${seat.chair && seat.member ? ', chair' : ''}${seat.speaking ? ', speaking' : seat.queued ? ', waiting to speak' : ''}${!state.current && seat.chair ? ', has the floor' : ''}`);
      if (seat.speaking) floorName = `Speaking: ${fullName}`;
    });
    for (const [id, node] of view.nodes) {
      if (!retained.has(id)) { node.remove(); view.nodes.delete(id); }
    }
    view.seats = seats;
    const overflow = view.panel.querySelector('.conference-overflow');
    const count = state.conferenceTable.overflowCount;
    overflow.hidden = count === 0;
    overflow.textContent = count ? `+ ${count} more` : '';
    const status = view.panel.querySelector('.conference-floor-status');
    if (status.textContent !== floorName) status.textContent = floorName;
    draw(view);
    const floorSeat = seats.find(s => s.speaking || (!state.current && s.chair));
    const floorId = floorSeat?.member?.id || 'empty-chair';
    if (view.floorId !== floorId) {
      view.floorId = floorId;
      centerFloor(view);
    }
  }
  window.ConferenceTableView = { render };
  window.SpeakerView = { avatar, renderQueue, renderCurrent };
})();
