/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.sdk;

import io.nuls.accout.ledger.rpc.dto.InputDto;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.RestFulUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/11/13
 */
public class SimpleTransferTest {

    private static final String PATH = "http://192.168.1.40:8001/api";
    private static final String PATH1 = "http://127.0.0.1:8001/api";

    private RestFulUtils restFul;
    private String from;
    private String to;
    private long amount;
    private String fromKey;

    @Before
    public void before() {
        RestFulUtils.getInstance().setServerUri(PATH1);
        restFul = RestFulUtils.getInstance();
        //from = "Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG";
        //fromKey = "3a9a47428fa5b687afd7a212aca49bcf2340cda45e261b6b7634c92390e800dd";
        //to = "Nse8gVoWwrWfC3GrV5zg5qQ9SX97iCgQ";
        from = "Nse8gVoWwrWfC3GrV5zg5qQ9SX97iCgQ";
        fromKey = "638521c05400808106e3eec5b5fd58782e0b9ad79f7b6d5a4630a6a5e7f54281";
        to = "Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG";
        amount = 10100000000L;
    }

    @Test
    public void transfer() {

        String txHex = create();

        String txHexSign = sign(txHex);

        broadcast(txHexSign);

    }

    private String create() {
        Map<String, Object> params = new HashMap<>();
        params.put("address", from);
        params.put("toAddress", to);
        params.put("amount", amount);
        params.put("remark", "sdk-test");
        params.put("utxos", getUTXOs(from, 100L));

        RpcClientResult post = restFul.post("/accountledger/transaction/simple", params);
        Object data = post.getData();
        Map<String, Object> map = (Map<String, Object>) data;
        Map<String, Object> value = (Map<String, Object>) map.get("value");
        Object hash = value.get("hash");
        Object txHex = value.get("txHex");
        Object inputs = value.get("inputs");
        Object outputs = value.get("outputs");

        return txHex.toString();
    }

    private String sign(String txHex) {
        Map<String, Object> paramsSign = new HashMap<>();
        paramsSign.put("txHex", txHex);
        paramsSign.put("address", from);
        paramsSign.put("priKey", fromKey);
        paramsSign.put("password", null);
        RpcClientResult postSign = restFul.post("/accountledger/transaction/sign", paramsSign);
        Object dataSign = postSign.getData();
        Map<String, Object> mapSign = (Map<String, Object>) dataSign;
        Object txHexSign = mapSign.get("value");
        return txHexSign.toString();
    }

    private String broadcast(String txHexSign) {
        Map<String, Object> paramsBroadcast = new HashMap<>();
        paramsBroadcast.put("txHex", txHexSign);
        RpcClientResult postBroadcast = restFul.post("/accountledger/transaction/broadcast", paramsBroadcast);
        Object dataBroadcast = postBroadcast.getData();
        Map<String, Object> mapBroadcast = (Map<String, Object>) dataBroadcast;
        Object valueBroadcast = mapBroadcast.get("value");
        System.out.println("txHash: " + valueBroadcast);
        return valueBroadcast.toString();
    }

    private List<InputDto> getUTXOs(String address, Long limit) {
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException("address not be null");
        }
        if (limit == null) {
            throw new IllegalArgumentException("limit not be null");
        }
        String url = "/utxo/limit/" + address + "/" + limit;
        RpcClientResult result = restFul.get(url, new HashMap());

        if (result.isFailed()) {
            return Collections.EMPTY_LIST;
        }
        List<InputDto> inputs = new ArrayList<>();
        Map data = (Map) result.getData();
        List<Map<String, Object>> utxos = (List<Map<String, Object>>) data.get("utxoDtoList");
        for (Map<String, Object> utxo : utxos) {
            InputDto input = new InputDto();
            input.setAddress(address);
            input.setFromHash(utxo.get("txHash").toString());
            input.setFromIndex((int) utxo.get("txIndex"));
            input.setValue(Long.parseLong(utxo.get("value").toString()));
            input.setLockTime(Long.valueOf(utxo.get("lockTime").toString()));
            inputs.add(input);
        }
        return inputs;
    }
}
