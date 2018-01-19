/**
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
 */
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
        this.chainList.forEach((BlockHeaderChain chain) -> removeBlock(chain,height));

    }

    private void removeBlock(BlockHeaderChain chain, long height) {
        HeaderDigest hd = chain.getHeaderDigest(height);
        if(hd==null){
            return;
        }
        chain.removeHeaderDigest(height);

    }
}
