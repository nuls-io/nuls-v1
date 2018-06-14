package io.nuls.sdk.utils;

import io.nuls.sdk.constant.KernelErrorCode;
import io.nuls.sdk.constant.TransactionConstant;
import io.nuls.sdk.crypto.ECKey;
import io.nuls.sdk.exception.NulsRuntimeException;
import io.nuls.sdk.model.*;
import io.nuls.sdk.script.P2PKHScriptSig;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionTool {

    private static final Map<Integer, Class<? extends Transaction>> TYPE_TX_MAP = new HashMap<>();

    public static void init() {
        TYPE_TX_MAP.put(TransactionConstant.TX_TYPE_TRANSFER, TransferTransaction.class);
    }

    public static Transaction createTransferTx(List<Coin> inputs, List<Coin> outputs, byte[] remark) {
        TransferTransaction tx = new TransferTransaction();
        CoinData coinData = new CoinData();
        coinData.setFrom(inputs);
        coinData.setTo(outputs);
        tx.setCoinData(coinData);
        tx.setTime(TimeService.currentTimeMillis());
        tx.setRemark(remark);
        return tx;
    }

    public static Transaction signTransaction(Transaction tx, ECKey ecKey) throws IOException {
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        P2PKHScriptSig sig = new P2PKHScriptSig();
        sig.setPublicKey(ecKey.getPubKey());
        sig.setSignData(signDigest(tx.getHash().getDigestBytes(), ecKey));
        tx.setScriptSig(sig.serialize());
        return tx;
    }

    public static NulsSignData signDigest(byte[] digest, ECKey ecKey) {
        byte[] signbytes = ecKey.sign(digest);
        NulsSignData nulsSignData = new NulsSignData();
        nulsSignData.setSignAlgType(NulsSignData.SIGN_ALG_ECC);
        nulsSignData.setSignBytes(signbytes);
        return nulsSignData;
    }

    public static Transaction getInstance(NulsByteBuffer byteBuffer) throws Exception {
        int txType = (int) new NulsByteBuffer(byteBuffer.getPayloadByCursor()).readVarInt();
        Class<? extends Transaction> txClass = TYPE_TX_MAP.get(txType);
        if (null == txClass) {
            throw new NulsRuntimeException(KernelErrorCode.FAILED, "transaction type not exist!");
        }
        Transaction tx = byteBuffer.readNulsData(txClass.newInstance());
        return tx;
    }
}
