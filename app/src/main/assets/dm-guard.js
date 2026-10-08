(() => {
  if (window.__contactOnlyGuard) return;
  window.__contactOnlyGuard = true;
  const inbox = '/direct/inbox/';
  const permitted = p => /^\/direct(?:\/|$)/.test(p) || /^\/(accounts|challenge|two_factor|consent)(?:\/|$)/.test(p);
  const blocked = p => p === '/' || /^\/(explore|reels)(?:\/|$)/.test(p);
  const style = document.createElement('style');
  style.textContent = `a[href="/"],a[href="/explore/"],a[href="/reels/"],a[href="/reels"]{display:none!important}`;
  (document.head || document.documentElement).appendChild(style);
  const atInbox = () => /^\/direct\/(inbox\/?|)$/.test(location.pathname);
  function hideNotes() {
    if (!atInbox()) return;
    const noteLabel = /^(notes?|your note|leave a note|share a note|메모|내 메모|메모 남기기|메모 공유)(?:\s*[.\u2026!]*\s*)$/i;
    const matches = [...document.querySelectorAll('[aria-label],[title],button,[role="button"],span')].filter(el => {
      const label = el.getAttribute('aria-label') || el.getAttribute('title') || (el.children.length === 0 ? el.textContent : '');
      return noteLabel.test((label || '').trim());
    });
    for (const marker of matches) {
      // Hide only the compact inbox Notes rail, never a conversation or the inbox panel.
      let candidate = null;
      for (let rail = marker; rail && rail !== document.body; rail = rail.parentElement) {
        if (rail.hasAttribute('data-contact-only-notes')) break;
        const r = rail.getBoundingClientRect();
        if (r.height > 230 || r.bottom > 550) break;
        if (rail.querySelector('a[href^="/direct/t/"],input,textarea,[contenteditable="true"]')) break;
        const css = getComputedStyle(rail);
        const horizontal = ['auto', 'scroll'].includes(css.overflowX) || rail.scrollWidth > rail.clientWidth + 8;
        const avatars = rail.querySelectorAll('img').length;
        const wide = r.width >= Math.min(window.innerWidth * 0.65, 260);
        if (r.top <= 340 && r.height >= 36 && wide && (horizontal || avatars >= 2 || /^(notes|메모)$/i.test(rail.getAttribute('aria-label') || ''))) {
          candidate = rail;
        }
      }
      // Collapse the outer rail as well, so its fixed/sticky height does not leave a gap.
      if (candidate) {
        candidate.setAttribute('data-contact-only-notes', '');
        candidate.style.setProperty('display', 'none', 'important');
      }
    }
  }
  function hideInboxBack() {
    const inInbox = atInbox();
    document.querySelectorAll('[data-contact-only-back]').forEach(el => {
      if (!inInbox) {
        el.style.removeProperty('display');
        el.removeAttribute('data-contact-only-back');
      }
    });
    if (!inInbox) return;
    document.querySelectorAll('svg[aria-label],button[aria-label],[role="button"][aria-label],a[aria-label]').forEach(icon => {
      const label = (icon.getAttribute('aria-label') || '').trim();
      if (!/^(back|go back|뒤로|뒤로 가기|돌아가기|이전)$/i.test(label)) return;
      const control = icon.closest('button,[role="button"],a') || icon;
      if (control.hasAttribute('data-contact-only-back')) return;
      control.setAttribute('data-contact-only-back', '');
      control.style.setProperty('display', 'none', 'important');
    });
  }
  function guard() {
    if (!permitted(location.pathname)) location.replace(inbox);
    hideInboxBack();
    hideNotes();
  }
  // Instagram's inbox back arrow can return to a previously rendered feed.
  // Keep back navigation inside conversations working, but disable inbox history exits.
  const originalBack = history.back.bind(history);
  history.back = () => { if (!atInbox()) originalBack(); };
  const originalGo = history.go.bind(history);
  history.go = delta => { if (!(atInbox() && Number(delta) < 0)) originalGo(delta); };
  let lastInboxMutation = Date.now();
  window.__contactOnlyPrepare = () => {
    hideInboxBack(); hideNotes();
    return !atInbox() || (document.readyState === 'complete' && Date.now() - lastInboxMutation >= 400 && document.querySelectorAll('input,a[href^="/direct/t/"],[role="main"]').length > 0);
  };
  let controlsScheduled = false;
  new MutationObserver(() => {
    if (!atInbox()) return;
    lastInboxMutation = Date.now();
    if (controlsScheduled) return;
    controlsScheduled = true;
    requestAnimationFrame(() => { controlsScheduled = false; hideInboxBack(); hideNotes(); });
  }).observe(document.documentElement, {
    childList: true, subtree: true, attributes: true, attributeFilter: ['aria-label']
  });
  // Only inspect navigation URLs; never inspect message text or credentials.
  document.addEventListener('click', e => {
    if (atInbox() && e.target.closest && e.target.closest('[data-contact-only-back]')) {
      e.preventDefault(); e.stopImmediatePropagation(); return;
    }
    const a = e.target.closest && e.target.closest('a[href]');
    if (!a) return;
    const u = new URL(a.href, location.href);
    if (u.origin !== location.origin || permitted(u.pathname)) return;
    e.preventDefault(); e.stopImmediatePropagation();
    if (blocked(u.pathname)) return;
    // A full navigation passes through the native external-link confirmation.
    location.href = u.href;
  }, true);
  ['pushState', 'replaceState'].forEach(name => {
    const original = history[name];
    history[name] = function(state, title, url) {
      if (url != null) {
        const u = new URL(url, location.href);
        if (u.origin === location.origin && !permitted(u.pathname)) return;
      }
      const result = original.apply(this, arguments);
      guard(); window.dispatchEvent(new Event('contact-only-route')); return result;
    };
  });
  window.addEventListener('popstate', guard);
  let lastPath = location.pathname;
  setInterval(() => {
    if (lastPath !== location.pathname) { lastPath = location.pathname; guard(); window.dispatchEvent(new Event('contact-only-route')); }
  }, 1000);
  guard();
})();
