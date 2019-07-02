package io.choerodon.devops.api.dto;

import com.google.gson.internal.LinkedTreeMap;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/2
 */
public class DevopsEnvLabelDTO {

    private String resourceName;

    private LinkedTreeMap labels;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public LinkedTreeMap getLabels() {
        return labels;
    }

    public void setLabels(LinkedTreeMap labels) {
        this.labels = labels;
    }
}
