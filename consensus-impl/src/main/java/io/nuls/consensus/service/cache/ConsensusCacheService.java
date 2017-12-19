package io.nuls.consensus.service.cache;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.params.QueryConsensusAccountParam;
import io.nuls.consensus.utils.ConsensusBeanUtils;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.DelegateAccountDao;
import io.nuls.db.dao.DelegateDao;
import io.nuls.db.entity.DelegateAccountPo;
import io.nuls.db.entity.DelegatePo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class ConsensusCacheService {

    /**
     * the title of the cache(ConsensusStatusInfo,single)
     */
    private static final String CACHE_CONSENSUS_STATUS_INFO = "consensus-status-info";
    private static final String IN_AGENT_LIST = "agent-list-in";
    private static final String WAIT_AGENT_LIST = "agent-list-wait";
    private static final String CACHE_DELEGATE_LIST = "delegate-list";

    private static final ConsensusCacheService INSTANCE = new ConsensusCacheService();

    private DelegateDao delegateDao = NulsContext.getInstance().getService(DelegateDao.class);
    private DelegateAccountDao delegateAccountDao = NulsContext.getInstance().getService(DelegateAccountDao.class);
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private CacheService cacheService = NulsContext.getInstance().getService(CacheService.class);

    private ConsensusCacheService() {
        initCache();
    }

    public static ConsensusCacheService getInstance() {
        return INSTANCE;
    }

    private void initCache() {
        this.cacheService.createCache(CACHE_CONSENSUS_STATUS_INFO);
        this.cacheService.createCache(IN_AGENT_LIST);
        this.cacheService.createCache(WAIT_AGENT_LIST);
        this.cacheService.createCache(CACHE_DELEGATE_LIST);

        Account self = accountService.getLocalAccount();
        List<DelegatePo> delegatePoList = this.delegateDao.queryAll();
        List<DelegateAccountPo> delegateAccountPoList = this.delegateAccountDao.queryAll();
        for(DelegateAccountPo po:delegateAccountPoList){
            Consensus<Agent> ca =ConsensusBeanUtils.fromPojo(po);
            this.addAgent(ca);
            if(ca.getAddress().equals(self.getAddress().toString())){
                //todo 自己的共识状态
                ConsensusStatusInfo info = new ConsensusStatusInfo();
                info.setStartTime(ca.getExtend().getStartTime());
            }
        }
    }

    public ConsensusStatusInfo getConsensusStatusInfo() {
        ConsensusStatusInfo info =
                (ConsensusStatusInfo) this.cacheService.getElementValueWithOutClone(CACHE_CONSENSUS_STATUS_INFO, CACHE_CONSENSUS_STATUS_INFO);
        return info;
    }

    public void updateConsensusStatusInfo(ConsensusStatusInfo info) {
        this.cacheService.putElementWithoutClone(CACHE_CONSENSUS_STATUS_INFO, CACHE_CONSENSUS_STATUS_INFO, info);
    }

    public void addAgent(Consensus<Agent> ca) {
        if (ca.getExtend().getStatus() == ConsensusStatusEnum.IN.getCode()) {
            cacheService.putElement(IN_AGENT_LIST, ca.getAddress(), ca);
        } else if (ca.getExtend().getStatus() == ConsensusStatusEnum.WAITING.getCode()) {
            cacheService.putElement(WAIT_AGENT_LIST, ca.getAddress(), ca);
        }
    }

    public Consensus<Agent> getConsensusAccount(String address) {
        Consensus<Agent> ca = (Consensus<Agent>) this.cacheService.getElementValue(IN_AGENT_LIST, address);
        if (ca == null) {
            ca = (Consensus<Agent>) this.cacheService.getElementValue(WAIT_AGENT_LIST, address);
        }
        return ca;
    }

    public void delConsensusAccount(String address) {
        this.cacheService.removeElement(IN_AGENT_LIST,address);
        this.cacheService.removeElement(WAIT_AGENT_LIST,address);
    }

    public void changeStatus(String address, ConsensusStatusEnum statusEnum) {
        Consensus<Agent> ca =getConsensusAccount(address);
        if (statusEnum.getCode() == ca.getExtend().getStatus()) {
            return;
        }
        this.delConsensusAccount(address);
        ca.getExtend().setStatus(statusEnum.getCode());
        this.addAgent(ca);
    }


    public void clear() {
        // todo auto-generated method stub(niels)

    }

    public List<Consensus> getConsensusAccountList(QueryConsensusAccountParam param) {
        List<Consensus> list = new ArrayList<>();
        Consensus<Delegate> consensus = new
                Consensus<>();
        // todo auto-generated method stub(niels)
        return null;
    }

    public ConsensusStatusInfo getConsensusStatusInfo(String address) {

        // todo auto-generated method stub(niels)
        return null;
    }

    public int getDelegateAccountCount() {
        // todo auto-generated method stub(niels)
        return 0;
    }
}
