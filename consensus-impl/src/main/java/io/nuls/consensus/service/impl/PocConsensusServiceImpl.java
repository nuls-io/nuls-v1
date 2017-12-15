package io.nuls.consensus.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Agent;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.consensus.params.JoinConsensusParam;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.service.intf.EventService;
import io.nuls.ledger.service.intf.LedgerCacheService;
import io.nuls.ledger.service.intf.LedgerService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/9
 */
public class PocConsensusServiceImpl implements ConsensusService {

    private static final ConsensusService INSTANCE = new PocConsensusServiceImpl();
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);
    private EventService eventService = NulsContext.getInstance().getService(EventService.class);
    private LedgerCacheService ledgerCacheService = NulsContext.getInstance().getService(LedgerCacheService.class);
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    private PocConsensusServiceImpl() {
    }

    public static ConsensusService getInstance() {
        return INSTANCE;
    }

    private void registerAgent(Agent agent, Account account, String password) throws IOException {
        RegisterAgentEvent event = new RegisterAgentEvent();
        RegisterAgentTransaction tx = new RegisterAgentTransaction();
        tx.setTxData(agent);
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        tx.setSign(accountService.signData(tx.getHash(),account,password));
        tx.verify();
        event.setEventBody(tx);
        eventService.broadcastHashAndCache(event);
        ledgerCacheService.putTx(tx);
    }

    private void joinTheConsensus(Account account, String password, double amount, String agent) {
        //todo
        //check password
        //check block height
        //check consensus state
        //check credit
        //check balance

//        Transaction tx = createTransaction(address,agentMiner,inputs,sign);
//        JoinConsensusEvent event = createJoinEvent( tx);
        //broadcast
        //change consensus status in cache
    }


    @Override
    public void exitTheConsensus(Address address, String password) {
        //todo 验证密码

        //组装申请

        //广播
    }

    @Override
    public List<ConsensusAccount> getConsensusAccountList(Map<String, Object> params) {
        //todo
        return null;
    }

    @Override
    public ConsensusStatusInfo getConsensusInfo(String address) {
        //todo
        return null;
    }

    @Override
    public Na getTxFee(long blockHeight, Transaction tx) {
        long x = blockHeight / PocConsensusConstant.BLOCK_COUNT_OF_YEAR + 1;
        return PocConsensusConstant.TRANSACTION_FEE.div(x);
    }

    @Override
    public void joinTheConsensus(String address, String password, Map<String, Object> paramsMap) {
        Account account = this.accountService.getAccount(address);
        if (null == account) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The account is not exist,address:" + address);
        }
        if (paramsMap == null || paramsMap.size() < 2) {
            throw new NulsRuntimeException(ErrorCode.NULL_PARAMETER);
        }
        JoinConsensusParam params = new JoinConsensusParam(paramsMap);
        if (params.getCommissionRate() != null) {
            Agent agent = new Agent();
            agent.setAddress(address);
            agent.setDelegateAddress(params.getAgentAddress());
            agent.setDeposit(params.getDeposit());
            agent.setCommissionRate(params.getCommissionRate());
            agent.setIntroduction(params.getIntroduction());
            try {
                this.registerAgent(agent, account, password);
            } catch (IOException e) {
                throw new NulsRuntimeException(e);
            }
            return;
        }
        this.joinTheConsensus(account, password, params.getDeposit(), params.getAgentAddress());
    }

}
