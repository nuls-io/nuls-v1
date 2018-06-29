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
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.consensus.poc.storage.service.PunishLogStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.db.model.Entry;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Niels Wang
 */
@Component
public class PunishLogStorageServiceImpl implements PunishLogStorageService, InitializingBean {

    @Autowired
    private DBService dbService;

    @Override
    public boolean save(PunishLogPo po) {
        if (po == null || po.getKey() == null) {
            return false;
        }
        Result result = null;
        try {
            result = dbService.put(ConsensusStorageConstant.DB_NAME_CONSENSUS_PUNISH_LOG, po.getKey(), po.serialize());
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        return result.isSuccess();
    }

    @Override
    public boolean delete(byte[] key) {
        if (null == key) {
            return false;
        }
        Result result = dbService.delete(ConsensusStorageConstant.DB_NAME_CONSENSUS_PUNISH_LOG, key);
        return result.isSuccess();
    }

    @Override
    public List<PunishLogPo> getPunishList() {
        List<Entry<byte[], byte[]>> list = dbService.entryList(ConsensusStorageConstant.DB_NAME_CONSENSUS_PUNISH_LOG);
        List<PunishLogPo> polist = new ArrayList<>();
        for (Entry<byte[], byte[]> entry : list) {
            PunishLogPo po = new PunishLogPo();
            try {
                po.parse(entry.getValue(), 0);
            } catch (NulsException e) {
                throw new NulsRuntimeException(e);
            }
            polist.add(po);
        }
        return polist;
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        dbService.createArea(ConsensusStorageConstant.DB_NAME_CONSENSUS_PUNISH_LOG);
    }
}
