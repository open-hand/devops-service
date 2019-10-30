package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.ClusterConfigVO;
import io.choerodon.devops.api.vo.PrometheusVo;
import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
public interface DevopsClusterResourceService {
    void baseCreate(DevopsClusterResourceDTO devopsClusterResourceDTO);

    void baseUpdate(DevopsClusterResourceDTO devopsClusterResourceDTO);

    void operateCertManager(DevopsClusterResourceDTO devopsClusterResourceDTO, Long clusterId);

    DevopsClusterResourceDTO queryCertManager(Long clusterId);

    Boolean deleteCertManager(Long clusterId);

    DevopsClusterResourceDTO queryByClusterIdAndConfigId(Long clusterId, Long configId);

    DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type);

    void delete(Long clusterId, Long configId);

    List<DevopsClusterResourceDTO> listClusterResource(Long clusterId);

    Boolean checkCertManager(Long clusterId);

    PrometheusVo deploy(Long clusterId, PrometheusVo prometheusVo);

    ClusterConfigVO queryDeployProess(Long projectId, Long clusterId, Long prometheusId);

    DevopsPrometheusDTO baseQuery(Long prometheusId);

    ClusterConfigVO queryPrometheusStatus(Long clusterId, Long prometheusId);
}
