const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict'),path=require('node:path');
class E {
 constructor(w=384,h=597){this.w=w;this.h=h;this.attrs=new Set();this.listeners={};this.isConnected=true;this.scrollTop=0;this.parentElement=null;}
 setAttribute(a){this.attrs.add(a);}removeAttribute(a){this.attrs.delete(a);}
 contains(el){for(;el;el=el.parentElement)if(el===this)return true;return false;}
 animate(frames,options){motions.push({frames,options});return {cancel(){}};}
 addEventListener(n,f){this.listeners[n]=f;}removeEventListener(n){delete this.listeners[n];}
 querySelectorAll(){return [];}
 closest(){return null;}
 getBoundingClientRect(){const width=this.attrs.has('data-contact-reel-width')?this.parentElement.getBoundingClientRect().width:this.w;return {left:0,top:0,right:width,bottom:this.h,width,height:this.h};}
}
const body=new E(),root=new E(),narrow=new E(338),video=new E();root.parentElement=body;narrow.parentElement=root;video.parentElement=narrow;root.snap='y mandatory';
const document=new E();document.body=body;document.documentElement=new E();document.head={appendChild(){}};document.createElement=()=>({});document.querySelectorAll=()=>video.isConnected?[video]:[];
const events={},jobs=[],timers=[],motions=[];let observer,returned=null,backCount=0;
const context={window:null,document,Element:E,innerWidth:384,innerHeight:667,location:{origin:'https://www.instagram.com',pathname:'/direct/t/1/',assign:url=>{returned=url;}},history:{back:()=>{backCount++;}},getComputedStyle:el=>({scrollSnapType:el.attrs.has('data-contact-reel-lock')?'none':el.snap||'none'}),setTimeout:f=>timers.push(f),requestAnimationFrame:f=>jobs.push(f),MutationObserver:class{constructor(f){observer=f;}observe(){}},addEventListener:(n,f)=>events[n]=f};context.window=context;
vm.runInNewContext(fs.readFileSync(path.join(__dirname,'../app/src/main/assets/dm-reel.js'),'utf8'),context);
assert.equal(narrow.getBoundingClientRect().width,384,'Player clipping ancestor fits viewport');assert(root.attrs.has('data-contact-reel-lock'));
root.scrollTop=597;root.listeners.scroll();assert.equal(root.scrollTop,0,'Programmatic next-reel scroll returns to shared reel');
let prevented=false;document.listeners.touchmove({target:video,touches:[{clientX:100,clientY:100}],preventDefault(){prevented=true;}});assert(prevented,'Reel swipe blocked');
prevented=false;document.listeners.touchmove({target:body,preventDefault(){prevented=true;}});assert(!prevented,'DM outside reel remains scrollable');
document.listeners.touchstart({target:video,touches:[{clientX:100,clientY:100}]});
document.listeners.touchmove({target:video,touches:[{clientX:170,clientY:105}],preventDefault(){}});
assert.equal(returned,null,'Horizontal swipe does not close reel');
document.listeners.touchmove({target:video,touches:[{clientX:102,clientY:35}],preventDefault(){}});
assert.equal(motions.at(-1).frames[1].transform,'translateY(-667px)','Up swipe exits upwards');timers.shift()();assert(document.documentElement.attrs.has('data-contact-reel-returning'),'Sideways transition masked');timers.shift()();assert(!document.documentElement.attrs.has('data-contact-reel-returning'),'DM becomes visible again');
assert.equal(returned,'https://www.instagram.com/direct/t/1/','Vertical swipe returns to originating thread');
returned=null;document.listeners.touchmove({target:video,touches:[{clientX:102,clientY:0}],preventDefault(){}});assert.equal(returned,null,'Return fires once per viewer');
video.isConnected=false;observer();jobs.shift()();assert(!root.attrs.has('data-contact-reel-lock'));assert(!narrow.attrs.has('data-contact-reel-width'),'Removed viewer restores layout');
video.isConnected=true;root.snap='none';context.__contactReelRefresh();assert(!root.attrs.has('data-contact-reel-lock'),'Inline DM videos are not locked');
root.snap='y mandatory';const viewer=new E();viewer.parentElement=body;root.parentElement=viewer;let closed=0;
const close=new E(24,24);close.getAttribute=()=> 'Back';close.click=()=>{closed++;};viewer.querySelectorAll=()=>[close];
context.__contactReelRefresh();document.listeners.touchstart({target:video,touches:[{clientX:100,clientY:100}]});
document.listeners.touchmove({target:video,touches:[{clientX:100,clientY:170}],preventDefault(){}});
assert.equal(motions.at(-1).frames[1].transform,'translateY(667px)','Down swipe exits downwards');timers.shift()();timers.shift()();
assert.equal(closed,1,'Downward swipe uses viewer Back control');assert.equal(returned,null,'No full navigation when Back is available');
console.log('Reel: fit, vertical return, horizontal isolation, Back control, thread fallback and cleanup passed.');
