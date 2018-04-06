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

import io.nuls.cache.util.CacheMap;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Deposit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ConsensusCacheManager_new {

    /**
     * the title of the cache(ConsensusStatusInfo,single)
     */
    private static final String AGENT_LIST = "agent-list";
    private static final String DEPOSIT_LIST = "delegate-list";

    private static final ConsensusCacheManager_new INSTANCE = new ConsensusCacheManager_new();

    private CacheMap<String, Consensus<Agent>> agentCache = new CacheMap<>(AGENT_LIST, 512);
    private CacheMap<String, Consensus<Deposit>> depositCache = new CacheMap<>(DEPOSIT_LIST, 1024);

    private ConsensusCacheManager_new() {
    }

    public static ConsensusCacheManager_new getInstance() {
        return INSTANCE;
    }

    public List<Consensus<Agent>> getCachedAgentList() {
        return this.agentCache.values();
    }

    public List<Consensus<Agent>> getCachedAgentList(ConsensusStatusEnum status) {
        List<Consensus<Agent>> agentList = new ArrayList<>();
        agentList.addAll(this.agentCache.values());
        for (int i = agentList.size() - 1; i >= 0; i--) {
            Consensus<Agent> ca = agentList.get(i);
            if (status.getCode() != ca.getExtend().getStatus()) {
                agentList.remove(i);
            }
        }
        return agentList;
    }

    public void cacheAgent(Consensus<Agent> ca) {
        this.agentCache.put(ca.getHexHash(), ca);
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
        Consensus<Agent> ca = this.agentCache.get(agentHash);
        return ca;
    }

    public void delAgent(String address) {
        this.agentCache.remove(address);
    }

    public void changeAgentStatusByHash(String agentHash, ConsensusStatusEnum statusEnum) {
        Consensus<Agent> ca = getCachedAgentByHash(agentHash);
        if (statusEnum.getCode() == ca.getExtend().getStatus()) {
            return;
        }
        ca.getExtend().setStatus(statusEnum.getCode());
        this.cacheAgent(ca);
    }

    public void clear() {
        this.agentCache.clear();
        this.depositCache.clear();
    }

    public void cacheDeposit(Consensus<Deposit> cd) {
        this.depositCache.put(cd.getHexHash(), cd);
    }

    public Consensus<Deposit> getCachedDeposit(String id) {
        Consensus<Deposit> cd = depositCache.get(id);
        return cd;
    }

    public List<Consensus<Deposit>> getCachedDepositList(ConsensusStatusEnum status) {
        List<Consensus<Deposit>> list = new ArrayList<>(depositCache.values());
        for (int i = list.size() - 1; i >= 0; i--) {
            Consensus<Deposit> cd = list.get(i);
            if (status.getCode() != cd.getExtend().getStatus()) {
                list.remove(i);
            }
        }
        return list;
    }

    public void delDeposit(String id) {
        depositCache.remove(id);
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
        List<Consensus<Deposit>> depositList = agentHashFilter(depositCache.values(), agentHash);
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
        List<Consensus<Deposit>> alllist = getCachedDepositListByAgentHash(agentHash);
        for (Consensus<Deposit> cd : alllist) {
            cd.getExtend().setStatus(status.getCode());
            depositCache.put(cd.getHexHash(), cd);
        }
    }

    public void delDepositByAgentHash(String agentHash) {
        List<Consensus<Deposit>> alllist = getCachedDepositListByAgentHash(agentHash);
        for (Consensus<Deposit> cd : alllist) {
            depositCache.remove(cd.getHexHash());
        }
    }

    public List<Consensus<Deposit>> getCachedDepositList() {
        Set<Consensus<Deposit>> allSet = new HashSet<>();
        allSet.addAll(depositCache.values());
        return new ArrayList<>(allSet);
    }

    public List<Consensus<Deposit>> getCachedDepositListByAddress(String address) {
        Set<Consensus<Deposit>> allSet = new HashSet<>();
        allSet.addAll(depositCache.values());
        List<Consensus<Deposit>> list = new ArrayList<>(allSet);
        for (int i = list.size() - 1; i >= 0; i--) {
            Consensus<Deposit> cd = list.get(i);
            if (!cd.getAddress().equals(address)) {
                list.remove(i);
            }
        }
        return list;
    }

    public Set<String> agentKeySet(){
        return agentCache.keySet();
    }
    public Set<String> depositKeySet(){
        return depositCache.keySet();
    }

    public void removeAgent(String key) {
        this.agentCache.remove(key);
    }
    public void removeDeposit(String key) {
        this.depositCache.remove(key);
    }
}
