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

import io.nuls.account.storage.constant.AccountStorageConstant;
import io.nuls.account.storage.service.MultiSigAccountStorageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * @author: Niels Wang
 */
@Component
public class MultiSigAccountStorageServiceImpl implements MultiSigAccountStorageService, InitializingBean {

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = this.dbService.createArea(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    /**
     * 创建账户
     * save account
     */
    @Override
    public Result saveAccount(Address address, byte[] multiSigAccount) {
        return dbService.put(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT, address.getAddressBytes(), multiSigAccount);
    }

    /**
     * 删除账户
     * Delete account
     *
     * @param address Account address to be deleted
     * @return the result of the opration
     */
    @Override
    public Result removeAccount(Address address) {
        return dbService.delete(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT, address.getAddressBytes());
    }

    /**
     * 获取所有账户
     *
     * @return the result of the opration and Result<List<Account>>
     */
    @Override
    public Result<List<byte[]>> getAccountList() {
        List<byte[]> valueList = dbService.valueList(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT);
        Result<List<byte[]>> result = new Result<>();
        result.setData(valueList);
        return result;
    }

    /**
     * 根据账户获取账户信息
     * According to the account to obtain account information
     *
     * @return the result of the opration
     */
    @Override
    public Result<byte[]> getAccount(Address address) {
        return new Result<byte[]>().setData(dbService.get(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT, address.getAddressBytes()));
    }
}
