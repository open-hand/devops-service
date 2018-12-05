package io.choerodon.devops.api.dto;

/**
 * @author zmf
 */
public class InstanceDeploymentDTO {
    /**
     * instance id from the web request
     */
    private Long instanceId;

    /**
     * the detail message with the format as json or yaml
     */
    private Object detail;

    public InstanceDeploymentDTO() {}

    public InstanceDeploymentDTO(Long instanceId, Object detail) {
        this.instanceId = instanceId;
        this.detail = detail;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Object getDetail() {
        return detail;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }
}
