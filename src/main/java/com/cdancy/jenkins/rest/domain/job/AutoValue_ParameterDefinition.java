package com.cdancy.jenkins.rest.domain.job;

import java.util.Map;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/6 16:29
 */
public class AutoValue_ParameterDefinition extends ParameterDefinition {

    public String name;
    public Map<String, String> defaultParameterValue;


    public AutoValue_ParameterDefinition(String name,
                                         Map<String, String> defaultParameterValue) {
        this.name = name;
        this.defaultParameterValue = defaultParameterValue;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Map<String, String> defaultParameterValue() {
        return defaultParameterValue;
    }
}
