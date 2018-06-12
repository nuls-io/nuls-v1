package io.nuls.account.ledger.sdk.service.impl;

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
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/6/12
 */
public class AccountLedgerServiceImpl implements AccountLedgerService {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public Result getTxByHash(String hash) {

        return null;
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
            System.out.println(JSONUtils.obj2json(als.getBalance("2ChDcC1nvki521xXhYAUzYXt4RLNuLs")));
            System.out.println(JSONUtils.obj2json(als.transfer("2ChDcC1nvki521xXhYAUzYXt4RLNuLs"
                    , "2Ci1r2FRgcbEg76QbPo4tsQAJkha6Q9"
                    , "nuls123456"
                    , 8888800000000L
                    , "lichao"
                    )));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
