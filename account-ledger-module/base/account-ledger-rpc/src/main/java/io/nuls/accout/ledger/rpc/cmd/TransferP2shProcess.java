package io.nuls.accout.ledger.rpc.cmd;

import io.nuls.accout.ledger.rpc.dto.MultipleTxToDto;
import io.nuls.accout.ledger.rpc.form.MultiSignForm;
import io.nuls.accout.ledger.rpc.form.TransferForm;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.*;

/**
 * @author: tag
 */
public class TransferP2shProcess implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    private ThreadLocal<MultiSignForm> paramsData = new ThreadLocal<>();

    @Override
    public String getCommand() {
        return "transferP2SH";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> \t\ttransfer address(If it's a trading promoter - Required; else - Not Required) ")
                .newLine("\t<signAddress> \tsign address - Required")
                .newLine("\t<toAddress>,<toamount>;....;<toAddress><toamount> \tThe meaning of [toAddress],[toamount] is pay  toAddress toamount nuls," +
                        "Separate multiple [toAddress],[toamount],If there are multiple payee Separate multiple. " +
                        "(If it's a trading promoter - Required; else - Not Required) toamount must greater than 0")
                .newLine("\t<amount> \t\tamount, you can have up to 8 valid digits after the decimal point(If it's a trading promoter - Required; else - Not Required) - Required")
                .newLine("\t<pubkey> \t\tPublic key that needs to be signed,If multiple commas are used to separate. (If it's a trading promoter - Required; else - Not Required)")
                .newLine("\t<m> \t\tAt least how many signatures are required to get the money. (If it's a trading promoter - Required; else - Not Required)")
                .newLine("\t<txdata> \t\ttransaction data (If it's not a trading promoter  - Required)")
                .newLine("\t[remark] \t\tremark - Not Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "transferP2SH --- If it's a trading promoter <address> <signAddress> <toAddress>,<toamount>;....;<toAddress><toamount> <pubkey>,...<pubkey> <m> <amount> [remark]" +
                "\t          --- else <address> <signAddress> <txdata>";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        //length=2表示不是第一个签名者
        if(length != 4 && length != 7 && length != 8){
            return  false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!AddressTool.validAddress(args[1]) || !AddressTool.validAddress(args[2])) {
            return false;
        }
        if(length == 4){
            if(args[3] == null || args[3].length() == 0){
                return false;
            }
        }else{
            if(!validToData(args[3])){
                return  false;
            }
            if(!StringUtils.validPubkeys(args[4],args[5])){
                return  false;
            }
            if(!StringUtils.isNuls(args[6])){
                return  false;
            }
        }
        MultiSignForm form =  getMultiSignForm(args);
        if(form == null)
            return false;
        paramsData.set(form);
        return true;
    }

    private MultiSignForm getMultiSignForm(String[] args) {
        MultiSignForm form = null;
        Long amount = null;
        try {
            Na na = Na.parseNuls(args[6]);
            if (na != null) {
                amount = na.getValue();
                if(amount <= 0)
                    return null;
                form = new MultiSignForm();
            } else {
                return null;
            }
            form.setAddress(args[1]);
            form.setSignAddress(args[2]);
            if(args.length == 4){
                form.setTxdata(args[3]);
            }else{
                List<MultipleTxToDto> toDatas = new ArrayList<>();
                String[] dataList = args[3].split(";");
                Long toAmount = null;
                for (String data:dataList) {
                    //将每个to数据拆分为数据
                    MultipleTxToDto toData = new MultipleTxToDto();
                    String[] separateData = data.split(",");
                    Na toNa = Na.parseNuls(separateData[1]);
                    if (toNa != null) {
                        toAmount = toNa.getValue();
                        if(toAmount <= 0)
                            return null;
                        toData.setAmount(toAmount);
                    } else {
                        return null;
                    }
                    toData.setToAddress(separateData[0]);
                    toDatas.add(toData);
                }
                form.setOutputs(toDatas);
                String[] pubkeys = args[4].split(",");
                form.setPubkeys(Arrays.asList(pubkeys));
                form.setM(Integer.parseInt(args[5]));
                form.setAmount(amount);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return form;
    }

    @Override
    public CommandResult execute(String[] args) {
        MultiSignForm form = paramsData.get();
        if (null == form) {
            form = getMultiSignForm(args);
        }
        String signAddress = form.getSignAddress();
        RpcClientResult res = CommandHelper.getPassword(signAddress, restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = (String)res.getData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("signAddress", form.getSignAddress());
        parameters.put("address", form.getAddress());
        parameters.put("password", password);
        if(args.length == 4){
            parameters.put("txdata", form.getTxdata());
        }else{
            parameters.put("outputs", form.getOutputs());
            parameters.put("amount", form.getAmount());
            parameters.put("remark", form.getRemark());
            parameters.put("pubkeys", form.getPubkeys());
            parameters.put("m", form.getM());
        }
        RpcClientResult result = restFul.post("/accountledger/transferP2SH", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(CommandResult.dataTransformValue(result));
    }

    public boolean validToData(String todata){
        if(StringUtils.isBlank(todata)){
            return  false;
        }
        //将多个to拆分
        String[] dataList = todata.split(";");
        if(dataList == null || dataList.length == 0){
            return false;
        }
        for (String data:dataList) {
            //将每个to数据拆分为数据
            String[] separateData = data.split(",");
            if(separateData == null || separateData.length != 2){
                return false;
            }
            if (!AddressTool.validAddress(separateData[0])  || !StringUtils.isNuls(separateData[1])) {
                return false;
            }
        }
        return true;
    }
}
