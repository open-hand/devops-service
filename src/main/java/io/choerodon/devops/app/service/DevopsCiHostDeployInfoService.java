package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.deploy.CustomDeployVO;
import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.infra.dto.DevopsCiHostDeployInfoDTO;

public interface DevopsCiHostDeployInfoService {
    void baseUpdate(DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO);

    DevopsCiHostDeployInfoDTO selectByPrimaryKey(Long configId);

    void updateDockerDeployInfoFromAppCenter(DockerDeployVO dockerDeployVO);

    void updateJarDeployInfoFromAppCenter(JarDeployVO jarDeployVO);

    void updateCustomDeployInfoFromAppCenter(CustomDeployVO customDeployVO);

    void deleteConfigByPipelineId(Long ciPipelineId);
}
