package io.choerodon.devops.api.validator;

import java.lang.reflect.Field;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.devops.api.validator.annotation.AtLeastOneNotEmpty;

/**
 * @author lihao
 * @date 2019-09-16 23:54
 */
public class AtLeastOneNotEmptyValidator implements ConstraintValidator<AtLeastOneNotEmpty, Object> {
    private String[] fields;
    private int n;

    @Override
    public void initialize(AtLeastOneNotEmpty atLeastOneNotEmpty) {
        fields = atLeastOneNotEmpty.fields();
        n = atLeastOneNotEmpty.n();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) {
            return false;
        }
        int emptyNumber = 0;
        for (String fieldName : fields) {
            Field field = ReflectionUtils.findField(object.getClass(), fieldName);
            if (field == null) {
                return false;
            }
            field.setAccessible(true);
            Object fieldValue = ReflectionUtils.getField(field, object);
            if (!ObjectUtils.isEmpty(fieldValue)) {
                emptyNumber++;
                if (emptyNumber == n) {
                    return true;
                }
            }
        }
        return false;
    }
}
