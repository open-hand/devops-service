package com.cdancy.jenkins.rest.domain.job;

import java.util.List;

import com.google.auto.value.AutoValue;
import org.jclouds.javax.annotation.Nullable;
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


    @SerializedNames({"_class", "parameterDefinitions"})
    public static ParametersDefinitionProperty create(String clazz, List<ParameterDefinition> parameterDefinitions) {
        return new AutoValue_ParametersDefinitionProperty(clazz, parameterDefinitions);
    }

    @Nullable
    public abstract String clazz();

    public abstract List<ParameterDefinition> parameterDefinitions();
}
