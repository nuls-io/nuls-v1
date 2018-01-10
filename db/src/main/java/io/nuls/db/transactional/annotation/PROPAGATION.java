package io.nuls.db.transactional.annotation;

/**
 *
 * @author zhouwei
 * @date 2017/10/26
 */
public enum  PROPAGATION {

    REQUIRED("required"),
    INDEPENDENT("independent");


    PROPAGATION(String value) {
        this.value = value;
    }

    private String value;
}
