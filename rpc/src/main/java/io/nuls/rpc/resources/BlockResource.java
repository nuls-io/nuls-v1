package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
public interface BlockResource {

    RpcResult getBlock(String hash);

    RpcResult getBlockCount();

    RpcResult getBestBlockHeiht();

    RpcResult getBestBlockHash();

    RpcResult getHashByHeight(Integer height);


}
