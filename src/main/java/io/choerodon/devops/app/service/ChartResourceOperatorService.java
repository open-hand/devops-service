package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.enums.ResourceType;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/9 14:03
 */
public interface ChartResourceOperatorService {

    void saveOrUpdateChartResource(String detailsJson, AppServiceInstanceDTO appServiceInstanceDTO);

    void deleteByEnvIdAndName(Long envId, String name);

    ResourceType getType();
}
