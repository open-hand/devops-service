package io.choerodon.devops.app.service;

import javax.annotation.Nullable;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DockerComposeDeployVO;
import io.choerodon.devops.api.vo.host.DevopsDockerInstanceVO;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.DockerComposeValueDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/7 9:56
 */
public interface DockerComposeService {

    DevopsHostCommandDTO deployDockerComposeApp(Long projectId, DockerComposeDeployVO dockerComposeDeployVO);
    DevopsHostCommandDTO updateDockerComposeApp(Long projectId,
                                                Long appId,
                                                @Nullable Long cdJobRecordId,
                                                @Nullable Long pipelineRecordId,
                                                DockerComposeDeployVO dockerComposeDeployVO,
                                                Boolean fromPipeline);

    void restartDockerComposeApp(Long projectId, Long id);

    Page<DevopsDockerInstanceVO> pageContainers(Long projectId, Long id, PageRequest pageable, String name, String param);

    void deleteAppData(Long id);


    void stopContainer(Long projectId, Long id, Long instanceId);

    void startContainer(Long projectId, Long id, Long instanceId);

    void removeContainer(Long projectId, Long id, Long instanceId);

    Page<DockerComposeValueDTO> listValueRecords(Long projectId, Long id, PageRequest pageable, String searchParam);

    void restartContainer(Long projectId, Long id, Long instanceId);
}
