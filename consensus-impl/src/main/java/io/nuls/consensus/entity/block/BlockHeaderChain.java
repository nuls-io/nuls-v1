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
        int index = indexOf(header.getPreHash().getDigestHex(),header.getHeight()-1);
        if (index == -1) {
            return new BlockHeaderChain();
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

    public int indexOf(String hash,long height) {
        HeaderDigest hd = new HeaderDigest(hash,height);
        return headerDigestList.indexOf(hd);
    }

    public boolean addHeader(BlockHeader header) {
        lock.lock();
        if (!headerDigestList.isEmpty() &&
                !headerDigestList.get(headerDigestList.size() - 1).getHash().equals(header.getPreHash().getDigestHex())) {
            return false;
        }
        headerDigestList.add(new HeaderDigest(header.getHash().getDigestHex(), header.getHeight()));
        lock.unlock();
        return true;
    }

    public void destroy() {
        headerDigestList.clear();
    }

    public HeaderDigest getFirst() {
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
                headerDigestList.remove(headerDigest);
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

    public HeaderDigest getHeaderDigest(long height) {
        for(HeaderDigest hd:this.headerDigestList){
            if(hd.getHeight()==height){
                return hd;
            }
        }
        return null;
    }
}
