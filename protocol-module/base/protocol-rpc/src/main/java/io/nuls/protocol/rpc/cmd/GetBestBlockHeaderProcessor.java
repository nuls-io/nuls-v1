package io.nuls.protocol.rpc.cmd;

import io.nuls.core.tools.date.DateUtil;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Date;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/28
 */
public class GetBestBlockHeaderProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getbestblockheader";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription());
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getbestblockheader --get the best block header";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length > 1) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        RpcClientResult result = restFul.get("/block/newest/",null);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        Map<String, Object> map = (Map) result.getData();
        map.put("reward", CommandHelper.naToNuls(map.get("reward")));
        map.put("fee", CommandHelper.naToNuls(map.get("fee")));
        map.put("time", DateUtil.convertDate(new Date((Long) map.get("time"))));
        map.put("roundStartTime", DateUtil.convertDate(new Date((Long) map.get("roundStartTime"))));
        result.setData(map);
        return CommandResult.getResult(result);
    }
}
