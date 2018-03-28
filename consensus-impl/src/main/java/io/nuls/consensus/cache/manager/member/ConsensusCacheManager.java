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
package io.nuls.consensus.cache.manager.member;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.util.CacheMap;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;

import java.util.ArrayList;
import java.util.HashSet;
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
    private static final String CACHE_CONSENSUS_STATUS_INFO = "consensus-status-info";
    private static final String IN_AGENT_LIST = "agent-list-in";
    private static final String OUT_AGENT_LIST = "agent-list-wait";
    private static final String IN_DEPOSIT_LIST = "in-delegate-list";
    private static final String OUT_DEPOSIT_LIST = "wait-delegate-list";

    private static final ConsensusCacheManager INSTANCE = new ConsensusCacheManager();

    private DepositDataService depositDao = NulsContext.getServiceBean(DepositDataService.class);
    private AgentDataService agentDao = NulsContext.getServiceBean(AgentDataService.class);
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);

    private CacheMap<String, Consensus<Agent>> inAgentCache = new CacheMap<>(IN_AGENT_LIST, 512);
    private CacheMap<String, Consensus<Agent>> outAgentCache = new CacheMap<>(OUT_AGENT_LIST, 512);
    private CacheMap<String, Consensus<Deposit>> inDelegateCache = new CacheMap<>(IN_DEPOSIT_LIST, 1024);
    private CacheMap<String, Consensus<Deposit>> outDelegateCache = new CacheMap<>(OUT_DEPOSIT_LIST, 1024);
    private CacheMap<String, ConsensusStatusInfo> consensusStatusCache = new CacheMap<>(CACHE_CONSENSUS_STATUS_INFO, 128);

    private ConsensusCacheManager() {
    }

    public static ConsensusCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        Account self = accountService.getDefaultAccount();
        List<DepositPo> depositPoList = this.depositDao.getList();
        List<AgentPo> agentPoList = this.agentDao.getList();
        Consensus mine = null;
        for (AgentPo po : agentPoList) {
            Consensus<Agent> ca = ConsensusTool.fromPojo(po);
            this.cacheAgent(ca);
            if (null != self && ca.getAddress().equals(self.getAddress().toString())) {
                mine = ca;
            }
        }
        for (DepositPo dpo : depositPoList) {
            Consensus<Deposit> cd = ConsensusTool.fromPojo(dpo);
            this.cacheDeposit(cd);
            if (null != self && null == mine && cd.getAddress().equals(self.getAddress().toString())) {
                mine = cd;
            }
        }
        if (null == self) {
            return;
        }
        ConsensusStatusInfo info = new ConsensusStatusInfo();
        info.setAccount(self);
        if (null == mine) {
            info.setStatus(ConsensusStatusEnum.NOT_IN.getCode());
        } else if (mine.getExtend() instanceof Agent) {
            info.setSeed(((Agent) mine.getExtend()).getSeed());
            info.setStatus(((Agent) mine.getExtend()).getStatus());
        }
        this.updateConsensusStatusInfo(info);
    }

    public ConsensusStatusInfo getConsensusStatusInfo(String address) {
        return consensusStatusCache.get(address);
    }

    public void updateConsensusStatusInfo(ConsensusStatusInfo info) {
        this.consensusStatusCache.put(info.getAccount().getAddress().toString(), info);
    }


    public List<Consensus<Agent>> getCachedAgentList(ConsensusStatusEnum status) {
        if (status == ConsensusStatusEnum.WAITING) {
            return this.outAgentCache.values();
        }
        return this.inAgentCache.values();
    }

    public List<Consensus<Agent>> getCachedAgentList() {
        List<Consensus<Agent>> agentList = new ArrayList<>();
        agentList.addAll(this.inAgentCache.values());
        agentList.addAll(this.outAgentCache.values());
        return agentList;
    }

    public void cacheAgent(Consensus<Agent> ca) {
        if (ca.getExtend().getStatus() == ConsensusStatusEnum.IN.getCode()) {
            this.inAgentCache.put(ca.getHexHash(), ca);
            this.outAgentCache.remove(ca.getHexHash());
        } else {
            this.outAgentCache.put(ca.getHexHash(), ca);
            this.inAgentCache.remove(ca.getHexHash());
        }
    }

    public Consensus<Agent> getCachedAgentByAddress(String agentAddress) {
        List<Consensus<Agent>> agentList = this.getCachedAgentList();
        for (Consensus<Agent> ca : agentList) {
            if (ca.getAddress().equals(agentAddress)) {
                return ca;
            }
        }
        return null;
    }

    public Consensus<Agent> getCachedAgentByHash(String agentHash) {
        Consensus<Agent> ca = this.inAgentCache.get(agentHash);
        if (ca == null) {
            ca = this.outAgentCache.get(agentHash);
        }
        return ca;
    }

    public int getAgentCount(ConsensusStatusEnum status) {
        if (status == ConsensusStatusEnum.WAITING) {
            return this.outAgentCache.size();
        }
        return this.inAgentCache.size();
    }

    public void delAgent(String address) {
        this.inAgentCache.remove(address);
        this.outAgentCache.remove(address);
    }

    public void changeAgentStatusByHash(String agentHash, ConsensusStatusEnum statusEnum) {
        Consensus<Agent> ca = getCachedAgentByHash(agentHash);
        if (statusEnum.getCode() == ca.getExtend().getStatus()) {
            return;
        }
        this.delAgent(agentHash);
        ca.getExtend().setStatus(statusEnum.getCode());
        this.cacheAgent(ca);
    }

    public void clear() {
        this.inAgentCache.clear();
        this.outAgentCache.clear();
        this.inDelegateCache.clear();
        this.outDelegateCache.clear();
        this.consensusStatusCache.clear();
    }

    public void cacheDeposit(Consensus<Deposit> cd) {
        if (cd.getExtend().getStatus() == ConsensusStatusEnum.IN.getCode()) {
            this.inDelegateCache.put(cd.getHexHash(), cd);
            this.outDelegateCache.remove(cd.getHexHash());
            return;
        }
        this.inDelegateCache.remove(cd.getHexHash());
        this.outDelegateCache.put(cd.getHexHash(), cd);
    }

    public Consensus<Deposit> getCachedDeposit(String id) {
        Consensus<Deposit> cd = inDelegateCache.get(id);
        if (null == cd) {
            cd = outDelegateCache.get(id);
        }
        return cd;
    }

    public List<Consensus<Deposit>> getCachedDepositList(ConsensusStatusEnum status) {
        if (ConsensusStatusEnum.WAITING == status) {
            return outDelegateCache.values();
        }
        return inDelegateCache.values();
    }

    public void delDeposit(String id) {
        inDelegateCache.remove(id);
        outDelegateCache.remove(id);
    }

    public void changeDepositStatus(String id, ConsensusStatusEnum statusEnum) {
        Consensus<Deposit> ca = getCachedDeposit(id);
        if (statusEnum.getCode() == ca.getExtend().getStatus()) {
            return;
        }
        this.delDeposit(id);
        ca.getExtend().setStatus(statusEnum.getCode());
        this.cacheDeposit(ca);
    }

    public List<Consensus<Deposit>> getCachedDepositListByAgentHash(String agentHash) {
        Consensus<Agent> ca = this.getCachedAgentByHash(agentHash);
        if (null == ca) {
            return null;
        }
        List<Consensus<Deposit>> depositList = agentHashFilter(inDelegateCache.values(), agentHash);
        depositList.addAll(agentHashFilter(outDelegateCache.values(), agentHash));
        return depositList;
    }

    private List<Consensus<Deposit>> agentHashFilter(List<Consensus<Deposit>> allList, String agentHash) {
        List<Consensus<Deposit>> list = new ArrayList<>();
        for (Consensus<Deposit> cd : allList) {
            if (cd.getExtend().getAgentHash().equals(agentHash)) {
                list.add(cd);
            }
        }
        return list;
    }

    public void changeDepositStatusByAgentHash(String agentHash, ConsensusStatusEnum status) {
        if (status == ConsensusStatusEnum.IN) {
            List<Consensus<Deposit>> allOutList = new ArrayList<>(outDelegateCache.values());
            for (Consensus<Deposit> cd : allOutList) {
                if (cd.getExtend().getAgentHash().equals(agentHash)) {
                    outDelegateCache.remove(cd.getHexHash());
                    inDelegateCache.put(cd.getHexHash(), cd);
                }
            }
            return;
        }
        List<Consensus<Deposit>> allOutList = new ArrayList<>(inDelegateCache.values());
        for (Consensus<Deposit> cd : allOutList) {
            if (cd.getExtend().getAgentHash().equals(agentHash)) {
                inDelegateCache.remove(cd.getHexHash());
                outDelegateCache.put(cd.getHexHash(), cd);
            }
        }
    }

    public void delDepositByAgentHash(String agentHash) {
        List<Consensus<Deposit>> allList = new ArrayList<>(outDelegateCache.values());
        for (Consensus<Deposit> cd : allList) {
            if (cd.getExtend().getAgentHash().equals(agentHash)) {
                outDelegateCache.remove(cd.getHexHash());
            }
        }
        allList = new ArrayList<>(inDelegateCache.values());
        for (Consensus<Deposit> cd : allList) {
            if (cd.getExtend().getAgentHash().equals(agentHash)) {
                inDelegateCache.remove(cd.getHexHash());
            }
        }
    }

    public List<Consensus<Deposit>> getCachedDepositList() {
        Set<Consensus<Deposit>> allSet = new HashSet<>(outDelegateCache.values());
        allSet.addAll(inDelegateCache.values());
        return new ArrayList<>(allSet);
    }

    public List<Consensus<Deposit>> getCachedDepositListByAddress(String address) {
        Set<Consensus<Deposit>> allSet = new HashSet<>(outDelegateCache.values());
        allSet.addAll(inDelegateCache.values());

        List<Consensus<Deposit>> list = new ArrayList<>(allSet);
        for (int i = list.size() - 1; i >= 0; i--) {
            Consensus<Deposit> cd = list.get(i);
            if (!cd.getAddress().equals(address)) {
                list.remove(i);
            }
        }
        return list;
    }
}
