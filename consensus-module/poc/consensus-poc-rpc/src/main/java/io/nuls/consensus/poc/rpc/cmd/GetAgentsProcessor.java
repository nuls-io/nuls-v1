package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.date.DateUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取共识节点列表
 * Get all the agent nodes
 *
 * @author: Charlie
 * @date: 2018/5/29
 */
public class GetAgentsProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getagents";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<pageNumber> pageNumber - Required")
                .newLine("\t<pageSize> pageSize(1~100) - Required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getagents <pageNumber> <pageSize> --get agent list";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 3) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!StringUtils.isNumeric(args[1]) || !StringUtils.isNumeric(args[2])) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        int pageNumber = Integer.parseInt(args[1]);
        int pageSize = Integer.parseInt(args[2]);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pageNumber", pageNumber);
        parameters.put("pageSize", pageSize);
        RpcClientResult result = restFul.get("/consensus/agent/list", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) ((Map) result.getData()).get("list");
        for (Map<String, Object> map : list) {
            map.put("deposit", CommandHelper.naToNuls(map.get("deposit")));
            map.put("totalDeposit", CommandHelper.naToNuls(map.get("totalDeposit")));
            map.put("time", DateUtil.convertDate(new Date((Long) map.get("time"))));
            map.put("status", CommandHelper.consensusExplain((Integer) map.get("status")));
        }
        result.setData(list);
        return CommandResult.getResult(result);
    }

}
