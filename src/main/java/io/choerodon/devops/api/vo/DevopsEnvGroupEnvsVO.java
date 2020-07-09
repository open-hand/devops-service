package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsEnvGroupEnvsVO {
    @ApiModelProperty("环境组id / 为空则表示是默认分组")
    @Encrypt
    private Long devopsEnvGroupId;

    @ApiModelProperty("环境组名")
    private String devopsEnvGroupName;

    @ApiModelProperty("环境组内的环境")
    private List<DevopsEnvironmentRepVO> devopsEnvironmentRepDTOs;

    public DevopsEnvGroupEnvsVO() {
    }


    public Long getDevopsEnvGroupId() {
        return devopsEnvGroupId;
    }

    public void setDevopsEnvGroupId(Long devopsEnvGroupId) {
        this.devopsEnvGroupId = devopsEnvGroupId;
    }

    public String getDevopsEnvGroupName() {
        return devopsEnvGroupName;
    }

    public void setDevopsEnvGroupName(String devopsEnvGroupName) {
        this.devopsEnvGroupName = devopsEnvGroupName;
    }

    public List<DevopsEnvironmentRepVO> getDevopsEnvironmentRepDTOs() {
        return devopsEnvironmentRepDTOs;
    }

    public void setDevopsEnvironmentRepDTOs(List<DevopsEnvironmentRepVO> devopsEnvironmentRepDTOs) {
        this.devopsEnvironmentRepDTOs = devopsEnvironmentRepDTOs;
    }
}
