package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiChartPublishConfigDTO;
import io.choerodon.devops.infra.dto.CiTplChartPublishConfigDTO;

/**
 * 流水线模板chart发布配置(CiTplChartPublishConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-09 14:37:57
 */
public interface CiTplChartPublishConfigService {

    void baseCreate(CiTplChartPublishConfigDTO ciTplChartPublishConfigDTO);

    void deleteByTemplateStepId(Long templateId);

    CiTplChartPublishConfigDTO queryByStepId(Long templateId);
}

