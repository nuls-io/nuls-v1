package io.nuls.client.processor;

import io.nuls.client.entity.CommandResult;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.NetworkService;

/**
 * @author: Charlie
 * @date: 2018/3/27
 */
public abstract class NetwrokProcessor implements CommandProcessor{

    protected NetworkService networkService = NetworkService.NETWORK_SERVICE;

    public static class GetNetworkInfo extends NetwrokProcessor{
        @Override
        public String getCommand() {
            return "getnetworkinfo";
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public String getCommandDescription() {
            return "getnetworkinfo  -- get network information";
        }

        @Override
        public boolean argsValidate(String[] args) {
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = networkService.getnetworkinfo();
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class getnetworknodes extends NetwrokProcessor{
        @Override
        public String getCommand() {
            return "getnetworknodes";
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public String getCommandDescription() {
            return "getnetworknodes --get IP of network nodes";
        }

        @Override
        public boolean argsValidate(String[] args) {
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = networkService.getnetworknodes();
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }
}
