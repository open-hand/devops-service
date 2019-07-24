package io.choerodon.devops.api.vo;

import com.github.pagehelper.PageInfo;

/**
 * 带有节点信息的集群信息
 *
 * @author zmf
 */
public class ClusterWithNodesVO extends DevopsClusterRepVO {
    private PageInfo<ClusterNodeInfoVO> nodes;

    public PageInfo<ClusterNodeInfoVO> getNodes() {
        return nodes;
    }

    public void setNodes(PageInfo<ClusterNodeInfoVO> nodes) {
        this.nodes = nodes;
    }
}
