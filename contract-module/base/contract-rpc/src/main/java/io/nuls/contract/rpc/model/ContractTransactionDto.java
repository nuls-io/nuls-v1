/**
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.contract.rpc.model;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.entity.txdata.CallContractData;
import io.nuls.contract.entity.txdata.ContractTransferData;
import io.nuls.contract.entity.txdata.CreateContractData;
import io.nuls.contract.entity.txdata.DeleteContractData;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.constant.TxStatusEnum;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.model.TransactionLogicData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.core.tools.str.StringUtils.EMPTY;

/**
 * @author: PierreLuo
 */
@ApiModel(value = "ContractTransactionDtoJSON")
public class ContractTransactionDto {

    @ApiModelProperty(name = "hash", value = "交易的hash值")
    private String hash;

    @ApiModelProperty(name = "type", value = "交易类型 ")
    private Integer type;

    @ApiModelProperty(name = "time", value = "交易发起时间")
    private Long time;

    @ApiModelProperty(name = "blockHeight", value = "区块高度")
    private Long blockHeight;

    @ApiModelProperty(name = "fee", value = "交易手续费")
    private Long fee;

    @ApiModelProperty(name = "value", value = "交易金额")
    private Long value;

    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    @ApiModelProperty(name = "scriptSig", value = "签名")
    private String scriptSig;

    @ApiModelProperty(name = "status", value = "交易状态 0:unConfirm(待确认), 1:confirm(已确认)")
    private Integer status;

    @ApiModelProperty(name = "confirmCount", value = "确认次数")
    private Long confirmCount;

    @ApiModelProperty(name = "size", value = "大小")
    private int size;

    @ApiModelProperty(name = "inputs", value = "输入")
    private List<InputDto> inputs;

    @ApiModelProperty(name = "outputs", value = "输出")
    private List<OutputDto> outputs;

    @ApiModelProperty(name = "txData", value = "合约调用数据")
    protected Map<String, Object> txData;

    @ApiModelProperty(name = "contractResult", value = "合约执行结果")
    protected ContractResultDto contractResult;

    public ContractTransactionDto(Transaction tx) {
        long bestBlockHeight = NulsContext.getInstance().getBestBlock().getHeader().getHeight();
        this.hash = tx.getHash().getDigestHex();
        this.type = tx.getType();
        this.time = tx.getTime();
        this.blockHeight = tx.getBlockHeight();
        this.fee = tx.getFee().getValue();
        this.size = tx.getSize();
        this.txData = makeTxData(tx);

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
        if (tx.getTransactionSignature() != null) {
            this.setScriptSig(Hex.encode(tx.getTransactionSignature()));
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
    }

    private Map<String,Object> makeTxData(Transaction tx) {
        Map<String,Object> result = new HashMap<>();
        TransactionLogicData txData = tx.getTxData();
        if(type == ContractConstant.TX_TYPE_CREATE_CONTRACT) {
            CreateContractData create = (CreateContractData) txData;
            result.put("data", new CreateContractDataDto(create));
        } else if(type == ContractConstant.TX_TYPE_CALL_CONTRACT) {
            CallContractData call = (CallContractData) txData;
            result.put("data", new CallContractDataDto(call));
        } else if(type == ContractConstant.TX_TYPE_DELETE_CONTRACT) {
            DeleteContractData delete = (DeleteContractData) txData;
            result.put("data", new DeleteContractDataDto(delete));
        } else if(type == ContractConstant.TX_TYPE_CONTRACT_TRANSFER) {
            ContractTransferData transfer = (ContractTransferData) txData;
            result.put("data", new ContractTransferDataDto(transfer));
        } else if(type == NulsConstant.TX_TYPE_COINBASE) {
            Map<String, String> map = MapUtil.createLinkedHashMap(1);
            map.put("sender", EMPTY);
            result.put("data", map);
        }
        return result;
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

    public Map<String, Object> getTxData() {
        return txData;
    }

    public void setTxData(Map<String, Object> txData) {
        this.txData = txData;
    }

    public ContractResultDto getContractResult() {
        return contractResult;
    }

    public void setContractResult(ContractResultDto contractResult) {
        this.contractResult = contractResult;
    }
}
