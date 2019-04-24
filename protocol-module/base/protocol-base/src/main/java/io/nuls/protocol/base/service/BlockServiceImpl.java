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

import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.dto.ContractTransfer;
import io.nuls.contract.entity.tx.CallContractTransaction;
import io.nuls.contract.entity.tx.ContractTransferTransaction;
import io.nuls.contract.service.ContractService;
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
import io.nuls.protocol.constant.ProtocolErroeCode;
import io.nuls.protocol.message.ForwardSmallBlockMessage;
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

    @Autowired
    private ContractService contractService;

    /**
     * 获取创世块（从存储中）
     * Get the creation block (from storage)
     */
    @Override
    public Result<Block> getGengsisBlock() {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBlockHeaderPo(0);
        if (null == headerPo) {
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
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
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
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
        return getBlock(headerPo, false);
    }

    private Block getBlock(BlockHeaderPo headerPo, boolean isNeedContractTransfer) {
        List<Transaction> txList = new ArrayList<>();
        for (NulsDigestData hash : headerPo.getTxHashList()) {
            Transaction tx = ledgerService.getTx(hash);
            txList.add(tx);
            if (isNeedContractTransfer) {
                //pierre add 增加合约转账(从合约转出)交易到查询的区块中
                contractTransfer(tx, txList);
            }
        }
        Block block = new Block();
        BlockHeader blockHeader = PoConvertUtil.fromBlockHeaderPo(headerPo);
        if (isNeedContractTransfer) {
            blockHeader.setTxCount(txList.size());
        }
        block.setHeader(blockHeader);
        block.setTxs(txList);
        return block;
    }

    private void contractTransfer(Transaction tx, List<Transaction> txList) {
        if (tx instanceof CallContractTransaction) {
            CallContractTransaction callTx = (CallContractTransaction) tx;
            ContractResult contractResult = callTx.getContractResult();
            if (contractResult != null) {
                List<ContractTransfer> transfers = contractResult.getTransfers();
                // 合约调用交易存在合约转账(从合约转出)交易，则从全网账本中查出完整交易添加到交易集合中
                if (transfers != null && transfers.size() > 0) {
                    for (ContractTransfer transfer : transfers) {
                        Transaction contractTx = ledgerService.getTx(transfer.getHash());
                        if (contractTx != null) {
                            txList.add(contractTx);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取最新的区块头（从存储中）
     * Get the highest block header (from storage)
     */
    @Override
    public Result<BlockHeader> getBestBlockHeader() {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBestBlockHeaderPo();
        if (null == headerPo) {
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
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
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
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
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
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
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
        }
        Block block = getBlock(headerPo);
        return Result.getSuccess().setData(block);
    }

    /**
     * 根据区块摘要获取区块（从存储中）
     * Get the block (from storage) according to the block hash
     *
     * @param hash                   区块摘要/block hash
     * @param isNeedContractTransfer 是否需要把合约转账(从合约转出)交易添加到区块中/If necessary to add the contract transfer (from the contract) to the block
     * @return 区块/block
     */
    @Override
    public Result<Block> getBlock(NulsDigestData hash, boolean isNeedContractTransfer) {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBlockHeaderPo(hash);
        if (null == headerPo) {
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
        }
        Block block = getBlock(headerPo, isNeedContractTransfer);
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
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
        }
        Block block = getBlock(headerPo);
        return Result.getSuccess().setData(block);
    }

    /**
     * 根据区块高度获取区块（从存储中）
     * Get the block (from storage) according to the block height
     *
     * @param height                 区块高度/block height
     * @param isNeedContractTransfer 是否需要把合约转账(从合约转出)交易添加到区块中/If necessary to add the contract transfer (from the contract) to the block
     * @return 区块/block
     */
    @Override
    public Result<Block> getBlock(long height, boolean isNeedContractTransfer) {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBlockHeaderPo(height);
        if (null == headerPo) {
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
        }
        Block block = getBlock(headerPo, isNeedContractTransfer);
        return Result.getSuccess().setData(block);
    }

    @Override
    public List<String> getBlockTxHash(long height) {
        BlockHeaderPo headerPo = blockHeaderStorageService.getBlockHeaderPo(height);
        List<String> list = new ArrayList<>();
        if (null != headerPo && null != headerPo.getTxHashList()) {
            for (NulsDigestData hash : headerPo.getTxHashList()) {
                list.add(hash.getDigestHex());
            }
        }
        return list;
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
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
        }
        long height = block.getHeader().getHeight();
        List<Transaction> savedList = new ArrayList<>();
        for (Transaction transaction : block.getTxs()) {
            transaction.setBlockHeight(height);
            boolean needRollback = false;
            Result result = transactionService.commitTx(transaction, block.getHeader());
            if (result.isSuccess()) {
                result = ledgerService.saveTx(transaction);
            } else {
                needRollback = true;
            }
            if (result.isSuccess()) {
                savedList.add(transaction);
            } else {
                if (needRollback) {
                    this.transactionService.rollbackCommit(transaction, block.getHeader());
                }
                this.rollbackTxList(savedList, block.getHeader(), false);
                return result;
            }
        }
        Result result = this.blockHeaderStorageService.saveBlockHeader(PoConvertUtil.toBlockHeaderPo(block));
        if (result.isFailed()) {
            this.rollbackTxList(savedList, block.getHeader(), false);
            return result;
        }
        try {
            accountLedgerService.saveConfirmedTransactionList(block.getTxs());
            // 保存合约相关交易
            contractService.saveConfirmedTransactionList(block.getTxs());
        } catch (Exception e) {
            Log.warn("save local tx failed", e);
        }
        return Result.getSuccess();
    }

    /**
     * 保存区块失败时，需要将已经存储的交易回滚
     * When you fail to save the block, you need to roll back the already stored transaction.
     */
    private boolean rollbackTxList(List<Transaction> savedList, BlockHeader blockHeader, boolean atomicity) throws NulsException {
        List<Transaction> rollbackedList = new ArrayList<>();
        for (int i = savedList.size() - 1; i >= 0; i--) {
            Transaction tx = savedList.get(i);
            Result result = transactionService.rollbackTx(tx, blockHeader);
            if (atomicity) {
                if (result.isFailed()) {
                    break;
                } else {
                    rollbackedList.add(tx);
                }
            }
        }
        if (atomicity && savedList.size() != rollbackedList.size()) {
            for (int i = rollbackedList.size() - 1; i >= 0; i--) {
                Transaction tx = rollbackedList.get(i);
                transactionService.commitTx(tx, blockHeader);
            }
            return false;
        }
        return true;
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
            return Result.getFailed(ProtocolErroeCode.BLOCK_IS_NULL);
        }
        boolean txsResult = this.rollbackTxList(block.getTxs(), block.getHeader(), true);
        if (!txsResult) {
            return Result.getFailed();
        }
        BlockHeaderPo po = new BlockHeaderPo();
        po.setHash(block.getHeader().getHash());
        po.setHeight(block.getHeader().getHeight());
        po.setPreHash(block.getHeader().getPreHash());
        Result result = this.blockHeaderStorageService.removeBlockHerader(po);
        if (result.isFailed()) {
            return result;
        }
        try {
            accountLedgerService.rollbackTransactions(block.getTxs());
            // 回滚合约相关交易
            contractService.rollbackTransactionList(block.getTxs());
        } catch (Exception e) {
            Log.warn("rollbackTransaction local tx failed", e);
        }
        return result;
    }

    /**
     * 转发区块给连接的其他对等节点，允许一个列外（不转发给它）
     * Forward block to other peers of the connection, allowing one column (not forward to it)
     *
     * @param blockHash   区块摘要/the hash of block
     * @param excludeNode 需要排除的节点，一般是因为从该节点处接收的本区块/The nodes that need to be excluded are generally due to the block received from the node.
     * @return 转发结果/forward results
     */
    @Override
    public Result forwardBlock(NulsDigestData blockHash, Node excludeNode) {
        ForwardSmallBlockMessage message = new ForwardSmallBlockMessage();
        message.setMsgBody(blockHash);
        return messageBusService.broadcast(message, excludeNode, true, 100);
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
        Result<List<String>> result = messageBusService.broadcast(message, null, true, 100);
        return result;
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
