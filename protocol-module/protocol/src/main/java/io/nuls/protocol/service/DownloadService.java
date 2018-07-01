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

package io.nuls.protocol.service;

import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.network.model.Node;
import io.nuls.protocol.model.TxGroup;

import java.util.List;

/**
 * 区块/交易下载服务接口
 * Block/transaction download service interface.
 *
 * @author Niels
 */
public interface DownloadService {

    /**
     * 从指定节点处根据hash下载一个区块，下载过程中线程是阻塞的
     * Download a block according from the node to the hash, and the download process is blocked.
     *
     * @param hash 区块摘要/block hash
     * @param node 指定的节点/Specified node
     * @return 区块及结果/ block  results
     */
    Result<Block> downloadBlock(NulsDigestData hash, Node node);

    /**
     * 根据交易摘要列表从指定节点处下载交易列表，下载过程中线程是阻塞的
     * Download the transaction list from the specified node according to the transaction summary list, and the thread is blocked in the download process.
     *
     * @param txHashList 想要下载的交易摘要列表/The list of transactions that you want to download.
     * @param node       指定的节点/Specified node
     * @return 交易列表的封装对象/A wrapper object for a transaction list.
     */
    Result<TxGroup> downloadTxGroup(List<NulsDigestData> txHashList, Node node);

    /**
     * 返回下载是否完成的结果
     * Returns the results of the download.
     * @return Result
     */
    Result isDownloadSuccess();

    /**
     * 重新检查当前状态是否需要重新同步区块，如果需要则下载
     * Recheck whether the current state needs to be resynchronized, and download if necessary.
     * @return Result
     */
    Result reset();

}
