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

package io.nuls.protocol.storage.service;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.protocol.storage.po.BlockHeaderPo;

/**
 * 区块头数据存储服务接口
 * Block header data storage service interface.
 *
 * @author: Niels Wang
 * @date: 2018/5/8
 */
public interface BlockHeaderStorageService {

    /**
     * 根据区块高度查询区块头数据
     * Query block header data according to block height.
     *
     * @param height 区块高度/block height
     * @return BlockHeaderPo 区块头数据
     */
    BlockHeaderPo getBlockHeaderPo(long height);

    /**
     * 根据区块hash查询区块头数据
     * Query block header data according to block hash.
     *
     * @param hash 区块头摘要/block hash
     * @return BlockHeaderPo 区块头数据
     */
    BlockHeaderPo getBlockHeaderPo(NulsDigestData hash);

    /**
     * 保存区块头数据到存储中
     * Save the block header data to the storage.
     *
     * @param po 区块头数据/block header data
     * @return 操作结果/operating result
     */
    Result saveBlockHeader(BlockHeaderPo po);

    /**
     * 从存储中删除区块头数据
     * Remove block header data from storage.
     *
     * @param po 区块头/block header data
     * @return 操作结果/operating result
     */
    Result removeBlockHerader(BlockHeaderPo po);

    /**
     * 获取最新的区块头
     * Get the latest block header.
     */
    BlockHeaderPo getBestBlockHeaderPo();
}
