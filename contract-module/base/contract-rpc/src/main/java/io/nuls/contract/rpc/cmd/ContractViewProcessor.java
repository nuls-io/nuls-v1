package io.nuls.contract.rpc.cmd;

import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * call contract without chain.
 * Created by wangkun23 on 2018/9/25.
 */
public class ContractViewProcessor implements CommandProcessor {
    private RestFulUtils restFulUtils = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "contractview";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<contractAddress>   contract address    -required")
                .newLine("\t<methodName>        the method to call    -required")
                .newLine("\t[methodDesc]        the method description    -not required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "contractview <contractAddress> <methodName> [-d methodDesc] --call contract without chain";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 3 && length != 4) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String contractAddress = args[1];
        /**
         * input contact method parameters args.
         * {
         *   "contractAddress": "string",
         *   "methodName": "string",
         *   "methodDesc": "string",
         *   "args": [
         *     {}
         *   ]
         * }
         */
        RpcClientResult res = CommandHelper.getContractCallArgsJson();
        if (!res.isSuccess()) {
            return CommandResult.getFailed(res);
        }
        Object[] contractArgs = (Object[]) res.getData();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("contractAddress", contractAddress);
        parameters.put("methodName", args[2]);
        if (args.length == 4) {
            parameters.put("methodDesc", args[3]);
        }
        parameters.put("args", contractArgs);
        /**
         * post url /api/contract/view
         */
        String url = "/contract/view";
        RpcClientResult result = restFulUtils.post(url, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }

}
