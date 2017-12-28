package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 *
 * @author Niels
 * @date 2017/9/27
 */
public interface AccountResource {

    RpcResult create(Integer count);

    RpcResult load(String address);

    RpcResult getBalance(String address);

    RpcResult getCredit(String address);

    RpcResult getPrikey(String address, String password);

    RpcResult lock(String address, String password,
                   Double amount, String remark,
                   String unlockTime);

    RpcResult getAddress(String publicKey, String subChainId);

    RpcResult getAddress(String address);
}
