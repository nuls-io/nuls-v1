/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.script.Script;
import io.nuls.kernel.script.ScriptBuilder;
import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.protocol.model.tx.TransferTransaction;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/10/5
 */
public class ScriptTransactionTestTool extends BaseTest {
    //@Test
    public void test() throws Exception {
        NulsContext.MAIN_NET_VERSION = 2;

        TransferTransaction tx = new TransferTransaction();
        tx.setRemark("test script".getBytes());


        CoinData data = new CoinData();
        Coin coin = new Coin();
        coin.setOwner(ArraysTool.concatenate(Hex.decode("0020dab71b3cd376e2ccf2f290e384d2917cc0929f8de582f63a01fc15144fe38371"), new byte[]{0}));
        coin.setNa(Na.parseNuls(9997));
        coin.setLockTime(0);
        List<Coin> from = new ArrayList<>();
        from.add(coin);
        data.setFrom(from);
        Coin toCoin = new Coin();
        toCoin.setLockTime(0);
        Script script = ScriptBuilder.createOutputScript(AddressTool.getAddress("NsdvuzHyQJEJkz4LEKweDeCs97845xN9"),1);
        toCoin.setOwner(script.getProgram());
        toCoin.setNa(Na.parseNuls(9994));

        List<Coin> to = new ArrayList<>();
        to.add(toCoin);
        data.setTo(to);
        tx.setCoinData(data);

//        ECKey ecKey = ECKey.fromPrivate(new BigInteger(1,Hex.decode("00b491621168dffd80c4684f7445ef378ba4d381b2fe2a7b1fbf905864ed8fbeb9")));
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(1,Hex.decode("4b19caef601a45531b7068430a5b0e380a004001f14bfec025ddf16d5d87fa8e")));
        List<ECKey> signEckeys = new ArrayList<>();
        signEckeys.add(ecKey);
        List<ECKey> scriptEckeys = new ArrayList<>();
        SignatureUtil.createTransactionSignture(tx, scriptEckeys, signEckeys);

        String param = "{\"txHex\": \"" + Hex.encode(tx.serialize()) + "\"}";
        String res = post("http://127.0.0.1:7001/api/accountledger/transaction/valiTransaction", param, "utf-8");
        System.out.println(res);
        res = post("http://127.0.0.1:7001/api/accountledger/transaction/broadcast", param, "utf-8");
        System.out.println(res);
    }
    //@Test
    public void test1() throws IOException {

        NulsContext.MAIN_NET_VERSION = 2;

        TransferTransaction tx = new TransferTransaction();
        tx.setRemark("test script".getBytes());


        CoinData data = new CoinData();
        Coin coin = new Coin();
        coin.setOwner(ArraysTool.concatenate(Hex.decode("0020dab71b3cd376e2ccf2f290e384d2917cc0929f8de582f63a01fc15144fe38371"), new byte[]{0}));
        coin.setNa(Na.parseNuls(9997));
        coin.setLockTime(0);
        List<Coin> from = new ArrayList<>();
        from.add(coin);
        data.setFrom(from);
        Coin toCoin = new Coin();
        toCoin.setLockTime(0);
        Script script = ScriptBuilder.createOutputScript(AddressTool.getAddress("NsdvuzHyQJEJkz4LEKweDeCs97845xN9"),1);
        toCoin.setOwner(script.getProgram());
        toCoin.setNa(Na.parseNuls(9994));

        List<Coin> to = new ArrayList<>();
        to.add(toCoin);
        data.setTo(to);
        tx.setCoinData(data);

//        ECKey ecKey = ECKey.fromPrivate(new BigInteger(1,Hex.decode("00b491621168dffd80c4684f7445ef378ba4d381b2fe2a7b1fbf905864ed8fbeb9")));
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(1,Hex.decode("4b19caef601a45531b7068430a5b0e380a004001f14bfec025ddf16d5d87fa8e")));
        List<ECKey> signEckeys = new ArrayList<>();
        signEckeys.add(ecKey);
        List<ECKey> scriptEckeys = new ArrayList<>();
        SignatureUtil.createTransactionSignture(tx, scriptEckeys, signEckeys);

        String param = "{\"txHex\": \"" + Hex.encode(tx.serialize()) + "\"}";
        String res = post("http://127.0.0.1:7001/api/accountledger/transaction/valiTransaction", param, "utf-8");
        System.out.println(res);
         res = post("http://127.0.0.1:7001/api/accountledger/transaction/broadcast", param, "utf-8");
        System.out.println(res);
    }
}
