package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.ClusterResourceVO;
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

    /**
     * 创建或者更新 cert-manager
     * @param clusterId
     * @param status
     * @param error
     */
    void operateCertManager(Long clusterId,String status,String error);

    DevopsClusterResourceDTO queryCertManager(Long clusterId);

    Boolean deleteCertManager(Long clusterId);

    DevopsClusterResourceDTO queryByClusterIdAndConfigId(Long clusterId, Long configId);

    DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type);

    void deletePrometheus(Long clusterId, Long configId);

    List<ClusterResourceVO> listClusterResource(Long clusterId,Long projectId);

    /**
     * 验证cert-manager 管理的证书是否存在启用或者操作状态的
     *
     * @param clusterId
     * @return
     */
    Boolean checkCertManager(Long clusterId);

    PrometheusVo createOrUpdate(Long clusterId, PrometheusVo prometheusVo);

    ClusterResourceVO queryDeployProcess(Long projectId, Long clusterId, Long prometheusId);

    DevopsPrometheusDTO baseQuery(Long prometheusId);

    ClusterResourceVO queryPrometheusStatus(Long projectId,Long clusterId, Long prometheusId);

    void unloadCertManager(Long clusterId);

}
