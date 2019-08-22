package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class DevopsEnvGroupEnvsVO {
    @ApiModelProperty("环境组id / 为空则表示是默认分组")
    private Long devopsEnvGroupId;

    @ApiModelProperty("环境组名")
    private String devopsEnvGroupName;

    @ApiModelProperty("环境组内的环境")
    private List<DevopsEnviromentRepVO> devopsEnviromentRepDTOs;

    @ApiModelProperty("组内的环境是否启用")
    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public List<DevopsEnviromentRepVO> getDevopsEnviromentRepDTOs() {
        return devopsEnviromentRepDTOs;
    }

    public void setDevopsEnviromentRepDTOs(List<DevopsEnviromentRepVO> devopsEnviromentRepDTOs) {
        this.devopsEnviromentRepDTOs = devopsEnviromentRepDTOs;
    }
}
