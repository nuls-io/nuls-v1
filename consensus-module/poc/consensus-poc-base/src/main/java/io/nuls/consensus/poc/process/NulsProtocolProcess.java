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
package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.manager.RoundManager;
import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.storage.service.TransactionCacheStorageService;
import io.nuls.consensus.poc.storage.service.TransactionQueueStorageService;
import io.nuls.consensus.poc.util.ProtocolTransferTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.protocol.base.version.NulsVersionManager;
import io.nuls.protocol.base.version.ProtocolContainer;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.storage.po.BlockProtocolInfoPo;
import io.nuls.protocol.storage.po.ProtocolInfoPo;
import io.nuls.protocol.storage.po.ProtocolTempInfoPo;
import io.nuls.protocol.storage.service.VersionManagerStorageService;

import java.math.BigDecimal;
import java.util.*;

public class NulsProtocolProcess {

    private static NulsProtocolProcess protocolProcess = new NulsProtocolProcess();

    private NulsProtocolProcess() {

    }

    public static NulsProtocolProcess getInstance() {
        return protocolProcess;
    }

    private VersionManagerStorageService versionManagerStorageService;

    private BlockService blockService;

    /**
     * 版本升级流程处理
     *
     * @param blockHeader
     */
    public void processProtocolUpGrade(BlockHeader blockHeader) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        //临时处理为空的情况，为空是由于第一个版本的区块不包含版本信息字段
        if (extendsData.getCurrentVersion() == null) {
            extendsData.setCurrentVersion(1);
        }
        NulsVersionManager.getConsensusVersionMap().put(AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress()), extendsData.getCurrentVersion());
        getVersionManagerStorageService().saveConsensusVersionMap(NulsVersionManager.getConsensusVersionMap());
        if (extendsData.getCurrentVersion() < 1) {
            getVersionManagerStorageService().saveConsensusVersionHeight(blockHeader.getHeight());
            return;
        }
        refreshProtocolCoverageRate(extendsData, blockHeader);
        refreshTempProtocolCoverageRate(extendsData, blockHeader);
        //收到的区块版本信息大于当前主网版本信息时，统计覆盖率和延迟块数
        if (extendsData.getCurrentVersion() > NulsVersionManager.getMainVersion()) {
            ProtocolContainer protocolContainer = NulsVersionManager.getProtocolContainer(extendsData.getCurrentVersion());
            if (protocolContainer != null) {
                calcNewProtocolCoverageRate(protocolContainer, extendsData, blockHeader);
                //遍历所有临时协议的tempInfo，如果当前block的packing地址在其addressSet中，则将其删除
                for (ProtocolTempInfoPo tempInfoPo : getVersionManagerStorageService().getProtocolTempMap().values()) {
                    if (tempInfoPo.getStatus() != ProtocolContainer.VALID) {
                        String packingAddress = AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress());
                        if (tempInfoPo.getAddressSet().contains(packingAddress)) {
                            tempInfoPo.getAddressSet().remove(packingAddress);
                        }
                    }
                }
                //针对其他能识别但未生效的协议Container，做同样的操作
                for (ProtocolContainer container : NulsVersionManager.getAllProtocolContainers().values()) {
                    if (container.getStatus() != ProtocolContainer.VALID
                            && container.getVersion().intValue() != protocolContainer.getVersion().intValue()) {
                        String packingAddress = AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress());
                        if (container.getAddressSet().contains(packingAddress)) {
                            container.getAddressSet().remove(packingAddress);
                        }
                    }
                }
            } else {
                //如果没有,则存入临时的协议容器里
                ProtocolTempInfoPo protocolTempInfoPo = getVersionManagerStorageService().getProtocolTempInfoPo(extendsData.getProtocolKey());
                if (protocolTempInfoPo == null) {
                    protocolTempInfoPo = ProtocolTransferTool.createProtocolTempInfoPo(extendsData);
                }
                calcTempProtocolCoverageRate(protocolTempInfoPo, extendsData, blockHeader);
                //遍历所有能识别的协议Container，如果当前block的packing地址在其中，则将其删除
                for (ProtocolContainer container : NulsVersionManager.getAllProtocolContainers().values()) {
                    if (container.getStatus() != ProtocolContainer.VALID) {
                        String packingAddress = AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress());
                        if (container.getAddressSet().contains(packingAddress)) {
                            container.getAddressSet().remove(packingAddress);
                        }
                    }
                }
                //针对未识别的临时协议tempInfo，做同样的操作
                for (ProtocolTempInfoPo tempInfoPo : getVersionManagerStorageService().getProtocolTempMap().values()) {
                    if (tempInfoPo.getStatus() != ProtocolContainer.VALID
                            && tempInfoPo.getVersion() != protocolTempInfoPo.getVersion()) {
                        String packingAddress = AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress());
                        if (tempInfoPo.getAddressSet().contains(packingAddress)) {
                            tempInfoPo.getAddressSet().remove(packingAddress);
                        }
                    }
                }
            }
        } else {
            calcDelay(blockHeader, extendsData);
            calcTempDelay(blockHeader, extendsData);
        }
        getVersionManagerStorageService().saveConsensusVersionHeight(blockHeader.getHeight());
        //处理完所有流程后还原数据
        if (extendsData.getCurrentVersion() == 1) {
            extendsData.setCurrentVersion(null);
        }
    }

    /**
     * 新的一轮出块开始时，需要重新计算覆盖率
     *
     * @param extendsData 当前出块头版本信息
     */
    private void refreshProtocolCoverageRate(BlockExtendsData extendsData, BlockHeader header) {
        //处理当前钱包已存在的版本
        for (ProtocolContainer container : NulsVersionManager.getAllProtocolContainers().values()) {
            if (container.getStatus() != ProtocolContainer.VALID) {
                //如果容器的轮次小于当前出块节点的轮次，说明是新的一轮开始
                if (container.getRoundIndex() < extendsData.getRoundIndex()) {
                    MeetingRound currentRound = PocConsensusContext.getChainManager().getMasterChain().getCurrentRound();
                    List<MeetingMember> memberList = currentRound.getMemberList();
                    Set<String> memberAddressSet = new HashSet<>();
                    for (MeetingMember member : memberList) {
                        memberAddressSet.add(AddressTool.getStringAddressByBytes(member.getPackingAddress()));
                    }
                    Iterator<String> iterator = container.getAddressSet().iterator();
                    while (iterator.hasNext()) {
                        String address = iterator.next();
                        if (!memberAddressSet.contains(address)) {
                            iterator.remove();
                        }
                    }
                    if (container.getStatus() == ProtocolContainer.INVALID) {
                        container.setCurrentDelay(0);
                    } else {
                        //已经开始统计延迟块的协议，由于存在新的一轮出块节点地址的变化，因此需要重新计算覆盖率
                        //如果覆盖率未达到，则清零延迟块数量，重新计算
                        Result<BlockHeader> result = getBlockService().getBlockHeader(header.getPreHash());
                        BlockHeader preHeader = result.getData();
                        BlockExtendsData preExtendsData = new BlockExtendsData(preHeader.getExtend());

                        int rate = calcRate(container, preExtendsData);
                        container.setCurrentPercent(rate);
                        if (rate < container.getPercent()) {
                            container.setCurrentDelay(0);
                            container.setStatus(ProtocolContainer.INVALID);
                        }
                    }
                    container.setPrePercent(container.getCurrentPercent());
                    //container.getAddressSet().clear();
                    container.setRoundIndex(extendsData.getRoundIndex());
                    saveProtocolInfo(container);
                }
            }
        }
    }

    /**
     * 处理当前版本里配置不存在的更高版本协议(更高版本的协议存储在临时持久化实体里)
     *
     * @param extendsData
     */
    private void refreshTempProtocolCoverageRate(BlockExtendsData extendsData, BlockHeader header) {
        for (ProtocolTempInfoPo tempInfoPo : getVersionManagerStorageService().getProtocolTempMap().values()) {
            if (tempInfoPo.getStatus() != ProtocolContainer.VALID) {

                //如果容器的轮次小于当前出块节点的轮次，说明是新的一轮开始
                if (tempInfoPo.getRoundIndex() < extendsData.getRoundIndex()) {
                    if (tempInfoPo.getStatus() == ProtocolContainer.INVALID) {
                        tempInfoPo.setCurrentDelay(0);
                    } else {

                        MeetingRound currentRound = PocConsensusContext.getChainManager().getMasterChain().getCurrentRound();
                        List<MeetingMember> memberList = currentRound.getMemberList();
                        Set<String> memberAddressSet = new HashSet<>();
                        for (MeetingMember member : memberList) {
                            memberAddressSet.add(AddressTool.getStringAddressByBytes(member.getPackingAddress()));
                        }
                        Iterator<String> iterator = tempInfoPo.getAddressSet().iterator();
                        while (iterator.hasNext()) {
                            String address = iterator.next();
                            if (!memberAddressSet.contains(address)) {
                                iterator.remove();
                            }
                        }
                        //已经开始统计延迟块的协议，由于存在新的一轮出块节点地址的变化，因此需要重新计算覆盖率
                        //如果覆盖率未达到，则清零延迟块数量，重新计算
                        Result<BlockHeader> result = getBlockService().getBlockHeader(header.getPreHash());
                        BlockHeader preHeader = result.getData();
                        BlockExtendsData preExtendsData = new BlockExtendsData(preHeader.getExtend());
                        int rate = calcRate(tempInfoPo, preExtendsData);
                        tempInfoPo.setCurrentPercent(rate);
                        if (rate < tempInfoPo.getPercent()) {
                            tempInfoPo.setCurrentDelay(0);
                            tempInfoPo.setStatus(ProtocolContainer.INVALID);
                        }
                    }
                    tempInfoPo.setPrePercent(tempInfoPo.getCurrentPercent());
                    //tempInfoPo.getAddressSet().clear();
                    tempInfoPo.setRoundIndex(extendsData.getRoundIndex());
                    getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
                }
            }
        }
    }

    /**
     * 计算最新版本协议的覆盖率和延迟块数，判断新协议是否生效
     *
     * @param container
     * @param extendsData
     */
    private void calcNewProtocolCoverageRate(ProtocolContainer container, BlockExtendsData extendsData, BlockHeader blockHeader) {
        container.getAddressSet().add(AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress()));
        container.setRoundIndex(extendsData.getRoundIndex());
        int rate = calcRate(container, extendsData);
        container.setCurrentPercent(rate);
        //协议未生效时，判断覆盖率
        if (container.getStatus() == ProtocolContainer.INVALID) {
            //覆盖率达到后，修改状态为延迟锁定中
            if (rate >= container.getPercent()) {
                container.setStatus(ProtocolContainer.DELAY_LOCK);
                container.setCurrentDelay(1);
            }
            Log.info("========== 统计协议 ==========");
            Log.info("========== 协议覆盖率：" + rate + " -->>> " + container.getPercent());
            Log.info("========== 上一轮协议覆盖率：" + container.getPrePercent());
            Log.info("========== 协议version：" + container.getVersion());
            Log.info("========== 当前高度：" + blockHeader.getHeight());
            Log.info("========== 当前hash：" + blockHeader.getHash());
            Log.info("========== 协议状态：" + container.getStatus());
            Log.info("========== 协议当前延迟块数：" + container.getCurrentDelay());
            Log.info("========== 协议当前轮次：" + container.getRoundIndex());
            Log.info("========== 协议AddressSet：" + Arrays.toString(container.getAddressSet().toArray()));
        } else if (container.getStatus() == ProtocolContainer.DELAY_LOCK) {
            //当状态为锁定等待延迟高度完成时，首先是从新的一轮开始添加延迟区块数
            container.setCurrentDelay(container.getCurrentDelay() + 1);

            //如果已经达到延迟块数，则新协议生效，从下一区块开始，走新协议
            if (container.getCurrentDelay() >= container.getDelay()) {
                container.setStatus(ProtocolContainer.VALID);
                container.setEffectiveHeight(blockHeader.getHeight() + 1);
                upgradeProtocol(container);
                clearIncompatibleTx();
                Log.info("********** 协议生效了！！！！！！！！！ **********");
                Log.info("********** 协议生效了！！！！！！！！！ **********");
                Log.info("********** 协议生效了！！！！！！！！！ **********");
                Log.info("********** 当前协议生效下一块开始执行新协议 **********");
                Log.info("********** 生效协议version：" + container.getVersion());
                Log.info("********** 生效协议高度：" + container.getEffectiveHeight());
                Log.info("********** 生效协议状态：" + container.getStatus());
                Log.info("********** 生效协议当前延迟块数：" + container.getCurrentDelay());
                Log.info("********** 生效协议当前轮次：" + container.getRoundIndex());
                Log.info("********** 生效协议AddressSet：" + Arrays.toString(container.getAddressSet().toArray()));
            } else {
                Log.info("========== 统计协议 ==========");
                Log.info("========== 协议version：" + container.getVersion());
                Log.info("========== 当前高度：" + blockHeader.getHeight());
                Log.info("========== 当前hash：" + blockHeader.getHash());
                Log.info("========== 协议状态：" + container.getStatus());
                Log.info("========== 协议当前延迟块数：" + container.getCurrentDelay());
                Log.info("========== 协议当前轮次：" + container.getRoundIndex());
                Log.info("========== 协议AddressSet：" + Arrays.toString(container.getAddressSet().toArray()));
            }
        }
        saveProtocolInfo(container);
        saveBlockProtocolInfo(blockHeader, container);
    }

    private void calcTempProtocolCoverageRate(ProtocolTempInfoPo tempInfoPo, BlockExtendsData extendsData, BlockHeader blockHeader) {
        tempInfoPo.getAddressSet().add(AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress()));
        tempInfoPo.setRoundIndex(extendsData.getRoundIndex());
        int rate = calcRate(tempInfoPo, extendsData);
        tempInfoPo.setCurrentPercent(rate);
        MeetingRound currentRound = PocConsensusContext.getChainManager().getMasterChain().getCurrentRound();
        //协议未生效时，判断覆盖率
        if (tempInfoPo.getStatus() == ProtocolContainer.INVALID) {
            //覆盖率达到后，修改状态为延迟锁定中
            if (rate >= tempInfoPo.getPercent()) {
                tempInfoPo.setStatus(ProtocolContainer.DELAY_LOCK);
                tempInfoPo.setCurrentDelay(1);
            }
            getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
            Log.info("========== 统计Temp协议 未定义 ==========");
            Log.info("========== 协议覆盖率：" + rate + " -->>>" + tempInfoPo.getPercent());
            Log.info("========== 上一轮协议覆盖率：" + tempInfoPo.getPrePercent());
            Log.info("========== 协议version：" + tempInfoPo.getVersion());
            Log.info("========== 当前高度：" + blockHeader.getHeight());
            Log.info("========== 当前hash：" + blockHeader.getHash());
            Log.info("========== 协议状态：" + tempInfoPo.getStatus());
            Log.info("========== 协议当前延迟块数：" + tempInfoPo.getCurrentDelay());
            Log.info("========== 协议当前轮次：" + tempInfoPo.getRoundIndex());
            Log.info("========== 协议AddressSet：" + Arrays.toString(tempInfoPo.getAddressSet().toArray()));
            Log.info("========== 当前轮出块节点数：" + currentRound.getMemberCount());
            Log.info("========== 当前轮出块节点：" + Arrays.toString(currentRound.getMemberList().toArray()));
        } else if (tempInfoPo.getStatus() == ProtocolContainer.DELAY_LOCK) {
            //当状态为锁定等待延迟高度完成时，首先是从新的一轮开始添加延迟区块数
            tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() + 1);

            //如果已经达到延迟块数，则新协议生效，从下一区块开始，走新协议
            if (tempInfoPo.getCurrentDelay() >= tempInfoPo.getDelay()) {
                tempInfoPo.setStatus(ProtocolContainer.VALID);
                tempInfoPo.setEffectiveHeight(blockHeader.getHeight() + 1);
                getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
                Log.info("********** 停止服务 **********");
                Log.info("********** 停止服务version：" + tempInfoPo.getVersion());
                Log.info("********** 停止服务高度：" + tempInfoPo.getEffectiveHeight());
                Log.info("********** 停止服务状态：" + tempInfoPo.getStatus());
                Log.info("********** 停止服务当前延迟块数：" + tempInfoPo.getCurrentDelay());
                Log.info("********** 停止服务当前轮次：" + tempInfoPo.getRoundIndex());
                Log.info("********** 停止服务AddressSet：" + Arrays.toString(tempInfoPo.getAddressSet().toArray()));
                //upgradeProtocol(container, blockHeader);
                //如果是linux系统则立即停止，否则将强制更新标志设为true，由钱包提示
                if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("LINUX") != -1) {
                    saveBlockTempProtocolInfo(blockHeader, tempInfoPo);
                    Log.error(">>>>>> The new protocol version has taken effect, this program version is too low has stopped automatically, please upgrade immediately **********");
                    Log.error(">>>>>> The new protocol version has taken effect, this program version is too low has stopped automatically, please upgrade immediately **********");
                    NulsContext.getInstance().exit(1);
                } else {
                    NulsContext.mastUpGrade = true;
                }
            } else {
                getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
                Log.info("========== 统计Temp协议 未定义 ==========");
                Log.info("========== 协议version：" + tempInfoPo.getVersion());
                Log.info("========== 当前高度：" + blockHeader.getHeight());
                Log.info("========== 当前hash：" + blockHeader.getHash());
                Log.info("========== 协议状态：" + tempInfoPo.getStatus());
                Log.info("========== 协议当前延迟块数：" + tempInfoPo.getCurrentDelay());
                Log.info("========== 协议当前轮次：" + tempInfoPo.getRoundIndex());
                Log.info("========== 协议AddressSet：" + Arrays.toString(tempInfoPo.getAddressSet().toArray()));
                Log.info("========== 当前轮出块节点数：" + currentRound.getMemberCount());
                Log.info("========== 当前轮出块节点：" + Arrays.toString(currentRound.getMemberList().toArray()));
            }
        }
        saveBlockTempProtocolInfo(blockHeader, tempInfoPo);
    }


    private void calcDelay(BlockHeader blockHeader, BlockExtendsData extendsData) {
        for (ProtocolContainer container : NulsVersionManager.getAllProtocolContainers().values()) {
            if (container.getVersion().intValue() != extendsData.getCurrentVersion().intValue()
                    && container.getStatus() != ProtocolContainer.VALID) {
                String packingAddress = AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress());
                if (container.getAddressSet().contains(packingAddress)) {
                    container.getAddressSet().remove(packingAddress);
                }
            }
            if (container.getStatus() == ProtocolContainer.DELAY_LOCK) {
                //当状态为锁定等待延迟高度完成时，首先是从新的一轮开始添加延迟区块数
                container.setCurrentDelay(container.getCurrentDelay() + 1);

                //如果已经达到延迟块数，则新协议生效，从下一区块开始，走新协议
                if (container.getCurrentDelay() >= container.getDelay()) {
                    container.setStatus(ProtocolContainer.VALID);
                    container.setEffectiveHeight(blockHeader.getHeight() + 1);
                    upgradeProtocol(container);
                    clearIncompatibleTx();
                    Log.info("********** 协议生效了！！！！！！！！！ **********");
                    Log.info("********** 协议生效了！！！！！！！！！ **********");
                    Log.info("********** 协议生效了！！！！！！！！！ **********");
                    Log.info("********** 当前协议生效下一块开始执行新协议 **********");
                    Log.info("********** 生效协议version：" + container.getVersion());
                    Log.info("********** 生效协议高度：" + container.getEffectiveHeight());
                    Log.info("********** 生效协议状态：" + container.getStatus());
                    Log.info("********** 生效协议当前延迟块数：" + container.getCurrentDelay());
                    Log.info("********** 生效协议当前轮次：" + container.getRoundIndex());
                    Log.info("********** 生效协议AddressSet：" + Arrays.toString(container.getAddressSet().toArray()));
                } else {
                    Log.info("========== 统计协议 ==========");
                    Log.info("========== 协议version：" + container.getVersion());
                    Log.info("========== 当前高度：" + blockHeader.getHeight());
                    Log.info("========== 当前hash：" + blockHeader.getHash());
                    Log.info("========== 协议状态：" + container.getStatus());
                    Log.info("========== 协议当前延迟块数：" + container.getCurrentDelay());
                    Log.info("========== 协议当前轮次：" + container.getRoundIndex());
                    Log.info("========== 协议AddressSet：" + Arrays.toString(container.getAddressSet().toArray()));
                }
                saveProtocolInfo(container);
                saveBlockProtocolInfo(blockHeader, container);
            }
        }
    }

    private void calcTempDelay(BlockHeader blockHeader, BlockExtendsData extendsData) {
        for (ProtocolTempInfoPo tempInfoPo : getVersionManagerStorageService().getProtocolTempMap().values()) {
            if (tempInfoPo.getVersion() != extendsData.getCurrentVersion().intValue()
                    && tempInfoPo.getStatus() != ProtocolContainer.VALID) {
                String packingAddress = AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress());
                if (tempInfoPo.getAddressSet().contains(packingAddress)) {
                    tempInfoPo.getAddressSet().remove(packingAddress);
                }
            }
            if (tempInfoPo.getStatus() == ProtocolContainer.DELAY_LOCK) {
                //当状态为锁定等待延迟高度完成时，首先是从新的一轮开始添加延迟区块数
                tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() + 1);

                //如果已经达到延迟块数，则新协议生效，从下一区块开始，走新协议
                if (tempInfoPo.getCurrentDelay() >= tempInfoPo.getDelay()) {
                    tempInfoPo.setStatus(ProtocolContainer.VALID);
                    tempInfoPo.setEffectiveHeight(blockHeader.getHeight() + 1);
                    getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
                    Log.info("********** 停止服务 **********");
                    Log.info("********** 停止服务version：" + tempInfoPo.getVersion());
                    Log.info("********** 停止服务高度：" + tempInfoPo.getEffectiveHeight());
                    Log.info("********** 停止服务状态：" + tempInfoPo.getStatus());
                    Log.info("********** 停止服务当前延迟块数：" + tempInfoPo.getCurrentDelay());
                    Log.info("********** 停止服务当前轮次：" + tempInfoPo.getRoundIndex());
                    Log.info("********** 停止服务AddressSet：" + Arrays.toString(tempInfoPo.getAddressSet().toArray()));
                    //upgradeProtocol(container, blockHeader);
                    //如果是linux系统则立即停止，否则将强制更新标志设为true，由钱包提示
                    if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("LINUX") != -1) {
                        saveBlockTempProtocolInfo(blockHeader, tempInfoPo);
                        Log.error(">>>>>> The new protocol version has taken effect, this program version is too low has stopped automatically, please upgrade immediately **********");
                        Log.error(">>>>>> The new protocol version has taken effect, this program version is too low has stopped automatically, please upgrade immediately **********");
                        NulsContext.getInstance().exit(1);
                    } else {
                        NulsContext.mastUpGrade = true;
                    }
                } else {
                    getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
                    Log.info("========== 统计Temp协议 未定义 ==========");
                    Log.info("========== 协议version：" + tempInfoPo.getVersion());
                    Log.info("========== 当前高度：" + blockHeader.getHeight());
                    Log.info("========== 当前hash：" + blockHeader.getHash());
                    Log.info("========== 协议状态：" + tempInfoPo.getStatus());
                    Log.info("========== 协议当前延迟块数：" + tempInfoPo.getCurrentDelay());
                    Log.info("========== 协议当前轮次：" + tempInfoPo.getRoundIndex());
                    Log.info("========== 协议AddressSet：" + Arrays.toString(tempInfoPo.getAddressSet().toArray()));
                }
            }
            saveBlockTempProtocolInfo(blockHeader, tempInfoPo);
        }
    }

    /**
     * 协议升级
     *
     * @param container
     */
    private void upgradeProtocol(ProtocolContainer container) {
        NulsContext.MAIN_NET_VERSION = container.getVersion();
        getVersionManagerStorageService().saveMainVersion(container.getVersion());
        if (container.getVersion() == 2) {
            getVersionManagerStorageService().saveChangeTxHashBlockHeight(container.getEffectiveHeight());
            NulsContext.CHANGE_HASH_SERIALIZE_HEIGHT = container.getEffectiveHeight();
        }
    }

    /**
     * 计算覆盖率
     *
     * @param tempInfoPo
     * @return
     */
    private int calcRate(ProtocolTempInfoPo tempInfoPo, BlockExtendsData extendsData) {
        int memberCount = extendsData.getConsensusMemberCount();
        int addressCount = tempInfoPo.getAddressSet().size();
        return calcRate(addressCount, memberCount);
    }

    private int calcRate(ProtocolContainer protocolContainer, BlockExtendsData extendsData) {
        int memberCount = extendsData.getConsensusMemberCount();
        int addressCount = protocolContainer.getAddressSet().size();

        return calcRate(addressCount, memberCount);
    }

    private int calcRate(int addressCount, int memeberCount) {
        BigDecimal b1 = new BigDecimal(addressCount);
        BigDecimal b2 = new BigDecimal(memeberCount);
        int rate = b1.divide(b2, 2, BigDecimal.ROUND_DOWN).movePointRight(2).intValue();
        return rate;
    }

    private void saveProtocolInfo(ProtocolContainer container) {
        ProtocolInfoPo infoPo = ProtocolTransferTool.toProtocolInfoPo(container);
        getVersionManagerStorageService().saveProtocolInfoPo(infoPo);
    }

    private void saveBlockProtocolInfo(BlockHeader blockHeader, ProtocolContainer container) {
        BlockProtocolInfoPo infoPo = ProtocolTransferTool.toBlockProtocolInfoPo(blockHeader, container);
        getVersionManagerStorageService().saveBlockProtocolInfoPo(infoPo);
    }

    private void saveBlockTempProtocolInfo(BlockHeader blockHeader, ProtocolTempInfoPo tempInfoPo) {
        BlockProtocolInfoPo infoPo = ProtocolTransferTool.toBlockProtocolInfoPo(blockHeader, tempInfoPo);
        getVersionManagerStorageService().saveBlockProtocolTempInfoPo(infoPo);
    }

    private VersionManagerStorageService getVersionManagerStorageService() {
        if (versionManagerStorageService == null) {
            versionManagerStorageService = NulsContext.getServiceBean(VersionManagerStorageService.class);
        }
        return versionManagerStorageService;
    }

    private BlockService getBlockService() {
        if (null == blockService) {
            blockService = NulsContext.getServiceBean(BlockService.class);
        }
        return blockService;
    }

    public void processProtocolRollback(BlockHeader blockHeader) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        //临时处理为空的情况，判断为空是由于第一个版本的区块不包含版本信息字段
        if (extendsData.getCurrentVersion() == null) {
            return;
        }
        //首先确定回滚块的协议对象
        ProtocolContainer protocolContainer = NulsVersionManager.getProtocolContainer(extendsData.getCurrentVersion());
        if (protocolContainer != null) {
            if (protocolContainer.getStatus() == ProtocolContainer.VALID && protocolContainer.getEffectiveHeight() < blockHeader.getHeight()) {
                //如果block对应的协议已经生效，并且当前块的高度大于协议生效时的高度，则不需要处理
                return;
            }
            //查找当前版本存储的区块对应索引
            List<Long> blockHeightIndex = getVersionManagerStorageService().getBlockProtocolIndex(protocolContainer.getVersion());
            //如果索引为空或者索引长度为1，回滚后container所有数据重置
            if (blockHeightIndex == null || blockHeightIndex.size() == 1) {
                protocolContainer.reset();
                getVersionManagerStorageService().clearBlockProtocol(blockHeader.getHeight(), protocolContainer.getVersion());
            } else {
                if (blockHeader.getHeight() == blockHeightIndex.get(blockHeightIndex.size() - 1)) {
                    blockHeightIndex.remove(blockHeightIndex.size() - 1);
                    getVersionManagerStorageService().saveBlockProtocolIndex(protocolContainer.getVersion(), blockHeightIndex);
                    getVersionManagerStorageService().deleteBlockProtocol(blockHeader.getHeight());
                    BlockProtocolInfoPo blockProtocolInfoPo = getVersionManagerStorageService().getBlockProtocolInfoPo(blockHeightIndex.get(blockHeightIndex.size() - 1));
                    if (blockProtocolInfoPo != null) {
                        ProtocolTransferTool.copyFromBlockProtocolInfoPo(blockProtocolInfoPo, protocolContainer);
                    }
                    /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
                    Log.info("@@@@@@@@@@@@@@ 回滚结果 统计协议 @@@@@@@@@@@@@@");
                    Log.info("@@@@@@@ 协议version：" + protocolContainer.getVersion());
                    Log.info("@@@@@@@ 回滚块的高度：" + blockHeader.getHeight());
                    Log.info("@@@@@@@ 回滚块的hash：" + blockHeader.getHash());
                    Log.info("@@@@@@@ 回滚后协议块高度：" + blockProtocolInfoPo.getBlockHeight());
                }
            }
            //版本为2的时候的特殊处理
            if (protocolContainer.getVersion() == 2 && protocolContainer.getStatus() != ProtocolContainer.VALID) {
                getVersionManagerStorageService().deleteChangeTxHashBlockHeight();
                NulsContext.CHANGE_HASH_SERIALIZE_HEIGHT = null;
            }
            rollbackMainVersion();
            saveProtocolInfo(protocolContainer);

            Log.info("@@@@@@@ 协议状态：" + protocolContainer.getStatus());
            Log.info("@@@@@@@ 协议延迟块数：" + protocolContainer.getCurrentDelay());
            Log.info("@@@@@@@ 协议轮次：" + protocolContainer.getRoundIndex());
            Log.info("@@@@@@@ 协议AddressSet：" + Arrays.toString(protocolContainer.getAddressSet().toArray()));
            /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
        } else {
            ProtocolTempInfoPo protocolTempInfoPo = getVersionManagerStorageService().getProtocolTempInfoPo(extendsData.getProtocolKey());
            if (protocolTempInfoPo != null) {
                if (protocolTempInfoPo.getStatus() == ProtocolContainer.VALID && protocolTempInfoPo.getEffectiveHeight() < blockHeader.getHeight()) {
                    //如果block对应的协议已经生效，并且当前块的高度大于协议生效时的高度，则不需要处理
                    return;
                }
                //查找当前版本存储的区块对应索引
                List<Long> blockHeightIndex = getVersionManagerStorageService().getBlockTempProtocolIndex(protocolTempInfoPo.getVersion());
                //如果索引为空或者索引长度为1，回滚后container所有数据重置
                if (blockHeightIndex == null || blockHeightIndex.size() == 1) {
                    protocolTempInfoPo.reset();
                    getVersionManagerStorageService().clearTempBlockProtocol(blockHeader.getHeight(), protocolTempInfoPo.getVersion());
                } else {
                    if (blockHeader.getHeight() == blockHeightIndex.get(blockHeightIndex.size() - 1)) {
                        blockHeightIndex.remove(blockHeightIndex.size() - 1);
                        getVersionManagerStorageService().saveTempBlockProtocolIndex(protocolTempInfoPo.getVersion(), blockHeightIndex);
                        getVersionManagerStorageService().deleteBlockTempProtocol(blockHeader.getHeight());

                        BlockProtocolInfoPo blockProtocolInfoPo = getVersionManagerStorageService().getBlockTempProtocolInfoPo(blockHeightIndex.get(blockHeightIndex.size() - 1));
                        if (blockProtocolInfoPo != null) {
                            ProtocolTransferTool.copyFromBlockProtocolTempInfoPo(blockProtocolInfoPo, protocolTempInfoPo);
                        }
                        /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
                        Log.info("@@@@@@@@@@@@@@ 回滚结果 Temp 统计协议 @@@@@@@@@@@@@@");
                        Log.info("@@@@@@@ 协议version：" + protocolTempInfoPo.getVersion());
                        Log.info("@@@@@@@ 回滚块的高度：" + blockHeader.getHeight());
                        Log.info("@@@@@@@ 回滚块的hash：" + blockHeader.getHash());
                        Log.info("@@@@@@@ 回滚后协议块高度：" + blockProtocolInfoPo.getBlockHeight());
                    }

                }
                getVersionManagerStorageService().saveProtocolTempInfoPo(protocolTempInfoPo);
            }
            Log.info("@@@@@@@ 协议状态：" + protocolTempInfoPo.getStatus());
            Log.info("@@@@@@@ 协议延迟块数：" + protocolTempInfoPo.getCurrentDelay());
            Log.info("@@@@@@@ 协议轮次：" + protocolTempInfoPo.getRoundIndex());
            Log.info("@@@@@@@ 协议AddressSet：" + Arrays.toString(protocolTempInfoPo.getAddressSet().toArray()));
            /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
        }
    }

    private void rollbackMainVersion() {
        int validVersion = 1;
        for (ProtocolContainer container : NulsVersionManager.getAllProtocolContainers().values()) {
            if (container.getStatus() == ProtocolContainer.VALID) {
                if (container.getVersion() > validVersion) {
                    validVersion = container.getVersion();
                }
            }
        }
        NulsContext.MAIN_NET_VERSION = validVersion;
        getVersionManagerStorageService().saveMainVersion(validVersion);
    }

    public void clearIncompatibleTx() {
        TransactionQueueStorageService tqs = NulsContext.getServiceBean(TransactionQueueStorageService.class);
        while (tqs.pollTx() != null) {
        }
        TransactionCacheStorageService tcs = NulsContext.getServiceBean(TransactionCacheStorageService.class);
        Transaction tx = null;
        while ((tx = tcs.pollTx()) != null) {
            tcs.removeTx(tx.getHash());
        }
        TxMemoryPool.getInstance().clear();
    }

}
