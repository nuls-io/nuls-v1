package io.nuls.utxo.accounts.service;


import io.nuls.kernel.model.Result;
import io.nuls.utxo.accounts.model.UtxoAccountsBalance;

public interface UtxoAccountsBalanceService {
    Result<UtxoAccountsBalance> getUtxoAccountsBalance(byte []owner);
}
