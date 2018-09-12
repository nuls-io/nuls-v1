package io.nuls.utxo.accounts.storage.po;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UtxoAccountsBalancePo extends BaseNulsData {
    private byte[] owner;
    private Long inputBalance;
    private Long outputBalance;
    private Long lockedPermanentBalance;
    private Long unLockedPermanentBalance;
    private Long contractFromBalance;
    private Long contractToBalance;
    private Long blockHeight=0L;
    private int txIndex=0;
    private List<LockedBalance> lockedTimeList=new ArrayList<>();
    private List<LockedBalance> lockedHeightList=new ArrayList<>();
    public UtxoAccountsBalancePo() {
        this.inputBalance=0L;
        this.outputBalance=0L;
        this.lockedPermanentBalance=0L;
        this.unLockedPermanentBalance=0L;
        this.contractFromBalance=0L;
        this.contractToBalance=0L;
    }




    public byte[] getOwner() {
        return owner;
    }

    public void setOwner(byte[] owner) {
        this.owner = owner;
    }
    public Long getInputBalance() {
        return inputBalance;
    }

    public void setInputBalance(Long inputBalance) {
        this.inputBalance = inputBalance;
    }

    public Long getOutputBalance() {
        return outputBalance;
    }

    public void setOutputBalance(Long outputBalance) {
        this.outputBalance = outputBalance;
    }

    public Long getContractFromBalance() {
        return contractFromBalance;
    }

    public void setContractFromBalance(Long contractFromBalance) {
        this.contractFromBalance = contractFromBalance;
    }

    public Long getContractToBalance() {
        return contractToBalance;
    }

    public void setContractToBalance(Long contractToBalance) {
        this.contractToBalance = contractToBalance;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(int txIndex) {
        this.txIndex = txIndex;
    }

    public Long getLockedPermanentBalance() {
        return lockedPermanentBalance;
    }

    public void setLockedPermanentBalance(Long lockedPermanentBalance) {
        this.lockedPermanentBalance = lockedPermanentBalance;
    }

    public Long getUnLockedPermanentBalance() {
        return unLockedPermanentBalance;
    }

    public void setUnLockedPermanentBalance(Long unLockedPermanentBalance) {
        this.unLockedPermanentBalance = unLockedPermanentBalance;
    }

    public List<LockedBalance> getLockedTimeList() {
        return lockedTimeList;
    }

    public void setLockedTimeList(List<LockedBalance> lockedTimeList) {
        this.lockedTimeList = lockedTimeList;
    }

    public List<LockedBalance> getLockedHeightList() {
        return lockedHeightList;
    }

    public void setLockedHeightList(List<LockedBalance> lockedHeightList) {
        this.lockedHeightList = lockedHeightList;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(owner);
        stream.writeInt64(inputBalance);
        stream.writeInt64(outputBalance);
        stream.writeInt64(lockedPermanentBalance);
        stream.writeInt64(unLockedPermanentBalance);
        stream.writeInt64(contractFromBalance);
        stream.writeInt64(contractToBalance);
        stream.writeInt64(blockHeight);
        stream.writeUint32(txIndex);
        int lockedTimeListSize = lockedTimeList == null ? 0 : lockedTimeList.size();
        stream.writeVarInt(lockedTimeListSize);
        if (null != lockedTimeList) {
            for (LockedBalance balance : lockedTimeList) {
                stream.writeInt64(balance.getLockedTime());
                stream.writeInt64(balance.getLockedBalance());
            }
        }
        int lockedHeightListSize = lockedHeightList == null ? 0 : lockedHeightList.size();
        stream.writeVarInt(lockedHeightListSize);
        if (null != lockedHeightList) {
            for (LockedBalance balance : lockedHeightList) {
                stream.writeInt64(balance.getLockedTime());
                stream.writeInt64(balance.getLockedBalance());
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.owner = byteBuffer.readByLengthByte();
        this.inputBalance=byteBuffer.readInt64();
        this.outputBalance=byteBuffer.readInt64();
        this.lockedPermanentBalance=byteBuffer.readInt64();
        this.unLockedPermanentBalance=byteBuffer.readInt64();
        this.contractFromBalance=byteBuffer.readInt64();
        this.contractToBalance=byteBuffer.readInt64();
        this.blockHeight=byteBuffer.readInt64();
        this.txIndex=byteBuffer.readInt32();
        int lockedTimeCount = (int) byteBuffer.readVarInt();
        if (0 < lockedTimeCount) {
            List<LockedBalance> timeBalanceList = new ArrayList<>();
            for (int i = 0; i < lockedTimeCount; i++) {
                LockedBalance balance=new LockedBalance();
                balance.setLockedTime(byteBuffer.readInt64());
                balance.setLockedBalance(byteBuffer.readInt64());
                timeBalanceList.add(balance);
            }
            this.lockedTimeList = timeBalanceList;
        }
        int lockedHeightCount = (int) byteBuffer.readVarInt();
        if (0 < lockedHeightCount) {
            List<LockedBalance> heightBalanceList = new ArrayList<>();
            for (int i = 0; i < lockedHeightCount; i++) {
                LockedBalance balance=new LockedBalance();
                balance.setLockedTime(byteBuffer.readInt64());
                balance.setLockedBalance(byteBuffer.readInt64());
                heightBalanceList.add(balance);
            }
            this.lockedHeightList = heightBalanceList;
        }
    }


    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfBytes(owner);
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfUint32();

        size+= SerializeUtils.sizeOfVarInt(lockedTimeList == null ? 0 : lockedTimeList.size());
        if (null != lockedTimeList) {
            for (LockedBalance balance : lockedTimeList) {
                size += SerializeUtils.sizeOfInt64();
                size += SerializeUtils.sizeOfInt64();
            }
        }
        size += SerializeUtils.sizeOfVarInt(lockedHeightList == null ? 0 : lockedHeightList.size());
        if (null != lockedHeightList) {
            for (LockedBalance balance : lockedHeightList) {
                size += SerializeUtils.sizeOfInt64();
                size += SerializeUtils.sizeOfInt64();
            }
        }

        return size;

    }
}
