package io.nuls.client.processor;

import io.nuls.client.entity.CommandResult;
import io.nuls.client.helper.CommandBulider;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.TransactionService;

/**
 * @author: Charlie
 * @date: 2018/3/26
 */
public abstract class TransactionProcessors implements CommandProcessor {

    protected TransactionService transactionService = TransactionService.TRANSACTION_SERVICE;

    public static class GetTx extends TransactionProcessors{
        @Override
        public String getCommand() {
            return "gettx";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription())
                    .newLine("\t<hash>   transaction hash -required");
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "gettx <hash> --get the transaction information by txhash";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2) {
                return false;
            }
            if(!StringUtils.validHash(args[1])){
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = transactionService.getTxNa2Nuls(args[1]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class GetTxList extends TransactionProcessors{

        @Override
        public String getCommand() {
            return "gettxlist";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription())
                    .newLine("\t<address>   账户地址 -required");
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "gettxlist <address> --get the transaction information list by address";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2) {
                return false;
            }
            if(!StringUtils.validAddressSimple(args[1])){
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = transactionService.getTxListNa2Nuls(args[1]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }


}
