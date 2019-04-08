package io.choerodon.devops.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.PipelineAppDeployDTO;
import io.choerodon.devops.app.service.PipelineTaskService;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployE;
import io.choerodon.devops.domain.application.entity.PipelineTaskE;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployRepository;
import io.choerodon.devops.domain.application.repository.PipelineTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:25 2019/4/4
 * Description:
 */
public class PipelineTaskServiceImpl implements PipelineTaskService {
    @Autowired
    private PipelineTaskRepository pipelineTaskRepository;
    @Autowired
    private PipelineAppDeployRepository pipelineAppDeployRepository;

    @Override
    public PipelineAppDeployDTO createAppDeploy(Long projectId, PipelineAppDeployDTO appDeployDTO) {
        PipelineAppDeployE pipelineAppDeployE = ConvertHelper.convert(appDeployDTO, PipelineAppDeployE.class);
        pipelineAppDeployE = pipelineAppDeployRepository.create(pipelineAppDeployE);
        PipelineTaskE pipelineTaskE = ConvertHelper.convert(appDeployDTO, PipelineTaskE.class);
        pipelineTaskE.setAppDeployId(pipelineAppDeployE.getId());
        pipelineTaskRepository.create(pipelineTaskE);
        return appDeployDTO;
    }
}
