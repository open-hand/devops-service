package io.choerodon.devops.api.vo;

import io.choerodon.devops.api.vo.deploy.FileInfoVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;

public class DevopsDeployGroupJarDeployVO extends JarDeployVO {
    private FileInfoVO fileInfoVO;

    @Override
    public FileInfoVO getFileInfoVO() {
        return fileInfoVO;
    }

    @Override
    public void setFileInfoVO(FileInfoVO fileInfoVO) {
        this.fileInfoVO = fileInfoVO;
    }
}
