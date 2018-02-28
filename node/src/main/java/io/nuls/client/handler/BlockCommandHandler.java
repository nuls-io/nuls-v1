package io.nuls.client.handler;

import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.BlockService;

public class BlockCommandHandler {

    private BlockService blockService;

    public String processCommand(String[] args) {
        RpcClientResult result = null;

        String command = args[1];

        switch (command) {
            case "count":
                result = blockService.getBlockCount();
                break;
            case "bestheight":
                result = blockService.getBestBlockHeight();
            case "besthash":
                result = blockService.getBestBlockHash();
            case "11":
                result = blockService.getBestBlockHash();
        }

        return null;
    }

}
