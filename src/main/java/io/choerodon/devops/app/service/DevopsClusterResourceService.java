package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.ClusterResourceVO;
import io.choerodon.devops.api.vo.DevopsPrometheusVO;
import io.choerodon.devops.api.vo.DevopsPvcReqVO;
import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;
import org.springframework.util.MultiValueMap;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
public interface DevopsClusterResourceService {
    void baseCreate(DevopsClusterResourceDTO devopsClusterResourceDTO);

    void baseUpdate(DevopsClusterResourceDTO devopsClusterResourceDTO);

    /**
     * 创建cert-manager
     *
     * @param clusterId
     */
    void createCertManager(Long clusterId);

    /**
     * 修改cert-manager的状态
     *
     * @param clusterId
     * @param status
     * @param error
     */
    void updateCertMangerStatus(Long clusterId, String status, String error);

    Boolean deleteCertManager(Long clusterId);

    DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type);

    void uploadPrometheus(Long clusterId);

    List<ClusterResourceVO> listClusterResource(Long clusterId, Long projectId);

    /**
     * 验证cert-manager 管理的证书是否存在启用或者操作状态的
     *
     * @param clusterId
     * @return
     */
    Boolean checkCertManager(Long clusterId);

    void createPromteheus(Long projectId,Long clusterId, DevopsPrometheusVO prometheusVo);

    void updatePromteheus(Long projectId,Long clusterId, DevopsPrometheusVO prometheusVo);

    DevopsPrometheusVO queryPrometheus(Long clusterId);

    ClusterResourceVO queryDeployProcess(Long clusterId);

    ClusterResourceVO queryPrometheusStatus(Long projectId, Long clusterId);

    void unloadCertManager(Long clusterId);

    void basedeletePromtheus(Long clusterId);

    String getGrafanaUrl(Long projectId, Long clusterId, String type);

    /**
     * 根据集群Id 查询cert-manager
     *
     * @param envId
     * @return
     */
    Boolean queryCertManagerByEnvId(Long envId);

     void deployPrometheus(Long clusterId, DevopsPvcReqVO pvc);
}
