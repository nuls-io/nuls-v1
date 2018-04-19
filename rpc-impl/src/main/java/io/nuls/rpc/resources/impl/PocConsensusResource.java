/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.rpc.resources.impl;

import io.nuls.account.entity.Address;
import io.nuls.consensus.poc.service.PocConsensusService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Transaction;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.dto.WholeNetConsensusInfoDTO;
import io.nuls.rpc.resources.form.CreateAgentForm;
import io.nuls.rpc.resources.form.DepositForm;
import io.nuls.rpc.resources.form.StopAgentForm;
import io.nuls.rpc.resources.form.WithdrawForm;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/consensus")
@Api(value = "/browse", description = "Consensus")
public class PocConsensusResource {
    private PocConsensusService consensusService = NulsContext.getServiceBean(PocConsensusService.class);

    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);

    public PocConsensusResource() {
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Get the whole network consensus infomation! 查询全网共识总体信息 [3.6.1]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = WholeNetConsensusInfoDTO.class)
    })
    public RpcResult getWholeInfo() {
        RpcResult result = RpcResult.getSuccess();
        WholeNetConsensusInfoDTO dto = new WholeNetConsensusInfoDTO();

        Map<String, Object> map = this.consensusService.getConsensusInfo();
        dto.setAgentCount((Integer) map.get("agentCount"));
        dto.setRewardOfDay((Long) map.get("rewardOfDay"));
        dto.setTotalDeposit((Long) map.get("totalDeposit"));
        dto.setConsensusAccountNumber((Integer) map.get("memberCount"));
        result.setData(dto);
        return result;

    }

    @GET
    @Path("/local")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("获取钱包内(本地)全部账户参与共识信息 [3.6.2b]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Map.class)
    })
    public RpcResult getInfo() {
        RpcResult result = RpcResult.getSuccess();
        Map<String, Object> dataMap = new HashMap<>();//todo consensusService.getConsensusInfo(null);
        result.setData(dataMap);
        return result;
    }

    @GET
    @Path("/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("获取钱包内某个账户参与共识信息 [3.6.2a]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Map.class)
    })
    public RpcResult getInfo(@PathParam("address") String address) {

        if (!Address.validAddress(StringUtils.formatStringPara(address))) {
            return RpcResult.getFailed(ErrorCode.ADDRESS_ERROR);
        }
        RpcResult result = RpcResult.getSuccess();

        Map<String, Object> dataMap = consensusService.getConsensusInfo(address);
        result.setData(dataMap);
        return result;
    }

    @POST
    @Path("/agent")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create an agent for consensus! 创建共识(代理)节点 [3.6.3]", notes = "返回创建的节点成功的交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = String.class)
    })
    public RpcResult createAgent(CreateAgentForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAgentAddress());
        AssertUtil.canNotEmpty(form.getAgentName());
        AssertUtil.canNotEmpty(form.getPackingAddress());
        AssertUtil.canNotEmpty(form.getDeposit());
        AssertUtil.canNotEmpty(form.getRemark());
        AssertUtil.canNotEmpty(form.getPassword());

        if (!Address.validAddress(form.getPackingAddress()) || !Address.validAddress(form.getAgentAddress())) {
            throw new NulsRuntimeException(ErrorCode.PARAMETER_ERROR);
        }

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("deposit", form.getDeposit());
        paramsMap.put("packingAddress", form.getPackingAddress());
        paramsMap.put("introduction", form.getRemark());
        paramsMap.put("commissionRate", form.getCommissionRate());
        paramsMap.put("agentName", form.getAgentName());
        Transaction tx = consensusService.startConsensus(form.getAgentAddress(), form.getPassword(), paramsMap);
        return RpcResult.getSuccess().setData(tx.getHash().getDigestHex());
    }


    @POST
    @Path("/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "deposit nuls to a bank! 申请参与共识 [3.6.4]", notes = "返回申请成功交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = String.class)
    })
    public RpcResult depositToAgent(DepositForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getAgentId());
        AssertUtil.canNotEmpty(form.getDeposit());
        AssertUtil.canNotEmpty(form.getPassword());
        Map<String, Object> paramsMap = new HashMap<>();
        if (!Address.validAddress(form.getAddress())) {
            throw new NulsRuntimeException(ErrorCode.PARAMETER_ERROR);
        }

        paramsMap.put("deposit", form.getDeposit());
        paramsMap.put("agentHash", form.getAgentId());
        Transaction tx = consensusService.startConsensus(form.getAddress(), form.getPassword(), paramsMap);
        return RpcResult.getSuccess().setData(tx.getHash().getDigestHex());
    }


    @POST
    @Path("/agent/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "删除共识节点 [3.6.5]", notes = "返回删除成功交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = String.class)
    })
    public RpcResult stopAgent(StopAgentForm form) throws NulsException, IOException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getPassword());
        Transaction tx = consensusService.stopConsensus(form.getAddress(), form.getPassword(), null);
        return RpcResult.getSuccess().setData(tx.getHash().getDigestHex());
    }

//
//    @GET
//    @Path("/profit")
//    @Produces(MediaType.APPLICATION_JSON)
//    public RpcResult profit(@QueryParam("address") String address) {
//        Map<String, Object> map = new HashMap<>();
//        if (!Address.validAddress(address)) {
//            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
//        }
//        if (address == null) {
//            address = accountService.getDefaultAccount().getAddress().getBase58();
//        }
//
//        // get all reward
//        List<UtxoOutputPo> outputList = outputDataService.getAccountOutputs(TransactionConstant.TX_TYPE_COIN_BASE, address, null, null);
//        long value = 0;
//        for (UtxoOutputPo output : outputList) {
//            value += output.getValue();
//        }
//        map.put("profit", Na.valueOf(value).toDouble());
//
//        // get last 24 hours reward
//        long nowTime = TimeService.currentTimeMillis();
//        nowTime = nowTime - DateUtil.DATE_TIME;
//        outputList = outputDataService.getAccountOutputs(TransactionConstant.TX_TYPE_COIN_BASE, address, nowTime, null);
//        value = 0;
//        for (UtxoOutputPo output : outputList) {
//            value += output.getValue();
//        }
//        map.put("lastProfit", Na.valueOf(value).toDouble());
//        map.put("investment", NulsContext.INVESTMENT.toDouble());
//        RpcResult rpcResult = RpcResult.getSuccess();
//        rpcResult.setData(map);
//        return rpcResult;
//    }


    @GET
    @Path("/agent/list")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询共识节点列表信息 [3.6.6]", notes = "result.data: Page<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Page.class)
    })
    public RpcResult getAgentList(@ApiParam(name="pageNumber", value="页码")
                                      @QueryParam("pageNumber") Integer pageNumber,
                                  @ApiParam(name="pageSize", value="每页条数")
                                    @QueryParam("pageSize") Integer pageSize,
                                  @ApiParam(name="keyword", value="搜索关键字")
                                    @QueryParam("keyword") String keyword,
                                  @ApiParam(name="sortType", value="排序字段名")
                                    @QueryParam("sortType") String sortType) {
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        RpcResult result = RpcResult.getSuccess();
        Page<Map<String, Object>> list = this.consensusService.getAgentList(keyword, null, null, sortType, pageNumber, pageSize);
        result.setData(list);
        return result;
    }

    @GET
    @Path("/agent/{agentAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询共识节点详细信息 [3.6.7]", notes = "result.data: Map<String, Object>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Map.class)
    })
    public RpcResult getAgentByAddress(@ApiParam(name="agentAddress", value="节点地址", required = true)
                                           @PathParam("agentAddress") String agentAddress) {
        if (!Address.validAddress(agentAddress)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        RpcResult result = RpcResult.getSuccess();
        Map<String, Object> data = this.consensusService.getAgent(agentAddress);
        result.setData(data);
        return result;
    }

    @GET
    @Path("/agent/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据地址查询其委托的节点列表 [3.6.12]", notes = "result.data: Page<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Page.class)
    })
    public RpcResult getAgentListByDepositAddress(@ApiParam(name="pageNumber", value="页码")
                                                      @QueryParam("pageNumber") Integer pageNumber,
                                                  @ApiParam(name="pageSize", value="每页条数")
                                                    @QueryParam("pageSize") Integer pageSize,
                                                  @ApiParam(name="address", value="账户地址", required = true)
                                                    @PathParam("address") String address) {
        RpcResult result = RpcResult.getSuccess();

        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        Page<Map<String, Object>> list = this.consensusService.getAgentList(null, address, null, null, pageNumber, pageSize);
        result.setData(list);
        return result;
    }

    @GET
    @Path("/deposit/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询指定地址参与的所有委托信息列表 [3.6.8]",
                    notes = "result.data: Page<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Page.class)
    })
    public RpcResult getDepositListByAddress(@ApiParam(name="address", value="账户地址", required = true)
                                                 @PathParam("address") String address,
                                             @ApiParam(name="pageNumber", value="页码")
                                                @QueryParam("pageNumber") Integer pageNumber,
                                             @ApiParam(name="pageSize", value="每页条数")
                                                @QueryParam("pageSize") Integer pageSize,
                                             @ApiParam(name="agentAddress", value="指定代理节点地址(不传查所有)")
                                                @QueryParam("agentAddress") String agentAddress) {
        AssertUtil.canNotEmpty(address);
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        RpcResult result = RpcResult.getSuccess();

        Page<Map<String, Object>> page = this.consensusService.getDepositList(address, agentAddress, pageNumber, pageSize);
        result.setData(page);
        return result;
    }

    @GET
    @Path("/deposit/agent/{agentAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询共识节点受托列表信息 [3.6.9]",
            notes = "result.data: Page<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Page.class)
    })
    public RpcResult queryDepositListByAgentAddress(@ApiParam(name="agentAddress", value="指定代理节点地址", required = true)
                                                        @PathParam("agentAddress") String agentAddress,
                                                    @ApiParam(name="pageNumber", value="页码")
                                                    @QueryParam("pageNumber") Integer pageNumber,
                                                    @ApiParam(name="pageSize", value="每页条数")
                                                    @QueryParam("pageSize") Integer pageSize) {
        AssertUtil.canNotEmpty(agentAddress);

        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        RpcResult result = RpcResult.getSuccess();

        Page<Map<String, Object>> page = this.consensusService.getDepositList(null, agentAddress, pageNumber, pageSize);
        result.setData(page);
        return result;
    }

    @GET
    @Path("/agent/status")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询所有共识信息状态 [3.6.10]",
            notes = "result.data: Map<String, Object>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Map.class)
    })
    public RpcResult getAllAgentStatusList() {
        RpcResult rpcResult = RpcResult.getSuccess();
        List<AgentPo> poList = agentDataService.getList();
        if (null == poList || poList.isEmpty()) {
            return rpcResult;
        }
        Map<String, Object> statusMap = new HashMap<>();
        for (AgentPo po : poList) {
            statusMap.put(po.getAgentAddress(), po.getStatus());
        }
        return rpcResult.setData(statusMap);
    }

    @POST
    @Path("/withdraw")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "退出共识 [3.6.11]",
            notes = "返回退出成功的交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = String.class)
    })
    public RpcResult exitConsensus(WithdrawForm form) {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getTxHash());
        AssertUtil.canNotEmpty(form.getPassword());
        AssertUtil.canNotEmpty(form.getAddress());
        if (!Address.validAddress(form.getAddress())) {
            return RpcResult.getFailed(ErrorCode.ADDRESS_ERROR);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("txHash", form.getTxHash());
        Transaction tx = null;
        try {
            tx = consensusService.stopConsensus(form.getAddress(), form.getPassword(), params);
        } catch (NulsException e) {
            Log.error(e);
            return RpcResult.getFailed(e.getMessage());
        } catch (IOException e) {
            Log.error(e);
            return RpcResult.getFailed(e.getMessage());
        }
        return RpcResult.getSuccess().setData(tx.getHash().getDigestHex());
    }
}
