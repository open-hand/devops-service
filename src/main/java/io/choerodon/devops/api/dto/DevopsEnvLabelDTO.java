package io.choerodon.devops.api.dto;

import com.google.gson.internal.LinkedTreeMap;

import java.util.List;
import java.util.Map;

/**
 * @author lizongwei
 * @date 2019/7/2
 */
public class DevopsEnvLabelDTO {

    private String resourceName;

    private Map labels;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Map getLabels() {
        return labels;
    }

    public void setLabels(Map labels) {
        this.labels = labels;
    }
}
