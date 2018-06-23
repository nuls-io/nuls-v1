/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.storage.service.impl;

import io.nuls.consensus.poc.storage.constant.ConsensusStorageConstant;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
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
 * Created by ln on 2018/5/10.
 */
@Component
public class AgentStorageServiceImpl implements AgentStorageService, InitializingBean {

    @Autowired
    private DBService dbService;

    @Override
    public boolean save(AgentPo agentPo) {
        if (agentPo == null || agentPo.getHash() == null) {
            return false;
        }
        byte[] hash;
        try {
            hash = agentPo.getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        Result result = null;
        try {
            result = dbService.put(ConsensusStorageConstant.DB_NAME_CONSENSUS_AGENT, hash, agentPo.serialize());
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        return result.isSuccess();
    }

    @Override
    public AgentPo get(NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        byte[] body = null;
        try {
            body = dbService.get(ConsensusStorageConstant.DB_NAME_CONSENSUS_AGENT, hash.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        if (body == null) {
            return null;
        }
        AgentPo agentPo = new AgentPo();
        try {
            agentPo.parse(body,0);
        } catch (NulsException e) {
            Log.error(e);
            throw  new NulsRuntimeException(e);
        }
        agentPo.setHash(hash);
        return agentPo;
    }

    @Override
    public boolean delete(NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        Result result = null;
        try {
            result = dbService.delete(ConsensusStorageConstant.DB_NAME_CONSENSUS_AGENT, hash.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        return result.isSuccess();
    }

    @Override
    public List<AgentPo> getList() {
        List<Entry<byte[], byte[]>> list = dbService.entryList(ConsensusStorageConstant.DB_NAME_CONSENSUS_AGENT);
        List<AgentPo> resultList = new ArrayList<>();
        if (list == null) {
            return resultList;
        }
        for (Entry<byte[], byte[]> entry : list) {
            AgentPo agentPo = new AgentPo();
            try {
                agentPo.parse(entry.getValue(),0);
            } catch (NulsException e) {
                Log.error(e);
                throw  new NulsRuntimeException(e);
            }
            NulsDigestData hash = new NulsDigestData();
            try {
                hash.parse(entry.getKey(),0);
            } catch (NulsException e) {
                Log.error(e);
            }
            agentPo.setHash(hash);
            resultList.add(agentPo);
        }
        return resultList;
    }

    @Override
    public int size() {
        Set<byte[]> list = dbService.keySet(ConsensusStorageConstant.DB_NAME_CONSENSUS_AGENT);
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        dbService.createArea(ConsensusStorageConstant.DB_NAME_CONSENSUS_AGENT);
    }
}