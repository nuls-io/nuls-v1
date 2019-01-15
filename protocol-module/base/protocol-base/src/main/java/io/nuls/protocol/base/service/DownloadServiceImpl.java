/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.protocol.base.service;

import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.network.model.Node;
import io.nuls.protocol.base.constant.DownloadStatus;
import io.nuls.protocol.base.download.processor.DownloadProcessor;
import io.nuls.protocol.base.download.utils.DownloadUtils;
import io.nuls.protocol.constant.ProtocolErroeCode;
import io.nuls.protocol.model.TxGroup;
import io.nuls.protocol.service.DownloadService;

import java.util.List;

/**
 * @author ln
 */
@Service
public class DownloadServiceImpl implements DownloadService {

    private DownloadProcessor processor = DownloadProcessor.getInstance();

    /**
     * 从指定节点处根据hash下载一个区块，下载过程中线程是阻塞的
     * Download a block according from the node to the hash, and the download process is blocked.
     *
     * @param hash 区块摘要/block hash
     * @param node 指定的节点/Specified node
     * @return 区块及结果/ block & results
     */
    @Override
    public Result<Block> downloadBlock(NulsDigestData hash, Node node) {
        Block block = null;
        try {
            block = DownloadUtils.getBlockByHash(hash, node);
        } catch (RuntimeException e) {
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        if (block == null) {
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
        }
        return Result.getSuccess().setData(block);
    }

    /**
     * 返回下载是否完成的结果
     * Returns the results of the download.
     */
    @Override
    public Result isDownloadSuccess() {
        if (processor.getDownloadStatus() == DownloadStatus.SUCCESS) {
            return Result.getSuccess();
        }
        return Result.getFailed(KernelErrorCode.FAILED);
    }

    public boolean start() {
        processor = DownloadProcessor.getInstance();
        return processor.startup();
    }

    public boolean stop() {
        return processor.shutdown();
    }

    @Override
    public Result reset() {
        processor.setDownloadStatus(DownloadStatus.WAIT);
        return Result.getSuccess();
    }

}
