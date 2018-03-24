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
package io.nuls.consensus.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.*;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.entity.params.JoinConsensusParam;
import io.nuls.consensus.entity.tx.PocExitConsensusTransaction;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.consensus.utils.AgentComparator;
import io.nuls.consensus.utils.DepositComparator;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.params.OperationType;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.ledger.service.intf.LedgerService;

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

    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();

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
        con.setExtend(agent);
        tx.setTxData(con);
        tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
        tx.setScriptSig(accountService.createP2PKHScriptSigFromDigest(tx.getHash(), account, password).serialize());
        tx.verifyWithException();
        event.setEventBody(tx);
        List<String> nodeList = eventBroadcaster.broadcastHashAndCache(event, true);
        if (null == nodeList || nodeList.isEmpty()) {
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
        List<String> nodeList = eventBroadcaster.broadcastAndCache(event, true);
        if (null == nodeList || nodeList.isEmpty()) {
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
        CoinTransferData coinTransferData = new CoinTransferData(OperationType.UNLOCK, this.ledgerService.getTxFee(TransactionConstant.TX_TYPE_EXIT_CONSENSUS));
        coinTransferData.setTotalNa(Na.ZERO);
        PocExitConsensusTransaction tx = new PocExitConsensusTransaction(coinTransferData, password);
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
        return tx;
    }

    @Override
    public List<Consensus> getConsensusAccountList() {
        List<Consensus<Agent>> list = consensusCacheManager.getCachedAgentList(ConsensusStatusEnum.IN);
        List<Consensus> resultList = new ArrayList<>(list);
        resultList.addAll(consensusCacheManager.getCachedDepositList(ConsensusStatusEnum.IN));
        return resultList;
    }

    @Override
    public Map<String, Object> getConsensusInfo() {
        List<Consensus<Agent>> agentList = consensusCacheManager.getCachedAgentList(ConsensusStatusEnum.IN);
        boolean hashAgent = agentList.size() > 0;
        List<Consensus<Deposit>> depositList = consensusCacheManager.getCachedDepositList(ConsensusStatusEnum.IN);
        long totalDeposit = 0L;
        Set<String> set = new HashSet<>();
        for (Consensus<Agent> agent : agentList) {
            totalDeposit += agent.getExtend().getDeposit().getValue();
            set.add(agent.getAddress());
        }
        for (Consensus<Deposit> deposit : depositList) {
            totalDeposit += deposit.getExtend().getDeposit().getValue();
            set.add(deposit.getAddress());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("agentCount", agentList.size());
        //todo 更改为真实计算
        long rewardOfDay = 0;
        if (hashAgent) {
            rewardOfDay = PocConsensusConstant.BLOCK_REWARD.getValue() * PocConsensusConstant.BLOCK_COUNT_OF_DAY;
        }
        map.put("rewardOfDay", rewardOfDay);
        map.put("totalDeposit", totalDeposit);
        map.put("memberCount", set.size());
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
        Map<String, Object> map = new HashMap<>();
        map.put("agentCount", 0);
        map.put("totalDeposit", 0);
        Set<String> joinedAgent = new HashSet<>();
        for (Account account : accountList) {
            Consensus<Agent> agent = this.consensusCacheManager.getCachedAgentByAddress(account.getAddress().toString());
            List<Consensus<Deposit>> depositList = this.consensusCacheManager.getCachedDepositListByAddress(account.getAddress().toString());
            long totalDeposit = 0;
            for (Consensus<Deposit> cd : depositList) {
                totalDeposit += cd.getExtend().getDeposit().getValue();
                joinedAgent.add(cd.getExtend().getAgentHash());
            }
            if (null != agent) {
                map.put("agentCount", 1 + (int) map.get("agentCount"));
                totalDeposit += agent.getExtend().getDeposit().getValue();
            }
            map.put("totalDeposit", totalDeposit + Long.parseLong("" + map.get("totalDeposit")));
        }
        map.put("joinAccountCount", joinedAgent.size());
        map.put("usableBalance", 2018);
        map.put("reward", 2018);
        map.put("rewardOfDay", 2018);
        return map;
    }

    private Map<String, Object> getConsensusInfoByAddress(String address) {
        if (!StringUtils.validAddress(address)) {
            return null;
        }
        Consensus<Agent> agent = this.consensusCacheManager.getCachedAgentByAddress(address);
        List<Consensus<Deposit>> depositList = this.consensusCacheManager.getCachedDepositListByAddress(address);
        long totalDeposit = 0;
        Set<String> joinedAgent = new HashSet<>();
        for (Consensus<Deposit> cd : depositList) {
            totalDeposit += cd.getExtend().getDeposit().getValue();
            joinedAgent.add(cd.getExtend().getAgentHash());
        }
        Map<String, Object> map = new HashMap<>();
        if (null != agent) {
            map.put("agentCount", 1);
            totalDeposit += agent.getExtend().getDeposit().getValue();
        } else {
            map.put("agentCount", 0);
        }
        map.put("totalDeposit", totalDeposit);
        map.put("reward", 2018);
        map.put("joinAccountCount", joinedAgent.size());
        map.put("usableBalance", 2018);
        map.put("rewardOfDay", 2018);
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
            agent.setSeed(params.isSeed());
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
    public ConsensusStatusInfo getConsensusStatus(String agentHash) {
        Consensus<Agent> ca = this.consensusCacheManager.getCachedAgentByHash(agentHash);
        if(null==ca){
            return null;
        }
        Account account = this.accountService.getAccount(ca.getAddress());
        ConsensusStatusInfo info = new ConsensusStatusInfo();
        info.setSeed(false);
        info.setStatus(ca.getExtend().getStatus());
        info.setAccount(account);
        return info;
    }

    @Override
    public Page<Map<String, Object>> getAgentList(String keyword, String depositAddress,String agentAddress, String sortType, Integer pageNumber, Integer pageSize) {
        List<Consensus<Agent>> agentList = this.consensusCacheManager.getCachedAgentList();
        Page<Map<String, Object>> page = new Page<>();
        int start = pageNumber * pageSize - pageSize;
        if (StringUtils.validAddress(agentAddress)) {
            for (int i = agentList.size() - 1; i >= 0; i--) {
                Consensus<Agent> consensus = agentList.get(i);
                if (!consensus.getAddress().equals(agentAddress)) {
                    agentList.remove(i);
                }
            }
        }
        if(StringUtils.validAddress(depositAddress)){
            List<Consensus<Deposit>> depositList = this.consensusCacheManager.getCachedDepositListByAddress(depositAddress);
            Set<String> agentHashSet = new HashSet<>();
            for(Consensus<Deposit> cd:depositList){
                agentHashSet.add(cd.getExtend().getAgentHash());
            }
            for (int i = agentList.size() - 1; i >= 0; i--) {
                Consensus<Agent> consensus = agentList.get(i);
                if (!agentHashSet.contains(consensus.getHexHash())) {
                    agentList.remove(i);
                }
            }
        }
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
        if (agentList.isEmpty() || start >= agentList.size()) {
            page.setPageNumber(pageNumber);
            page.setPageSize(pageSize);
            page.setTotal(agentList.size());
            int sum = 0;
            if (page.getTotal() % pageSize > 0) {
                sum = 1;
            }
            page.setPages((int) ((page.getTotal() / pageSize) + sum));
            return page;
        }
        int end = pageNumber * pageSize - 1;
        if (end > agentList.size()) {
            end = agentList.size();
        }

        int type = AgentComparator.COMMISSION_RATE;
        if ("owndeposit".equals(sortType)) {
            type = AgentComparator.DEPOSIT;
        } else if ("commissionRate".equals(sortType)) {
            type = AgentComparator.COMMISSION_RATE;
        } else if ("creditRatio".equals(sortType)) {
            type = AgentComparator.CREDIT_VALUE;
        } else if ("totalDeposit".equals(sortType)) {
            type = AgentComparator.DEPOSITABLE;
        }
        Collections.sort(agentList, AgentComparator.getInstance(type));
        List<Consensus<Agent>> sublist = agentList.subList(start, end);
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        page.setTotal(agentList.size());
        int sum = 0;
        if (page.getTotal() % pageSize > 0) {
            sum = 1;
        }
        page.setPages((int) ((page.getTotal() / pageSize) + sum));
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Consensus<Agent> ca : sublist) {
            Map<String, Object> map = new HashMap<>();
            map.put("agentId", ca.getHexHash());
            map.put("agentName", ca.getExtend().getAgentName());
            map.put("packingAddress",ca.getExtend().getPackingAddress());
            map.put("agentAddress", ca.getAddress());
            map.put("agentAddressAlias", null);
            map.put("status", ca.getExtend().getStatus());
            map.put("owndeposit", ca.getExtend().getDeposit().getValue());
            map.put("commissionRate", ca.getExtend().getCommissionRate());
            map.put("introduction", ca.getExtend().getIntroduction());
            map.put("startTime", ca.getExtend().getStartTime());
            map.put("creditRatio", 1);
            map.put("reward", 2018);
            map.put("packedCount", 2018);
            List<Consensus<Deposit>> deposits = this.consensusCacheManager.getCachedDepositListByAgentHash(ca.getHexHash());
            long totalDeposit = 0;
            Set<String> memberSet = new HashSet<>();
            for(Consensus<Deposit> cd:deposits){
                totalDeposit+=cd.getExtend().getDeposit().getValue();
                memberSet.add(cd.getAddress());
            }
            map.put("totalDeposit", totalDeposit);
            map.put("memberCount", memberSet.size());
            resultList.add(map);
        }
        page.setList(resultList);
        return page;
    }

    @Override
    public Page<Map<String, Object>> getDepositList(String address, String agentAddress, Integer pageNumber, Integer pageSize) {
        List<Consensus<Deposit>> depositList = this.consensusCacheManager.getCachedDepositList();
        boolean isAddress = StringUtils.validAddress(address);
        Consensus<Agent> agent = null;
        if (StringUtils.validAddress(agentAddress)) {
            agent = this.consensusCacheManager.getCachedAgentByAddress(agentAddress);
            if (null == agent) {
                depositList.clear();
            }
        }
        for (int i = depositList.size() - 1; i >= 0; i--) {
            Consensus<Deposit> cd = depositList.get(i);
            if (isAddress && !cd.getAddress().equals(address)) {
                depositList.remove(i);
            }
            if (null != agent && !cd.getExtend().getAgentHash().equals(agent.getHexHash())) {
                depositList.remove(i);
            }
        }
        Page<Map<String, Object>> page = new Page<>();
        int start = pageNumber * pageSize - pageSize;
        if (depositList.isEmpty() || start >= depositList.size()) {
            page.setPageNumber(pageNumber);
            page.setPageSize(pageSize);
            page.setTotal(depositList.size());
            int sum = 0;
            if (page.getTotal() % pageSize > 0) {
                sum = 1;
            }
            page.setPages((int) ((page.getTotal() / pageSize) + sum));
            return page;
        }
        int end = pageNumber * pageSize - 1;
        if (end > depositList.size()) {
            end = depositList.size();
        }

        //todo 排序：委托时间倒序
        Collections.sort(depositList, DepositComparator.getInstance());
        List<Consensus<Deposit>> sublist = depositList.subList(start, end);
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        page.setTotal(depositList.size());
        int sum = 0;
        if (page.getTotal() % pageSize > 0) {
            sum = 1;
        }
        page.setPages((int) ((page.getTotal() / pageSize) + sum));
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Consensus<Deposit> cd : sublist) {
            if (agent == null || !agent.getHexHash().equals(cd.getExtend().getAgentHash())) {
                agent = this.consensusCacheManager.getCachedAgentByHash(cd.getExtend().getAgentHash());
            }
            Map<String, Object> map = new HashMap<>();
            map.put("agentId", cd.getExtend().getAgentHash());
            map.put("agentName", agent.getExtend().getAgentName());
            map.put("agentAddress", agent.getAddress());
            map.put("agentAddressAlias", null);
            map.put("address", cd.getAddress());
            map.put("status", cd.getExtend().getStatus());
            map.put("depositTime", cd.getExtend().getStartTime());
            map.put("amount", cd.getExtend().getDeposit().getValue());
            resultList.add(map);
        }
        page.setList(resultList);
        return page;
    }

    @Override
    public Map<String, Object> getAgent(String agentAddress) {
        Consensus<Agent> ca = this.consensusCacheManager.getCachedAgentByAddress(agentAddress);
        if (ca == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("agentId", ca.getHexHash());
        map.put("agentName", ca.getExtend().getAgentName());
        map.put("packingAddress",ca.getExtend().getPackingAddress());
        map.put("agentAddress", ca.getAddress());
        map.put("agentAddressAlias", null);
        map.put("status", ca.getExtend().getStatus());
        map.put("owndeposit", ca.getExtend().getDeposit().getValue());
        map.put("commissionRate", ca.getExtend().getCommissionRate());
        map.put("introduction", ca.getExtend().getIntroduction());
        map.put("startTime", ca.getExtend().getStartTime());
        map.put("creditRatio", 1);
        map.put("reward", 2018);
        map.put("packedCount", 2018);
        List<Consensus<Deposit>> deposits = this.consensusCacheManager.getCachedDepositListByAgentHash(ca.getHexHash());
        long totalDeposit = 0;
        Set<String> memberSet = new HashSet<>();
        for(Consensus<Deposit> cd:deposits){
            totalDeposit+=cd.getExtend().getDeposit().getValue();
            memberSet.add(cd.getAddress());
        }
        map.put("totalDeposit", totalDeposit);
        map.put("memberCount", memberSet.size());
        return map;
    }
}
