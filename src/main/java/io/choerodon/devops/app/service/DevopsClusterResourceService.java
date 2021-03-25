package io.choerodon.devops.app.service;

import java.util.List;
import javax.annotation.Nullable;

import io.choerodon.devops.api.vo.ClusterResourceVO;
import io.choerodon.devops.api.vo.DevopsPrometheusVO;
import io.choerodon.devops.api.vo.PrometheusStageVO;
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
     * 创建cert-manager
     */
    void createCertManager(Long projectId, Long clusterId);

    /**
     * 修改cert-manager的状态
     */
    void updateCertMangerStatus(Long clusterId, String status, String error);

    /**
     * 查询集群可能存在的CertManager的版本
     *
     * @param clusterId 集群id
     * @return CertManager版本，如果没有，返回null
     */
    @Nullable
    String queryCertManagerVersion(Long clusterId);

    Boolean deleteCertManager(Long projectId, Long clusterId);

    DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type);

    Boolean uninstallPrometheus(Long projectId, Long clusterId);

    List<ClusterResourceVO> listClusterResource(Long clusterId, Long projectId);

    /**
     * 验证cert-manager 管理的证书是否存在启用或者操作状态的
     */
    Boolean checkCertManager(Long clusterId);

    Boolean createPrometheus(Long projectId, Long clusterId, DevopsPrometheusVO prometheusVo);

    Boolean updatePrometheus(Long projectId, Long clusterId, DevopsPrometheusVO prometheusVo);

    /**
     * 查询集群下的prometheus，返回vo对象
     */
    DevopsPrometheusVO queryPrometheus(Long clusterId);

    /**
     * 查询集群下的prometheus，返回DTO对象
     */
    DevopsPrometheusDTO baseQueryPrometheusDTO(Long clusterId);

    /**
     * 查询安装prometheus的进程
     */
    PrometheusStageVO queryDeployStage(Long clusterId);

    /**
     * 查询部署prometheus状态
     */
    ClusterResourceVO queryPrometheusStatus(Long projectId, Long clusterId);

    void unloadCertManager(Long clusterId);

    /**
     * 删除prometheus和对应的集群资源数据
     */
    void baseDeletePrometheus(Long clusterId);

    String getGrafanaUrl(Long projectId, Long clusterId, String type);

    /**
     * 根据集群Id 查询cert-manager
     */
    Boolean queryCertManagerByEnvId(Long envId);

    /**
     * 部署prometheus
     */
    void installPrometheus(Long clusterId, DevopsPrometheusDTO devopsPrometheusDTO);

    /**
     * prometheus的实例异常时进行重试
     *
     * @param instanceId 实例id
     * @param envId      环境id
     */
    void retryPrometheusInstance(Long instanceId, Long envId);

    void retryInstallPrometheus(Long projectId, Long clusterId);

}
