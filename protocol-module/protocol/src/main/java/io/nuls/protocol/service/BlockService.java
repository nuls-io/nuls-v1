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

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.network.model.Node;
import io.nuls.protocol.model.SmallBlock;

/**
 * 区块处理服务接口
 * The block handles the service interface.
 *
 * @author Niels
 * @date 2017/11/10
 */
public interface BlockService {
    /**
     * 获取创世块（从存储中）
     * Get the creation block (from storage)
     */
    Result<Block>  getGengsisBlock();

    /**
     * 获取最新的区块（从存储中）
     * Get the highest block (from storage)
     */
    Result<Block> getBestBlock();

    /**
     * 获取最新的区块头（从存储中）
     * Get the highest block header (from storage)
     */
    Result<BlockHeader> getBestBlockHeader();

    /**
     * 根据区块高度获取区块头（从存储中）
     * Get the block head (from storage) according to the block height
     *
     * @param height 区块高度/block height
     * @return 区块头
     */
    Result<BlockHeader> getBlockHeader(long height);

    /**
     * 根据区块摘要获取区块头（从存储中）
     * Get the block head (from storage) according to the block hash
     *
     * @param hash 区块摘要/block hash
     * @return 区块头/block header
     */
    Result<BlockHeader> getBlockHeader(NulsDigestData hash);

    /**
     * 根据区块摘要获取区块（从存储中）
     * Get the block (from storage) according to the block hash
     *
     * @param hash 区块摘要/block hash
     * @return 区块/block
     */
    Result<Block> getBlock(NulsDigestData hash);

    /**
     * 根据区块高度获取区块（从存储中）
     * Get the block (from storage) according to the block height
     *
     * @param height 区块高度/block height
     * @return 区块/block
     */
    Result<Block> getBlock(long height);

    /**
     * 保存区块到存储中
     * Save the block to the store.
     *
     * @param block 完整区块/whole block
     * @return 操作结果/operating result
     * @throws NulsException 保存区块有可能出现异常，请捕获后谨慎处理/There may be exceptions to the save block, please handle it carefully after capture.
     */
    Result saveBlock(Block block) throws NulsException;

    /**
     * 回滚区块
     * roll back the block to the store.
     *
     * @param block 完整区块/whole block
     * @return 操作结果/operating result
     * @throws NulsException 回滚区块有可能出现异常，请捕获后谨慎处理/There may be exceptions to the roll back block, please handle it carefully after capture.
     */
    Result rollbackBlock(Block block) throws NulsException;

    /**
     * 转发区块给连接的其他对等节点，允许一个列外（不转发给它）
     * Forward block to other peers of the connection, allowing one column (not forward to it)
     *
     * @param block       完整区块/the whole block
     * @param excludeNode 需要排除的节点，一般是因为从该节点处接收的本区块/The nodes that need to be excluded are generally due to the block received from the node.
     * @return 转发结果/forward results
     */
    Result forwardBlock(SmallBlock block, Node excludeNode);

    /**
     * 广播区块给连接的其他对等节点
     * The broadcast block gives the connection to other peers.
     *
     * @param block 完整区块/the whole block
     * @return 广播结果/Broadcast the results
     */
    Result broadcastBlock(SmallBlock block);
}
