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

import java.util.List;

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
    private static final String WAIT_AGENT_LIST = "agent-list-wait";
    private static final String IN_DELEGATE_LIST = "in-delegate-list";
    private static final String WAIT_DELEGATE_LIST = "wait-delegate-list";

    private static final ConsensusCacheManager INSTANCE = new ConsensusCacheManager();

    private DelegateDataService delegateDao = NulsContext.getInstance().getService(DelegateDataService.class);
    private DelegateAccountDataService delegateAccountDao = NulsContext.getInstance().getService(DelegateAccountDataService.class);
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private CacheMap<String, Consensus<Agent>> inAgentCache = new CacheMap<>(IN_AGENT_LIST,512);
    private CacheMap<String, Consensus<Agent>> waitAgentCache = new CacheMap<>(WAIT_AGENT_LIST,512);
    private CacheMap<String, Consensus<Delegate>> inDelegateCache = new CacheMap<>(IN_DELEGATE_LIST,1024);
    private CacheMap<String, Consensus<Delegate>> waitDelegateCache = new CacheMap<>(WAIT_DELEGATE_LIST,1024);
    private CacheMap<String, ConsensusStatusInfo> consensusStatusCache = new CacheMap<>(CACHE_CONSENSUS_STATUS_INFO,128);

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
            return this.waitAgentCache.values();
        }
        return this.inAgentCache.values();
    }

    public void cacheAgent(Consensus<Agent> ca) {
        if (ca.getExtend().getStatus() == ConsensusStatusEnum.IN.getCode()) {
            this.inAgentCache.put(ca.getAddress(), ca);
        } else if (ca.getExtend().getStatus() == ConsensusStatusEnum.WAITING.getCode()) {
            this.waitAgentCache.put(ca.getAddress(), ca);
        }
    }

    public Consensus<Agent> getCachedAgent(String address) {
        Consensus<Agent> ca = this.inAgentCache.get(address);
        if (ca == null) {
            ca = this.waitAgentCache.get(address);
        }
        return ca;
    }

    public int getAgentCount(ConsensusStatusEnum status) {
        if (status == ConsensusStatusEnum.WAITING) {
            return this.waitAgentCache.size();
        }
        return this.inAgentCache.size();
    }

    public void delAgent(String address) {
        this.inAgentCache.remove(address);
        this.waitAgentCache.remove(address);
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
        this.waitAgentCache.clear();
        this.inDelegateCache.clear();
        this.waitDelegateCache.clear();
        this.consensusStatusCache.clear();
    }

    public void cacheDelegate(Consensus<Delegate> cd) {
        if (cd.getExtend().getStatus() == ConsensusStatusEnum.IN.getCode()) {
            this.inDelegateCache.put(cd.getExtend().getId(), cd);
            return;
        }
        this.waitDelegateCache.put(cd.getExtend().getId(), cd);
    }

    public Consensus<Delegate> getCachedDelegate(String id) {
        Consensus<Delegate> cd = inDelegateCache.get(id);
        if (null == cd) {
            cd = waitDelegateCache.get(id);
        }
        return cd;
    }

    public int getDelegateCount() {
        return inDelegateCache.size();
    }

    public List<Consensus<Delegate>> getCachedDelegateList() {
        return inDelegateCache.values();
    }

    public void delDelegate(String id) {
        inDelegateCache.remove(id);
        waitDelegateCache.remove(id);
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
}
