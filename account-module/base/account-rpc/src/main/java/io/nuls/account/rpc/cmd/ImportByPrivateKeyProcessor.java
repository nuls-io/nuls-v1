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
public class ImportByPrivateKeyProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "import";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<privatekey> private key - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "import <privatekey> --import the account according to the private key, if the account exists, it will not be executed ";
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
        String prikey = args[1];
        String password = CommandHelper.getPwdOptional();
        if(StringUtils.isNotBlank(password)){
            CommandHelper.confirmPwd(password);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("priKey", prikey);
        parameters.put("password", password);
        parameters.put("overwrite", false);
        RpcClientResult result = restFul.post("/account/import/pri", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
