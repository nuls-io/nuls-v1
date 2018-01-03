package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;

public class UtxoTool {


    public static UtxoInput toInput(UtxoInputPo po) {
        UtxoInput input = new UtxoInput();
        input.setFromId(po.getFromId());
        input.setTxHash(new NulsDigestData(Hex.decode(po.getTxHash())));
        input.setSign(new NulsSignData());
        //todo
        return input;
    }

    public static UtxoInputPo toInputPo(UtxoInput input) {
        UtxoInputPo po = new UtxoInputPo();
//        po.setFromId(input.getFrom().get);
        return null;
    }

    public static UtxoOutput toOutput(UtxoOutputPo po) {
        return null;
    }

    public static UtxoOutputPo toOutputPo(UtxoOutput output) {
        return null;
    }
}
