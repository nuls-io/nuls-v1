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
import io.nuls.account.util.AccountTool;
import io.nuls.consensus.entity.AgentInfo;
import io.nuls.consensus.entity.DepositItem;
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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/consensus")
public class PocConsensusResource {
    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private UtxoOutputDataService outputDataService = NulsContext.getServiceBean(UtxoOutputDataService.class);
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);

    private AgentDataService delegateAccountDataService = NulsContext.getServiceBean(AgentDataService.class);

    //todo 临时使用的
    private int temp = 1;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getWholeInfo() {
        RpcResult result = RpcResult.getSuccess();
        WholeNetConsensusInfoDTO dto = new WholeNetConsensusInfoDTO();
        if (temp == 1) {
            dto.setAgentCount(18);
            dto.setRewardOfDay(20112345678L);
            dto.setTotalDeposit(321000000000L);
            dto.setConsensusAccountNumber(10000);
            result.setData(dto);
            return result;
        }
        //todo
        this.consensusService.getConsensusInfo();
        return null;
    }

    @GET
    @Path("/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getInfo(@PathParam("address") String address) {
        RpcResult result = RpcResult.getSuccess();
        ConsensusInfoDTO dto = new ConsensusInfoDTO();
        if (temp == 1) {
            if (StringUtils.isBlank(address)) {
                dto.setAgentCount(2);
                dto.setConsensusAccountCount(10);
                dto.setReward(1234500000000L);
                dto.setRewardOfDay(234500000000L);
                dto.setTotalDeposit(300000000000000L);
                dto.setUsableBalance(2234500000000L);
            } else {
                dto.setAgentCount(0);
                dto.setConsensusAccountCount(2);
                dto.setReward(5500000000L);
                dto.setRewardOfDay(1500000000L);
                dto.setTotalDeposit(20000000000000L);
                dto.setUsableBalance(234500000000L);
            }
            result.setData(dto);
            return result;
        }
        Map<String, Object> dataMap = consensusService.getConsensusInfo(address);
        //todo
        result.setData(dto);
        return result;
    }

    @POST
    @Path("/agent")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult createAgent(CreateAgentForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getAgentName());
        AssertUtil.canNotEmpty(form.getPackingAddress());
        AssertUtil.canNotEmpty(form.getDeposit());
        AssertUtil.canNotEmpty(form.getRemark());
        AssertUtil.canNotEmpty(form.getPassword());
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("deposit", form.getDeposit());
        paramsMap.put("agentAddress", form.getPackingAddress());
        paramsMap.put("introduction", form.getRemark());
        paramsMap.put("commissionRate", form.getCommissionRate());
        paramsMap.put("agentName", form.getAgentName());
        Transaction tx = consensusService.startConsensus(form.getAddress(), form.getPassword(), paramsMap);
        return RpcResult.getSuccess().setData(tx.getHash().getDigestHex());
    }


    @POST
    @Path("/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult depositToAgent(DepositForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getAgentAddress());
        AssertUtil.canNotEmpty(form.getDeposit());
        AssertUtil.canNotEmpty(form.getPassword());
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("deposit", form.getDeposit());
        paramsMap.put("agentAddress", form.getAgentAddress());
        Transaction tx = consensusService.startConsensus(form.getAddress(), form.getPassword(), paramsMap);
        return RpcResult.getSuccess().setData(tx.getHash().getDigestHex());
    }


    @DELETE
    @Path("/agent")
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

        RpcResult result = RpcResult.getSuccess();
        Page<AgentInfo> listPage = new Page<>();
        if (temp == 1) {
            listPage.setPageNumber(pageNumber);
            listPage.setPageSize(pageSize);
            listPage.setTotal(pageSize * 3);
            listPage.setPages(3);
            List<AgentInfo> list = new ArrayList<>();
            for (int i = 0; i < pageSize; i++) {
                AgentInfo item = new AgentInfo();
                item.setCommissionRate(15);
                item.setCreditRatio(0.9);
                item.setStatus(2);
                item.setMemberCount(3 + pageNumber);
                item.setOwndeposit(Na.parseNuls(50000 + pageNumber));
                item.setTotalDeposit(Na.parseNuls(300000 + pageNumber));
                item.setIntroduction("哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈" + pageNumber);
                item.setAgentAddress("2CYdNLysoMbPRc4Q5YsVreT99Q61ZSg");
                item.setAgentName("超级节点" + i + pageNumber);
                item.setStartTime(System.currentTimeMillis());
                item.setPackedCount(11234 + i * pageNumber);
                item.setReward(Na.parseNuls(1120 * i + pageNumber));
                list.add(item);
            }
            listPage.setList(list);
            result.setData(listPage);
            return result;
        }

        Page<AgentInfo> pageDto = new Page<>();
        List<AgentInfo> dtoList = new ArrayList<>();
        //todo
        pageDto.setList(dtoList);
        result.setData(pageDto);
        return result;
    }


    @GET
    @Path("/agent/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getAgentListByDepositAddress(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize, @PathParam("address") String address) {
        RpcResult result = RpcResult.getSuccess();
        Page<AgentInfo> listPage = new Page<>();
        if (temp == 1) {
            listPage.setPageNumber(pageNumber);
            listPage.setPageSize(pageSize);
            listPage.setTotal(pageSize * 3);
            listPage.setPages(3);
            List<AgentInfo> list = new ArrayList<>();
            for (int i = 0; i < pageSize; i++) {
                AgentInfo item = new AgentInfo();
                item.setCommissionRate(15);
                item.setCreditRatio(0.9);
                item.setStatus(2);
                item.setMemberCount(3 + pageNumber);
                item.setOwndeposit(Na.parseNuls(50000 + pageNumber));
                item.setTotalDeposit(Na.parseNuls(300000 + pageNumber));
                item.setIntroduction("哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈" + pageNumber);
                item.setAgentAddress("2CYdNLysoMbPRc4Q5YsVreT99Q61ZSg");
                item.setAgentName("超级节点" + i + pageNumber);
                item.setStartTime(System.currentTimeMillis());
                item.setPackedCount(11234 + i * pageNumber);
                item.setReward(Na.parseNuls(1120 * i + pageNumber));
                list.add(item);
            }
            listPage.setList(list);
            result.setData(listPage);
            return result;
        }

        AgentInfo agentInfo = new AgentInfo();
        //todo

        result.setData(agentInfo);
        return result;
    }

    @GET
    @Path("/deposit/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getDepositListByAddress(@PathParam("address") String address,
                                             @QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize, @QueryParam("agentAddress") String agentAddress) {
        RpcResult result = RpcResult.getSuccess();
        Page<DepositItem> listPage = new Page<>();
        if (temp == 1) {
            listPage.setPageNumber(pageNumber);
            listPage.setPageSize(pageSize);
            listPage.setTotal(pageSize * 3);
            listPage.setPages(3);
            List<DepositItem> list = new ArrayList<>();
            for (int i = 0; i < pageSize; i++) {
                DepositItem item = new DepositItem();
                item.setAddress(address);
                item.setAmount(1000000000);
                item.setDepositTime(System.currentTimeMillis());
                item.setStatus(2);
                item.setAgentName("二货节点"+i);
                if (StringUtils.isNotBlank(agentAddress)) {
                    item.setAgentAddress(agentAddress);
                } else {
                    try {
                        item.setAgentAddress(AccountTool.createAccount().getAddress().toString());
                    } catch (NulsException e) {
                        Log.error(e);
                    }
                }
                list.add(item);
            }
            listPage.setList(list);
            result.setData(listPage);
            return result;
        }
        //todo 排序：委托时间倒序
        return result;
    }

    @GET
    @Path("/deposit/agent/{agentAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult queryDepositListByAgentAddress(@PathParam("agentAddress") String agentAddress,
                                                    @QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 10;
        }
        RpcResult result = RpcResult.getSuccess();
        Page<DepositItem> listPage = new Page<>();
        if (temp == 1) {
            listPage.setPageNumber(pageNumber);
            listPage.setPageSize(pageSize);
            listPage.setTotal(pageSize * 3);
            listPage.setPages(3);
            List<DepositItem> list = new ArrayList<>();
            for (int i = 0; i < pageSize; i++) {
                DepositItem item = new DepositItem();
                try {
                    item.setAddress(AccountTool.createAccount().getAddress().toString());
                } catch (NulsException e) {
                    Log.error(e);
                }
                item.setAgentName("二货节点"+i);
                item.setAmount(100000000 + i);
                item.setDepositTime(System.currentTimeMillis());
                item.setStatus(i / 2);
                item.setAgentAddress(agentAddress);
                list.add(item);
            }
            listPage.setList(list);
            result.setData(listPage);
            return result;
        }
        if (!StringUtils.validAddress(agentAddress)) {
            return RpcResult.getFailed(ErrorCode.ADDRESS_ERROR);
        }

        Page<DepositItem> pageDto = new Page<>();
        List<DepositItem> dtoList = new ArrayList<>();
        //todo 排序：委托时间倒序
        pageDto.setList(dtoList);
        result.setData(pageDto);
        return result;
    }

    @GET
    @Path("/agent/status")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getAllAgentStatusList() {
        RpcResult rpcResult = RpcResult.getSuccess();
        List<AgentPo> polist = delegateAccountDataService.getList();
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
    public RpcResult exitConsensus(WithdrawForm form) throws NulsException, IOException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getTxHash());
        AssertUtil.canNotEmpty(form.getPassword());
        AssertUtil.canNotEmpty(form.getAddress());
        Map<String, Object> params = new HashMap<>();
        params.put("txHash", form.getTxHash());
        Transaction tx = consensusService.stopConsensus(form.getAddress(), form.getPassword(), params);
        return RpcResult.getSuccess().setData(tx.getHash().getDigestHex());
    }
}
