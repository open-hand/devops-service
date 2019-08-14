package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.AgentPodInfoVO;
import org.springframework.lang.Nullable;

/**
 * 关于Pod实时数据
 *
 * @author zmf
 */
public interface AgentPodService {
    /**
     * 处理Agent发送的实时Pod数据
     *
     * @param pods pod数据
     */
    void handleRealTimePodData(List<AgentPodInfoVO> pods);

    /**
     * 查询一个Pod的所有快照数据
     *
     * @param podName   pod名称
     * @param namespace kubernetes namespace
     * @return 实时数据
     */
    List<AgentPodInfoVO> queryAllPodSnapshots(String podName, String namespace);

    /**
     * 查询Pod在Redis中最新的实时数据
     *
     * @param podName   pod name
     * @param namespace kubernetes namespace
     * @return 最新的实时数据
     */
    @Nullable
    AgentPodInfoVO queryLatestPodSnapshot(String podName, String namespace);
}
