package io.choerodon.devops.api.vo;

import com.github.pagehelper.PageInfo;

/**
 * 带有节点信息的集群信息
 *
 * @author zmf
 */
public class ClusterWithNodesDTO extends DevopsClusterRepDTO {
    private PageInfo<ClusterNodeInfoDTO> nodes;

    public PageInfo<ClusterNodeInfoDTO> getNodes() {
        return nodes;
    }

    public void setNodes(PageInfo<ClusterNodeInfoDTO> nodes) {
        this.nodes = nodes;
    }
}
