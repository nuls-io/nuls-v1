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
package io.nuls.consensus.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusAgentImpl;
import io.nuls.consensus.entity.ConsensusDelegateImpl;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.entity.params.JoinConsensusParam;
import io.nuls.consensus.entity.tx.PocExitConsensusTransaction;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.event.TransactionEvent;
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

    @Autowired
    private AccountService accountService;
    @Autowired
    private EventBroadcaster eventBroadcaster;
    @Autowired
    private LedgerService ledgerService;
    @Autowired
    private BlockService blockService;

    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();

    private void registerAgent(Agent agent, Account account, String password) throws IOException, NulsException {
        TransactionEvent event = new TransactionEvent();
        CoinTransferData data = new CoinTransferData();
        data.setFee(this.getTxFee(TransactionConstant.TX_TYPE_REGISTER_AGENT));
        data.setTotalNa(agent.getDeposit());
        data.addFrom(account.getAddress().toString(), agent.getDeposit());
        Coin coin = new Coin();
        coin.setCanBeUnlocked(true);
        coin.setUnlockHeight(Long.MAX_VALUE);
        coin.setUnlockTime(0);
        coin.setNa(agent.getDeposit());
        data.addTo(account.getAddress().toString(), coin);
        RegisterAgentTransaction tx = null;
        try {
            tx = new RegisterAgentTransaction(data, password);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        Consensus<Agent> con = new ConsensusAgentImpl();
        con.setAddress(account.getAddress().toString());
        con.setExtend(agent);
        tx.setTxData(con);
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), account, password).serialize());
        tx.verifyWithException();
        event.setEventBody(tx);
       List<String> nodeList = eventBroadcaster.broadcastHashAndCache(event, true);
       if(null==nodeList||nodeList.isEmpty()){
           throw new NulsRuntimeException(ErrorCode.FAILED,"broadcast transaction failed!");
       }
    }

    private void joinTheConsensus(Account account, String password, long amount, String agentAddress) throws IOException, NulsException {
        AssertUtil.canNotEmpty(account);
        AssertUtil.canNotEmpty(password);
        if (amount < PocConsensusConstant.ENTRUSTER_DEPOSIT_LOWER_LIMIT.getValue()) {
            throw new NulsRuntimeException(ErrorCode.NULL_PARAMETER);
        }
        AssertUtil.canNotEmpty(agentAddress);
        TransactionEvent event = new TransactionEvent();
        Consensus<Delegate> ca = new ConsensusDelegateImpl();
        ca.setAddress(account.getAddress().toString());
        Delegate delegate = new Delegate();
        delegate.setDelegateAddress(agentAddress);
        delegate.setDeposit(Na.valueOf(amount));
        delegate.setStartTime(TimeService.currentTimeMillis());
        ca.setExtend(delegate);
        CoinTransferData data = new CoinTransferData();
        data.setFee(this.getTxFee(TransactionConstant.TX_TYPE_REGISTER_AGENT));
        data.setTotalNa(delegate.getDeposit());
        data.addFrom(account.getAddress().toString(), delegate.getDeposit());
        Coin coin = new Coin();
        coin.setCanBeUnlocked(true);
        coin.setUnlockHeight(Long.MAX_VALUE);
        coin.setUnlockTime(0);
        coin.setNa(delegate.getDeposit());
        data.addTo(account.getAddress().toString(), coin);
        PocJoinConsensusTransaction tx = null;
        try {
            tx = new PocJoinConsensusTransaction(data, password);
        } catch (NulsException e) {
            throw new NulsRuntimeException(e);
        }
        tx.setTime(TimeService.currentTimeMillis());
        tx.setTxData(ca);
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), account, password).serialize());
        tx.verifyWithException();
        event.setEventBody(tx);

//        TransactionEvent newEvent = new TransactionEvent();
//        try {
//            newEvent.parse(event.serialize());
//            System.out.println(Hex.encode(newEvent.serialize()).equalsIgnoreCase(Hex.encode(event.serialize())));
//        } catch (NulsException e) {
//            Log.error(e);
//        }
        List<String> nodeList = eventBroadcaster.broadcastHashAndCache(event, true);
        if(null==nodeList||nodeList.isEmpty()){
            throw new NulsRuntimeException(ErrorCode.FAILED,"broadcast transaction failed!");
        }
    }

    @Override
    public void stopConsensus(String address, String password, Map<String, Object> paramsMap) throws NulsException, IOException {
        Transaction joinTx = null;
        if (null != paramsMap && StringUtils.isNotBlank((String) paramsMap.get("txHash"))) {
            PocJoinConsensusTransaction tx = (PocJoinConsensusTransaction) ledgerService.getTx(NulsDigestData.fromDigestHex((String) paramsMap.get("txHash")));
            address = tx.getTxData().getAddress();
            joinTx = tx;
        } else {
            try {
                List<Transaction> txlist = this.ledgerService.getTxList(address, TransactionConstant.TX_TYPE_REGISTER_AGENT);
                if (null != txlist || !txlist.isEmpty()) {
                    joinTx = txlist.get(0);
                }
            } catch (Exception e) {
                Log.error(e);
            }

        }
        if (null == joinTx) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The related transaction is not exist!");
        }
        Account account = this.accountService.getAccount(address);
        if (null == account) {
            throw new NulsRuntimeException(ErrorCode.ACCOUNT_NOT_EXIST, "address:" + address.toString());
        }
        if (!account.validatePassword(password)) {
            throw new NulsRuntimeException(ErrorCode.PASSWORD_IS_WRONG);
        }
        TransactionEvent event = new TransactionEvent();
        PocExitConsensusTransaction tx = new PocExitConsensusTransaction();
        tx.setTxData(joinTx.getHash());
        try {
            tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.HASH_ERROR, e);
        }
        tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), account, password).serialize());
        event.setEventBody(tx);
        eventBroadcaster.broadcastHashAndCache(event, true);
    }

    @Override
    public List<Consensus> getConsensusAccountList() {
        List<Consensus<Agent>> list = consensusCacheManager.getCachedAgentList(ConsensusStatusEnum.IN);
        List<Consensus> resultList = new ArrayList<>(list);
        resultList.addAll(consensusCacheManager.getCachedDelegateList(ConsensusStatusEnum.IN));
        return resultList;
    }

    @Override
    public ConsensusStatusInfo getConsensusInfo(String address) {
        if (StringUtils.isBlank(address)) {
            address = this.accountService.getDefaultAccount().getAddress().getBase58();
        }
        return consensusCacheManager.getConsensusStatusInfo(address);
    }

    @Override
    public Na getTxFee(int txType) {
        long blockHeight = blockService.getLocalHeight();
        if (txType == TransactionConstant.TX_TYPE_COIN_BASE ||
                txType == TransactionConstant.TX_TYPE_SMALL_CHANGE ||
                txType == TransactionConstant.TX_TYPE_EXIT_CONSENSUS
                ) {
            return Na.ZERO;
        }
        long x = blockHeight / PocConsensusConstant.BLOCK_COUNT_OF_YEAR + 1;
        return PocConsensusConstant.TRANSACTION_FEE.div(x);
    }

    @Override
    public void startConsensus(String address, String password, Map<String, Object> paramsMap) throws NulsException {
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
            Agent agent = new Agent();
            agent.setDelegateAddress(params.getAgentAddress());
            agent.setDeposit(Na.valueOf(params.getDeposit()));
            agent.setIntroduction(params.getIntroduction());
            agent.setSeed(params.isSeed());
            agent.setCommissionRate(params.getCommissionRate());
            agent.setAgentName(params.getAgentName());
            try {
                this.registerAgent(agent, account, password);
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
