package io.nuls.consensus.poc.storage.service;



import io.nuls.consensus.poc.storage.po.EvidencePo;

import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/9/4
 */
public interface BifurcationEvidenceStorageService {

    Map<String, List<EvidencePo>> getBifurcationEvidence();

    boolean save(Map<String, List<EvidencePo>> map);

}
