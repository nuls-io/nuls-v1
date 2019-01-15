/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author: Charlie
 */
@Service
public class AliasStorageServiceImpl implements AliasStorageService, InitializingBean {

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = this.dbService.createArea(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result<List<AliasPo>> getAliasList() {
        List<AliasPo> list = dbService.values(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS, AliasPo.class);
        return Result.getSuccess().setData(list);
    }

    @Override
    public Result<AliasPo> getAlias(String alias) {
        try {
            byte[] aliasByte = alias.getBytes(NulsConfig.DEFAULT_ENCODING);
            AliasPo aliasPo = dbService.getModel(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS, aliasByte, AliasPo.class);
            return Result.getSuccess().setData(aliasPo);
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            return Result.getFailed();
        }
    }

    @Override
    public Result saveAlias(AliasPo aliasPo) {
        try {
            return dbService.putModel(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS, aliasPo.getAlias().getBytes(NulsConfig.DEFAULT_ENCODING), aliasPo);
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            return Result.getFailed();
        }
    }

    @Override
    public Result removeAlias(String alias) {
        try {
            return dbService.delete(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS, alias.getBytes(NulsConfig.DEFAULT_ENCODING));
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed();
        }
    }
}
