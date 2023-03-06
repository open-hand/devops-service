package com.cdancy.jenkins.rest.domain.job;

import java.util.List;

import com.google.auto.value.AutoValue;
import org.jclouds.json.SerializedNames;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/6 16:17
 */
@AutoValue
public abstract class ParametersDefinitionProperty {
    @SerializedNames({"parameterDefinitions"})
    public static ParametersDefinitionProperty create(List<ParameterDefinition> parameterDefinitions) {
        return new AutoValue_ParametersDefinitionProperty(parameterDefinitions);
    }

    public abstract List<ParameterDefinition> parameterDefinitions();
}
