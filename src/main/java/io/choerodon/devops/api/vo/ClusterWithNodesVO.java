package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.core.domain.Page;

/**
 * 带有节点信息的集群信息
 *
 * @author zmf
 */
public class ClusterWithNodesVO extends DevopsClusterRepVO {
    @ApiModelProperty("节点列表")
    private Page<ClusterNodeInfoVO> nodes;

    public Page<ClusterNodeInfoVO> getNodes() {
        return nodes;
    }

    public void setNodes(Page<ClusterNodeInfoVO> nodes) {
        this.nodes = nodes;
    }
}
