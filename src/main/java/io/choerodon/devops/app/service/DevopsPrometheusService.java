package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.PrometheusVo;

/**
 * @author: 25499
 * @date: 2019/10/28 15:52
 * @description:
 */
public interface DevopsPrometheusService {
    PrometheusVo deploy(Long clusterId,PrometheusVo prometheusVo);

    String queryDeployStatus(Long clusterId,Long prometheusId);

}
