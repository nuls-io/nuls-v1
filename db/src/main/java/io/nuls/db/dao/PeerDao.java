package io.nuls.db.dao;

import io.nuls.db.entity.PeerPo;

import java.util.List;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public interface PeerDao extends BaseDao< String,PeerPo> {


    public List<PeerPo> getRandomPeerPoList(int size);

    public void saveChange(PeerPo po);
}
