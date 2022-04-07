package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DockerComposeDeployVO;
import io.choerodon.devops.api.vo.host.DevopsDockerInstanceVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/7 9:56
 */
public interface DockerComposeService {

    void deployDockerComposeApp(Long projectId, DockerComposeDeployVO dockerComposeDeployVO);

    void updateDockerComposeApp(Long projectId, Long id, DockerComposeDeployVO dockerComposeDeployVO);

    void restartDockerComposeApp(Long projectId, Long id);

    Page<DevopsDockerInstanceVO> pageContainers(Long projectId, Long id);

    void deleteAppData(Long id);
}
