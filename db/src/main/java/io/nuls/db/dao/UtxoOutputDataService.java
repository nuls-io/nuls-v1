/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.db.dao;

import io.nuls.db.entity.UtxoOutputPo;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/20
 */
public interface UtxoOutputDataService extends BaseDataService<Map<String, Object>, UtxoOutputPo> {

    List<UtxoOutputPo> getTxOutputs(String txHash);

    List<UtxoOutputPo> getAccountOutputs(String address, byte status);

    List<UtxoOutputPo> getAccountOutputs(int txType, String address, Long beginTime, Long endTime);

    List<UtxoOutputPo> getAllUnSpend();

    long getLockUtxoCount(String address, Long beginTime, Long bestHeight, Long genesisTime);

    List<UtxoOutputPo> getLockUtxo(String address, Long beginTime, Long bestHeight, Long genesisTime, Integer start, Integer limit);

    List<UtxoOutputPo> getAccountUnSpend(String address);

    int updateStatus(List<UtxoOutputPo> list);

    int updateStatus(UtxoOutputPo po);

    void deleteByHash(String txHash);

    long getRewardByBlockHeight(long height);

    long getLastDayTimeReward();

    long getAccountReward(String address, long lastTime);

    long getAgentReward(String address, int type);

    void unlockTxOutput(String txHash);

    void lockTxOutput(String txHash);
}
