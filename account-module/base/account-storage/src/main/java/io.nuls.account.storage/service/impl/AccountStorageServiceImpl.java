package io.nuls.account.storage.service.impl;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Address;
import io.nuls.account.storage.constant.AccountStorageConstant;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.model.Entry;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
@Component
public class AccountStorageServiceImpl implements AccountStorageService, InitializingBean {

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = this.dbService.createArea(AccountStorageConstant.DB_AREA_ACCOUNT);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result saveAccountList(List<AccountPo> accountPoList) {
        BatchOperation batch = dbService.createWriteBatch(AccountStorageConstant.DB_AREA_ACCOUNT);
        for (AccountPo po : accountPoList) {
            batch.put(po.getAddressObj().getBase58Bytes(), po.serialize());
        }
        return batch.executeBatch();
    }

    @Override
    public Result saveAccount(AccountPo po) {
        return dbService.put(AccountStorageConstant.DB_AREA_ACCOUNT, po.getAddressObj().getBase58Bytes(), po.serialize());
    }

    @Override
    public Result removeAccount(Address address) {
        if (null == address || address.getBase58Bytes() == null || address.getBase58Bytes().length <= 0) {
            return Result.getFailed(AccountErrorCode.NULL_PARAMETER);
        }
        return dbService.delete(AccountStorageConstant.DB_AREA_ACCOUNT, address.getBase58Bytes());
    }

    @Override
    public Result<List<AccountPo>> getAccountList() {
        List<Entry<byte[], byte[]>> entryList = dbService.entryList(AccountStorageConstant.DB_AREA_ACCOUNT);
        List<AccountPo> list = new ArrayList<>();
        for (Entry<byte[], byte[]> entry : entryList){
            AccountPo account = new AccountPo();
            account.parse(entry.getValue());
            list.add(account);
        }
        return Result.getSuccess().setData(list) ;
    }

    @Override
    public Result<AccountPo> getAccount(Address address) {
        byte[] poByte = dbService.get(AccountStorageConstant.DB_AREA_ACCOUNT, address.getBase58Bytes());
        if(null == poByte || poByte.length<=0){
            return Result.getFailed();
        }
        AccountPo account = new AccountPo();
        account.parse(poByte);
        return Result.getSuccess().setData(account);
    }
}
