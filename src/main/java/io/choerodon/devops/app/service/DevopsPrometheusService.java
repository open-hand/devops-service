package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.ClusterConfigVO;
import io.choerodon.devops.api.vo.PrometheusVo;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;

/**
 * @author: 25499
 * @date: 2019/10/28 15:52
 * @description:
 */
public interface DevopsPrometheusService {
    PrometheusVo deploy(Long clusterId, PrometheusVo prometheusVo);

    ClusterConfigVO queryDeployProess(Long projectId, Long clusterId, Long prometheusId);

    void delete(Long prometheusId,Long clusterId);

    DevopsPrometheusDTO baseQuery(Long prometheusId);

    ClusterConfigVO queryPrometheusStatus(Long clusterId, Long prometheusId);
}
