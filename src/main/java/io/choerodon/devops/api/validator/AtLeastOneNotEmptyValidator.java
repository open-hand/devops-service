package io.choerodon.devops.api.validator;

import java.lang.reflect.Field;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.devops.api.validator.annotation.AtLeastOneNotEmpty;

/**
 * @author lihao
 * @date 2019-09-16 23:54
 */
public class AtLeastOneNotEmptyValidator implements ConstraintValidator<AtLeastOneNotEmpty, Object> {
    private String[] fields;

    @Override
    public void initialize(AtLeastOneNotEmpty atLeastOneNotEmpty) {
        fields = atLeastOneNotEmpty.fields();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) {
            return false;
        }
        for (String fieldName : fields) {
            Field field = ReflectionUtils.findField(object.getClass(), fieldName);
            assert field != null;
            field.setAccessible(true);
            Object fieldValue = ReflectionUtils.getField(field, object);
            if (!StringUtils.isEmpty(fieldValue)) {
                return true;
            }
        }
        return false;
    }
}
