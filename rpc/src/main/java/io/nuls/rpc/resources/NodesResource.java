package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 *
 * @author Niels
 * @date 2017/9/27
 */
public interface NodesResource {
    RpcResult getList();

    RpcResult getCount();

    RpcResult getConsensusNodes();

    RpcResult getConsensusCount();

    RpcResult getGroups();
}
