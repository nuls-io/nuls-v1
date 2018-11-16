package io.nuls.contract.rpc;

import io.nuls.accout.ledger.rpc.dto.InputDto;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.RestFulUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by wangkun23 on 2018/11/13.
 */
public class ContractTxTest {

    final Logger logger = LoggerFactory.getLogger(ContractTxTest.class);

    private RestFulUtils restFul = null;

    private String sender;


    @Before
    public void init() {
        restFul = RestFulUtils.getInstance();
        RestFulUtils.getInstance().setServerUri("http://192.168.1.40:8001/api");
        sender = "Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG";
    }

    public List<InputDto> loadUTXOs() {
        String address = sender;
        Long limit = 100L;
        String url = "/utxo/limit/" + address + "/" + limit;
        RpcClientResult result = restFul.get(url, new HashMap());
        List<InputDto> utxos = new ArrayList<>();
        Map data = (Map) result.getData();
        List<Map<String, Object>> utxoList = (List<Map<String, Object>>) data.get("utxoDtoList");
        for (Map<String, Object> utxo : utxoList) {
            InputDto input = new InputDto();
            input.setAddress(address);
            input.setFromHash(utxo.get("txHash").toString());
            input.setFromIndex((int) utxo.get("txIndex"));
            input.setValue(Long.parseLong(utxo.get("value").toString()));
            input.setLockTime(Long.valueOf(utxo.get("lockTime").toString()));
            utxos.add(input);
        }

        return utxos;
    }

    @Test
    public void create() {
        List<InputDto> utxos = loadUTXOs();
        String sender = this.sender;
        String url = "/contract/sdk/create";

        long gasLimit = 27043L;
        Long price = 25L;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("vote-contract-hex.txt")));//构造一个BufferedReader类来读取文件
            String buf = null;
            while ((buf = bufferedReader.readLine()) != null) {
                stringBuilder.append(buf);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String contractCode = stringBuilder.toString();
        Object[] args = {100_0000_0000L};
        String remark = "";

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("sender", sender);
        paramsMap.put("contractCode", contractCode);
        paramsMap.put("gasLimit", gasLimit);
        paramsMap.put("price", price);
        paramsMap.put("args", args);
        paramsMap.put("utxos", utxos);
        try {
            RpcClientResult result = restFul.post(url, paramsMap);
            logger.info("result {}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void callContract() {
        List<InputDto> utxos = loadUTXOs();
        String url = "/contract/sdk/call";

        String contractAddress = "NseA4LYf5kBXjrnag4xN8BjzgWx7YrVm";
        String sender = this.sender;
        Long value = 100_0000_0000L;
        Long gasLimit = 81325l;
        Long price = 25L;
        String methodName = "create";
        String methodDesc = "";
        Object[] args = {"test", "test desc", Arrays.asList("1", "2", "3"), "1542042000000", "1542646800000", "false", "1", "1", "false"};
        String remark = "test call contract";

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("sender", sender);
        paramsMap.put("value", value);
        paramsMap.put("gasLimit", gasLimit);
        paramsMap.put("price", price);
        paramsMap.put("contractAddress", contractAddress);
        paramsMap.put("methodName", methodName);
        paramsMap.put("methodDesc", methodDesc);
        paramsMap.put("args", args);
        paramsMap.put("remark", remark);
        paramsMap.put("utxos", utxos);

        try {

            logger.info("{}", JSONUtils.obj2json(paramsMap));
            RpcClientResult result = restFul.post(url, paramsMap);
            logger.info("result {}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void deleteContract() {
        List<InputDto> utxos = loadUTXOs();
        String url = "/contract/sdk/delete";
        String sender = this.sender;
        String contractAddress = "NseDkRagNCj8kNE6JRPB55i5bZuAeKtf";

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("sender", sender);
        paramsMap.put("contractAddress", contractAddress);
        paramsMap.put("remark", "");
        paramsMap.put("utxos", utxos);
        try {
            logger.info("{}", JSONUtils.obj2json(paramsMap));
            RpcClientResult result = restFul.post(url, paramsMap);
            logger.info("result {}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
