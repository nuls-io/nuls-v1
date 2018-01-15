package io.nuls.ledger.util;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.script.Script;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.entity.UtxoOutput;

import java.io.IOException;

public class UtxoTransferTool {

    public static UtxoOutput toOutput(UtxoOutputPo po) {
        UtxoOutput output = new UtxoOutput();
        output.setIndex(po.getOutIndex());
        output.setLockTime(po.getLockTime());
        output.setScript(new Script(po.getScript()));
        output.setTxHash(new NulsDigestData(Hex.decode(po.getTxHash())));
        output.setValue(po.getValue());
        output.setAddress(new Address(po.getAddress()).getHash160());
        //todo
//        output.setVersion(new NulsVersion(po.get));
        return output;
    }

    public static UtxoOutputPo toOutPutPojo(UtxoOutput output) throws IOException {
        UtxoOutputPo po = new UtxoOutputPo();
        po.setOutIndex(output.getIndex());
        po.setLockTime(po.getLockTime());
        po.setScript(output.getScriptBytes());
        po.setTxHash(output.getTxHash().getDigestHex());
        po.setValue(output.getValue());
        po.setAddress(new Address(po.getAddress()).getBase58());
        po.setHash(Hex.encode(Sha256Hash.hash(output.serialize())));
        //todo version
        return po;
    }
}
