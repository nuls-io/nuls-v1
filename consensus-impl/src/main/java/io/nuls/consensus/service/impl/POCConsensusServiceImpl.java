package io.nuls.consensus.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.entity.ConsensusInfo;
import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.chain.entity.transaction.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;

import java.util.List;

/**
 * Created by Niels on 2017/11/9.
 * nuls.io
 */
public class POCConsensusServiceImpl implements ConsensusService {

    private static final ConsensusService instance = new POCConsensusServiceImpl();
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    private POCConsensusServiceImpl() {
    }

    public static ConsensusService getInstance() {
        return instance;
    }

    @Override
    public void joinTheConsensus(String address, String password, double amount) {
        this.joinTheConsensus(address, password, amount, null);
    }

    @Override
    public void joinTheConsensus(String password, double amount) {
        Account localAccount = this.accountService.getLocalAccount();
        if (null == localAccount) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Please specify the account address");
        }
        this.joinTheConsensus(localAccount, password, amount, null);
    }

    @Override
    public void joinTheConsensus(String address, String password, double amount, String agent) {
        Account account = this.accountService.getAccount(address);
        if (null == account) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The account is not exist,address:" + address);
        }
        this.joinTheConsensus(account, password, amount, agent);
    }

    @Override
    public void joinTheConsensus(String password, double amount, String agent) {
        Account localAccount = this.accountService.getLocalAccount();
        if (null == localAccount) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Please specify the account address");
        }
        this.joinTheConsensus(localAccount, password, amount, agent);
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
        return null;
    }

    @Override
    public ConsensusStatusEnum getConsensusStatus() {
        return null;
    }

    @Override
    public ConsensusInfo getConsensusInfo() {
        return null;
    }

    @Override
    public Double getMarginAmount() {
        return null;
    }
}
