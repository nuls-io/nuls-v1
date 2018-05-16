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

/**
 * @author: Charlie
 * @date: 2018/5/12
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
