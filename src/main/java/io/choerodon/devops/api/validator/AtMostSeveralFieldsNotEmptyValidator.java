package io.choerodon.devops.api.validator;

import java.lang.reflect.Field;
import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import io.choerodon.devops.api.validator.annotation.AtMostSeveralFieldsNotEmpty;

/**
 * This validator is used to check that at most several specified fields can be not empty.
 *
 * @author zmf
 * @since 10/15/19
 */
public class AtMostSeveralFieldsNotEmptyValidator implements ConstraintValidator<AtMostSeveralFieldsNotEmpty, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtMostSeveralFieldsNotEmptyValidator.class);
    private String[] fields;
    private int maxCount = 1;

    @Override
    public void initialize(AtMostSeveralFieldsNotEmpty constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
        this.maxCount = constraintAnnotation.notEmptyFieldCountAtMost();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        int notNullFieldCount = 0;
        for (String fieldName : fields) {
            Field field = Objects.requireNonNull(ReflectionUtils.findField(value.getClass(), fieldName));
            boolean preAccessible = field.isAccessible();
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(value);
                if (!ObjectUtils.isEmpty(fieldValue)) {
                    if (++notNullFieldCount > maxCount) {
                        return false;
                    }
                }
            } catch (IllegalAccessException e) {
                LOGGER.warn("Unexpected IllegalAccessException occurred when validating the object annotated by @AtMostSeveralFieldsNotEmpty. ");
                return false;
            } finally {
                field.setAccessible(preAccessible);
            }
        }

        return true;
    }
}
