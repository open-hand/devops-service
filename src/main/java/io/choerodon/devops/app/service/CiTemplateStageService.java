package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.infra.dto.CiTemplateStageDTO;

/**
 * 流水线模阶段(CiTemplateStage)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:20
 */
public interface CiTemplateStageService {

    List<CiTemplateStageDTO> listByPipelineTemplateIds(Set<Long> pipelineTemplateIds);
}

