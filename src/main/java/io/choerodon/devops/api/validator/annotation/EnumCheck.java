package io.choerodon.devops.api.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import io.choerodon.devops.api.validator.EnumCheckValidator;

/**
 * 用于校验属性属于枚举值
 *
 * @author zmf
 * @since 2020/9/15
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumCheckValidator.class)
public @interface EnumCheck {
    /**
     * the message code after validation failed.
     */
    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 枚举类
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * 跳过空值, 为空则不校验
     */
    boolean skipNull() default false;
}
