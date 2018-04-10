/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.utils.str.StringUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2018/1/11
 */
public class BlockHeaderChain implements NulsCloneable {
    private final String id;
    private List<HeaderDigest> headerDigestList = new CopyOnWriteArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private HeaderDigest lastHd;

    public BlockHeaderChain() {
        this.id = StringUtils.getNewUUID();
    }

    public BlockHeaderChain getBifurcateChain(BlockHeader header) {
        int index = indexOf(header.getPreHash().getDigestHex(), header.getHeight() - 1, header.getTime());
        if (index == -1) {
            return new BlockHeaderChain();
        }
        List<HeaderDigest> list = new CopyOnWriteArrayList<>(headerDigestList.subList(0, index));
        list.add(new HeaderDigest(header.getHash().getDigestHex(), header.getHeight(), header.getTime()));
        BlockHeaderChain chain = new BlockHeaderChain();
        chain.setHeaderDigestList(list);
        return chain;
    }

    private int indexOf(String hash, long height, long time) {
        HeaderDigest hd = new HeaderDigest(hash, height, time);
        return headerDigestList.indexOf(hd);
    }

    public boolean addHeader(BlockHeader header) {
        lock.lock();
        if (null != lastHd && !this.lastHd.getHash().equals(header.getPreHash().getDigestHex())) {
            return false;
        }
        HeaderDigest newHd = new HeaderDigest(header.getHash().getDigestHex(), header.getHeight(), header.getTime());
        headerDigestList.add(newHd);
        this.lastHd = newHd;
        lock.unlock();
        return true;
    }

    public void destroy() {
        headerDigestList.clear();
    }


    public void removeHeaderDigest(String hashHex) {
        for (HeaderDigest hd : headerDigestList) {
            if (hd.getHash().equals(hashHex)) {
                removeHeaderDigest(hd.getHeight());
                return;
            }
        }
    }

    private void removeHeaderDigest(long height) {
        for (int i = headerDigestList.size() - 1; i >= 0; i--) {
            HeaderDigest headerDigest = headerDigestList.get(i);
            if (headerDigest.getHeight() <= height) {
                headerDigestList.remove(headerDigest);
            }
        }
    }

    public void rollbackHeaderDigest(String hash) {
        for (int i = 0; i < headerDigestList.size(); i++) {
            HeaderDigest headerDigest = headerDigestList.get(i);
            if (headerDigest.getHash().equals(hash)) {
                headerDigestList.remove(headerDigest);
                for (int x = i + 1; x < headerDigestList.size(); x++) {
                    headerDigestList.remove(x);
                }
                this.lastHd = null;
                break;
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
        this.lastHd = null;
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
        for (int i = 0; i < this.headerDigestList.size(); i++) {
            HeaderDigest hd = this.headerDigestList.get(i);
            if (null == hd) {
                continue;
            }
            if (hd.getHeight() == height) {
                return hd;
            }
        }
        return null;
    }

    public HeaderDigest getHeaderDigest(String hashHex) {
        for (int i = 0; i < this.headerDigestList.size(); i++) {
            HeaderDigest hd = this.headerDigestList.get(i);
            if (null == hd) {
                continue;
            }
            if (hd.getHash().equals(hashHex)) {
                return hd;
            }
        }
        return null;
    }

    public boolean contains(HeaderDigest hd) {
        return headerDigestList.contains(hd);
    }

    public boolean contains(BlockHeader header) {
        return headerDigestList.contains(new HeaderDigest(header.getHash().getDigestHex(), header.getHeight(), header.getTime()));
    }

    @Override
    public boolean equals(Object object) {
        if (null == object) {
            return false;
        }
        if (!(object instanceof BlockHeaderChain)) {
            return false;
        }
        return this.id.equals(((BlockHeaderChain) object).getId());
    }

    public String getId() {
        return id;
    }

    public Set<String> getHashSet() {
        Set<String> hashSet = new HashSet<>();
        for (HeaderDigest hd : this.headerDigestList) {
            hashSet.add(hd.getHash());
        }
        return hashSet;
    }

    public HeaderDigest getFirstHd() {
        HeaderDigest headerDigest = headerDigestList.get(0);
        return headerDigest;
    }

    public HeaderDigest getLastHd() {
        if (null == lastHd) {
            List<HeaderDigest> list = new ArrayList<>(headerDigestList);
            this.lastHd = list.get(list.size() - 1);
        }
        return lastHd;
    }
}
