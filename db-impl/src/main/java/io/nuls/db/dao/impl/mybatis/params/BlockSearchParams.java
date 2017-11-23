package io.nuls.db.dao.impl.mybatis.params;

import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/23
 */
public class BlockSearchParams extends Searchable {
    public static final String SEARCH_FIELD_HASH = "hash";
    public static final String SEARCH_FIELD_HEIGHT = "height";
    public static final String SEARCH_FIELD_PRE_HASH = "pre_Hash";
    public static final String SEARCH_FIELD_MERKLE_HASH = "merkle_hash";
    public static final String SEARCH_FIELD_CONSENSUS_ADDRESS = "consensus_address";

    public BlockSearchParams(Map<String, Object> params) {
        if (null == params) {
            return;
        }
        if (params.containsKey(SEARCH_FIELD_HASH)) {
            this.addCondition(SEARCH_FIELD_HASH, SearchOperator.eq, params.get(SEARCH_FIELD_HASH));
        }
        if (params.containsKey(SEARCH_FIELD_HEIGHT)) {
            this.addCondition(SEARCH_FIELD_HEIGHT, SearchOperator.eq, params.get(SEARCH_FIELD_HEIGHT));
        }
        if (params.containsKey(SEARCH_FIELD_PRE_HASH)) {
            this.addCondition(SEARCH_FIELD_PRE_HASH, SearchOperator.eq, params.get(SEARCH_FIELD_PRE_HASH));
        }
        if (params.containsKey(SEARCH_FIELD_MERKLE_HASH)) {
            this.addCondition(SEARCH_FIELD_MERKLE_HASH, SearchOperator.eq, params.get(SEARCH_FIELD_MERKLE_HASH));
        }
        if (params.containsKey(SEARCH_FIELD_CONSENSUS_ADDRESS)) {
            this.addCondition(SEARCH_FIELD_CONSENSUS_ADDRESS, SearchOperator.eq, params.get(SEARCH_FIELD_CONSENSUS_ADDRESS));
        }
    }
}
