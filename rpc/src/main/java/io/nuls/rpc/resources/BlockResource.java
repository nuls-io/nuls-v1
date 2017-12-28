package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 *
 * @author Niels
 * @date 2017/9/27
 */
public interface BlockResource {

    RpcResult loadBlock(String hash);

    RpcResult getBlockCount();

    RpcResult getBestBlockHeiht();

    RpcResult getBestBlockHash();

    RpcResult getHashByHeight(Integer height);

    RpcResult getHeaderByHeight(Integer height);

    RpcResult getHeader(String hash);

    RpcResult getBlock(Integer height);

}
