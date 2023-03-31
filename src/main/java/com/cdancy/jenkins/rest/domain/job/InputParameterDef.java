package com.cdancy.jenkins.rest.domain.job;

import java.util.Map;

import com.google.auto.value.AutoValue;
import org.jclouds.json.SerializedNames;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/7 16:19
 */
@AutoValue
public abstract class InputParameterDef {
    InputParameterDef() {
    }

    @SerializedNames({"type", "name", "description", "definition"})
    public static InputParameterDef create(String type,
                                           String name,
                                           String description,
                                           Map<String, Object> definition) {
        return new AutoValue_InputParameterDef(type,
                name,
                description,
                definition);
    }

    public abstract String type();

    public abstract String name();

    public abstract String description();

    public abstract Map<String, Object> definition();
}
