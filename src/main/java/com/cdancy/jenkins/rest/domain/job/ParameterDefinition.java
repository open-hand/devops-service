package com.cdancy.jenkins.rest.domain.job;

import java.util.Map;

import com.google.auto.value.AutoValue;
import org.jclouds.json.SerializedNames;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/6 16:20
 */
@AutoValue
public abstract class ParameterDefinition {
    @SerializedNames({"name", "defaultParameterValue"})
    public static ParameterDefinition create(String name,
                                             Map<String, String> defaultParameterValue) {
        return new AutoValue_ParameterDefinition(name, defaultParameterValue);
    }

    public abstract String name();

    public abstract Map<String, String> defaultParameterValue();
}
