package io.nuls.accout.ledger.rpc.cmd;

import io.nuls.account.model.Address;
import io.nuls.accout.ledger.rpc.form.TransferForm;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/28
 */
public class TransferProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    private ThreadLocal<TransferForm> paramsData = new ThreadLocal<>();

    @Override
    public String getCommand() {
        return "transfer";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> \t\tsource address - Required")
                .newLine("\t<toaddress> \treceiving address - Required")
                .newLine("\t<amount> \t\tamount - Required")
                .newLine("\t[remark] \t\tremark - ");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "transfer <address> <toAddress> <amount> [remark] --transfer";
    }

    @Override
    public boolean argsValidate(String[] args) {
        boolean result;
        do {
            int length = args.length;
            if (length != 4 && length != 5) {
                result = false;
                break;
            }
            if (!CommandHelper.checkArgsIsNull(args)) {
                result = false;
                break;
            }
            if (!Address.validAddress(args[1]) || !Address.validAddress(args[2])) {
                return false;
            }
            if (!StringUtils.isNumberGtZero(args[3])) {
                result = false;
                break;
            }
            TransferForm form = getTransferForm(args);
            paramsData.set(form);
            result = StringUtils.isNotBlank(form.getToAddress());
            if (!result) {
                break;
            }
            result = form.getAmount() > 0;
        } while (false);
        return result;
    }

    private TransferForm getTransferForm(String[] args) {
        TransferForm form = null;
        Long amount = null;
        try {
            Na na = Na.parseNuls(args[3]);
            if (na != null) {
                amount = na.getValue();
                form = new TransferForm();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        switch (args.length) {
            case 4:
                form.setAddress(args[1]);
                form.setToAddress(args[2]);
                form.setAmount(amount);
                break;
            case 5:
                form.setAddress(args[1]);
                form.setToAddress(args[2]);
                form.setAmount(amount);
                form.setRemark(args[4]);
                break;
        }
        return form;
    }

    @Override
    public CommandResult execute(String[] args) {
        TransferForm form = paramsData.get();
        if (null == form) {
            form = getTransferForm(args);
        }
        String password = CommandHelper.getPwd();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", form.getAddress());
        parameters.put("toAddress", form.getToAddress());
        parameters.put("password", password);
        parameters.put("amount", form.getAmount());
        parameters.put("remark", form.getRemark());
        RpcClientResult result = restFul.post("/accountledger/transfer", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
