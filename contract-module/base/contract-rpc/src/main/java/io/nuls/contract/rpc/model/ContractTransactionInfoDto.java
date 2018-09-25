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

package io.nuls.contract.rpc.model;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.storage.po.TransactionInfoPo;
import io.nuls.kernel.model.NulsDigestData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: PierreLuo
 * @date: 2018/7/23
 */
@ApiModel(value = "ContractTransactionInfoDtoJSON")
public class ContractTransactionInfoDto {

    private static final String CREATE_INFO = "contract create";
    private static final String CALL_INFO = "contract call";
    private static final String DELETE_INFO = "contract delete";
    private static final String TRANSFER_INFO = "contract transfer";

    @ApiModelProperty(name = "hash", value = "交易的hash值")
    private String txHash;
    @ApiModelProperty(name = "blockHeight", value = "区块高度")
    private long blockHeight;
    @ApiModelProperty(name = "time", value = "交易发起时间")
    private long time;
    @ApiModelProperty(name = "txType", value = "交易类型")
    private int txType;
    @ApiModelProperty(name = "status", value = "交易状态")
    private byte status;
    @ApiModelProperty(name = "info", value = "交易信息")
    private String info;

    public ContractTransactionInfoDto() {

    }

    public ContractTransactionInfoDto(TransactionInfoPo po) {
        if(po == null) {
            return;
        }
        this.txHash = po.getTxHash().getDigestHex();
        this.blockHeight = po.getBlockHeight();
        this.time = po.getTime();
        this.txType = po.getTxType();
        this.status = po.getStatus();
        if(this.txType == ContractConstant.TX_TYPE_CREATE_CONTRACT) {
            this.info = CREATE_INFO;
        } else if(this.txType == ContractConstant.TX_TYPE_CALL_CONTRACT) {
            this.info = CALL_INFO;
        } else if(this.txType == ContractConstant.TX_TYPE_DELETE_CONTRACT) {
            this.info = DELETE_INFO;
        } else if(this.txType == ContractConstant.TX_TYPE_CONTRACT_TRANSFER) {
            this.info = TRANSFER_INFO;
        }
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int compareTo(long thatTime) {
        if(this.time > thatTime) {
            return -1;
        } else if(this.time < thatTime) {
            return 1;
        }
        return 0;
    }
}
