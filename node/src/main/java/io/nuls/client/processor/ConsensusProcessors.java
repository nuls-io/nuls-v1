/*
 *
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.client.processor;

import io.nuls.client.entity.CommandResult;
import io.nuls.client.helper.CommandBulider;
import io.nuls.client.helper.CommandHelper;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.params.CreateAgentParams;
import io.nuls.rpc.sdk.params.DepositParams;
import io.nuls.rpc.sdk.params.StopAgentParams;
import io.nuls.rpc.sdk.params.WithdrawParams;
import io.nuls.rpc.sdk.service.ConsensusService;

/**
 * @author Niels
 * @date 2018/3/7
 */
public abstract class ConsensusProcessors implements CommandProcessor {

    protected ConsensusService consensusService = ConsensusService.CONSENSUS_SERVICE;

    public static class GetConsensus extends ConsensusProcessors {

        @Override
        public String getCommand() {
            return "getconsensus";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription());
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getconsensus -- get general consensus information";
        }

        @Override
        public boolean argsValidate(String[] args) {
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = consensusService.getConsensusNa2Nuls();
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class GetConsensusAddress extends ConsensusProcessors{
        @Override
        public String getCommand() {
            return "getconsensusaddress";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription())
                    .newLine("\t<address> 钱包账户地址 -required");
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getconsensusaddress <address> -- get consensus information by wallet address";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2){
                return false;
            }
            if(!StringUtils.validAddressSimple(args[1])){
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = consensusService.getConsensusAddressNa2Nuls(args[1]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class Agent extends ConsensusProcessors{
        @Override
        public String getCommand() {
            return "agent";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription())
                    .newLine("\t<agentAddress>   申请账户的地址 -required")
                    .newLine("\t<packingAddress>    打包地址    -required")
                    .newLine("\t<commissionRate>    佣金比例    -required")
                    .newLine("\t<deposit>   参与共识需要的总金额 -required")
                    .newLine("\t<agentName> 节点名称    -required")
                    .newLine("\t<password>  密码  -required")
                    .newLine("\t<remark>    节点备注    -required");
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "agent <agentAddress> <packingAddress> <commissionRate> <deposit> <agentName> <password> <remark>  --create a agent";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 8){
                return false;
            }
            if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validAddressSimple(args[2]) || StringUtils.isBlank(args[3])
                    || StringUtils.isBlank(args[4]) || StringUtils.isBlank(args[5]) || !StringUtils.validPassword(args[6])
                    || StringUtils.isBlank(args[7])){
                return false;
            }
            if(!StringUtils.isNumberGtZero(args[3])){
                return false;
            }
            if(!StringUtils.isNumberGtZero(args[4])){
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            CreateAgentParams params = new CreateAgentParams();
            params.setAgentAddress(args[1]);
            params.setPackingAddress(args[2]);
            params.setCommissionRate(Double.valueOf(args[3]));
            params.setDeposit(CommandHelper.getLongAmount(args[4]));
            params.setAgentName(args[5]);
            params.setPassword(args[6]);
            params.setRemark(args[7]);
            RpcClientResult result = consensusService.agent(params);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }


    public static class Deposit extends ConsensusProcessors {
        @Override
        public String getCommand() {
            return "deposit";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription())
                    .newLine("\t<address>   参与共识账户的地址 -required")
                    .newLine("\t<agentId>    共识节点的Id    -required")
                    .newLine("\t<deposit>   参与共识的金额 -required")
                    .newLine("\t<password>  密码  -required");
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "deposit <address> <agentId> <deposit> <password>  -- apply for deposit";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 5){
                return false;
            }
            if(!StringUtils.validAddressSimple(args[1]) || StringUtils.isBlank(args[2])
                    || StringUtils.isBlank(args[3]) || !StringUtils.validPassword(args[4])){
                return false;
            }

            if(!StringUtils.isNumberGtZero(args[3])){
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            DepositParams params = new DepositParams();
            params.setAddress(args[1]);
            params.setAgentId(args[2]);
            params.setDeposit(CommandHelper.getLongAmount(args[3]));
            params.setPassword(args[4]);
            RpcClientResult result = consensusService.deposit(params);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class StopAgent extends ConsensusProcessors{
        @Override
        public String getCommand() {
            return "stopagent";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription())
                    .newLine("\t<address>   账户的地址 -required")
                    .newLine("\t<password>  密码  -required");
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "stopagent <address> <password> -- stop the agent";
        }

        @Override
        public boolean argsValidate(String[] args) {
           int length = args.length;
           if(length != 3){
               return false;
           }
            if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validPassword(args[2])){
                return false;
            }
           return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            StopAgentParams stopAgentParams = new StopAgentParams();
            stopAgentParams.setAddress(args[1]);
            stopAgentParams.setPassword(args[2]);
            RpcClientResult result = consensusService.stopAgent(stopAgentParams);
            if(null == result){
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class GetAgent extends ConsensusProcessors{
        @Override
        public String getCommand() {
            return "getagent";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription())
                    .newLine("\t<address>   节点拥有人地址 -required");
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getagent <address> -- get agent information";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2){
                return false;
            }
            if(!StringUtils.validAddressSimple(args[1])){
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = consensusService.getAgentNa2Nuls(args[1]);
            if(null == result){
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class GetAgentStatus extends ConsensusProcessors{
        @Override
        public String getCommand() {
            return "getagentstatus";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription());
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getagentstatus -- get agent status";
        }

        @Override
        public boolean argsValidate(String[] args) {
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = consensusService.getAgentStatus();
            if(null == result){
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class Withdraw extends ConsensusProcessors{
        @Override
        public String getCommand() {
            return "withdraw";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription())
                    .newLine("\t<address>   节点地址 -required")
                    .newLine("\t<password>  密码  -required")
                    .newLine("\t<txHash>  加入共识时的交易hash  -required");
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "withdraw <address> <password> <txHash> -- withdraw the agent";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 4){
                return false;
            }
            if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validPassword(args[2])
                    || !StringUtils.validHash(args[3])){
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            WithdrawParams withdrawParams = new WithdrawParams();
            withdrawParams.setAddress(args[1]);
            withdrawParams.setPassword(args[2]);
            withdrawParams.setTxHash(args[3]);
            RpcClientResult result = consensusService.withdraw(withdrawParams);
            if(null == result){
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class GetAllAgents extends ConsensusProcessors{
        @Override
        public String getCommand() {
            return "getallagents";
        }

        @Override
        public String getHelp() {
            CommandBulider bulider = new CommandBulider();
            bulider.newLine(getCommandDescription());
            return bulider.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getallagents -- get all of agents";
        }

        @Override
        public boolean argsValidate(String[] args) {
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = consensusService.getAllAgent();
            if(null == result){
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

}
