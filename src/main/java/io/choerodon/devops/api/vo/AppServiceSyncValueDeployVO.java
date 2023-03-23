package io.choerodon.devops.api.vo;

import java.util.List;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author scp
 * @date 2023-03-22 12:52
 */
public class AppServiceSyncValueDeployVO {
    @Encrypt
    @ApiModelProperty("环境id/必填")
    @NotNull(message = "{devops.env.id.null}")
    private Long environmentId;

    @Encrypt
    @ApiModelProperty("实例id/必填")
    @NotNull(message = "{devops.app.instance.id.null}")
    private List<Long> instanceIds;

    @Encrypt
    @ApiModelProperty("部署配置id/必填")
    @NotNull(message = "{devops.app.value.id.null}")
    private Long valueId;

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

    public List<Long> getInstanceIds() {
        return instanceIds;
    }

    public void setInstanceIds(List<Long> instanceIds) {
        this.instanceIds = instanceIds;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }
}
