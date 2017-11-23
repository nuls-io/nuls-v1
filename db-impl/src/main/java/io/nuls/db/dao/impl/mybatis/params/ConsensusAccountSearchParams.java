package io.nuls.db.dao.impl.mybatis.params;

import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/23
 */
public class ConsensusAccountSearchParams extends Searchable {
    public static final String SEARCH_FIELD_ADDRESS = "address";
    public static final String SEARCH_FIELD_AGENT_ADDRESS = "agent_address";
    public static final String SEARCH_FIELD_DEPOSIT = "deposit";
    public static final String SEARCH_FIELD_DEPOSIT_GREAT_THAN = "deposit_gt";
    public static final String SEARCH_FIELD_DEPOSIT_LESS_THAN = "deposit_lt";

    public ConsensusAccountSearchParams(Map<String, Object> params) {
        if (null == params) {
            return;
        }
        if (params.containsKey(SEARCH_FIELD_ADDRESS)) {
            this.addCondition(SEARCH_FIELD_ADDRESS, SearchOperator.eq, params.get(SEARCH_FIELD_ADDRESS));
        }
        if (params.containsKey(SEARCH_FIELD_AGENT_ADDRESS)) {
            this.addCondition(SEARCH_FIELD_AGENT_ADDRESS, SearchOperator.eq, params.get(SEARCH_FIELD_AGENT_ADDRESS));
        }
        if (params.containsKey(SEARCH_FIELD_DEPOSIT)) {
            this.addCondition(SEARCH_FIELD_DEPOSIT, SearchOperator.eq, params.get(SEARCH_FIELD_DEPOSIT));
        }
        if (params.containsKey(SEARCH_FIELD_DEPOSIT_GREAT_THAN)) {
            this.addCondition(SEARCH_FIELD_DEPOSIT, SearchOperator.gt, params.get(SEARCH_FIELD_DEPOSIT_GREAT_THAN));
        }
        if (params.containsKey(SEARCH_FIELD_DEPOSIT_LESS_THAN)) {
            this.addCondition(SEARCH_FIELD_DEPOSIT, SearchOperator.lt, params.get(SEARCH_FIELD_DEPOSIT_LESS_THAN));
        }
    }
}
