package io.choerodon.devops.app.service;

import java.util.Set;

import io.choerodon.devops.infra.dto.CiNpmPublishConfigDTO;

/**
 * 流水线npm发布配置(CiNpmPublishConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-09 15:26:47
 */
public interface CiNpmPublishConfigService {

    void baseCreate(CiNpmPublishConfigDTO npmPublishConfig);

    CiNpmPublishConfigDTO queryByStepId(Long stepId);

    void batchDeleteByStepIds(Set<Long> stepIds);
}

