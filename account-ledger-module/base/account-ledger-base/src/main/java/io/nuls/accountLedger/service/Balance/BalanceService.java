package io.nuls.accountLedger.service.Balance;

import io.nuls.account.model.Balance;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * author Facjas
 * date 2018/5/15.
 */
public interface BalanceService {

    Balance getBalance(String address);

    Result<Balance> getBalance(byte[] address);

    void reloadAccountBalance(List<String> addresses);
}
