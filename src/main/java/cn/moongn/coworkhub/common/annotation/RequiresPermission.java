package cn.moongn.coworkhub.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限控制注解
 * 可标注在类或方法上，用于指定访问所需的权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    /**
     * 权限代码
     */
    String value();

    /**
     * 权限校验逻辑，默认为AND
     */
    Logical logical() default Logical.AND;

    /**
     * 权限校验逻辑枚举
     */
    enum Logical {
        AND, OR
    }
}