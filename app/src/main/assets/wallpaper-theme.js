(() => {
  if (window.__contactWallpaperRefresh) { window.__contactWallpaperRefresh(); return; }
  const style = document.createElement('style');
  style.id = 'contact-wallpaper-style';
  style.textContent = `html[data-contact-wallpaper],html[data-contact-wallpaper] body,
    html[data-contact-wallpaper] main,html[data-contact-wallpaper] [role="main"],
    html[data-contact-wallpaper] [data-contact-surface]{background:transparent!important;border-color:transparent!important;box-shadow:none!important}
    html[data-contact-wallpaper] [data-contact-white-before]::before,
    html[data-contact-wallpaper] [data-contact-white-after]::after{background:transparent!important;border-color:transparent!important;box-shadow:none!important}
    html[data-contact-wallpaper] body{color:#202228!important;color-scheme:normal!important}
    html[data-contact-wallpaper] [role="article"],html[data-contact-wallpaper] [role="article"] *{--mwp-message-row-background:transparent!important}
    html[data-contact-wallpaper] [role="article"]{background:transparent!important}
    html[data-contact-wallpaper] input,html[data-contact-wallpaper] textarea{background:rgba(255,255,255,.92)!important}`;
  (document.head || document.documentElement).appendChild(style);
  const cardStyle=document.createElement('style');cardStyle.id='contact-readability-style';(document.head||document.documentElement).appendChild(cardStyle);
  function cardTheme(){
    const cfg=window.__contactCardConfig||{},dark=!!cfg.dark,tone=cfg.tone===1;
    document.documentElement.toggleAttribute('data-contact-dark',dark && /^\/direct(?:\/|$)/.test(location.pathname));
    document.documentElement.toggleAttribute('data-contact-light',!dark && /^\/direct(?:\/|$)/.test(location.pathname));
    document.documentElement.toggleAttribute('data-contact-cards',cfg.enabled!==false && /^\/direct(?:\/|$)/.test(location.pathname));
    const opacity=Math.max(0,Math.min(100,Number(cfg.opacity ?? 78)))/100;
    const radius=Math.max(0,Math.min(32,Number(cfg.radius ?? 18)));
    cardStyle.textContent=`html[data-contact-light],html[data-contact-light] body,html[data-contact-light] div,html[data-contact-light] span{--ig-primary-background:255,255,255;--ig-highlight-background:239,239,239;--ig-elevated-background:255,255,255;--ig-primary-text:28,30,34;--ig-secondary-text:115,115,115;--ig-separator:219,219,219;--ig-incoming-message-bubble:239,239,239}
      html[data-contact-light] body{color:#1c1e22!important}
      html[data-contact-dark],html[data-contact-dark] body,html[data-contact-dark] div,html[data-contact-dark] span{--ig-primary-background:18,18,18;--ig-highlight-background:38,38,38;--ig-elevated-background:38,38,38;--ig-primary-text:245,245,245;--ig-secondary-text:168,168,168;--ig-separator:55,55,55;--ig-incoming-message-bubble:38,38,38}
      html[data-contact-dark] body{color:#f5f5f5!important}
      html[data-contact-dark] input,html[data-contact-dark] textarea{background:#262626!important;color:#f5f5f5!important}
      html[data-contact-light] [data-contact-search]{background:#efefef!important;border-radius:12px!important}
      html[data-contact-dark] [data-contact-search]{background:#262626!important;border-radius:12px!important;color:#a8a8a8!important;box-shadow:none!important}
      html[data-contact-light] [data-contact-search] div,html[data-contact-light] [data-contact-search] input,html[data-contact-dark] [data-contact-search] div,html[data-contact-dark] [data-contact-search] input{background:transparent!important;box-shadow:none!important}
      html[data-contact-light] [data-contact-search] *,html[data-contact-dark] [data-contact-search] *{background-color:transparent!important;background-image:none!important;box-shadow:none!important}
      html[data-contact-light] [data-contact-search]::before,html[data-contact-light] [data-contact-search]::after,html[data-contact-dark] [data-contact-search]::before,html[data-contact-dark] [data-contact-search]::after,html [data-contact-search] *::before,html [data-contact-search] *::after{background:transparent!important;box-shadow:none!important}
      html [data-contact-search] input{color:#1c1e22!important}
      html[data-contact-dark] [data-contact-search] input{color:#f5f5f5!important}
      html[data-contact-cards] [data-contact-text-card]{background:rgba(${tone?'24,24,28':'255,255,255'},${opacity})!important;border-radius:${radius}px!important;box-shadow:none!important;color:${tone?'#f5f5f5':'#1c1e22'}!important;display:inline-block!important;width:fit-content!important;max-width:100%!important;box-sizing:border-box!important;padding:2px 6px!important;box-decoration-break:clone;-webkit-box-decoration-break:clone}`;
  }
  function markTextCards(row){
    row.removeAttribute('data-contact-card');
    row.setAttribute('data-contact-card-row','');
    for(const span of row.querySelectorAll('span')){
      if(span.children.length || !Array.from(span.childNodes).some(n=>n.nodeType===3) || span.closest('button,svg'))continue;
      const r=span.getBoundingClientRect();
      if(r.width>2 && r.height>=8 && r.height<=48)span.setAttribute('data-contact-text-card','');
    }
  }
  function markCard(img){
    const inbox=/^\/direct\/(?:inbox\/?|)$/.test(location.pathname);
    let candidate=null;
    for(let el=img.parentElement,depth=0;el && depth++<7;el=el.parentElement){
      const r=el.getBoundingClientRect();
      if(r.height>150)break;
      if(r.width<innerWidth*.65 || r.height<48 || el.querySelector('input,textarea,[contenteditable="true"]'))continue;
      if(inbox || r.top<90)candidate=el;
      if(el.matches('a,[role="button"],[role="row"]') && candidate)break;
    }
    if(candidate)markTextCards(candidate);
  }
  let active = false, photoActive = false, scheduled = false;
  const pending = new Set(), roots = [];
  let seen = new WeakSet();
  const inlineOverrides = new Map();
  let walker = null;
  const later = fn => window.requestIdleCallback ? requestIdleCallback(fn, {timeout:250}) : setTimeout(fn,32);
  function color(value) {
    const match=(value || '').match(/^rgba?\((\d+)[,\s]+(\d+)[,\s]+(\d+)(?:[,\s\/]+([\d.]+))?\)$/);
    return match ? [+match[1],+match[2],+match[3],match[4] == null ? 1 : +match[4]] : null;
  }
  function white(value) { const c=color(value); return c && Math.min(c[0],c[1],c[2])>=235 && c[3]>=.9; }
  function rounded(css) { return [css.borderTopLeftRadius,css.borderTopRightRadius,css.borderBottomLeftRadius,css.borderBottomRightRadius].some(r=>parseFloat(r)>0); }
  function coloredBubble(el) {
    const css=getComputedStyle(el), c=color(css.backgroundColor);
    return rounded(css) && c && c[3]>.5 && Math.max(c[0],c[1],c[2])-Math.min(c[0],c[1],c[2])>20;
  }
  function restoreInline(el) {
    const saved=inlineOverrides.get(el); if(!saved) return;
    for(const [key,value,priority] of saved) {
      if(el.style.getPropertyValue(key)==='transparent' && el.style.getPropertyPriority(key)==='important') {
        if(value) el.style.setProperty(key,value,priority); else el.style.removeProperty(key);
      }
    }
    inlineOverrides.delete(el);
    seen.delete(el);
  }
  function markSearch(el) {
    if(el.tagName==='INPUT' && /^\/direct\/(?:inbox\/?|)$/.test(location.pathname)){
      let box=el.closest('[role="search"]')||el.parentElement;
      // Include icon and end caps, not just the inner input wrapper.
      for(let parent=el.parentElement,depth=0;parent && depth++<6;parent=parent.parentElement){
        const r=parent.getBoundingClientRect();
        if(r.height>64)break;
        if(r.height>=32 && r.width>=el.getBoundingClientRect().width && !parent.querySelector('textarea,[contenteditable="true"]'))box=parent;
      }
      if(box)box.setAttribute('data-contact-search','');
    }
  }
  function inspect(el) {
    if (seen.has(el)) return;
    seen.add(el);
    markSearch(el);
    if(el.tagName==='IMG')markCard(el);
    if(el.tagName==='SPAN' && el.closest && el.closest('[data-contact-card-row]'))markTextCards(el.closest('[data-contact-card-row]'));
    if(el.closest && el.closest('[data-contact-search]'))return;
    if(!window.__contactWallpaper)return;
    if (!el.matches('div,span,section,header,nav')) return;
    // Instagram also gives message wrappers role=button; keep those eligible.
    if (el.closest('[role="dialog"],button,form,[contenteditable="true"]')) return;
    const css = getComputedStyle(el);
    // White pseudo-elements can also extend outside a colored rounded bubble.
    const c=color(css.backgroundColor);
    if(rounded(css) && c && Math.max(c[0],c[1],c[2])-Math.min(c[0],c[1],c[2])>20) {
      for(const side of ['before','after']) {
        const pseudo=getComputedStyle(el,'::'+side);
        if(pseudo.content && !['none','normal'].includes(pseudo.content) && white(pseudo.backgroundColor) && !rounded(pseudo) && pseudo.backgroundImage==='none') el.setAttribute('data-contact-white-'+side,'');
      }
    }
    if (!white(css.backgroundColor)) return;
    // Some wrappers have a corner radius too: recognize the blue bubble inside them.
    if (rounded(css) && !Array.from(el.children).some(coloredBubble)) return;
    const image=css.backgroundImage || 'none';
    if (image!=='none') {
      const stops=image.match(/rgba?\([^)]*\)/g);
      if(!image.startsWith('linear-gradient(') || !stops || !stops.every(white)) return;
    }
    el.setAttribute('data-contact-surface','');
    // Stylesheets cannot beat an inline !important background. Override only this case.
    if(el.style && ['background','background-color'].some(key=>el.style.getPropertyPriority(key)==='important')) {
      const saved=['background','background-color'].map(key=>[key,el.style.getPropertyValue(key),el.style.getPropertyPriority(key)]);
      inlineOverrides.set(el,saved);
      el.style.setProperty('background','transparent','important'); el.style.setProperty('background-color','transparent','important');
    }
  }
  function drain() {
    scheduled = false;
    if (!active) { pending.clear(); roots.length=0; walker=null; return; }
    let budget = 100;
    while (budget-- > 0) {
      if (pending.size) { const el=pending.values().next().value; pending.delete(el); if(el.isConnected) inspect(el); continue; }
      if (!walker) {
        const root=roots.shift(); if(!root) break; if(!root.isConnected) continue;
        inspect(root); walker=document.createTreeWalker(root,NodeFilter.SHOW_ELEMENT);
      }
      const el=walker.nextNode(); if(el) inspect(el); else walker=null;
    }
    if(pending.size || roots.length || walker) schedule();
  }
  function schedule() { if(!scheduled && active) { scheduled=true; later(drain); } }
  function refresh() {
    cardTheme();
    for(const input of document.documentElement.querySelectorAll('input'))markSearch(input);
    const direct=/^\/direct(?:\/|$)/.test(location.pathname);
    const photo=!!window.__contactWallpaper && direct;
    const next=direct;
    document.documentElement.toggleAttribute('data-contact-wallpaper',photo);
    if(next && (!active || photo!==photoActive)) {seen=new WeakSet();roots.push(document.documentElement);}
    if(!photo && photoActive) for(const el of inlineOverrides.keys()) restoreInline(el);
    photoActive=photo;
    active=next; schedule();
  }
  window.__contactWallpaperRefresh=refresh;
  new MutationObserver(records => {
    if(!active) return;
    for(const record of records) {
      if(record.type==='attributes') {
        const el=record.target;
        if(record.attributeName==='style' && inlineOverrides.has(el) && el.style.getPropertyValue('background-color')==='transparent' && el.style.getPropertyValue('background')==='transparent') continue;
        restoreInline(el); seen.delete(el); el.removeAttribute('data-contact-surface'); el.removeAttribute('data-contact-white-before'); el.removeAttribute('data-contact-white-after'); pending.add(el);
      } else for(const node of record.addedNodes) if(node.nodeType===1) {
        markSearch(node); for(const input of node.querySelectorAll('input'))markSearch(input); roots.push(node);
      }
    }
    schedule();
  }).observe(document.documentElement,{subtree:true,childList:true,attributes:true,attributeFilter:['class','style']});
  window.addEventListener('popstate',refresh);
  window.addEventListener('contact-only-route',refresh);
  refresh();
})();
