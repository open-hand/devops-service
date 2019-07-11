package io.choerodon.devops.api.vo;

import java.util.List;

public class DeployEnvVersionDTO {

    private String envName;
    private List<DeployInstanceVersionDTO> deployIntanceVersionDTO;

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public List<DeployInstanceVersionDTO> getDeployIntanceVersionDTO() {
        return deployIntanceVersionDTO;
    }

    public void setDeployIntanceVersionDTO(List<DeployInstanceVersionDTO> deployIntanceVersionDTO) {
        this.deployIntanceVersionDTO = deployIntanceVersionDTO;
    }
}
