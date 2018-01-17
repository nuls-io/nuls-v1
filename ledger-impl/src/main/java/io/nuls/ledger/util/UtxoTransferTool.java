package io.nuls.ledger.util;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.crypto.script.Script;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.entity.UtxoInput;
import io.nuls.ledger.entity.UtxoOutput;

public class UtxoTransferTool {

    public static UtxoOutput toOutput(UtxoOutputPo po) {
        UtxoOutput output = new UtxoOutput();
        output.setTxHash(new NulsDigestData(Hex.decode(po.getTxHash())));
        output.setIndex(po.getOutIndex());
        output.setLockTime(po.getLockTime());
        output.setValue(po.getValue());
        output.setAddress(new Address(po.getAddress()).getHash160());
        output.setScript(new Script(po.getScript()));
        return output;
    }

    public static UtxoOutputPo toOutPutPojo(UtxoOutput output) {
        UtxoOutputPo po = new UtxoOutputPo();
        po.setTxHash(output.getTxHash().getDigestHex());
        po.setOutIndex(output.getIndex());
        po.setValue(output.getValue());
        po.setLockTime(po.getLockTime());
        po.setAddress(new Address(po.getAddress()).getBase58());
        po.setScript(output.getScriptBytes());
        return po;
    }

    public static UtxoInputPo toOutPutPojo(UtxoInput input) {
        UtxoInputPo po = new UtxoInputPo();
        po.setTxHash(input.getTxHash().getDigestHex());
        po.setInIndex(input.getIndex());
        po.setFromIndex(input.getFromIndex());
        po.setSign(input.getSign().getSignBytes());
        return po;
    }

}
