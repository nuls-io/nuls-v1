package io.nuls.utxo.accounts.module;


import io.nuls.kernel.module.BaseModuleBootstrap;
import io.nuls.utxo.accounts.constant.UtxoAccountsConstant;

public abstract class AbstractUtxoAccountsModule extends BaseModuleBootstrap {
    public AbstractUtxoAccountsModule() {
        super(UtxoAccountsConstant.MODULE_ID_UTXOACCOUNTS);
    }
}