package io.nuls.rpc.entity;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoInput;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDto {

    private String hash;

    private Integer type;

    private Integer index;

    private Long time;

    private Long blockHeight;

    private Double fee;

    private Double value;

    private List<InputDto> inputs;

    private List<OutputDto> outputs;

    private Integer transferType;

    private String remark;

    private String sign;

    public TransactionDto(Transaction tx) {
        this.hash = tx.getHash().getDigestHex();
        this.type = tx.getType();
        this.time = tx.getTime();
        this.blockHeight = tx.getBlockHeight();
        this.setFee(tx.getFee().toDouble());
        this.setTransferType(tx.getTransferType());
        if (tx.getRemark() != null) {
            try {
                this.setRemark(new String(tx.getRemark(), NulsContext.DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException e) {
                this.setRemark(Hex.encode(tx.getRemark()));
            }
        }
        this.setSign(tx.getSign().getSignHex());

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
        boolean isTransfer = false;
        double value = 0d;
        for (InputDto input : inputs) {
            if (address.equals(input.getAddress())) {
                if (!isTransfer) isTransfer = true;
                value += input.getValue();
            }
        }
        for (OutputDto output : outputs) {
            if (address.equals(output.getAddress())) {
                if (!isTransfer) isTransfer = true;
                value -= output.getValue();
            }
        }
        value = Math.abs(value);
        this.value = value;
        if (isTransfer) {
            this.transferType = Transaction.TRANSFER_SEND;
        } else {
            this.transferType = Transaction.TRANSFER_RECEIVE;
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

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
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

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

}
