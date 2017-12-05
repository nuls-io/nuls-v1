package io.nuls.consensus.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.entity.Agent;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/9
 */
public class POCConsensusServiceImpl implements ConsensusService {

    private static final ConsensusService INSTANCE = new POCConsensusServiceImpl();
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private POCConsensusServiceImpl() {
    }

    public static ConsensusService getInstance() {
        return INSTANCE;
    }

    public void registerAgent(Agent agent, String password){
        //todo e
    }

    private void joinTheConsensus(Account account, String password, double amount, String agent) {
        //todo
        //check password
        //check block height
        //check consensus state
        //check credit
        //check balance

//        Transaction tx = createTransaction(address,agentMiner,inputs,sign);
//        JoinConsensusEvent event = createJoinEvent( tx);
        //broadcast
        //change consensus status in cache
    }


    @Override
    public void exitTheConsensus(Address address, String password) {
        //验证密码

        //组装申请

        //广播
    }

    @Override
    public List<ConsensusAccount> getConsensusAccountList() {
        //todo
        return null;
    }

    @Override
    public ConsensusStatusInfo getConsensusInfo() {
        //todo
        return null;
    }

    @Override
    public void joinTheConsensus(String address, String password,Object... params) {
        final int paramsLength = 3;
        Account account = this.accountService.getAccount(address);
        if (null == account) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The account is not exist,address:" + address);
        }
        if(params ==null||params.length<2){
            throw new NulsRuntimeException(ErrorCode.NULL_PARAMETER );
        }
        double deposit = (double) params[0];
        String agentAddress = (String) params[1];
        if(params.length>=paramsLength){
            Agent agent = new Agent();
            agent.setAddress(address);
            agent.setDelegateAddress(agentAddress);
            agent.setDeposit(deposit);
            agent.setCommissionRate((Double) params[2]);
            if(params.length>paramsLength){
                agent.setIntroduction((String) params[3]);
            }
            this.registerAgent(agent,password);
            return;
        }
        this.joinTheConsensus(account, password, deposit, agentAddress);
    }

    public void joinTheConsensus(String password, double amount, String agent) {
        Account localAccount = this.accountService.getLocalAccount();
        if (null == localAccount) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Please specify the account address");
        }
        this.joinTheConsensus(localAccount, password, amount, agent);
    }
}
