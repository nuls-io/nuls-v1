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

package io.nuls.protocol.base.service;

import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.ledger.service.LedgerService;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.base.utils.PoConvertUtil;
import io.nuls.protocol.message.SmallBlockMessage;
import io.nuls.protocol.model.SmallBlock;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.TransactionService;
import io.nuls.protocol.storage.po.BlockHeaderPo;
import io.nuls.protocol.storage.service.BlockHeaderStorageService;

import java.util.ArrayList;
import java.util.List;

/**
 * 区块处理服务类
 * Block processing service classes.
 *
 * @author: Niels Wang
 * @date: 2018/5/8
 */
@Service("blockService")
public class BlockServiceImpl implements BlockService {

    /**
     * 存储工具类
     * Storage utility class
     */
    @Autowired
    private BlockHeaderStorageService blockHeaderStorageService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MessageBusService messageBusService;
    @Autowired
    private AccountLedgerService accountLedgerService;

    /**
     * 获取创世块（从存储中）
     * Get the creation block (from storage)
     */
    @Override
    public Result<Block> getGengsisBlock() {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBlockHeaderPo(0);
        if (null == headerPo) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        Block block = getBlock(headerPo);
        return Result.getSuccess().setData(block);
    }

    /**
     * 获取最新的区块（从存储中）
     * Get the highest block (from storage)
     */
    @Override
    public Result<Block> getBestBlock() {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBestBlockHeaderPo();
        if (null == headerPo) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        Block block = getBlock(headerPo);
        return Result.getSuccess().setData(block);
    }

    /**
     * 根据区块头po组装完整区块
     * Assemble the complete block according to block head Po.
     *
     * @param headerPo 区块头对象/block header po
     * @return 完整的区块/the complete block
     */
    private Block getBlock(BlockHeaderPo headerPo) {
        List<Transaction> txList = new ArrayList<>();
        for (NulsDigestData hash : headerPo.getTxHashList()) {
            Transaction tx = ledgerService.getTx(hash);
            txList.add(tx);
        }
        Block block = new Block();
        block.setHeader(PoConvertUtil.fromBlockHeaderPo(headerPo));
        block.setTxs(txList);
        return block;
    }

    /**
     * 获取最新的区块头（从存储中）
     * Get the highest block header (from storage)
     */
    @Override
    public Result<BlockHeader> getBestBlockHeader() {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBestBlockHeaderPo();
        if (null == headerPo) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        return Result.getSuccess().setData(PoConvertUtil.fromBlockHeaderPo(headerPo));
    }

    /**
     * 根据区块高度获取区块头（从存储中）
     * Get the block head (from storage) according to the block height
     *
     * @param height 区块高度/block height
     * @return 区块头
     */
    @Override
    public Result<BlockHeader> getBlockHeader(long height) {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBlockHeaderPo(height);
        if (null == headerPo) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        return Result.getSuccess().setData(PoConvertUtil.fromBlockHeaderPo(headerPo));
    }

    /**
     * 根据区块摘要获取区块头（从存储中）
     * Get the block head (from storage) according to the block hash
     *
     * @param hash 区块摘要/block hash
     * @return 区块头/block header
     */
    @Override
    public Result<BlockHeader> getBlockHeader(NulsDigestData hash) {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBlockHeaderPo(hash);
        if (null == headerPo) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        return Result.getSuccess().setData(PoConvertUtil.fromBlockHeaderPo(headerPo));
    }

    /**
     * 根据区块摘要获取区块（从存储中）
     * Get the block (from storage) according to the block hash
     *
     * @param hash 区块摘要/block hash
     * @return 区块/block
     */
    @Override
    public Result<Block> getBlock(NulsDigestData hash) {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBlockHeaderPo(hash);
        if (null == headerPo) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        Block block = getBlock(headerPo);
        return Result.getSuccess().setData(block);
    }

    /**
     * 根据区块高度获取区块（从存储中）
     * Get the block (from storage) according to the block height
     *
     * @param height 区块高度/block height
     * @return 区块/block
     */
    @Override
    public Result<Block> getBlock(long height) {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBlockHeaderPo(height);
        if (null == headerPo) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        Block block = getBlock(headerPo);
        return Result.getSuccess().setData(block);
    }

    /**
     * 保存区块到存储中
     * Save the block to the store.
     *
     * @param block 完整区块/whole block
     * @return 操作结果/operating result
     * @throws NulsException 保存区块有可能出现异常，请捕获后谨慎处理/There may be exceptions to the save block, please handle it carefully after capture.
     */
    @Override
    public Result saveBlock(Block block) throws NulsException {
        if (null == block || block.getHeader() == null || block.getTxs() == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        List<Transaction> savedList = new ArrayList<>();
        for (Transaction transaction : block.getTxs()) {
            transaction.setBlockHeight(block.getHeader().getHeight());
            Result result = transactionService.commitTx(transaction, block.getHeader());
            if (result.isSuccess()) {
                result = ledgerService.saveTx(transaction);
            }
            if (result.isSuccess()) {
                savedList.add(transaction);
            } else {
                this.rollbackTxList(savedList, block.getHeader());
                return result;
            }
        }
        Result result = this.blockHeaderStorageService.saveBlockHeader(PoConvertUtil.toBlockHeaderPo(block));
        if (result.isFailed()) {
            this.rollbackTxList(savedList, block.getHeader());
            return result;
        }
        try {
            accountLedgerService.saveConfirmedTransactionList(block.getTxs());
        } catch (Exception e) {
            Log.warn("save local tx failed", e);
        }
        return Result.getSuccess();
    }

    /**
     * 保存区块失败时，需要将已经存储的交易回滚
     * When you fail to save the block, you need to roll back the already stored transaction.
     */
    private void rollbackTxList(List<Transaction> savedList, BlockHeader blockHeader) throws NulsException {
        for (Transaction tx : savedList) {
            transactionService.rollbackTx(tx, blockHeader);
            ledgerService.rollbackTx(tx);
        }
    }

    /**
     * 回滚区块
     * roll back the block to the store.
     *
     * @param block 完整区块/whole block
     * @return 操作结果/operating result
     * @throws NulsException 回滚区块有可能出现异常，请捕获后谨慎处理/There may be exceptions to the roll back block, please handle it carefully after capture.
     */
    @Override
    public Result rollbackBlock(Block block) throws NulsException {
        if (null == block) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        this.rollbackTxList(block.getTxs(), block.getHeader());
        BlockHeaderPo po = new BlockHeaderPo();
        po.setHash(block.getHeader().getHash());
        po.setHeight(block.getHeader().getHeight());
        Result result = this.blockHeaderStorageService.removeBlockHerader(po);
        if (result.isFailed()) {
            return result;
        }
        try {
            accountLedgerService.rollback(block.getTxs());
        } catch (Exception e) {
            Log.warn("rollback local tx failed", e);
        }
        return result;
    }

    /**
     * 转发区块给连接的其他对等节点，允许一个列外（不转发给它）
     * Forward block to other peers of the connection, allowing one column (not forward to it)
     *
     * @param smallBlock  小区块/the small block
     * @param excludeNode 需要排除的节点，一般是因为从该节点处接收的本区块/The nodes that need to be excluded are generally due to the block received from the node.
     * @return 转发结果/forward results
     */
    @Override
    public Result forwardBlock(SmallBlock smallBlock, Node excludeNode) {
        SmallBlockMessage message = fillSmallBlockMessage(smallBlock);
        return messageBusService.broadcastHashAndCache(message, excludeNode, true);
    }

    /**
     * 广播小区块给连接的其他对等节点
     * The broadcast small block gives the connection to other peers.
     *
     * @param smallBlock 小区块/the small block
     * @return 广播结果/Broadcast the results
     */
    @Override
    public Result broadcastBlock(SmallBlock smallBlock) {
        SmallBlockMessage message = fillSmallBlockMessage(smallBlock);
        return messageBusService.broadcastHashAndCache(message, null, false);
    }

    /**
     * 将小区块放入消息容器中，返回消息容器
     * the block is put into the message container and the message container is returned.
     *
     * @param smallBlock 小区块对象
     * @return 小区块消息容器/Block message container.
     */
    private SmallBlockMessage fillSmallBlockMessage(SmallBlock smallBlock) {
        SmallBlockMessage message = new SmallBlockMessage();
        message.setMsgBody(smallBlock);
        return message;
    }
}
