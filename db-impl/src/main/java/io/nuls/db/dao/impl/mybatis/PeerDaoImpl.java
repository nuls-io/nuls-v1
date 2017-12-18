package io.nuls.db.dao.impl.mybatis;

import com.github.pagehelper.PageHelper;
import io.nuls.core.utils.date.TimeService;
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
    public List<PeerPo> getRandomPeerPoList(int size, Set<String> keys) {
        Searchable searchable = new Searchable();
        PageHelper.startPage(1, 100);
        PageHelper.orderBy("last_fail_time asc");
        searchable.addCondition("id", SearchOperator.notIn, keys);
        searchable.addCondition("last_fail_time", SearchOperator.lt, TimeService.currentTimeMillis() - TimeService.ONE_HOUR);
        List<PeerPo> list = getMapper().selectList(searchable);
        if (list.size() <= size) {
            return list;
        } else {
            Collections.shuffle(list);
        }

        return list.subList(0, size - 1);
    }

    @Override
    @SessionAnnotation
    public void saveChange(PeerPo po) {
        try {
            Searchable searchable = new Searchable();
            searchable.addCondition("ip", SearchOperator.eq, po.getIp());
            searchable.addCondition("port", SearchOperator.eq, po.getPort());
            if (getMapper().selectCount(searchable) > 0) {
                if (po.getFailCount() >= 3) {
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
