package io.nuls.consensus.service.cache;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusMember;
import io.nuls.consensus.entity.member.ConsensusMemberData;
import io.nuls.consensus.entity.member.ConsensusMemberImpl;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.utils.ConsensusBeanUtils;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.db.dao.ConsensusAccountDao;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.ConsensusAccountPo;

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
    private static final String CACHE_CONSENSUS_ACCOUNT_LIST = "consensus-account-list";
    private static final String CACHE_BLOCK_HEADER_LIST = "block-header-list";
    private static final String CACHE_BLOCK_HEIGHT_HASH_MAPPING = "block-height-hash-mapping";

    private static final ConsensusCacheService INSTANCE = new ConsensusCacheService();

    private ConsensusAccountDao consensusAccountDao = NulsContext.getInstance().getService(ConsensusAccountDao.class);
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


        Account self = accountService.getLocalAccount();
        List<ConsensusAccountPo> polist = this.consensusAccountDao.queryAll();
        for (ConsensusAccountPo po : polist) {
            ConsensusMemberImpl member = ConsensusBeanUtils.fromPojo(po);
            cacheService.putElement(CACHE_CONSENSUS_ACCOUNT_LIST, member.getExtend().getId() , member);
            if (member.getAddress().toString().equals(self.getAddress().toString())) {
                ConsensusStatusInfo info = new ConsensusStatusInfo();
                info.setAccumulativeReward(0);
                info.setParkedCount(0);
                info.setStartTime(0);
//                if(member.getStatus()==)
                //todo
            }
        }
    }

    public ConsensusStatusInfo getConsensusStatusInfo() {
        ConsensusStatusInfo info =
                (ConsensusStatusInfo) this.cacheService.getElementValue(CACHE_CONSENSUS_STATUS_INFO, CACHE_CONSENSUS_STATUS_INFO);
        return info;
    }

    public void updateConsensusStatusInfo(ConsensusStatusInfo info) {
        this.cacheService.putElement(CACHE_CONSENSUS_STATUS_INFO, CACHE_CONSENSUS_STATUS_INFO, info);
    }

    public void addConsensusMember(ConsensusMember ca) {
        cacheService.putElement(CACHE_CONSENSUS_ACCOUNT_LIST, ca.getAddress(), ca);
    }

    public ConsensusMember<ConsensusMemberData> getConsensusMember(String address) {
        return (ConsensusMember) this.cacheService.getElementValue(CACHE_CONSENSUS_ACCOUNT_LIST, address);
    }

    public void delConsensusMember(String address) {
        this.cacheService.removeElement(CACHE_CONSENSUS_ACCOUNT_LIST, address);
    }

    public void changeStatus(String address, ConsensusStatusEnum statusEnum) {
        ConsensusMember<ConsensusMemberData> ca = getConsensusMember(address);
        if (null == ca) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "temporary consensus account not exist!");
        }
        ca.getExtend().setStatus(statusEnum.getCode());
        this.cacheService.putElement(CACHE_CONSENSUS_ACCOUNT_LIST, address, ca);
    }


}
