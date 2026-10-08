(() => {
  if(window.__contactReelRefresh){window.__contactReelRefresh();return;}
  const style=document.createElement('style');
  style.textContent=`[data-contact-reel-width]{width:100%!important;max-width:100%!important;min-width:0!important}
    [data-contact-reel-video]{object-fit:contain!important}
    [data-contact-reel-lock]{overflow-y:hidden!important;scroll-snap-type:none!important;overscroll-behavior:none!important;touch-action:none!important;scrollbar-width:none!important}
    [data-contact-reel-lock]::-webkit-scrollbar{display:none!important}
    html[data-contact-reel-returning] body{visibility:hidden!important}`;
  (document.head||document.documentElement).appendChild(style);
  let active=null,scheduled=false,gesture=null;
  let lastThread=/^\/direct\/t\//.test(location.pathname)?location.origin+location.pathname:null;
  const direct=()=>/^\/direct(?:\/|$)/.test(location.pathname);
  function release(){
    if(!active)return;
    active.root.removeEventListener('scroll',active.hold);
    active.root.removeAttribute('data-contact-reel-lock');
    active.video.removeAttribute('data-contact-reel-video');
    active.widths.forEach(el=>el.removeAttribute('data-contact-reel-width'));
    active=null;
  }
  function findRoot(video){
    for(let el=video.parentElement;el&&el!==document.body;el=el.parentElement){
      const css=getComputedStyle(el),r=el.getBoundingClientRect();
      if(css.scrollSnapType.includes('y')&&r.height>innerHeight*.5&&r.width>innerWidth*.5)return el;
    }
    return null;
  }
  function fit(){
    if(!direct()){release();return;}
    if(/^\/direct\/t\//.test(location.pathname))lastThread=location.origin+location.pathname;
    if(active&&(!active.root.isConnected||!active.video.isConnected||!active.root.contains(active.video)))release();
    if(!active){
      const candidates=[...document.querySelectorAll('video')].map(video=>{
        const r=video.getBoundingClientRect();
        return {video,area:Math.max(0,Math.min(r.bottom,innerHeight)-Math.max(r.top,0))*Math.max(0,Math.min(r.right,innerWidth)-Math.max(r.left,0))};
      }).sort((a,b)=>b.area-a.area);
      for(const {video,area} of candidates){
        if(!area)continue;
        const root=findRoot(video);if(!root)continue;
        active={root,video,widths:new Set(),position:root.scrollTop,hold:null};
        active.hold=()=>{if(active&&Math.abs(root.scrollTop-active.position)>1)root.scrollTop=active.position;};
        root.addEventListener('scroll',active.hold);
        root.setAttribute('data-contact-reel-lock','');
        video.setAttribute('data-contact-reel-video','');
        break;
      }
    }
    if(!active)return;
    for(let el=active.video.parentElement;el&&el!==active.root;el=el.parentElement){
      const r=el.getBoundingClientRect();
      if(r.height>innerHeight*.5&&r.width>innerWidth*.5&&r.width<innerWidth-2){
        el.setAttribute('data-contact-reel-width','');active.widths.add(el);
      }
    }
    active.hold();
  }
  function schedule(){if(scheduled)return;scheduled=true;requestAnimationFrame(()=>{scheduled=false;fit();});}
  function blockedTarget(target){return active&&target instanceof Element&&active.root.contains(target);}
  function returnToThread(direction){
    if(!active||active.returning)return;
    active.returning=true;
    const viewer=active.root.parentElement||active.root;
    const reduce=window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const animation=viewer.animate?viewer.animate([{transform:'translateY(0)',opacity:1},{transform:`translateY(${direction*innerHeight}px)`,opacity:0}],{duration:reduce?0:180,easing:'cubic-bezier(.3,0,.2,1)',fill:'forwards'}):null;
    const close=()=>{
      document.documentElement.setAttribute('data-contact-reel-returning','');
      setTimeout(()=>{
        if(animation)animation.cancel();
        document.documentElement.removeAttribute('data-contact-reel-returning');
        if(!reduce&&document.body.animate)document.body.animate([{opacity:0},{opacity:1}],{duration:120});
      },320);
      closeViewer();
    };
    setTimeout(close,reduce?0:180);
  }
  function closeViewer(){
    if(!active)return;
    // Hide Instagram's sideways Back transition behind the vertical exit.
    for(let scope=active.root.parentElement;scope&&scope!==document.body;scope=scope.parentElement){
      for(const icon of scope.querySelectorAll('svg[aria-label],button[aria-label],[role="button"][aria-label]')){
        if(!/^(back|go back|close|뒤로|뒤로 가기|돌아가기|이전|닫기)$/i.test((icon.getAttribute('aria-label')||'').trim()))continue;
        const control=icon.closest('button,[role="button"],a')||icon,r=control.getBoundingClientRect();
        if(r.width>0&&r.height>0){control.click();return;}
      }
    }
    if(lastThread)location.assign(lastThread);
    else {active.returning=false;history.back();}
  }
  document.addEventListener('touchstart',e=>{
    gesture=null;
    if(blockedTarget(e.target)&&e.touches.length===1)gesture={x:e.touches[0].clientX,y:e.touches[0].clientY};
  },{capture:true,passive:true});
  document.addEventListener('touchmove',e=>{
    if(!blockedTarget(e.target))return;
    e.preventDefault();
    if(!gesture||e.touches.length!==1)return;
    const dx=e.touches[0].clientX-gesture.x,dy=e.touches[0].clientY-gesture.y;
    if(Math.abs(dy)>=56&&Math.abs(dy)>Math.abs(dx)*1.2){gesture=null;returnToThread(dy<0?-1:1);}
  },{capture:true,passive:false});
  document.addEventListener('touchend',()=>{gesture=null;},true);
  document.addEventListener('touchcancel',()=>{gesture=null;},true);
  document.addEventListener('wheel',e=>{if(blockedTarget(e.target))e.preventDefault();},{capture:true,passive:false});
  document.addEventListener('keydown',e=>{if(blockedTarget(e.target)&&['ArrowDown','ArrowUp','PageDown','PageUp','Home','End',' '].includes(e.key)&&!e.target.closest('input,textarea,[contenteditable="true"]'))e.preventDefault();},true);
  new MutationObserver(schedule).observe(document.documentElement,{childList:true,subtree:true});
  window.addEventListener('resize',schedule);
  window.addEventListener('popstate',schedule);
  window.addEventListener('contact-only-route',schedule);
  window.__contactReelRefresh=fit;
  fit();
})();
