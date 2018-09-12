package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.container.ChainContainer;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.util.ProtocolTransferTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.AddressTool;
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
        if (extendsData.getCurrentVersion() < 1) {
            return;
        }
        refreshProtocolCoverageRate(extendsData, blockHeader);
        refreshTempProtocolCoverageRate(extendsData, blockHeader);
        //收到的区块版本信息大于当前主网版本信息时，统计覆盖率和延迟块数
        if (extendsData.getCurrentVersion() > NulsVersionManager.getMainVersion()) {
            ProtocolContainer protocolContainer = NulsVersionManager.getProtocolContainer(extendsData.getCurrentVersion());
            if (protocolContainer != null) {
                calcNewProtocolCoverageRate(protocolContainer, extendsData, blockHeader);
            } else {
                //如果没有,则存入临时的协议容器里
                ProtocolTempInfoPo tempInfoPo = getVersionManagerStorageService().getProtocolTempInfoPo(extendsData.getProtocolKey());
                if (tempInfoPo == null) {
                    tempInfoPo = ProtocolTransferTool.createProtocolTempInfoPo(extendsData);
                    tempInfoPo.getAddressSet().add(AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress()));
                }
                calcTempProtocolCoverageRate(tempInfoPo, extendsData, blockHeader);
            }
        }
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
                    if (container.getStatus() == ProtocolContainer.INVALID) {
                        container.setCurrentDelay(0);
                    } else {
                        //已经开始统计延迟块的协议，由于存在新的一轮出块节点地址的变化，因此需要重新计算覆盖率
                        //如果覆盖率未达到，则清零延迟块数量，重新计算
                        Result<BlockHeader> result = getBlockService().getBlockHeader(header.getPreHash());
                        BlockHeader preHeader = result.getData();
                        BlockExtendsData preExtendsData = new BlockExtendsData(preHeader.getExtend());
                        int rate = calcRate(container, preExtendsData);
                        if (rate < container.getPercent()) {
                            container.setCurrentDelay(0);
                            container.setStatus(ProtocolContainer.INVALID);
                        }
                    }
                    container.getAddressSet().clear();
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
                        //已经开始统计延迟块的协议，由于存在新的一轮出块节点地址的变化，因此需要重新计算覆盖率
                        //如果覆盖率未达到，则清零延迟块数量，重新计算
                        Result<BlockHeader> result = getBlockService().getBlockHeader(header.getPreHash());
                        BlockHeader preHeader = result.getData();
                        BlockExtendsData preExtendsData = new BlockExtendsData(preHeader.getExtend());
                        int rate = calcRate(tempInfoPo, preExtendsData);
                        if (rate < tempInfoPo.getPercent()) {
                            tempInfoPo.setCurrentDelay(0);
                            tempInfoPo.setStatus(ProtocolContainer.INVALID);
                        }
                    }
                    tempInfoPo.getAddressSet().clear();
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
        //协议未生效时，判断覆盖率
        if (container.getStatus() == ProtocolContainer.INVALID) {
            //覆盖率达到后，修改状态为延迟锁定中
            int rate = calcRate(container, extendsData);
            if (rate >= container.getPercent()) {
                container.setStatus(ProtocolContainer.DELAY_LOCK);
                container.setCurrentDelay(1);
            }
            saveProtocolInfo(container);

            Log.info("========== 统计协议 ==========");
            Log.info("========== 协议覆盖率：" + rate + " -->>> " + container.getPercent());
            Log.info("========== 协议version：" + container.getVersion());
            Log.info("========== 当前高度：" + blockHeader.getHeight());
            Log.info("========== 当前hash：" + blockHeader.getHash());
            Log.info("========== 协议状态：" + container.getStatus());
            Log.info("========== 协议当前延迟块数：" + container.getCurrentDelay());
            Log.info("========== 协议当前轮次：" + container.getRoundIndex());
            Log.info("========== 协议AddressSet：" + Arrays.toString(container.getAddressSet().toArray()));
        } else if (container.getStatus() == ProtocolContainer.DELAY_LOCK) {
            //当状态为锁定等待延迟高度完成时，首先是从新的一轮开始添加延迟区块数
//            if (container.getCurrentDelay() == 0) {
//                if (container.getRoundIndex() < currentMember.getRoundIndex()) {
//                    container.setCurrentDelay(container.getCurrentDelay() + 1);
//                }
//            } else {
//                container.setCurrentDelay(container.getCurrentDelay() + 1);
//            }
            container.setCurrentDelay(container.getCurrentDelay() + 1);


            //如果已经达到延迟块数，则新协议生效，从下一区块开始，走新协议
            if (container.getCurrentDelay() >= container.getDelay()) {
                container.setStatus(ProtocolContainer.VALID);
                container.setEffectiveHeight(blockHeader.getHeight() + 1);
                saveProtocolInfo(container);
                upgradeProtocol(container);
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
                saveProtocolInfo(container);
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
    }

    private void calcTempProtocolCoverageRate(ProtocolTempInfoPo tempInfoPo, BlockExtendsData extendsData, BlockHeader blockHeader) {
        tempInfoPo.getAddressSet().add(AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress()));
        tempInfoPo.setRoundIndex(extendsData.getRoundIndex());

        MeetingRound currentRound = PocConsensusContext.getChainManager().getMasterChain().getCurrentRound();
        //协议未生效时，判断覆盖率
        if (tempInfoPo.getStatus() == ProtocolContainer.INVALID) {
            //覆盖率达到后，修改状态为延迟锁定中
            int rate = calcRate(tempInfoPo, extendsData);
            if (rate >= tempInfoPo.getPercent()) {
                tempInfoPo.setStatus(ProtocolContainer.DELAY_LOCK);
                tempInfoPo.setCurrentDelay(1);
            }
            Result result = getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
            Log.info("========== 统计Temp协议 未定义 ==========");
            Log.info("========== 协议覆盖率：" + rate + " -->>>" + tempInfoPo.getPercent());
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
//            if (tempInfoPo.getCurrentDelay() == 0) {
//                if (tempInfoPo.getRoundIndex() < currentMember.getRoundIndex()) {
//                    tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() + 1);
//                }
//            } else {
//                tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() + 1);
//            }
            tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() + 1);


            //如果已经达到延迟块数，则新协议生效，从下一区块开始，走新协议
            if (tempInfoPo.getCurrentDelay() >= tempInfoPo.getDelay()) {
                tempInfoPo.setStatus(ProtocolContainer.VALID);
                tempInfoPo.setEffectiveHeight(blockHeader.getHeight() + 1);
                getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
                System.out.println("停止服务！");
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
                    Log.error(">>>>>> The new protocol version has taken effect, this program version is too low has stopped automatically, please update immediately **********");
                    Log.error(">>>>>> The new protocol version has taken effect, this program version is too low has stopped automatically, please update immediately **********");
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
    }


//    /**
//     * 每一轮出块结束后，判断可升级的版本覆盖率，如果未达到覆盖率直接清空覆盖率和已延迟块重新计算
//     */
//    private void checkProtocolCoverageRateWithRoundEnd(MeetingRound currentRound, MeetingMember currentMember) {
//        if (currentMember.getPackingIndexOfRound() == currentRound.getMemberCount()) {
//            for (ProtocolContainer container : NulsVersionManager.getAllProtocolContainers().values()) {
//                if (container.getStatus() != ProtocolContainer.VALID) {
//                    container.getAddressSet().clear();
//                    int rate = calcRate(container);
//                    if (rate < container.getPercent()) {
//                        container.setCurrentDelay(0);
//                        container.setStatus(ProtocolContainer.INVALID);
//                        saveProtocolInfo(container);
//                    }
//                }
//            }
//        }
//    }

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
        int memeberCount = extendsData.getConsensusMemberCount();
        int addressCount = tempInfoPo.getAddressSet().size();
        return calcRate(addressCount, memeberCount);
    }

    private int calcRate(ProtocolContainer protocolContainer, BlockExtendsData extendsData) {
        int memeberCount = extendsData.getConsensusMemberCount();
        int addressCount = protocolContainer.getAddressSet().size();

        return calcRate(addressCount, memeberCount);
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

    /**
     * 回滚协议升级的数据
     *
     * @param blockHeader
     */
    public void processProtoclRollback(BlockHeader blockHeader) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        //临时处理为空的情况，判断为空是由于第一个版本的区块不包含版本信息字段
        if (extendsData.getCurrentVersion() == null) {
            return;
        }
        List<BlockHeader> currentRoundBlockHeaderList = new ArrayList<>();
        //获得当前block相同轮次的所有块
        currentRoundBlockHeaderList = getCurrentRoundBlockHeaderList(currentRoundBlockHeaderList, blockHeader);
        /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@ 同轮次所有区块 @@@@@@@@@@@@@@@@@@@@@@@@");
        for (BlockHeader bh : currentRoundBlockHeaderList) {
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@ 同轮次区块：" + bh.getHeight());
        }
        /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
        //获取当前的Container
        ProtocolContainer protocolContainer = NulsVersionManager.getProtocolContainer(extendsData.getCurrentVersion());
        if (protocolContainer != null) {
            calcProtocolRollbackCoverageRate(protocolContainer, blockHeader, currentRoundBlockHeaderList);
        } else {
            //如果没有,则从临时的协议容器里获取
            ProtocolTempInfoPo tempInfoPo = getVersionManagerStorageService().getProtocolTempInfoPo(extendsData.getProtocolKey());
            if (tempInfoPo != null) {
                calcTempProtocolRollbackCoverageRate(tempInfoPo, blockHeader, currentRoundBlockHeaderList);
            }
        }
    }

    /**
     * 根据区块头获取同轮次所有区块(包含当前块)
     *
     * @param currentRoundBlockHeaderList
     * @param blockHeader
     */
    private List<BlockHeader> getCurrentRoundBlockHeaderList(List<BlockHeader> currentRoundBlockHeaderList, BlockHeader blockHeader) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        BlockHeader preBlockHeader = getBlockService().getBlock(blockHeader.getPreHash()).getData().getHeader();
        BlockExtendsData preExtendsData = new BlockExtendsData(preBlockHeader.getExtend());
        /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
        System.out.println("@@@@@@@@@@@@@@ 轮次比较 获取同轮次区块区块 @@@@@@@@@@@@@@");
        System.out.println("extendsData.getRoundIndex(): " + extendsData.getRoundIndex());
        System.out.println("preExtendsData.getRoundIndex(): " + preExtendsData.getRoundIndex());
        /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
        if (extendsData.getRoundIndex() > preExtendsData.getRoundIndex()) {
            return currentRoundBlockHeaderList;
        } else {
            currentRoundBlockHeaderList.add(blockHeader);
            return getCurrentRoundBlockHeaderList(currentRoundBlockHeaderList, preBlockHeader);
        }
    }

    /**
     * 递归统计之前轮次的覆盖率和状态，获取回滚当前块后的延迟块数
     *
     * @param blockHeader
     * @param rollbackRoundIndex 回滚块的轮次
     * @return
     */
    private long getRollbackCurrentDelay(ProtocolContainer protocolContainer, BlockHeader blockHeader, long rollbackRoundIndex, long currentDelay) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        List<BlockHeader> list = new ArrayList<>();
        getCurrentRoundBlockHeaderList(list, blockHeader);
        //当前轮次可能没有其他块，则需要拿到再前一轮次的所有块
        if(list.size() <= 0 && rollbackRoundIndex == extendsData.getRoundIndex() + 2){
            return currentDelay;
        } else {
            //遍历所有块
            //首先统计出块地址和覆盖率
            //达到条件，统计延迟块数记录协议容器
            Set<String> addressSet = getRoundPackingAddress(list);
            //计算覆盖率
            BlockHeader first = list.get(0);
            BlockExtendsData ed = new BlockExtendsData(first.getExtend());
            int rate = calcRate(addressSet.size(), ed.getConsensusMemberCount());
            if (rate >= protocolContainer.getPercent()) {
                for (BlockHeader bh : list) {
                    BlockExtendsData extendsDataBH = new BlockExtendsData(bh.getExtend());
                    if (extendsDataBH.getCurrentVersion() > NulsContext.MAIN_NET_VERSION
                            && extendsDataBH.getCurrentVersion().intValue() == protocolContainer.getVersion().intValue()) {
                        currentDelay += 1;
                    }
                }
                //本轮第一块的前一块为上一轮的最后一块
                BlockHeader preRoundBlockHeader = getBlockService().getBlock(first.getPreHash()).getData().getHeader();
                //当前轮次满足覆盖率，则继续计算前一轮的数据
                return getRollbackCurrentDelay(protocolContainer, preRoundBlockHeader, rollbackRoundIndex, currentDelay);
            }
            return currentDelay;
        }
    }

    /**
     * 递归统计之前轮次的覆盖率和状态，获取回滚当前块后的延迟块数
     *
     * @param blockHeader
     * @param rollbackRoundIndex 回滚块的轮次
     * @return
     */
    private long getRollbackCurrentDelayTemp(ProtocolTempInfoPo tempInfoPo, BlockHeader blockHeader, long rollbackRoundIndex, long currentDelay) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        List<BlockHeader> list = new ArrayList<>();
        getCurrentRoundBlockHeaderList(list, blockHeader);
        //当前轮次可能没有其他块，则需要拿到再前一轮次的所有块
        if (list.size() <= 0 && rollbackRoundIndex == extendsData.getRoundIndex() + 2) {
            return 0;
        } else {
            //遍历所有块
            //首先统计出块地址和覆盖率
            //达到条件，统计延迟块数记录协议容器
            Set<String> addressSetTemp = getRoundPackingAddressTemp(list);
            //计算覆盖率
            BlockHeader first = list.get(0);
            BlockExtendsData ed = new BlockExtendsData(first.getExtend());
            int rate = calcRate(addressSetTemp.size(), ed.getConsensusMemberCount());
            if (rate >= tempInfoPo.getPercent()) {
                for (BlockHeader bh : list) {
                    BlockExtendsData extendsDataBH = new BlockExtendsData(bh.getExtend());
                    if (extendsDataBH.getCurrentVersion() > NulsContext.MAIN_NET_VERSION
                            && extendsDataBH.getCurrentVersion().intValue() == tempInfoPo.getVersion()) {
                        currentDelay += 1;
                    }
                }
                //本轮第一块的前一块为上一轮的最后一块
                BlockHeader preRoundBlockHeader = getBlockService().getBlock(first.getPreHash()).getData().getHeader();
                //当前轮次满足覆盖率，则继续计算前一轮的数据
                return getRollbackCurrentDelayTemp(tempInfoPo, preRoundBlockHeader, rollbackRoundIndex, currentDelay);
            }
            return currentDelay;

        }
    }

    private void calcProtocolRollbackCoverageRateTest2(ProtocolContainer protocolContainer, BlockHeader bh, BlockExtendsData extendsDataBH) {


    }

    private void calcTempProtocolRollbackCoverageRateTest2(ProtocolTempInfoPo tempInfoPo, BlockHeader bh, BlockExtendsData extendsDataBH) {

    }


    /**
     * 获取一轮中的出块地址
     */
    private Set<String> getRoundPackingAddress(List<BlockHeader> list) {
        Set<String> addressSet = new HashSet<>();
        for (BlockHeader bh : list) {
            BlockExtendsData extendsDataBH = new BlockExtendsData(bh.getExtend());
            //如果该块版本大于当前主网版本说明是需要统计的块
            if (extendsDataBH.getCurrentVersion() > NulsContext.MAIN_NET_VERSION) {
                //获取当前的Container
                ProtocolContainer protocolContainer = NulsVersionManager.getProtocolContainer(extendsDataBH.getCurrentVersion());
                //判断并获取块对应的协议对应的
                if (protocolContainer != null) {
                    if (extendsDataBH.getCurrentVersion().intValue() == protocolContainer.getVersion().intValue()) {
                        addressSet.add(AddressTool.getStringAddressByBytes(bh.getPackingAddress()));
                    }
                }
            }
        }
        return addressSet;
    }

    /**
     * 获取一轮中临时协议的出块地址
     *
     * @param list
     * @return
     */
    private Set<String> getRoundPackingAddressTemp(List<BlockHeader> list) {
        Set<String> addressSetTemp = new HashSet<>();
        for (BlockHeader bh : list) {
            BlockExtendsData extendsDataBH = new BlockExtendsData(bh.getExtend());
            //如果该块版本大于当前主网版本说明是需要统计的块
            if (extendsDataBH.getCurrentVersion() > NulsContext.MAIN_NET_VERSION) {
                //如果没有,则从临时的协议容器里获取
                ProtocolTempInfoPo tempInfoPo = getVersionManagerStorageService().getProtocolTempInfoPo(extendsDataBH.getProtocolKey());
                if (tempInfoPo != null) {
                    if (extendsDataBH.getCurrentVersion().intValue() == tempInfoPo.getVersion()) {
                        addressSetTemp.add(AddressTool.getStringAddressByBytes(bh.getPackingAddress()));
                    }
                }

            }
        }
        return addressSetTemp;
    }

    private void calcProtocolRollbackCoverageRate(ProtocolContainer protocolContainer, BlockHeader blockHeader, List<BlockHeader> currentRoundBlockHeaderList) {

        if (protocolContainer.getStatus() == ProtocolContainer.VALID && protocolContainer.getEffectiveHeight() < blockHeader.getHeight()) {
            //如果block对应的协议已经生效，并且当前块的高度大于协议生效时的高度，则不需要处理
            return;
        }
        //通过高度判断该block否是恰好是一个协议生效的块
        if (null != protocolContainer.getEffectiveHeight() && protocolContainer.getEffectiveHeight() == blockHeader.getHeight()) {
            protocolContainer.setStatus(ProtocolContainer.DELAY_LOCK);
            protocolContainer.setEffectiveHeight(null);

            BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
            long currentDelay = getRollbackCurrentDelay(protocolContainer, blockHeader,extendsData.getRoundIndex(), 0);
            protocolContainer.setCurrentDelay(currentDelay - 1);
//            protocolContainer.setCurrentDelay(protocolContainer.getCurrentDelay() - 1);
            //回退协议
            NulsContext.MAIN_NET_VERSION = NulsContext.MAIN_NET_VERSION - 1 < 1 ? 1 : NulsContext.MAIN_NET_VERSION - 1;
        } else if (protocolContainer.getStatus() == ProtocolContainer.DELAY_LOCK) {
            //如果状态为“协议覆盖率已达到，处于延迟生效”时，判断延迟块处于0时，则回退至协议未生效状态，否则直接回滚延迟块
            if (protocolContainer.getCurrentDelay() == 0) {
                protocolContainer.setStatus(ProtocolContainer.INVALID);
            } else {
                BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
                long currentDelay = getRollbackCurrentDelay(protocolContainer, blockHeader,extendsData.getRoundIndex(), 0);
                protocolContainer.setCurrentDelay(currentDelay - 1);
            }
        }
        BlockHeader preBlockHeader = getBlockService().getBlock(blockHeader.getPreHash()).getData().getHeader();
        BlockExtendsData preExtendsData = new BlockExtendsData(preBlockHeader.getExtend());
        //轮次回滚到前一个块的轮次
        protocolContainer.setRoundIndex(preExtendsData.getRoundIndex());
        //根据当前轮次重置addressSet
        Set<String> addressSet = new HashSet<>();
        for (BlockHeader bh : currentRoundBlockHeaderList) {
            BlockExtendsData eData = new BlockExtendsData(bh.getExtend());
            if (eData.getCurrentVersion().intValue() == protocolContainer.getVersion().intValue()) {
                addressSet.add(AddressTool.getStringAddressByBytes(bh.getPackingAddress()));
            }
        }
        protocolContainer.setAddressSet(addressSet);
        saveProtocolInfo(protocolContainer);
        Log.info("@@@@@@@@@@@@@@ 回滚 统计协议 @@@@@@@@@@@@@@");
        Log.info("@@@@@@@ 协议version：" + protocolContainer.getVersion());
        Log.info("@@@@@@@ 当前高度：" + blockHeader.getHeight());
        Log.info("@@@@@@@ 当前hash：" + blockHeader.getHash());
        Log.info("@@@@@@@ 协议状态：" + protocolContainer.getStatus());
        Log.info("@@@@@@@ 协议当前延迟块数：" + protocolContainer.getCurrentDelay());
        Log.info("@@@@@@@ 协议当前轮次：" + protocolContainer.getRoundIndex());
        Log.info("@@@@@@@ 协议AddressSet：" + Arrays.toString(protocolContainer.getAddressSet().toArray()));
    }

    private void calcTempProtocolRollbackCoverageRate(ProtocolTempInfoPo tempInfoPo, BlockHeader blockHeader, List<BlockHeader> currentRoundBlockHeaderList) {
        if (tempInfoPo.getStatus() == ProtocolContainer.VALID && tempInfoPo.getEffectiveHeight() < blockHeader.getHeight()) {
            //如果block对应的协议已经生效，并且当前块的高度大于协议生效时的高度，则不需要处理
            return;
        }

        if (tempInfoPo.getEffectiveHeight() == blockHeader.getHeight()) {
            tempInfoPo.setStatus(ProtocolContainer.DELAY_LOCK);
            tempInfoPo.setEffectiveHeight(null);
            tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() - 1);

        } else if (tempInfoPo.getStatus() == ProtocolContainer.DELAY_LOCK) {
            //如果状态为“协议覆盖率已达到，处于延迟生效”时，判断延迟块处于0时，则回退至协议未生效状态，否则直接回滚延迟块
            if (tempInfoPo.getCurrentDelay() == 0) {
                tempInfoPo.setStatus(ProtocolContainer.INVALID);
            } else {
                tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() - 1);
            }
        }
        BlockHeader preBlockHeader = getBlockService().getBlock(blockHeader.getPreHash()).getData().getHeader();
        BlockExtendsData preExtendsData = new BlockExtendsData(preBlockHeader.getExtend());
        //轮次回滚到前一个块的轮次
        tempInfoPo.setRoundIndex(preExtendsData.getRoundIndex());
        //根据当前轮次重置addressSet
        Set<String> addressSet = new HashSet<>();
        for (BlockHeader bh : currentRoundBlockHeaderList) {
            BlockExtendsData eData = new BlockExtendsData(bh.getExtend());
            if (eData.getCurrentVersion().intValue() == tempInfoPo.getVersion()) {
                addressSet.add(AddressTool.getStringAddressByBytes(bh.getPackingAddress()));
            }
        }
        tempInfoPo.setAddressSet(addressSet);
        getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
        Log.info("@@@@@@@@@@@@@@ 回滚 Temp 统计协议 @@@@@@@@@@@@@@");
        Log.info("@@@@@@@ 协议version：" + tempInfoPo.getVersion());
        Log.info("@@@@@@@ 当前高度：" + blockHeader.getHeight());
        Log.info("@@@@@@@ 当前hash：" + blockHeader.getHash());
        Log.info("@@@@@@@ 协议状态：" + tempInfoPo.getStatus());
        Log.info("@@@@@@@ 协议当前延迟块数：" + tempInfoPo.getCurrentDelay());
        Log.info("@@@@@@@ 协议当前轮次：" + tempInfoPo.getRoundIndex());
        Log.info("@@@@@@@ 协议AddressSet：" + Arrays.toString(tempInfoPo.getAddressSet().toArray()));
    }


}
