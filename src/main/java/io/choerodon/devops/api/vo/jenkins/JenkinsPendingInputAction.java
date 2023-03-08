package io.choerodon.devops.api.vo.jenkins;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/7 16:29
 */
public class JenkinsPendingInputAction {
    private String id;
    private String proceedText;
    private String message;

    private List<PropertyVO> propertyList;
    private List<JenkinsInputParameterDef> inputs;

    public List<PropertyVO> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<PropertyVO> propertyList) {
        this.propertyList = propertyList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProceedText() {
        return proceedText;
    }

    public void setProceedText(String proceedText) {
        this.proceedText = proceedText;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<JenkinsInputParameterDef> getInputs() {
        return inputs;
    }

    public void setInputs(List<JenkinsInputParameterDef> inputs) {
        this.inputs = inputs;
    }
}
