package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.PeerDao;
import io.nuls.db.dao.impl.mybatis.mapper.PeerMapper;
import io.nuls.db.dao.impl.mybatis.params.PeerSearchParams;
import io.nuls.db.dao.impl.mybatis.session.SessionAnnotation;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.PeerPo;

import java.util.*;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class PeerDaoImpl extends BaseDaoImpl<PeerMapper, String, PeerPo> implements PeerDao {
    public PeerDaoImpl() {
        super(PeerMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return new PeerSearchParams(params);
    }

    @Override
    public List<PeerPo> getRandomPeerPoList(int size) {
        long count = getCount();
        if (count == 0) {
            return null;
        }

        List<PeerPo> peerPos = null;
        if (count <= size) {
            peerPos = searchList(null);
        } else {
            Random random = new Random();
            Set<Integer> set = new HashSet<>();
            while (set.size() < size) {
                set.add(random.nextInt((int) count));
            }

            Map<String, Object> map = new HashMap<>();
            map.put(PeerSearchParams.SEARCH_RANDOM, set);
            peerPos = searchList(map);
        }
        return peerPos;
    }

    @Override
    @SessionAnnotation
    public void saveChange(PeerPo po) {
        try {
            Searchable searchable = new Searchable();
            searchable.addCondition("ip", SearchOperator.eq, po.getIp());
            searchable.addCondition("port", SearchOperator.eq, po.getPort());
            if (getMapper().selectCount(searchable) > 0) {
                if (po.getFailCount() >= 5) {
                    getMapper().deleteByIp(po);
                } else {
                    getMapper().updateByIp(po);
                }
            } else {
                getMapper().insert(po);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
