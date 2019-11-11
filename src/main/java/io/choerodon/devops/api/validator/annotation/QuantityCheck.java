package io.choerodon.devops.api.validator.annotation;

import io.choerodon.devops.api.validator.QuantityCheckValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PV和PVC的容量参数格式校验
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = QuantityCheckValidator.class)
public @interface QuantityCheck {
    String message() ;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] fields() default {};
}
