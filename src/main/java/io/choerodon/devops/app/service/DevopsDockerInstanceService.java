package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.host.DockerProcessUpdatePayload;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;

import java.util.List;

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
     *
     * @param projectId
     * @param dockerDeployVO
     */
    void deployDockerInstance(Long projectId, DockerDeployVO dockerDeployVO);


    DevopsDockerInstanceDTO baseQuery(Long instanceId);

    DevopsDockerInstanceDTO queryByAppIdAndContainerId(Long appId, String containerId);

    void baseUpdate(DevopsDockerInstanceDTO devopsDockerInstanceDTO);

    void baseDelete(Long instanceId);


    List<DevopsDockerInstanceDTO> listByHostId(Long hostId);

    List<DevopsDockerInstanceDTO> listByAppId(Long appId);

    DevopsDockerInstanceDTO queryByHostIdAndName(Long hostId, String containerName);

    void deleteByAppId(Long appId);

    void baseCreate(DevopsDockerInstanceDTO devopsDockerInstanceDTO);

    void createOrUpdate(String hostId, DockerProcessUpdatePayload processPayload);

}
