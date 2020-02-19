package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.polaris.PolarisResponsePayloadVO;
import io.choerodon.devops.infra.dto.DevopsPolarisRecordDTO;

/**
 * @author zmf
 * @since 2/17/20
 */
public interface PolarisScanningService {
    /**
     * 查询扫描纪录
     *
     * @param projectId 项目id
     * @param scope     扫描的范围 env/cluster
     * @param scopeId   对应scope的envId或者clusterId
     * @return 扫描纪录
     */
    DevopsPolarisRecordRespVO queryRecordByScopeAndScopeId(Long projectId, String scope, Long scopeId);

    /**
     * 获取扫描的环境报告
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return 扫描报告
     */
    List<InstanceWithPolarisResultVO> queryEnvPolarisResult(Long projectId, Long envId);

    /**
     * 扫描环境
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return 扫描纪录
     */
    DevopsPolarisRecordVO scanEnv(Long projectId, Long envId);

    /**
     * 扫描集群
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @return 扫描纪录
     */
    DevopsPolarisRecordVO scanCluster(Long projectId, Long clusterId);

    /**
     * 获取扫描的集群概览报告
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @return 报告
     */
    DevopsPolarisSummaryVO clusterPolarisSummary(Long projectId, Long clusterId);

    /**
     * 获取扫描的集群环境详情报告
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @return 报告
     */
    List<DevopsEnvWithPolarisResultVO> clusterPolarisEnvDetail(Long projectId, Long clusterId);

    DevopsPolarisRecordDTO queryRecordByScopeIdAndScope(Long scopeId, String scope);

    /**
     * 处理来自agent的polaris扫描结果消息
     *
     * @param message polaris扫描结果
     */
    void handleAgentPolarisMessage(PolarisResponsePayloadVO message);

    /**
     * 检查纪录是否超时，如果超时，更新状态为超时
     *
     * @param recordId 纪录id
     * @return true表示超时且更新了纪录
     */
    boolean checkTimeout(Long recordId);

    /**
     * 清楚和recordId相关的数据（不包含record本身，是删除其他表的数据）
     *
     * @param recordId 扫描记录id
     */
    void deleteAssociatedData(Long recordId);
}
