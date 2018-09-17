/*
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
 *
 */

package io.nuls.consensus.poc.rpc.resource;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.Account;
import io.nuls.account.service.AccountService;
import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.protocol.constant.PocConsensusErrorCode;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.CancelDeposit;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.entity.StopAgent;
import io.nuls.consensus.poc.protocol.tx.CancelDepositTransaction;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.DepositTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.protocol.util.PoConvertUtil;
import io.nuls.consensus.poc.rpc.model.*;
import io.nuls.consensus.poc.rpc.utils.AgentComparator;
import io.nuls.consensus.poc.service.impl.PocRewardCacheService;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.consensus.poc.util.ConsensusTool;
import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;

import io.nuls.kernel.script.*;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.service.TransactionService;
import io.swagger.annotations.*;
import sun.management.resources.agent;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Niels
 */
@Path("/consensus")
@Api(value = "/consensus", description = "poc-consensus")
@Component
public class PocConsensusResource {

    @Autowired
    private ConsensusService consensusService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private PocRewardCacheService rewardCacheService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Get the whole network consensus infomation! 查询全网共识总体信息 [3.6.1]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = WholeNetConsensusInfoDTO.class)
    })
    public RpcClientResult getWholeInfo() {
        Result result = Result.getSuccess();

        WholeNetConsensusInfoDTO dto = new WholeNetConsensusInfoDTO();

        List<Agent> allAgentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        long startBlockHeight = NulsContext.getInstance().getBestHeight();
        List<Agent> agentList = new ArrayList<>();

        for (int i = allAgentList.size() - 1; i >= 0; i--) {
            Agent agent = allAgentList.get(i);
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            agentList.add(agent);
        }
        MeetingRound round = PocConsensusContext.getChainManager().getMasterChain().getCurrentRound();
        long totalDeposit = 0;
        int packingAgentCount = 0;
        if (null != round) {
            for (MeetingMember member : round.getMemberList()) {
                totalDeposit += (member.getTotalDeposit().getValue() + member.getOwnDeposit().getValue());
                if (member.getAgent() != null) {
                    packingAgentCount++;
                }
            }
        }

        dto.setAgentCount(agentList.size());
        dto.setTotalDeposit(totalDeposit);
        dto.setConsensusAccountNumber(agentList.size());
        dto.setPackingAgentCount(packingAgentCount);
        result.setData(dto);
        return result.toRpcClientResult();
    }

    @GET
    @Path("/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("获取钱包内某个账户参与共识信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Map.class)
    })
    public RpcClientResult getInfo(@ApiParam(name = "address", value = "钱包账户地", required = true)
                                   @PathParam("address") String address) {

        if (!AddressTool.validAddress(StringUtils.formatStringPara(address))) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Result accountResult = accountService.getAccount(address);
        if (accountResult.isFailed()) {
            return accountResult.toRpcClientResult();
        }
        //Account account = (Account) accountResult.getData();
        Result result = Result.getSuccess();
        AccountConsensusInfoDTO dto = new AccountConsensusInfoDTO();
        List<Agent> allAgentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        long startBlockHeight = NulsContext.getInstance().getBestHeight();
        int agentCount = 0;
        String agentHash = null;
        byte[] addressBytes = AddressTool.getAddress(address);
        for (int i = allAgentList.size() - 1; i >= 0; i--) {
            Agent agent = allAgentList.get(i);
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            if (Arrays.equals(agent.getAgentAddress(), addressBytes)) {
                agentCount = 1;
                agentHash = agent.getTxHash().getDigestHex();
                break;
            }
        }
        List<Deposit> depositList = PocConsensusContext.getChainManager().getMasterChain().getChain().getDepositList();
        Set<NulsDigestData> agentSet = new HashSet<>();
        long totalDeposit = 0;
        for (Deposit deposit : depositList) {
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (!Arrays.equals(deposit.getAddress(), addressBytes)) {
                continue;
            }
            agentSet.add(deposit.getAgentHash());
            totalDeposit += deposit.getDeposit().getValue();
        }


        dto.setAgentCount(agentCount);
        dto.setAgentHash(agentHash);
        dto.setJoinAgentCount(agentSet.size());
        dto.setReward(this.rewardCacheService.getReward(address).getValue());
        dto.setRewardOfDay(rewardCacheService.getRewardToday(address).getValue());
        dto.setTotalDeposit(totalDeposit);
        try {
            dto.setUsableBalance(accountLedgerService.getBalance(addressBytes).getData().getUsable().getValue());
        } catch (Exception e) {
            Log.error(e);
            dto.setUsableBalance(0L);
        }
        result.setData(dto);
        return result.toRpcClientResult();
    }


    @GET
    @Path("/agent/fee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get the fee of create agent! 获取创建共识(代理)节点的手续费", notes = "返回创建的节点成功的交易手续费")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult getCreateAgentFee(
            @BeanParam() GetCreateAgentFeeForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAgentAddress(), "agent address can not be null");
        AssertUtil.canNotEmpty(form.getCommissionRate(), "commission rate can not be null");
        AssertUtil.canNotEmpty(form.getDeposit(), "deposit can not be null");
        AssertUtil.canNotEmpty(form.getPackingAddress(), "packing address can not be null");
        if (StringUtils.isBlank(form.getRewardAddress())) {
            form.setRewardAddress(form.getAgentAddress());
        }
        CreateAgentTransaction tx = new CreateAgentTransaction();
        tx.setTime(TimeService.currentTimeMillis());
        Agent agent = new Agent();
        agent.setAgentAddress(AddressTool.getAddress(form.getAgentAddress()));
        agent.setPackingAddress(AddressTool.getAddress(form.getPackingAddress()));
        if (StringUtils.isBlank(form.getRewardAddress())) {
            agent.setRewardAddress(agent.getAgentAddress());
        } else {
            agent.setRewardAddress(AddressTool.getAddress(form.getRewardAddress()));
        }

        agent.setDeposit(Na.valueOf(form.getDeposit()));
        agent.setCommissionRate(form.getCommissionRate());
        tx.setTxData(agent);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        if(agent.getAgentAddress()[2] == 3){
            Script scriptPubkey = SignatureUtil.createOutputScript(agent.getAgentAddress());
            toList.add(new Coin(scriptPubkey.getProgram(), agent.getDeposit(), -1));
        }else {
            toList.add(new Coin(agent.getAgentAddress(), agent.getDeposit(), -1));
        }
        coinData.setTo(toList);
        tx.setCoinData(coinData);
        CoinDataResult result = accountLedgerService.getCoinData(agent.getAgentAddress(), agent.getDeposit(), tx.size()- P2PHKSignature.SERIALIZE_LENGTH, TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
        tx.getCoinData().setFrom(result.getCoinList());
        if (null != result.getChange()) {
            tx.getCoinData().getTo().add(result.getChange());
        }
        Na fee = TransactionFeeCalculator.getMaxFee(tx.size());
        Result rs = accountLedgerService.getMaxAmountOfOnce(AddressTool.getAddress(form.getAgentAddress()), tx, TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
        Map<String, Long> map = new HashMap<>();
        Long maxAmount = null;
        if (rs.isSuccess()) {
            maxAmount = ((Na) rs.getData()).getValue();
        }
        map.put("fee", fee.getValue());
        map.put("maxAmount", maxAmount);
        rs.setData(map);
        return Result.getSuccess().setData(rs).toRpcClientResult();
    }

    @GET
    @Path("/deposit/fee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get the fee of create agent! 获取加入共识的手续费", notes = "返回加入共识交易手续费")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult getDepositFee(@BeanParam() GetDepositFeeForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress(), "address can not be null");
        AssertUtil.canNotEmpty(form.getAgentHash(), "agent hash can not be null");
        AssertUtil.canNotEmpty(form.getDeposit(), "deposit can not be null");
        DepositTransaction tx = new DepositTransaction();
        Deposit deposit = new Deposit();
        deposit.setAddress(AddressTool.getAddress(form.getAddress()));
        deposit.setAgentHash(NulsDigestData.fromDigestHex(form.getAgentHash()));
        deposit.setDeposit(Na.valueOf(form.getDeposit()));
        tx.setTxData(deposit);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(deposit.getAddress(), deposit.getDeposit(), -1));
        coinData.setTo(toList);
        tx.setCoinData(coinData);
        CoinDataResult result = accountLedgerService.getCoinData(deposit.getAddress(), deposit.getDeposit(), tx.size(), TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
        tx.getCoinData().setFrom(result.getCoinList());
        if (null != result.getChange()) {
            tx.getCoinData().getTo().add(result.getChange());
        }
        Na fee = TransactionFeeCalculator.getMaxFee(tx.size());
        Result rs = accountLedgerService.getMaxAmountOfOnce(AddressTool.getAddress(form.getAddress()), tx, TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
        Map<String, Long> map = new HashMap<>();
        Long maxAmount = null;
        if (rs.isSuccess()) {
            maxAmount = ((Na) rs.getData()).getValue();
        }
        map.put("fee", fee.getValue());
        map.put("maxAmount", maxAmount);
        rs.setData(map);
        return Result.getSuccess().setData(rs).toRpcClientResult();
    }

    @GET
    @Path("/agent/stop/fee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get the fee of stop agent! 获取停止节点的手续费", notes = "返回停止节点交易手续费")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult getStopAgentFee(@ApiParam(name = "address", value = "创建节点的账户地址", required = true)
                                           @QueryParam("address") String address) throws NulsException, IOException {
        AssertUtil.canNotEmpty(address, "address can not be null");
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }

        StopAgentTransaction tx = new StopAgentTransaction();
        StopAgent stopAgent = new StopAgent();
        stopAgent.setAddress(AddressTool.getAddress(address));
        List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        Agent agent = null;
        for (Agent a : agentList) {
            if (a.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(a.getAgentAddress(), stopAgent.getAddress())) {
                agent = a;
                break;
            }
        }
        if (agent == null || agent.getDelHeight() > 0) {
            return Result.getFailed(PocConsensusErrorCode.AGENT_NOT_EXIST).toRpcClientResult();
        }
        NulsDigestData createTxHash = agent.getTxHash();
        stopAgent.setCreateTxHash(createTxHash);
        tx.setTxData(stopAgent);
        CoinData coinData = ConsensusTool.getStopAgentCoinData(agent,TimeService.currentTimeMillis() +  PocConsensusConstant.STOP_AGENT_LOCK_TIME);
        tx.setCoinData(coinData);
        Na fee = TransactionFeeCalculator.getMaxFee(tx.size());
        coinData.getTo().get(0).setNa(coinData.getTo().get(0).getNa().subtract(fee));
        Na resultFee = TransactionFeeCalculator.getMaxFee(tx.size() );
        Result rs = accountLedgerService.getMaxAmountOfOnce(AddressTool.getAddress(address), tx, TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
        Map<String, Long> map = new HashMap<>();
        Long maxAmount = null;
        if (rs.isSuccess()) {
            maxAmount = ((Na) rs.getData()).getValue();
        }
        map.put("fee", resultFee.getValue());
        map.put("maxAmount", maxAmount);
        rs.setData(map);
        return Result.getSuccess().setData(rs).toRpcClientResult();
    }


    @POST
    @Path("/agent")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create an agent for consensus! 创建共识(代理)节点 [3.6.3]", notes = "返回创建的节点成功的交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult createAgent(@ApiParam(name = "form", value = "创建节点表单数据", required = true)
                                               CreateAgentForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAgentAddress(), "agent address can not be null");
        AssertUtil.canNotEmpty(form.getCommissionRate(), "commission rate can not be null");
        AssertUtil.canNotEmpty(form.getDeposit(), "deposit can not be null");
        AssertUtil.canNotEmpty(form.getPackingAddress(), "packing address can not be null");

        if (!AddressTool.validAddress(form.getPackingAddress()) || !AddressTool.validAddress(form.getAgentAddress())) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = accountService.getAccount(form.getAgentAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(form.getPassword(), "password is wrong");
            if (!account.validatePassword(form.getPassword())) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
            }
        }
        CreateAgentTransaction tx = new CreateAgentTransaction();
        tx.setTime(TimeService.currentTimeMillis());
        Agent agent = new Agent();
        agent.setAgentAddress(AddressTool.getAddress(form.getAgentAddress()));
        agent.setPackingAddress(AddressTool.getAddress(form.getPackingAddress()));
        if (StringUtils.isBlank(form.getRewardAddress())) {
            agent.setRewardAddress(agent.getAgentAddress());
        } else {
            agent.setRewardAddress(AddressTool.getAddress(form.getRewardAddress()));
        }

        agent.setDeposit(Na.valueOf(form.getDeposit()));
        agent.setCommissionRate(form.getCommissionRate());
        tx.setTxData(agent);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(agent.getAgentAddress(), agent.getDeposit(), PocConsensusConstant.CONSENSUS_LOCK_TIME));
        coinData.setTo(toList);
        tx.setCoinData(coinData);
        CoinDataResult result = accountLedgerService.getCoinData(agent.getAgentAddress(), agent.getDeposit(), tx.size(), TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
        RpcClientResult result1 = this.txProcessing(tx, result, account, form.getPassword());
        if (!result1.isSuccess()) {
            return result1;
        }
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("value", tx.getHash().getDigestHex());
        return Result.getSuccess().setData(valueMap).toRpcClientResult();
    }


    @POST
    @Path("/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "deposit nuls to a bank! 申请参与共识 ", notes = "返回申请成功交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult depositToAgent(@ApiParam(name = "form", value = "申请参与共识表单数据", required = true)
                                                  DepositForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getAgentHash());
        if (!NulsDigestData.validHash(form.getAgentHash())) {
            return Result.getFailed(PocConsensusErrorCode.AGENT_NOT_EXIST).toRpcClientResult();
        }
        AssertUtil.canNotEmpty(form.getDeposit());
        if (!AddressTool.validAddress(form.getAddress())) {
            throw new NulsRuntimeException(KernelErrorCode.PARAMETER_ERROR);
        }
        Account account = accountService.getAccount(form.getAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(form.getPassword(), "password is wrong");
            if (!account.validatePassword(form.getPassword())) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
            }
        }

        DepositTransaction tx = new DepositTransaction();
        Deposit deposit = new Deposit();
        deposit.setAddress(AddressTool.getAddress(form.getAddress()));
        deposit.setAgentHash(NulsDigestData.fromDigestHex(form.getAgentHash()));
        deposit.setDeposit(Na.valueOf(form.getDeposit()));
        tx.setTxData(deposit);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(deposit.getAddress(), deposit.getDeposit(), PocConsensusConstant.CONSENSUS_LOCK_TIME));
        coinData.setTo(toList);
        tx.setCoinData(coinData);
        CoinDataResult result = accountLedgerService.getCoinData(deposit.getAddress(), deposit.getDeposit(), tx.size(), TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);

        RpcClientResult result1 = this.txProcessing(tx, result, account, form.getPassword());
        if (!result1.isSuccess()) {
            return result1;
        }
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("value", tx.getHash().getDigestHex());
        return Result.getSuccess().setData(valueMap).toRpcClientResult();
    }

    public RpcClientResult txProcessing(Transaction tx, CoinDataResult result, Account account, String password) {
        if (null != result) {
            if (result.isEnough()) {
                tx.getCoinData().setFrom(result.getCoinList());
                if (null != result.getChange()) {
                    tx.getCoinData().getTo().add(result.getChange());
                }
            } else {
                return Result.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE).toRpcClientResult();
            }
        }
        try {
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            //生成签名
            List<ECKey> signEckeys = new ArrayList<>();
            ECKey eckey = account.getEcKey(password);
            signEckeys.add(eckey);
            SignatureUtil.createTransactionSignture(tx,null,signEckeys);

        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION).toRpcClientResult();
        }
        Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
        if (saveResult.isFailed()) {
            if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
                //重新算一次交易(不超出最大交易数据大小下)的最大金额
                Result rs = accountLedgerService.getMaxAmountOfOnce(account.getAddress().getAddressBytes(), tx,
                        TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
                if(rs.isSuccess()){
                    Na maxAmount = (Na)rs.getData();
                    rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
                    rs.setMsg(rs.getMsg() + maxAmount.toDouble());
                }
                return rs.toRpcClientResult();

            }
            return saveResult.toRpcClientResult();
        }
        transactionService.newTx(tx);
        Result sendResult = this.transactionService.broadcastTx(tx);
        if (sendResult.isFailed()) {
            accountLedgerService.deleteTransaction(tx);
            return sendResult.toRpcClientResult();
        }
        return Result.getSuccess().toRpcClientResult();
    }


    @POST
    @Path("/agent/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "注销共识节点 [3.6.5]", notes = "返回注销成功交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult stopAgent(@ApiParam(name = "form", value = "注销共识节点表单数据", required = true)
                                             StopAgentForm form) throws NulsException, IOException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        if (!AddressTool.validAddress(form.getAddress())) {
            throw new NulsRuntimeException(KernelErrorCode.PARAMETER_ERROR);
        }
        Account account = accountService.getAccount(form.getAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(form.getPassword(), "password is wrong");
            if (!account.validatePassword(form.getPassword())) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
            }
        }
        StopAgentTransaction tx = new StopAgentTransaction();
        StopAgent stopAgent = new StopAgent();
        stopAgent.setAddress(AddressTool.getAddress(form.getAddress()));
        List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        Agent agent = null;
        for (Agent a : agentList) {
            if (a.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(a.getAgentAddress(), account.getAddress().getAddressBytes())) {
                agent = a;
                break;
            }
        }
        if (agent == null || agent.getDelHeight() > 0) {
            return Result.getFailed(PocConsensusErrorCode.AGENT_NOT_EXIST).toRpcClientResult();
        }

        stopAgent.setCreateTxHash(agent.getTxHash());
        tx.setTxData(stopAgent);

        CoinData coinData = ConsensusTool.getStopAgentCoinData(agent, TimeService.currentTimeMillis() + PocConsensusConstant.STOP_AGENT_LOCK_TIME);

        tx.setCoinData(coinData);
        Na fee = TransactionFeeCalculator.getMaxFee(tx.size());
        coinData.getTo().get(0).setNa(coinData.getTo().get(0).getNa().subtract(fee));
        RpcClientResult result1 = this.txProcessing(tx, null, account, form.getPassword());
        if (!result1.isSuccess()) {
            return result1;
        }
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("value", tx.getHash().getDigestHex());
        return Result.getSuccess().setData(valueMap).toRpcClientResult();
    }


    @GET
    @Path("/agent/list")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询共识节点列表信息 [3.6.6]", notes = "result.data.page.list: List<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public RpcClientResult getAgentList(@ApiParam(name = "pageNumber", value = "页码")
                                        @QueryParam("pageNumber") Integer pageNumber,
                                        @ApiParam(name = "pageSize", value = "每页条数")
                                        @QueryParam("pageSize") Integer pageSize,
                                        @ApiParam(name = "keyword", value = "搜索关键字")
                                        @QueryParam("keyword") String keyword,
                                        @ApiParam(name = "sortType", value = "排序字段名")
                                        @QueryParam("sortType") String sortType) throws UnsupportedEncodingException {
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result result = Result.getSuccess();
        List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        agentList = new ArrayList<>(agentList);
        long startBlockHeight = NulsContext.getInstance().getBestHeight();
        for (int i = agentList.size() - 1; i >= 0; i--) {
            Agent agent = agentList.get(i);
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                agentList.remove(i);
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                agentList.remove(i);
            } else if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.toUpperCase();
                String agentAddress = AddressTool.getStringAddressByBytes(agent.getAgentAddress()).toUpperCase();
                String packingAddress = AddressTool.getStringAddressByBytes(agent.getPackingAddress()).toUpperCase();
                String agentId = PoConvertUtil.getAgentId(agent.getTxHash()).toUpperCase();
                String alias = accountService.getAlias(agent.getAgentAddress()).getData();
                boolean b = agentId.indexOf(keyword) >= 0;
                b = b || agentAddress.equals(keyword) || packingAddress.equals(keyword);
                if (StringUtils.isNotBlank(alias)) {
                    b = b || alias.toUpperCase().indexOf(keyword) >= 0;
                }
                if (!b) {
                    agentList.remove(i);
                }
            }
        }
        int start = pageNumber * pageSize - pageSize;
        Page<AgentDTO> page = new Page<>(pageNumber, pageSize, agentList.size());
        if (start >= page.getTotal()) {
            result.setData(page);
            return result.toRpcClientResult();
        }
        fillAgentList(agentList, null);
        int type = AgentComparator.COMPREHENSIVE;
        if ("deposit".equals(sortType)) {
            type = AgentComparator.DEPOSIT;
        } else if ("commissionRate".equals(sortType)) {
            type = AgentComparator.COMMISSION_RATE;
        } else if ("creditVal".equals(sortType)) {
            type = AgentComparator.CREDIT_VALUE;
        } else if ("totalDeposit".equals(sortType)) {
            type = AgentComparator.DEPOSITABLE;
        } else if ("comprehensive".equals(sortType)) {
            type = AgentComparator.COMPREHENSIVE;
        }
        Collections.sort(agentList, AgentComparator.getInstance(type));
        List<AgentDTO> resultList = new ArrayList<>();
        for (int i = start; i < agentList.size() && i < (start + pageSize); i++) {
            Agent agent = agentList.get(i);
            resultList.add(new AgentDTO(agent, accountService.getAlias(agent.getAgentAddress()).getData()));
        }
        page.setList(resultList);
        result.setData(page);
        return result.toRpcClientResult();
    }

    private void fillAgentList(List<Agent> agentList, List<Deposit> depositList) {
        MeetingRound round = PocConsensusContext.getChainManager().getMasterChain().getCurrentRound();
        for (Agent agent : agentList) {
            fillAgent(agent, round, depositList);
        }
    }

    private void fillAgent(Agent agent, MeetingRound round, List<Deposit> depositList) {
        if (null == depositList || depositList.isEmpty()) {
            depositList = PocConsensusContext.getChainManager().getMasterChain().getChain().getDepositList();
        }
        Set<String> memberSet = new HashSet<>();
        Na total = Na.ZERO;
        for (int i = 0; i < depositList.size(); i++) {
            Deposit deposit = depositList.get(i);
            if (!agent.getTxHash().equals(deposit.getAgentHash())) {
                continue;
            }
            if (deposit.getDelHeight() >= 0) {
                continue;
            }
            total = total.add(deposit.getDeposit());
            memberSet.add(AddressTool.getStringAddressByBytes(deposit.getAddress()));
        }
        agent.setMemberCount(memberSet.size());
        agent.setTotalDeposit(total.getValue());

        if (round == null) {
            return;
        }
        MeetingMember member = round.getMember(agent.getPackingAddress());
        if (null == member) {
            agent.setStatus(0);
            return;
        }
        agent.setStatus(1);
        agent.setCreditVal(member.getCreditVal());

    }

    @GET
    @Path("/agent/{agentHash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询共识节点详细信息 [3.6.7]", notes = "result.data: Map<String, Object>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Map.class)
    })
    public RpcClientResult getAgent(@ApiParam(name = "agentHash", value = "节点标识", required = true)
                                    @PathParam("agentHash") String agentHash) throws NulsException {

        if (!NulsDigestData.validHash(agentHash)) {
            return Result.getFailed(PocConsensusErrorCode.AGENT_NOT_EXIST).toRpcClientResult();
        }
        Result result = Result.getSuccess();
        List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        NulsDigestData agentHashData = NulsDigestData.fromDigestHex(agentHash);
        for (Agent agent : agentList) {
            if (agent.getTxHash().equals(agentHashData)) {
                MeetingRound round = PocConsensusContext.getChainManager().getMasterChain().getCurrentRound();
                this.fillAgent(agent, round, null);
                String alias = accountService.getAlias(agent.getAgentAddress()).getData();
                AgentDTO dto = new AgentDTO(agent, alias);
                result.setData(dto);
                return result.toRpcClientResult();
            }
        }
        return Result.getFailed(PocConsensusErrorCode.AGENT_NOT_EXIST).toRpcClientResult();
    }

    @GET
    @Path("/agent/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据地址查询其委托的节点信息列表 [3.6.12]",
            notes = "result.data.page.list: List<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public RpcClientResult getAgentListByDepositAddress(@ApiParam(name = "pageNumber", value = "页码")
                                                        @QueryParam("pageNumber") Integer pageNumber,
                                                        @ApiParam(name = "pageSize", value = "每页条数")
                                                        @QueryParam("pageSize") Integer pageSize,
                                                        @ApiParam(name = "address", value = "账户地址", required = true)
                                                        @PathParam("address") String address) {
        AssertUtil.canNotEmpty(address);
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result result = Result.getSuccess();
        List<Deposit> allList = PocConsensusContext.getChainManager().getMasterChain().getChain().getDepositList();
        long startBlockHeight = NulsContext.getInstance().getBestHeight();
        byte[] addressBytes = AddressTool.getAddress(address);
        Set<NulsDigestData> agentHashSet = new HashSet<>();
        for (Deposit deposit : allList) {
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (!Arrays.equals(deposit.getAddress(), addressBytes)) {
                continue;
            }
            agentHashSet.add(deposit.getAgentHash());
        }
        List<Agent> allAgentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        List<Agent> agentList = new ArrayList<>();
        Agent ownAgent = null;
        for (int i = allAgentList.size() - 1; i >= 0; i--) {
            Agent agent = allAgentList.get(i);
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            if (Arrays.equals(agent.getAgentAddress(), addressBytes)) {
                ownAgent = agent;
                continue;
            }
            if (!agentHashSet.contains(agent.getTxHash())) {
                continue;
            }
            agentList.add(agent);
        }
        if (null != ownAgent) {
            agentList.add(0, ownAgent);
        }
        int start = pageNumber * pageSize - pageSize;
        Page<AgentDTO> page = new Page<>(pageNumber, pageSize, agentList.size());
        if (start >= agentList.size()) {
            result.setData(page);
            return result.toRpcClientResult();
        }
        fillAgentList(agentList, allList);
        List<AgentDTO> resultList = new ArrayList<>();
        for (int i = start; i < agentList.size() && i < (start + pageSize); i++) {
            Agent agent = agentList.get(i);
            resultList.add(new AgentDTO(agent, accountService.getAlias(agent.getAgentAddress()).getData()));
        }
        page.setList(resultList);
        result.setData(page);
        return result.toRpcClientResult();
    }

    @GET
    @Path("/deposit/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询指定地址参与的所有委托信息列表 [3.6.8]",
            notes = "result.data.page.list: List<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public RpcClientResult getDepositListByAddress(@ApiParam(name = "address", value = "账户地址", required = true)
                                                   @PathParam("address") String address,
                                                   @ApiParam(name = "pageNumber", value = "页码")
                                                   @QueryParam("pageNumber") Integer pageNumber,
                                                   @ApiParam(name = "pageSize", value = "每页条数")
                                                   @QueryParam("pageSize") Integer pageSize,
                                                   @ApiParam(name = "agentHash", value = "指定代理节点标识(不传查所有)")
                                                   @QueryParam("agentHash") String agentHash) {
        AssertUtil.canNotEmpty(address);
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (null != agentHash && !NulsDigestData.validHash(agentHash)) {
            return Result.getFailed(PocConsensusErrorCode.AGENT_NOT_EXIST).toRpcClientResult();
        }
        Result result = Result.getSuccess();
        List<Deposit> allList = PocConsensusContext.getChainManager().getMasterChain().getChain().getDepositList();
        List<Deposit> depositList = new ArrayList<>();
        long startBlockHeight = NulsContext.getInstance().getBestHeight();
        byte[] addressBytes = AddressTool.getAddress(address);
        for (Deposit deposit : allList) {
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (!Arrays.equals(deposit.getAddress(), addressBytes)) {
                continue;
            }
            if (agentHash != null && !deposit.getAgentHash().getDigestHex().equals(agentHash)) {
                continue;
            }
            depositList.add(deposit);
        }
        int start = pageNumber * pageSize - pageSize;
        Page<DepositDTO> page = new Page<>(pageNumber, pageSize, depositList.size());
        if (start >= depositList.size()) {
            result.setData(page);
            return result.toRpcClientResult();
        }
        Map<NulsDigestData, Agent> map = new HashMap<>();
        for (MeetingMember member : PocConsensusContext.getChainManager().getMasterChain().getCurrentRound().getMemberList()) {
            if (null != member.getAgent()) {
                map.put(member.getAgent().getTxHash(), member.getAgent());
            }
        }
        List<DepositDTO> resultList = new ArrayList<>();

        for (int i = start; i < depositList.size() && i < (start + pageSize); i++) {
            Deposit deposit = depositList.get(i);
            List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
            Agent agent = null;
            for (Agent a : agentList) {
                if (a.getTxHash().equals(deposit.getAgentHash())) {
                    agent = a;
                    break;
                }
            }
            deposit.setStatus(agent == null ? 0 : agent.getStatus());
            resultList.add(new DepositDTO(deposit, agent));
        }
        page.setList(resultList);
        result.setData(page);
        return result.toRpcClientResult();
    }

    @GET
    @Path("/deposit/agent/{agentHash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询共识节点受托列表信息 [3.6.9]",
            notes = "result.data.page.list: List<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public RpcClientResult queryDepositListByAgentAddress(@ApiParam(name = "agentHash", value = "指定代理节点标识", required = true)
                                                          @PathParam("agentHash") String agentHash,
                                                          @ApiParam(name = "pageNumber", value = "页码")
                                                          @QueryParam("pageNumber") Integer pageNumber,
                                                          @ApiParam(name = "pageSize", value = "每页条数")
                                                          @QueryParam("pageSize") Integer pageSize) throws NulsException {
        AssertUtil.canNotEmpty(agentHash);
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        Result result = Result.getSuccess();
        List<Deposit> allList = PocConsensusContext.getChainManager().getMasterChain().getChain().getDepositList();
        List<Deposit> depositList = new ArrayList<>();
        long startBlockHeight = NulsContext.getInstance().getBestHeight();
        NulsDigestData agentDigestData = NulsDigestData.fromDigestHex(agentHash);
        for (Deposit deposit : allList) {
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (!deposit.getAgentHash().equals(agentDigestData)) {
                continue;
            }
            depositList.add(deposit);
        }
        int start = pageNumber * pageSize - pageSize;
        Page<DepositDTO> page = new Page<>(pageNumber, pageSize, depositList.size());
        if (start >= depositList.size()) {
            result.setData(page);
            return result.toRpcClientResult();
        }
        Map<NulsDigestData, Integer> map = new HashMap<>();
        for (MeetingMember member : PocConsensusContext.getChainManager().getMasterChain().getCurrentRound().getMemberList()) {
            if (null != member.getAgent()) {
                map.put(member.getAgent().getTxHash(), 1);
            }
        }

        List<DepositDTO> resultList = new ArrayList<>();
        for (int i = start; i < depositList.size() && i < (start + pageSize); i++) {
            Deposit deposit = depositList.get(i);
            deposit.setStatus(map.get(deposit.getAgentHash()) == null ? 0 : 1);
            resultList.add(new DepositDTO(deposit));
        }
        page.setList(resultList);
        result.setData(page);
        return result.toRpcClientResult();
    }

    @POST
    @Path("/withdraw")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "退出共识 [3.6.11]",
            notes = "返回退出成功的交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult withdraw(@ApiParam(name = "form", value = "退出共识表单数据", required = true)
                                            WithdrawForm form) throws NulsException, IOException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getTxHash());
        if (!NulsDigestData.validHash(form.getTxHash())) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        AssertUtil.canNotEmpty(form.getAddress());
        if (!AddressTool.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Account account = accountService.getAccount(form.getAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(form.getPassword(), "password is wrong");
            if (!account.validatePassword(form.getPassword())) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
            }
        }
        CancelDepositTransaction tx = new CancelDepositTransaction();
        CancelDeposit cancelDeposit = new CancelDeposit();
        NulsDigestData hash = NulsDigestData.fromDigestHex(form.getTxHash());
        DepositTransaction depositTransaction = null;
        try {
            depositTransaction = (DepositTransaction) ledgerService.getTx(hash);
        } catch (Exception e) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        if (null == depositTransaction) {
            return Result.getFailed(TransactionErrorCode.TX_NOT_EXIST).toRpcClientResult();
        }
        cancelDeposit.setAddress(AddressTool.getAddress(form.getAddress()));
        cancelDeposit.setJoinTxHash(hash);
        tx.setTxData(cancelDeposit);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(cancelDeposit.getAddress(), depositTransaction.getTxData().getDeposit(), 0));
        coinData.setTo(toList);
        List<Coin> fromList = new ArrayList<>();
        for (int index = 0; index < depositTransaction.getCoinData().getTo().size(); index++) {
            Coin coin = depositTransaction.getCoinData().getTo().get(index);
            if (coin.getLockTime() == -1L && coin.getNa().equals(depositTransaction.getTxData().getDeposit())) {
                coin.setOwner(ArraysTool.concatenate(hash.serialize(), new VarInt(index).encode()));
                fromList.add(coin);
                break;
            }
        }
        if (fromList.isEmpty()) {
            return Result.getFailed(KernelErrorCode.DATA_ERROR).toRpcClientResult();
        }
        coinData.setFrom(fromList);
        tx.setCoinData(coinData);
        Na fee = TransactionFeeCalculator.getMaxFee(tx.size() );
        coinData.getTo().get(0).setNa(coinData.getTo().get(0).getNa().subtract(fee));
        RpcClientResult result1 = this.txProcessing(tx, null, account, form.getPassword());
        if (!result1.isSuccess()) {
            return result1;
        }
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("value", tx.getHash().getDigestHex());
        return Result.getSuccess().setData(valueMap).toRpcClientResult();
    }


    @GET
    @Path("/withdraw/fee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get the fee of cancel deposit! 获取撤销委托的手续费", notes = "返回撤销委托交易手续费")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult getWithdrawFee(@ApiParam(name = "address", value = "委托账户地址", required = true)
                                          @QueryParam("address") String address,
                                          @ApiParam(name = "depositTxHash", value = "委托交易摘要", required = true)
                                          @QueryParam("depositTxHash") String depositTxHash) throws NulsException, IOException {
        AssertUtil.canNotEmpty(depositTxHash);
        if (!NulsDigestData.validHash(depositTxHash)) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR).toRpcClientResult();
        }
        AssertUtil.canNotEmpty(address);
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        CancelDepositTransaction tx = new CancelDepositTransaction();
        CancelDeposit cancelDeposit = new CancelDeposit();
        NulsDigestData hash = NulsDigestData.fromDigestHex(depositTxHash);
        DepositTransaction depositTransaction = (DepositTransaction) ledgerService.getTx(hash);
        if (null == depositTransaction) {
            return Result.getFailed(TransactionErrorCode.TX_NOT_EXIST).toRpcClientResult();
        }
        cancelDeposit.setAddress(account.getAddress().getAddressBytes());
        cancelDeposit.setJoinTxHash(hash);
        tx.setTxData(cancelDeposit);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(cancelDeposit.getAddress(), depositTransaction.getTxData().getDeposit(), 0));
        coinData.setTo(toList);
        List<Coin> fromList = new ArrayList<>();
        for (int index = 0; index < depositTransaction.getCoinData().getTo().size(); index++) {
            Coin coin = depositTransaction.getCoinData().getTo().get(index);
            if (coin.getLockTime() == -1L && coin.getNa().equals(depositTransaction.getTxData().getDeposit())) {
                coin.setOwner(ArraysTool.concatenate(hash.serialize(), new VarInt(index).encode()));
                fromList.add(coin);
                break;
            }
        }
        if (fromList.isEmpty()) {
            return Result.getFailed(KernelErrorCode.DATA_ERROR).toRpcClientResult();
        }
        coinData.setFrom(fromList);
        tx.setCoinData(coinData);
        Na fee = TransactionFeeCalculator.getMaxFee(tx.size() );
        coinData.getTo().get(0).setNa(coinData.getTo().get(0).getNa().subtract(fee));
        Na resultFee = TransactionFeeCalculator.getMaxFee(tx.size() );
        Result rs = accountLedgerService.getMaxAmountOfOnce(account.getAddress().getAddressBytes(), tx, TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
        Map<String, Long> map = new HashMap<>();
        Long maxAmount = null;
        if (rs.isSuccess()) {
            maxAmount = ((Na) rs.getData()).getValue();
        }
        map.put("fee", resultFee.getValue());
        map.put("maxAmount", maxAmount);
        rs.setData(map);
        return Result.getSuccess().setData(rs).toRpcClientResult();
    }


    @GET
    @Path("/redPunish/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据地址查询该账户是否被红牌惩罚过")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Boolean.class)
    })
    public RpcClientResult getRedPunishByAddress(@ApiParam(name = "address", value = "账户地址", required = true)
                                                 @PathParam("address") String address) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        List<PunishLogPo> list = PocConsensusContext.getChainManager().getMasterChain().getChain().getRedPunishList();
        boolean rs = false;
        for (PunishLogPo po : list) {
            if (Arrays.equals(AddressTool.getAddress(address), po.getAddress())) {
                rs = true;
                break;
            }
        }
        return Result.getSuccess().setData(rs).toRpcClientResult();
    }

    @POST
    @Path("/mutilAgent")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create an agent for consensus! 创建共识(代理)节点 [3.6.3]", notes = "返回创建的节点成功的交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult createMutilAgent(@ApiParam(name = "form", value = "多签地址创建节点表单数据", required = true)
                                               CreateMutilAgentForm form) throws NulsException,IOException{
        if(NulsContext.MAIN_NET_VERSION  <=1){
            return Result.getFailed(KernelErrorCode.VERSION_TOO_LOW).toRpcClientResult();
        }
        if (form == null) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getSignAddress())  || !AddressTool.validAddress(form.getAgentAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Account account = accountService.getAccount(form.getSignAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(form.getPassword(), "password is wrong");
            if (!account.validatePassword(form.getPassword())) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
            }
        }
        //如果为交易发起人，则需要填写组装交易需要的信息
        CreateAgentTransaction tx = new CreateAgentTransaction();
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        List<Script> scripts = new ArrayList<>();
        if(form.getTxdata() == null || form.getTxdata().trim().length() == 0){
            if (!AddressTool.validAddress(form.getPackingAddress())) {
                throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
            }
            if (form.getM() <= 0) {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            if (form.getPubkeys() == null || form.getPubkeys().size() == 0 || form.getPubkeys().size() < form.getM()) {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            Script redeemScript = ScriptBuilder.createNulsRedeemScript(form.getM(),form.getPubkeys());
            tx.setTime(TimeService.currentTimeMillis());
            Agent agent = new Agent();
            agent.setAgentAddress(AddressTool.getAddress(form.getAgentAddress()));
            agent.setPackingAddress(AddressTool.getAddress(form.getPackingAddress()));
            if (StringUtils.isBlank(form.getRewardAddress())) {
                agent.setRewardAddress(agent.getAgentAddress());
            } else {
                agent.setRewardAddress(AddressTool.getAddress(form.getRewardAddress()));
            }
            agent.setDeposit(Na.valueOf(form.getDeposit()));
            agent.setCommissionRate(form.getCommissionRate());
            tx.setTxData(agent);
            CoinData coinData = new CoinData();
            List<Coin> toList = new ArrayList<>();
            if(agent.getAgentAddress()[2] == NulsContext.P2SH_ADDRESS_TYPE){
                Script scriptPubkey = SignatureUtil.createOutputScript(agent.getAgentAddress());
                toList.add(new Coin(scriptPubkey.getProgram(), agent.getDeposit(), PocConsensusConstant.CONSENSUS_LOCK_TIME));
            }else{
                toList.add(new Coin(agent.getAgentAddress(), agent.getDeposit(), PocConsensusConstant.CONSENSUS_LOCK_TIME));
            }
            coinData.setTo(toList);
            tx.setCoinData(coinData);
            //交易签名的长度为m*单个签名长度+赎回脚本长度
            int scriptSignLenth = redeemScript.getProgram().length + form.getM()*72;
            CoinDataResult result = accountLedgerService.getMutilCoinData(agent.getAgentAddress(), agent.getDeposit(), tx.size()+scriptSignLenth, TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
            if (null != result) {
                if (result.isEnough()) {
                    tx.getCoinData().setFrom(result.getCoinList());
                    if (null != result.getChange()) {
                        tx.getCoinData().getTo().add(result.getChange());
                    }
                } else {
                    return Result.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE).toRpcClientResult();
                }
            }
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            //将赎回脚本先存储在签名脚本中
            scripts.add(redeemScript);
            transactionSignature.setScripts(scripts);
        }else{
            byte[] txByte = Hex.decode(form.getTxdata());
            tx.parse(new NulsByteBuffer(txByte));
            transactionSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
            p2PHKSignatures = transactionSignature.getP2PHKSignatures();
            scripts = transactionSignature.getScripts();
        }
        //使用签名账户对交易进行签名
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        ECKey eckey = account.getEcKey(form.getPassword());
        p2PHKSignature.setPublicKey(eckey.getPubKey());
        //用当前交易的hash和账户的私钥账户
        p2PHKSignature.setSignData(accountService.signDigest(tx.getHash().getDigestBytes(),eckey));
        p2PHKSignatures.add(p2PHKSignature);
        Result result = txMutilProcessing(tx,p2PHKSignatures,scripts,transactionSignature,AddressTool.getAddress(form.getAgentAddress()));
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("txData", (String) result.getData());
        return Result.getSuccess().setData(valueMap).toRpcClientResult();
    }

    @POST
    @Path("/mutilDeposit")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "deposit nuls to a bank! 申请参与共识 ", notes = "返回申请成功交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult mutilDepositToAgent(@ApiParam(name = "form", value = "申请参与共识表单数据", required = true)
                                                  MutilDepositForm form) throws NulsException,IOException {
        if (NulsContext.MAIN_NET_VERSION <= 1) {
            return Result.getFailed(KernelErrorCode.VERSION_TOO_LOW).toRpcClientResult();
        }
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getSignAddress());
        if (form == null) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        if (!AddressTool.validAddress(form.getSignAddress()) || !AddressTool.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Account account = accountService.getAccount(form.getSignAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(form.getPassword(), "password is wrong");
            if (!account.validatePassword(form.getPassword())) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
            }
        }
        //如果为交易发起人，则需要填写组装交易需要的信息
        DepositTransaction tx = new DepositTransaction();
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        List<Script> scripts = new ArrayList<>();
        if (form.getTxdata() == null || form.getTxdata().trim().length() == 0) {
            AssertUtil.canNotEmpty(form.getAgentHash());
            if (!NulsDigestData.validHash(form.getAgentHash())) {
                return Result.getFailed(PocConsensusErrorCode.AGENT_NOT_EXIST).toRpcClientResult();
            }
            AssertUtil.canNotEmpty(form.getDeposit());
            tx = new DepositTransaction();
            if (form.getM() <= 0) {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            if (form.getPubkeys() == null || form.getPubkeys().size() == 0 || form.getPubkeys().size() < form.getM()) {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            Script redeemScript = ScriptBuilder.createNulsRedeemScript(form.getM(), form.getPubkeys());
            Deposit deposit = new Deposit();
            deposit.setAddress(AddressTool.getAddress(form.getAddress()));
            deposit.setAgentHash(NulsDigestData.fromDigestHex(form.getAgentHash()));
            deposit.setDeposit(Na.valueOf(form.getDeposit()));
            tx.setTxData(deposit);
            CoinData coinData = new CoinData();
            List<Coin> toList = new ArrayList<>();
            //AddressTool.getAddress(addr)
            if (deposit.getAddress()[2] == NulsContext.P2SH_ADDRESS_TYPE) {
                Script scriptPubkey = SignatureUtil.createOutputScript(deposit.getAddress());
                toList.add(new Coin(scriptPubkey.getProgram(), deposit.getDeposit(), PocConsensusConstant.CONSENSUS_LOCK_TIME));
            } else {
                toList.add(new Coin(deposit.getAddress(), deposit.getDeposit(), PocConsensusConstant.CONSENSUS_LOCK_TIME));
            }
            coinData.setTo(toList);
            tx.setCoinData(coinData);
            //交易签名的长度为m*单个签名长度+赎回脚本长度
            int scriptSignLenth = redeemScript.getProgram().length + form.getM() * 72;
            CoinDataResult result = accountLedgerService.getMutilCoinData(deposit.getAddress(), deposit.getDeposit(), tx.size() + scriptSignLenth, TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
            if (null != result) {
                if (result.isEnough()) {
                    tx.getCoinData().setFrom(result.getCoinList());
                    if (null != result.getChange()) {
                        tx.getCoinData().getTo().add(result.getChange());
                    }
                } else {
                    return Result.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE).toRpcClientResult();
                }
            }
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            //将赎回脚本先存储在签名脚本中
            scripts.add(redeemScript);
            transactionSignature.setScripts(scripts);
        }else {
            byte[] txByte = Hex.decode(form.getTxdata());
            tx.parse(new NulsByteBuffer(txByte));
            transactionSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
            p2PHKSignatures = transactionSignature.getP2PHKSignatures();
            scripts = transactionSignature.getScripts();
        }
        //使用签名账户对交易进行签名
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        ECKey eckey = account.getEcKey(form.getPassword());
        p2PHKSignature.setPublicKey(eckey.getPubKey());
        //用当前交易的hash和账户的私钥账户
        p2PHKSignature.setSignData(accountService.signDigest(tx.getHash().getDigestBytes(), eckey));
        p2PHKSignatures.add(p2PHKSignature);
        Result resultData = txMutilProcessing(tx, p2PHKSignatures, scripts, transactionSignature, AddressTool.getAddress(form.getAddress()));
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("txData", (String) resultData.getData());
        return Result.getSuccess().setData(valueMap).toRpcClientResult();

    }

    @POST
    @Path("/agent/stopMutil")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "注销共识节点 [3.6.5]", notes = "返回注销成功交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public RpcClientResult stopMutilAgent(@ApiParam(name = "form", value = "注销共识节点表单数据", required = true)
                                               StopAgentWithMSForm form) throws NulsException, IOException {

        if(NulsContext.MAIN_NET_VERSION  <=1){
            return Result.getFailed(KernelErrorCode.VERSION_TOO_LOW).toRpcClientResult();
        }

        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAgentAddress());
        if (!AddressTool.validAddress(form.getAgentAddress())) {
            throw new NulsRuntimeException(KernelErrorCode.PARAMETER_ERROR);
        }
        Account account = accountService.getAccount(form.getSignAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(form.getPassword(), "password is wrong");
            if (!account.validatePassword(form.getPassword())) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
            }
        }

        List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        Agent agent = null;
        for (Agent p : agentList) {
            if (p.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(p.getAgentAddress(), form.getAgentAddress().getBytes())) {
                agent = p;
                break;
            }
        }

        if (agent == null || agent.getDelHeight() > 0) {
            return Result.getFailed(PocConsensusErrorCode.AGENT_NOT_EXIST).toRpcClientResult();
        }

        // 发起者，创建
        if(form.getTxdata() == null || form.getTxdata().trim().length() == 0) {
            if (form.getM() <= 0) {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }
            if (form.getPubkeys() == null || form.getPubkeys().size() == 0 || form.getPubkeys().size() < form.getM()) {
                return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR).toRpcClientResult();
            }

            // Create unlock script
            Script redeemScript = ScriptBuilder.createNulsRedeemScript(form.getM(),form.getPubkeys());

            StopAgent stopAgent = new StopAgent();
            stopAgent.setAddress(agent.getAgentAddress());
            stopAgent.setCreateTxHash(agent.getTxHash());

            StopAgentTransaction tx = new StopAgentTransaction();
            tx.setTime(TimeService.currentTimeMillis());
            tx.setTxData(stopAgent);

            CoinData coinData = ConsensusTool.getStopAgentCoinData(agent, TimeService.currentTimeMillis() + PocConsensusConstant.STOP_AGENT_LOCK_TIME);
            tx.setCoinData(coinData);
            Na fee = TransactionFeeCalculator.getMaxFee(tx.size());
            coinData.getTo().get(0).setNa(coinData.getTo().get(0).getNa().subtract(fee));
        }

        StopAgentTransaction tx = new StopAgentTransaction();
        StopAgent stopAgent = new StopAgent();
        stopAgent.setAddress(AddressTool.getAddress(form.getSignAddress()));

        stopAgent.setCreateTxHash(agent.getTxHash());
        tx.setTxData(stopAgent);

        CoinData coinData = ConsensusTool.getStopAgentCoinData(agent, TimeService.currentTimeMillis() + PocConsensusConstant.STOP_AGENT_LOCK_TIME);

        tx.setCoinData(coinData);
        Na fee = TransactionFeeCalculator.getMaxFee(tx.size());
        coinData.getTo().get(0).setNa(coinData.getTo().get(0).getNa().subtract(fee));
        RpcClientResult result1 = this.txProcessing(tx, null, account, form.getPassword());
        if (!result1.isSuccess()) {
            return result1;
        }
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("value", tx.getHash().getDigestHex());
        return Result.getSuccess().setData(valueMap).toRpcClientResult();
    }


    public Result txMutilProcessing(Transaction tx, List<P2PHKSignature> p2PHKSignatures,List<Script> scripts,TransactionSignature transactionSignature,byte[] fromAddr) throws NulsException,IOException{
        //当已签名数等于M则自动广播该交易
        if(p2PHKSignatures.size() == SignatureUtil.getM(scripts.get(0))){
            //将交易中的签名数据P2PHKSignatures按规则排序
            Collections.sort(p2PHKSignatures,P2PHKSignature.PUBKEY_COMPARATOR);
            //将排序后的P2PHKSignatures的签名数据取出和赎回脚本结合生成解锁脚本
            List<byte[]> signatures= new ArrayList<>();
            for (P2PHKSignature p2PHKSignatureTemp:p2PHKSignatures) {
                signatures.add(p2PHKSignatureTemp.getSignData().getSignBytes());
            }
            transactionSignature.setP2PHKSignatures(null);
            Script scriptSign = ScriptBuilder.createNulsP2SHMultiSigInputScript(signatures,scripts.get(0));
            transactionSignature.getScripts().clear();
            transactionSignature.getScripts().add(scriptSign);
            tx.setTransactionSignature(transactionSignature.serialize());
            // 保存未确认交易到本地账户
            Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
                    //重新算一次交易(不超出最大交易数据大小下)的最大金额
                    Result rs = accountLedgerService.getMaxAmountOfOnce(fromAddr, tx,
                            TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
                    if (rs.isSuccess()) {
                        Na maxAmount = (Na) rs.getData();
                        rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
                        rs.setMsg(rs.getMsg() + maxAmount.toDouble());
                    }
                    return rs;
                }
                return saveResult;
            }
            transactionService.newTx(tx);
            Result sendResult = transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                accountLedgerService.deleteTransaction(tx);
                return sendResult;
            }
            return Result.getSuccess().setData(tx.getHash().getDigestHex());
        }
        //如果签名数还没达到，则返回交易
        else{
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
            return Result.getSuccess().setData(Hex.encode(tx.serialize()));
        }
    }
}
