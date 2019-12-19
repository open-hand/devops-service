package io.choerodon.devops.api.validator.annotation;

import io.choerodon.devops.api.validator.VolumeTypeCheckValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 卷类型检查
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = VolumeTypeCheckValidator.class)
public @interface VolumeTypeCheck {
    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
