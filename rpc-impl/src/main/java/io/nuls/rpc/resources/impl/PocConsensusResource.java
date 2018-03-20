/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
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
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.dto.ConsensusAddressDTO;
import io.nuls.rpc.resources.form.CreateAgentForm;
import io.nuls.rpc.resources.form.WithdrawForm;
import io.nuls.rpc.resources.form.DepositForm;
import io.nuls.rpc.resources.form.StopAgentForm;

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
public class PocConsensusResource {
    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private UtxoOutputDataService outputDataService = NulsContext.getServiceBean(UtxoOutputDataService.class);
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getInfo(@QueryParam("address") String address) {
        AssertUtil.canNotEmpty(address, ErrorCode.NULL_PARAMETER);
        RpcResult result = RpcResult.getSuccess();
        ConsensusStatusInfo status = consensusService.getConsensusInfo(address);
        ConsensusAddressDTO dto = new ConsensusAddressDTO();
        //todo
        result.setData(dto);
        return result;
    }


    @POST
    @Path("/createAgent")
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
        consensusService.startConsensus(form.getAddress(), form.getPassword(), paramsMap);
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult in(DepositForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getAgentAddress());
        AssertUtil.canNotEmpty(form.getDeposit());
        AssertUtil.canNotEmpty(form.getPassword());
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("deposit", form.getDeposit());
        paramsMap.put("agentAddress", form.getAgentAddress());
        consensusService.startConsensus(form.getAddress(), form.getPassword(), paramsMap);
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/stopAgent")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult stopAgent(StopAgentForm form) throws NulsException, IOException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getPassword());
        consensusService.stopConsensus(form.getAddress(), form.getPassword(),null);
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/withdraw")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult out(WithdrawForm form) throws NulsException, IOException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getTxHash());
        Map<String,Object> params  = new HashMap<>();
        params.put("txHash",form.getTxHash());
        consensusService.stopConsensus(null,form.getPassword(),params);
        return RpcResult.getSuccess();
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
}
