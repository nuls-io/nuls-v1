package io.nuls.account.ledger.sdk.test;

import io.nuls.account.ledger.sdk.model.InputDto;
import io.nuls.account.ledger.sdk.model.OutputDto;
import io.nuls.account.ledger.sdk.service.AccountLedgerService;
import io.nuls.account.ledger.sdk.service.impl.AccountLedgerServiceImpl;
import io.nuls.sdk.SDKBootstrap;
import io.nuls.sdk.model.Result;
import io.nuls.sdk.utils.TransactionTool;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountLedgerSDKTest {

    AccountLedgerService service;

    @Before
    public void init() {
        SDKBootstrap.sdkStart();
        service = new AccountLedgerServiceImpl();
        TransactionTool.init();
    }

    @Test
    public void testCreateTransaction() {
        try {
            String remark = "create transaction demo";
            long fee = 100000;
            List<InputDto> inputs = new ArrayList<>();
            List<OutputDto> outputs = new ArrayList<>();

            InputDto input = new InputDto();
            input.setFromHash("002028d2cc1701fa85c489178aefb55352e34ffc04d0a057ae56bb96b9559c0d2a5b");
            input.setFromIndex(0);
            input.setValue(10000000000000L);
            inputs.add(input);

            OutputDto output = new OutputDto();
            output.setAddress("2Cht55uh8JR5ZAQkp1CVyNPwmFvNuLS");
            output.setValue(1234000000L);
            output.setLockTime(0L);
            outputs.add(output);

            output = new OutputDto();
            output.setAddress("2CbtLw2Z4LugrdvPNdvAphNBBSpjpAr");
            output.setValue(10000000000000L - 1234000000L - fee);
            output.setLockTime(0L);
            outputs.add(output);

            Result result = service.createTransaction(inputs, outputs, remark);
            Map<String, Object> resultMap = (Map<String, Object>) result.getData();
            String txHex = resultMap.get("value").toString();
            String prikey = "00cba38a6c67d3fa55ea1aff2a2a0796894666ad8f9b4e63f43199f7eba8fe1d92";
            String address = "2CbtLw2Z4LugrdvPNdvAphNBBSpjpAr";
            result = service.signTransaction(txHex, prikey, address, null);
            resultMap = (Map<String, Object>) result.getData();
            txHex = resultMap.get("value").toString();
            result = service.broadcastTransaction(txHex);

            System.out.println(result.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSignTransaction() {
        String txHex = "0200197320f9630100ffffffff0123002023c66d10cf9047dbcca12aee2235ff9dfe0f13db3c921a2ec22e0dd63331cb85040080c6a47e8d030000000000000002170100eb6ea0cf4493273b6ae5e254da19cfedfa31e1954740420f000000000000000000000017010026cfc9025e1a78cb7fb8bb7b1710b4afa390d8341080fba7a47e8d030000000000000000";
        String priKey = "077d69758382b0cdd49c9252f6d9d55b7ef539ea58df99ebaf71c9929bd9d0054338baf7a59c9b85b4fa631f816907c8";
        String address = "2CXJEuoXZMajeTEgL6TgiSxTRRMwiMM";
        String password = "nuls123654";

        Result result = service.signTransaction(txHex, priKey, address, password);
        System.out.println(result.isFailed());
    }
}
