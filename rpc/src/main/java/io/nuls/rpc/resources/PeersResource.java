package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 * Created by Niels on 2017/9/27.
 *
 */
public interface PeersResource {
    RpcResult getList();

    RpcResult getCount();

    RpcResult getConsensusPeers();

    RpcResult getConsensusCount();

    RpcResult getGroups();
}
