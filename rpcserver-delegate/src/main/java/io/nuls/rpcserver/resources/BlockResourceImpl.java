package io.nuls.rpcserver.resources;

import io.nuls.rpcserver.vo.RpcResult;

/**
 * Created by Niels on 2017/9/30.
 * nuls.io
 */
public class BlockResourceImpl implements BlockResource {
    @Override
    public RpcResult getBlock(String hash) {
        return null;
    }

    @Override
    public RpcResult getBlockCount() {
        return null;
    }

    @Override
    public RpcResult getBestBlockHeiht() {
        return null;
    }

    @Override
    public RpcResult getBestBlockHash() {
        return null;
    }

    @Override
    public RpcResult getHashByHeight(Integer height) {
        return null;
    }
}
