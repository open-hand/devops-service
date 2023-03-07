package com.cdancy.jenkins.rest.domain.job;

import java.util.Map;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/7 16:21
 */
public class AutoValue_InputParameterDef extends InputParameterDef {

    private String type;
    private String name;
    private String description;
    private Map<String, Object> definition;

    public AutoValue_InputParameterDef(String type, String name, String description, Map<String, Object> definition) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.definition = definition;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public Map<String, Object> definition() {
        return definition;
    }
}
