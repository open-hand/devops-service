package com.cdancy.jenkins.rest.domain.job;


import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/6 16:24
 */
public class AutoValue_ParametersDefinitionProperty extends ParametersDefinitionProperty {

    public final String clazz;
    public final List<ParameterDefinition> parameterDefinitions;


    public AutoValue_ParametersDefinitionProperty(String clazz, List<ParameterDefinition> parameterDefinitions) {
        this.clazz = clazz;
        this.parameterDefinitions = parameterDefinitions;
    }

    @Override
    public String clazz() {
        return clazz;
    }

    @Override
    public List<ParameterDefinition> parameterDefinitions() {

        return parameterDefinitions;
    }
}
