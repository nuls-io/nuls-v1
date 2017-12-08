package io.nuls.consensus.service.intf;

import io.nuls.account.entity.Address;
import io.nuls.consensus.entity.ConsensusMember;
import io.nuls.consensus.entity.ConsensusStatusInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/7
 */
public interface ConsensusService {

    void joinTheConsensus(String address, String password, Map<String, Object> paramsMap);

    void exitTheConsensus(Address address, String password);

    List<ConsensusMember> getConsensusAgentList(Map<String, Object> params);

    ConsensusStatusInfo getConsensusInfo(String address);

}