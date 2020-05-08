package io.choerodon.devops.infra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 只能用在Map(及其子类)类型的字段上,否则无法生效，其属性被展开到包含他这个字段的对象一级  (2020年04月03日)
 * (展开的顺序按照Map的遍历顺序)
 * 参考 {@link com.fasterxml.jackson.annotation.JsonUnwrapped}
 *
 * @author zmf
 * @since 20-4-2
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface YamlUnwrapped {
}
