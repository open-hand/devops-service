package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiTplNpmBuildConfigDTO;

/**
 * 流水线模板npm发布配置(CiTplNpmBuildConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-11 10:42:25
 */
public interface CiTplNpmBuildConfigService {

    void baseCreate(CiTplNpmBuildConfigDTO ciTplNpmBuildConfigDTO);

    void deleteByTemplateStepId(Long templateStepId);

    CiTplNpmBuildConfigDTO queryByStepId(Long stepId);
}

