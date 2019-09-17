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
 * @date 2019-09-16 23:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Constraint(validatedBy = AtLeastOneNotEmptyValidator.class)
public @interface AtLeastOneNotEmpty {
    String message() default "error.atleast.one.not.empty";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] fields() default {};
}
