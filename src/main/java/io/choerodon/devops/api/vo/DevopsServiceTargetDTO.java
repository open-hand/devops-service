package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.ServiceInstanceDO;

/**
 * Creator: Runge
 * Date: 2018/8/3
 * Time: 13:39
 * Description:
 */
public class DevopsServiceTargetDTO {
    private List<ServiceInstanceDO> appInstance;
    private Map<String, String> labels;
    private Map<String, List<EndPointPortDTO>> endPoints;

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

    public Map<String, List<EndPointPortDTO>> getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(Map<String, List<EndPointPortDTO>> endPoints) {
        this.endPoints = endPoints;
    }
}
