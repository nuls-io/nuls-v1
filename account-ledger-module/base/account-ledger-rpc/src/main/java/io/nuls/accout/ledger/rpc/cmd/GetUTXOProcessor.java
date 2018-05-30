package io.nuls.accout.ledger.rpc.cmd;

import io.nuls.account.model.Address;
import io.nuls.core.tools.date.DateUtil;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/28
 */
public class GetUTXOProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getutxo";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address>      the account address - Required")
                .newLine("\t<pageNumber>   pageNumber -required")
                .newLine("\t<pageSize>     pageSize -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getutxo <address> <pageNumber> <pageSize> -- get utxo list ";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 4) {
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
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        int pageNumber = Integer.parseInt(args[2]);
        int pageSize = Integer.parseInt(args[3]);
        String address = args[1];
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pageNumber", pageNumber);
        parameters.put("pageSize", pageSize);
        RpcClientResult result = restFul.get("/accountledger/utxo/lock/" + address, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>)((Map)result.getData()).get("list");
        for(Map<String, Object> map : list){
            map.put("value",  CommandHelper.naToNuls(map.get("value")));
            map.put("lockTime",  DateUtil.convertDate(new Date((Long)map.get("lockTime"))));
            map.put("createTime",  DateUtil.convertDate(new Date((Long)map.get("createTime"))));
        }
        result.setData(list);
        return CommandResult.getResult(result);
    }
}
