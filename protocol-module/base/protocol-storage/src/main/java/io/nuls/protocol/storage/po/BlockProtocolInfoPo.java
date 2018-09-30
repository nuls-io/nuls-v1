/*
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
 *
 */
package io.nuls.protocol.storage.po;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: Charlie
 * @date: 2018/8/17
 */
public class BlockProtocolInfoPo {
    /**
     * 区块高度
     */
    private long blockHeight;
    /**
     * 协议版本号
     */
    private int version;
    /**
     * 当前延迟区块数
     */
    private long currentDelay;
    /**
     * 当前轮新协议打包出块地址
     */
    private Set<String> addressSet;

    /***当前出块轮次*/
    private long roundIndex;
    /**
     * 协议生效状态
     */
    private int status;
    /**
     * 协议生效时的区块高度
     */
    private Long effectiveHeight;

    private int prePercent;

    public BlockProtocolInfoPo() {
        addressSet = new HashSet<>();
    }

    public BlockProtocolInfoPo(ProtocolTempInfoPo tempInfoPo) {
        this.version = tempInfoPo.getVersion();
        this.currentDelay = tempInfoPo.getCurrentDelay();
        this.addressSet = tempInfoPo.getAddressSet();
        this.roundIndex = tempInfoPo.getRoundIndex();
        this.status = tempInfoPo.getStatus();
        this.effectiveHeight = tempInfoPo.getEffectiveHeight();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getCurrentDelay() {
        return currentDelay;
    }

    public void setCurrentDelay(long currentDelay) {
        this.currentDelay = currentDelay;
    }

    public Set<String> getAddressSet() {
        return addressSet;
    }

    public void setAddressSet(Set<String> addressSet) {
        this.addressSet = addressSet;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public Long getEffectiveHeight() {
        return effectiveHeight;
    }

    public void setEffectiveHeight(Long effectiveHeight) {
        this.effectiveHeight = effectiveHeight;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int getPrePercent() {
        return prePercent;
    }

    public void setPrePercent(int prePercent) {
        this.prePercent = prePercent;
    }

}
