package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.core.tools.date.DateUtil;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Date;
import java.util.Map;

/**
 * 根据节点hash获取单个共识节点信息
 * Get a consensus node information According to agent hash
 *
 * @author: Charlie
 * @date: 2018/5/29
 */
public class GetAgentProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getagent";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<agentHash>  the hash of an agent -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getagent <agentHash>  -- get an agent node information According to agent hash";
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
        if (!StringUtils.validHash(args[1])) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String agentHash = args[1];
        RpcClientResult result = restFul.get("/consensus/agent/" + agentHash, null);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        Map<String, Object> map = (Map) result.getData();
        map.put("deposit", CommandHelper.naToNuls(map.get("deposit")));
        map.put("totalDeposit", CommandHelper.naToNuls(map.get("totalDeposit")));
        map.put("time", DateUtil.convertDate(new Date((Long) map.get("time"))));
        map.put("status", CommandHelper.consensusExplain((Integer) map.get("status")));
        result.setData(map);
        return CommandResult.getResult(result);
    }
}
