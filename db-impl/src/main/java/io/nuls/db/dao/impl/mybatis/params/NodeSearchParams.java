package io.nuls.db.dao.impl.mybatis.params;

import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/23
 */
public class NodeSearchParams extends Searchable {
    public static final String SEARCH_FIELD_IP = "ip";
    public static final String SEARCH_FIELD_MAGIC_NUM = "magic_num";
    public static final String SEARCH_RANDOM = "ROWNUM()";
    public static final String SEARCH_FAIL_TIME = "last_fail_time";

    public NodeSearchParams(Map<String, Object> params) {
        if (null == params) {
            return;
        }
        if (params.containsKey(SEARCH_FIELD_IP)) {
            this.addCondition(SEARCH_FIELD_IP, SearchOperator.eq, params.get(SEARCH_FIELD_IP));
        }
        if (params.containsKey(SEARCH_FIELD_MAGIC_NUM)) {
            this.addCondition(SEARCH_FIELD_MAGIC_NUM, SearchOperator.eq, params.get(SEARCH_FIELD_MAGIC_NUM));
        }
        if(params.containsKey(SEARCH_RANDOM)) {
            this.addCondition(SEARCH_RANDOM, SearchOperator.in, params.get(SEARCH_RANDOM));
        }
        if(params.containsKey(SEARCH_FAIL_TIME)) {
            this.addCondition(SEARCH_FAIL_TIME, SearchOperator.lt, params.get(SEARCH_FAIL_TIME));
        }
    }
}
