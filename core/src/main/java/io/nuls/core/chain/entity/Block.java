package io.nuls.core.chain.entity;

import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.chain.manager.BlockValidatorManager;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.NulsDataValidator;

import java.io.IOException;
import java.util.List;

/**
 * @author win10
 * @date 2017/10/30
 */
public class Block extends BaseNulsData implements NulsCloneable {

    private BlockHeader header;

    private List<Transaction> txs;

    private byte[] extend;

    public Block() {
        initValidators();
    }

    private void initValidators() {
        List<NulsDataValidator> list = BlockValidatorManager.getValidators();
        for (NulsDataValidator<Block> validator : list) {
            this.registerValidator(validator);
        }
    }

    @Override
    public int size() {
        int size = header.size();
        size += Utils.sizeOfSerialize(extend);
        for (Transaction tx : txs) {
            size += tx.size();
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        header.serializeToStream(stream);
        stream.writeBytesWithLength(extend);
        for (Transaction tx : txs) {
            stream.write(tx.serialize());
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        header = new BlockHeader();
        header.parse(byteBuffer);
        extend = byteBuffer.readByLengthByte();
        try {
            txs = TransactionManager.getInstances(byteBuffer);
        } catch (Exception e) {
            throw new NulsRuntimeException(ErrorCode.PARSE_OBJECT_ERROR);
        }
    }

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }

    public BlockHeader getHeader() {
        return header;
    }

    public void setHeader(BlockHeader header) {
        this.header = header;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    @Override
    public Object copy() {
        //Temporary non realization
        try {
            return this.clone();
        } catch (CloneNotSupportedException e) {
            Log.error(e);
            return null;
        }
    }
}
