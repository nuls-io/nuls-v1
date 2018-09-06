package io.nuls.utxo.accounts.service;

import io.nuls.kernel.exception.NulsException;

public interface UtxoAccountsService {
    public boolean validateIntegrityBootstrap(long hadSynBlockHeight) throws NulsException;
//    public boolean validateBlock();
//    public boolean rollbackBlock();
    public boolean  synBlock(long blockHeight);
}
