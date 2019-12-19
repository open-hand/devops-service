package io.choerodon.devops.api.validator;

import io.choerodon.devops.api.validator.annotation.VolumeTypeCheck;
import io.choerodon.devops.infra.enums.VolumeTypeEnum;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class VolumeTypeCheckValidator implements ConstraintValidator<VolumeTypeCheck, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            if (value.length() == 0) {
                return false;
            }
            if (!VolumeTypeEnum.checkExist(value)) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
