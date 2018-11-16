package io.choerodon.devops.api.dto;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dataobject.ServiceInstanceDO;

/**
 * Creator: Runge
 * Date: 2018/8/3
 * Time: 13:39
 * Description:
 */
public class DevopsServiceTargetDTO {
    private List<ServiceInstanceDO> appInstance;
    private Map<String, String> labels;

    public List<ServiceInstanceDO> getAppInstance() {
        return appInstance;
    }

    public void setAppInstance(List<ServiceInstanceDO> appInstance) {
        this.appInstance = appInstance;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
