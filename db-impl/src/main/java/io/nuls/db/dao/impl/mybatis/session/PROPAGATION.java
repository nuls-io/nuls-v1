package io.nuls.db.dao.impl.mybatis.session;

/**
 *
 * @author zhouwei
 * @date 2017/10/26
 */
public enum  PROPAGATION {

    REQUIRED("required"),
    INDEPENDENT("independent");


    private PROPAGATION(String value) {
        this.value = value;
    }

    private String value;
}
