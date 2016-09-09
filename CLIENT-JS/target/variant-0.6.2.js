/* Variant 0.6.2 © Variant, Inc. All rights reserved. getvariant.com */
(function(){if(!window.jQuery){throw Error("jQuery is required, but wasn't found")}if(typeof window.variant==="undefined"){window.variant={url:null,success:function(){},error:function(d){throw Error("Bad response from Variant server: "+d.status+" "+d.statusText+": "+d.responseText)}}}var c=!(typeof(Storage)==="undefined");var b=false;var a={push:function(e){if(!c){return}var d=JSON.parse(sessionStorage.variantQueue);d.push(e);sessionStorage.variantQueue=JSON.stringify(d)},drain:function(){if(!c){return}var d=setInterval(function(){var f=JSON.parse(sessionStorage.variantQueue);if(f.length>0&&!variant.url){clearInterval(d);throw Error("API endpoint URL has not been set. Call variant.options() to set. "+sessionStorage.variantQueue)}var e=0;while(f.length>0){var g=f.pop();$.ajax({url:variant.url+"/event",method:"post",data:JSON.stringify(g),contentType:"application/json; charset=utf-8",success:variant.success,error:variant.error});e++}sessionStorage.variantQueue="[]"},500)}};variant.boot=function(d){if(b){throw Error("Variant.js is already booted.")}if(arguments.length!=0){variant.url=d.url||variant.url;if(!variant.url.endsWith("/")){variant.url+="/"}variant.success=d.success||variant.success;variant.error=d.error||variant.error}if(c&&!sessionStorage.variantQueue){sessionStorage.variantQueue="[]"}a.drain();b=true};variant.Event=function(d,e,f){if(!c){return}if(arguments.length<2){throw Error("Contructor variant.Event(name, value, params) requires at least 2 arguments.")}this.name=d;this.value=e;this.params=f;this.sid=(function(){var g=document.cookie.indexOf("variant-ssnid");return g<0?null:document.cookie.substring(g+14,g+30)})()};variant.Event.prototype.send=function(){if(!c){return}if(!b){throw Error("Variant.js is not booted.  Call variant.boot() first.")}a.push(this)}})();