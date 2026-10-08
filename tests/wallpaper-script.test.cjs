const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const path = require('node:path');
const code = fs.readFileSync(path.join(__dirname,'../app/src/main/assets/wallpaper-theme.js'),'utf8');
const normal = {backgroundColor:'rgb(255, 255, 255)',backgroundImage:'none',borderTopLeftRadius:'0px',borderTopRightRadius:'0px',borderBottomLeftRadius:'0px',borderBottomRightRadius:'0px'};
class Element {
  constructor(tag,css={},protectedControl=false) {
    this.tag=tag;this.tagName=tag.toUpperCase();this.css={...normal,...css};this.protectedControl=protectedControl;this.attrs=new Set();this.children=[];this.nodeType=1;this.isConnected=true;
    const props=new Map();this.style={getPropertyValue:key=>props.get(key)?.[0]||'',getPropertyPriority:key=>props.get(key)?.[1]||'',setProperty:(key,value,priority)=>props.set(key,[value,priority||'']),removeProperty:key=>props.delete(key)};
  }
  matches() {return ['div','span','section','header','nav'].includes(this.tag);}
  closest(selector) {return this.protectedControl && (this.protectedControl !== 'rolebutton' || selector.includes('[role="button"]')) ? this : null;}
  setAttribute(key) {this.attrs.add(key);}
  removeAttribute(key) {this.attrs.delete(key);}
  toggleAttribute(key,value) {if(value)this.attrs.add(key);else this.attrs.delete(key);}
  appendChild(child) {this.children.push(child);}
  querySelectorAll(selector) {const result=[];const visit=el=>{for(const child of el.children){if(child.tag===selector)result.push(child);visit(child);}};visit(this);return result;}
  getBoundingClientRect() {throw Error('Unexpected synchronous layout measurement');}
}
const root=new Element('html'), head=new Element('head');
const square=new Element('div',{},'rolebutton'), blue=new Element('div',{backgroundColor:'rgb(75, 84, 255)',borderTopLeftRadius:'20px'});
const received=new Element('div',{backgroundColor:'rgb(245, 245, 245)',borderTopLeftRadius:'20px'});
const photo=new Element('div',{backgroundImage:'url(photo.png)'}), button=new Element('div',{},true);
const roundedWrapper=new Element('div',{borderTopLeftRadius:'6px'});roundedWrapper.children=[new Element('div',{backgroundColor:'rgb(75, 84, 255)',borderTopLeftRadius:'20px'})];
const inline=new Element('div');inline.style.setProperty('background-color','rgb(255, 255, 255)','important');
const gradient=new Element('div',{backgroundImage:'linear-gradient(rgb(255, 255, 255), rgb(255, 255, 255))'});
blue.before={...normal,content:'""'};
root.children=[square,blue,received,photo,button,roundedWrapper,inline,gradient];
const jobs=[], listeners={};let observer,styleReads=0;
const context={window:null,document:{documentElement:root,head,createElement:tag=>new Element(tag),createTreeWalker(node){const nodes=[];function walk(el){for(const child of el.children){nodes.push(child);walk(child);}}walk(node);let i=0;return{nextNode:()=>nodes[i++]||null};}},
  location:{pathname:'/direct/t/123/'},NodeFilter:{SHOW_ELEMENT:1},getComputedStyle:(el,pseudo)=>{styleReads++;return pseudo?(el[pseudo.slice(2)]||{...normal,content:'none'}):el.css;},
  MutationObserver:class {constructor(callback){observer=callback;}observe(){}},
  requestIdleCallback:fn=>jobs.push(fn),setTimeout:fn=>jobs.push(fn),setInterval:()=>{throw Error('Periodic DOM scans must not return');},
  addEventListener:(name,fn)=>listeners[name]=fn,__contactWallpaper:true};
context.window=context;vm.createContext(context);
function flush(){let n=0;while(jobs.length){assert.ok(n++<1000,'Unbounded scheduling');jobs.shift()();}}
vm.runInContext(code,context);flush();
assert.ok(square.attrs.has('data-contact-surface'),'White rectangular wrapper removed');
assert.ok(roundedWrapper.attrs.has('data-contact-surface'),'Rounded white wrapper around blue bubble removed');
assert.ok(gradient.attrs.has('data-contact-surface'),'White gradient wrapper removed');
assert.ok(blue.attrs.has('data-contact-white-before'),'White pseudo-element behind bubble removed');
assert.equal(inline.style.getPropertyValue('background-color'),'transparent','Inline important white background overridden');
for(const el of [blue,received,photo,button])assert.ok(!el.attrs.has('data-contact-surface'),'Bubble/media/control preserved');
const before=styleReads;vm.runInContext(code,context);flush();assert.equal(styleReads,before,'Reinjection does not rescan unchanged DOM');
const added=new Element('span');root.children.push(added);observer([{type:'childList',addedNodes:[added]}]);flush();
assert.ok(added.attrs.has('data-contact-surface'));assert.equal(styleReads,before+1,'Only newly added node inspected');
square.css.backgroundColor='rgb(75, 84, 255)';square.css.borderTopLeftRadius='20px';observer([{type:'attributes',target:square}]);flush();assert.ok(!square.attrs.has('data-contact-surface'),'Recycled element becoming a bubble restored');
context.__contactWallpaper=false;context.__contactWallpaperRefresh();flush();assert.ok(!root.attrs.has('data-contact-wallpaper'),'Disabling wallpaper restores original CSS');
assert.equal(inline.style.getPropertyValue('background-color'),'rgb(255, 255, 255)','Inline background restored when disabling wallpaper');
context.__contactWallpaper=true;context.__contactWallpaperRefresh();flush();assert.equal(inline.style.getPropertyValue('background-color'),'transparent','Inline override reapplied after enabling wallpaper again');
context.__contactWallpaper=true;context.location.pathname='/accounts/login/';context.__contactWallpaperRefresh();assert.ok(!root.attrs.has('data-contact-wallpaper'),'Login not themed');
context.__contactCardConfig={dark:true,enabled:false,tone:1,opacity:80,radius:24};context.location.pathname='/direct/inbox/';context.__contactWallpaperRefresh();flush();
assert.ok(root.attrs.has('data-contact-dark'),'Dark mode applied to DM');assert.ok(!root.attrs.has('data-contact-cards'),'Card setting can be disabled');
const row=new Element('div'),avatar=new Element('img');row.children=[avatar];avatar.parentElement=row;row.parentElement=root;
row.getBoundingClientRect=()=>({width:370,height:80,top:120});row.querySelector=()=>null;
const nameText=new Element('span'),recentText=new Element('span');
for(const el of [nameText,recentText]){el.childNodes=[{nodeType:3}];el.getBoundingClientRect=()=>({width:80,height:18});el.parentElement=row;}
row.children.push(nameText,recentText);row.querySelectorAll=()=>[nameText,recentText];
root.children.push(row);observer([{type:'childList',addedNodes:[row]}]);context.innerWidth=384;flush();
assert.ok(!row.attrs.has('data-contact-card'),'Parent row has no background card');
assert.ok(row.attrs.has('data-contact-card-row'),'Row used only to locate text');
assert.ok(nameText.attrs.has('data-contact-text-card')&&recentText.attrs.has('data-contact-text-card'),'Name and recent message receive separate text cards');
assert.ok(!avatar.attrs.has('data-contact-text-card'),'Avatar does not receive a card');
context.__contactCardConfig.enabled=true;context.__contactWallpaperRefresh();flush();assert.ok(root.attrs.has('data-contact-cards'),'Cards can be re-enabled');
context.location.pathname='/accounts/login/';context.__contactWallpaperRefresh();assert.ok(!root.attrs.has('data-contact-dark')&&!root.attrs.has('data-contact-cards'),'No dark/cards overrides on login');
console.log('Wallpaper: wrapper removal, bubble/media preservation, incremental updates, reuse and login isolation passed.');
