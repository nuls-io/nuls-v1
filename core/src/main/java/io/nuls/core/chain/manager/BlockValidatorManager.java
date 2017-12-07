package io.nuls.core.chain.manager;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.validate.NulsDataValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/7
 */
public class BlockValidatorManager {

    private static final List<NulsDataValidator<Block>> ALL_LIST = new ArrayList<>();

    /**
     * the validator fit Block instance
     * @param validator
     */
    public static void addBlockDefValitor(NulsDataValidator validator) {
        ALL_LIST.add(validator);
    }

    public static final List<NulsDataValidator> getValidators() {
        List<NulsDataValidator> list = new ArrayList<>();
        list.addAll(ALL_LIST);
        return list;
    }
}
