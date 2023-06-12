package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.ClusterPolarisEnvDetailsVO;
import io.choerodon.devops.api.vo.DevopsPolarisRecordRespVO;
import io.choerodon.devops.api.vo.DevopsPolarisRecordVO;
import io.choerodon.devops.api.vo.DevopsPolarisSummaryVO;
import io.choerodon.devops.api.vo.polaris.PolarisResponsePayloadVO;
import io.choerodon.devops.infra.dto.DevopsPolarisRecordDTO;
import io.choerodon.devops.infra.enums.PolarisScopeType;

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
    String queryEnvPolarisResult(Long projectId, Long envId);

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
    ClusterPolarisEnvDetailsVO clusterPolarisEnvDetail(Long projectId, Long clusterId);

    DevopsPolarisRecordDTO queryRecordByScopeIdAndScope(Long scopeId, String scope);

    /**
     * 处理来自agent的polaris扫描结果消息
     *
     * @param message polaris扫描结果
     */
    void handleAgentPolarisMessage(PolarisResponsePayloadVO message);

    /**
     * 处理经过http的来自agent的polaris扫描结果消息
     *
     * @param token     集群token
     * @param clusterId 集群id
     * @param message   消息
     */
    void handleAgentPolarisMessageFromHttp(String token, Long clusterId, PolarisResponsePayloadVO message);

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

    /**
     * 通过scope和scopeId删除扫描纪录及其相关数据
     * 一般用于删除环境和删除集群时
     *
     * @param scope   scope
     * @param scopeId clusterId或者envId
     */
    void deleteAllByScopeAndScopeId(PolarisScopeType scope, Long scopeId);

    Map<Long, Double> listProjectScores(List<Long> actualPids);
}
