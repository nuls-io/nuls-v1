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
            input.setFromHash("002023c66d10cf9047dbcca12aee2235ff9dfe0f13db3c921a2ec22e0dd63331cb85");
            input.setFromIndex(1);
            input.setValue(1000000000L);
            inputs.add(input);

            OutputDto output = new OutputDto();
            output.setAddress("2CjPVMKST7h4Q5Dqa8Q9P9CwYSmN7mG");
            output.setValue(1000000L);
            output.setLockTime(0L);
            outputs.add(output);

            output = new OutputDto();
            output.setAddress("2CXJEuoXZMajeTEgL6TgiSxTRRMwiMM");
            output.setValue(1000000000L - 1000000 - fee);
            output.setLockTime(0L);
            outputs.add(output);

            Result result = service.createTransaction(inputs, outputs, remark);
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
