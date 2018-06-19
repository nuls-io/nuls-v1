/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.account.storage.service.impl;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Address;
import io.nuls.account.storage.constant.AccountStorageConstant;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
@Service
public class AccountStorageServiceImpl implements AccountStorageService, InitializingBean {

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = this.dbService.createArea(AccountStorageConstant.DB_NAME_ACCOUNT);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result saveAccountList(List<AccountPo> accountPoList) {
        BatchOperation batch = dbService.createWriteBatch(AccountStorageConstant.DB_NAME_ACCOUNT);
        for (AccountPo po : accountPoList) {
            batch.putModel(po.getAddressObj().getBase58Bytes(), po);
        }
        return batch.executeBatch();
    }

    @Override
    public Result saveAccount(AccountPo po) {
        return dbService.putModel(AccountStorageConstant.DB_NAME_ACCOUNT, po.getAddressObj().getBase58Bytes(), po);
    }

    @Override
    public Result removeAccount(Address address) {
        if (null == address || address.getBase58Bytes() == null || address.getBase58Bytes().length <= 0) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        return dbService.delete(AccountStorageConstant.DB_NAME_ACCOUNT, address.getBase58Bytes());
    }

    @Override
    public Result<List<AccountPo>> getAccountList() {
        List<AccountPo> listPo = dbService.values(AccountStorageConstant.DB_NAME_ACCOUNT, AccountPo.class);
        return Result.getSuccess().setData(listPo) ;
    }

    @Override
    public Result<AccountPo> getAccount(Address address) {
        return this.getAccount(address.getBase58Bytes());
    }

    @Override
    public Result<AccountPo> getAccount(byte[] address) {
        AccountPo account = dbService.getModel(AccountStorageConstant.DB_NAME_ACCOUNT, address, AccountPo.class);
        if(null == account){
            return Result.getFailed();
        }
        return Result.getSuccess().setData(account);
    }

    @Override
    public Result updateAccount(AccountPo po) {
        if(null == po.getAddressObj()){
            po.setAddressObj(new Address(po.getAddress()));
        }
        AccountPo account = dbService.getModel(AccountStorageConstant.DB_NAME_ACCOUNT, po.getAddressObj().getBase58Bytes(), AccountPo.class);
        if(null == account){
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        return dbService.putModel(AccountStorageConstant.DB_NAME_ACCOUNT, po.getAddressObj().getBase58Bytes(), po);
    }

    @Override
    public Result saveDefaultAccount(AccountPo po) {
        return dbService.putModel(AccountStorageConstant.DB_NAME_ACCOUNT, AccountStorageConstant.DEFAULT_ACCOUNT_KEY, po);
    }

    @Override
    public Result<AccountPo> getDefaultAccount() {
        return Result.getSuccess().setData(dbService.getModel(AccountStorageConstant.DB_NAME_ACCOUNT, AccountStorageConstant.DEFAULT_ACCOUNT_KEY));
    }

    @Override
    public Result removeDefaultAccount() {
        return dbService.delete(AccountStorageConstant.DB_NAME_ACCOUNT, AccountStorageConstant.DEFAULT_ACCOUNT_KEY);
    }
}
