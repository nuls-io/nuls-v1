webpackJsonp([18],{Jcl2:function(t,s){},eD5G:function(t,s,e){"use strict";Object.defineProperty(s,"__esModule",{value:!0});var a=e("LPk9"),n=e("KcW0"),o=e("6ROu"),d=e.n(o),r=e("FJop"),i=e("x47x"),g={data:function(){return{address:"",agentAddress:this.$route.query.agentAddress,agentHash:this.$route.query.agentHash,agentAddressInfo:[],myMortgageData:[],total:0,pageNumber:"1",outInfo:{address:"",txHash:""},myNodeSetInterval:null,toCheckOk:!1}},components:{Back:a.a,ProgressBar:n.a,Password:r.a},mounted:function(){var t=this;this.getAgentAddressInfo("/consensus/agent/"+this.agentHash),this.getAddressList("/consensus/deposit/address/"+localStorage.getItem("newAccountAddress"),{agentHash:this.agentHash,pageSize:"10",pageNumber:this.pageNumber}),this.myNodeSetInterval=setInterval(function(){t.getAgentAddressInfo("/consensus/agent/"+t.agentHash),t.getAddressList("/consensus/deposit/address/"+localStorage.getItem("newAccountAddress"),{agentHash:t.agentHash,pageSize:"10",pageNumber:t.pageNumber})},5e3)},destroyed:function(){clearInterval(this.myNodeSetInterval)},methods:{getAgentAddressInfo:function(t,s){var e=this;this.$fetch(t,s).then(function(t){if(t.success){var s=new i.BigNumber(1e-8);e.toCheckOk=t.data.agentAddress===localStorage.getItem("newAccountAddress"),t.data.deposit=parseFloat(s.times(t.data.deposit).toString()),t.data.creditVals=t.data.creditVal,t.data.creditVal=((t.data.creditVal+1)/2*100).toFixed().toString()+"%",t.data.agentAddresss=t.data.agentAddress.substr(0,10)+"..."+t.data.agentAddress.substr(-10),t.data.totalDeposits=(1e-8*t.data.totalDeposit).toFixed(0)+"/500000",t.data.totalDeposit>5e13?t.data.totalDeposit="100%":t.data.totalDeposit=(t.data.totalDeposit/5e11).toString()+"%",e.agentAddressInfo=t.data}})},getAddressList:function(t,s){var e=this;this.$fetch(t,s).then(function(t){if(t.success){var s=new i.BigNumber(1e-8);e.total=t.data.total;for(var a=0;a<t.data.list.length;a++)t.data.list[a].deposit=parseFloat(s.times(t.data.list[a].deposit).toString()),t.data.list[a].time=d()(t.data.list[a].time).format("YYYY-MM-DD HH:mm:ss");e.myMortgageData=t.data.list}})},myMortgageSize:function(t){this.pageNumber=t,this.getAddressList("/consensus/deposit/address/"+localStorage.getItem("newAccountAddress"),{agentHash:this.agentAddress,pageSize:"10",pageNumber:t})},toCheck:function(){this.$router.push({path:"/consensus/nodeInfo",query:{txHash:this.agentAddressInfo.txHash}})},addNode:function(){this.$store.getters.getNetWorkInfo.localBestHeight===this.$store.getters.getNetWorkInfo.netBestHeight?this.$router.push({path:"/consensus/myNode/addNode",query:{agentAddress:this.agentAddress,agentId:this.agentAddressInfo.agentHash}}):this.$message({message:this.$t("message.c133")})},outNode:function(t){var s=this;this.$confirm(this.$t("message.c60")+t.agentName+"？( "+this.$t("message.c51")+t.deposit+" NULS)",this.$t("message.c61"),{confirmButtonText:this.$t("message.confirmButtonText"),cancelButtonText:this.$t("message.cancelButtonText")}).then(function(){s.outInfo.address=t.address,s.outInfo.txHash=t.txHash,"true"===localStorage.getItem("encrypted")?s.$refs.password.showPassword(!0):s.toSubmit("")}).catch(function(){s.$message({type:"info",message:s.$t("message.c59")})})},toSubmit:function(t){var s=this,e={address:this.outInfo.address,password:t,txHash:this.outInfo.txHash};this.$post("/consensus/withdraw/",e).then(function(t){console.log(t),t.success?(s.$message({type:"success",message:s.$t("message.passWordSuccess")}),s.getAddressList("/consensus/deposit/address/"+localStorage.getItem("newAccountAddress"),{agentHash:s.agentHash,pageSize:"10",pageNumber:s.pageNumber})):s.$message({type:"warning",message:s.$t("message.passWordFailed")+t.data.msg}),s.outInfo.address="",s.outInfo.txHash=""})}}},c={render:function(){var t=this,s=t.$createElement,e=t._self._c||s;return e("div",{staticClass:"my-node"},[e("Back",{attrs:{backTitle:this.$t("message.consensusManagement")}}),t._v(" "),e("h2",[t._v(t._s(this.agentAddressInfo.agentId))]),t._v(" "),e("div",{staticClass:"div-icon1 node-page-top"},[e("p",{staticClass:"subscript",class:0===this.agentAddressInfo.status?"stay":""},[t._v("\n      "+t._s(t.$t("message.status"+this.agentAddressInfo.status))+"\n    ")]),t._v(" "),e("ul",[e("li",{staticClass:"li-bg overflow"},[e("label",[t._v(t._s(t.$t("message.c16"))+"：")]),t._v(t._s(this.agentAddressInfo.agentName?this.agentAddressInfo.agentName:this.agentAddressInfo.agentAddresss)+"\n        "),e("span",{directives:[{name:"show",rawName:"v-show",value:t.toCheckOk,expression:"toCheckOk"}],staticClass:"cursor-p text-d",on:{click:t.toCheck}},[t._v(t._s(t.$t("message.c5_1")))])]),t._v(" "),e("li",[e("label",[t._v(t._s(t.$t("message.c17"))+"：")]),t._v(t._s(this.agentAddressInfo.commissionRate)+"%\n      ")]),t._v(" "),e("li",[e("label",[t._v(t._s(t.$t("message.c25"))+"：")]),t._v(t._s(this.agentAddressInfo.deposit)+"\n        NULS\n      ")]),t._v(" "),e("li",[e("label",[t._v(t._s(t.$t("message.c19"))+"：")]),t._v(t._s(this.agentAddressInfo.memberCount)+"\n      ")]),t._v(" "),e("li",[e("label",[t._v(t._s(t.$t("message.c18"))+"：")]),t._v(" "),e("ProgressBar",{attrs:{colorData:this.agentAddressInfo.creditVals<0?"#f64b3e":"#82bd39",widthData:this.agentAddressInfo.creditVal}}),t._v(" "),e("span",[t._v(" "+t._s(this.agentAddressInfo.creditVals))])],1),t._v(" "),e("li",[e("label",[t._v(t._s(t.$t("message.c47"))+"：")]),t._v(" "),e("ProgressBar",{attrs:{colorData:"#58a5c9",widthData:this.agentAddressInfo.totalDeposit}}),t._v(" "),e("span",[t._v(" "+t._s(this.agentAddressInfo.totalDeposits))])],1)])]),t._v(" "),e("div",{staticClass:"my-node-bottom"},[e("div",{staticClass:"my-node-list"},[t._v("\n      "+t._s(t.$t("message.c56"))+"\n      "),e("span",{staticClass:"text-d cursor-p fr",on:{click:t.addNode}},[t._v(t._s(t.$t("message.c57")))])]),t._v(" "),e("el-table",{attrs:{data:t.myMortgageData}},[e("el-table-column",{attrs:{prop:"deposit",label:t.$t("message.c51"),"min-width":"100",align:"center"}}),t._v(" "),e("el-table-column",{attrs:{label:t.$t("message.state"),width:"70",align:"center"},scopedSlots:t._u([{key:"default",fn:function(s){return[t._v("\n          "+t._s(t.$t("message.status"+s.row.status))+"\n        ")]}}])}),t._v(" "),e("el-table-column",{attrs:{prop:"time",label:t.$t("message.c49"),"min-width":"85",align:"center"}}),t._v(" "),e("el-table-column",{attrs:{label:t.$t("message.operation"),align:"center"},scopedSlots:t._u([{key:"default",fn:function(s){return[e("el-button",{attrs:{type:"text",size:"small"},on:{click:function(e){t.outNode(s.row)}}},[t._v(t._s(t.$t("message.c58"))+"\n          ")])]}}])})],1),t._v(" "),e("el-pagination",{directives:[{name:"show",rawName:"v-show",value:t.totalOK=this.total>10,expression:"totalOK = this.total > 10 ? true:false"}],staticClass:"cb",attrs:{layout:"prev, pager, next","page-size":10,total:this.total},on:{"current-change":t.myMortgageSize}})],1),t._v(" "),e("Password",{ref:"password",on:{toSubmit:t.toSubmit}})],1)},staticRenderFns:[]};var l=e("vSla")(g,c,!1,function(t){e("Jcl2")},null,null);s.default=l.exports}});