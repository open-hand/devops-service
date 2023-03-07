package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiTemplateSonarDTO;

/**
 * devops_ci_template_sonar(CiTemplateSonar)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:19
 */
public interface CiTemplateSonarService {

    CiTemplateSonarDTO queryByStepId(Long id);

    void baseCreate(Long templateStepId, CiTemplateSonarDTO ciTemplateSonarDTO);

    void deleteByTemplateStepId(Long templateStepId);
}

