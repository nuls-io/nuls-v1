/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.db.dao.impl.mybatis;

import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.BaseDataService;
import io.nuls.db.dao.impl.mybatis.common.BaseMapper;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.dao.impl.mybatis.session.SessionManager;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.transactional.annotation.PROPAGATION;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

/**
 * @author NielsWang
 * @date 2017/10/24
 * T : the type of Mapper interface
 * K : the type of primary key of Object
 * V : the type of Object
 */
@DbSession(transactional = PROPAGATION.NONE)
public abstract class BaseDaoImpl<T extends BaseMapper<K, V>, K, V> implements BaseDataService<K, V> {
    private Class<T> mapperClass;

    public BaseDaoImpl(Class<T> mapperClass) {
        this.mapperClass = mapperClass;
    }

    private SqlSession getSession() {
        return SessionManager.getSession();
    }

    protected T getMapper() {
        SqlSession session = getSession();
        return session.getMapper(mapperClass);
    }

    @Override
    @DbSession
    public int save(V o) {
        return getMapper().insert(o);
    }

    @Override
    @DbSession
    public int save(List<V> list) {
        try {
            if (null == list || list.isEmpty()) {
                return 0;
            }
            return getMapper().batchInsert(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    @Override
    @DbSession
    public int update(V o) {
        return this.getMapper().updateByPrimaryKey(o);
    }

    @Override
    @DbSession
    public int update(List<V> list) {
        int result = 0;
        for (int i = 0; i < list.size(); i++) {
            result += update(list.get(i));
        }
        return result;
    }

    @Override
    public V get(K key) {
        return this.getMapper().selectByPrimaryKey(key);
    }

    @Override
    @DbSession
    public int delete(K key) {
        return this.getMapper().deleteByPrimaryKey(key);
    }

    @Override
    public List<V> getList() {
        return this.getMapper().selectList(null);
    }

    @Override
    public final List<V> getList(Map<String, Object> params) {
        if (null == params || params.isEmpty()) {
            return getList();
        }
        return this.getMapper().selectList(getSearchable(params));
    }

    /**
     * change params to searchable object
     *
     * @param params
     * @return
     */
    protected abstract Searchable getSearchable(Map<String, Object> params);

    @Override
    public Long getCount() {
        return this.getMapper().countAll();
    }

    public long getCount(Map<String, Object> params) {
        return this.getMapper().selectCount(getSearchable(params));
    }
}
