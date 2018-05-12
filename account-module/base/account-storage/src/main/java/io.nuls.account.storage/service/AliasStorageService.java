package io.nuls.account.storage.service;

import io.nuls.account.storage.po.AliasPo;

/**
 * @author: Charlie
 * @date: 2018/5/12
 */
public interface AliasStorageService {

    AliasPo getAlias(byte[] address);

    void saveAlias(AliasPo aliasPo);

    void rollbackAlias(AliasPo aliasPo);

}
