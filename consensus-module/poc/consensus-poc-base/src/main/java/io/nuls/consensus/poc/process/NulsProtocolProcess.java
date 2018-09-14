package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.util.ProtocolTransferTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
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
        saveBLockProtocolInfo(blockHeader, container);
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
            getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
            saveBLockTempProtocolInfo(blockHeader, tempInfoPo);
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
                saveBLockTempProtocolInfo(blockHeader, tempInfoPo);
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
                saveBLockTempProtocolInfo(blockHeader, tempInfoPo);
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

    private void saveBLockProtocolInfo(BlockHeader blockHeader, ProtocolContainer container) {
        BlockProtocolInfoPo infoPo = ProtocolTransferTool.toBlockProtocolInfoPo(blockHeader, container);
        getVersionManagerStorageService().saveBlockProtocolInfoPo(infoPo);
    }

    private void saveBLockTempProtocolInfo(BlockHeader blockHeader, ProtocolTempInfoPo tempInfoPo) {
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
                        getVersionManagerStorageService().saveTempBlockProtocolIndex(protocolContainer.getVersion(), blockHeightIndex);
                        getVersionManagerStorageService().deleteBlockTempProtocol(blockHeader.getHeight());

                        BlockProtocolInfoPo blockProtocolInfoPo = getVersionManagerStorageService().getBlockTempProtocolInfoPo(blockHeightIndex.get(blockHeightIndex.size() - 1));
                        if (blockProtocolInfoPo != null) {
                            ProtocolTransferTool.copyFromBlockProtocolTempInfoPo(blockProtocolInfoPo, protocolTempInfoPo);
                        }
                        /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
                        Log.info("@@@@@@@@@@@@@@ 回滚结果 统计协议 @@@@@@@@@@@@@@");
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


//    /**
//     * 回滚协议升级的数据
//     *
//     * @param blockHeader
//     */
//    public void processProtocolRollback(BlockHeader blockHeader) {
//        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
//        //临时处理为空的情况，判断为空是由于第一个版本的区块不包含版本信息字段
//        if (extendsData.getCurrentVersion() == null) {
//            return;
//        }
//        //首先确定回滚块的协议对象
//        ProtocolContainer protocolContainer = NulsVersionManager.getProtocolContainer(extendsData.getCurrentVersion());
//        if (protocolContainer != null) {
//            if (protocolContainer.getStatus() == ProtocolContainer.VALID && protocolContainer.getEffectiveHeight() < blockHeader.getHeight()) {
//                //如果block对应的协议已经生效，并且当前块的高度大于协议生效时的高度，则不需要处理
//                return;
//            }
//            //通过高度判断该block否是恰好是一个协议生效的块
//            if (null != protocolContainer.getEffectiveHeight() && protocolContainer.getEffectiveHeight() == blockHeader.getHeight()) {
//                //回退恰好一个协议生效的块
//                rollbackUpgradeBlock(protocolContainer, blockHeader, extendsData);
//            } else {
//                rollbackContainerBlock(protocolContainer, blockHeader, extendsData);
//            }
//            saveProtocolInfo(protocolContainer);
//            /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
//            Log.info("@@@@@@@@@@@@@@ 回滚 统计协议 @@@@@@@@@@@@@@");
//            Log.info("@@@@@@@ 协议version：" + protocolContainer.getVersion());
//            Log.info("@@@@@@@ 当前高度：" + blockHeader.getHeight());
//            Log.info("@@@@@@@ 当前hash：" + blockHeader.getHash());
//            Log.info("@@@@@@@ 协议状态：" + protocolContainer.getStatus());
//            Log.info("@@@@@@@ 协议当前延迟块数：" + protocolContainer.getCurrentDelay());
//            Log.info("@@@@@@@ 协议当前轮次：" + protocolContainer.getRoundIndex());
//            Log.info("@@@@@@@ 协议AddressSet：" + Arrays.toString(protocolContainer.getAddressSet().toArray()));
//            /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
//        } else {
//            //如果没有,则从临时的协议容器里获取
//            ProtocolTempInfoPo tempInfoPo = getVersionManagerStorageService().getProtocolTempInfoPo(extendsData.getProtocolKey());
//            if (tempInfoPo != null) {
//                if (tempInfoPo.getStatus() == ProtocolContainer.VALID && tempInfoPo.getEffectiveHeight() < blockHeader.getHeight()) {
//                    //如果block对应的协议已经生效，并且当前块的高度大于协议生效时的高度，则不需要处理
//                    return;
//                }
//                //通过高度判断该block否是恰好是一个协议生效的块
//                if (null != tempInfoPo.getEffectiveHeight() && tempInfoPo.getEffectiveHeight() == blockHeader.getHeight()) {
//                    //回退恰好一个协议生效的块
//                    rollbackUpgradeBlockTemp(tempInfoPo, blockHeader, extendsData);
//                } else {
//                    rollbackContainerBlockTemp(tempInfoPo, blockHeader, extendsData);
//                }
//                getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);
//
//                /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
//                Log.info("@@@@@@@@@@@@@@ 回滚 Temp 统计协议 @@@@@@@@@@@@@@");
//                Log.info("@@@@@@@ 协议version：" + tempInfoPo.getVersion());
//                Log.info("@@@@@@@ 当前高度：" + blockHeader.getHeight());
//                Log.info("@@@@@@@ 当前hash：" + blockHeader.getHash());
//                Log.info("@@@@@@@ 协议状态：" + tempInfoPo.getStatus());
//                Log.info("@@@@@@@ 协议当前延迟块数：" + tempInfoPo.getCurrentDelay());
//                Log.info("@@@@@@@ 协议当前轮次：" + tempInfoPo.getRoundIndex());
//                Log.info("@@@@@@@ 协议AddressSet：" + Arrays.toString(tempInfoPo.getAddressSet().toArray()));
//                /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
//
//            }
//        }
//
//    }


    /**
     * 回滚块，统计协议信息
     *
     * @param protocolContainer
     * @param blockHeader
     * @param extendsData
     */
    private void rollbackContainerBlock(ProtocolContainer protocolContainer, BlockHeader blockHeader, BlockExtendsData extendsData) {
        //满足覆盖率的所有块，分轮次存到map中
        Map<Long, List<BlockHeader>> roundBlocksMap = new TreeMap<>();
        getRollbackBlocks(protocolContainer, roundBlocksMap, blockHeader, extendsData.getRoundIndex());
        boolean isStartDelayLock = false;
        for (Map.Entry<Long, List<BlockHeader>> entry : roundBlocksMap.entrySet()) {
            long roundIndex = entry.getKey();
            List<BlockHeader> currentRoundBlockHeaderList = entry.getValue();

            /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@ 轮次map @@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@ 轮次map @@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("@@@@@@ 轮次：" + roundIndex);
            System.out.println("@@@@@@ 块数量：" + currentRoundBlockHeaderList.size());
            /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */

            //存储本轮出当前协议块的地址，用于计算到这个块时当前协议的覆盖率
            Set<String> addressSet = new HashSet<>();
            for (BlockHeader header : currentRoundBlockHeaderList) {
                //如果是当前需回滚的块，则不计算
                if (header.getHash().equals(blockHeader.getHash())) {
                    break;
                }
                BlockExtendsData extendsDataHeader = new BlockExtendsData(header.getExtend());
                //判断协议block协议版本是否和当前Container版本相同
                if (extendsDataHeader.getCurrentVersion().intValue() == protocolContainer.getVersion().intValue()) {
                    addressSet.add(AddressTool.getStringAddressByBytes(header.getPackingAddress()));
                    //计算到这个块时当前协议的覆盖率
                    int rate = calcRate(addressSet.size(), currentRoundBlockHeaderList.size());
                    //如果覆盖率达到或者状态已处于延迟块统计中者继续统计
                    if (rate >= protocolContainer.getPercent() || protocolContainer.getStatus() == ProtocolContainer.DELAY_LOCK) {
                        if (!isStartDelayLock) {
                            //统计开始的块
                            protocolContainer.setCurrentDelay(0L);
                            protocolContainer.setStatus(ProtocolContainer.DELAY_LOCK);
                            isStartDelayLock = true;
                        }
                        //如果满足这开始正向统计
                        protocolContainer.setRoundIndex(extendsDataHeader.getRoundIndex());
                        protocolContainer.setCurrentDelay(protocolContainer.getCurrentDelay() + 1);
                        protocolContainer.setAddressSet(addressSet);

                    }
                }
            }
        }
    }

    /**
     * 回滚块，统计临时协议信息
     *
     * @param tempInfoPo
     * @param blockHeader
     * @param extendsData
     */
    private void rollbackContainerBlockTemp(ProtocolTempInfoPo tempInfoPo, BlockHeader blockHeader, BlockExtendsData extendsData) {
        //满足覆盖率的所有块，分轮次存到map中
        Map<Long, List<BlockHeader>> roundBlocksMap = new TreeMap<>();
        getRollbackBlocksTemp(tempInfoPo, roundBlocksMap, blockHeader, extendsData.getRoundIndex());
        for (Map.Entry<Long, List<BlockHeader>> entry : roundBlocksMap.entrySet()) {
            long roundIndex = entry.getKey();
            List<BlockHeader> currentRoundBlockHeaderList = entry.getValue();

            /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@ temp轮次map @@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@ temp轮次map @@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("@@@@@@ temp轮次：" + roundIndex);
            System.out.println("@@@@@@ temp块数量：" + currentRoundBlockHeaderList.size());
            /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */

            //存储本轮出当前协议块的地址，用于计算到这个块时当前协议的覆盖率
            Set<String> addressSetTemp = new HashSet<>();
            for (BlockHeader header : currentRoundBlockHeaderList) {
                if (header.getHeight() == blockHeader.getHeight()) {
                    break;
                }
                BlockExtendsData extendsDataHeader = new BlockExtendsData(header.getExtend());
                //判断协议block协议版本是否和当前Container版本相同
                if (extendsDataHeader.getCurrentVersion().intValue() == tempInfoPo.getVersion()) {
                    addressSetTemp.add(AddressTool.getStringAddressByBytes(header.getPackingAddress()));
                    //计算到这个块时当前协议的覆盖率
                    int rate = calcRate(addressSetTemp.size(), currentRoundBlockHeaderList.size());
                    //如果覆盖率达到或者状态已处于延迟块统计中者继续统计
                    if (rate >= tempInfoPo.getPercent() || tempInfoPo.getStatus() == ProtocolContainer.DELAY_LOCK) {
                        //如果满足这开始正向统计
                        tempInfoPo.setRoundIndex(extendsDataHeader.getRoundIndex());
                        tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() + 1);
                        tempInfoPo.setAddressSet(addressSetTemp);
                        tempInfoPo.setStatus(ProtocolContainer.DELAY_LOCK);
                    }
                }
            }
        }
    }

    /**
     * 回滚的块恰好是升级的块
     *
     * @param protocolContainer
     * @param blockHeader
     * @param extendsData
     */
    private void rollbackUpgradeBlock(ProtocolContainer protocolContainer, BlockHeader blockHeader, BlockExtendsData extendsData) {
        NulsContext.MAIN_NET_VERSION = protocolContainer.getVersion() - 1 < 1 ? 1 : protocolContainer.getVersion() - 1;
        protocolContainer.setStatus(ProtocolContainer.DELAY_LOCK);
        protocolContainer.setCurrentDelay(protocolContainer.getCurrentDelay() - 1);
        protocolContainer.setEffectiveHeight(null);
        BlockHeader preBlockHeader = getBlockService().getBlockHeader(blockHeader.getPreHash()).getData();
        BlockExtendsData preExtendsData = new BlockExtendsData(preBlockHeader.getExtend());
        //设置成上一个块的轮次
        protocolContainer.setRoundIndex(preExtendsData.getRoundIndex());

        //重置addressSet
        Set<String> addressSet = new HashSet<>();
        if (extendsData.getRoundIndex() > preExtendsData.getRoundIndex()) {
            //如果是当前轮次的第一个块，则取前一轮的块来计算addressSet

            List<BlockHeader> list = new ArrayList<>();
            getCurrentRoundBlockHeaderList(list, preBlockHeader);
            for (BlockHeader header : list) {
                BlockExtendsData extendsDataHeader = new BlockExtendsData(header.getExtend());
                if (extendsDataHeader.getCurrentVersion().intValue() == protocolContainer.getVersion().intValue()) {
                    addressSet.add(AddressTool.getStringAddressByBytes(header.getPackingAddress()));
                }
            }
        } else {
            List<BlockHeader> list = new ArrayList<>();
            getCurrentRoundBlockHeaderList(list, blockHeader);
            for (BlockHeader header : list) {
                //如果是当前需回滚的块，则不加入
                if (header.getHash().equals(blockHeader.getHash())) {
                    break;
                }
                BlockExtendsData extendsDataHeader = new BlockExtendsData(header.getExtend());
                if (extendsDataHeader.getCurrentVersion().intValue() == protocolContainer.getVersion().intValue()) {
                    addressSet.add(AddressTool.getStringAddressByBytes(header.getPackingAddress()));
                }
            }
        }

        protocolContainer.setAddressSet(addressSet);
    }

    /**
     * 回滚的块恰好是升级的块
     *
     * @param tempInfoPo
     * @param blockHeader
     * @param extendsData
     */
    private void rollbackUpgradeBlockTemp(ProtocolTempInfoPo tempInfoPo, BlockHeader blockHeader, BlockExtendsData extendsData) {
        NulsContext.MAIN_NET_VERSION = tempInfoPo.getVersion() - 1 < 1 ? 1 : tempInfoPo.getVersion() - 1;
        tempInfoPo.setStatus(ProtocolContainer.DELAY_LOCK);
        tempInfoPo.setCurrentDelay(tempInfoPo.getCurrentDelay() - 1);
        tempInfoPo.setEffectiveHeight(null);
        BlockHeader preBlockHeader = getBlockService().getBlockHeader(blockHeader.getPreHash()).getData();
        BlockExtendsData preExtendsData = new BlockExtendsData(preBlockHeader.getExtend());
        //设置成上一个块的轮次
        tempInfoPo.setRoundIndex(preExtendsData.getRoundIndex());
        //重置addressSet
        Set<String> addressSetTemp = new HashSet<>();
        if (extendsData.getRoundIndex() > preExtendsData.getRoundIndex()) {
            //如果是当前轮次的第一个块，则取前一轮的块来计算addressSet
            List<BlockHeader> list = new ArrayList<>();
            getCurrentRoundBlockHeaderList(list, preBlockHeader);
            for (BlockHeader header : list) {
                BlockExtendsData extendsDataHeader = new BlockExtendsData(header.getExtend());
                if (extendsDataHeader.getCurrentVersion().intValue() == tempInfoPo.getVersion()) {
                    addressSetTemp.add(AddressTool.getStringAddressByBytes(header.getPackingAddress()));
                }
            }
        } else {
            List<BlockHeader> list = new ArrayList<>();
            getCurrentRoundBlockHeaderList(list, blockHeader);
            for (BlockHeader header : list) {
                //如果是当前需回滚的块，则不加入
                if (header.getHash().equals(blockHeader.getHash())) {
                    break;
                }
                BlockExtendsData extendsDataHeader = new BlockExtendsData(header.getExtend());
                if (extendsDataHeader.getCurrentVersion().intValue() == tempInfoPo.getVersion()) {
                    addressSetTemp.add(AddressTool.getStringAddressByBytes(header.getPackingAddress()));
                }
            }
        }
        tempInfoPo.setAddressSet(addressSetTemp);
    }


    /**
     * 根据区块头获取同轮次所有区块(包含当前块)
     *
     * @param currentRoundBlockHeaderList
     * @param blockHeader
     */
    private List<BlockHeader> getCurrentRoundBlockHeaderList(List<BlockHeader> currentRoundBlockHeaderList, BlockHeader blockHeader) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        BlockHeader preBlockHeader = getBlockService().getBlockHeader(blockHeader.getPreHash()).getData();
        BlockExtendsData preExtendsData = new BlockExtendsData(preBlockHeader.getExtend());

        /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
        System.out.println("@@@@@@@@@@@@@@ 轮次比较 获取同轮次区块区块 @@@@@@@@@@@@@@");
        System.out.println("extendsData.getRoundIndex(): " + extendsData.getRoundIndex());
        System.out.println("preExtendsData.getRoundIndex(): " + preExtendsData.getRoundIndex());
        /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
        currentRoundBlockHeaderList.add(0, blockHeader);
        if (extendsData.getRoundIndex() > preExtendsData.getRoundIndex()) {
            return currentRoundBlockHeaderList;
        } else {
            return getCurrentRoundBlockHeaderList(currentRoundBlockHeaderList, preBlockHeader);
        }
    }

    /**
     * (获取块)获取当前块之前满足当前块协议覆盖率条件的所有轮次的块
     *
     * @param protocolContainer
     * @param roundBlocksMap
     * @param blockHeader
     * @param rollbackRoundIndex
     * @return
     */
    private Map<Long, List<BlockHeader>> getRollbackBlocks(ProtocolContainer protocolContainer, Map<Long, List<BlockHeader>> roundBlocksMap, BlockHeader blockHeader, long rollbackRoundIndex) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        List<BlockHeader> list = new ArrayList<>();
        getCurrentRoundBlockHeaderList(list, blockHeader);
        if (list.size() > 0) {
            BlockHeader bh = list.get(0);
            BlockExtendsData extendsDataBH = new BlockExtendsData(bh.getExtend());
            Set<String> addressSet = getRoundPackingAddress(list);
            int rate = calcRate(addressSet.size(), extendsDataBH.getConsensusMemberCount());

            //获取上一个块
            BlockHeader first = list.get(0);
            BlockHeader preRoundBlockHeader = getBlockService().getBlockHeader(first.getPreHash()).getData();
            BlockExtendsData preExtendsData = new BlockExtendsData(preRoundBlockHeader.getExtend());
            //如果一轮覆盖率满足条件，或者回滚的块为对应轮次的第一个块时，则继续往前取
            System.out.println();
            System.out.println(bh.getHeight());
            System.out.println(extendsDataBH.getRoundIndex() + " @ " + rollbackRoundIndex);
            System.out.println(extendsDataBH.getRoundIndex() + " @ " + preExtendsData.getRoundIndex());
            if (rate >= protocolContainer.getPercent()
                    || (extendsDataBH.getRoundIndex() == rollbackRoundIndex && extendsDataBH.getRoundIndex() > preExtendsData.getRoundIndex())
                    || (extendsDataBH.getRoundIndex() == rollbackRoundIndex - 1 && protocolContainer.getStatus() == ProtocolContainer.INVALID)) {
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ 继续取了");
                roundBlocksMap.put(extendsData.getRoundIndex(), list);

                return getRollbackBlocks(protocolContainer, roundBlocksMap, preRoundBlockHeader, rollbackRoundIndex);
            }
        }
        return roundBlocksMap;
    }

    private Map<Long, List<BlockHeader>> getRollbackBlocksTemp(ProtocolTempInfoPo tempInfoPo, Map<Long, List<BlockHeader>> roundBlocksMap, BlockHeader blockHeader, long rollbackRoundIndex) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        List<BlockHeader> list = new ArrayList<>();
        getCurrentRoundBlockHeaderList(list, blockHeader);
        if (list.size() > 0) {
            //判断轮次是否达到，达到这继续往前取
            Set<String> addressSetTemp = getRoundPackingAddressTemp(list);
            int rate = calcRate(addressSetTemp.size(), list.size());
            if (rate >= tempInfoPo.getPercent()) {
                roundBlocksMap.put(extendsData.getRoundIndex(), list);
                BlockHeader first = list.get(0);
                //本轮第一块的前一块为上一轮的最后一块
                BlockHeader preRoundBlockHeader = getBlockService().getBlockHeader(first.getPreHash()).getData();
                return getRollbackBlocksTemp(tempInfoPo, roundBlocksMap, preRoundBlockHeader, rollbackRoundIndex);
            }
        }
        return roundBlocksMap;
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

}
