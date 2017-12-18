package io.nuls.consensus.service.cache;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.params.QueryConsensusAccountParam;
import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.DelegateAccountDao;
import io.nuls.db.dao.DelegateDao;

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
    private static final String WAIT_AGENT_LIST = "agent-list-in";
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

        //todo 代理节点与委托单
        Account self = accountService.getLocalAccount();
        this.delegateDao.queryAll();
    }

    public ConsensusStatusInfo getConsensusStatusInfo() {
        ConsensusStatusInfo info =
                (ConsensusStatusInfo) this.cacheService.getElementValueWithOutClone(CACHE_CONSENSUS_STATUS_INFO, CACHE_CONSENSUS_STATUS_INFO);
        return info;
    }

    public void updateConsensusStatusInfo(ConsensusStatusInfo info) {
        this.cacheService.putElementWithOutClone(CACHE_CONSENSUS_STATUS_INFO, CACHE_CONSENSUS_STATUS_INFO, info);
    }

    public void addAgent(ConsensusAccount<Agent> ca) {
        if (ca.getExtend().getStatus() == ConsensusStatusEnum.IN.getCode()) {
            cacheService.putElement(IN_AGENT_LIST, ca.getAddress(), ca);
        } else if (ca.getExtend().getStatus() == ConsensusStatusEnum.WAITING.getCode()) {
            cacheService.putElement(WAIT_AGENT_LIST, ca.getAddress(), ca);
        }
    }

    public ConsensusAccount<Agent> getConsensusAccount(String address) {
        ConsensusAccount<Agent> ca = (ConsensusAccount<Agent>) this.cacheService.getElementValue(IN_AGENT_LIST, address);
        if (ca == null) {
            ca = (ConsensusAccount<Agent>) this.cacheService.getElementValue(WAIT_AGENT_LIST, address);
        }
        return ca;
    }

    public void delConsensusAccount(String address) {
        this.cacheService.removeElement(IN_AGENT_LIST,address);
        this.cacheService.removeElement(WAIT_AGENT_LIST,address);
    }

    public void changeStatus(String address, ConsensusStatusEnum statusEnum) {
        ConsensusAccount<Agent> ca =getConsensusAccount(address);
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

    public List<ConsensusAccount> getConsensusAccountList(QueryConsensusAccountParam param) {
        List<ConsensusAccount> list = new ArrayList<>();
        ConsensusAccount<Delegate> consensusAccount = new
                ConsensusAccount<>();
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
