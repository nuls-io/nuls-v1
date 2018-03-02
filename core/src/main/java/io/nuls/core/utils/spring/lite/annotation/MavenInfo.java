package io.nuls.core.utils.spring.lite.annotation;

import java.lang.annotation.*;

/**
 * @author: Niels Wang
 * @date: 2018/3/1
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MavenInfo {
    /**
     * the type of the Annotation on a method
     * @return
     */
    String value() default "";
}
