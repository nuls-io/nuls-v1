package io.nuls.db.dao.impl.mybatis.session;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhouwei on 2017/10/26.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SessionAnnotation {
    PROPAGATION value() default PROPAGATION.REQUIRED;
}
