webpackJsonp([1],{"7wgv":function(s,t,e){"use strict";var r={data:function(){var s=this;return{passVisible:!1,passForm:{pass:"",checkPass:""},rulesPass:{pass:[{validator:function(t,e,r){""===e?r(new Error(s.$t("message.walletPassWord1"))):/(?!^((\d+)|([a-zA-Z]+)|([~!@#\$%\^&\*\(\)]+))$)^[a-zA-Z0-9~!@#\$%\^&\*\(\)]{8,21}$/.exec(e)?(""!==s.passForm.checkPass&&s.$refs.passForm.validateField("checkPass"),r()):r(new Error(s.$t("message.walletPassWord1")))},trigger:"blur"}],checkPass:[{validator:function(t,e,r){""===e?r(new Error(s.$t("message.affirmWalletPassWordEmpty"))):e!==s.passForm.pass?r(new Error(s.$t("message.passWordAtypism"))):r()},trigger:"blur"}]}}},created:function(){},methods:{passwordShow:function(){},passwordClose:function(){},showPasswordTwo:function(s){this.passForm.password="",this.passVisible=s},submitForm:function(s){var t=this;this.$refs[s].validate(function(s){if(!s)return!1;t.$emit("toSubmit",t.passForm.checkPass),t.passVisible=!1})},noPassword:function(){this.passForm.checkPass="",this.$emit("toSubmit",this.passForm.checkPass),this.passVisible=!1}}},o={render:function(){var s=this,t=s.$createElement,e=s._self._c||t;return e("el-dialog",{staticClass:"password-two-dialog",attrs:{title:"",visible:s.passVisible,top:"15vh"},on:{"update:visible":function(t){s.passVisible=t},open:s.passwordShow,close:s.passwordClose}},[e("h2",[s._v(s._s(s.$t("message.setPassWord")))]),s._v(" "),e("el-form",{ref:"passForm",staticClass:"set-pass",attrs:{model:s.passForm,"status-icon":"",rules:s.rulesPass}},[e("el-form-item",{staticStyle:{"margin-bottom":"5px"},attrs:{label:s.$t("message.walletPassWord"),prop:"pass"}},[e("el-input",{attrs:{type:"password",maxlength:20,placeholder:this.$t("message.walletPassWord1")},model:{value:s.passForm.pass,callback:function(t){s.$set(s.passForm,"pass",t)},expression:"passForm.pass"}})],1),s._v(" "),e("el-form-item",{staticStyle:{"margin-bottom":"5px"},attrs:{label:s.$t("message.affirmWalletPassWord"),prop:"checkPass"}},[e("el-input",{attrs:{type:"password",maxlength:20,placeholder:this.$t("message.affirmWalletPassWordEmpty")},model:{value:s.passForm.checkPass,callback:function(t){s.$set(s.passForm,"checkPass",t)},expression:"passForm.checkPass"}})],1),s._v(" "),e("div",{staticClass:"set-pass-title"},[s._v(s._s(s.$t("message.passWordInfo")))]),s._v(" "),e("el-form-item",[e("el-button",{staticClass:"set-pass-submit",attrs:{type:"primary",id:"setPassTwo"},on:{click:function(t){s.submitForm("passForm")}}},[s._v("\n                "+s._s(s.$t("message.passWordAffirm"))+"\n            ")]),s._v(" "),e("div",{staticClass:"new-no-pass",on:{click:s.noPassword}},[s._v("\n               "+s._s(s.$t("message.c159"))+"\n           ")])],1)],1)],1)},staticRenderFns:[]};var a=e("vSla")(r,o,!1,function(s){e("8q5C")},null,null);t.a=a.exports},"8q5C":function(s,t){},KPSb:function(module,exports,__webpack_require__){(function(process,global){var __WEBPACK_AMD_DEFINE_RESULT__;
/**
 * [js-md5]{@link https://github.com/emn178/js-md5}
 *
 * @namespace md5
 * @version 0.7.3
 * @author Chen, Yi-Cyuan [emn178@gmail.com]
 * @copyright Chen, Yi-Cyuan 2014-2017
 * @license MIT
 */
/**
 * [js-md5]{@link https://github.com/emn178/js-md5}
 *
 * @namespace md5
 * @version 0.7.3
 * @author Chen, Yi-Cyuan [emn178@gmail.com]
 * @copyright Chen, Yi-Cyuan 2014-2017
 * @license MIT
 */
!function(){"use strict";var ERROR="input is invalid type",WINDOW="object"==typeof window,root=WINDOW?window:{};root.JS_MD5_NO_WINDOW&&(WINDOW=!1);var WEB_WORKER=!WINDOW&&"object"==typeof self,NODE_JS=!root.JS_MD5_NO_NODE_JS&&"object"==typeof process&&process.versions&&process.versions.node;NODE_JS?root=global:WEB_WORKER&&(root=self);var COMMON_JS=!root.JS_MD5_NO_COMMON_JS&&"object"==typeof module&&module.exports,AMD=__webpack_require__("Ycmu"),ARRAY_BUFFER=!root.JS_MD5_NO_ARRAY_BUFFER&&"undefined"!=typeof ArrayBuffer,HEX_CHARS="0123456789abcdef".split(""),EXTRA=[128,32768,8388608,-2147483648],SHIFT=[0,8,16,24],OUTPUT_TYPES=["hex","array","digest","buffer","arrayBuffer","base64"],BASE64_ENCODE_CHAR="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".split(""),blocks=[],buffer8;if(ARRAY_BUFFER){var buffer=new ArrayBuffer(68);buffer8=new Uint8Array(buffer),blocks=new Uint32Array(buffer)}!root.JS_MD5_NO_NODE_JS&&Array.isArray||(Array.isArray=function(s){return"[object Array]"===Object.prototype.toString.call(s)}),!ARRAY_BUFFER||!root.JS_MD5_NO_ARRAY_BUFFER_IS_VIEW&&ArrayBuffer.isView||(ArrayBuffer.isView=function(s){return"object"==typeof s&&s.buffer&&s.buffer.constructor===ArrayBuffer});var createOutputMethod=function(s){return function(t){return new Md5(!0).update(t)[s]()}},createMethod=function(){var s=createOutputMethod("hex");NODE_JS&&(s=nodeWrap(s)),s.create=function(){return new Md5},s.update=function(t){return s.create().update(t)};for(var t=0;t<OUTPUT_TYPES.length;++t){var e=OUTPUT_TYPES[t];s[e]=createOutputMethod(e)}return s},nodeWrap=function(method){var crypto=eval("require('crypto')"),Buffer=eval("require('buffer').Buffer"),nodeMethod=function(s){if("string"==typeof s)return crypto.createHash("md5").update(s,"utf8").digest("hex");if(null===s||void 0===s)throw ERROR;return s.constructor===ArrayBuffer&&(s=new Uint8Array(s)),Array.isArray(s)||ArrayBuffer.isView(s)||s.constructor===Buffer?crypto.createHash("md5").update(new Buffer(s)).digest("hex"):method(s)};return nodeMethod};function Md5(s){if(s)blocks[0]=blocks[16]=blocks[1]=blocks[2]=blocks[3]=blocks[4]=blocks[5]=blocks[6]=blocks[7]=blocks[8]=blocks[9]=blocks[10]=blocks[11]=blocks[12]=blocks[13]=blocks[14]=blocks[15]=0,this.blocks=blocks,this.buffer8=buffer8;else if(ARRAY_BUFFER){var t=new ArrayBuffer(68);this.buffer8=new Uint8Array(t),this.blocks=new Uint32Array(t)}else this.blocks=[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];this.h0=this.h1=this.h2=this.h3=this.start=this.bytes=this.hBytes=0,this.finalized=this.hashed=!1,this.first=!0}Md5.prototype.update=function(s){if(!this.finalized){var t,e=typeof s;if("string"!==e){if("object"!==e)throw ERROR;if(null===s)throw ERROR;if(ARRAY_BUFFER&&s.constructor===ArrayBuffer)s=new Uint8Array(s);else if(!(Array.isArray(s)||ARRAY_BUFFER&&ArrayBuffer.isView(s)))throw ERROR;t=!0}for(var r,o,a=0,i=s.length,n=this.blocks,c=this.buffer8;a<i;){if(this.hashed&&(this.hashed=!1,n[0]=n[16],n[16]=n[1]=n[2]=n[3]=n[4]=n[5]=n[6]=n[7]=n[8]=n[9]=n[10]=n[11]=n[12]=n[13]=n[14]=n[15]=0),t)if(ARRAY_BUFFER)for(o=this.start;a<i&&o<64;++a)c[o++]=s[a];else for(o=this.start;a<i&&o<64;++a)n[o>>2]|=s[a]<<SHIFT[3&o++];else if(ARRAY_BUFFER)for(o=this.start;a<i&&o<64;++a)(r=s.charCodeAt(a))<128?c[o++]=r:r<2048?(c[o++]=192|r>>6,c[o++]=128|63&r):r<55296||r>=57344?(c[o++]=224|r>>12,c[o++]=128|r>>6&63,c[o++]=128|63&r):(r=65536+((1023&r)<<10|1023&s.charCodeAt(++a)),c[o++]=240|r>>18,c[o++]=128|r>>12&63,c[o++]=128|r>>6&63,c[o++]=128|63&r);else for(o=this.start;a<i&&o<64;++a)(r=s.charCodeAt(a))<128?n[o>>2]|=r<<SHIFT[3&o++]:r<2048?(n[o>>2]|=(192|r>>6)<<SHIFT[3&o++],n[o>>2]|=(128|63&r)<<SHIFT[3&o++]):r<55296||r>=57344?(n[o>>2]|=(224|r>>12)<<SHIFT[3&o++],n[o>>2]|=(128|r>>6&63)<<SHIFT[3&o++],n[o>>2]|=(128|63&r)<<SHIFT[3&o++]):(r=65536+((1023&r)<<10|1023&s.charCodeAt(++a)),n[o>>2]|=(240|r>>18)<<SHIFT[3&o++],n[o>>2]|=(128|r>>12&63)<<SHIFT[3&o++],n[o>>2]|=(128|r>>6&63)<<SHIFT[3&o++],n[o>>2]|=(128|63&r)<<SHIFT[3&o++]);this.lastByteIndex=o,this.bytes+=o-this.start,o>=64?(this.start=o-64,this.hash(),this.hashed=!0):this.start=o}return this.bytes>4294967295&&(this.hBytes+=this.bytes/4294967296<<0,this.bytes=this.bytes%4294967296),this}},Md5.prototype.finalize=function(){if(!this.finalized){this.finalized=!0;var s=this.blocks,t=this.lastByteIndex;s[t>>2]|=EXTRA[3&t],t>=56&&(this.hashed||this.hash(),s[0]=s[16],s[16]=s[1]=s[2]=s[3]=s[4]=s[5]=s[6]=s[7]=s[8]=s[9]=s[10]=s[11]=s[12]=s[13]=s[14]=s[15]=0),s[14]=this.bytes<<3,s[15]=this.hBytes<<3|this.bytes>>>29,this.hash()}},Md5.prototype.hash=function(){var s,t,e,r,o,a,i=this.blocks;this.first?t=((t=((s=((s=i[0]-680876937)<<7|s>>>25)-271733879<<0)^(e=((e=(-271733879^(r=((r=(-1732584194^2004318071&s)+i[1]-117830708)<<12|r>>>20)+s<<0)&(-271733879^s))+i[2]-1126478375)<<17|e>>>15)+r<<0)&(r^s))+i[3]-1316259209)<<22|t>>>10)+e<<0:(s=this.h0,t=this.h1,e=this.h2,t=((t+=((s=((s+=((r=this.h3)^t&(e^r))+i[0]-680876936)<<7|s>>>25)+t<<0)^(e=((e+=(t^(r=((r+=(e^s&(t^e))+i[1]-389564586)<<12|r>>>20)+s<<0)&(s^t))+i[2]+606105819)<<17|e>>>15)+r<<0)&(r^s))+i[3]-1044525330)<<22|t>>>10)+e<<0),t=((t+=((s=((s+=(r^t&(e^r))+i[4]-176418897)<<7|s>>>25)+t<<0)^(e=((e+=(t^(r=((r+=(e^s&(t^e))+i[5]+1200080426)<<12|r>>>20)+s<<0)&(s^t))+i[6]-1473231341)<<17|e>>>15)+r<<0)&(r^s))+i[7]-45705983)<<22|t>>>10)+e<<0,t=((t+=((s=((s+=(r^t&(e^r))+i[8]+1770035416)<<7|s>>>25)+t<<0)^(e=((e+=(t^(r=((r+=(e^s&(t^e))+i[9]-1958414417)<<12|r>>>20)+s<<0)&(s^t))+i[10]-42063)<<17|e>>>15)+r<<0)&(r^s))+i[11]-1990404162)<<22|t>>>10)+e<<0,t=((t+=((s=((s+=(r^t&(e^r))+i[12]+1804603682)<<7|s>>>25)+t<<0)^(e=((e+=(t^(r=((r+=(e^s&(t^e))+i[13]-40341101)<<12|r>>>20)+s<<0)&(s^t))+i[14]-1502002290)<<17|e>>>15)+r<<0)&(r^s))+i[15]+1236535329)<<22|t>>>10)+e<<0,t=((t+=((r=((r+=(t^e&((s=((s+=(e^r&(t^e))+i[1]-165796510)<<5|s>>>27)+t<<0)^t))+i[6]-1069501632)<<9|r>>>23)+s<<0)^s&((e=((e+=(s^t&(r^s))+i[11]+643717713)<<14|e>>>18)+r<<0)^r))+i[0]-373897302)<<20|t>>>12)+e<<0,t=((t+=((r=((r+=(t^e&((s=((s+=(e^r&(t^e))+i[5]-701558691)<<5|s>>>27)+t<<0)^t))+i[10]+38016083)<<9|r>>>23)+s<<0)^s&((e=((e+=(s^t&(r^s))+i[15]-660478335)<<14|e>>>18)+r<<0)^r))+i[4]-405537848)<<20|t>>>12)+e<<0,t=((t+=((r=((r+=(t^e&((s=((s+=(e^r&(t^e))+i[9]+568446438)<<5|s>>>27)+t<<0)^t))+i[14]-1019803690)<<9|r>>>23)+s<<0)^s&((e=((e+=(s^t&(r^s))+i[3]-187363961)<<14|e>>>18)+r<<0)^r))+i[8]+1163531501)<<20|t>>>12)+e<<0,t=((t+=((r=((r+=(t^e&((s=((s+=(e^r&(t^e))+i[13]-1444681467)<<5|s>>>27)+t<<0)^t))+i[2]-51403784)<<9|r>>>23)+s<<0)^s&((e=((e+=(s^t&(r^s))+i[7]+1735328473)<<14|e>>>18)+r<<0)^r))+i[12]-1926607734)<<20|t>>>12)+e<<0,t=((t+=((a=(r=((r+=((o=t^e)^(s=((s+=(o^r)+i[5]-378558)<<4|s>>>28)+t<<0))+i[8]-2022574463)<<11|r>>>21)+s<<0)^s)^(e=((e+=(a^t)+i[11]+1839030562)<<16|e>>>16)+r<<0))+i[14]-35309556)<<23|t>>>9)+e<<0,t=((t+=((a=(r=((r+=((o=t^e)^(s=((s+=(o^r)+i[1]-1530992060)<<4|s>>>28)+t<<0))+i[4]+1272893353)<<11|r>>>21)+s<<0)^s)^(e=((e+=(a^t)+i[7]-155497632)<<16|e>>>16)+r<<0))+i[10]-1094730640)<<23|t>>>9)+e<<0,t=((t+=((a=(r=((r+=((o=t^e)^(s=((s+=(o^r)+i[13]+681279174)<<4|s>>>28)+t<<0))+i[0]-358537222)<<11|r>>>21)+s<<0)^s)^(e=((e+=(a^t)+i[3]-722521979)<<16|e>>>16)+r<<0))+i[6]+76029189)<<23|t>>>9)+e<<0,t=((t+=((a=(r=((r+=((o=t^e)^(s=((s+=(o^r)+i[9]-640364487)<<4|s>>>28)+t<<0))+i[12]-421815835)<<11|r>>>21)+s<<0)^s)^(e=((e+=(a^t)+i[15]+530742520)<<16|e>>>16)+r<<0))+i[2]-995338651)<<23|t>>>9)+e<<0,t=((t+=((r=((r+=(t^((s=((s+=(e^(t|~r))+i[0]-198630844)<<6|s>>>26)+t<<0)|~e))+i[7]+1126891415)<<10|r>>>22)+s<<0)^((e=((e+=(s^(r|~t))+i[14]-1416354905)<<15|e>>>17)+r<<0)|~s))+i[5]-57434055)<<21|t>>>11)+e<<0,t=((t+=((r=((r+=(t^((s=((s+=(e^(t|~r))+i[12]+1700485571)<<6|s>>>26)+t<<0)|~e))+i[3]-1894986606)<<10|r>>>22)+s<<0)^((e=((e+=(s^(r|~t))+i[10]-1051523)<<15|e>>>17)+r<<0)|~s))+i[1]-2054922799)<<21|t>>>11)+e<<0,t=((t+=((r=((r+=(t^((s=((s+=(e^(t|~r))+i[8]+1873313359)<<6|s>>>26)+t<<0)|~e))+i[15]-30611744)<<10|r>>>22)+s<<0)^((e=((e+=(s^(r|~t))+i[6]-1560198380)<<15|e>>>17)+r<<0)|~s))+i[13]+1309151649)<<21|t>>>11)+e<<0,t=((t+=((r=((r+=(t^((s=((s+=(e^(t|~r))+i[4]-145523070)<<6|s>>>26)+t<<0)|~e))+i[11]-1120210379)<<10|r>>>22)+s<<0)^((e=((e+=(s^(r|~t))+i[2]+718787259)<<15|e>>>17)+r<<0)|~s))+i[9]-343485551)<<21|t>>>11)+e<<0,this.first?(this.h0=s+1732584193<<0,this.h1=t-271733879<<0,this.h2=e-1732584194<<0,this.h3=r+271733878<<0,this.first=!1):(this.h0=this.h0+s<<0,this.h1=this.h1+t<<0,this.h2=this.h2+e<<0,this.h3=this.h3+r<<0)},Md5.prototype.hex=function(){this.finalize();var s=this.h0,t=this.h1,e=this.h2,r=this.h3;return HEX_CHARS[s>>4&15]+HEX_CHARS[15&s]+HEX_CHARS[s>>12&15]+HEX_CHARS[s>>8&15]+HEX_CHARS[s>>20&15]+HEX_CHARS[s>>16&15]+HEX_CHARS[s>>28&15]+HEX_CHARS[s>>24&15]+HEX_CHARS[t>>4&15]+HEX_CHARS[15&t]+HEX_CHARS[t>>12&15]+HEX_CHARS[t>>8&15]+HEX_CHARS[t>>20&15]+HEX_CHARS[t>>16&15]+HEX_CHARS[t>>28&15]+HEX_CHARS[t>>24&15]+HEX_CHARS[e>>4&15]+HEX_CHARS[15&e]+HEX_CHARS[e>>12&15]+HEX_CHARS[e>>8&15]+HEX_CHARS[e>>20&15]+HEX_CHARS[e>>16&15]+HEX_CHARS[e>>28&15]+HEX_CHARS[e>>24&15]+HEX_CHARS[r>>4&15]+HEX_CHARS[15&r]+HEX_CHARS[r>>12&15]+HEX_CHARS[r>>8&15]+HEX_CHARS[r>>20&15]+HEX_CHARS[r>>16&15]+HEX_CHARS[r>>28&15]+HEX_CHARS[r>>24&15]},Md5.prototype.toString=Md5.prototype.hex,Md5.prototype.digest=function(){this.finalize();var s=this.h0,t=this.h1,e=this.h2,r=this.h3;return[255&s,s>>8&255,s>>16&255,s>>24&255,255&t,t>>8&255,t>>16&255,t>>24&255,255&e,e>>8&255,e>>16&255,e>>24&255,255&r,r>>8&255,r>>16&255,r>>24&255]},Md5.prototype.array=Md5.prototype.digest,Md5.prototype.arrayBuffer=function(){this.finalize();var s=new ArrayBuffer(16),t=new Uint32Array(s);return t[0]=this.h0,t[1]=this.h1,t[2]=this.h2,t[3]=this.h3,s},Md5.prototype.buffer=Md5.prototype.arrayBuffer,Md5.prototype.base64=function(){for(var s,t,e,r="",o=this.array(),a=0;a<15;)s=o[a++],t=o[a++],e=o[a++],r+=BASE64_ENCODE_CHAR[s>>>2]+BASE64_ENCODE_CHAR[63&(s<<4|t>>>4)]+BASE64_ENCODE_CHAR[63&(t<<2|e>>>6)]+BASE64_ENCODE_CHAR[63&e];return s=o[a],r+=BASE64_ENCODE_CHAR[s>>>2]+BASE64_ENCODE_CHAR[s<<4&63]+"=="};var exports=createMethod();COMMON_JS?module.exports=exports:(root.md5=exports,AMD&&(__WEBPACK_AMD_DEFINE_RESULT__=function(){return exports}.call(exports,__webpack_require__,exports,module),void 0===__WEBPACK_AMD_DEFINE_RESULT__||(module.exports=__WEBPACK_AMD_DEFINE_RESULT__)))}()}).call(exports,__webpack_require__("V0EG"),__webpack_require__("9AUj"))},VcaK:function(s,t,e){"use strict";Object.defineProperty(t,"__esModule",{value:!0});e("6ROu"),e("KPSb");var r=e("LPk9"),o=e("7wgv"),a={data:function(){return{passwordValue:"",backOk:""!==localStorage.getItem("newAccountAddress"),backOks:""!==localStorage.getItem("newAccountAddress"),isPassword:!0}},components:{Back:r.a,PasswordTow:o.a},created:function(){document.onkeydown=function(s){13===window.event.keyCode&&document.getElementById("setPassTwo").click()}},methods:{newAccount:function(){this.$refs.passTwo.showPasswordTwo(!0)},toSubmit:function(s){var t="";""===s?(t='{"count":1,"password":""}',this.isPassword=!1):(localStorage.setItem("userPass",s),t='{"count":1,"password":"'+s+'"}'),this.postAccount("/account",t)},postAccount:function(s,t){var e=this;this.$post(s,t).then(function(s){s.success?(localStorage.setItem("newAccountAddress",s.data.list[0]),localStorage.setItem("addressAlias",""),localStorage.setItem("addressRemark",""),localStorage.setItem("encrypted",e.isPassword),e.getAccountList("/account"),e.$message({type:"success",message:e.$t("message.passWordSuccess")})):e.$message({type:"warning",message:e.$t("message.passWordFailed")+s.data.msg})})},getAccountList:function(s){var t=this;this.$fetch(s).then(function(s){s.success&&(t.$store.commit("setAddressList",s.data.list),t.$router.push({name:"/newAccount",params:{newOk:!0,address:localStorage.getItem("newAccountAddress")}}))}).catch(function(s){console.log("User List err"+s)})},importAccount:function(){this.$router.push({name:"/importAccount"})}}},i={render:function(){var s=this,t=s.$createElement,e=s._self._c||t;return e("div",{staticClass:"first-info"},[e("div",{staticClass:"first-info-top"},[e("Back",{directives:[{name:"show",rawName:"v-show",value:s.backOk,expression:"backOk"}],attrs:{backTitle:this.$t("message.accountManagement")}})],1),s._v(" "),e("h2",[s._v(s._s(s.$t("message.firstInfoTitle")))]),s._v(" "),e("ul",[e("li",{on:{click:s.newAccount}},[e("span",[s._v(s._s(s.$t("message.createNewAccount")))]),s._v(" "),e("label",[s._v(s._s(s.$t("message.createNewAccountInfo")))])]),s._v(" "),e("li",{on:{click:s.importAccount}},[e("span",[s._v(s._s(s.$t("message.importAccount")))]),s._v(" "),e("label",[s._v(s._s(s.$t("message.importAccountInfo")))])])]),s._v(" "),e("div",{directives:[{name:"show",rawName:"v-show",value:s.backOks,expression:"backOks"}],staticClass:"backOk"}),s._v(" "),e("PasswordTow",{ref:"passTwo",on:{toSubmit:s.toSubmit}})],1)},staticRenderFns:[]};var n=e("vSla")(a,i,!1,function(s){e("uPK1")},null,null);t.default=n.exports},uPK1:function(s,t){}});