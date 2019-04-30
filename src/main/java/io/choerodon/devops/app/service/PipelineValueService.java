package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.PipelineValueDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:57 2019/4/10
 * Description:
 */
public interface PipelineValueService {
    PipelineValueDTO createOrUpdate(Long projectId, PipelineValueDTO pipelineValueDTO);

    void delete(Long projectId, Long valueId);

    Page<PipelineValueDTO> listByOptions(Long projectId, Long appId, Long envId, PageRequest pageRequest, String params);

    PipelineValueDTO queryById(Long pipelineId, Long valueId);

    void checkName(Long projectId, String name);

    List<PipelineValueDTO> queryByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    Boolean checkDelete(Long projectId, Long valueId);
}
