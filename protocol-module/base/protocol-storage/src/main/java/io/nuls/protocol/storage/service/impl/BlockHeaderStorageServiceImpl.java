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

package io.nuls.protocol.storage.service.impl;

import io.nuls.core.tools.crypto.VarInt;
import io.nuls.db.service.DBService;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.protocol.storage.constant.ProtocolStorageConstant;
import io.nuls.protocol.storage.po.BlockHeaderPo;
import io.nuls.protocol.storage.service.BlockHeaderStorageService;

/**
 * 区块头数据存储服务实现类
 * Block header data storage service implementation class.
 *
 * @author: Niels Wang
 * @date: 2018/5/8
 */

@Service
public class BlockHeaderStorageServiceImpl implements BlockHeaderStorageService {
    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    public BlockHeaderStorageServiceImpl() {
        this.dbService.createArea(ProtocolStorageConstant.DB_AREA_BLOCK_HEADER_INDEX);
        this.dbService.createArea(ProtocolStorageConstant.DB_AREA_BLOCK_HEADER);
    }

    /**
     * 根据区块高度查询区块头数据
     * Query block header data according to block height.
     *
     * @param height 区块高度/block height
     * @return BlockHeaderPo 区块头数据
     */
    @Override
    public BlockHeaderPo getBlockHeaderPo(long height) {
        byte[] hashBytes = dbService.get(ProtocolStorageConstant.DB_AREA_BLOCK_HEADER_INDEX, new VarInt(height).encode());
        if (null == hashBytes) {
            return null;
        }
        return getBlockHeaderPo(hashBytes);
    }

    /**
     * 根据区块hash查询区块头数据
     * Query block header data according to block hash.
     *
     * @param hash 区块头摘要/block hash
     * @return BlockHeaderPo 区块头数据
     */
    @Override
    public BlockHeaderPo getBlockHeaderPo(NulsDigestData hash) {
        if (null == hash) {
            return null;
        }
        return getBlockHeaderPo(hash.serialize());
    }

    /**
     * 根据区块hash查询区块头数据
     * Query block header data according to block hash.
     *
     * @param hashBytes 区块头摘要/block hash
     * @return BlockHeaderPo 区块头数据
     */
    private BlockHeaderPo getBlockHeaderPo(byte[] hashBytes) {
        byte[] bytes = dbService.get(ProtocolStorageConstant.DB_AREA_BLOCK_HEADER, hashBytes);
        if (null == bytes) {
            return null;
        }
        BlockHeaderPo po = new BlockHeaderPo();
        po.parse(bytes);
        NulsDigestData hash = new NulsDigestData();
        hash.parse(hashBytes);
        po.setHash(hash);
        return po;
    }

    /**
     * 保存区块头数据到存储中
     * Save the block header data to the storage.
     *
     * @param po 区块头数据/block header data
     * @return 操作结果/operating result
     */
    @Override
    public Result saveBlockHeader(BlockHeaderPo po) {
        if (null == po) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] hashBytes = po.getHash().serialize();
        Result result = dbService.put(ProtocolStorageConstant.DB_AREA_BLOCK_HEADER, hashBytes, po.serialize());
        if (result.isFailed()) {
            return result;
        }
        result = dbService.put(ProtocolStorageConstant.DB_AREA_BLOCK_HEADER_INDEX, new VarInt(po.getHeight()).encode(), hashBytes);
        if (result.isFailed()) {
            this.removeBlockHerader(hashBytes);
            return result;
        }
        dbService.put(ProtocolStorageConstant.DB_AREA_BLOCK_HEADER_INDEX, new VarInt(ProtocolStorageConstant.BEST_BLOCK_HASH_INDEX).encode(), hashBytes);
        return Result.getSuccess();
    }

    private Result removeBlockHerader(byte[] hashBytes) {
        return dbService.delete(ProtocolStorageConstant.DB_AREA_BLOCK_HEADER, hashBytes);
    }

    /**
     * 从存储中删除区块头数据
     * Remove block header data from storage.
     *
     * @param po 区块头,摘要和高度必须要有/Block heads, abstracts and heights must be available.
     * @return 操作结果/operating result
     */
    @Override
    public Result removeBlockHerader(BlockHeaderPo po) {
        if (null == po || po.getHeight() < 0 || po.getHash() == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        dbService.delete(ProtocolStorageConstant.DB_AREA_BLOCK_HEADER_INDEX, new VarInt(po.getHeight()).encode());
        dbService.put(ProtocolStorageConstant.DB_AREA_BLOCK_HEADER_INDEX, new VarInt(ProtocolStorageConstant.BEST_BLOCK_HASH_INDEX).encode(), po.getPreHash().serialize());
        return removeBlockHerader(po.getHash().serialize());
    }

    /**
     * 获取最新区块头数据
     * Gets the latest block header data.
     */
    @Override
    public BlockHeaderPo getBestBlockHeaderPo() {
        return getBlockHeaderPo(ProtocolStorageConstant.BEST_BLOCK_HASH_INDEX);
    }
}
