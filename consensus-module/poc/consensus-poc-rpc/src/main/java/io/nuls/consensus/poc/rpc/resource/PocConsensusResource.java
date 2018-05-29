package io.nuls.consensus.poc.rpc.resource;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.service.AccountService;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.CancelDeposit;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.entity.StopAgent;
import io.nuls.consensus.poc.protocol.tx.CancelDepositTransaction;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.DepositTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.rpc.model.*;
import io.nuls.consensus.poc.rpc.utils.AgentComparator;
import io.nuls.consensus.poc.service.impl.PocRewardCacheService;
import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.service.TransactionService;
import io.protostuff.Rpc;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Niels
 * @date 2018/5/15
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
        int memberCount = 0;
        long totalDeposit = 0;
        if (null != round) {
            memberCount = round.getMemberList().size();
            for (MeetingMember member : round.getMemberList()) {
                totalDeposit += (member.getTotalDeposit().getValue() + member.getOwnDeposit().getValue());
            }
        }

        dto.setAgentCount(agentList.size());
        dto.setRewardOfDay(this.rewardCacheService.getAllReward().getValue());
        dto.setTotalDeposit(totalDeposit);
        dto.setConsensusAccountNumber(memberCount);
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

        if (!Address.validAddress(StringUtils.formatStringPara(address))) {
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
        AssertUtil.canNotEmpty(form.getAgentName(), "agent name can not be null");
        AssertUtil.canNotEmpty(form.getCommissionRate(), "commission rate can not be null");
        AssertUtil.canNotEmpty(form.getDeposit(), "deposit can not be null");
        AssertUtil.canNotEmpty(form.getPackingAddress(), "packing address can not be null");
        AssertUtil.canNotEmpty(form.getRemark(), "agent instraction can not be null");
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
        try {
            agent.setIntroduction(form.getRemark().getBytes(NulsConfig.DEFAULT_ENCODING));
            agent.setAgentName(form.getAgentName().getBytes(NulsConfig.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            return Result.getFailed(e.getMessage()).toRpcClientResult();
        }
        agent.setDeposit(Na.valueOf(form.getDeposit()));
        agent.setCommissionRate(form.getCommissionRate());
        tx.setTxData(agent);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(agent.getAgentAddress(), agent.getDeposit(), -1));
        coinData.setTo(toList);
        tx.setCoinData(coinData);
        CoinDataResult result = accountLedgerService.getCoinData(agent.getAgentAddress(), agent.getDeposit(), tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);
        tx.getCoinData().setFrom(result.getCoinList());
        if (null != result.getChange()) {
            tx.getCoinData().getTo().add(result.getChange());
        }
        Na fee = TransactionFeeCalculator.getFee(tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);
        return Result.getSuccess().setData(fee.getValue()).toRpcClientResult();
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
        CoinDataResult result = accountLedgerService.getCoinData(deposit.getAddress(), deposit.getDeposit(), tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);
        tx.getCoinData().setFrom(result.getCoinList());
        if (null != result.getChange()) {
            tx.getCoinData().getTo().add(result.getChange());
        }
        Na fee = TransactionFeeCalculator.getFee(tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);
        return Result.getSuccess().setData(fee.getValue()).toRpcClientResult();
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
            return Result.getFailed("Can not found any agent!").toRpcClientResult();
        }
        NulsDigestData createTxHash = agent.getTxHash();
        stopAgent.setCreateTxHash(createTxHash);
        tx.setTxData(stopAgent);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(stopAgent.getAddress(), agent.getDeposit(), 0));
        coinData.setTo(toList);
        CreateAgentTransaction transaction = (CreateAgentTransaction) ledgerService.getTx(createTxHash);
        if (null == transaction) {
            return Result.getFailed("Can not find the create agent transaction!").toRpcClientResult();
        }
        List<Coin> fromList = new ArrayList<>();
        for (int index = 0; index < transaction.getCoinData().getTo().size(); index++) {
            Coin coin = transaction.getCoinData().getTo().get(index);
            if (coin.getLockTime() == -1L && coin.getNa().equals(agent.getDeposit())) {
                coin.setOwner(ArraysTool.joinintTogether(transaction.getHash().serialize(), new VarInt(index).encode()));
                fromList.add(coin);
                break;
            }
        }
        if (fromList.isEmpty()) {
            return Result.getFailed(KernelErrorCode.DATA_ERROR).toRpcClientResult();
        }
        coinData.setFrom(fromList);
        Na fee = TransactionFeeCalculator.getFee(tx.size());
        coinData.getTo().get(0).setNa(coinData.getTo().get(0).getNa().subtract(fee));
        tx.setCoinData(coinData);
        Na resultFee = TransactionFeeCalculator.getFee(tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);
        return Result.getSuccess().setData(resultFee.getValue()).toRpcClientResult();
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
        AssertUtil.canNotEmpty(form.getAgentName(), "agent name can not be null");
        AssertUtil.canNotEmpty(form.getCommissionRate(), "commission rate can not be null");
        AssertUtil.canNotEmpty(form.getDeposit(), "deposit can not be null");
        AssertUtil.canNotEmpty(form.getPackingAddress(), "packing address can not be null");
        AssertUtil.canNotEmpty(form.getRemark(), "agent instraction can not be null");

        if (!AddressTool.validAddress(form.getPackingAddress()) || !AddressTool.validAddress(form.getAgentAddress())) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = accountService.getAccount(form.getAgentAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(form.getPassword());
            try {
                if (!account.decrypt(form.getPassword())) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
                }
            } catch (NulsException e) {
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
        try {
            agent.setAgentName(form.getAgentName().getBytes(NulsConfig.DEFAULT_ENCODING));
            agent.setIntroduction(form.getRemark().getBytes(NulsConfig.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            return Result.getFailed(e.getMessage()).toRpcClientResult();
        }
        agent.setDeposit(Na.valueOf(form.getDeposit()));
        agent.setCommissionRate(form.getCommissionRate());
        tx.setTxData(agent);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(agent.getAgentAddress(), agent.getDeposit(), -1));
        coinData.setTo(toList);
        tx.setCoinData(coinData);
        CoinDataResult result = accountLedgerService.getCoinData(agent.getAgentAddress(), agent.getDeposit(), tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);
        RpcClientResult result1 = this.txProcessing(tx, result, account, form.getPassword());
        if (!result1.isSuccess()) {
            return result1;
        }
        return Result.getSuccess().setData(tx.getHash().getDigestHex()).toRpcClientResult();
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
        AssertUtil.canNotEmpty(form.getDeposit());
        if (!AddressTool.validAddress(form.getAddress())) {
            throw new NulsRuntimeException(KernelErrorCode.PARAMETER_ERROR);
        }
        Account account = accountService.getAccount(form.getAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(form.getPassword());
            try {
                if (!account.decrypt(form.getPassword())) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
                }
            } catch (NulsException e) {
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
        toList.add(new Coin(deposit.getAddress(), deposit.getDeposit(), -1));
        coinData.setTo(toList);
        tx.setCoinData(coinData);
        CoinDataResult result = accountLedgerService.getCoinData(deposit.getAddress(), deposit.getDeposit(), tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);

        RpcClientResult result1 = this.txProcessing(tx, result, account, form.getPassword());
        if (!result1.isSuccess()) {
            return result1;
        }
        return Result.getSuccess().setData(tx.getHash().getDigestHex()).toRpcClientResult();
    }

    public RpcClientResult txProcessing(Transaction tx, CoinDataResult result, Account account, String password) {
        if (null != result) {
            if (result.isEnough()) {
                tx.getCoinData().setFrom(result.getCoinList());
                if (null != result.getChange()) {
                    tx.getCoinData().getTo().add(result.getChange());
                }
            } else {
                return Result.getFailed(TransactionErrorCode.BALANCE_NOT_ENOUGH).toRpcClientResult();
            }
        }
        try {
            tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
            P2PKHScriptSig sig = new P2PKHScriptSig();
            sig.setPublicKey(account.getPubKey());
            sig.setSignData(accountService.signDigest(tx.getHash().serialize(), account, password));
            tx.setScriptSig(sig.serialize());
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage()).toRpcClientResult();
        }
        Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
        if (saveResult.isFailed()) {
            return saveResult.toRpcClientResult();
        }
        Result sendResult = this.transactionService.broadcastTx(tx);
        if (sendResult.isFailed()) {
            accountLedgerService.rollbackTransaction(tx);
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
            try {
                if (!account.decrypt(form.getPassword())) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
                }
            } catch (NulsException e) {
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
            if (Arrays.equals(a.getAgentAddress(), account.getAddress().getBase58Bytes())) {
                agent = a;
                break;
            }
        }
        if (agent == null || agent.getDelHeight() > 0) {
            return Result.getFailed("Can not found any agent!").toRpcClientResult();
        }
        NulsDigestData createTxHash = agent.getTxHash();
        stopAgent.setCreateTxHash(createTxHash);
        tx.setTxData(stopAgent);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(stopAgent.getAddress(), agent.getDeposit(), 0));
        coinData.setTo(toList);
        CreateAgentTransaction transaction = (CreateAgentTransaction) ledgerService.getTx(createTxHash);
        if (null == transaction) {
            return Result.getFailed("Can not find the create agent transaction!").toRpcClientResult();
        }
        List<Coin> fromList = new ArrayList<>();
        for (int index = 0; index < transaction.getCoinData().getTo().size(); index++) {
            Coin coin = transaction.getCoinData().getTo().get(index);
            if (coin.getLockTime() == -1L && coin.getNa().equals(agent.getDeposit())) {
                coin.setOwner(ArraysTool.joinintTogether(transaction.getHash().serialize(), new VarInt(index).encode()));
                fromList.add(coin);
                break;
            }
        }
        if (fromList.isEmpty()) {
            return Result.getFailed(KernelErrorCode.DATA_ERROR).toRpcClientResult();
        }
        coinData.setFrom(fromList);
        Na fee = TransactionFeeCalculator.getFee(tx.size());
        coinData.getTo().get(0).setNa(coinData.getTo().get(0).getNa().subtract(fee));
        tx.setCoinData(coinData);
        RpcClientResult result1 = this.txProcessing(tx, null, account, form.getPassword());
        if (!result1.isSuccess()) {
            return result1;
        }
        return Result.getSuccess().setData(tx.getHash().getDigestHex()).toRpcClientResult();
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
                String agentName = new String(agent.getAgentName(), NulsConfig.DEFAULT_ENCODING);
                String agentAddress = Base58.encode(agent.getAgentAddress());
                String packingAddress = Base58.encode(agent.getPackingAddress());
                boolean b = agentName.indexOf(keyword) >= 0;
                b = b || agentAddress.equals(keyword) || packingAddress.equals(keyword);
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
        int type = AgentComparator.COMMISSION_RATE;
        if ("deposit".equals(sortType)) {
            type = AgentComparator.DEPOSIT;
        } else if ("commissionRate".equals(sortType)) {
            type = AgentComparator.COMMISSION_RATE;
        } else if ("creditVal".equals(sortType)) {
            type = AgentComparator.CREDIT_VALUE;
        } else if ("totalDeposit".equals(sortType)) {
            type = AgentComparator.DEPOSITABLE;
        }
        Collections.sort(agentList, AgentComparator.getInstance(type));
        List<AgentDTO> resultList = new ArrayList<>();
        for (int i = start; i < agentList.size() && i < (start + pageSize); i++) {
            resultList.add(new AgentDTO(agentList.get(i)));
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
        for (Deposit deposit : depositList) {
            if (!agent.getTxHash().equals(deposit.getAgentHash())) {
                continue;
            }
            if (deposit.getDelHeight() >= 0) {
                continue;
            }
            total = total.add(deposit.getDeposit());
            memberSet.add(Base58.encode(deposit.getAddress()));
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
    public RpcClientResult getAgentByAddress(@ApiParam(name = "agentHash", value = "节点标识", required = true)
                                              @PathParam("agentHash") String agentHash) throws NulsException {
        AssertUtil.canNotEmpty(agentHash);
        Result result = Result.getSuccess();
        List<Agent> agentList = PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();
        NulsDigestData agentHashData = NulsDigestData.fromDigestHex(agentHash);
        for (Agent agent : agentList) {
            if (agent.getTxHash().equals(agentHashData)) {
                MeetingRound round = PocConsensusContext.getChainManager().getMasterChain().getCurrentRound();
                this.fillAgent(agent, round, null);
                AgentDTO dto = new AgentDTO(agent);
                result.setData(dto);
                return result.toRpcClientResult();
            }
        }
        return Result.getFailed("Can not find agent!").toRpcClientResult();
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
            return Result.getFailed("The address is wrong!").toRpcClientResult();
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
        for (int i = allAgentList.size() - 1; i >= 0; i--) {
            Agent agent = allAgentList.get(i);
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            if (!agentHashSet.contains(agent.getTxHash())) {
                continue;
            }
            agentList.add(agent);
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
            resultList.add(new AgentDTO(agentList.get(i)));
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
            return Result.getFailed("The address is wrong!").toRpcClientResult();
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
    public RpcClientResult exitConsensus(@ApiParam(name = "form", value = "退出共识表单数据", required = true)
                                        WithdrawForm form) throws NulsException, IOException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getTxHash());
        AssertUtil.canNotEmpty(form.getAddress());
        if (!Address.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR).toRpcClientResult();
        }
        Account account = accountService.getAccount(form.getAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST).toRpcClientResult();
        }
        if (account.isEncrypted() && account.isLocked()) {
            AssertUtil.canNotEmpty(form.getPassword());
            try {
                if (!account.decrypt(form.getPassword())) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
                }
            } catch (NulsException e) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG).toRpcClientResult();
            }
        }
        CancelDepositTransaction tx = new CancelDepositTransaction();
        CancelDeposit cancelDeposit = new CancelDeposit();
        NulsDigestData hash = NulsDigestData.fromDigestHex(form.getTxHash());
        DepositTransaction depositTransaction = (DepositTransaction) ledgerService.getTx(hash);
        if (null == depositTransaction) {
            return Result.getFailed("Cann't find the deposit transaction!").toRpcClientResult();
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
                coin.setOwner(ArraysTool.joinintTogether(hash.serialize(), new VarInt(index).encode()));
                fromList.add(coin);
                break;
            }
        }
        if (fromList.isEmpty()) {
            return Result.getFailed(KernelErrorCode.DATA_ERROR).toRpcClientResult();
        }
        coinData.setFrom(fromList);
        tx.setCoinData(coinData);
        RpcClientResult result1 = this.txProcessing(tx, null, account, form.getPassword());
        if (!result1.isSuccess()) {
            return result1;
        }
        return Result.getSuccess().setData(tx.getHash().getDigestHex()).toRpcClientResult();
    }
}
