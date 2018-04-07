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
package io.nuls.consensus.cache.manager.member;

import io.nuls.cache.util.CacheMap;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ConsensusCacheManager {

    /**
     * the title of the cache(ConsensusStatusInfo,single)
     */
    private static final String AGENT_LIST = "agent-list";
    private static final String DEPOSIT_LIST = "delegate-list";

    private static final String AGENT_DEPOSIT_MAPPING = "agent-deposit-mapping";
    private static final String ADDRESS_DEPOSIT_MAPPING = "address-deposit-mapping";
    private static final ConsensusCacheManager INSTANCE = new ConsensusCacheManager();

    private CacheMap<String, Consensus<Agent>> agentCache = new CacheMap<>(AGENT_LIST, 64);
    private CacheMap<String, Consensus<Deposit>> depositCache = new CacheMap<>(DEPOSIT_LIST, 512);
    private CacheMap<String, List<String>> agentDepositCache = new CacheMap<>(AGENT_DEPOSIT_MAPPING, 64);
    private CacheMap<String, List<String>> addressDepositCache = new CacheMap<>(ADDRESS_DEPOSIT_MAPPING, 64);

    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);

    private ConsensusCacheManager() {
    }


    public static ConsensusCacheManager getInstance() {
        return INSTANCE;
    }


    public void init() {
        List<AgentPo> agentPoList = agentDataService.getAllList(Long.MAX_VALUE);
        for (AgentPo po : agentPoList) {
            this.putAgent(ConsensusTool.fromPojo(po));
        }
        List<DepositPo> depositPoList = depositDataService.getAllList(Long.MAX_VALUE);
        for (DepositPo po : depositPoList) {
            this.putDeposit(ConsensusTool.fromPojo(po));
        }
    }


    public void putAgent(Consensus<Agent> ca) {
        this.agentCache.put(ca.getHexHash(), ca);
    }

    public void putDeposit(Consensus<Deposit> cd) {
        this.depositCache.put(cd.getHexHash(), cd);
        List<String> depositIdList = agentDepositCache.get(cd.getExtend().getAgentHash());
        if (null == depositIdList) {
            depositIdList = new ArrayList<>();
        }
        depositIdList.add(cd.getHexHash());
        agentDepositCache.put(cd.getExtend().getAgentHash(), depositIdList);

        List<String> depositIdList0 = addressDepositCache.get(cd.getAddress());
        if (null == depositIdList0) {
            depositIdList0 = new ArrayList<>();
        }
        depositIdList0.add(cd.getHexHash());
        addressDepositCache.put(cd.getAddress(), depositIdList0);
    }

    public Consensus<Agent> getAgentById(String agentHash) {
        Consensus<Agent> ca = this.agentCache.get(agentHash);

        return ca;
    }

    public Consensus<Deposit> getDepositById(String id) {
        Consensus<Deposit> cd = depositCache.get(id);
        return cd;
    }

    public void delAgent(String agentId, long delHeight) {
        Consensus<Agent> agent = getAgentById(agentId);
        if (agent == null) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "the agent is not exist!:" + agentId);
        }
        agent.setDelHeight(delHeight);
        this.putAgent(agent);
    }

    public void delDeposit(String depositId, long delHeight) {
        Consensus<Deposit> cd = getDepositById(depositId);
        if (cd == null) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "the deposit is not exist!:" + depositId);
        }
        cd.setDelHeight(delHeight);
        this.depositCache.put(depositId, cd);
    }

    public void clear() {
        this.agentCache.clear();
        this.depositCache.clear();
    }

    public void updateDepositStatusByAgentId(String agentId, long height, ConsensusStatusEnum status) {
        List<Consensus<Deposit>> depositList = this.getDepositListByAgentId(agentId, height);
        for (Consensus<Deposit> deposit : depositList) {
            deposit.getExtend().setStatus(status.getCode());
            this.depositCache.put(deposit.getHexHash(), deposit);
        }
    }

    public void updateDepositStatusById(String depositId, ConsensusStatusEnum status) {
        Consensus<Deposit> deposit = this.getDepositById(depositId);
        if (deposit == null) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "the deposit is not exist!:" + depositId);
        }
        deposit.getExtend().setStatus(status.getCode());
        this.depositCache.put(deposit.getHexHash(), deposit);
    }

    public void updateAgentStatusById(String agentId, ConsensusStatusEnum status) {
        Consensus<Agent> agent = this.getAgentById(agentId);
        if (agent == null) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "the agent is not exist!:" + agentId);
        }
        agent.getExtend().setStatus(status.getCode());
        this.putAgent(agent);
    }

    public void delDepositByAgentId(String agentId, long delHeight) {
        List<String> cdIdList = agentDepositCache.get(agentId);
        for (String cdId : cdIdList) {
            this.delDeposit(cdId, delHeight);
        }
    }

    public List<Consensus<Agent>> getAllAgentList() {
        return this.agentCache.values();
    }

    /**
     * get the list of agent that the agent created before [height] && undelete or deleted after the [height]
     *
     * @param height block height
     */
    public List<Consensus<Agent>> getAliveAgentList(long height) {
        List<Consensus<Agent>> agentList = new ArrayList<>(this.agentCache.values());
        List<Consensus<Agent>> resultList = new ArrayList<>();
        for (int i = agentList.size() - 1; i >= 0; i--) {
            Consensus<Agent> ca = agentList.get(i);
            if (ca.getDelHeight() != 0 && ca.getDelHeight() <= height) {
                continue;
            }
            if (ca.getExtend().getBlockHeight() >= height||ca.getExtend().getBlockHeight()<0) {
                continue;
            }
            resultList.add(ca);
        }
        return resultList;
    }

    public List<Consensus<Agent>> getAgentList(ConsensusStatusEnum status) {
        List<Consensus<Agent>> agentList = this.getAliveAgentList(NulsContext.getInstance().getBestHeight());
        List<Consensus<Agent>> resultList = new ArrayList<>();
        for (int i = agentList.size() - 1; i >= 0; i--) {
            Consensus<Agent> ca = agentList.get(i);
            if (status.getCode() != ca.getExtend().getStatus()) {
                continue;
            }
            resultList.add(ca);
        }
        return resultList;
    }

    public List<Consensus<Deposit>> getDepositList(ConsensusStatusEnum status) {
        List<Consensus<Deposit>> depositList = getAliveDepositList(NulsContext.getInstance().getBestHeight());
        List<Consensus<Deposit>> resultList = new ArrayList<>();
        for (int i = depositList.size() - 1; i >= 0; i--) {
            Consensus<Deposit> ca = depositList.get(i);
            if (status.getCode() != ca.getExtend().getStatus()) {
                continue;
            }
            resultList.add(ca);
        }
        return resultList;
    }

    public List<Consensus<Deposit>> getAllDepositList() {
        return new ArrayList<>(this.depositCache.values());
    }

    public List<Consensus<Deposit>> getAliveDepositList(long height) {
        List<Consensus<Deposit>> depositList = getAllDepositList();
        List<Consensus<Deposit>> resultList = new ArrayList<>();
        for (int i = depositList.size() - 1; i >= 0; i--) {
            Consensus<Deposit> cd = depositList.get(i);
            if (cd.getDelHeight() != 0 && cd.getDelHeight() <= height) {
                continue;
            }
            if (cd.getExtend().getBlockHeight() >= height||cd.getExtend().getBlockHeight()<0) {
                continue;
            }
            resultList.add(cd);
        }
        return resultList;
    }


    public List<Consensus<Deposit>> getDepositListByAddress(String address) {
        List<Consensus<Deposit>> depositList = getAliveDepositList(NulsContext.getInstance().getBestHeight());
        List<Consensus<Deposit>> resultList = new ArrayList<>();
        for (int i = depositList.size() - 1; i >= 0; i--) {
            Consensus<Deposit> ca = depositList.get(i);
            if (!ca.getAddress().equals(address)) {
                continue;
            }
            resultList.add(ca);
        }
        return resultList;
    }

    public List<Consensus<Deposit>> getDepositListByAgentId(String agentId, long height) {
        List<Consensus<Deposit>> depositList = getAliveDepositList(height);
        List<Consensus<Deposit>> resultList = new ArrayList<>();
        for (int i = depositList.size() - 1; i >= 0; i--) {
            Consensus<Deposit> ca = depositList.get(i);
            if (!ca.getExtend().getAgentHash().equals(agentId)) {
                continue;
            }
            resultList.add(ca);
        }
        return resultList;
    }


    public Set<String> agentKeySet() {
        return agentCache.keySet();
    }

    public Set<String> depositKeySet() {
        return depositCache.keySet();
    }

    public Consensus<Agent> getAgentByAddress(String address) {
        List<Consensus<Agent>> agentList = getAliveAgentList(NulsContext.getInstance().getBestHeight());
        for (int i = agentList.size() - 1; i >= 0; i--) {
            Consensus<Agent> ca = agentList.get(i);
            if (ca.getAddress().equals(address)) {
                return ca;
            }
        }
        return null;
    }
}
