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

package io.nuls.network.rpc.model;

import io.nuls.core.tools.date.DateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "networkInfoJSON")
public class NetworkInfoDto {

    @ApiModelProperty(name = "localBestHeight", value = "本地最新区块高度")
    private Long localBestHeight;

    @ApiModelProperty(name = "netBestHeight", value = "网络最新区块高度")
    private Long netBestHeight;

    @ApiModelProperty(name = "timeOffset", value = "网络时间偏移值")
    private String timeOffset;

    @ApiModelProperty(name = "inCount", value = "被动连接节点数量")
    private int inCount;

    @ApiModelProperty(name = "outCount", value = "主动连接节点数量")
    private int outCount;

    @ApiModelProperty(name = "mastUpGrade", value = "是否需要强制升级")
    private boolean mastUpGrade;

    public NetworkInfoDto() {

    }

    public NetworkInfoDto(long localBestHeight, long netBestHeight, long offsetTime) {
        this.localBestHeight = localBestHeight;
        this.netBestHeight = netBestHeight;
        this.timeOffset = DateUtil.getOffsetStringDate(offsetTime);
    }

    public Long getLocalBestHeight() {
        return localBestHeight;
    }

    public void setLocalBestHeight(Long localBestHeight) {
        this.localBestHeight = localBestHeight;
    }

    public Long getNetBestHeight() {
        return netBestHeight;
    }

    public void setNetBestHeight(Long netBestHeight) {
        this.netBestHeight = netBestHeight;
    }

    public String getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(String timeOffset) {
        this.timeOffset = timeOffset;
    }

    public int getInCount() {
        return inCount;
    }

    public void setInCount(int inCount) {
        this.inCount = inCount;
    }

    public int getOutCount() {
        return outCount;
    }

    public void setOutCount(int outCount) {
        this.outCount = outCount;
    }

    public boolean isMastUpGrade() {
        return mastUpGrade;
    }

    public void setMastUpGrade(boolean mastUpGrade) {
        this.mastUpGrade = mastUpGrade;
    }
}
