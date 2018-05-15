package io.nuls.consensus.poc.rpc.resource;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.service.AccountService;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.consensus.poc.protocol.tx.DepositTransaction;
import io.nuls.consensus.poc.rpc.model.*;
import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.page.Page;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.protocol.service.TransactionService;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2018/5/15
 */
@Path("/poc")
@Api(value = "/consensus", description = "poc-consensus")
@Component
public class PocConsensusResource {

    @Autowired
    private ConsensusService consensusService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Get the whole network consensus infomation! 查询全网共识总体信息 [3.6.1]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = WholeNetConsensusInfoDTO.class)
    })
    public Result getWholeInfo() {
        Result result = Result.getSuccess();
//        WholeNetConsensusInfoDTO dto = new WholeNetConsensusInfoDTO();
//
//        Map<String, Object> map = this.consensusService.getConsensusInfo();
//        dto.setAgentCount((Integer) map.get("agentCount"));
//        dto.setRewardOfDay((Long) map.get("rewardOfDay"));
//        dto.setTotalDeposit((Long) map.get("totalDeposit"));
//        dto.setConsensusAccountNumber((Integer) map.get("memberCount"));
//        result.setData(dto);
        return result;
    }

    @GET
    @Path("/local")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("获取钱包内(本地)全部账户参与共识信息 [3.6.2b]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Map.class)
    })
    public Result getInfo() {
        Result result = Result.getSuccess();
//        Map<String, Object> dataMap = consensusService.getConsensusInfo(null);
//        result.setData(dataMap);
        return result;
    }

    @GET
    @Path("/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("获取钱包内某个账户参与共识信息 [3.6.2a]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Map.class)
    })
    public Result getInfo(@ApiParam(name = "address", value = "钱包账户地", required = true)
                          @PathParam("address") String address) {

//        if (!Address.validAddress(StringUtils.formatStringPara(address))) {
//            return Result.getFailed(ErrorCode.ADDRESS_ERROR);
//        }
        Result result = Result.getSuccess();
//
//        Map<String, Object> dataMap = consensusService.getConsensusInfo(address);
//        result.setData(dataMap);
        return result;
    }

    @POST
    @Path("/agent")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create an agent for consensus! 创建共识(代理)节点 [3.6.3]", notes = "返回创建的节点成功的交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public Result<String> createAgent(@ApiParam(name = "form", value = "创建节点表单数据", required = true)
                                              CreateAgentForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAgentAddress());
        AssertUtil.canNotEmpty(form.getAgentName());
        AssertUtil.canNotEmpty(form.getPackingAddress());
        AssertUtil.canNotEmpty(form.getDeposit());
        AssertUtil.canNotEmpty(form.getRemark());
        AssertUtil.canNotEmpty(form.getPassword());

        if (!AddressTool.validAddress(form.getPackingAddress()) || !AddressTool.validAddress(form.getAgentAddress())) {
            throw new NulsRuntimeException(KernelErrorCode.PARAMETER_ERROR);
        }
        Account account = accountService.getAccount(form.getAgentAddress()).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        CreateAgentTransaction tx = new CreateAgentTransaction();
        tx.setTime(TimeService.currentTimeMillis());
        Agent agent = new Agent();
        agent.setAgentAddress(AddressTool.getAddress(form.getAgentAddress()));
        agent.setPackingAddress(AddressTool.getAddress(form.getPackingAddress()));
        if (StringUtils.isBlank(form.getRewardAddress())) {
            agent.setRewardAddress(agent.getAgentAddress());
        } else {
            agent.setRewardAddress(AddressTool.getAddress(form.getRewardAddress()));
        }
        try {
            agent.setAgentName(form.getAgentName().getBytes(NulsConfig.DEFAULT_ENCODING));
            agent.setIntroduction(form.getRemark().getBytes(NulsConfig.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
        agent.setDeposit(Na.valueOf(form.getDeposit()));
        agent.setCommissionRate(form.getCommissionRate());
        tx.setTxData(agent);
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(agent.getAgentAddress(), agent.getDeposit(), -1));
        tx.setCoinData(coinData);
        CoinDataResult result = accountLedgerService.getCoinData(agent.getAgentAddress(), agent.getDeposit(), tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH);
        if (result.isEnough()) {
            tx.getCoinData().setFrom(result.getCoinList());
            if (null != result.getChange()) {
                tx.getCoinData().getTo().add(result.getChange());
            }
        } else {
            return Result.getFailed(TransactionErrorCode.BALANCE_NOT_ENOUGH);
        }
        try {
            tx.setHash(NulsDigestData.calcDigestData(tx.serialize()));
            P2PKHScriptSig sig = new P2PKHScriptSig();
            sig.setPublicKey(account.getPubKey());
            sig.setSignData(accountService.signData(tx.getHash().serialize(), account, form.getPassword()));
            tx.setScriptSig(sig.serialize());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
        tx.verifyWithException();

        Result saveResult = accountLedgerService.saveUnconfirmedTransaction(tx);
        if (saveResult.isFailed()) {
            return saveResult;
        }
        Result sendResult = this.transactionService.broadcastTx(tx);
        if (sendResult.isFailed()) {
            return sendResult;
        }
        return Result.getSuccess().setData(tx.getHash().getDigestHex());
    }


    @POST
    @Path("/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "deposit nuls to a bank! 申请参与共识 [3.6.4]", notes = "返回申请成功交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public Result depositToAgent(@ApiParam(name = "form", value = "申请参与共识表单数据", required = true)
                                         DepositForm form) throws NulsException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getAgentHash());
        AssertUtil.canNotEmpty(form.getDeposit());
        AssertUtil.canNotEmpty(form.getPassword());
        Map<String, Object> paramsMap = new HashMap<>();
        if (!Address.validAddress(form.getAddress())) {
            throw new NulsRuntimeException(KernelErrorCode.PARAMETER_ERROR);
        }

        paramsMap.put("deposit", form.getDeposit());
        paramsMap.put("agentHash", form.getAgentHash());
        DepositTransaction tx = new DepositTransaction();

        return Result.getSuccess().setData(tx.getHash().getDigestHex());
    }


    @POST
    @Path("/agent/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "删除共识节点 [3.6.5]", notes = "返回删除成功交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public Result stopAgent(@ApiParam(name = "form", value = "删除共识节点表单数据", required = true)
                                    StopAgentForm form) throws NulsException, IOException {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getAddress());
        AssertUtil.canNotEmpty(form.getPassword());
        Transaction tx = null;//consensusService.stopConsensus(form.getAddress(), form.getPassword(), null);
        return Result.getSuccess().setData(tx.getHash().getDigestHex());
    }

    @GET
    @Path("/agent/list")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询共识节点列表信息 [3.6.6]", notes = "result.data.page.list: List<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public Result getAgentList(@ApiParam(name = "pageNumber", value = "页码")
                               @QueryParam("pageNumber") Integer pageNumber,
                               @ApiParam(name = "pageSize", value = "每页条数")
                               @QueryParam("pageSize") Integer pageSize,
                               @ApiParam(name = "keyword", value = "搜索关键字")
                               @QueryParam("keyword") String keyword,
                               @ApiParam(name = "sortType", value = "排序字段名")
                               @QueryParam("sortType") String sortType) {
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = Result.getSuccess();
        Page<Map<String, Object>> list = null;//this.consensusService.getAgentList(keyword, null, null, sortType, pageNumber, pageSize);
        result.setData(list);
        return result;
    }

    @GET
    @Path("/agent/{agentAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询共识节点详细信息 [3.6.7]", notes = "result.data: Map<String, Object>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Map.class)
    })
    public Result getAgentByAddress(@ApiParam(name = "agentAddress", value = "节点地址", required = true)
                                    @PathParam("agentAddress") String agentAddress) {
        if (!Address.validAddress(agentAddress)) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = Result.getSuccess();
        Map<String, Object> data = null;//this.consensusService.getAgent(agentAddress);
        result.setData(data);
        return result;
    }

    @GET
    @Path("/agent/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据地址查询其委托的节点列表 [3.6.12]",
            notes = "result.data.page.list: List<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public Result getAgentListByDepositAddress(@ApiParam(name = "pageNumber", value = "页码")
                                               @QueryParam("pageNumber") Integer pageNumber,
                                               @ApiParam(name = "pageSize", value = "每页条数")
                                               @QueryParam("pageSize") Integer pageSize,
                                               @ApiParam(name = "address", value = "账户地址", required = true)
                                               @PathParam("address") String address) {
        Result result = Result.getSuccess();

        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }

        Page<Map<String, Object>> list = null;//this.consensusService.getAgentList(null, address, null, null, pageNumber, pageSize);
        result.setData(list);
        return result;
    }

    @GET
    @Path("/deposit/address/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询指定地址参与的所有委托信息列表 [3.6.8]",
            notes = "result.data.page.list: List<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public Result getDepositListByAddress(@ApiParam(name = "address", value = "账户地址", required = true)
                                          @PathParam("address") String address,
                                          @ApiParam(name = "pageNumber", value = "页码")
                                          @QueryParam("pageNumber") Integer pageNumber,
                                          @ApiParam(name = "pageSize", value = "每页条数")
                                          @QueryParam("pageSize") Integer pageSize,
                                          @ApiParam(name = "agentAddress", value = "指定代理节点地址(不传查所有)")
                                          @QueryParam("agentAddress") String agentAddress) {
        AssertUtil.canNotEmpty(address);
        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = Result.getSuccess();

        Page<Map<String, Object>> page = null;//this.consensusService.getDepositList(address, agentAddress, pageNumber, pageSize);
        result.setData(page);
        return result;
    }

    @GET
    @Path("/deposit/agent/{agentAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询共识节点受托列表信息 [3.6.9]",
            notes = "result.data.page.list: List<Map<String, Object>>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Page.class)
    })
    public Result queryDepositListByAgentAddress(@ApiParam(name = "agentAddress", value = "指定代理节点地址", required = true)
                                                 @PathParam("agentAddress") String agentAddress,
                                                 @ApiParam(name = "pageNumber", value = "页码")
                                                 @QueryParam("pageNumber") Integer pageNumber,
                                                 @ApiParam(name = "pageSize", value = "每页条数")
                                                 @QueryParam("pageSize") Integer pageSize) {
        AssertUtil.canNotEmpty(agentAddress);

        if (null == pageNumber || pageNumber == 0) {
            pageNumber = 1;
        }
        if (null == pageSize || pageSize == 0) {
            pageSize = 10;
        }
        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = Result.getSuccess();

        Page<Map<String, Object>> page = null;//this.consensusService.getDepositList(null, agentAddress, pageNumber, pageSize);
        result.setData(page);
        return result;
    }

    @GET
    @Path("/agent/status")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询所有共识信息状态 [3.6.10]",
            notes = "result.data: Map<String, Object>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Map.class)
    })
    public Result getAllAgentStatusList() {
//        List<AgentPo> poList = agentDataService.getList();
//        if (null == poList || poList.isEmpty()) {
//            return Result;
//        }
        Map<String, Object> statusMap = new HashMap<>();
//        MeetingRound round = this.consensusService.getCurrentRound();
//        for (AgentPo po : poList) {
//            if (null != round && round.getMember(po.getPackingAddress()) != null) {
//                statusMap.put(po.getAgentAddress(), ConsensusStatusEnum.IN.getCode());
//            } else {
//                statusMap.put(po.getAgentAddress(), ConsensusStatusEnum.WAITING.getCode());
//            }
//        }
        return Result.getSuccess().setData(statusMap);
    }

    @POST
    @Path("/withdraw")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "退出共识 [3.6.11]",
            notes = "返回退出成功的交易hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String.class)
    })
    public Result exitConsensus(@ApiParam(name = "form", value = "退出共识表单数据", required = true)
                                        WithdrawForm form) {
        AssertUtil.canNotEmpty(form);
        AssertUtil.canNotEmpty(form.getTxHash());
        AssertUtil.canNotEmpty(form.getPassword());
        AssertUtil.canNotEmpty(form.getAddress());
        if (!Address.validAddress(form.getAddress())) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("txHash", form.getTxHash());
        Transaction tx = null;
//        try {
//            tx = consensusService.stopConsensus(form.getAddress(), form.getPassword(), params);
//        } catch (NulsException e) {
//            Log.error(e);
//            return Result.getFailed(e.getMessage());
//        } catch (IOException e) {
//            Log.error(e);
//            return Result.getFailed(e.getMessage());
//        }
        return Result.getSuccess().setData(tx.getHash().getDigestHex());
    }
}
