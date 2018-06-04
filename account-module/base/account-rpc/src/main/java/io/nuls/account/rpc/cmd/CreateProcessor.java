package io.nuls.account.rpc.cmd;

import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/25
 */
public class CreateProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "create";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t[count] The count of accounts you want to create, - default 1");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "create [count] --create account, [count] the count of accounts you want to create, - default 1";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length < 1 || length > 2) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (length == 2 && !StringUtils.isNumeric(args[1])) {
            return false;
        }
        if(length == 2 && Integer.parseInt(args[1]) < 1 ){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String password = CommandHelper.getPwdOptional();
        int count = 1;
        if(args.length == 2){
            count = Integer.parseInt(args[1]);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        parameters.put("count", count);
        RpcClientResult result = restFul.post("/account", parameters);
        if(result.isFailed()){
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
