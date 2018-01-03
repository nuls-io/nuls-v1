package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2018/1/3
 */
public class TxGroup extends BaseNulsData {

    private NulsDigestData blockHash;
    private List<Transaction> txList;
    private Map<String, Transaction> txMap;

    @Override
    public int size() {
        int size = 0;
        size += Utils.sizeOfSerialize(blockHash);
        size += VarInt.sizeOf(txList.size());
        size += this.getTxListLength();
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(blockHash);
        stream.writeVarInt(txList.size());
        for (Transaction data : txList) {
            stream.writeNulsData(data);
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.blockHash = byteBuffer.readHash();
        long txCount = byteBuffer.readVarInt();
        this.txList = new ArrayList<>();
        for (int i = 0; i < txCount; i++) {
            this.txList.add(byteBuffer.readTransaction());
        }
        initTxMap();
    }

    private int getTxListLength() {
        int size = 0;
        for (Transaction tx : txList) {
            size += Utils.sizeOfSerialize(tx);
        }
        return size;
    }

    private void initTxMap() {
        this.txMap = new HashMap<>();
        for (Transaction tx : txList) {
            txMap.put(tx.getHash().getDigestHex(), tx);
        }
    }

    public NulsDigestData getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(NulsDigestData blockHash) {
        this.blockHash = blockHash;
    }

    public List<Transaction> getTxList() {
        return txList;
    }

    public void setTxList(List<Transaction> txList) {
        this.txList = txList;
        initTxMap();
    }

    public Transaction getTx(String digestHex) {
        return txMap.get(digestHex);
    }
}
