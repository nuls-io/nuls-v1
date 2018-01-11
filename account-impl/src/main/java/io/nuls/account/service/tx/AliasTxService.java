package io.nuls.account.service.tx;

import io.nuls.account.entity.Alias;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.account.util.AccountTool;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.db.dao.AccountAliasDataService;
import io.nuls.db.entity.AliasPo;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class AliasTxService implements TransactionService<AliasTransaction> {

    private static AliasTxService instance = new AliasTxService();

    private AliasTxService() {

    }

    public static AliasTxService getInstance() {
        return instance;
    }


    private AccountAliasDataService dataService;

    @Override
    public void onRollback(AliasTransaction tx) throws NulsException {
        // todo auto-generated method stub(niels)
        AliasPo po = AccountTool.toAliasPojo(tx.getTxData());
        dataService.rollbackAlias(po);

    }

    @Override
    public void onCommit(AliasTransaction tx) throws NulsException {
        Alias alias = tx.getTxData();
        alias.setStatus(1);
        dataService.saveAlias(AccountTool.toAliasPojo(alias));
    }

    @Override
    public void onApproval(AliasTransaction tx) throws NulsException {
        try {
            AliasPo po = AccountTool.toAliasPojo(tx.getTxData());
            dataService.saveAlias(po);
        }catch (Exception e) {
            throw new NulsException(ErrorCode.DB_SAVE_ERROR, e);
        }
    }

    public void setDataService(AccountAliasDataService dataService) {
        this.dataService = dataService;
    }
}
