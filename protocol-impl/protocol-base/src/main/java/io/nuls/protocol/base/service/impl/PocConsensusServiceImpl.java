/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.protocol.base.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.params.OperationType;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.base.constant.PocConsensusConstant;
import io.nuls.protocol.base.entity.ConsensusAgentImpl;
import io.nuls.protocol.base.entity.ConsensusDepositImpl;
import io.nuls.protocol.base.entity.member.Agent;
import io.nuls.protocol.base.entity.member.Deposit;
import io.nuls.protocol.base.entity.params.JoinConsensusParam;
import io.nuls.protocol.base.entity.tx.CancelDepositTransaction;
import io.nuls.protocol.base.entity.tx.PocJoinConsensusTransaction;
import io.nuls.protocol.base.entity.tx.RegisterAgentTransaction;
import io.nuls.protocol.base.entity.tx.StopAgentTransaction;
import io.nuls.protocol.entity.Consensus;
import io.nuls.protocol.intf.BlockService;
import io.nuls.protocol.intf.ConsensusService;

import java.io.IOException;
import java.util.*;

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

    private Transaction registerAgent(Agent agent, Account account, String password) throws IOException, NulsException {
        TransactionEvent event = new TransactionEvent();
        CoinTransferData data = new CoinTransferData(OperationType.LOCK, this.ledgerService.getTxFee(TransactionConstant.TX_TYPE_REGISTER_AGENT));
        data.setTotalNa(agent.getDeposit());
        data.addFrom(account.getAddress().toString());
        Coin coin = new Coin();
        coin.setUnlockHeight(0);
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
        agent.setStartTime(TimeService.currentTimeMillis());
        con.setExtend(agent);
        tx.setTxData(con);
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), account, password).serialize());
        tx.verifyWithException();
        event.setEventBody(tx);
        boolean b  = eventBroadcaster.publishToLocal(event);
        if (!b) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "broadcast transaction failed!");
        }
        return tx;
    }

    private Transaction joinTheConsensus(Account account, String password, long amount, String agentHash) throws IOException, NulsException {
        AssertUtil.canNotEmpty(account);
        AssertUtil.canNotEmpty(password);
        if (amount < PocConsensusConstant.ENTRUSTER_DEPOSIT_LOWER_LIMIT.getValue()) {
            throw new NulsRuntimeException(ErrorCode.NULL_PARAMETER);
        }
        AssertUtil.canNotEmpty(agentHash);
        TransactionEvent event = new TransactionEvent();
        Consensus<Deposit> ca = new ConsensusDepositImpl();
        ca.setAddress(account.getAddress().toString());
        Deposit deposit = new Deposit();
        deposit.setAgentHash(agentHash);
        deposit.setDeposit(Na.valueOf(amount));
        deposit.setStartTime(TimeService.currentTimeMillis());
        ca.setExtend(deposit);
        CoinTransferData data = new CoinTransferData(OperationType.LOCK, this.ledgerService.getTxFee(TransactionConstant.TX_TYPE_JOIN_CONSENSUS));
        data.setTotalNa(deposit.getDeposit());
        data.addFrom(account.getAddress().toString());
        Coin coin = new Coin();
        coin.setUnlockHeight(0);
        coin.setUnlockTime(0);
        coin.setNa(deposit.getDeposit());
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
        boolean b = eventBroadcaster.publishToLocal(event);
        if (!b) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "broadcast transaction failed!");
        }
        return tx;
    }

    @Override
    public Transaction stopConsensus(String address, String password, Map<String, Object> paramsMap) throws NulsException, IOException {
        AbstractCoinTransaction joinTx = null;
        if (null != paramsMap && StringUtils.isNotBlank((String) paramsMap.get("txHash"))) {
            PocJoinConsensusTransaction tx = (PocJoinConsensusTransaction) ledgerService.getTx(NulsDigestData.fromDigestHex((String) paramsMap.get("txHash")));
            joinTx = tx;
        } else {
            try {
                List<Transaction> txlist = this.ledgerService.getTxList(address, TransactionConstant.TX_TYPE_REGISTER_AGENT);
                if (null != txlist || !txlist.isEmpty()) {
                    joinTx = (AbstractCoinTransaction) txlist.get(0);
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
        CoinTransferData coinTransferData = new CoinTransferData(OperationType.UNLOCK, this.ledgerService.getTxFee(TransactionConstant.TX_TYPE_CANCEL_DEPOSIT));
        if (joinTx.getType() == TransactionConstant.TX_TYPE_REGISTER_AGENT) {
            coinTransferData = new CoinTransferData(OperationType.UNLOCK, this.ledgerService.getTxFee(TransactionConstant.TX_TYPE_STOP_AGENT));
            Map<String, Object> sData = new HashMap<>();

            sData.put("type", 1);
            sData.put("lockedTxHash", joinTx.getHash());
            sData.put("lockTime", PocConsensusConstant.STOP_AGENT_DEPOSIT_LOCKED_TIME * 24 * 3600 * 1000);

            coinTransferData.setSpecialData(sData);

            coinTransferData.setTotalNa(Na.ZERO);
            coinTransferData.addFrom(account.getAddress().toString());
            StopAgentTransaction tx = new StopAgentTransaction(coinTransferData, password);
            tx.setTxData(joinTx.getHash());
            try {
                tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
            } catch (IOException e) {
                Log.error(e);
                throw new NulsRuntimeException(ErrorCode.HASH_ERROR, e);
            }
            tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), account, password).serialize());
            tx.verifyWithException();
            event.setEventBody(tx);
            eventBroadcaster.publishToLocal(event);

            return tx;
        }


        coinTransferData.setTotalNa(Na.ZERO);
        coinTransferData.addFrom(account.getAddress().toString());
        CancelDepositTransaction tx = new CancelDepositTransaction(coinTransferData, password);
        tx.setTxData(joinTx.getHash());
        try {
            tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.HASH_ERROR, e);
        }
        tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), account, password).serialize());
        tx.verifyWithException();
        event.setEventBody(tx);
        eventBroadcaster.publishToLocal(event);
        return tx;
    }

    @Override
    public Map<String, Object> getConsensusInfo() {
//        List<Consensus<Agent>> agentList = consensusCacheManager.getAgentList(ConsensusStatusEnum.IN);
//        List<Consensus<Deposit>> depositList = consensusCacheManager.getDepositList(ConsensusStatusEnum.IN);
//
//        long totalDeposit = 0L;
//        Set<String> memberSet = new HashSet<>();
//        for (Consensus<Agent> agent : agentList) {
//            totalDeposit += agent.getExtend().getDeposit().getValue();
//            memberSet.add(agent.getAddress());
//        }
//        for (Consensus<Deposit> deposit : depositList) {
//            totalDeposit += deposit.getExtend().getDeposit().getValue();
//            memberSet.add(deposit.getAddress());
//        }
//        //calc last 24h reward
//        long rewardOfDay = ledgerService.getLastDayTimeReward();
//
        Map<String, Object> map = new HashMap<>();
//        map.put("agentCount", agentList.size());
//        map.put("rewardOfDay", rewardOfDay);
//        map.put("totalDeposit", totalDeposit);
//        map.put("memberCount", memberSet.size());
        return map;
    }

    @Override
    public Map<String, Object> getConsensusInfo(String address) {
        if (StringUtils.isNotBlank(address)) {
            return getConsensusInfoByAddress(address);
        }
        List<Account> accountList = this.accountService.getAccountList();
        if (accountList == null || accountList.isEmpty()) {
            return null;
        }

        long lastDayTime = TimeService.currentTimeMillis() - DateUtil.DATE_TIME;
        int agentCount = 0;
        long totalDeposit = 0;
        long reward = 0;
        long rewardOfDay = 0;
        long usableBalance = 0;
        Set<String> joinedAgent = new HashSet<>();
//        for (Account account : accountList) {
//            Consensus<Agent> agent = this.consensusCacheManager.getAgentByAddress(account.getAddress().toString());
//            List<Consensus<Deposit>> depositList = this.consensusCacheManager.getDepositListByAddress(account.getAddress().toString());
//
//            for (Consensus<Deposit> cd : depositList) {
//                totalDeposit += cd.getExtend().getDeposit().getValue();
//                joinedAgent.add(cd.getExtend().getAgentHash());
//            }
//            if (null != agent) {
//                agentCount++;
//                totalDeposit += agent.getExtend().getDeposit().getValue();
//            }
//            reward += ledgerService.getAccountReward(account.getAddress().getBase58(), 0);
//            rewardOfDay += ledgerService.getAccountReward(account.getAddress().getBase58(), lastDayTime);
//            Balance balance = ledgerService.getBalance(account.getAddress().getBase58());
//            if (balance != null) {
//                usableBalance += balance.getUsable().getValue();
//            }
//        }
        Map<String, Object> map = new HashMap<>();
        map.put("agentCount", agentCount);
        map.put("totalDeposit", totalDeposit);
        map.put("joinAgentCount", joinedAgent.size());
        map.put("usableBalance", usableBalance);
        map.put("reward", reward);
        map.put("rewardOfDay", rewardOfDay);
        return map;
    }

    private Map<String, Object> getConsensusInfoByAddress(String address) {
        if (!Address.validAddress(address)) {
            return null;
        }
//        Consensus<Agent> agent = this.consensusCacheManager.getAgentByAddress(address);
//        List<Consensus<Deposit>> depositList = this.consensusCacheManager.getDepositListByAddress(address);
//        long totalDeposit = 0;
//        Set<String> joinedAgent = new HashSet<>();
//        for (Consensus<Deposit> cd : depositList) {
//            totalDeposit += cd.getExtend().getDeposit().getValue();
//            joinedAgent.add(cd.getExtend().getAgentHash());
//        }
        Map<String, Object> map = new HashMap<>();
//        if (null != agent) {
//            map.put("agentCount", 1);
////            totalDeposit += agent.getExtend().getDeposit().getValue();
//        } else {
//            map.put("agentCount", 0);
//        }
//        map.put("totalDeposit", totalDeposit);
        long lastDayTime = TimeService.currentTimeMillis() - DateUtil.DATE_TIME;
        long reward = ledgerService.getAccountReward(address, 0);
        long rewardOfDay = ledgerService.getAccountReward(address, lastDayTime);
        Balance balance = ledgerService.getBalance(address);
        map.put("reward", reward);
//        map.put("joinAccountCount", joinedAgent.size());
        if (null == balance || balance.getUsable() == null) {
            map.put("usableBalance", 0);
        } else {
            map.put("usableBalance", balance.getUsable().getValue());
        }
        map.put("rewardOfDay", rewardOfDay);
        return map;
    }

    @Override
    public Transaction startConsensus(String address, String password, Map<String, Object> paramsMap) throws NulsException {
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
            agent.setPackingAddress(params.getPackingAddress());
            agent.setDeposit(Na.valueOf(params.getDeposit()));
            agent.setIntroduction(params.getIntroduction());
            agent.setCommissionRate(params.getCommissionRate());
            agent.setAgentName(params.getAgentName());
            try {
                return this.registerAgent(agent, account, password);
            } catch (IOException e) {
                throw new NulsRuntimeException(e);
            }
        }
        try {
            return this.joinTheConsensus(account, password, params.getDeposit(), params.getAgentHash());
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
    }

    @Override
    public Page<Map<String, Object>> getAgentList(String keyword, String depositAddress, String agentAddress,
                                                  String sortType, Integer pageNumber, Integer pageSize) {
//        List<Consensus<Agent>> agentList = this.consensusCacheManager.getAliveAgentList(NulsContext.getInstance().getBestHeight());
//        agentList.addAll(this.consensusCacheManager.getUnconfirmedAgentList());
//        filterAgentList(agentList, agentAddress, depositAddress, keyword);
//
        Page<Map<String, Object>> page = new Page<>();
//        int start = pageNumber * pageSize - pageSize;
//
//        if (agentList.isEmpty() || start >= agentList.size()) {
//            if (StringUtils.isNotBlank(depositAddress)) {
//                Consensus<Agent> ca = consensusCacheManager.getAgentByAddress(depositAddress);
//                if (null != ca) {
//                    agentList.add(0, ca);
//                }
//            }
//            page.setPageNumber(pageNumber);
//            page.setPageSize(pageSize);
//            page.setTotal(agentList.size());
//            int sum = 0;
//            if (page.getTotal() % pageSize > 0) {
//                sum = 1;
//            }
//            page.setList(transList(agentList));
//            page.setPages((int) ((page.getTotal() / pageSize) + sum));
//            return page;
//        }
//
//        int end = pageNumber * pageSize;
//        if (end > agentList.size()) {
//            end = agentList.size();
//        }
//
//        int type = AgentComparator.COMMISSION_RATE;
//        if ("owndeposit".equals(sortType)) {
//            type = AgentComparator.DEPOSIT;
//        } else if ("commissionRate".equals(sortType)) {
//            type = AgentComparator.COMMISSION_RATE;
//        } else if ("creditRatio".equals(sortType)) {
//            type = AgentComparator.CREDIT_VALUE;
//        } else if ("totalDeposit".equals(sortType)) {
//            type = AgentComparator.DEPOSITABLE;
//        }
//        Collections.sort(agentList, AgentComparator.getInstance(type));
//
//        if (StringUtils.isNotBlank(depositAddress)) {
//            boolean b = true;
//            for (int i = 0; i < agentList.size(); i++) {
//                Consensus<Agent> ca = agentList.get(i);
//                if (ca.getAddress().equals(depositAddress)) {
//                    agentList.remove(i);
//                    agentList.add(0, ca);
//                    b = false;
//                    break;
//                }
//            }
//            if (b) {
//                Consensus<Agent> ca = consensusCacheManager.getAgentByAddress(depositAddress);
//                if (null != ca) {
//                    agentList.add(0, ca);
//                }
//            }
//        }
//        List<Consensus<Agent>> sublist = agentList.subList(start, end);
//        page.setPageNumber(pageNumber);
//        page.setPageSize(pageSize);
//        page.setTotal(agentList.size());
//        int sum = 0;
//        if (page.getTotal() % pageSize > 0) {
//            sum = 1;
//        }
//        page.setPages((int) ((page.getTotal() / pageSize) + sum));
//        List<Map<String, Object>> resultList = transList(sublist);
//        page.setList(resultList);
        return page;
    }

    private List<Map<String, Object>> transList(List<Consensus<Agent>> agentList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Consensus<Agent> ca : agentList) {
            Map<String, Object> map = new HashMap<>();
            map.put("agentId", ca.getHexHash());
            map.put("agentName", ca.getExtend().getAgentName());
            map.put("packingAddress", ca.getExtend().getPackingAddress());
            map.put("agentAddress", ca.getAddress());
            map.put("agentAddressAlias", null);
            map.put("status", ca.getExtend().getStatus());
            map.put("owndeposit", ca.getExtend().getDeposit().getValue());
            map.put("commissionRate", ca.getExtend().getCommissionRate());
            map.put("introduction", ca.getExtend().getIntroduction());
            map.put("startTime", ca.getExtend().getStartTime());
            map.put("creditRatio", ca.getExtend().getCreditVal());

            long reward = ledgerService.getAgentReward(ca.getAddress(), 1);
            map.put("reward", reward);
            map.put("packedCount", blockService.getPackingCount(ca.getExtend().getPackingAddress()));
//            List<Consensus<Deposit>> deposits = this.consensusCacheManager.getDepositListByAgentId(ca.getHexHash(), NulsContext.getInstance().getBestHeight());
            long totalDeposit = 0;
            Set<String> memberSet = new HashSet<>();
//            for (Consensus<Deposit> cd : deposits) {
//                totalDeposit += cd.getExtend().getDeposit().getValue();
//                memberSet.add(cd.getAddress());
//            }
            map.put("totalDeposit", totalDeposit);
            map.put("memberCount", memberSet.size());
            resultList.add(map);
        }
        return resultList;
    }


    @Override
    public Page<Map<String, Object>> getDepositList(String address, String agentAddress, Integer pageNumber, Integer pageSize) {
//        List<Consensus<Deposit>> depositList = this.consensusCacheManager.getAliveDepositList(NulsContext.getInstance().getBestHeight());
//        depositList.addAll(this.consensusCacheManager.getUnconfirmedDepositList());
//        boolean isAddress = Address.validAddress(address);
//        Consensus<Agent> agent = null;
//        if (Address.validAddress(agentAddress)) {
//            agent = this.consensusCacheManager.getAgentByAddress(agentAddress);
//            if (null == agent) {
//                depositList.clear();
//            }
//        }
//        for (int i = depositList.size() - 1; i >= 0; i--) {
//            Consensus<Deposit> cd = depositList.get(i);
//            if (isAddress && !cd.getAddress().equals(address)) {
//                depositList.remove(i);
//            }else
//            if (null != agent && !cd.getExtend().getAgentHash().equals(agent.getHexHash())) {
//                depositList.remove(i);
//            }
//        }
        Page<Map<String, Object>> page = new Page<>();
//        int start = pageNumber * pageSize - pageSize;
//        if (depositList.isEmpty() || start >= depositList.size()) {
//            page.setPageNumber(pageNumber);
//            page.setPageSize(pageSize);
//            page.setTotal(depositList.size());
//            int sum = 0;
//            if (page.getTotal() % pageSize > 0) {
//                sum = 1;
//            }
//            page.setPages((int) ((page.getTotal() / pageSize) + sum));
//            return page;
//        }
//        int end = pageNumber * pageSize;
//        if (end > depositList.size()) {
//            end = depositList.size();
//        }
//
//        Collections.sort(depositList, DepositComparator.getInstance());
//        List<Consensus<Deposit>> sublist = depositList.subList(start, end);
//        page.setPageNumber(pageNumber);
//        page.setPageSize(pageSize);
//        page.setTotal(depositList.size());
//        int sum = 0;
//        if (page.getTotal() % pageSize > 0) {
//            sum = 1;
//        }
//        page.setPages((int) ((page.getTotal() / pageSize) + sum));
//        List<Map<String, Object>> resultList = new ArrayList<>();
//        for (Consensus<Deposit> cd : sublist) {
//            if (agent == null || !agent.getHexHash().equals(cd.getExtend().getAgentHash())) {
//                agent = this.consensusCacheManager.getAgentById(cd.getExtend().getAgentHash());
//            }
//            Map<String, Object> map = new HashMap<>();
//            map.put("agentId", cd.getExtend().getAgentHash());
//            map.put("agentName", agent.getExtend().getAgentName());
//            map.put("agentAddress", agent.getAddress());
//            map.put("txHash", cd.getExtend().getTxHash());
//            map.put("agentAddressAlias", null);
//            map.put("address", cd.getAddress());
//            map.put("status", cd.getExtend().getStatus());
//            map.put("depositTime", cd.getExtend().getStartTime());
//            map.put("amount", cd.getExtend().getDeposit().getValue());
//            resultList.add(map);
//        }
//        page.setList(resultList);
        return page;
    }

    @Override
    public Map<String, Object> getAgent(String agentAddress) {
//        Consensus<Agent> ca = this.consensusCacheManager.getAgentByAddress(agentAddress);
//        if (ca == null) {
//            return null;
//        }
        Map<String, Object> map = new HashMap<>();
//        map.put("agentId", ca.getHexHash());
//        map.put("agentName", ca.getExtend().getAgentName());
//        map.put("packingAddress", ca.getExtend().getPackingAddress());
//        map.put("agentAddress", ca.getAddress());
//        map.put("agentAddressAlias", null);
//        map.put("status", ca.getExtend().getStatus());
//        map.put("owndeposit", ca.getExtend().getDeposit().getValue());
//        map.put("commissionRate", ca.getExtend().getCommissionRate());
//        map.put("introduction", ca.getExtend().getIntroduction());
//        map.put("startTime", ca.getExtend().getStartTime());
//        map.put("creditRatio", ca.getExtend().getCreditVal());
//        map.put("reward", ledgerService.getAgentReward(ca.getAddress(), 1));
//
//        Map<String, Object> countMap = blockService.getSumTxCount(ca.getExtend().getPackingAddress(), 0, 0);
//        map.put("packedCount", countMap.get("blockCount"));
//        map.put("txCount", countMap.get("txCount"));
//        List<Consensus<Deposit>> deposits = this.consensusCacheManager.getDepositListByAgentId(ca.getHexHash(), NulsContext.getInstance().getBestHeight());
//        long totalDeposit = 0;
//        Set<String> memberSet = new HashSet<>();
//        for (Consensus<Deposit> cd : deposits) {
//            totalDeposit += cd.getExtend().getDeposit().getValue();
//            memberSet.add(cd.getAddress());
//        }
//        map.put("totalDeposit", totalDeposit);
//        map.put("memberCount", memberSet.size());
        return map;
    }

    private void filterAgentList(List<Consensus<Agent>> agentList, String agentAddress, String depositAddress, String keyword) {
        if (Address.validAddress(agentAddress)) {
            for (int i = agentList.size() - 1; i >= 0; i--) {
                Consensus<Agent> consensus = agentList.get(i);
                if (!consensus.getAddress().equals(agentAddress)) {
                    agentList.remove(i);
                }
            }
        }
//        if (Address.validAddress(depositAddress)) {
//            List<Consensus<Deposit>> depositList = this.consensusCacheManager.getDepositListByAddress(depositAddress);
//            Set<String> agentHashSet = new HashSet<>();
//            for (Consensus<Deposit> cd : depositList) {
//                agentHashSet.add(cd.getExtend().getAgentHash());
//            }
//            for (int i = agentList.size() - 1; i >= 0; i--) {
//                Consensus<Agent> consensus = agentList.get(i);
//                if (!agentHashSet.contains(consensus.getHexHash())) {
//                    agentList.remove(i);
//                }
//            }
//        }
        if (StringUtils.isNotBlank(keyword)) {
            for (int i = agentList.size() - 1; i >= 0; i--) {
                Consensus<Agent> consensus = agentList.get(i);
                boolean like = consensus.getAddress().indexOf(keyword) >= 0;
                like = like || consensus.getExtend().getAgentName().indexOf(keyword) >= 0;
                if (!like) {
                    agentList.remove(i);
                }
            }
        }
    }
}
