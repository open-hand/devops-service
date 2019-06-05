package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsDeployValueDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:57 2019/4/10
 * Description:
 */
public interface DevopsDeployValueService {
    DevopsDeployValueDTO createOrUpdate(Long projectId, DevopsDeployValueDTO pipelineValueDTO);

    void delete(Long projectId, Long valueId);

    Page<DevopsDeployValueDTO> listByOptions(Long projectId, Long appId, Long envId, PageRequest pageRequest, String params);

    DevopsDeployValueDTO queryById(Long pipelineId, Long valueId);

    void checkName(Long projectId, String name);

    List<DevopsDeployValueDTO> queryByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    Boolean checkDelete(Long projectId, Long valueId);
}
