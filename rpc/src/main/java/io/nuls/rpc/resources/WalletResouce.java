package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

import java.io.File;

/**
 * Created by Niels on 2017/9/30.
 *
 */
public interface WalletResouce {
    RpcResult lock();

    RpcResult unlock(String password, String unlockTime);

    RpcResult password(String password);

    RpcResult password(String password, String newPassword);

    RpcResult accountList();

    RpcResult transfer(String address, String password, String toAddress, String amount, String remark);

    RpcResult backup(String address, String password);

    RpcResult importWallet(File file);
}
