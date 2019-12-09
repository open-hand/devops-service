package io.choerodon.devops.api.validator;

import io.choerodon.devops.api.validator.annotation.QuantityCheck;
import io.choerodon.devops.infra.enums.ResourceUnitLevelEnum;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * PV和PVC的容量参数格式校验
 */
public class QuantityCheckValidator implements ConstraintValidator<QuantityCheck, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            if (value.length() == 0) {
                return false;
            }
            long size = Long.parseLong(value.substring(0, value.length() - 2));
            String unit = value.substring(value.length() - 2);
            if (size <= 0 || !ResourceUnitLevelEnum.checkExist(unit.toUpperCase())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
