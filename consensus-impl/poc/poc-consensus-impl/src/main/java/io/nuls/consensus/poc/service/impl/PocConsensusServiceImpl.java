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
 */
package io.nuls.consensus.poc.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.manager.RoundManager;
import io.nuls.consensus.poc.protocol.constant.ConsensusStatusEnum;
import io.nuls.consensus.poc.protocol.constant.PocConsensusConstant;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.event.entity.JoinConsensusParam;
import io.nuls.consensus.poc.protocol.model.*;
import io.nuls.consensus.poc.protocol.model.block.BlockRoundData;
import io.nuls.consensus.poc.protocol.service.BlockService;
import io.nuls.consensus.poc.protocol.tx.CancelDepositTransaction;
import io.nuls.consensus.poc.protocol.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.poc.protocol.tx.RegisterAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.protocol.utils.AgentComparator;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.consensus.poc.protocol.utils.DepositComparator;
import io.nuls.consensus.poc.provider.ConsensusSystemProvider;
import io.nuls.consensus.poc.provider.QueueProvider;
import io.nuls.consensus.poc.scheduler.MainControlScheduler;
import io.nuls.consensus.poc.service.PocConsensusService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.calc.DoubleUtils;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.dao.PunishLogDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.params.OperationType;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.network.entity.Node;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.TransactionEvent;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by ln on 2018/4/13.
 */
public class PocConsensusServiceImpl implements PocConsensusService {

    private QueueProvider blockQueueProvider;
    private QueueProvider txQueueProvider;

    @Autowired
    private AgentDataService agentDataService;

    @Autowired
    private DepositDataService depositDataService;

    @Autowired
    private AccountService accountService;
    @Autowired
    private EventBroadcaster eventBroadcaster;
    @Autowired
    private LedgerService ledgerService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private PunishLogDataService punishLogDataService;


    private MainControlScheduler mainControlScheduler = MainControlScheduler.getInstance();

    @Override
    public boolean startup() {
        return mainControlScheduler.start();
    }

    @Override
    public boolean restart() {
        return mainControlScheduler.restart();
    }

    @Override
    public boolean shutdown() {
        return mainControlScheduler.stop();
    }

    @Override
    public boolean newTx(Transaction<? extends BaseNulsData> tx) {
        return txQueueProvider.put(tx, true);
    }

    @Override
    public boolean newBlock(Block block) {
        return blockQueueProvider.put(block, true);
    }

    @Override
    public boolean newBlock(Block block, Node node) {
        BlockContainer blockContainer = new BlockContainer();
        blockContainer.setBlock(block);
        blockContainer.setNode(node);
        return blockQueueProvider.put(blockContainer, true);
    }

    @Override
    public boolean addBlock(Block block) {
        return blockQueueProvider.put(block, false);
    }

    @Override
    public ConsensusStatus getConsensusStatus() {
        return ConsensusSystemProvider.getConsensusStatus();
    }

    @Override
    public List<BaseNulsData> getMemoryTxs() {
        List<BaseNulsData> list = new ArrayList<>();

        TxMemoryPool memoryPool = mainControlScheduler.getTxMemoryPool();

        list.addAll(memoryPool.getAll());
        list.addAll(memoryPool.getAll());

        return list;
    }

    @Override
    public Transaction getAndRemoveOfMemoryTxs(String hash) {
        return mainControlScheduler.getTxMemoryPool().getAndRemove(hash);
    }

    @Override
    public Transaction getTxFromMemory(String hash) {
        return mainControlScheduler.getTxMemoryPool().get(hash);
    }

    @Override
    public List<Transaction> getMemoryTxList() {
        return mainControlScheduler.getTxMemoryPool().getAll();
    }

    @Override
    public boolean rollbackBlock(Block block) throws NulsException {
        ChainManager chainManager = mainControlScheduler.getChainManager();
        if (chainManager == null) {
            return false;
        }

        ChainContainer masterChain = chainManager.getMasterChain();
        if (masterChain == null) {
            //throw new NulsException(ErrorCode.FAILED, "master chain has not init");
            return false;
        }
        boolean success = blockService.rollbackBlock(block);
        if (success) {
            success = masterChain.rollback(block);
        }
        NulsContext.getInstance().setBestBlock(masterChain.getBestBlock());
        return success;
    }

    @Override
    public void addProvider(QueueProvider blockQueueProvider, QueueProvider txQueueProvider) {
        this.blockQueueProvider = blockQueueProvider;
        this.txQueueProvider = txQueueProvider;
    }


    private Transaction registerAgent(Agent agent, Account account, String password) throws IOException, NulsException {
        TransactionEvent event = new TransactionEvent();
        CoinTransferData data = new CoinTransferData(OperationType.LOCK, this.ledgerService.getTxFee(TransactionConstant.TX_TYPE_REGISTER_AGENT));
        data.setTotalNa(agent.getDeposit());
        data.addFrom(account.getAddress().toString());
        Coin coin = new Coin(account.getAddress().toString(), agent.getDeposit(), 0, 0);
        data.addTo(coin);
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
        this.ledgerService.verifyTxWithException(tx, this.ledgerService.getWaitingTxList());
        event.setEventBody(tx);
        this.ledgerService.saveLocalTx(tx);
        boolean b = eventBroadcaster.publishToLocal(event);
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
        Coin coin = new Coin(account.getAddress().toString(), deposit.getDeposit(), 0, 0);
        data.addTo(coin);
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
        this.ledgerService.verifyTxWithException(tx, this.ledgerService.getWaitingTxList());
        event.setEventBody(tx);
        this.ledgerService.saveLocalTx(tx);
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
               Consensus<Agent> agentConsensus = this.getAgentByAddress(address);
               if(null==agentConsensus){
                   throw new NulsRuntimeException(ErrorCode.FAILED,"the agent is not exist!");
               }

                    joinTx = (AbstractCoinTransaction)this.ledgerService.getTx(NulsDigestData.fromDigestHex(agentConsensus.getExtend().getTxHash()));

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
            this.ledgerService.verifyTxWithException(tx, this.ledgerService.getWaitingTxList());
            event.setEventBody(tx);
            this.ledgerService.saveLocalTx(tx);
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
        this.ledgerService.saveLocalTx(tx);
        eventBroadcaster.publishToLocal(event);
        return tx;
    }

    @Override
    public Map<String, Object> getConsensusInfo() {
       // List<Consensus<Agent>> agentList = this.getEffectiveAgentList(null, NulsContext.getInstance().getBestHeight(), null);
       // List<Consensus<Deposit>> depositList = this.getEffectiveDepositList(null, null, NulsContext.getInstance().getBestHeight(), null);

        Map<String,Object> valueMap = agentDataService.getAgentCount(NulsContext.getInstance().getBestHeight());
        long sumDeposit = depositDataService.getSumDeposit(NulsContext.getInstance().getBestHeight());
//        long totalDeposit = 0L;
//        for (Consensus<Agent> agent : agentList) {
//            totalDeposit += agent.getExtend().getDeposit().getValue();
//        }
//        for (Consensus<Deposit> deposit : depositList) {
//            totalDeposit += deposit.getExtend().getDeposit().getValue();
//        }
        BigDecimal sumAgentDeposit = (BigDecimal) valueMap.get("deposit");
        //calc last 24h reward
        long rewardOfDay = ledgerService.getLastDayTimeReward();
//
        Map<String, Object> map = new HashMap<>();
        map.put("agentCount", ((Long)valueMap.get("getCount")).intValue());
        map.put("rewardOfDay", rewardOfDay);
        map.put("totalDeposit", sumDeposit + sumAgentDeposit.longValue());
        if (null == this.getCurrentRound()) {
            map.put("memberCount", 0);
        } else {
            map.put("memberCount", this.getCurrentRound().getMemberCount());
        }
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
        for (Account account : accountList) {
            Consensus<Agent> agent = this.getAgentByAddress(account.getAddress().toString());
            List<Consensus<Deposit>> depositList = new ArrayList<>();
            if (null != agent) {
                depositList = this.getEffectiveDepositList(null, agent.getHexHash(), NulsContext.getInstance().getBestHeight(), null);
            }
            for (Consensus<Deposit> cd : depositList) {
                totalDeposit += cd.getExtend().getDeposit().getValue();
                joinedAgent.add(cd.getExtend().getAgentHash());
            }
            if (null != agent) {
                agentCount++;
                totalDeposit += agent.getExtend().getDeposit().getValue();
            }
            reward += ledgerService.getAccountReward(account.getAddress().getBase58(), 0);
            rewardOfDay += ledgerService.getAccountReward(account.getAddress().getBase58(), lastDayTime);
            Balance balance = ledgerService.getBalance(account.getAddress().getBase58());
            if (balance != null) {
                usableBalance += balance.getUsable().getValue();
            }
        }
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
        Consensus<Agent> agent = this.getAgentByAddress(address);
        List<Consensus<Deposit>> depositList = new ArrayList<>();
        long totalDeposit = 0;
        depositList = this.getEffectiveDepositList(address, null, NulsContext.getInstance().getBestHeight(), null);
//        totalDeposit = agent.getExtend().getDeposit().getValue();
        Set<String> joinedAgent = new HashSet<>();
        for (Consensus<Deposit> cd : depositList) {
            totalDeposit += cd.getExtend().getDeposit().getValue();
            joinedAgent.add(cd.getExtend().getAgentHash());
        }
        Map<String, Object> map = new HashMap<>();
        if (null != agent) {
            map.put("agentCount", 1);
//            totalDeposit += agent.getExtend().getDeposit().getValue();
        } else {
            map.put("agentCount", 0);
        }
        map.put("totalDeposit", totalDeposit);
        long lastDayTime = TimeService.currentTimeMillis() - DateUtil.DATE_TIME;
        long reward = ledgerService.getAccountReward(address, 0);
        long rewardOfDay = ledgerService.getAccountReward(address, lastDayTime);
        Balance balance = ledgerService.getBalance(address);
        map.put("reward", reward);
        map.put("joinAgentCount", joinedAgent.size());
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
        List<Consensus<Agent>> agentList = this.getEffectiveAgentList(agentAddress, NulsContext.getInstance().getBestHeight(), null);
//        agentList.addAll(this.consensusCacheManager.getUnconfirmedAgentList());
        filterAgentList(agentList, depositAddress, keyword);
//
        Page<Map<String, Object>> page = new Page<>();
        int start = pageNumber * pageSize - pageSize;

        if (agentList.isEmpty() || start >= agentList.size()) {
            if (StringUtils.isNotBlank(depositAddress)) {
                Consensus<Agent> ca = this.getAgentByAddress(depositAddress);
                if (null != ca) {
                    agentList.add(0, ca);
                }
            }
            fillConsensusInfo(agentList);
            page.setPageNumber(pageNumber);
            page.setPageSize(pageSize);
            page.setTotal(agentList.size());
            int sum = 0;
            if (page.getTotal() % pageSize > 0) {
                sum = 1;
            }
            page.setList(transList(agentList));
            page.setPages((int) ((page.getTotal() / pageSize) + sum));
            return page;
        }

        int end = pageNumber * pageSize;
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

        if (StringUtils.isNotBlank(depositAddress)) {
            boolean b = true;
            for (int i = 0; i < agentList.size(); i++) {
                Consensus<Agent> ca = agentList.get(i);
                if (ca.getAddress().equals(depositAddress)) {
                    agentList.remove(i);
                    agentList.add(0, ca);
                    b = false;
                    break;
                }
            }
            if (b) {
                Consensus<Agent> ca = this.getAgentByAddress(depositAddress);
                if (null != ca) {
                    agentList.add(0, ca);
                }
            }
        }
        List<Consensus<Agent>> sublist = agentList.subList(start, end);
        fillConsensusInfo(sublist);
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        page.setTotal(agentList.size());
        int sum = 0;
        if (page.getTotal() % pageSize > 0) {
            sum = 1;
        }
        page.setPages((int) ((page.getTotal() / pageSize) + sum));
        List<Map<String, Object>> resultList = transList(sublist);
        page.setList(resultList);
        return page;
    }

    private void fillConsensusInfo(List<Consensus<Agent>> list) {
        MeetingRound round = this.getCurrentRound();
        for (Consensus<Agent> agent : list) {
            fillConsensusInfo(agent, round);
        }

    }

    private void fillConsensusInfo(Consensus<Agent> agent, MeetingRound round) {
        MeetingMember member = round.getMember(agent.getExtend().getPackingAddress());
        if (null == member) {
            agent.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
            agent.getExtend().setCreditVal(calcCreditVal(agent));
            List<Consensus<Deposit>> depositList = this.getEffectiveDepositList(null, agent.getHexHash(), NulsContext.getInstance().getBestHeight(), null);
            long totalDeposit = 0L;
            for (Consensus<Deposit> cd : depositList) {
                totalDeposit = cd.getExtend().getDeposit().getValue();
            }
            agent.getExtend().setTotalDeposit(totalDeposit);
        } else {
            agent.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
            agent.getExtend().setCreditVal(member.getRealCreditVal());
            agent.getExtend().setTotalDeposit(member.getTotalDeposit().getValue());
        }
    }

    private double calcCreditVal(Consensus<Agent> agent) {
        long count = this.punishLogDataService.getCountByType(agent.getAddress(), PunishType.RED.getCode());
        if (count > 0) {
            return -1;
        }

        BlockHeader blockHeader = NulsContext.getInstance().getBestBlock().getHeader();
        BlockRoundData roundData = new BlockRoundData(blockHeader.getExtend());

        long roundStart = roundData.getRoundIndex() - PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT;
        if (roundStart < 0) {
            roundStart = 0;
        }
        long blockCount = this.blockService.getPackingCount(agent.getExtend().getPackingAddress(), roundStart, roundData.getRoundIndex() - 1);
        long sumRoundVal =
                punishLogDataService.getCountByType(agent.getAddress(), PunishType.YELLOW.getCode(), roundStart, roundData.getRoundIndex() - 1);

        double ability = DoubleUtils.div(blockCount, PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT);

        double penalty = DoubleUtils.div(DoubleUtils.mul(PocConsensusConstant.CREDIT_MAGIC_NUM, sumRoundVal),
                DoubleUtils.mul(PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT, PocConsensusConstant.RANGE_OF_CAPACITY_COEFFICIENT));
        return DoubleUtils.round(DoubleUtils.sub(ability, penalty), 4);
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
            List<Consensus<Deposit>> deposits = this.getEffectiveDepositList(null, ca.getHexHash(), NulsContext.getInstance().getBestHeight(), null);
            long totalDeposit = 0;
            Set<String> memberSet = new HashSet<>();
            for (Consensus<Deposit> cd : deposits) {
                totalDeposit += cd.getExtend().getDeposit().getValue();
                memberSet.add(cd.getAddress());
            }
            map.put("totalDeposit", totalDeposit);
            map.put("memberCount", memberSet.size());
            resultList.add(map);
        }
        return resultList;
    }


    @Override
    public Page<Map<String, Object>> getDepositList(String address, String agentAddress, Integer pageNumber, Integer pageSize) {
        String agentId = null;
        if ((null != address && !Address.validAddress(address)) || (null != agentAddress && !Address.validAddress(agentAddress))) {
            throw new NulsRuntimeException(ErrorCode.PARAMETER_ERROR);
        }
        if (null != agentAddress) {
            Consensus<Agent> ca = this.getAgentByAddress(agentAddress);
            if (null != ca) {
                agentId = ca.getHexHash();
            }
        }
        List<Consensus<Deposit>> depositList = this.getEffectiveDepositList(address, agentId, NulsContext.getInstance().getBestHeight(), null);
        boolean isAddress = Address.validAddress(address);
        Consensus<Agent> agent = null;
        if (Address.validAddress(agentAddress)) {
            agent = this.getAgentByAddress(agentAddress);
            if (null == agent) {
                depositList.clear();
            }
        }
        for (int i = depositList.size() - 1; i >= 0; i--) {
            Consensus<Deposit> cd = depositList.get(i);
            if (isAddress && !cd.getAddress().equals(address)) {
                depositList.remove(i);
            } else if (null != agent && !cd.getExtend().getAgentHash().equals(agent.getHexHash())) {
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
        int end = pageNumber * pageSize;
        if (end > depositList.size()) {
            end = depositList.size();
        }

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
        MeetingRound round = this.getCurrentRound();


        for (Consensus<Deposit> cd : sublist) {
            if (agent == null || !agent.getHexHash().equals(cd.getExtend().getAgentHash())) {
                agent = this.getAgentById(cd.getExtend().getAgentHash());
            }
            Map<String, Object> map = new HashMap<>();
            MeetingMember member = round.getMember(agent.getExtend().getPackingAddress());
            if (null == member) {
                agent.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
            } else {
                agent.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
            }
            map.put("agentId", cd.getExtend().getAgentHash());
            map.put("agentName", agent.getExtend().getAgentName());
            map.put("agentAddress", agent.getAddress());
            map.put("txHash", cd.getExtend().getTxHash());
            map.put("agentAddressAlias", null);
            map.put("address", cd.getAddress());
            map.put("status", agent.getExtend().getStatus());
            map.put("depositTime", cd.getExtend().getStartTime());
            map.put("amount", cd.getExtend().getDeposit().getValue());
            resultList.add(map);
        }
        page.setList(resultList);
        return page;
    }


    @Override
    public Map<String, Object> getAgent(String agentAddress) {
        Consensus<Agent> ca = this.getAgentByAddress(agentAddress);
        if (ca == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();

        this.fillConsensusInfo(ca, this.getCurrentRound());

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
        map.put("reward", ledgerService.getAgentReward(ca.getAddress(), 1));

        Map<String, Object> countMap = blockService.getSumTxCount(ca.getExtend().getPackingAddress(), 0, 0);
        map.put("packedCount", countMap.get("blockCount"));
        map.put("txCount", countMap.get("txCount"));
        List<Consensus<Deposit>> deposits = this.getEffectiveDepositList(null, ca.getHexHash(), NulsContext.getInstance().getBestHeight(), null);
        long totalDeposit = 0;
        Set<String> memberSet = new HashSet<>();
        for (Consensus<Deposit> cd : deposits) {
            totalDeposit += cd.getExtend().getDeposit().getValue();
            memberSet.add(cd.getAddress());
        }
        map.put("totalDeposit", totalDeposit);
        map.put("memberCount", memberSet.size());
        return map;
    }

    @Override
    public MeetingRound getCurrentRound() {
        return mainControlScheduler.getChainManager().getMasterChain().getCurrentRound();
    }

    private void filterAgentList(List<Consensus<Agent>> agentList, String depositAddress, String keyword) {
        if (Address.validAddress(depositAddress)) {
            List<Consensus<Deposit>> depositList = this.getEffectiveDepositList(depositAddress, null, NulsContext.getInstance().getBestHeight(), null);
            Set<String> agentHashSet = new HashSet<>();
            for (Consensus<Deposit> cd : depositList) {
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
    }


    /**
     * for client Customized
     */
    @Override
    public List<Consensus<Agent>> getAllAgentList() {
        List<AgentPo> polist = this.agentDataService.getAllList();
        List<Consensus<Agent>> agentList = new ArrayList<>();
        for (AgentPo po : polist) {
            agentList.add(ConsensusTool.fromPojo(po));
        }
        return agentList;
    }

    @Override
    public List<Consensus<Agent>> getEffectiveAgentList(String address, long height, Integer status) {
        List<AgentPo> polist = this.agentDataService.getEffectiveList(address, height, status);
        List<Consensus<Agent>> agentList = new ArrayList<>();
        for (AgentPo po : polist) {
            agentList.add(ConsensusTool.fromPojo(po));
        }
        return agentList;
    }

    @Override
    public List<Consensus<Deposit>> getAllDepositList() {
        List<DepositPo> poList = this.depositDataService.getAllList();
        List<Consensus<Deposit>> depositList = new ArrayList<>();
        for (DepositPo po : poList) {
            depositList.add(ConsensusTool.fromPojo(po));
        }
        return depositList;
    }

    @Override
    public List<Consensus<Deposit>> getEffectiveDepositList(String address, String agentId, long height, Integer status) {
        List<DepositPo> poList = this.depositDataService.getEffectiveList(address, height, agentId, status);
        List<Consensus<Deposit>> depositList = new ArrayList<>();
        for (DepositPo po : poList) {
            depositList.add(ConsensusTool.fromPojo(po));
        }
        return depositList;
    }

    @Override
    public Consensus<Agent> getAgentByAddress(String address) {
        List<Consensus<Agent>> agentList = this.getEffectiveAgentList(address, NulsContext.getInstance().getBestHeight(), null);
        if (agentList.isEmpty()) {
            return null;
        }
        if (agentList.size() > 1) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "the address has agents more than one!");
        }
        return agentList.get(0);
    }


    private Consensus<Agent> getAgentById(String agentHash) {
        AgentPo po = this.agentDataService.get(agentHash);
        return ConsensusTool.fromPojo(po);
    }
}
