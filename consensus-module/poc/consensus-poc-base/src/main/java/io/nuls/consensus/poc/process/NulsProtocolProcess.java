package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.protocol.base.version.NulsVersionManager;
import io.nuls.protocol.base.version.ProtocolContainer;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NulsProtocolProcess {

    /**
     * 版本升级流程处理
     * 基本思路：
     * 1.判断当前块里的版本信息，自己是否有
     *
     * @param block
     */
    public static void processProtoclUpGrade(Block block) {
        BlockExtendsData extendsData = new BlockExtendsData(block.getHeader().getExtend());
        //临时处理为空的情况，为空是由于第一个版本的区块不包含版本信息字段
        if (extendsData.getCurrentVersion() == null) {
            extendsData.setCurrentVersion(1);
        }
        //版本信息低于当前主网信息直接返回
        if (extendsData.getCurrentVersion() < NulsVersionManager.getMainVersion()) {
            return;
        }

        ProtocolContainer protocolContainer = NulsVersionManager.getProtocolContainer(extendsData.getCurrentVersion());
        //当自己的配置里有此版本时
        if (protocolContainer != null) {
            //获取当前轮信息和当前出块人信息
            MeetingRound currentRound = PocConsensusContext.getChainManager().getMasterChain().getCurrentRound();
            MeetingMember currentMember = currentRound.getMember(block.getHeader().getPackingAddress());
            if (currentMember == null) {
                return;
            }
            refreshProtocolAddressSet(protocolContainer, block, currentRound, currentMember);
            checkNewProtocolCoverageRate(protocolContainer, currentRound, currentMember);
            //判断自己是否是共识节点，如果是达到条件后发送一条版本升级交易
//            PocConsensusContext.getChainManager().getMasterChain().getCurrentRound().getMyMember();
            return;
        }
    }

    /**
     * 每轮新开始时，都要清空出块节点的地址集合，以便检查本轮到结束时是否达到升级协议的覆盖率
     *
     * @param container 协议容器
     * @param block     当前最新区块
     */
    private static void refreshProtocolAddressSet(ProtocolContainer container, Block block,
                                                  MeetingRound currentRound, MeetingMember currentMember) {
        //协议已生效，直接退出
        if (container.getStatus() == ProtocolContainer.VALID) {
            return;
        }
        //当前出块人为本轮的第一个出块节点时，清空协议里的已升级打包节点地址集合
        if (currentMember.getPackingIndexOfRound() == 1) {
            container.getAddressSet().clear();
        }

        Set<String> addressSet = container.getAddressSet();
        addressSet.add(AddressTool.getStringAddressByBytes(block.getHeader().getPackingAddress()));
        //如果当前是本轮的最后一个出块节点，判断是否未达到覆盖率，如果未达到覆盖率直接清空覆盖率和已延迟块重新计算
        if (currentMember.getPackingIndexOfRound() == currentRound.getMemberCount()) {
            int rate = calcRate(container);
            if (rate < container.getPercent()) {
                container.getAddressSet().clear();
                container.setCurrentDelay(0);
                container.setStatus(ProtocolContainer.INVALID);
            }
        }
    }

    /**
     * 检查最新版本协议的覆盖率和延迟块数，判断新协议是否生效
     *
     * @param protocolContainer
     */
    private static void checkNewProtocolCoverageRate(ProtocolContainer protocolContainer, MeetingRound currentRound, MeetingMember currentMember) {
        //协议未生效时，判断覆盖率
        if (protocolContainer.getStatus() == protocolContainer.INVALID) {
            //覆盖率达到后，修改状态为延迟锁定中
            int rate = calcRate(protocolContainer);
            if (rate >= protocolContainer.getPercent()) {
                protocolContainer.setStatus(ProtocolContainer.DELAY_LOCK);
            }
        } else if (protocolContainer.getStatus() == protocolContainer.DELAY_LOCK) {
            //当状态为锁定等待延迟高度完成时，首先是从新的一轮开始添加延迟区块数
            if (protocolContainer.getCurrentDelay() == 0) {
                if (currentMember.getPackingIndexOfRound() == currentRound.getMemberCount()) {
                    protocolContainer.setCurrentDelay(protocolContainer.getCurrentDelay() + 1);
                }
            } else {
                protocolContainer.setCurrentDelay(protocolContainer.getCurrentDelay() + 1);
            }

        }
    }

    /**
     * 计算覆盖率
     *
     * @param protocolContainer
     * @return
     */
    private static int calcRate(ProtocolContainer protocolContainer) {
        int memeberCount = PocConsensusContext.getChainManager().getMasterChain().getCurrentRound().getMemberCount();
        int currentCount = protocolContainer.getAddressSet().size();

        BigDecimal b1 = new BigDecimal(currentCount);
        BigDecimal b2 = new BigDecimal(memeberCount);
        int rate = b1.divide(b2, 2, BigDecimal.ROUND_DOWN).movePointRight(2).intValue();
        return rate;
    }

}
