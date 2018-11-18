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
import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.storage.service.TransactionCacheStorageService;
import io.nuls.consensus.poc.storage.service.TransactionQueueStorageService;
import io.nuls.consensus.poc.util.ProtocolTransferTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.base.version.NulsVersionManager;
import io.nuls.protocol.base.version.ProtocolContainer;
import io.nuls.protocol.service.BlockService;
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
     * 版本升级
     * 升级规则：
     * 1.通过block.extend字段获取当前出块人的版本号
     * 2.如果一个节点出块的版本号为N，之后若未收到过该节点继续出块，视为该出块节点版本号为N，
     * 3.记录每一轮都有哪些节点出过哪些个版本的块，若某个版本达到覆盖率后，从下一轮开始进入延迟块数的累计
     * 4.进入延迟块数累计的版本，若下一轮覆盖率未达标，则延迟块数清理，待重新达标后进入延迟块的统计
     *
     * @param blockHeader
     */
    public void processProtocolUpGrade(BlockHeader blockHeader) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        String packingAddress = blockHeader.getPackingAddressStr();
        MeetingRound currentRound = PocConsensusContext.getChainManager().getMasterChain().getRoundManager().getRoundByIndex(extendsData.getRoundIndex());

        //临时处理为空的情况，为空是由于第一个版本的区块不包含版本信息字段
        if (extendsData.getCurrentVersion() == null) {
            extendsData.setCurrentVersion(1);
        }
        //处理有人发送错误的版本格式情况
        if (extendsData.getCurrentVersion() < 1) {
            return;
        }

        calculateCoverageEveryRound(blockHeader, extendsData, currentRound);
        containerRemoveAddress(packingAddress);

        //收到的新块版本号大于当前主网版本时
        if (extendsData.getCurrentVersion() > NulsContext.MAIN_NET_VERSION) {
            //能找到本地对应的版本号配置，走正常升级流程
            ProtocolContainer container = NulsVersionManager.getProtocolContainer(extendsData.getCurrentVersion());
            if (container != null) {
                processHigherVersion(blockHeader, extendsData, packingAddress, container);
            } else {
                //不能找到配置，说明当前也许有更高版本的配置，走临时保存流程
                ProtocolTempInfoPo tempInfoPo = NulsVersionManager.getTempProtocolContainer(extendsData.getProtocolKey());
                if (tempInfoPo == null) {
                    tempInfoPo = ProtocolTransferTool.createProtocolTempInfoPo(extendsData);
                    NulsVersionManager.addTempProtocolContainer(tempInfoPo);
                }
                processTempHigherVersion(blockHeader, extendsData, packingAddress, tempInfoPo);
            }
        } else {
            calcDelay(blockHeader, extendsData);
            calcTempDelay(blockHeader, extendsData);
        }

        saveProtocol(blockHeader);
    }

    /**
     * 处理高版本协议升级流程
     *
     * @param blockHeader
     * @param extendsData
     * @param packingAddress
     * @param container
     */
    private void processHigherVersion(BlockHeader blockHeader, BlockExtendsData extendsData, String packingAddress, ProtocolContainer container) {
        container.getAddressSet().add(packingAddress);
        container.setRoundIndex(extendsData.getRoundIndex());

//        Log.info("========== 统计协议 ==========");
//        Log.info("========== 上一轮协议覆盖率：" + container.getCurrentPercent() + " -->>> " + container.getPercent());

        //当进入延迟升级时，需要累计延迟块数
        if (container.getStatus() == ProtocolContainer.DELAY_LOCK) {
            container.setCurrentDelay(container.getCurrentDelay() + 1);
            //当延迟块数达到升级条件时，改变协议状态为生效
            if (container.getCurrentDelay() >= container.getDelay()) {
                upgradeProtocol(container, blockHeader);
            }
        }
//        Log.info("========== 协议version：" + container.getVersion());
//        Log.info("========== 当前高度：" + blockHeader.getHeight());
//        Log.info("========== 当前hash：" + blockHeader.getHash());
//        Log.info("========== 协议状态：" + container.getStatus());
        if (container.getStatus() == ProtocolContainer.VALID) {
            Log.info("********** 协议生效了！！！！！！！！！ **********");
            Log.info("********** 协议生效了！！！！！！！！！ **********");
            Log.info("********** 协议生效了！！！！！！！！！ **********");
            Log.info("********** 当前协议生效下一块开始执行新协议 **********");
            Log.info("********** 生效协议version：" + container.getVersion());
            Log.info("********** 生效协议高度：" + container.getEffectiveHeight());
        }
//        Log.info("========== 协议当前延迟块数：" + container.getCurrentDelay());
//        Log.info("========== 协议当前轮次：" + container.getRoundIndex());
//        Log.info("========== 协议当前轮出块节点数：" + extendsData.getConsensusMemberCount());
//        Log.info("========== 协议AddressSet：" + Arrays.toString(container.getAddressSet().toArray()));
    }

    /**
     * 处理本地钱包不存在的更高版本的升级流程
     *
     * @param blockHeader
     * @param extendsData
     * @param packingAddress
     * @param tempInfoPo
     */
    private void processTempHigherVersion(BlockHeader blockHeader, BlockExtendsData extendsData, String packingAddress, ProtocolTempInfoPo tempInfoPo) {
        tempInfoPo.getAddressSet().add(packingAddress);
        tempInfoPo.setRoundIndex(extendsData.getRoundIndex());
        //当进入延迟升级时，需要累计延迟块数
//        Log.info("========== 统计Temp协议 未定义 ==========");
//        Log.info("========== Temp上一轮协议覆盖率：" + tempInfoPo.getCurrentPercent() + " -->>> " + tempInfoPo.getPercent());
//        Log.info("========== Temp协议version：" + tempInfoPo.getVersion());
//        Log.info("========== Temp当前高度：" + blockHeader.getHeight());
//        Log.info("========== Temp当前hash：" + blockHeader.getHash());
//        Log.info("========== Temp协议状态：" + tempInfoPo.getStatus());
//        Log.info("========== Temp协议当前轮次：" + tempInfoPo.getRoundIndex());
//        Log.info("========== Temp协议当前轮出块节点数：" + extendsData.getConsensusMemberCount());
//        Log.info("========== Temp协议AddressSet：" + Arrays.toString(tempInfoPo.getAddressSet().toArray()));
        if (tempInfoPo.getStatus() == ProtocolContainer.DELAY_LOCK) {
            tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() + 1);
            //当延迟块数达到升级条件时，改变协议状态为生效
            if (tempInfoPo.getCurrentDelay() >= tempInfoPo.getDelay()) {
                upgradeTempProtocol(tempInfoPo, blockHeader);
            }
        }
        Log.info("========== Temp协议当前延迟块数：" + tempInfoPo.getCurrentDelay());
    }

    /**
     * 协议升级
     */
    private void upgradeProtocol(ProtocolContainer container, BlockHeader blockHeader) {
        container.setStatus(ProtocolContainer.VALID);
        container.setEffectiveHeight(blockHeader.getHeight() + 1);
        if (container.getVersion() > NulsContext.MAIN_NET_VERSION) {
            NulsContext.MAIN_NET_VERSION = container.getVersion();
            getVersionManagerStorageService().saveMainVersion(container.getVersion());
        }
        if (container.getVersion() == 2) {
            getVersionManagerStorageService().saveChangeTxHashBlockHeight(container.getEffectiveHeight());
            NulsContext.CHANGE_HASH_SERIALIZE_HEIGHT = container.getEffectiveHeight();
        }
        clearIncompatibleTx();
    }

    /**
     * 本地不存在的更高版本的协议升级
     *
     * @param tempInfoPo
     * @param blockHeader
     */
    private void upgradeTempProtocol(ProtocolTempInfoPo tempInfoPo, BlockHeader blockHeader) {
        tempInfoPo.setStatus(ProtocolContainer.VALID);
        tempInfoPo.setEffectiveHeight(blockHeader.getHeight() + 1);
        Log.info("========== Temp协议当前延迟块数：" + tempInfoPo.getCurrentDelay());
        Log.info("********** Temp协议生效了！！！！！！！！！ **********");
        Log.info("********** Temp协议生效了！！！！！！！！！ **********");
        Log.info("********** Temp协议生效了！！！！！！！！！ **********");
        Log.info("********** Temp当前协议生效下一块开始执行新协议 **********");
        Log.info("********** Temp生效协议version：" + tempInfoPo.getVersion());
        Log.info("********** Temp生效协议高度：" + tempInfoPo.getEffectiveHeight());
        //如果是linux系统则立即停止，否则将强制更新标志设为true，由钱包提示
        if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("LINUX") != -1) {
            saveProtocol(blockHeader);
            Log.error(">>>>>> The new protocol version has taken effect, this program version is too low has stopped automatically, please upgrade immediately **********");
            Log.error(">>>>>> The new protocol version has taken effect, this program version is too low has stopped automatically, please upgrade immediately **********");
            NulsContext.getInstance().exit(1);
            return;
        } else {
            NulsContext.mastUpGrade = true;
        }
    }

    private void containerRemoveAddress(String packingAddress) {
        for (ProtocolContainer container : NulsVersionManager.getAllProtocolContainers().values()) {
            if (container.getStatus() == ProtocolContainer.VALID) {
                continue;
            }
            container.getAddressSet().remove(packingAddress);
        }
        for (ProtocolTempInfoPo tempInfoPo : getVersionManagerStorageService().getProtocolTempMap().values()) {
            if (tempInfoPo.getStatus() == ProtocolContainer.VALID) {
                continue;
            }
            tempInfoPo.getAddressSet().remove(packingAddress);
        }
    }

    /**
     * 每轮重新计算覆盖率
     */
    private void calculateCoverageEveryRound(BlockHeader header, BlockExtendsData extendsData, MeetingRound currentRound) {
        Set<String> memberAddressSet = new HashSet<>();
        if (currentRound != null && currentRound.getMemberList() != null) {
            List<MeetingMember> memberList = currentRound.getMemberList();
            for (MeetingMember member : memberList) {
                memberAddressSet.add(member.getPackingAddressStr());
            }
        }
        calculateProtocols(extendsData, header, memberAddressSet);
        calculateTempProtocols(extendsData, header, memberAddressSet);
    }

    private void calculateProtocols(BlockExtendsData extendsData, BlockHeader header, Set<String> memberAddressSet) {
        for (ProtocolContainer container : NulsVersionManager.getAllProtocolContainers().values()) {
            //如果容器的轮次小于当前出块节点的轮次，说明是新的一轮开始
            if (container.getStatus() != ProtocolContainer.VALID && container.getRoundIndex() < extendsData.getRoundIndex()) {
                //获取上一个块，得到上一轮的共识节点数量
                Result<BlockHeader> result = getBlockService().getBlockHeader(header.getPreHash());
                BlockHeader preHeader = result.getData();
                BlockExtendsData preExtendsData = new BlockExtendsData(preHeader.getExtend());
                //计算上一轮的覆盖率
                int rate = calcRate(container, preExtendsData);
                container.setCurrentPercent(rate);

                if (rate < container.getPercent()) {
                    container.setStatus(ProtocolContainer.INVALID);
                    container.setCurrentDelay(0);
                } else {
                    container.setStatus(ProtocolContainer.DELAY_LOCK);
                }

                //新的轮次开始时，也许会有节点时上一轮已经退出的，因此在这一轮里要去掉
                Iterator<String> iterator = container.getAddressSet().iterator();
                if(iterator != null) {
                    while (iterator.hasNext()) {
                        String address = iterator.next();
                        if (!memberAddressSet.contains(address)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private void calculateTempProtocols(BlockExtendsData extendsData, BlockHeader header, Set<String> memberAddressSet) {
        for (ProtocolTempInfoPo tempInfoPo : NulsVersionManager.getTempProtocolContainers().values()) {
            //如果容器的轮次小于当前出块节点的轮次，说明是新的一轮开始
            if (tempInfoPo.getStatus() != ProtocolContainer.VALID && tempInfoPo.getRoundIndex() < extendsData.getRoundIndex()) {
                //获取上一个块，得到上一轮的共识节点数量
                Result<BlockHeader> result = getBlockService().getBlockHeader(header.getPreHash());
                BlockHeader preHeader = result.getData();
                BlockExtendsData preExtendsData = new BlockExtendsData(preHeader.getExtend());
                //计算上一轮的覆盖率
                int rate = calcRate(tempInfoPo, preExtendsData);
                tempInfoPo.setCurrentPercent(rate);

                if (rate < tempInfoPo.getPercent()) {
                    tempInfoPo.setStatus(ProtocolContainer.INVALID);
                    tempInfoPo.setCurrentDelay(0);
                } else {
                    tempInfoPo.setStatus(ProtocolContainer.DELAY_LOCK);
                }

                //新的轮次开始时，也许会有节点时上一轮已经退出的，因此在这一轮里要去掉
                Iterator<String> iterator = tempInfoPo.getAddressSet().iterator();
                while (iterator.hasNext()) {
                    String address = iterator.next();
                    if (!memberAddressSet.contains(address)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void calcDelay(BlockHeader blockHeader, BlockExtendsData extendsData) {
        for (ProtocolContainer container : NulsVersionManager.getAllProtocolContainers().values()) {
            if (container.getStatus() != ProtocolContainer.DELAY_LOCK || container.getVersion() < NulsContext.MAIN_NET_VERSION) {
                continue;
            }
            container.setRoundIndex(extendsData.getRoundIndex());
            container.setCurrentDelay(container.getCurrentDelay() + 1);
            //当延迟块数达到升级条件时，改变协议状态为生效
            if (container.getCurrentDelay() >= container.getDelay()) {
                upgradeProtocol(container, blockHeader);
                Log.info("********** 协议生效了！！！！！！！！！ **********");
                Log.info("********** 协议生效了！！！！！！！！！ **********");
                Log.info("********** 协议生效了！！！！！！！！！ **********");
                Log.info("********** 当前协议生效下一块开始执行新协议 **********");
                Log.info("********** 生效协议version：" + container.getVersion());
                Log.info("********** 生效协议高度：" + container.getEffectiveHeight());
            }
        }
    }

    private void calcTempDelay(BlockHeader blockHeader, BlockExtendsData extendsData) {
        for (ProtocolTempInfoPo tempInfoPo : NulsVersionManager.getTempProtocolContainers().values()) {
            if (tempInfoPo.getStatus() != ProtocolContainer.DELAY_LOCK || tempInfoPo.getVersion() < NulsContext.MAIN_NET_VERSION) {
                continue;
            }
            tempInfoPo.setRoundIndex(extendsData.getRoundIndex());
            tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() + 1);
            //当延迟块数达到升级条件时，改变协议状态为生效
            if (tempInfoPo.getCurrentDelay() >= tempInfoPo.getDelay()) {
                upgradeTempProtocol(tempInfoPo, blockHeader);
            }
        }
    }

    /**
     * 每个块处理之后，存储当前所有协议容器信息
     */
    private void saveProtocol(BlockHeader blockHeader) {
        saveContainerList(blockHeader.getHeight());
        saveTempContainerList(blockHeader.getHeight());
        getVersionManagerStorageService().saveConsensusVersionHeight(blockHeader.getHeight());
    }

    private void saveContainerList(long versionHeight) {
        List<ProtocolInfoPo> infoPoList = new ArrayList<>();
        for (ProtocolContainer container : NulsVersionManager.getAllProtocolContainers().values()) {
            ProtocolInfoPo infoPo = ProtocolTransferTool.toProtocolInfoPo(container);
            infoPoList.add(infoPo);
        }
        getVersionManagerStorageService().saveProtocolInfoList(versionHeight, infoPoList);
    }

    private void saveTempContainerList(long versionHeight) {
        if (NulsVersionManager.getTempProtocolContainers().size() > 0) {
            List<ProtocolTempInfoPo> tempInfoPoList = new ArrayList<>(NulsVersionManager.getTempProtocolContainers().values());
            getVersionManagerStorageService().saveProtocolTempInfoList(versionHeight, tempInfoPoList);
        }
    }

    private int calcRate(ProtocolContainer protocolContainer, BlockExtendsData extendsData) {
        int memberCount = extendsData.getConsensusMemberCount();
        int addressCount = protocolContainer.getAddressSet().size();
        BigDecimal b1 = new BigDecimal(addressCount);
        BigDecimal b2 = new BigDecimal(memberCount);
        int rate = b1.divide(b2, 2, BigDecimal.ROUND_DOWN).movePointRight(2).intValue();
        return rate;
    }

    private int calcRate(ProtocolTempInfoPo tempInfoPo, BlockExtendsData extendsData) {
        int memberCount = extendsData.getConsensusMemberCount();
        int addressCount = tempInfoPo.getAddressSet().size();
        BigDecimal b1 = new BigDecimal(addressCount);
        BigDecimal b2 = new BigDecimal(memberCount);
        int rate = b1.divide(b2, 2, BigDecimal.ROUND_DOWN).movePointRight(2).intValue();
        return rate;
    }

    /**
     * 区块回滚的处理
     * 只需要把上个块存储的协议相关统计信息拿到就可以
     *
     * @param blockHeader
     */
    public void processProtocolRollback(BlockHeader blockHeader) {
        long preHeight = blockHeader.getHeight() - 1;
        if (preHeight < PocConsensusConstant.POC_START_PROCESS_PROTOCOL_VERSION_HEIGHT) {
            return;
        }
        //删除当前块已存储的相关协议统计信息
        getVersionManagerStorageService().saveConsensusVersionHeight(preHeight);
        getVersionManagerStorageService().removeProtocolInfoList(blockHeader.getHeight());
        getVersionManagerStorageService().removeProtocolTempInfoList(blockHeader.getHeight());

        //重新获取上一个块的统计信息，下面两个方法的顺序一定不能改
        tempContainerRollBack(blockHeader, preHeight);
        containerRollBack(preHeight);
    }

    private void containerRollBack(long versionHeight) {
        List<ProtocolInfoPo> infoPoList = getVersionManagerStorageService().getProtocolInfoList(versionHeight);
        //用数据库已保存的每个区块统计的协议信息覆盖容器
        int version = NulsContext.MAIN_NET_VERSION;
        boolean b = false;
        if (infoPoList != null && !infoPoList.isEmpty()) {
            for (ProtocolInfoPo infoPo : infoPoList) {
                ProtocolContainer container = NulsVersionManager.getProtocolContainer(infoPo.getVersion());
                copyProtocolFromInfoPo(container, infoPo);
                //获取未升级版本最高的版本
                if (container.getStatus() != ProtocolContainer.VALID && version >= container.getVersion()) {
                    version = container.getVersion() - 1;
                    b = true;
                }
            }
        }
        //如果当前主网版本已经高于未升级的最高协议版本，则说明回滚的时候，协议降级，需要做降级处理
        if (NulsContext.MAIN_NET_VERSION > version && b == true) {
            NulsContext.MAIN_NET_VERSION = version;
            getVersionManagerStorageService().saveMainVersion(version);
            if (version == 1) {
                getVersionManagerStorageService().deleteChangeTxHashBlockHeight();
            }
        }
    }

    private void tempContainerRollBack(BlockHeader blockHeader, long versionHeight) {
        boolean reProcess = false;
        List<ProtocolTempInfoPo> tempInfoPoList = getVersionManagerStorageService().getProtocolTempInfoList(versionHeight);
        if (tempInfoPoList == null || tempInfoPoList.isEmpty()) {
            return;
        }
        //回滚的时候 ，有可能上次记录在temp里的数据，这次已经有对应的container容器了
        //遇到这个情况的时候，需要复制到container里，重新保存到数据库后，再递归判断是否还有
        for (int i = tempInfoPoList.size() - 1; i >= 0; i--) {
            ProtocolTempInfoPo tempInfoPo = tempInfoPoList.get(i);
            ProtocolContainer container = NulsVersionManager.getProtocolContainer(tempInfoPo.getVersion(),tempInfoPo.getPercent(), tempInfoPo.getDelay());
            if (container != null) {
                copyProtocolFromTempInfoPo(container, tempInfoPo);
                getVersionManagerStorageService().removeProtocolTempInfoList(versionHeight);
                saveProtocol(blockHeader);
                reProcess = true;
                break;
            }
        }
        if (reProcess) {
            tempContainerRollBack(blockHeader, versionHeight);
        } else {
            Map<String, ProtocolTempInfoPo> tempContainers = new HashMap<>();
            for (ProtocolTempInfoPo tempInfoPo : tempInfoPoList) {
                tempContainers.put(tempInfoPo.getProtocolKey(), tempInfoPo);
            }
            NulsVersionManager.setTempProtocolContainers(tempContainers);
        }
    }


    public static void copyProtocolFromInfoPo(ProtocolContainer container, ProtocolInfoPo infoPo) {
        container.setCurrentDelay(infoPo.getCurrentDelay());
        container.setCurrentPercent(infoPo.getCurrentPercent());
        container.setAddressSet(infoPo.getAddressSet());
        container.setStatus(infoPo.getStatus());
        container.setRoundIndex(infoPo.getRoundIndex());
        container.setEffectiveHeight(infoPo.getEffectiveHeight());
        container.setPrePercent(infoPo.getPrePercent());
    }

    public static void copyProtocolFromTempInfoPo(ProtocolContainer container, ProtocolTempInfoPo infoPo) {
        container.setCurrentDelay(infoPo.getCurrentDelay());
        container.setCurrentPercent(infoPo.getCurrentPercent());
        container.setAddressSet(infoPo.getAddressSet());
        container.setStatus(infoPo.getStatus());
        container.setRoundIndex(infoPo.getRoundIndex());
        container.setEffectiveHeight(infoPo.getEffectiveHeight());
        container.setPrePercent(infoPo.getPrePercent());
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
}
