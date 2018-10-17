package io.choerodon.devops.api.dto;

import java.util.List;

public class DeployVersionDTO {

    private String latestVersion;
    private List<DeployEnvVersionDTO> deployEnvVersionDTO;


    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public List<DeployEnvVersionDTO> getDeployEnvVersionDTO() {
        return deployEnvVersionDTO;
    }

    public void setDeployEnvVersionDTO(List<DeployEnvVersionDTO> deployEnvVersionDTO) {
        this.deployEnvVersionDTO = deployEnvVersionDTO;
    }
}
