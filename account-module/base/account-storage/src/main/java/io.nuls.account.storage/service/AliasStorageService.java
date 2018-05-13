package io.nuls.account.storage.service;

import io.nuls.account.storage.po.AliasPo;
import io.nuls.kernel.model.Result;

/**
 * @author: Charlie
 * @date: 2018/5/12
 */
public interface AliasStorageService {

    Result<AliasPo> getAlias(String alias);

    Result saveAlias(AliasPo aliasPo);

    Result removeAlias(String alias);

}
