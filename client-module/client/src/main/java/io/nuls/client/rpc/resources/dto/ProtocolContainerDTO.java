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
package io.nuls.client.rpc.resources.dto;

import io.nuls.protocol.base.version.ProtocolContainer;
import io.nuls.protocol.storage.po.ProtocolTempInfoPo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/9/15
 */
@ApiModel(value = "protocolContainerJSON")
public class ProtocolContainerDTO {
    /**
     * 协议版本号
     */
    @ApiModelProperty(name = "version", value = "协议版本号")
    private Integer version;

    /***满足协议生效目标覆盖率*/
    @ApiModelProperty(name = "percent", value = "满足协议生效目标覆盖率百分比")
    private int percent;

    /**
     * 当前覆盖率(status = 0时，记录当前轮的覆盖率，status大于1时记录上一轮覆盖率)
     */
    @ApiModelProperty(name = "currentPercent", value = "当前覆盖率百分比")
    private int currentPercent;

    /***当前出块轮次*/
    @ApiModelProperty(name = "roundIndex", value = "当前出块轮次")
    private long roundIndex;

    /***延迟生效区块目标数*/
    @ApiModelProperty(name = "delay", value = "延迟生效区块目标数")
    private long delay;

    /**
     * 当前延迟区块数
     */
    @ApiModelProperty(name = "currentDelay", value = "当前延迟区块数")
    private long currentDelay;

    /**
     * 剩余延迟块数
     */
    @ApiModelProperty(name = "countdownDelay", value = "剩余延迟块数")
    private long countdownDelay;

    /**
     * 预计协议生效的区块高度
     */
    @ApiModelProperty(name = "effectiveHeight", value = "预计协议生效的区块高度")
    private Long effectiveHeight;

    /**
     * 协议生效状态
     */
    @ApiModelProperty(name = "status", value = "协议生效状态")
    private int status;

    public ProtocolContainerDTO(){}

    public ProtocolContainerDTO(ProtocolContainer protocolContainer){
        this.version = protocolContainer.getVersion();
        this.percent = protocolContainer.getPercent();
        this.currentPercent = protocolContainer.getCurrentPercent();
        this.roundIndex = protocolContainer.getRoundIndex();
        this.delay = protocolContainer.getDelay();
        this.currentDelay = protocolContainer.getCurrentDelay();
        this.countdownDelay = delay - countdownDelay;
        this.effectiveHeight = protocolContainer.getEffectiveHeight();
        this.status = protocolContainer.getStatus();
    }

    public ProtocolContainerDTO(ProtocolTempInfoPo tempInfoPo){
        this.version = tempInfoPo.getVersion();
        this.percent = tempInfoPo.getPercent();
        this.currentPercent = tempInfoPo.getCurrentPercent();
        this.roundIndex = tempInfoPo.getRoundIndex();
        this.delay = tempInfoPo.getDelay();
        this.currentDelay = tempInfoPo.getCurrentDelay();
        this.countdownDelay = delay - countdownDelay;
        this.effectiveHeight = tempInfoPo.getEffectiveHeight();
        this.status = tempInfoPo.getStatus();
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getCurrentDelay() {
        return currentDelay;
    }

    public void setCurrentDelay(long currentDelay) {
        this.currentDelay = currentDelay;
    }

    public long getCountdownDelay() {
        return countdownDelay;
    }

    public void setCountdownDelay(long countdownDelay) {
        this.countdownDelay = countdownDelay;
    }

    public int getCurrentPercent() {
        return currentPercent;
    }

    public void setCurrentPercent(int currentPercent) {
        this.currentPercent = currentPercent;
    }

    public Long getEffectiveHeight() {
        return effectiveHeight;
    }

    public void setEffectiveHeight(Long effectiveHeight) {
        this.effectiveHeight = effectiveHeight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
