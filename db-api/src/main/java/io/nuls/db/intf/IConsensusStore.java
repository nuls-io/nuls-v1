package io.nuls.db.intf;

import io.nuls.db.entity.ConsensusAccount;

import java.util.List;

/**
 * Created by zhouwei on 2017/10/19.
 */
public interface IConsensusStore extends IStore<ConsensusAccount, Long> {

    /**
     * 获取当前所有共识节点
     * @return
     */
    List<ConsensusAccount> getConsensusAccts();
}
