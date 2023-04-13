package com.cdancy.jenkins.rest.domain.job;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/7 16:23
 */
public class AutoValue_PendingInputAction extends PendingInputAction {

    private String id;
    private String proceedText;
    private String message;
    private List<InputParameterDef> inputs;

    public AutoValue_PendingInputAction(String id, String proceedText, String message, List<InputParameterDef> inputs) {
        this.id = id;
        this.proceedText = proceedText;
        this.message = message;
        this.inputs = inputs;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String proceedText() {
        return proceedText;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public List<InputParameterDef> inputs() {
        return inputs;
    }
}
