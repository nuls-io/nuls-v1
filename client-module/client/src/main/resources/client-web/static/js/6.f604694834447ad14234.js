webpackJsonp([6],{"8OiZ":function(t,e){},EmQZ:function(t,e){},OKVl:function(t,e,s){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var a=s("rdcW"),o={render:function(){var t=this,e=t.$createElement,s=t._self._c||e;return s("div",{staticClass:"import-nuls"},[s("Back",{attrs:{backTitle:this.$t("message.inportAccount")}}),t._v(" "),s("h2",[t._v(t._s(t.$t("message.c146")))]),t._v(" "),s("el-upload",{staticClass:"avatar-uploader",attrs:{action:"http://192.168.1.201:8001/posts/","show-file-list":!0,limit:1,"before-upload":t.beforeAvatarUpload}},[t.imageUrl?s("img",{staticClass:"avatar",attrs:{src:t.imageUrl}}):s("i",{staticClass:"el-icon-plus avatar-uploader-icon"}),t._v(" "),s("div",{staticClass:"el-upload__tip",attrs:{slot:"tip"},slot:"tip"},[t._v(t._s(t.$t("message.c147")))])]),t._v(" "),s("el-button",{attrs:{type:"primary",id:"importKeystore"},on:{click:t.keyStoreSubmit}},[t._v("\n        "+t._s(t.$t("message.confirmButtonText"))+"\n    ")]),t._v(" "),s("Password",{ref:"password",attrs:{submitId:t.submitId},on:{toSubmit:t.toSubmit}})],1)},staticRenderFns:[]};var r=function(t){s("EmQZ")},i=s("vSla")(a.a,o,!1,r,null,null);e.default=i.exports},rdcW:function(module,__webpack_exports__,__webpack_require__){"use strict";var __WEBPACK_IMPORTED_MODULE_0__components_BackBar_vue__=__webpack_require__("LPk9"),__WEBPACK_IMPORTED_MODULE_1__components_PasswordBar_vue__=__webpack_require__("FJop");__webpack_exports__.a={data:function(){return{imageUrl:"",keyStorePath:"",keyStoreInfo:""}},components:{Back:__WEBPACK_IMPORTED_MODULE_0__components_BackBar_vue__.a,Password:__WEBPACK_IMPORTED_MODULE_1__components_PasswordBar_vue__.a},methods:{beforeAvatarUpload:function(t){var e="keystore"===t.name.substr(t.name.length-8),s=t.size/1024/1024<2;return e?s?this.keyStorePath=t.path:this.$message.error("上传头像图片大小不能超过 2MB!"):this.$message.error("上传只能是 keystore 格式文件!"),e&&s},keySubmit:function(t){var e=this;this.$refs[t].validate(function(t){if(!t)return console.log("error submit!!"),!1;e.$refs.password.showPassword(!0)})},keyStoreSubmit:function(){""!==this.keyStorePath?this.$refs.password.showPassword(!0):this.$message.error("message.passWordFailed")},toSubmit:function toSubmit(password){var fs=__webpack_require__("8OiZ"),dataInfo=fs.readFileSync(this.keyStorePath,"utf-8"),param={accountKeyStoreDto:eval("("+dataInfo+")"),password:password};this.postKeyStore("/account/import",param)},postKeyStore:function(t,e){var s=this;this.$post(t,e).then(function(t){console.log(t),t.success?(localStorage.setItem("newAccountAddress",t.data),"1"!==localStorage.getItem("toUserInfo")?(s.getAccountList("/account"),s.$router.push({name:"/wallet"})):s.$router.push({path:"/wallet/users/userInfo"}),s.$message({type:"success",message:s.$t("message.passWordSuccess")})):s.$message({type:"warning",message:s.$t("message.passWordFailed")+t.msg})})},getAccountList:function(t){var e=this;this.$fetch(t).then(function(t){t.success&&e.$store.commit("setAddressList",t.data.list)})}}}}});