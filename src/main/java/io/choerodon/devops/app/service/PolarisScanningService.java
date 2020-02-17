package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.polaris.PolarisResponsePayloadVO;
import io.choerodon.devops.infra.dto.DevopsPolarisRecordDTO;

/**
 * @author zmf
 * @since 2/17/20
 */
public interface PolarisScanningService {
    /**
     * 扫描环境
     *
     * @param envId 环境id
     * @return 扫描纪录
     */
    DevopsPolarisRecordDTO scanEnv(Long envId);

    /**
     * 扫描集群
     *
     * @param clusterId 集群id
     * @return 扫描纪录
     */
    DevopsPolarisRecordDTO scanCluster(Long clusterId);

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
