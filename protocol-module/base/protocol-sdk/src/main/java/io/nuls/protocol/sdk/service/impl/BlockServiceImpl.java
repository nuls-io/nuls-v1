package io.nuls.protocol.sdk.service.impl;

import io.nuls.protocol.sdk.service.BlockService;
import io.nuls.sdk.model.Result;
import io.nuls.sdk.utils.RestFulUtils;

/**
 * @author: Charlie
 * @date: 2018/6/13
 */
public class BlockServiceImpl implements BlockService {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public Result getHeaderByHeight(int height) {
        return null;
    }

    @Override
    public Result getHeaderByHash(String hash) {
        return null;
    }

    @Override
    public Result getBlockByHeight(int height) {
        return null;
    }

    @Override
    public Result getBlockByHash(String hash) {
        return null;
    }
}
