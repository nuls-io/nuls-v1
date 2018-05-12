package io.nuls.account.storage.service.impl;

import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AliasStorageService;

/**
 * @author: Charlie
 * @date: 2018/5/12
 */
public class AliasStorageServiceImpl implements AliasStorageService {

    @Override
    public AliasPo getAlias(byte[] address) {
        return null;
    }

    @Override
    public void saveAlias(AliasPo aliasPo) {

    }

    @Override
    public void rollbackAlias(AliasPo aliasPo) {

    }
}
