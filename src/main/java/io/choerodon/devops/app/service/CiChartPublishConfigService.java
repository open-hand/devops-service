package io.choerodon.devops.app.service;

import java.util.Set;

import io.choerodon.devops.infra.dto.CiChartPublishConfigDTO;

/**
 * 流水线chart发布配置(CiChartPublishConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-04 15:28:30
 */
public interface CiChartPublishConfigService {

    CiChartPublishConfigDTO queryByStepId(Long stepId);

    void baseCreate(CiChartPublishConfigDTO chartPublishConfig);

    void batchDeleteByStepIds(Set<Long> stepIds);
}

