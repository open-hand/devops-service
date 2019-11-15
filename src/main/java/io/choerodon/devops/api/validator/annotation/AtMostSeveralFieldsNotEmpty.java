package io.choerodon.devops.api.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import io.choerodon.devops.api.validator.AtMostSeveralFieldsNotEmptyValidator;

/**
 * This annotation is used to check that at most several of the specified fields can be not empty.
 *
 * @author zmf
 * @since 10/15/19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtMostSeveralFieldsNotEmptyValidator.class)
public @interface AtMostSeveralFieldsNotEmpty {
    /**
     * the message code after validation failed.
     */
    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * the set of total fields to be checked
     */
    String[] fields() default {};

    /**
     * the count of not empty fields at most
     */
    int notEmptyFieldCountAtMost() default 1;
}
