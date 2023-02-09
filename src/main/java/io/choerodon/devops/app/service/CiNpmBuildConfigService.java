package io.choerodon.devops.app.service;

import java.util.Set;

import io.choerodon.devops.infra.dto.CiNpmBuildConfigDTO;

/**
 * 流水线npm构建配置(CiNpmBuildConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-11 10:42:10
 */
public interface CiNpmBuildConfigService {

    void baseCreate(CiNpmBuildConfigDTO npmBuildConfig);

    CiNpmBuildConfigDTO queryByStepId(Long stepId);

    void batchDeleteByStepIds(Set<Long> stepIds);
}

