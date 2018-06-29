package io.nuls.client.storage.impl;

import io.nuls.client.constant.CommandConstant;
import io.nuls.client.storage.LanguageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;

/**
 * @author: Charlie
 */
@Service
public class LanguageServiceImpl implements LanguageService, InitializingBean {
    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = this.dbService.createArea(CommandConstant.DB_LANGUAGE);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result saveLanguage(String language) {
        return dbService.putModel(CommandConstant.DB_LANGUAGE, CommandConstant.DB_LANGUAGE.getBytes(), language);
    }

    @Override
    public Result getLanguage() {
        return Result.getSuccess().setData(dbService.getModel(CommandConstant.DB_LANGUAGE, CommandConstant.DB_LANGUAGE.getBytes()));
    }
}
