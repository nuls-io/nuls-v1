package io.nuls.account.ledger.sdk.service.impl;

import io.nuls.account.ledger.sdk.model.InputDto;
import io.nuls.account.ledger.sdk.model.OutputDto;
import io.nuls.account.ledger.sdk.model.TransactionDto;
import io.nuls.account.ledger.sdk.service.AccountLedgerService;
import io.nuls.sdk.SDKBootstrap;
import io.nuls.sdk.constant.AccountErrorCode;
import io.nuls.sdk.constant.SDKConstant;
import io.nuls.sdk.model.Address;
import io.nuls.sdk.model.Result;
import io.nuls.sdk.model.dto.BalanceDto;
import io.nuls.sdk.utils.JSONUtils;
import io.nuls.sdk.utils.RestFulUtils;
import io.nuls.sdk.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author: Charlie
 * @date: 2018/6/12
 */
public class AccountLedgerServiceImpl implements AccountLedgerService {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public Result getTxByHash(String hash) {
        if(StringUtils.isBlank(hash)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }

        Result result = restFul.get("/accountledger/tx/" + hash, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map)result.getData();
        //重新组装input
        List<Map<String, Object>> inputMaps = (List<Map<String, Object>>)map.get("inputs");
        List<InputDto> inputs = new ArrayList<>();
        for(Map<String, Object> inputMap : inputMaps){
            InputDto inputDto = new InputDto(inputMap);
            inputs.add(inputDto);
        }
        map.put("inputs", inputs);

        //重新组装output
        List<Map<String, Object>> outputMaps = (List<Map<String, Object>>)map.get("outputs");
        List<OutputDto> outputs = new ArrayList<>();
        for(Map<String, Object> outputMap : outputMaps){
            OutputDto outputDto = new OutputDto(outputMap);
            outputs.add(outputDto);
        }
        map.put("outputs", outputs);
        TransactionDto transactionDto = new TransactionDto(map);
        result.setData(transactionDto);
        return result;
    }

    @Override
    public Result transfer(String address, String toAddress, String password, long amount, String remark) {
        if(!Address.validAddress(address) || !Address.validAddress(toAddress)){
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if(!StringUtils.validPassword(password)){
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        if(!validTxRemark(remark)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", address);
        parameters.put("toAddress", toAddress);
        parameters.put("password", password);
        parameters.put("amount", amount);
        parameters.put("remark", remark);
        Result result = restFul.post("/accountledger/transfer", parameters);
        return result;
    }

    private boolean validTxRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return true;
        }
        try {
            byte[] bytes = remark.getBytes(SDKConstant.DEFAULT_ENCODING);
            if (bytes.length > 100) {
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }
    @Override
    public Result getBalance(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.get("/accountledger/balance/" + address, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        map.put("balance", ((Map) map.get("balance")).get("value"));
        map.put("usable", ((Map) map.get("usable")).get("value"));
        map.put("locked", ((Map) map.get("locked")).get("value"));
        BalanceDto balanceDto = new BalanceDto(map);
        return result.setData(balanceDto);
    }

    public static void main(String[] args) {
        SDKBootstrap.sdkStart();
        AccountLedgerService als = new AccountLedgerServiceImpl();
        try {
            System.out.println(JSONUtils.obj2json(als.getTxByHash("002023c66d10cf9047dbcca12aee2235ff9dfe0f13db3c921a2ec22e0dd63331cb85")));
//            System.out.println(JSONUtils.obj2json(als.getBalance("2ChDcC1nvki521xXhYAUzYXt4RLNuLs")));
/*            System.out.println(JSONUtils.obj2json(als.transfer("2ChDcC1nvki521xXhYAUzYXt4RLNuLs"
                    , "2CZ4AUEFkAx4AJUk365mdZ75Qod3Shk"
                    , "nuls123456"
                    , 8888800000000L
                    , "lichao"
                    )));*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
