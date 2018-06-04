package io.nuls.account.rpc.cmd;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 覆盖导入
 * Overwrite import
 * @author: Charlie
 * @date: 2018/6/4
 */
public class ImportForcedByPrivateKeyProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "importforced";
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
        return "importforced <privatekey> --import the account according to the private key, if the account exists, it will be overwritten";
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
        parameters.put("overwrite", true);
        RpcClientResult result = restFul.post("/account/import/pri", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }

}
