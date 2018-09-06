package io.nuls.protocol.storage.service.impl;

import io.nuls.core.tools.crypto.Util;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;
import io.nuls.protocol.storage.constant.ProtocolStorageConstant;
import io.nuls.protocol.storage.po.UpgradeInfoPo;
import io.nuls.protocol.storage.service.VersionManagerStorageService;

/**
 * @author: Charlie
 * @date: 2018/8/17
 */
@Service
public class VersionManagerStorageServiceImpl implements VersionManagerStorageService, InitializingBean {
    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    /**
     * 创建存储表，创建失败时如果是因为已存在则正常，否则抛出异常
     * Create a storage table, or throw an exception if it is normal if it is already existing.
     */
    @Override
    public void afterPropertiesSet() {
        Result result = this.dbService.createArea(ProtocolStorageConstant.NULS_VERSION_MANAGER);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result saveMainVersion(int version) {
        return dbService.put(ProtocolStorageConstant.NULS_VERSION_MANAGER, ProtocolStorageConstant.MAIN_VERSION_KEY, Util.intToBytes(version));
    }

    @Override
    public Integer getMainVersion() {
        byte[] mainVersion = dbService.get(ProtocolStorageConstant.NULS_VERSION_MANAGER, ProtocolStorageConstant.MAIN_VERSION_KEY);
        return null == mainVersion ? null : Util.byteToInt(mainVersion);
    }

    @Override
    public Result saveUpgradeCount(UpgradeInfoPo upgradeInfoPo) {
        return dbService.putModel(ProtocolStorageConstant.NULS_VERSION_MANAGER, Util.intToBytes(upgradeInfoPo.getVersion()), upgradeInfoPo);
    }

    @Override
    public UpgradeInfoPo getUpgradeCount(int version) {
        return (UpgradeInfoPo)dbService.getModel(ProtocolStorageConstant.NULS_VERSION_MANAGER, Util.intToBytes(version));
    }

}
