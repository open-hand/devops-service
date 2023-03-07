package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiTemplateDockerDTO;

/**
 * 流水线任务模板与步骤模板关系表(CiTemplateDocker)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:56:48
 */
public interface CiTemplateDockerService {

    CiTemplateDockerDTO queryByStepId(Long stepId);

    void baseCreate(CiTemplateDockerDTO dockerBuildConfig);

    void deleteByTemplateId(Long templateStepId);
}

