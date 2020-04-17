package io.choerodon.devops.infra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定序列化为Yaml的字段名
 * 参考 {@link com.fasterxml.jackson.annotation.JsonProperty} 的作用
 *
 * @author zmf
 * @since 20-4-2
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface YamlProperty {
    /**
     * 序列化为yaml的字段名
     */
    String value();
}
