/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

    public static UtxoOutputPo toOutputPojo(UtxoOutput output) {
        UtxoOutputPo po = new UtxoOutputPo();
        po.setTxHash(output.getTxHash().getDigestHex());
        po.setOutIndex(output.getIndex());
        po.setValue(output.getValue());
        po.setLockTime(po.getLockTime());
        po.setAddress(new Address(po.getAddress()).getBase58());
        po.setScript(output.getScriptBytes());
        return po;
    }

    public static UtxoInputPo toInputPojo(UtxoInput input) {
        UtxoInputPo po = new UtxoInputPo();
        po.setTxHash(input.getTxHash().getDigestHex());
        po.setInIndex(input.getIndex());
        po.setFromIndex(input.getFromIndex());
        po.setSign(input.getSign().getSignBytes());
        return po;
    }

}
