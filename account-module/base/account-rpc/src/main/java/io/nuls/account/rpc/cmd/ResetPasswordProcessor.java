package io.nuls.account.rpc.cmd;

import io.nuls.account.model.Address;
import io.nuls.kernel.constant.KernelErrorCode;
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
public class ResetPasswordProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "resetpwd";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> address of the account - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "resetpwd <address> --reset password for account";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 2) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        RpcClientResult res = CommandHelper.getPassword(address, restFul, "Enter your old password:");
        if(!res.getCode().equals(KernelErrorCode.SUCCESS.getCode())){
            return CommandResult.getFailed(res.getMsg());
        }
        if(res.isFailed()){
            return CommandResult.getFailed("No password has been set up yet");
        }
        String password = (String)res.getData();
        String newPassword = CommandHelper.getNewPwd();
        CommandHelper.confirmPwd(newPassword);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        parameters.put("newPassword", newPassword);
        RpcClientResult result = restFul.put("/account/password/" + address, parameters);
        if(result.isFailed()){
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
