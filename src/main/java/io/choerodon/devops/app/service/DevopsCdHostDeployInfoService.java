package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.deploy.CustomDeployVO;
import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.infra.dto.DevopsCdHostDeployInfoDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/14 10:02
 */
public interface DevopsCdHostDeployInfoService {

    DevopsCdHostDeployInfoDTO baseCreate(DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO);


    DevopsCdHostDeployInfoDTO queryById(Long id);

    void baseUpdate(DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO);

    void updateDockerDeployInfoFromAppCenter(DockerDeployVO dockerDeployVO);

    void updateJarDeployInfoFromAppCenter(JarDeployVO jarDeployVO);

    void updateCustomDeployInfoFromAppCenter(CustomDeployVO customDeployVO);
}
