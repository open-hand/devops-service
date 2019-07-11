package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsDeployValueDTO;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:57 2019/4/10
 * Description:
 */
public interface DevopsDeployValueService {
    DevopsDeployValueDTO createOrUpdate(Long projectId, DevopsDeployValueDTO pipelineValueDTO);

    void delete(Long projectId, Long valueId);

    PageInfo<DevopsDeployValueDTO> listByOptions(Long projectId, Long appId, Long envId, PageRequest pageRequest, String params);

    DevopsDeployValueDTO queryById(Long pipelineId, Long valueId);

    void checkName(Long projectId, String name);

    List<DevopsDeployValueDTO> queryByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    Boolean checkDelete(Long projectId, Long valueId);
}
