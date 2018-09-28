package io.nuls.contract.rpc.cmd;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * get contract program constructors
 * Created by wangkun23 on 2018/9/25.
 */
public class GetContractConstructorProcessor implements CommandProcessor {
    /**
     * rest client utils
     */
    private RestFulUtils restFulUtils = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getcontractcontructor";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<contractCode> contract code -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getcontractcontructor <contractCode> --get contract contructor from smart contract program";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 2) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String code = args[1];
        if (StringUtils.isBlank(code)) {
            return CommandResult.getFailed(KernelErrorCode.PARAMETER_ERROR.getMsg());
        }
        /**
         * assemble request body JSON
         */
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("contractCode", code);
        String url = "/contract/constructor";
        RpcClientResult result = restFulUtils.post(url, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }
}
