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
    private List<HeaderDigest> headerDigestList = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public BlockHeaderChain getBifurcateChain(BlockHeader header) {
        int index = indexOf(header.getPreHash().getDigestHex());
        if (index == -1) {
            return null;
        }
        List<HeaderDigest> list = new ArrayList<>();
        for (int i = 0; i <= index; i++) {
            list.add(headerDigestList.get(i));
        }
        list.add(new HeaderDigest(header.getHash().getDigestHex(), header.getHeight()));
        BlockHeaderChain chain = new BlockHeaderChain();
        chain.setHeaderDigestList(list);
        return chain;
    }

    public int indexOf(String hash) {
        return headerDigestList.indexOf(hash);
    }

    public boolean addHeader(BlockHeader header) {
        lock.lock();
        if (!headerDigestList.isEmpty() &&
                !headerDigestList.get(headerDigestList.size() - 1).equals(header.getPreHash().getDigestHex())) {
            return false;
        }
        headerDigestList.add(new HeaderDigest(header.getHash().getDigestHex(), header.getHeight()));
        lock.unlock();
        return true;
    }

    public void destroy() {
        headerDigestList.clear();
    }

    public HeaderDigest poll() {
        lock.lock();
        if (headerDigestList.size() < 1 + PocConsensusConstant.CONFIRM_BLOCK_COUNT) {
            return null;
        }
        HeaderDigest headerDigest = headerDigestList.get(0);

        lock.unlock();
        return headerDigest;
    }

    public void removeHeaderDigest(long height) {
        for (int i = headerDigestList.size() - 1; i >= 0; i--) {
            HeaderDigest headerDigest = headerDigestList.get(i);
            if (headerDigest.getHeight() <= height) {
                headerDigestList.remove(i);
            }
        }
    }

     public HeaderDigest rollback() {
        lock.lock();
        if (headerDigestList.isEmpty()) {
            lock.unlock();
            return null;
        }
        HeaderDigest headerDigest = headerDigestList.get(headerDigestList.size() - 1);
        headerDigestList.remove(headerDigestList.size() - 1);
        lock.unlock();
        return headerDigest;
    }

    public int size() {
        return headerDigestList.size();
    }

    @Override
    public Object copy() {
        return this;
    }

    public List<HeaderDigest> getHeaderDigestList() {
        return headerDigestList;
    }

    public void setHeaderDigestList(List<HeaderDigest> headerDigestList) {
        this.headerDigestList = headerDigestList;
    }

}
