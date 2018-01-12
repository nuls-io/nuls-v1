package io.nuls.consensus.entity.block;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.intf.NulsCloneable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2018/1/11
 */
public class BlockHeaderChain implements NulsCloneable {
    private List<BlockHeader> headerList = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public boolean addHeader(BlockHeader header) {
        lock.lock();
        if (!headerList.isEmpty() &&
                !headerList.get(headerList.size() - 1).getHash().getDigestHex().equals(header.getHash().getDigestHex())) {
            return false;
        }
        headerList.add(header);
        lock.unlock();
        return true;
    }

    public void destroy() {
        headerList.clear();
    }

    public BlockHeader poll() {
        lock.lock();
        if (headerList.size() < 1 + PocConsensusConstant.CONFIRM_BLOCK_COUNT) {
            return null;
        }
        BlockHeader header = headerList.get(0);
        headerList.remove(0);
        lock.unlock();
        return header;
    }

    public BlockHeader rollback() {
        lock.lock();
        if (headerList.isEmpty()) {
            lock.unlock();
            return null;
        }
        BlockHeader header = headerList.get(headerList.size() - 1);
        headerList.remove(headerList.size() - 1);
        lock.unlock();
        return header;
    }

    public int size() {
        return headerList.size();
    }

    @Override
    public Object copy() {
        return this;
    }
}
