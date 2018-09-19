package io.nuls.kernel.script;

import io.nuls.kernel.utils.SerializeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptUtil {

    /**
     * 根据交易签名和公钥生成解锁脚本 （P2PSH）
     *
     * @param sigByte    交易签名
     * @param pubkeyByte 公钥
     * @return Script      生成的解锁脚本
     */
    public static Script createP2PKHInputScript(byte[] sigByte, byte[] pubkeyByte) {
        return ScriptBuilder.createNulsInputScript(sigByte, pubkeyByte);
    }

    /**
     * 根据输出地址生成锁定脚本
     *
     * @param address 输出地址
     * @return Script  生成的锁定脚本
     */
    public static Script createP2PKHOutputScript(byte[] address) {
        return ScriptBuilder.createOutputScript(address, 1);
    }


    /**
     * M-N多重签名模式下根据多个公钥和M-N生成赎回脚本
     *
     * @param pub_keys 公钥列表
     * @param m        表示至少需要多少个签名验证通过
     * @return Script  生成的锁定脚本
     */
    public static Script creatRredeemScript(List<String> pub_keys, int m) {
        return ScriptBuilder.createNulsRedeemScript(m, pub_keys);
    }

    /**
     * M-N多重签名模式下根据多个公钥和M-N生成解锁脚本（N就是公钥列表长度）
     *
     * @param signatures      签名列表
     * @param multisigProgram 当交易为P2SH时，表示的就是赎回脚本
     * @return Script     生成的解鎖脚本
     */
    public static Script createP2SHInputScript(List<byte[]> signatures, Script multisigProgram) {
        return ScriptBuilder.createNulsP2SHMultiSigInputScript(signatures, multisigProgram);
    }

    /**
     * M-N多重签名模式下根据多个公钥和M-N生成锁定脚本（N就是公钥列表长度）
     *
     * @param redeemScript 贖回腳本
     * @return Script  生成的锁定脚本
     */
    public static Script createP2SHOutputScript(Script redeemScript) {
        return ScriptBuilder.createP2SHOutputScript(redeemScript);
    }

    /**
     * M-N多重签名模式下，根据输出地址生成锁定脚本
     *
     * @param address 输出地址
     * @return Script  生成的锁定脚本
     */
    public static Script createP2SHOutputScript(byte[] address) {
        return ScriptBuilder.createOutputScript(address, 0);
    }

    public static void main(String[] args) {
        /**
         * 脚本序列化测试代码
         * */
        try {
            /*TransferTransaction tx = new TransferTransaction();
            tx.setTime(TimeService.currentTimeMillis());

            CoinData coinData = new CoinData();
            List<Coin> from = new ArrayList<Coin>();
            for(int i=0;i<3;i++){
                String addr = "tx_hash+index"+1;
                Coin from_coin = new Coin(addr.getBytes(),Na.valueOf(100));
                from.add(from_coin);
            }
            List<Coin> to = new ArrayList<Coin>();
            for(int i=0;i<3;i++){
                String addr = "Nsdybg1xmP7z4PTUKKN26stocrJ1qrU"+1;
                Coin to_coin = new Coin(AddressTool.getAddress(addr),Na.valueOf(100));
                to_coin.setScript(ScriptBuilder.createOutputScript(AddressTool.getAddress(addr),0));
                to.add(to_coin);
            }
            coinData.setFrom(from);
            coinData.setTo(to);
            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            P2PKHScriptSig sig = new P2PKHScriptSig();
            List<Script> scripts = new ArrayList<Script>();
            for(int i=0;i<3;i++){
                String addr = "Nse84JjkxBoR9zXLGftu8X8xjsfnwxW"+1;
                scripts.add(ScriptBuilder.createOutputScript(AddressTool.getAddress(addr),1));
            }
            sig.setScripts(scripts);
            sig.setPublicKey("publickey".getBytes());

            sig.setSignData(new NulsSignData());
            tx.setBlockSignature(sig.serialize());

            byte[] bytes = tx.serialize();


            TransferTransaction tx2 = new TransferTransaction();
            tx2.parse(new NulsByteBuffer(bytes));
            System.out.println(bytes.length);
            for (Coin coin : tx2.getCoinData().getTo()) {
                System.out.println(coin.getNa());
                System.out.println(coin.getScript().getChunks());
            }
            P2PKHScriptSig scriptSig = new P2PKHScriptSig();
            scriptSig.parse(new NulsByteBuffer(tx2.getBlockSignature()));
            for (Script script:scriptSig.getScripts()) {
                System.out.println(script.getChunks());
            }*/

            /**
             * 脚本创建测试代码
             * */
            //P2PKHInput
            byte[] signbyte = "cVLwRLTvz3BxDAWkvS3yzT9pUcTCup7kQnfT2smRjvmmm1wAP6QT".getBytes();
            byte[] pubkeyByte = "public_key".getBytes();
            Script inputScript = createP2PKHInputScript(signbyte, pubkeyByte);
            System.out.println("P2PKH_INPUT:" + inputScript.getChunks());
            //System.out.println(new String(inputScript.getChunks().get(0).data));
            //P2PKHOutput
            byte[] addrByte = "Nsdybg1xmP7z4PTUKKN26stocrJ1qrUJ".getBytes();
            Script outputScript = createP2PKHOutputScript(addrByte);
            System.out.println("P2PKH_OUTPUT:" + outputScript.getChunks());
            //redeemScript
            List<String> pub_keys = new ArrayList<String>();
            for (int i = 0; i < 3; i++) {
                pub_keys.add("Nsdybg1xmP7z4PTUKKN26stocrJ1qrU" + i);
            }
            Script redeemScript = creatRredeemScript(pub_keys, 2);
            System.out.println("REDEEM:" + redeemScript.getChunks());
            //P2SHInput
            List<byte[]> signBytes = new ArrayList<byte[]>();
            for (int i = 0; i < 3; i++) {
                signBytes.add("cVLwRLTvz3BxDAWkvS3yzT9pUcTCup7kQnfT2smRjvmmm1wAP6Q".getBytes());
            }
            System.out.println(redeemScript.getProgram().length);
            Script p2shInput = createP2SHInputScript(signBytes, redeemScript);
            System.out.println("P2SH_INPUT:" + p2shInput.getChunks());
            ScriptChunk scriptChunk = p2shInput.getChunks().get(p2shInput.getChunks().size() - 1); //scriptChunk.data存放的就是赎回脚本的序列化信息
            Script redeemScriptParse = new Script(scriptChunk.data);
            System.out.println(redeemScriptParse.getChunks());
            //P2SHOutput
            Script p2shOutput = createP2SHOutputScript(redeemScript);
            System.out.println("P2SH_OUTPUT:" + p2shOutput.getChunks());

            System.out.println(Arrays.toString(SerializeUtils.sha256hash160("03a690c7f3b07e320566162b0ff7d79c8c9f453c0a4a13305fcd90f4e4f4cf215c".getBytes())));

            /**
             * P2PKH脚本验证测试代码
             * */
           /* Na values = Na.valueOf(10);
            byte[] from = "".getBytes();                               //输入地址
            byte[] to   = "".getBytes();                               //输出地址
            String pub_key = "";                                       //输入账户公钥
            String password ="";
            String remark ="";
            Na price = Na.valueOf(5);
            TransferTransaction tx = new TransferTransaction();
            tx.setTime(TimeService.currentTimeMillis());
            CoinData coinData = new CoinData();
            Coin toCoin = new Coin(to, values);
            coinData.getTo().add(toCoin);
            if (price == null) {
                price = TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES;
            }
            CoinDataResult coinDataResult = accountLedgerService.getCoinData(from, values, tx.size() +  + coinData.size(), price);
            if (!coinDataResult.isEnough()) {
                //return Result.getFailed(AccountLedgerErrorCode.INSUFFICIENT_BALANCE);
                System.out.println("余额不足！");
                return;
            }
            coinData.setFrom(coinDataResult.getCoinList());
            if (coinDataResult.getChange() != null) {
                coinData.getTo().add(coinDataResult.getChange());
            }
            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            P2PKHScriptSig sig = new P2PKHScriptSig();
            //sig.setPublicKey(account.getPubKey());*/
            //用当前交易的hash和账户的私钥账户
            //sig.setSignData(accountService.signDigest(tx.getHash().getDigestBytes(), account, password));
            //tx.setBlockSignature(sig.serialize());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
