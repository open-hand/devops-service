package io.choerodon.devops.api.vo;

import io.choerodon.core.domain.Page;
import io.choerodon.core.domain.PageInfo;

/**
 * 带有节点信息的集群信息
 *
 * @author zmf
 */
public class ClusterWithNodesVO extends DevopsClusterRepVO {
    private Page<ClusterNodeInfoVO> nodes;

    public Page<ClusterNodeInfoVO> getNodes() {
        return nodes;
    }

    public void setNodes(Page<ClusterNodeInfoVO> nodes) {
        this.nodes = nodes;
    }
}
