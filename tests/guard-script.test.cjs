const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict'),path=require('node:path');
const {URL}=require('node:url');
const source=fs.readFileSync(path.join(__dirname,'../app/src/main/assets/dm-guard.js'),'utf8');
const jobs=[],ticks=[];let reads=0,observer,back=0,clock=0;
const location={origin:'https://www.instagram.com',href:'https://www.instagram.com/direct/inbox/',pathname:'/direct/inbox/',replace(value){this.pathname=value;}};
const context={window:null,location,URL,Date:{now:()=>clock},Event:class{constructor(type){this.type=type;}},
  document:{documentElement:{appendChild(){}},head:{appendChild(){}},createElement:()=>({}),querySelectorAll:()=>{reads++;return[];},addEventListener(){}},
  history:{back(){back++;},go(){back++;},pushState(a,b,url){location.pathname=new URL(url,location.origin).pathname;},replaceState(a,b,url){location.pathname=new URL(url,location.origin).pathname;}},
  MutationObserver:class{constructor(fn){observer=fn;}observe(){}},requestAnimationFrame:fn=>jobs.push(fn),setTimeout:fn=>jobs.push(fn),setInterval:fn=>ticks.push(fn),addEventListener(){},dispatchEvent(){}};
context.window=context;vm.createContext(context);vm.runInContext(source,context);
const initial=reads;for(let i=0;i<100;i++)ticks.forEach(fn=>fn());assert.equal(reads,initial,'Idle route checks never scan the DOM');
context.history.back();context.history.go(-1);assert.equal(back,0,'Inbox back exits blocked');
context.history.pushState(null,'','/reels/');assert.equal(location.pathname,'/direct/inbox/','Reels navigation blocked');
for(let i=0;i<100;i++)observer();assert.equal(jobs.length,1,'Mutation burst cleaned before next paint');jobs.shift()();
context.history.pushState(null,'','/direct/t/123/');assert.equal(location.pathname,'/direct/t/123/');context.history.back();assert.equal(back,1,'Conversation back works');
observer();assert.equal(jobs.length,0,'Conversation changes do not rescan Notes');
console.log('Guard: idle DOM scans eliminated, mutation bursts batched, inbox protection and conversation navigation passed.');

location.pathname='/direct/inbox/';context.document.readyState='loading';
assert.equal(context.__contactOnlyPrepare(),false,'Do not reveal a loading inbox');
context.document.readyState='complete';clock=1000;
assert.equal(context.__contactOnlyPrepare(),false,'Do not reveal an empty uninitialized DOM');
context.document.querySelectorAll=selector=>selector==='input,a[href^="/direct/t/"],[role="main"]'?[{}]:[];
assert.equal(context.__contactOnlyPrepare(),true,'Reveal prepared inbox after quiet period');
observer();assert.equal(context.__contactOnlyPrepare(),false,'Late inbox changes delay reveal');
clock+=399;assert.equal(context.__contactOnlyPrepare(),false);
clock+=1;assert.equal(context.__contactOnlyPrepare(),true);
location.pathname='/accounts/login/';assert.equal(context.__contactOnlyPrepare(),true,'Login stays usable');
console.log('Readiness: loading, empty DOM, late mutations and login passed.');
