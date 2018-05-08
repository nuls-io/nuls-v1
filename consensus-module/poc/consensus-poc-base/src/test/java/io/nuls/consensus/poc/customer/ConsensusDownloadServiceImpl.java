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

package io.nuls.consensus.poc.customer;

import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.network.entity.Node;
import io.nuls.protocol.model.TxGroup;
import io.nuls.protocol.service.DownloadService;

import java.util.List;

/**
 * Created by ln on 2018/5/8.
 */
public class ConsensusDownloadServiceImpl implements DownloadService {

    private boolean isDownloadSuccess;

    @Override
    public Result<Block> downloadBlock(NulsDigestData hash) {
        return null;
    }

    @Override
    public Result<Block> downloadBlock(NulsDigestData hash, Node node) {
        return null;
    }

    @Override
    public Result<TxGroup> downloadTxGroup(List<NulsDigestData> txHashList) {
        return null;
    }

    @Override
    public Result<TxGroup> downloadTxGroup(List<NulsDigestData> txHashList, Node node) {
        return null;
    }

    @Override
    public Result isDownloadSuccess() {
        return new Result(isDownloadSuccess, null);
    }

    @Override
    public Result reset() {
        return null;
    }

    public void setDownloadSuccess(boolean downloadSuccess) {
        isDownloadSuccess = downloadSuccess;
    }
}