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
public class SetPasswordProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "setpwd";
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
        return "setpwd <address> --set password for the account";
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
        if (!Address.validAddress(args[1])) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        RpcClientResult rs = restFul.get("/account/encrypted/" + address, null);
        if (!rs.getCode().equals(KernelErrorCode.SUCCESS.getCode())) {
            return CommandResult.getFailed(rs.getMsg());
        }
        if(rs.isSuccess()){
            return CommandResult.getFailed("This account already has a password.");
        }
        String password = CommandHelper.getNewPwd();
        CommandHelper.confirmPwd(password);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        RpcClientResult result = restFul.post("/account/password/" + address, parameters);
        if(result.isFailed()){
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
