webpackJsonp([17],{Ms6F:function(t,e){},XV2l:function(t,e,s){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var a=s("LPk9"),o=s("FJop"),n=s("YgNb"),i={data:function(){return{selectAddress:localStorage.getItem("newAccountAddress"),submitId:"zeroToEhole",totalInfo:[],fee:0,balanceIf:!1,threeIf:!1,buttonIf:!1,loading:!1}},components:{Back:a.a,Password:o.a},created:function(){this.getTotalUTXO()},methods:{getTotalUTXO:function(){var t=this,e="/accountledger/getTotalUTXO/"+this.selectAddress;this.$fetch(e).then(function(e){e.success&&(e.data.max=Object(n.b)(e.data.max).toString(),t.threeIf=e.data.size>=20,t.buttonIf=e.data.size<20,t.totalInfo=e.data,t.estimateFee())})},estimateFee:function(){var t=this,e="/accountledger/estimateFee/"+this.selectAddress;this.$fetch(e).then(function(e){e.success&&(t.fee=Object(n.b)(e.data.fee).toString(),e.data.fee>=Object(n.e)(t.totalInfo.max)&&(t.balanceIf=!0,t.buttonIf=!0))})},tochangeWhole:function(){var t=this;"true"===localStorage.getItem("encrypted")?this.$refs.password.showPassword(!0):this.$confirm(this.$t("message.c172"),"",{confirmButtonText:this.$t("message.confirmButtonText"),cancelButtonText:this.$t("message.cancelButtonText")}).then(function(){t.toSubmit("")}).catch(function(){console.log("")})},toSubmit:function(t){var e=this,s='{"address":"'+this.selectAddress+'","password":"'+t+'"}';this.loading=!0,this.$post("/accountledger/changeWhole",s).then(function(t){t.success?(e.loading=!1,e.$router.push({name:"deallist",query:{address:""}})):(e.loading=!1,e.$message({message:e.$t("message.passWordFailed")+t.data.msg,type:"warning"}))})}}},c={render:function(){var t=this,e=t.$createElement,s=t._self._c||e;return s("div",{staticClass:"zero-to-whole"},[s("Back",{attrs:{backTitle:this.$t("message.back")}}),t._v(" "),s("div",{directives:[{name:"loading",rawName:"v-loading",value:t.loading,expression:"loading"}],staticClass:"zth-info"},[s("h3",[t._v(t._s(t.$t("message.c259")))]),t._v(" "),s("div",{staticClass:"zth-info-top"},[s("p",[t._v("• "+t._s(t.$t("message.c260")))]),t._v(" "),s("p",[t._v("• "+t._s(t.$t("message.c261")))])]),t._v(" "),s("div",{staticClass:"zth-info-bottom"},[s("div",{staticClass:"zero"},[s("span",[t._v(t._s(t.$t("message.c262"))+":")]),t._v(t._s(this.totalInfo.size)+" "+t._s(t.$t("message.c263"))+"\n        "),s("el-tooltip",{attrs:{placement:"right"}},[s("div",{attrs:{slot:"content"},slot:"content"},[t._v(t._s(t.$t("message.c264"))+"\n          ")]),t._v(" "),s("i",{staticClass:"el-icon-info"})])],1),t._v(" "),s("div",[s("span",[t._v(t._s(t.$t("message.c265"))+":")]),t._v(t._s(this.totalInfo.max)+" NULS")]),t._v(" "),s("div",[s("span",[t._v(t._s(t.$t("message.c266"))+":")]),t._v(t._s(this.fee)+" NULS")]),t._v(" "),s("p",[s("span",{directives:[{name:"show",rawName:"v-show",value:t.balanceIf,expression:"balanceIf"}]},[t._v(t._s(t.$t("message.c267")))]),t._v(" "),s("span",{directives:[{name:"show",rawName:"v-show",value:this.totalInfo.size<20,expression:"this.totalInfo.size < 20"}]},[t._v(t._s(t.$t("message.c2671")))])]),t._v(" "),s("el-button",{attrs:{type:"primary",disabled:t.buttonIf},on:{click:t.tochangeWhole}},[t._v(t._s(t.$t("message.c269")))])],1)]),t._v(" "),s("Password",{ref:"password",attrs:{submitId:t.submitId},on:{toSubmit:t.toSubmit}})],1)},staticRenderFns:[]};var l=s("vSla")(i,c,!1,function(t){s("Ms6F")},null,null);e.default=l.exports}});