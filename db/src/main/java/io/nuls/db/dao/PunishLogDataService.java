/*
 *
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
package io.nuls.db.dao;

import io.nuls.db.entity.PunishLogPo;

import java.util.List;

/**
 * @author vivi
 * @date 2017/12/13.
 */
public interface PunishLogDataService extends BaseDataService<String, PunishLogPo> {

    /**
     * @param height
     * @return
     */
    int deleteByHeight(long height);

    long getCountByType(String agentAddress,int punishType);

    List<PunishLogPo> getListByType(int type);
    List<PunishLogPo> getListByType(String address,int type);

    /**
     * the count of punishlog
     * @param address
     * @param type
     * @param roundStart
     * @param roundEnd
     * @return
     */
    long getCountByType(String address, int type, long roundStart, long roundEnd);
}
