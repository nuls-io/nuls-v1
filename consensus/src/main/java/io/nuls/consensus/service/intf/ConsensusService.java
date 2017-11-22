package io.nuls.consensus.service.intf;

import io.nuls.account.entity.Address;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.entity.ConsensusInfo;

import java.util.List;

/**
 *
 * @author Niels
 * @date 2017/11/7
 *
 */
public interface ConsensusService {

    /**
     * @param password
     * @param amount
     * @return lock margin Transaction id
     */
    void joinTheConsensus(String address, String password, double amount);
    void joinTheConsensus(String password, double amount);
    void joinTheConsensus(String address, String password, double amount, String agent);
    void joinTheConsensus(String password, double amount, String agent);

    void exitTheConsensus(Address address, String password);

    List<ConsensusAccount> getConsensusAccountList();

    ConsensusStatusEnum getConsensusStatus();

    ConsensusInfo getConsensusInfo();

    Double getMarginAmount();
}
