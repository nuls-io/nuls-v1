package io.nuls.consensus.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.entity.params.JoinConsensusParam;
import io.nuls.consensus.entity.params.QueryConsensusAccountParam;
import io.nuls.consensus.entity.tx.PocExitConsensusTransaction;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.event.ExitConsensusEvent;
import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.service.intf.LedgerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/9
 */
public class PocConsensusServiceImpl implements ConsensusService {

    private static final ConsensusService INSTANCE = new PocConsensusServiceImpl();
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);
    private NetworkEventBroadcaster networkEventBroadcaster = NulsContext.getInstance().getService(NetworkEventBroadcaster.class);
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);
    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();

    private PocConsensusServiceImpl() {
    }

    public static ConsensusService getInstance() {
        return INSTANCE;
    }

    private void registerAgent(Agent agent, Account account, String password) throws IOException {
        RegisterAgentEvent event = new RegisterAgentEvent();
        CoinTransferData data = new CoinTransferData();
        data.setFee(this.getTxFee(TransactionConstant.TX_TYPE_REGISTER_AGENT));

        data.setTotalNa(agent.getDeposit());
        data.addFrom(account.getAddress().toString(), agent.getDeposit());
        Coin coin = new Coin();
        coin.setCanBeUnlocked(true);
        coin.setUnlockHeight(0);
        coin.setUnlockTime(0);
        coin.setNa(agent.getDeposit());
        data.addTo(account.getAddress().toString(), coin);
        RegisterAgentTransaction tx = new RegisterAgentTransaction(data, password);
        Consensus<Agent> con = new Consensus<>();
        con.setAddress(account.getAddress().toString());
        con.setExtend(agent);
        tx.setTxData(con);
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        tx.setSign(accountService.signData(tx.getHash(), account, password));
        ledgerService.verifyTx(tx);
        //todo 缓存

        event.setEventBody(tx);
        networkEventBroadcaster.broadcastHashAndCache(event);
    }

    private void joinTheConsensus(Account account, String password, double amount, String agentAddress) throws IOException {
        JoinConsensusEvent event = new JoinConsensusEvent();
        PocJoinConsensusTransaction tx = new PocJoinConsensusTransaction();
        Consensus<Delegate> ca = new Consensus<>();
        ca.setAddress(account.getAddress().toString());
        Delegate delegate = new Delegate();
        delegate.setDelegateAddress(agentAddress);
        delegate.setDeposit(Na.parseNuls(amount));
        ca.setExtend(delegate);
        tx.setTxData(ca);
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        tx.setSign(accountService.signData(tx.getHash(), account, password));
        ledgerService.verifyTx(tx);
        //todo 缓存
        event.setEventBody(tx);
        networkEventBroadcaster.broadcastHashAndCache(event);
    }

    @Override
    public void stopConsensus(NulsDigestData joinTxHash, String password) {
        PocJoinConsensusTransaction joinTx = (PocJoinConsensusTransaction) ledgerService.getTx(joinTxHash);
        if (null == joinTx) {
            throw new NulsRuntimeException(ErrorCode.ACCOUNT_NOT_EXIST, "address:" + joinTx.getTxData().getAddress().toString());
        }
        Account account = this.accountService.getAccount(joinTx.getTxData().getAddress().toString());
        if (null == account) {
            throw new NulsRuntimeException(ErrorCode.ACCOUNT_NOT_EXIST, "address:" + joinTx.getTxData().getAddress().toString());
        }
        if (!account.validatePassword(password)) {
            throw new NulsRuntimeException(ErrorCode.PASSWORD_IS_WRONG);
        }
        ExitConsensusEvent event = new ExitConsensusEvent();
        PocExitConsensusTransaction tx = new PocExitConsensusTransaction();
        tx.setTxData(joinTxHash);
        try {
            tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.HASH_ERROR, e);
        }
        tx.setSign(accountService.signData(tx.getHash(), account, password));
        event.setEventBody(tx);
        networkEventBroadcaster.broadcastHashAndCache(event);
    }

    @Override
    public List<Consensus> getConsensusAccountList(String address, String agentAddress) {
        QueryConsensusAccountParam param = new QueryConsensusAccountParam();
        param.setAddress(address);
        param.setAgentAddress(agentAddress);
        List<Consensus<Agent>> list = consensusCacheManager.getCachedAgentList(ConsensusStatusEnum.IN);
        List<Consensus> resultList = new ArrayList<>(list);
        resultList.addAll(consensusCacheManager.getCachedDelegateList());
        return resultList;
    }

    @Override
    public ConsensusStatusInfo getConsensusInfo(String address) {
        if (StringUtils.isBlank(address)) {
            address = this.accountService.getDefaultAccount();
        }
        return consensusCacheManager.getConsensusStatusInfo(address);
    }

    @Override
    public Na getTxFee(int txType) {
        long blockHeight = blockService.getLocalHeight();
        if (txType == TransactionConstant.TX_TYPE_COIN_BASE ||
                txType == TransactionConstant.TX_TYPE_SMALL_CHANGE) {
            return Na.ZERO;
        }
        long x = blockHeight / PocConsensusConstant.BLOCK_COUNT_OF_YEAR + 1;
        return PocConsensusConstant.TRANSACTION_FEE.div(x);
    }

    @Override
    public void startConsensus(String address, String password, Map<String, Object> paramsMap) {
        Account account = this.accountService.getAccount(address);
        if (null == account) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The account is not exist,address:" + address);
        }
        if (paramsMap == null || paramsMap.size() < 2) {
            throw new NulsRuntimeException(ErrorCode.NULL_PARAMETER);
        }
        if (!account.validatePassword(password)) {
            throw new NulsRuntimeException(ErrorCode.PASSWORD_IS_WRONG);
        }
        JoinConsensusParam params = new JoinConsensusParam(paramsMap);
        if (StringUtils.isNotBlank(params.getIntroduction())) {
            Agent delegate = new Agent();
            delegate.setDelegateAddress(params.getAgentAddress());
            delegate.setDeposit(Na.parseNuls(params.getDeposit()));
            delegate.setIntroduction(params.getIntroduction());
            delegate.setSeed(params.isSeed());
            try {
                this.registerAgent(delegate, account, password);
            } catch (IOException e) {
                throw new NulsRuntimeException(e);
            }
            return;
        }
        try {
            this.joinTheConsensus(account, password, params.getDeposit(), params.getAgentAddress());
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
    }

}
