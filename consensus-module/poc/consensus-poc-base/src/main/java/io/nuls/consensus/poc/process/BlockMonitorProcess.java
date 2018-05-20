package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.config.ConsensusConfig;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.service.impl.ConsensusPocServiceImpl;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2018/5/18
 */
public class BlockMonitorProcess {

    private final ChainManager chainManager;

    public BlockMonitorProcess(ChainManager chainManager) {
        this.chainManager = chainManager;
    }

    public void doProcess() {
        List<Block> blockList = chainManager.getMasterChain().getChain().getBlockList();
        int count = 0;
        Set<String> addressSet = new HashSet<>();
        for (Block block : blockList) {
            addressSet.add(Base58.encode(block.getHeader().getPackingAddress()));
            count++;
            if (count > 10) {
                break;
            }
        }
        if (addressSet.size() == 1 && ConsensusConfig.getSeedNodeList().size() > 1) {
            NulsContext.getServiceBean(ConsensusPocServiceImpl.class).reset();
        }
    }
}
