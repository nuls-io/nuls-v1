package io.nuls.consensus.poc.storage.service.impl;

import io.nuls.consensus.poc.storage.constant.ConsensusStorageConstant;
import io.nuls.consensus.poc.storage.po.EvidencePo;
import io.nuls.consensus.poc.storage.service.BifurcationEvidenceStorageService;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;

import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/9/4
 */
@Component
public class BifurcationEvidenceStorageServiceImpl implements BifurcationEvidenceStorageService, InitializingBean {
    @Autowired
    private DBService dbService;

    @Override
    public Map<String, List<EvidencePo>> getBifurcationEvidence() {
        Map<String, List<EvidencePo>> map = dbService.getModel(ConsensusStorageConstant.DB_NAME_CONSENSUS_BIFURCATION_EVIDENCE,
                ConsensusStorageConstant.DB_BIFURCATION_EVIDENCE_KEY.getBytes(), Map.class);
        return map;
    }

    @Override
    public boolean save(Map<String, List<EvidencePo>> map) {
       Result result = dbService.putModel(ConsensusStorageConstant.DB_NAME_CONSENSUS_BIFURCATION_EVIDENCE,
                ConsensusStorageConstant.DB_BIFURCATION_EVIDENCE_KEY.getBytes(), map);
        return result.isSuccess();
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        dbService.createArea(ConsensusStorageConstant.DB_NAME_CONSENSUS_BIFURCATION_EVIDENCE);
    }
}
