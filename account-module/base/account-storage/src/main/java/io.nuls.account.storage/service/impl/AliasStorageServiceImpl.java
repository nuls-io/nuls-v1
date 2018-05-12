package io.nuls.account.storage.service.impl;

import io.nuls.account.storage.constant.AccountStorageConstant;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;

/**
 * @author: Charlie
 * @date: 2018/5/12
 */
public class AliasStorageServiceImpl implements AliasStorageService, InitializingBean {

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = this.dbService.createArea(AccountStorageConstant.DB_AREA_ALIAS);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result<AliasPo> getAlias(String alias) {
        AliasPo aliasPo = dbService.getModel(AccountStorageConstant.DB_AREA_ALIAS, Hex.decode(alias), AliasPo.class);
        return Result.getSuccess().setData(aliasPo);
    }

    @Override
    public Result saveAlias(AliasPo aliasPo) {
        return dbService.putModel(AccountStorageConstant.DB_AREA_ALIAS, Hex.decode(aliasPo.getAlias()), aliasPo);
    }

    @Override
    public Result removeAlias(String alias) {
        return dbService.delete(AccountStorageConstant.DB_AREA_ALIAS, Hex.decode(alias));
    }
}
