package io.choerodon.devops.api.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.choerodon.devops.api.validator.annotation.EnumCheck;

/**
 * @author zmf
 * @since 2020/9/15
 */
public class EnumCheckValidator implements ConstraintValidator<EnumCheck, Object> {
    private EnumCheck enumCheck;

    @Override
    public void initialize(EnumCheck constraintAnnotation) {
        // 初始化数据, 存储这个字段的注解数据
        this.enumCheck = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 如果字段值为空, 根据skipNull值返回结果
        if (value == null) {
            return enumCheck.skipNull();
        }

        Class<? extends Enum<?>> enumClass = enumCheck.enumClass();
        // 获取枚举类
        Enum<?>[] enums = enumClass.getEnumConstants();

        // 匹配枚举值
        for (Enum<?> constant : enums) {
            if (constant.name().equalsIgnoreCase(value.toString())) {
                return true;
            }
        }

        return false;
    }
}
