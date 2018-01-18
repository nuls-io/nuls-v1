package io.nuls.db.dao.impl.mybatis.params;

import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/23
 */
public class DelegateSearchParams extends Searchable {
    public static final String SEARCH_FIELD_AGENT_ADDRESS = "agentAddress";
    public static final String SEARCH_FIELD_ADDRESS = "address";

    public DelegateSearchParams(Map<String, Object> params) {
        if (null == params) {
            return;
        }
        if (params.containsKey(SEARCH_FIELD_ADDRESS)) {
            this.addCondition(SEARCH_FIELD_ADDRESS, SearchOperator.eq, params.get(SEARCH_FIELD_ADDRESS));
        }
        if (params.containsKey(SEARCH_FIELD_AGENT_ADDRESS)) {
            this.addCondition(SEARCH_FIELD_AGENT_ADDRESS, SearchOperator.eq, params.get(SEARCH_FIELD_AGENT_ADDRESS));
        }
    }
}
