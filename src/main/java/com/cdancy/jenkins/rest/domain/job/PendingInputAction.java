package com.cdancy.jenkins.rest.domain.job;

import java.util.List;

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
public abstract class PendingInputAction {

    @SerializedNames({"id", "proceedText", "message", "inputs"})
    public static PendingInputAction create(String id,
                                            String proceedText,
                                            String message,
                                            List<InputParameterDef> inputs) {
        return new AutoValue_PendingInputAction(id, proceedText, message, inputs);
    }

    public abstract String id();

    public abstract String proceedText();

    public abstract String message();

    public abstract List<InputParameterDef> inputs();

}
