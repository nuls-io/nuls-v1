package io.nuls.rpc.entity;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.str.StringUtils;
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

    private Long fee;

    private Long value;

    private List<InputDto> inputs;

    private List<OutputDto> outputs;

    // -1 transfer,  1 receiver
    private Integer transferType;

    private String remark;

    private String scriptSig;
    // 0, unConfirm  1, confirm
    private Integer status;

    private Long confirmCount;

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
                this.setRemark(new String(tx.getRemark(), NulsContext.DEFAULT_ENCODING));
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
