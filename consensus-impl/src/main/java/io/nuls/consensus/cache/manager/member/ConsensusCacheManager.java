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

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.util.CacheMap;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.DelegateAccountDataService;
import io.nuls.db.dao.DelegateDataService;
import io.nuls.db.entity.DelegateAccountPo;
import io.nuls.db.entity.DelegatePo;

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
    private static final String IN_DELEGATE_LIST = "in-delegate-list";
    private static final String OUT_DELEGATE_LIST = "wait-delegate-list";

    private static final ConsensusCacheManager INSTANCE = new ConsensusCacheManager();

    private DelegateDataService delegateDao = NulsContext.getInstance().getService(DelegateDataService.class);
    private DelegateAccountDataService delegateAccountDao = NulsContext.getInstance().getService(DelegateAccountDataService.class);
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private CacheMap<String, Consensus<Agent>> inAgentCache = new CacheMap<>(IN_AGENT_LIST, 512);
    private CacheMap<String, Consensus<Agent>> outAgentCache = new CacheMap<>(OUT_AGENT_LIST, 512);
    private CacheMap<String, Consensus<Delegate>> inDelegateCache = new CacheMap<>(IN_DELEGATE_LIST, 1024);
    private CacheMap<String, Consensus<Delegate>> outDelegateCache = new CacheMap<>(OUT_DELEGATE_LIST, 1024);
    private CacheMap<String, ConsensusStatusInfo> consensusStatusCache = new CacheMap<>(CACHE_CONSENSUS_STATUS_INFO, 128);

    private ConsensusCacheManager() {
    }

    public static ConsensusCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        Account self = accountService.getDefaultAccount();
        List<DelegatePo> delegatePoList = this.delegateDao.getList();
        List<DelegateAccountPo> delegateAccountPoList = this.delegateAccountDao.getList();
        Consensus mine = null;
        for (DelegateAccountPo po : delegateAccountPoList) {
            Consensus<Agent> ca = ConsensusTool.fromPojo(po);
            this.cacheAgent(ca);
            if (null != self && ca.getAddress().equals(self.getAddress().toString())) {
                mine = ca;
            }
        }
        for (DelegatePo dpo : delegatePoList) {
            Consensus<Delegate> cd = ConsensusTool.fromPojo(dpo);
            this.cacheDelegate(cd);
            if (null != self && cd.getAddress().equals(self.getAddress().toString())) {
                mine = cd;
            }
        }
        if (null == self) {
            return;
        }
        ConsensusStatusInfo info = new ConsensusStatusInfo();
        info.setAddress(self.getAddress().getBase58());
        if (null == mine) {
            info.setStatus(ConsensusStatusEnum.NOT_IN.getCode());
        } else if (mine.getExtend() instanceof Agent) {
            info.setStartTime(((Agent) mine.getExtend()).getStartTime());
            info.setStatus(((Agent) mine.getExtend()).getStatus());
        } else if (mine.getExtend() instanceof Delegate) {
            info.setStartTime(((Delegate) mine.getExtend()).getStartTime());
            info.setStatus(((Delegate) mine.getExtend()).getStatus());
        }
        this.updateConsensusStatusInfo(info);
    }

    public ConsensusStatusInfo getConsensusStatusInfo(String address) {
        return consensusStatusCache.get(address);
    }

    public void updateConsensusStatusInfo(ConsensusStatusInfo info) {
        this.consensusStatusCache.put(info.getAddress(), info);
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
            this.inAgentCache.put(ca.getAddress(), ca);
            this.outAgentCache.remove(ca.getAddress());
        } else {
            this.outAgentCache.put(ca.getAddress(), ca);
            this.inAgentCache.remove(ca.getAddress());
        }
    }

    public Consensus<Agent> getCachedAgent(String address) {
        Consensus<Agent> ca = this.inAgentCache.get(address);
        if (ca == null) {
            ca = this.outAgentCache.get(address);
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

    public void changeAgentStatus(String address, ConsensusStatusEnum statusEnum) {
        Consensus<Agent> ca = getCachedAgent(address);
        if (statusEnum.getCode() == ca.getExtend().getStatus()) {
            return;
        }
        this.delAgent(address);
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

    public void cacheDelegate(Consensus<Delegate> cd) {
        if (cd.getExtend().getStatus() == ConsensusStatusEnum.IN.getCode()) {
            this.inDelegateCache.put(cd.getExtend().getHash(), cd);
            this.outDelegateCache.remove(cd.getExtend().getHash());
            return;
        }
        this.inDelegateCache.remove(cd.getExtend().getHash());
        this.outDelegateCache.put(cd.getExtend().getHash(), cd);
    }

    public Consensus<Delegate> getCachedDelegate(String id) {
        Consensus<Delegate> cd = inDelegateCache.get(id);
        if (null == cd) {
            cd = outDelegateCache.get(id);
        }
        return cd;
    }

    public List<Consensus<Delegate>> getCachedDelegateList(ConsensusStatusEnum status) {
        if (ConsensusStatusEnum.WAITING == status) {
            return outDelegateCache.values();
        }
        return inDelegateCache.values();
    }

    public void delDelegate(String id) {
        inDelegateCache.remove(id);
        outDelegateCache.remove(id);
    }

    public void changeDelegateStatus(String id, ConsensusStatusEnum statusEnum) {
        Consensus<Delegate> ca = getCachedDelegate(id);
        if (statusEnum.getCode() == ca.getExtend().getStatus()) {
            return;
        }
        this.delDelegate(id);
        ca.getExtend().setStatus(statusEnum.getCode());
        this.cacheDelegate(ca);
    }

    public List<Consensus<Delegate>> getCachedDelegateList(String agentAddress) {
        Consensus<Agent> ca = this.getCachedAgent(agentAddress);
        if (null == ca) {
            return null;
        }
        if (ca.getExtend().getStatus() == ConsensusStatusEnum.IN.getCode()) {
            return filter(inDelegateCache.values(), agentAddress);
        } else {
            return filter(outDelegateCache.values(), agentAddress);
        }
    }

    private List<Consensus<Delegate>> filter(List<Consensus<Delegate>> allList, String agentAddress) {
        List<Consensus<Delegate>> list = new ArrayList<>();
        for (Consensus<Delegate> cd : allList) {
            if (cd.getExtend().getDelegateAddress().equals(agentAddress)) {
                list.add(cd);
            }
        }
        return list;
    }

    public void changeDelegateStatusByAgent(String address, ConsensusStatusEnum status) {
        if (status == ConsensusStatusEnum.IN) {
            List<Consensus<Delegate>> allOutList = new ArrayList<>(outDelegateCache.values());
            for (Consensus<Delegate> cd : allOutList) {
                if (cd.getExtend().getDelegateAddress().equals(address)) {
                    outDelegateCache.remove(cd.getExtend().getHash());
                    inDelegateCache.put(cd.getExtend().getHash(), cd);
                }
            }
            return;
        }
        List<Consensus<Delegate>> allOutList = new ArrayList<>(inDelegateCache.values());
        for (Consensus<Delegate> cd : allOutList) {
            if (cd.getExtend().getDelegateAddress().equals(address)) {
                inDelegateCache.remove(cd.getExtend().getHash());
                outDelegateCache.put(cd.getExtend().getHash(), cd);
            }
        }
    }

    public void delDelegateByAgent(String address) {
        List<Consensus<Delegate>> allList = new ArrayList<>(outDelegateCache.values());
        for (Consensus<Delegate> cd : allList) {
            if (cd.getExtend().getDelegateAddress().equals(address)) {
                outDelegateCache.remove(cd.getExtend().getHash());
            }
        }
        allList = new ArrayList<>(inDelegateCache.values());
        for (Consensus<Delegate> cd : allList) {
            if (cd.getExtend().getDelegateAddress().equals(address)) {
                inDelegateCache.remove(cd.getExtend().getHash());
            }
        }
    }

    public List<Consensus<Delegate>> getCachedDelegateList() {
        Set<Consensus<Delegate>> allSet = new HashSet<>(outDelegateCache.values());
        allSet.addAll(inDelegateCache.values());
        return new ArrayList<>(allSet);
    }
}
