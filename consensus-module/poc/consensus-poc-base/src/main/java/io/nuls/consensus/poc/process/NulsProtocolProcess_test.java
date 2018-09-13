package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.util.ProtocolTransferTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
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

public class NulsProtocolProcess_test {

    private static NulsProtocolProcess_test protocolProcess = new NulsProtocolProcess_test();

    private NulsProtocolProcess_test() {

    }

    public static NulsProtocolProcess_test getInstance() {
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

    private int calcRate(List<BlockHeader> headers, int memberCount, ProtocolContainer container) {
        BlockExtendsData extendsData;
        for(BlockHeader header: headers) {
            extendsData = new BlockExtendsData(header.getExtend());
//            if(header.get)
        }
        return 1;
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
    public void processProtocolRollback(BlockHeader blockHeader) {
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        //判断为空是由于第一个版本的区块不包含版本信息字段，此时区块回退不涉及到版本更新逻辑
        if (extendsData.getCurrentVersion() == null) {
            return;
        }
        //首先确定回滚块的协议容器对象
        ProtocolContainer protocolContainer = NulsVersionManager.getProtocolContainer(extendsData.getCurrentVersion());
        if (protocolContainer != null) {
            //如果block对应的协议已经生效，并且当前块的高度大于协议生效时的高度，则不需要处理
            if (protocolContainer.getStatus() == ProtocolContainer.VALID) {
                if (protocolContainer.getEffectiveHeight() < blockHeader.getHeight()) {
                    return;
                }
                rollbackUpgradeBlock(protocolContainer, blockHeader, extendsData);
            } else if (protocolContainer.getStatus() == ProtocolContainer.DELAY_LOCK) {
                rollbackDelayBlock(protocolContainer, blockHeader, extendsData);
            } else if (protocolContainer.getStatus() == ProtocolContainer.INVALID) {
                rollbackInvalidBlock(protocolContainer, blockHeader, extendsData);
            }

            saveProtocolInfo(protocolContainer);
            /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
            Log.info("@@@@@@@@@@@@@@ 回滚 统计协议 @@@@@@@@@@@@@@");
            Log.info("@@@@@@@ 协议version：" + protocolContainer.getVersion());
            Log.info("@@@@@@@ 当前高度：" + blockHeader.getHeight());
            Log.info("@@@@@@@ 当前hash：" + blockHeader.getHash());
            Log.info("@@@@@@@ 协议状态：" + protocolContainer.getStatus());
            Log.info("@@@@@@@ 协议当前延迟块数：" + protocolContainer.getCurrentDelay());
            Log.info("@@@@@@@ 协议当前轮次：" + protocolContainer.getRoundIndex());
            Log.info("@@@@@@@ 协议AddressSet：" + Arrays.toString(protocolContainer.getAddressSet().toArray()));
            /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
        } else {
            //如果没有,则从临时的协议容器里获取
            ProtocolTempInfoPo tempInfoPo = getVersionManagerStorageService().getProtocolTempInfoPo(extendsData.getProtocolKey());
            if (tempInfoPo != null) {
                if (tempInfoPo.getStatus() == ProtocolContainer.VALID && tempInfoPo.getEffectiveHeight() < blockHeader.getHeight()) {
                    //如果block对应的协议已经生效，并且当前块的高度大于协议生效时的高度，则不需要处理
                    return;
                }
                //通过高度判断该block否是恰好是一个协议生效的块
                if (null != tempInfoPo.getEffectiveHeight() && tempInfoPo.getEffectiveHeight() == blockHeader.getHeight()) {
                    //回退恰好一个协议生效的块
//                    rollbackUpgradeBlockTemp(tempInfoPo, blockHeader, extendsData);
                } else {
                    rollbackContainerBlockTemp(tempInfoPo, blockHeader, extendsData);
                }
                getVersionManagerStorageService().saveProtocolTempInfoPo(tempInfoPo);

                /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */
                Log.info("@@@@@@@@@@@@@@ 回滚 Temp 统计协议 @@@@@@@@@@@@@@");
                Log.info("@@@@@@@ 协议version：" + tempInfoPo.getVersion());
                Log.info("@@@@@@@ 当前高度：" + blockHeader.getHeight());
                Log.info("@@@@@@@ 当前hash：" + blockHeader.getHash());
                Log.info("@@@@@@@ 协议状态：" + tempInfoPo.getStatus());
                Log.info("@@@@@@@ 协议当前延迟块数：" + tempInfoPo.getCurrentDelay());
                Log.info("@@@@@@@ 协议当前轮次：" + tempInfoPo.getRoundIndex());
                Log.info("@@@@@@@ 协议AddressSet：" + Arrays.toString(tempInfoPo.getAddressSet().toArray()));
                /**  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   */

            }
        }

    }


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
//        getRollbackBlocks(protocolContainer, roundBlocksMap, blockHeader, extendsData.getRoundIndex());
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
                        //如果满足这开始正向统计
                        protocolContainer.setStatus(ProtocolContainer.DELAY_LOCK);
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
//        getRollbackBlocksTemp(tempInfoPo, roundBlocksMap, blockHeader, extendsData.getRoundIndex());
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
        //回滚到前一个块的轮次
        BlockHeader preBlockHeader = getBlockService().getBlock(blockHeader.getPreHash()).getData().getHeader();
        BlockExtendsData preExtendsData = new BlockExtendsData(preBlockHeader.getExtend());
        protocolContainer.setRoundIndex(preExtendsData.getRoundIndex());

        //重置addressSet
        Set<String> addressSet;
        if (extendsData.getRoundIndex() > preExtendsData.getRoundIndex()) {
            //当前块的轮次大于上一块的轮次，则取前一轮的所有块来计算addressSet
            List<BlockHeader> headerList = getCurrentRoundBlockHeaders(preBlockHeader.getHash(), true);
            addressSet = getRoundPackingAddress(headerList, protocolContainer, null);
        } else {
            //当前块的轮次等于上衣块的轮次，则取本轮的所有已出块来计算addressSet
            List<BlockHeader> headerList = getCurrentRoundBlockHeaders(blockHeader.getHash(), false);
            addressSet = getRoundPackingAddress(headerList, protocolContainer, blockHeader.getHash());
        }
        protocolContainer.setAddressSet(addressSet);
    }

    /**
     * 回滚区块的时候协议容器处于延迟生效的时候
     *
     * @param protocolContainer
     * @param blockHeader
     * @param extendsData
     */
    private void rollbackDelayBlock(ProtocolContainer protocolContainer, BlockHeader blockHeader, BlockExtendsData extendsData) {
        protocolContainer.setCurrentDelay(protocolContainer.getCurrentDelay() - 1);
        //如果延迟块数回滚到0时，状态改为未生效
        if (protocolContainer.getCurrentDelay() == 0) {
            protocolContainer.setStatus(ProtocolContainer.INVALID);
        }
        //回滚到前一个块的轮次
        BlockHeader preBlockHeader = getBlockService().getBlock(blockHeader.getPreHash()).getData().getHeader();
        BlockExtendsData preExtendsData = new BlockExtendsData(preBlockHeader.getExtend());
        protocolContainer.setRoundIndex(preExtendsData.getRoundIndex());
        //重置addressSet
        Set<String> addressSet;
        if (extendsData.getRoundIndex() > preExtendsData.getRoundIndex()) {
            //如果是当前轮次的第一个块，则取前一轮的块来计算addressSet
            List<BlockHeader> headerList = getCurrentRoundBlockHeaders(preBlockHeader.getHash(), true);
            addressSet = getRoundPackingAddress(headerList, protocolContainer, null);
        } else {
            List<BlockHeader> headerList = getCurrentRoundBlockHeaders(blockHeader.getHash(), false);
            addressSet = getRoundPackingAddress(headerList, protocolContainer, blockHeader.getHash());
        }
        protocolContainer.setAddressSet(addressSet);
    }

    /**
     * 未达到延迟块数时的回滚处理
     *
     * @param protocolContainer
     * @param blockHeader
     * @param extendsData
     */
    private void rollbackInvalidBlock(ProtocolContainer protocolContainer, BlockHeader blockHeader, BlockExtendsData extendsData) {
        //回滚到前一个块的轮次
        BlockHeader preBlockHeader = getBlockService().getBlock(blockHeader.getPreHash()).getData().getHeader();
        BlockExtendsData preExtendsData = new BlockExtendsData(preBlockHeader.getExtend());
        protocolContainer.setRoundIndex(preExtendsData.getRoundIndex());
        //重置addressSet
        Set<String> addressSet;
        if (extendsData.getRoundIndex() > preExtendsData.getRoundIndex()) {
            //未达到延迟块且当前块是新一轮的第一块时，回滚处理最为复杂
            //因为可能恰好是自己分叉了，导致本轮开始时，覆盖率计算不准确，清空了所有延迟块数和状态
            //因此需要递归找出本轮之前所有的达到覆盖率的轮次，重新计算延迟区块数

            //首先获取上一轮所有块，并计算出上一轮的addressSet
            List<BlockHeader> lastRoundHeaders = getCurrentRoundBlockHeaders(preBlockHeader.getHash(), true);
            addressSet = getRoundPackingAddress(lastRoundHeaders, protocolContainer, null);

            //再获取上上轮的所有块，如果上上轮的所有块没有达到覆盖率，则立刻回滚
            BlockHeader firstBlock = lastRoundHeaders.get(0);  //每一轮的第一个区块
            List<BlockHeader> beforeLastRoundHeaders = getCurrentRoundBlockHeaders(firstBlock.getPreHash(), true);  //通过每一轮的第一个区块获取上一轮的所有区块
//            for(BlockHeader header : )
            firstBlock = beforeLastRoundHeaders.get(0);
            BlockExtendsData tempData = new BlockExtendsData(firstBlock.getExtend());
            int rate = calcRate(1, tempData.getConsensusMemberCount());
            if (rate < protocolContainer.getPercent()) {
                protocolContainer.setAddressSet(addressSet);
                return;
            }

            //如果上上轮覆盖率达到，则依次递归到第一次开始记录延迟块数的时候，然后重新统计延迟块数
            while (true) {
                //通过每一轮的第一个区块得到上一轮的所有区块
                List<BlockHeader> tempHeaders = getCurrentRoundBlockHeaders(firstBlock.getPreHash(), true);
                Set<String> tempSet = getRoundPackingAddress(tempHeaders, protocolContainer, null);
                firstBlock = tempHeaders.get(0);
//                BlockExtendsData tempData = new BlockExtendsData(firstBlock.getExtend());
//                int rate = calcRate(tempSet.size(), tempData.getConsensusMemberCount());
                if (rate < protocolContainer.getPercent()) {
                    break;
                }
            }
        } else {
            List<BlockHeader> headerList = getCurrentRoundBlockHeaders(blockHeader.getHash(), false);
            addressSet = getRoundPackingAddress(headerList, protocolContainer, blockHeader.getHash());
        }
        protocolContainer.setAddressSet(addressSet);
    }


    private void aaa(Set<String> addressSet, BlockHeader preBlockHeader, ProtocolContainer protocolContainer) {
        addressSet = null;
        //首先计算出上一轮的addressSet
        List<BlockHeader> beforeCurrentHeaders = getCurrentRoundBlockHeaders(preBlockHeader.getHash(), true);
        addressSet = getRoundPackingAddress(beforeCurrentHeaders, protocolContainer, null);


    }

    /**
     * 查询区块所在当前轮的所有区块
     *
     * @param blockHash
     * @return
     */
    private List<BlockHeader> getCurrentRoundBlockHeaders(NulsDigestData blockHash, boolean comprise) {
        BlockHeader blockHeader = getBlockService().getBlockHeader(blockHash).getData();
        BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
        //获取前一个块
        BlockHeader preBlockHeader;
        BlockExtendsData preExtendData;
        List<BlockHeader> list = new ArrayList<>();
        if (comprise) {
            list.add(blockHeader);
        }
        //循环取出当前轮次的所有块
        while (true) {
            preBlockHeader = getBlockService().getBlockHeader(blockHeader.getPreHash()).getData();
            preExtendData = new BlockExtendsData(preBlockHeader.getExtend());
            if (preExtendData.getRoundIndex() == extendsData.getRoundIndex()) {
                list.add(0, preBlockHeader);
                blockHeader = preBlockHeader;
                extendsData = preExtendData;
            } else {
                return list;
            }
        }
    }


    /**
     * 获取一轮中的出块地址
     */
    private Set<String> getRoundPackingAddress(List<BlockHeader> headerList, ProtocolContainer container, NulsDigestData blockHash) {
        Set<String> addressSet = new HashSet<>();
        for (BlockHeader header : headerList) {
            //如果是当前需回滚的块，则不加入
            if (blockHash != null && header.getHash().equals(blockHash)) {
                break;
            }
            BlockExtendsData extendsDataHeader = new BlockExtendsData(header.getExtend());
            if (extendsDataHeader.getCurrentVersion().intValue() == container.getVersion().intValue()) {
                addressSet.add(AddressTool.getStringAddressByBytes(header.getPackingAddress()));
            }
        }
        return addressSet;
    }

}
