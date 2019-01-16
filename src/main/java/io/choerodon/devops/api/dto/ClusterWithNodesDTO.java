package io.choerodon.devops.api.dto;

import io.choerodon.core.domain.Page;

/**
 * 带有节点信息的集群信息
 *
 * @author zmf
 */
public class ClusterWithNodesDTO extends DevopsClusterRepDTO {
    private Page<ClusterNodeInfoDTO> nodes;

    public Page<ClusterNodeInfoDTO> getNodes() {
        return nodes;
    }

    public void setNodes(Page<ClusterNodeInfoDTO> nodes) {
        this.nodes = nodes;
    }
}
