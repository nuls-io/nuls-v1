package io.nuls.utxo.accounts.util;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.VarInt;

public class UtxoAccountsUtil {
    private final static int TX_HASH_LENGTH = NulsDigestData.HASH_LENGTH;
    public static byte[] getTxHashBytes(byte[] fromBytes) {
        if(fromBytes == null || fromBytes.length < TX_HASH_LENGTH) {
            return null;
        }
        byte[] txBytes = new byte[TX_HASH_LENGTH];
        System.arraycopy(fromBytes, 0, txBytes, 0, TX_HASH_LENGTH);
        return txBytes;
    }

    public static byte[] getIndexBytes(byte[] fromBytes) {
        if(fromBytes == null || fromBytes.length < TX_HASH_LENGTH) {
            return null;
        }
        int length = fromBytes.length - TX_HASH_LENGTH;
        byte[] indexBytes = new byte[length];
        System.arraycopy(fromBytes, TX_HASH_LENGTH, indexBytes, 0, length);
        return indexBytes;
    }

    public static Integer getIndex(byte[] fromBytes) {
        byte[] indexBytes = getIndexBytes(fromBytes);
        if(indexBytes != null) {
            VarInt varInt = new VarInt(indexBytes, 0);
            return Math.toIntExact(varInt.value);
        }
        return null;
    }

}
