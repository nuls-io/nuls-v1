package io.nuls.contract.rpc.cmd;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Map;

/**
 * query contract balance by contact address.
 * Created by wangkun23 on 2018/9/20.
 */
public class GetContractBalanceProcessor implements CommandProcessor {
    /**
     * rest utils
     */
    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getcontractbalance";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> contract address -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getcontractbalance <address> --get the contract balance by contract address";
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
        String address = args[1];
        if (StringUtils.isBlank(address)) {
            return CommandResult.getFailed(KernelErrorCode.PARAMETER_ERROR.getMsg());
        }
        RpcClientResult result = restFul.get("/contract/balance/" + address, null);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        /**
         * assemble display data info
         */
        Map<String, Object> data = (Map) result.getData();
        /**
         * convert balance unit Na to NULS
         * the result balance unit is Na
         */
        data.put("balance", CommandHelper.naToNuls(data.get("balance")));
        return CommandResult.getResult(result);
    }
}
