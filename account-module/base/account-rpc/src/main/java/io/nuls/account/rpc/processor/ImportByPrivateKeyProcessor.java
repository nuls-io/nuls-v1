package io.nuls.account.rpc.processor;

import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.cmd.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.lite.annotation.Cmd;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/25
 */
@Cmd
@Component
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
                .newLine("\t<privatekey> private key - Required")
                .newLine("\t[password] the password is between 8 and 20 inclusive of numbers and letters, not encrypted by default");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "import <privatekey> [password] --Import the account according to the private key ";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length < 2 || length > 3) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (length == 3 && !StringUtils.validPassword(args[2])) {
            return false;
        }
        if (length == 3) {
            String newPwd = args[2];
            CommandHelper.confirmPwd(newPwd);
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String prikey = args[1];
        String password = args.length == 3 ? args[2] : null;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("priKey", prikey);
        parameters.put("password", password);
        parameters.put("overwrite", false);
        Result result = restFul.post("/account/import/pri", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
