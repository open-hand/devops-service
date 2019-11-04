package io.choerodon.devops.api.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import io.choerodon.devops.api.validator.AtLeastOneNotEmptyValidator;

/**
 * @author lihao
 * 注解在类上，验证指定的属性中，至少有n个不为empty
 * @date 2019-09-16 23:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Constraint(validatedBy = AtLeastOneNotEmptyValidator.class)
public @interface AtLeastOneNotEmpty {
    String message() ;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] fields() default {};

    /**
     * n 表示不为empty的元素的个数
     * @return
     */
    int n() default 1;
}
