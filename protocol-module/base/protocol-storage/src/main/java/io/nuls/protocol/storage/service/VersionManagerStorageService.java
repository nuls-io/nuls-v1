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

import io.nuls.kernel.model.Result;
import io.nuls.protocol.storage.po.BlockProtocolInfoPo;
import io.nuls.protocol.storage.po.ProtocolInfoPo;
import io.nuls.protocol.storage.po.ProtocolTempInfoPo;

import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/8/17
 */
public interface VersionManagerStorageService {

    /**
     * 保存当前主网运行中的版本
     * Save the version currently running on the main net
     *
     * @param version 版本号
     * @return Result 结果
     */
    Result saveMainVersion(int version);

    /**
     * 获取当前主网运行中的版本
     * Gets the version currently running on the main net
     *
     * @return version 版本号
     */
    Integer getMainVersion();

    /**
     *
     * @param protocolInfoPo 某个版本的升级信息
     * @return Result 结果
     */
    Result saveProtocolInfoPo(ProtocolInfoPo protocolInfoPo);

    /**
     *
     * @param version 版本号
     * @return ProtocolInfoPo 对应版本的升级信息
     */
    ProtocolInfoPo getProtocolInfoPo(int version);

    /**
     * 保存升级新版的临时数据
     * Save the number of nodes that have upgraded the new version of the program
     *
     * @param tempInfoPo 某个版本的升级信息
     * @return Result 结果
     */
    Result saveProtocolTempInfoPo(ProtocolTempInfoPo tempInfoPo);

    /**
     * 获取升级新版的临时数据
     * Gets the number of nodes that have upgraded the new version of the program
     *
     * @param key 版本号
     * @return ProtocolTempInfoPo 对应版本的升级信息
     */
    ProtocolTempInfoPo getProtocolTempInfoPo(String key);

    Result saveBlockProtocolInfoPo(BlockProtocolInfoPo protocolInfoPo);

    List<Long> getBlockProtocolIndex(int version);

    List<Long> getBlockTempProtocolIndex(int version);

    void saveBlockProtocolIndex(int version, List<Long> list);

    void saveTempBlockProtocolIndex(int version, List<Long> list);

    BlockProtocolInfoPo getBlockProtocolInfoPo(long blockHeight);

    BlockProtocolInfoPo getBlockTempProtocolInfoPo(long blockHeight);

    void clearBlockProtocol(long blockHeight, int version);

    void clearTempBlockProtocol(long blockHeight, int version);

    Result saveBlockProtocolTempInfoPo(BlockProtocolInfoPo protocolInfoPo);
    /**
     * 获取所有升级新版的临时数据
     * @return
     */
    Map<String,ProtocolTempInfoPo> getProtocolTempMap();

    void removeProtocolTempInfo(String key);

    Result saveChangeTxHashBlockHeight(Long effectiveHeight);

    Long getChangeTxHashBlockHeight();

    void deleteBlockProtocol(long blockHeight);

    void deleteBlockTempProtocol(long blockHeight);

    Result saveConsensusVersionMap(Map<String, Integer> versionMap);

    Map<String, Integer> getConsensusVersionMap();
}
