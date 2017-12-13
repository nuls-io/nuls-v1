package io.nuls.consensus.service.cache;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.entity.member.ConsensusAccountData;
import io.nuls.consensus.entity.member.ConsensusAccountImpl;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.utils.ConsensusBeanUtils;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.db.dao.DelegateAccountDao;
import io.nuls.db.dao.DelegateDao;

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
    private static final String CACHE_CONSENSUS_ACCOUNT_LIST = "consensus-account-list";
    private static final String CACHE_BLOCK_HEADER_LIST = "block-header-list";
    private static final String CACHE_BLOCK_HEIGHT_HASH_MAPPING = "block-height-hash-mapping";

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
        this.cacheService.createCache(CACHE_CONSENSUS_ACCOUNT_LIST);

        this.cacheService.createCache(CACHE_BLOCK_HEADER_LIST);
        this.cacheService.createCache(CACHE_BLOCK_HEIGHT_HASH_MAPPING);

//todo 代理节点与委托单
        Account self = accountService.getLocalAccount();
    }

    public ConsensusStatusInfo getConsensusStatusInfo() {
        ConsensusStatusInfo info =
                (ConsensusStatusInfo) this.cacheService.getElementValueWithOutClone(CACHE_CONSENSUS_STATUS_INFO, CACHE_CONSENSUS_STATUS_INFO);
        return info;
    }

    public void updateConsensusStatusInfo(ConsensusStatusInfo info) {
        this.cacheService.putElementWithOutClone(CACHE_CONSENSUS_STATUS_INFO, CACHE_CONSENSUS_STATUS_INFO, info);
    }

    public void addConsensusAccount(ConsensusAccount ca) {
        cacheService.putElement(CACHE_CONSENSUS_ACCOUNT_LIST, ca.getAddress(), ca);
    }

    public ConsensusAccount<ConsensusAccountData> getConsensusAccount(String address) {
        return (ConsensusAccount) this.cacheService.getElementValue(CACHE_CONSENSUS_ACCOUNT_LIST, address);
    }

    public void delConsensusAccount(String address) {
        this.cacheService.removeElement(CACHE_CONSENSUS_ACCOUNT_LIST, address);
    }

    public void changeStatus(String address, ConsensusStatusEnum statusEnum) {
        ConsensusAccount<ConsensusAccountData> ca = getConsensusAccount(address);
        if (null == ca) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "temporary consensus account not exist!");
        }
        ca.getExtend().setStatus(statusEnum.getCode());
        this.cacheService.putElement(CACHE_CONSENSUS_ACCOUNT_LIST, address, ca);
    }


}
