package io.nuls.accout.ledger.rpc.cmd;

import io.nuls.account.model.Address;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.lite.annotation.Cmd;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/05/28
 */
public class GetAccountTxListProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "gettxlist";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address>   address -required")
                .newLine("\t[txType]    transaction type -default 0")
                .newLine("\t<pageNumber>     pageNumber -required")
                .newLine("\t<pageSize>     pageSize -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "gettxlist <address> [txType] <pageNumber> <pageSize> --get the transaction information list by address";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length < 4 || length > 5) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!Address.validAddress(args[1])) {
            return false;
        }
        if (!StringUtils.isNumeric(args[2]) || !StringUtils.isNumeric(args[3])) {
            return false;
        }
        if (args.length == 5) {
            if (!StringUtils.isNumeric(args[4])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        int type = 0;
        int pageNumber = 0;
        int pageSize = 0;
        if (args.length == 4) {
            pageNumber = Integer.parseInt(args[2]);
            pageSize = Integer.parseInt(args[3]);
        } else {
            type = Integer.parseInt(args[2]);
            pageNumber = Integer.parseInt(args[3]);
            pageSize = Integer.parseInt(args[4]);
        }
        String address = args[1];
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", type);
        parameters.put("pageNumber", pageNumber);
        parameters.put("pageSize", pageSize);
        RpcClientResult result = restFul.get("/accountledger/tx/list/" + address, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        result.setData(((Map) result.getData()).get("list"));
        return CommandResult.getResult(result);
    }
}
