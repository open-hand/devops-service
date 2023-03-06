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


    public final List<ParameterDefinition> parameterDefinitions;


    public AutoValue_ParametersDefinitionProperty(List<ParameterDefinition> parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

    @Override
    public List<ParameterDefinition> parameterDefinitions() {

        return parameterDefinitions;
    }
}
