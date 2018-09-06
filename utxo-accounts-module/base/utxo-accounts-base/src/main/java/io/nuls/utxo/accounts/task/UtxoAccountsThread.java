package io.nuls.utxo.accounts.task;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.utxo.accounts.locker.Lockers;
import io.nuls.utxo.accounts.service.UtxoAccountsService;
import io.nuls.utxo.accounts.storage.service.UtxoAccountsStorageService;

@Component
public class UtxoAccountsThread implements Runnable {
   @Autowired
    private UtxoAccountsService utxoAccountsService;
    @Autowired
    private UtxoAccountsStorageService utxoAccountsStorageService;

    @Override
    public void run() {
        Lockers.SYN_UTXO_ACCOUNTS_LOCK.lock();
        try{
            long hadSynBlockHeight=utxoAccountsStorageService.getHadSynBlockHeight();
            long end = NulsContext.getInstance().getBestHeight();
            for(long i=hadSynBlockHeight+1;i<=end;i++){
                if(!utxoAccountsService.synBlock(i)){
                    Log.error("utxoAccounts block syn fail!");
                    break;
                }
            }
        }catch (Exception e){
            Log.error(e);
        }
        finally{
            Lockers.SYN_UTXO_ACCOUNTS_LOCK.unlock();
        }
    }

}
