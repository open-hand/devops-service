package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.ClusterResourceVO;
import io.choerodon.devops.api.vo.DevopsPrometheusVO;
import io.choerodon.devops.api.vo.PrometheusStageVO;
import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;

import java.util.List;

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
    void createCertManager(Long projectId, Long clusterId);

    /**
     * 修改cert-manager的状态
     *
     * @param clusterId
     * @param status
     * @param error
     */
    void updateCertMangerStatus(Long clusterId, String status, String error);

    Boolean deleteCertManager(Long projectId, Long clusterId);

    DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type);

    Boolean uninstallPrometheus(Long projectId, Long clusterId);

    List<ClusterResourceVO> listClusterResource(Long clusterId, Long projectId);

    /**
     * 验证cert-manager 管理的证书是否存在启用或者操作状态的
     *
     * @param clusterId
     * @return
     */
    Boolean checkCertManager(Long clusterId);

    Boolean createPrometheus(Long projectId, Long clusterId, DevopsPrometheusVO prometheusVo);

    Boolean updatePrometheus(Long projectId, Long clusterId, DevopsPrometheusVO prometheusVo);

    /**
     * 查询集群下的prometheus，返回vo对象
     *
     * @param clusterId
     * @return
     */
    DevopsPrometheusVO queryPrometheus(Long clusterId);

    /**
     * 查询集群下的prometheus，返回DTO对象
     *
     * @param clusterId
     * @return
     */
    DevopsPrometheusDTO baseQueryPrometheusDTO(Long clusterId);

    /**
     * 查询安装prometheus的进程
     *
     * @param clusterId
     * @return
     */
    PrometheusStageVO queryDeployStage(Long clusterId);

    /**
     * 查询部署prometheus状态
     *
     * @param projectId
     * @param clusterId
     * @return
     */
    ClusterResourceVO queryPrometheusStatus(Long projectId, Long clusterId);

    void unloadCertManager(Long clusterId);

    /**
     * 删除prometheus和对应的集群资源数据
     *
     * @param clusterId
     */
    void baseDeletePrometheus(Long clusterId);

    String getGrafanaUrl(Long projectId, Long clusterId, String type);

    /**
     * 根据集群Id 查询cert-manager
     *
     * @param envId
     * @return
     */
    Boolean queryCertManagerByEnvId(Long envId);

    /**
     * 部署prometheus
     *
     * @param clusterId
     * @param devopsPrometheusDTO
     */
    void installPrometheus(Long clusterId, DevopsPrometheusDTO devopsPrometheusDTO);

    /**
     * prometheus的实例异常时进行重试
     *
     * @param instanceId 实例id
     * @param envId      环境id
     * @param type       prometheus类型
     */
    void retryPrometheusInstance(Long instanceId, Long envId, String type);

    void retryInstallPrometheus(Long projectId, Long clusterId);

}
