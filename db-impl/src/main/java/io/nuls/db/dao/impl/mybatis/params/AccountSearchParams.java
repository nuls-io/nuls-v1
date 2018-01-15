package io.nuls.db.dao.impl.mybatis.params;

import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/23
 */
public class AccountSearchParams extends Searchable {
    public static final String SEARCH_FIELD_ALIAS = "alias";
    public static final String SEARCH_FIELD_ADDRESS = "address";
    public static final String SEARCH_FIELD_PRI_KEY = "priKey";

    public AccountSearchParams(Map<String, Object> params) {
        if (null == params) {
            return;
        }
        if (params.containsKey(SEARCH_FIELD_ALIAS)) {
            this.addCondition(SEARCH_FIELD_ALIAS, SearchOperator.eq, params.get(SEARCH_FIELD_ALIAS));
        }
        if (params.containsKey(SEARCH_FIELD_ADDRESS)) {
            this.addCondition(SEARCH_FIELD_ADDRESS, SearchOperator.eq, params.get(SEARCH_FIELD_ADDRESS));
        }
        if (params.containsKey(SEARCH_FIELD_PRI_KEY)) {
            this.addCondition(SEARCH_FIELD_PRI_KEY, SearchOperator.eq, params.get(SEARCH_FIELD_PRI_KEY));
        }
    }
}
