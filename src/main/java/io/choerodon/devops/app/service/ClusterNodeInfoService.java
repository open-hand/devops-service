package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.AgentNodeInfoVO;
import io.choerodon.devops.api.vo.ClusterNodeInfoVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zmf
 */
public interface ClusterNodeInfoService {
    /**
     * get redis cluster key to baseQueryByRecordId node information
     *
     * @param clusterId the cluster id
     * @return the redis key according to the cluster id
     */
    String getRedisClusterKey(Long clusterId);

    /**
     * get redis cluster key to baseQueryByRecordId node information
     *
     * @param clusterId the cluster id
     * @param projectId the project id
     * @return the redis key according to the cluster id
     */
    String getRedisClusterKey(Long clusterId, Long projectId);

    /**
     * set the node information for the redis key.
     * The previous value will be discarded.
     *
     * @param redisClusterKey  the key
     * @param agentNodeInfoVOS the information of nodes.
     */
    void setValueForKey(String redisClusterKey, List<AgentNodeInfoVO> agentNodeInfoVOS);

    /**
     * page query the node information of the cluster
     *
     * @param clusterId the cluster id
     * @param projectId the project id
     * @param pageable  the page parameters
     * @return a page of nodes
     */
    Page<ClusterNodeInfoVO> pageClusterNodeInfo(Long clusterId, Long projectId, PageRequest pageable);

    /**
     * get cluster node information by cluster id and node name
     * There is a requirement of organization because the organization id is
     * available in front end and this can save a query in database for organization id.
     *
     * @param projectId project id
     * @param clusterId the cluster id
     * @param nodeName  the node name
     * @return the node information
     */
    ClusterNodeInfoVO queryNodeInfo(Long projectId, Long clusterId, String nodeName);

    List<String> queryNodeName(Long projectId, Long clusterId);

    /**
     * 获取集群节点的数量
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @return 数量
     */
    long countNodes(Long projectId, Long clusterId);
}
