package io.nuls.rpc.sdk.service;

import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.utils.AssertUtil;
import io.nuls.rpc.sdk.utils.RestFulUtils;

/**
 *
 * @author Niels
 * @date 2017/11/1
 *
 */
public class BlockService {
    private RestFulUtils restFul = RestFulUtils.getInstance();

    public RpcClientResult getBlock(String hash) {
        AssertUtil.canNotEmpty(hash, "block hash cannot null!");
        return restFul.get("/block/" + hash, null);
    }

    public RpcClientResult getBlockCount() {
        return restFul.get("/block/count", null);
    }

    public RpcClientResult getBestBlockHeight() {
        return restFul.get("/block/bestheight", null);
    }

    public RpcClientResult getBestBlockHash() {
        return restFul.get("/block/bestgasg", null);
    }

    public RpcClientResult getBlockHashByHeight(int height) {
        return restFul.get("/block/height/" + height + "/hash", null);
    }

    public RpcClientResult getBlockHeaderByHeight(int height) {
        return restFul.get("/block/height/" + height + "/header", null);
    }

    public RpcClientResult getBlockHeaderByHash(int hash) {
        return restFul.get("/block/" + hash + "/header", null);
    }

    public RpcClientResult getBlockByHeight(int height) {
        return restFul.get("/block/height/"+height, null);
    }


}
