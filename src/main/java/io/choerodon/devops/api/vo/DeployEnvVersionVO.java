package io.choerodon.devops.api.vo;

import java.util.List;

public class DeployEnvVersionVO {

    private String envName;
    private List<DeployInstanceVersionVO> deployIntanceVersionDTO;

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public List<DeployInstanceVersionVO> getDeployIntanceVersionDTO() {
        return deployIntanceVersionDTO;
    }

    public void setDeployIntanceVersionDTO(List<DeployInstanceVersionVO> deployIntanceVersionDTO) {
        this.deployIntanceVersionDTO = deployIntanceVersionDTO;
    }
}
