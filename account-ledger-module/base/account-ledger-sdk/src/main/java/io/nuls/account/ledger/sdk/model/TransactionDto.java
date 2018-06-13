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
 *
 */

package io.nuls.account.ledger.sdk.model;

import io.nuls.sdk.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/14
 */
public class TransactionDto {

    /**
     * 交易的hash值
     */
    private String hash;

    /**
     * 交易类型
     */
    private Integer type;

    /**
     * 交易发起时间
     */
    private Long time;

    /**
     * 区块高度
     */
    private Long blockHeight;

    /**
     * 交易手续费
     */
    private Long fee;

    /**
     * 交易金额
     */
    private Long value;

    /**
     * 备注
     */
    private String remark;

    /**
     * 签名
     */
    private String scriptSig;

    /**
     * 交易状态 0:unConfirm(待确认), 1:confirm(已确认)
     */
    private Integer status;

    /**
     * 确认次数
     */
    private Long confirmCount;

    /**
     * 大小
     */
    private int size;

    /**
     * 输入
     */
    private List<InputDto> inputs;

    /**
     * 输出
     */
    private List<OutputDto> outputs;

    public TransactionDto(Map<String, Object> map) {
        this.hash = (String) map.get("hash");
        this.type = (Integer) map.get("type");
        this.time = StringUtils.parseLong(map.get("time"));
        this.blockHeight = StringUtils.parseLong(map.get("blockHeight"));
        this.fee = StringUtils.parseLong(map.get("fee"));
        this.value = StringUtils.parseLong(map.get("value"));
        this.size = (int) map.get("size");
        this.confirmCount = StringUtils.parseLong(map.get("confirmCount"));
        this.status = (Integer) map.get("status");
        this.remark = (String) map.get("remark");
        this.scriptSig = (String) map.get("scriptSig");
        this.inputs = (List<InputDto>)map.get("inputs");
        this.outputs = (List<OutputDto>)map.get("outputs");
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public List<InputDto> getInputs() {
        return inputs;
    }

    public void setInputs(List<InputDto> inputs) {
        this.inputs = inputs;
    }

    public List<OutputDto> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<OutputDto> outputs) {
        this.outputs = outputs;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getScriptSig() {
        return scriptSig;
    }

    public void setScriptSig(String scriptSig) {
        this.scriptSig = scriptSig;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getConfirmCount() {
        return confirmCount;
    }

    public void setConfirmCount(Long confirmCount) {
        this.confirmCount = confirmCount;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }


}
