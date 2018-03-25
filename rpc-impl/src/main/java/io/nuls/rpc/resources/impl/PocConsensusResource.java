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

import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.dto.ConsensusInfoDTO;
import io.nuls.rpc.resources.dto.WholeNetConsensusInfoDTO;
import io.nuls.rpc.resources.form.CreateAgentForm;
import io.nuls.rpc.resources.form.WithdrawForm;
import io.nuls.rpc.resources.form.DepositForm;
import io.nuls.rpc.resources.form.StopAgentForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private UtxoOutputDataService outputDataService = NulsContext.getServiceBean(UtxoOutputDataService.class);
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);

    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);

    public PocConsensusResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Get the whole network consensus infomation!")
    public RpcResult getWholeInfo() {
        RpcResult result = RpcResult.getSuccess();
        WholeNetConsensusInfoDTO dto = new WholeNetConsensusInfoDTO();
//        if (temp == 1) {
//            dto.setAgentCount(18);
//            dto.setRewardOfDay(20112345678L);
//            dto.setTotalDeposit(321000000000L);
//            dto.setConsensusAccountNumber(10000);
//            result.setData(dto);
//            return result;
//        }
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
    public RpcResult getInfo() {
        RpcResult result = RpcResult.getSuccess();
//        ConsensusInfoDTO dto = new ConsensusInfoDTO();
//        if (temp == 1) {
//            dto.setAgentCount(2);
//            dto.setConsensusAccountCount(10);
//            dto.setReward(1234500000000L);
//            dto.setRewardOfDay(234500000000L);
//            dto.setTotalDeposit(300000000000000L);
//            dto.setUsableBalance(2234500000000L);
//            result.setData(dto);
//            return result;
//        }
        Map<String, Object> dataMap = consensusService.getConsensusInfo(null);
        result.setData(dataMap);
        return result;
    }

    @GET
    @Path("/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getInfo(@PathParam("address") String address) {
        AssertUtil.canNotEmpty(address);
        RpcResult result = RpcResult.getSuccess();
//        ConsensusInfoDTO dto = new ConsensusInfoDTO();
//        if (temp == 1) {
//            dto.setAgentCount(0);
//            dto.setConsensusAccountCount(2);
//            dto.setReward(5500000000L);
//            dto.setRewardOfDay(1500000000L);
//            dto.setTotalDeposit(20000000000000L);
//            dto.setUsableBalance(234500000000L);
//            result.setData(dto);
//            return result;
//        }
        Map<String, Object> dataMap = consensusService.getConsensusInfo(address);
        result.setData(dataMap);
        return result;
    }

    @POST
    @Path("/agent")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Create an agent for consensus!")
    public RpcResult createAgent(CreateAgentForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAgentAddress());
        AssertUtil.canNotEmpty(form.getAgentName());
        AssertUtil.canNotEmpty(form.getPackingAddress());
        AssertUtil.canNotEmpty(form.getDeposit());
        AssertUtil.canNotEmpty(form.getRemark());
        AssertUtil.canNotEmpty(form.getPassword());
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
    @ApiOperation("deposit nuls to a bank!")
    public RpcResult depositToAgent(DepositForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getAgentId());
        AssertUtil.canNotEmpty(form.getDeposit());
        AssertUtil.canNotEmpty(form.getPassword());
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("deposit", form.getDeposit());
        paramsMap.put("agentHash", form.getAgentId());
        Transaction tx = consensusService.startConsensus(form.getAddress(), form.getPassword(), paramsMap);
        return RpcResult.getSuccess().setData(tx.getHash().getDigestHex());
    }


    @POST
    @Path("/agent/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult stopAgent(StopAgentForm form) throws NulsException, IOException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getPassword());
        Transaction tx = consensusService.stopConsensus(form.getAddress(), form.getPassword(), null);
        return RpcResult.getSuccess().setData(tx.getHash().getDigestHex());
    }


    @GET
    @Path("/profit")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult profit(@QueryParam("address") String address) {
        Map<String, Object> map = new HashMap<>();
        if ((address != null && !StringUtils.validAddress(address))) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (address == null) {
            address = accountService.getDefaultAccount().getAddress().getBase58();
        }

        // get all reward
        List<UtxoOutputPo> outputList = outputDataService.getAccountOutputs(TransactionConstant.TX_TYPE_COIN_BASE, address, null, null);
        long value = 0;
        for (UtxoOutputPo output : outputList) {
            value += output.getValue();
        }
        map.put("profit", Na.valueOf(value).toDouble());

        // get last 24 hours reward
        long nowTime = TimeService.currentTimeMillis();
        nowTime = nowTime - DateUtil.DATE_TIME;
        outputList = outputDataService.getAccountOutputs(TransactionConstant.TX_TYPE_COIN_BASE, address, nowTime, null);
        value = 0;
        for (UtxoOutputPo output : outputList) {
            value += output.getValue();
        }
        map.put("lastProfit", Na.valueOf(value).toDouble());
        map.put("investment", NulsContext.INVESTMENT.toDouble());
        RpcResult rpcResult = RpcResult.getSuccess();
        rpcResult.setData(map);
        return rpcResult;
    }


    @GET
    @Path("/agent/list")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getAgentList(@QueryParam("pageNumber") Integer pageNumber, @QueryParam("pageSize") Integer pageSize,
                                  @QueryParam("keyword") String keyword, @QueryParam("sortType") String sortType) {

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
//        Page<AgentInfo> listPage = new Page<>();
//        if (temp == 1) {
//            listPage.setPageNumber(pageNumber);
//            listPage.setPageSize(pageSize);
//            listPage.setTotal(pageSize * 3);
//            listPage.setPages(3);
//            List<AgentInfo> list = new ArrayList<>();
//            for (int i = 0; i < pageSize; i++) {
//                AgentInfo item = new AgentInfo();
//                item.setAgentId(StringUtils.getNewUUID());
//                item.setCommissionRate(15);
//                item.setCreditRatio(0.9);
//                item.setStatus(2);
//                item.setMemberCount(3 + pageNumber);
//                item.setOwndeposit(Na.parseNuls(50000 + pageNumber));
//                item.setTotalDeposit(Na.parseNuls(300000 + pageNumber));
//                item.setIntroduction("哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈" + pageNumber);
//                item.setAgentAddress("2CYdNLysoMbPRc4Q5YsVreT99Q61ZSg");
//                item.setAgentName("超级节点" + i + pageNumber);
//                item.setStartTime(System.currentTimeMillis());
//                item.setPackedCount(11234 + i * pageNumber);
//                item.setReward(Na.parseNuls(1120 * i + pageNumber));
//                list.add(item);
//            }
//            listPage.setList(list);
//            result.setData(listPage);
//            return result;
//        }
        Page<Map<String, Object>> list = this.consensusService.getAgentList(keyword, null, null, sortType, pageNumber, pageSize);
        result.setData(list);
        return result;
    }

    @GET
    @Path("/agent/{agentAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getAgentByAddress(@PathParam("agentAddress") String agentAddress) {
        RpcResult result = RpcResult.getSuccess();
        Map<String, Object> data = this.consensusService.getAgent(agentAddress);
        result.setData(data);
        return result;
    }

    @GET
    @Path("/agent/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getAgentListByDepositAddress(@QueryParam("pageNumber") Integer pageNumber, @QueryParam("pageSize") Integer pageSize, @PathParam("address") String address) {
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
//        Page<AgentInfo> listPage = new Page<>();
//        if (temp == 1) {
//            listPage.setPageNumber(pageNumber);
//            listPage.setPageSize(pageSize);
//            listPage.setTotal(pageSize * 3);
//            listPage.setPages(3);
//            List<AgentInfo> list = new ArrayList<>();
//            for (int i = 0; i < pageSize; i++) {
//                AgentInfo item = new AgentInfo();
//                item.setCommissionRate(15);
//                item.setCreditRatio(0.9);
//                item.setStatus(2);
//                item.setAgentId(StringUtils.getNewUUID());
//                item.setMemberCount(3 + pageNumber);
//                item.setOwndeposit(Na.parseNuls(50000 + pageNumber));
//                item.setTotalDeposit(Na.parseNuls(300000 + pageNumber));
//                item.setIntroduction("哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈" + pageNumber);
//                item.setAgentAddress("2CYdNLysoMbPRc4Q5YsVreT99Q61ZSg");
//                item.setAgentName("超级节点" + i + pageNumber);
//                item.setStartTime(System.currentTimeMillis());
//                item.setPackedCount(11234 + i * pageNumber);
//                item.setReward(Na.parseNuls(1120 * i + pageNumber));
//                list.add(item);
//            }
//            listPage.setList(list);
//            result.setData(listPage);
//            return result;
//        }

        Page<Map<String, Object>> list = this.consensusService.getAgentList(null, address, null, null, pageNumber, pageSize);
        result.setData(list);
        return result;
    }

    @GET
    @Path("/deposit/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getDepositListByAddress(@PathParam("address") String address,
                                             @QueryParam("pageNumber") Integer pageNumber, @QueryParam("pageSize") Integer pageSize, @QueryParam("agentAddress") String agentAddress) {
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
//        Page<DepositItem> listPage = new Page<>();
//        if (temp == 1) {
//            listPage.setPageNumber(pageNumber);
//            listPage.setPageSize(pageSize);
//            listPage.setTotal(pageSize * 3);
//            listPage.setPages(3);
//            List<DepositItem> list = new ArrayList<>();
//            for (int i = 0; i < pageSize; i++) {
//                DepositItem item = new DepositItem();
//                item.setAddress(address);
//                item.setAmount(1000000000);
//                item.setDepositTime(System.currentTimeMillis());
//                item.setStatus(2);
//                item.setAgentName("二货节点" + i);
//                if (StringUtils.isNotBlank(agentAddress)) {
//                    item.setAgentAddress(agentAddress);
//                } else {
//                    try {
//                        item.setAgentAddress(AccountTool.createAccount().getAddress().toString());
//                    } catch (NulsException e) {
//                        Log.error(e);
//                    }
//                }
//                list.add(item);
//            }
//            listPage.setList(list);
//            result.setData(listPage);
//            return result;
//        }

        Page<Map<String, Object>> page = this.consensusService.getDepositList(address, agentAddress, pageNumber, pageSize);
        result.setData(page);
        return result;
    }

    @GET
    @Path("/deposit/agent/{agentAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult queryDepositListByAgentAddress(@PathParam("agentAddress") String agentAddress,
                                                    @QueryParam("pageNumber") Integer pageNumber, @QueryParam("pageSize") Integer pageSize) {
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
//        Page<DepositItem> listPage = new Page<>();
//        if (temp == 1) {
//            listPage.setPageNumber(pageNumber);
//            listPage.setPageSize(pageSize);
//            listPage.setTotal(pageSize * 3);
//            listPage.setPages(3);
//            List<DepositItem> list = new ArrayList<>();
//            for (int i = 0; i < pageSize; i++) {
//                DepositItem item = new DepositItem();
//                try {
//                    item.setAddress(AccountTool.createAccount().getAddress().toString());
//                } catch (NulsException e) {
//                    Log.error(e);
//                }
//                item.setAgentName("二货节点" + i);
//                item.setAmount(100000000 + i);
//                item.setDepositTime(System.currentTimeMillis());
//                item.setStatus(i / 2);
//                item.setAgentAddress(agentAddress);
//                list.add(item);
//            }
//            listPage.setList(list);
//            result.setData(listPage);
//            return result;
//        }
        Page<Map<String, Object>> page = this.consensusService.getDepositList(null, agentAddress, pageNumber, pageSize);
        result.setData(page);
        return result;
    }

    @GET
    @Path("/agent/status")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getAllAgentStatusList() {
        RpcResult rpcResult = RpcResult.getSuccess();
        List<AgentPo> polist = agentDataService.getList();
        if (null == polist || polist.isEmpty()) {
            return rpcResult;
        }
        Map<String, Integer> statusMap = new HashMap<>();
        for (AgentPo po : polist) {
            statusMap.put(po.getAgentAddress(), po.getStatus());
        }
        return rpcResult.setData(statusMap);
    }

    @POST
    @Path("/withdraw")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult exitConsensus(WithdrawForm form) {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getTxHash());
        AssertUtil.canNotEmpty(form.getPassword());
        AssertUtil.canNotEmpty(form.getAddress());
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
