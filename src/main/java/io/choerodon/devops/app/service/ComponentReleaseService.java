package io.choerodon.devops.app.service;

import java.util.Map;

import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;

/**
 * 为集群组件创建对应的Release
 *
 * @author zmf
 * @since 10/29/19
 */
public interface ComponentReleaseService {
    /**
     * 为Prometheus组件创建相应的release
     *
     * @param values 此组件配置的键值对
     * @return 组件对应的实例纪录
     */
    AppServiceInstanceDTO createReleaseForPrometheus(Map<String, String> values);
}
