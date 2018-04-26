package io.nuls.rpc.entity;

import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoInput;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.constant.TxStatusEnum;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Transaction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "transactionJSON")
public class TransactionDto {

    @ApiModelProperty(name = "hash", value = "交易的hash值")
    private String hash;

    @ApiModelProperty(name = "type",
                        value = "交易类型 " +
                                "1:共识奖励, " +
                                "2:转账交易, " +
                                "3:锁仓交易, " +
                                "4:解锁交易, " +
                                "5:零钱换整, " +
                                "11:设置别名, " +
                                "90:注册共识节点, " +
                                "91:加入共识, " +
                                "92:退出共识, " +
                                "93:黄牌惩罚, " +
                                "94:红牌惩罚, " +
                                "95:删除共识节点")
    private Integer type;

    @ApiModelProperty(name = "index", value = "所在打包区块里的索引")
    private Integer index;

    @ApiModelProperty(name = "time", value = "交易发起时间")
    private Long time;

    @ApiModelProperty(name = "blockHeight", value = "区块高度")
    private Long blockHeight;

    @ApiModelProperty(name = "fee", value = "交易手续费")
    private Long fee;

    @ApiModelProperty(name = "value", value = "交易金额")
    private Long value;

    @ApiModelProperty(name = "inputs", value = "交易输入")
    private List<InputDto> inputs;

    @ApiModelProperty(name = "outputs", value = "交易输出")
    private List<OutputDto> outputs;

    @ApiModelProperty(name = "transferType", value = "1:receiver(转入), -1:transfer(转出)")
    private Integer transferType;

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

    public TransactionDto(Transaction tx) {
        long bestBlockHeight = NulsContext.getInstance().getBestBlock().getHeader().getHeight();
        this.hash = tx.getHash().getDigestHex();
        this.type = tx.getType();
        this.time = tx.getTime();
        this.blockHeight = tx.getBlockHeight();
        this.setFee(tx.getFee().getValue());
        this.setTransferType(tx.getTransferType());
        this.setIndex(tx.getIndex());
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
        if(tx.getScriptSig()!=null){
            this.setScriptSig(Hex.encode(tx.getScriptSig()));
        }

        List<InputDto> inputs = new ArrayList<>();
        List<OutputDto> outputs = new ArrayList<>();

        if (tx instanceof AbstractCoinTransaction) {
            AbstractCoinTransaction coinTx = (AbstractCoinTransaction) tx;
            UtxoData utxoData = (UtxoData) coinTx.getCoinData();
            for (UtxoInput input : utxoData.getInputs()) {
                inputs.add(new InputDto(input));
            }

            for (UtxoOutput output : utxoData.getOutputs()) {
                outputs.add(new OutputDto(output));
            }
        }
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public TransactionDto(Transaction tx, String address) {
        this(tx);
        if(tx.getType() >= TransactionConstant.TX_TYPE_REGISTER_AGENT) {
            this.value = 0L;
            return;
        }
        if (StringUtils.isNotBlank(address)) {
            boolean isTransfer = false;
            long value = 0;
            for (InputDto input : inputs) {
                if (address.equals(input.getAddress())) {
                    if (!isTransfer) {
                        isTransfer = true;
                    }
                    value -= input.getValue();
                }
            }

            for (OutputDto output : outputs) {
                if (address.equals(output.getAddress())) {
                    value += output.getValue();
                }
            }
            if (isTransfer) {
                this.transferType = -1;
                value += this.getFee();
            } else {
                this.transferType = 1;
            }
            this.value = value;
        }
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

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
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

    public Integer getTransferType() {
        return transferType;
    }

    public void setTransferType(Integer transferType) {
        this.transferType = transferType;
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
