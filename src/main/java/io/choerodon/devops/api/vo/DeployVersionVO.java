package io.choerodon.devops.api.vo;

import java.util.List;

public class DeployVersionVO {

    private String latestVersion;
    private List<DeployEnvVersionVO> deployEnvVersionVO;


    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public List<DeployEnvVersionVO> getDeployEnvVersionVO() {
        return deployEnvVersionVO;
    }

    public void setDeployEnvVersionVO(List<DeployEnvVersionVO> deployEnvVersionVO) {
        this.deployEnvVersionVO = deployEnvVersionVO;
    }
}
