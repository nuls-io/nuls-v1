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
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.db.model.Entry;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author: Niels Wang
 */
@Component
public class DepositStorageServiceImpl implements DepositStorageService, InitializingBean {

    @Autowired
    private DBService dbService;

    @Override
    public boolean save(DepositPo depositPo) {
        if (depositPo == null || depositPo.getTxHash() == null) {
            return false;
        }
        byte[] hash;
        try {
            hash = depositPo.getTxHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        Result result = null;
        try {
            result = dbService.put(ConsensusStorageConstant.DB_NAME_CONSENSUS_DEPOSIT, hash, depositPo.serialize());
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        return result.isSuccess();
    }

    @Override
    public DepositPo get(NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        byte[] body = null;
        try {
            body = dbService.get(ConsensusStorageConstant.DB_NAME_CONSENSUS_DEPOSIT, hash.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        if (body == null) {
            return null;
        }
        DepositPo depositPo = new DepositPo();
        try {
            depositPo.parse(body, 0);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        depositPo.setTxHash(hash);
        return depositPo;
    }

    @Override
    public boolean delete(NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        Result result = null;
        try {
            result = dbService.delete(ConsensusStorageConstant.DB_NAME_CONSENSUS_DEPOSIT, hash.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        return result.isSuccess();
    }

    @Override
    public List<DepositPo> getList() {
        List<Entry<byte[], byte[]>> list = dbService.entryList(ConsensusStorageConstant.DB_NAME_CONSENSUS_DEPOSIT);
        List<DepositPo> resultList = new ArrayList<>();
        if (list == null) {
            return resultList;
        }
        for (Entry<byte[], byte[]> entry : list) {
            DepositPo depositPo = new DepositPo();
            try {
                depositPo.parse(entry.getValue(), 0);
            } catch (NulsException e) {
                Log.error(e);
                throw new NulsRuntimeException(e);
            }
            NulsDigestData hash = new NulsDigestData();
            try {
                hash.parse(entry.getKey(), 0);
            } catch (NulsException e) {
                Log.error(e);
            }
            depositPo.setTxHash(hash);
            resultList.add(depositPo);
        }
        return resultList;
    }

    @Override
    public int size() {
        Set<byte[]> list = dbService.keySet(ConsensusStorageConstant.DB_NAME_CONSENSUS_DEPOSIT);
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        dbService.createArea(ConsensusStorageConstant.DB_NAME_CONSENSUS_DEPOSIT);
    }
}
