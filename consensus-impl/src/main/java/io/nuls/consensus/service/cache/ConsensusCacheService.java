package io.nuls.consensus.service.cache;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.consensus.entity.ConsensusMember;
import io.nuls.consensus.entity.member.ConsensusMemberImpl;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.utils.ConsensusBeanUtils;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.db.dao.ConsensusAccountDao;
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
    private static final String CACHE_CONSENSUS_ACCOUNT_LIST_TEMP = "consensus-account-list-temp";

    private static final ConsensusCacheService INSTANCE = new ConsensusCacheService();

    private ConsensusAccountDao consensusAccountDao = NulsContext.getInstance().getService(ConsensusAccountDao.class);

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
        this.cacheService.createCache(CACHE_CONSENSUS_ACCOUNT_LIST_TEMP);
        List<ConsensusAccountPo> polist = this.consensusAccountDao.queryAll();
        List<ConsensusMemberImpl> list = new ArrayList<>();
        for(ConsensusAccountPo po:polist){
            list.add(ConsensusBeanUtils.fromPojo(po));
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
       cacheService.putElement(CACHE_CONSENSUS_ACCOUNT_LIST,ca.getAddress(),ca);
    }

    public ConsensusMember getConsensusMember(String address){
        return (ConsensusMember)this.cacheService.getElementValue(CACHE_CONSENSUS_ACCOUNT_LIST,address);
    }

    public void delConsensusMember(String address) {
        this.cacheService.removeElement(CACHE_CONSENSUS_ACCOUNT_LIST,address);
    }

    public void addTempConsensusMember(ConsensusMember ca) {
        cacheService.putElement(CACHE_CONSENSUS_ACCOUNT_LIST_TEMP,ca.getAddress(),ca);
    }

    public ConsensusMember getTempConsensusMember(String address){
        return (ConsensusMember)this.cacheService.getElementValue(CACHE_CONSENSUS_ACCOUNT_LIST_TEMP,address);
    }
    public void delTempConsensusMember(String address) {
        this.cacheService.removeElement(CACHE_CONSENSUS_ACCOUNT_LIST_TEMP,address);
    }

    public void moveAccountFromTemp(String address){
        ConsensusMember ca = getTempConsensusMember(address);
        if(null==ca){
            throw new NulsRuntimeException(ErrorCode.FAILED,"temporary consensus account not exist!");
        }
        delTempConsensusMember(address);
        addConsensusMember(ca);
    }


}
