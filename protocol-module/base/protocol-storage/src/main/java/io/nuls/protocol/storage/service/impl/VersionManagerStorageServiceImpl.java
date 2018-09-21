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

import io.nuls.core.tools.crypto.Util;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.VarInt;
import io.nuls.protocol.storage.constant.ProtocolStorageConstant;
import io.nuls.protocol.storage.po.BlockProtocolInfoPo;
import io.nuls.protocol.storage.po.ProtocolInfoPo;
import io.nuls.protocol.storage.po.ProtocolTempInfoPo;
import io.nuls.protocol.storage.service.VersionManagerStorageService;
import org.checkerframework.checker.units.qual.A;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/8/17
 */
@Service
public class VersionManagerStorageServiceImpl implements VersionManagerStorageService, InitializingBean {
    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    /**
     * 创建存储表，创建失败时如果是因为已存在则正常，否则抛出异常
     * Create a storage table, or throw an exception if it is normal if it is already existing.
     */
    @Override
    public void afterPropertiesSet() {
        Result result = this.dbService.createArea(ProtocolStorageConstant.NULS_VERSION_AREA);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }

        result = this.dbService.createArea(ProtocolStorageConstant.NULS_PROTOCOL_AREA);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }

        result = this.dbService.createArea(ProtocolStorageConstant.PROTOCOL_TEMP_AREA);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }

        result = this.dbService.createArea(ProtocolStorageConstant.BLOCK_PROTOCOL_INDEX);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }

        result = this.dbService.createArea(ProtocolStorageConstant.BLOCK_PROTOCOL_AREA);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }

        result = this.dbService.createArea(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_INDEX);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }

        result = this.dbService.createArea(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_AREA);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result saveMainVersion(int version) {
        return dbService.put(ProtocolStorageConstant.NULS_VERSION_AREA, ProtocolStorageConstant.MAIN_VERSION_KEY, Util.intToBytes(version));
    }

    @Override
    public Integer getMainVersion() {
        byte[] mainVersion = dbService.get(ProtocolStorageConstant.NULS_VERSION_AREA, ProtocolStorageConstant.MAIN_VERSION_KEY);
        return null == mainVersion ? null : Util.byteToInt(mainVersion);
    }

    @Override
    public Result saveProtocolInfoPo(ProtocolInfoPo upgradeInfoPo) {
        return dbService.putModel(ProtocolStorageConstant.NULS_PROTOCOL_AREA, Util.intToBytes(upgradeInfoPo.getVersion()), upgradeInfoPo);
    }

    @Override
    public ProtocolInfoPo getProtocolInfoPo(int version) {
        return (ProtocolInfoPo) dbService.getModel(ProtocolStorageConstant.NULS_PROTOCOL_AREA, Util.intToBytes(version));
    }

    @Override
    public Result saveProtocolTempInfoPo(ProtocolTempInfoPo tempInfoPo) {
        try {
            return dbService.putModel(ProtocolStorageConstant.PROTOCOL_TEMP_AREA, tempInfoPo.getProtocolKey().getBytes("utf-8"), tempInfoPo);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Result.getFailed();
    }

    @Override
    public ProtocolTempInfoPo getProtocolTempInfoPo(String key) {
        try {
            return dbService.getModel(ProtocolStorageConstant.PROTOCOL_TEMP_AREA, key.getBytes("utf-8"), ProtocolTempInfoPo.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Result saveBlockProtocolInfoPo(BlockProtocolInfoPo protocolInfoPo) {
        List<Long> blockHeightIndex = dbService.getModel(ProtocolStorageConstant.BLOCK_PROTOCOL_INDEX, Util.intToBytes(protocolInfoPo.getVersion()), List.class);
        if (blockHeightIndex == null) {
            blockHeightIndex = new ArrayList<>();
        }
        blockHeightIndex.add(protocolInfoPo.getBlockHeight());
        dbService.putModel(ProtocolStorageConstant.BLOCK_PROTOCOL_INDEX, Util.intToBytes(protocolInfoPo.getVersion()), blockHeightIndex);
        return dbService.putModel(ProtocolStorageConstant.BLOCK_PROTOCOL_AREA, new VarInt(protocolInfoPo.getBlockHeight()).encode(), protocolInfoPo);
    }

    @Override
    public List<Long> getBlockProtocolIndex(int version) {
        return dbService.getModel(ProtocolStorageConstant.BLOCK_PROTOCOL_INDEX, Util.intToBytes(version), List.class);
    }

    @Override
    public List<Long> getBlockTempProtocolIndex(int version) {
        return dbService.getModel(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_INDEX, Util.intToBytes(version), List.class);
    }

    @Override
    public void saveBlockProtocolIndex(int version, List<Long> list) {
        dbService.putModel(ProtocolStorageConstant.BLOCK_PROTOCOL_INDEX, Util.intToBytes(version), list);
    }

    @Override
    public void saveTempBlockProtocolIndex(int version, List<Long> list) {
        dbService.putModel(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_INDEX, Util.intToBytes(version), list);
    }

    @Override
    public BlockProtocolInfoPo getBlockProtocolInfoPo(long blockHeight) {
        return dbService.getModel(ProtocolStorageConstant.BLOCK_PROTOCOL_AREA, new VarInt(blockHeight).encode(), BlockProtocolInfoPo.class);
    }

    @Override
    public BlockProtocolInfoPo getBlockTempProtocolInfoPo(long blockHeight) {
        return dbService.getModel(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_AREA, new VarInt(blockHeight).encode(), BlockProtocolInfoPo.class);
    }

    @Override
    public void clearBlockProtocol(long blockHeight, int version) {
        dbService.delete(ProtocolStorageConstant.BLOCK_PROTOCOL_INDEX, Util.intToBytes(version));
        dbService.delete(ProtocolStorageConstant.BLOCK_PROTOCOL_AREA, new VarInt(blockHeight).encode());
    }

    @Override
    public void clearTempBlockProtocol(long blockHeight, int version) {
        dbService.delete(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_INDEX, Util.intToBytes(version));
        dbService.delete(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_AREA, new VarInt(blockHeight).encode());
    }

    @Override
    public Result saveBlockProtocolTempInfoPo(BlockProtocolInfoPo protocolInfoPo) {
        List<Long> blockHeightIndex = dbService.getModel(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_INDEX, Util.intToBytes(protocolInfoPo.getVersion()), List.class);
        if (blockHeightIndex == null) {
            blockHeightIndex = new ArrayList<>();
        }
        blockHeightIndex.add(protocolInfoPo.getBlockHeight());
        dbService.putModel(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_INDEX, Util.intToBytes(protocolInfoPo.getVersion()), blockHeightIndex);
        return dbService.putModel(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_AREA, new VarInt(protocolInfoPo.getBlockHeight()).encode(), protocolInfoPo);
    }


    @Override
    public void deleteBlockProtocol(long blockHeight) {
        dbService.delete(ProtocolStorageConstant.BLOCK_PROTOCOL_AREA, new VarInt(blockHeight).encode());
    }

    @Override
    public void deleteBlockTempProtocol(long blockHeight) {
        dbService.delete(ProtocolStorageConstant.BLOCK_TEMP_PROTOCOL_AREA, new VarInt(blockHeight).encode());
    }

    @Override
    public Map<String, ProtocolTempInfoPo> getProtocolTempMap() {
        List<ProtocolTempInfoPo> list = dbService.values(ProtocolStorageConstant.PROTOCOL_TEMP_AREA, ProtocolTempInfoPo.class);
        Map<String, ProtocolTempInfoPo> map = new HashMap<>();
        if (null == list) {
            return map;
        }
        for (ProtocolTempInfoPo protocolTempInfoPo : list) {
            map.put((protocolTempInfoPo.getProtocolKey()), protocolTempInfoPo);
        }
        return map;
    }

    @Override
    public void removeProtocolTempInfo(String key) {
        dbService.delete(ProtocolStorageConstant.PROTOCOL_TEMP_AREA, key.getBytes());
    }

    @Override
    public Result saveChangeTxHashBlockHeight(Long effectiveHeight) {
        return dbService.put(ProtocolStorageConstant.NULS_VERSION_AREA, ProtocolStorageConstant.CHANGE_HASH_HEIGHT_KEY, Util.longToBytes(effectiveHeight));

    }

    @Override
    public Long getChangeTxHashBlockHeight() {
        byte[] height = dbService.get(ProtocolStorageConstant.NULS_VERSION_AREA, ProtocolStorageConstant.CHANGE_HASH_HEIGHT_KEY);
        return height == null ? null : Long.valueOf(Util.byteToInt(height));
    }

}
