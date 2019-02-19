/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
 *
 */
package io.nuls.consensus.poc.config;

import io.nuls.account.service.AccountService;
import io.nuls.consensus.poc.model.BlockExtendsData;
import io.nuls.core.tools.cfg.ConfigLoader;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.io.StringFileLoader;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.*;

import io.nuls.kernel.script.BlockSignature;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.model.tx.CoinBaseTransaction;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 */
public final class GenesisBlock extends Block {

    private final static String GENESIS_BLOCK_FILE = "block/genesis-block.json";

    private static final String CONFIG_FILED_TIME = "time";
    private static final String CONFIG_FILED_HEIGHT = "height";
    private static final String CONFIG_FILED_TXS = "txs";
    private static final String CONFIG_FILED_ADDRESS = "address";
    private static final String CONFIG_FILED_AMOUNT = "amount";
    private static final String CONFIG_FILED_LOCK_TIME = "lockTime";
    private static final String CONFIG_FILED_REMARK = "remark";
    private static final String priKey = "009cf05b6b3fe8c09b84c13783140c0f1958e8841f8b6f894ef69431522bc65712";

    private static GenesisBlock INSTANCE = new GenesisBlock();

    private transient long blockTime;

    private transient int status = 0;

    public static GenesisBlock getInstance() throws Exception {
        if (INSTANCE.status == 0) {
            String json = null;
            try {
                String mode = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, "mode", "main");
                if ("main".equals(mode)) {
                    json = StringFileLoader.read(GENESIS_BLOCK_FILE);
                } else {
                    json = StringFileLoader.read(mode + "/" + GENESIS_BLOCK_FILE);
                }
            } catch (NulsException e) {
                Log.error(e);
            }
            INSTANCE.init(json);
        }
        return INSTANCE;
    }

    private GenesisBlock() {

    }

    private synchronized void init(String json) throws Exception {
        if (status > 0) {
            return;
        }
        Map<String, Object> jsonMap = null;
        try {
            jsonMap = JSONUtils.json2map(json);
        } catch (Exception e) {
            Log.error(e);
        }
        String time = (String) jsonMap.get(CONFIG_FILED_TIME);
        AssertUtil.canNotEmpty(time, KernelErrorCode.CONFIG_ERROR.getMsg());
        blockTime = Long.parseLong(time);
        this.initGengsisTxs(jsonMap);
        this.fillHeader(jsonMap);
        ValidateResult validateResult = this.verify();
        if (validateResult.isFailed()) {
            throw new NulsRuntimeException(validateResult.getErrorCode());
        }
        this.status = 1;
    }

    private void initGengsisTxs(Map<String, Object> jsonMap) throws Exception {
        List<Map<String, Object>> list = (List<Map<String, Object>>) jsonMap.get(CONFIG_FILED_TXS);
        if (null == list || list.isEmpty()) {
            throw new NulsRuntimeException(KernelErrorCode.CONFIG_ERROR);
        }

        CoinData coinData = new CoinData();

        for (Map<String, Object> map : list) {
            String address = (String) map.get(CONFIG_FILED_ADDRESS);
            AssertUtil.canNotEmpty(address, KernelErrorCode.NULL_PARAMETER.getMsg());

            Double amount = Double.valueOf("" + map.get(CONFIG_FILED_AMOUNT));
            AssertUtil.canNotEmpty(amount, KernelErrorCode.NULL_PARAMETER.getMsg());
            Long lockTime = Long.valueOf("" + map.get(CONFIG_FILED_LOCK_TIME));

            Address ads = Address.fromHashs(address);

            Coin coin = new Coin(ads.getAddressBytes(), Na.parseNuls(amount), lockTime == null ? 0 : lockTime.longValue());
            coinData.addTo(coin);
        }

        CoinBaseTransaction tx = new CoinBaseTransaction();
        tx.setTime(this.blockTime);
        tx.setCoinData(coinData);
        String remark = (String) jsonMap.get(CONFIG_FILED_REMARK);
        if (StringUtils.isNotBlank(remark)) {
            tx.setRemark(Hex.decode(remark));
        }
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        List<Transaction> txlist = new ArrayList<>();
        txlist.add(tx);
        setTxs(txlist);
    }


    private void fillHeader(Map<String, Object> jsonMap) throws NulsException {
        Integer height = (Integer) jsonMap.get(CONFIG_FILED_HEIGHT);
        AssertUtil.canNotEmpty(height, KernelErrorCode.CONFIG_ERROR.getMsg());

        BlockHeader header = new BlockHeader();
        this.setHeader(header);
        header.setHeight(height);
        header.setTime(blockTime);
        header.setPreHash(NulsDigestData.calcDigestData(new byte[35]));
        header.setTxCount(this.getTxs().size());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (Transaction tx : this.getTxs()) {
            txHashList.add(tx.getHash());
        }
        header.setMerkleHash(NulsDigestData.calcMerkleDigestData(txHashList));

        BlockExtendsData data = new BlockExtendsData();
        data.setRoundIndex(1);
        data.setRoundStartTime(header.getTime() - ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND * 1000);
        data.setConsensusMemberCount(1);
        data.setPackingIndexOfRound(1);
        try {
            header.setExtend(data.serialize());
        } catch (IOException e) {
            throw new NulsRuntimeException(e);

        }
        header.setHash(NulsDigestData.calcDigestData(header));

        BlockSignature p2PKHScriptSig = new BlockSignature();
        NulsSignData signData = this.signature(header.getHash().getDigestBytes());
        p2PKHScriptSig.setSignData(signData);
        p2PKHScriptSig.setPublicKey(getGenesisPubkey());
        header.setBlockSignature(p2PKHScriptSig);
    }

    private NulsSignData signature(byte[] bytes) throws NulsException {
        AccountService service = NulsContext.getServiceBean(AccountService.class);
        return service.signDigest(bytes, ECKey.fromPrivate(new BigInteger(1, Hex.decode(priKey))));
    }

    private byte[] getGenesisPubkey() {
        return ECKey.fromPrivate(new BigInteger(1, Hex.decode(priKey))).getPubKey();
    }
}

