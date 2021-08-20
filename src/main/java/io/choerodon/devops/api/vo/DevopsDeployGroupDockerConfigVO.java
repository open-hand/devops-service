package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.deploy.DockerDeployVO;

public class DevopsDeployGroupDockerConfigVO extends DockerDeployVO {
    @Encrypt
    private Long appServiceVersionId;

    @Encrypt
    private Long appServiceId;

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }
}
