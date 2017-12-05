package io.nuls.consensus.service.intf;

import io.nuls.account.entity.Address;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.entity.ConsensusStatusInfo;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/7
 */
public interface ConsensusService {

    void joinTheConsensus(String address, String password, Object... params);

    void exitTheConsensus(Address address, String password);

    List<ConsensusAccount> getConsensusAccountList();

    ConsensusStatusInfo getConsensusInfo();
}
