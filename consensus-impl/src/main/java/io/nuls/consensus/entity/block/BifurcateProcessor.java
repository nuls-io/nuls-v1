package io.nuls.consensus.entity.block;

import io.nuls.core.chain.entity.BlockHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2018/1/12
 */
public class BifurcateProcessor {

    private static final BifurcateProcessor INSTANCE = new BifurcateProcessor();

    private List<BlockHeaderChain> chainList = new ArrayList<>();

    private long bestHeight;

    private BifurcateProcessor() {
    }

    public static BifurcateProcessor getInstance() {
        return INSTANCE;
    }

    public void addHeader(BlockHeader header) {
        for (BlockHeaderChain chain : chainList) {
            int index = chain.indexOf(header.getPreHash().getDigestHex(), header.getHeight());
            if (index == chain.size() - 1) {
                chain.addHeader(header);
                return;
            } else {
                BlockHeaderChain newChain = chain.getBifurcateChain(header);
                chainList.add(newChain);
                return;
            }
        }
        if (bestHeight > 0 && bestHeight < header.getHeight()) {
            return;
        }
        BlockHeaderChain chain = new BlockHeaderChain();
        chain.addHeader(header);
        chainList.add(chain);
    }

    public BlockHeaderChain getLongestChain() {
        List<BlockHeaderChain> longestChainList = new ArrayList<>();
        for (BlockHeaderChain chain : chainList) {
            if (longestChainList.isEmpty() || chain.size() > longestChainList.get(0).size()) {
                longestChainList.clear();
                longestChainList.add(chain);
            } else if (longestChainList.isEmpty() || chain.size() == longestChainList.get(0).size()) {
                longestChainList.add(chain);
            }
        }
        if (longestChainList.size() > 1 || longestChainList.isEmpty()) {
            return null;
        }
        return longestChainList.get(0);
    }


    public long getBestHeight() {
        return bestHeight;
    }

    public void removeHeight(long height) {
        this.chainList.forEach((BlockHeaderChain chain) -> chain.removeHeaderDigest(height));
    }
}
