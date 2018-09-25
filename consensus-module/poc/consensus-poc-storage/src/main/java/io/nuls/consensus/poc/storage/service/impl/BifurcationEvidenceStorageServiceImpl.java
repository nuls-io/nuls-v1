/*
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
 *
 */
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
