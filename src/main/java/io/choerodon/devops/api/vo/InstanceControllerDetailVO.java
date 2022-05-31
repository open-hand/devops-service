package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zmf
 */
public class InstanceControllerDetailVO {
    /**
     * instance id from the web request
     */
    @Encrypt
    @ApiModelProperty("实例id")
    private Long instanceId;

    /**
     * the detail message with the format as json or yaml
     */
    @ApiModelProperty("资源详情")
    private Object detail;

    public InstanceControllerDetailVO() {}

    public InstanceControllerDetailVO(Long instanceId, Object detail) {
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
