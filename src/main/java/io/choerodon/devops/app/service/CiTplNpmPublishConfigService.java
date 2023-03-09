package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiTplNpmPublishConfigDTO;

/**
 * 流水线模板npm发布配置(CiTplNpmPublishConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-09 17:46:47
 */
public interface CiTplNpmPublishConfigService {

    void baseCreate(CiTplNpmPublishConfigDTO ciTplNpmPublishConfigDTO);

    void deleteByTemplateStepId(Long templateStepId);

    CiTplNpmPublishConfigDTO queryByStepId(Long stepId);
}

