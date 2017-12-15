package io.nuls.consensus.service.intf;

import io.nuls.account.entity.Address;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.Transaction;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/7
 */
public interface ConsensusService {

    Na getTxFee(long blockHeight, Transaction tx);

    void joinTheConsensus(String address, String password, Map<String, Object> paramsMap);

    void exitTheConsensus(Address address, String password);

    List<ConsensusAccount> getConsensusAccountList(Map<String, Object> params);

    ConsensusStatusInfo getConsensusInfo(String address);

}