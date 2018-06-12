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

package io.nuls.account.ledger.sdk.model.dto;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

   /* public TransactionDto(Transaction tx) {
        long bestBlockHeight = NulsContext.getInstance().getBestBlock().getHeader().getHeight();
        this.hash = tx.getHash().getDigestHex();
        this.type = tx.getType();
        this.time = tx.getTime();
        this.blockHeight = tx.getBlockHeight();
        this.fee = tx.getFee().getValue();
        this.size = tx.getSize();
        if (this.blockHeight > 0 || TxStatusEnum.CONFIRMED.equals(tx.getStatus())) {
            this.confirmCount = bestBlockHeight - this.blockHeight;
        } else {
            this.confirmCount = 0L;
        }
        if (TxStatusEnum.CONFIRMED.equals(tx.getStatus())) {
            this.status = 1;
        } else {
            this.status = 0;
        }

        if (tx.getRemark() != null) {
            try {
                this.setRemark(new String(tx.getRemark(), NulsConfig.DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException e) {
                this.setRemark(Hex.encode(tx.getRemark()));
            }
        }
        if (tx.getScriptSig() != null) {
            this.setScriptSig(Hex.encode(tx.getScriptSig()));
        }

        CoinData coinData = tx.getCoinData();
        List<InputDto> inputs = new ArrayList<>();
        if(coinData != null) {
            List<Coin> froms = coinData.getFrom();
            for(Coin from : froms) {
                inputs.add(new InputDto(from));
            }
        }
        this.inputs = inputs;
    }*/

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
