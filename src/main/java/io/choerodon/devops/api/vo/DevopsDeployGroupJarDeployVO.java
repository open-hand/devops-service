package io.choerodon.devops.api.vo;

import io.choerodon.devops.api.vo.deploy.JarDeployVO;

public class DevopsDeployGroupJarDeployVO extends JarDeployVO {
    private String jarFileDownloadUrl;

    public String getJarFileDownloadUrl() {
        return jarFileDownloadUrl;
    }

    public void setJarFileDownloadUrl(String jarFileDownloadUrl) {
        this.jarFileDownloadUrl = jarFileDownloadUrl;
    }
}
