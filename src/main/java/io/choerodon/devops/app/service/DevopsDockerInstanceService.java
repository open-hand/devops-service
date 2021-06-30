package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 14:13
 */
public interface DevopsDockerInstanceService {
    /**
     * 部署docker进程
     * @param projectId
     * @param dockerDeployVO
     */
    void deployDockerInstance(Long projectId, DockerDeployVO dockerDeployVO);


    DevopsDockerInstanceDTO baseQuery(Long instanceId);

    void baseUpdate(DevopsDockerInstanceDTO devopsDockerInstanceDTO);

    void baseDelete(Long instanceId);
}
