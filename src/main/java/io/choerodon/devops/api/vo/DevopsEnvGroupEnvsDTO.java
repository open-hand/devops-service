package io.choerodon.devops.api.vo;

import java.util.List;

public class DevopsEnvGroupEnvsDTO {
    private Long devopsEnvGroupId;
    private String devopsEnvGroupName;
    private List<DevopsEnviromentRepDTO> devopsEnviromentRepDTOs;

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

    public List<DevopsEnviromentRepDTO> getDevopsEnviromentRepDTOs() {
        return devopsEnviromentRepDTOs;
    }

    public void setDevopsEnviromentRepDTOs(List<DevopsEnviromentRepDTO> devopsEnviromentRepDTOs) {
        this.devopsEnviromentRepDTOs = devopsEnviromentRepDTOs;
    }
}
