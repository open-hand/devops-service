package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * ssh连接的节点信息
 */
public class DevopsClusterSshNodeInfoVO {
    private DevopsClusterNodeVO devopsClusterNodeVO;
    @Encrypt
    private Long clusterId;

    public DevopsClusterNodeVO getDevopsClusterNodeVO() {
        return devopsClusterNodeVO;
    }

    public DevopsClusterSshNodeInfoVO setDevopsClusterNodeVO(DevopsClusterNodeVO devopsClusterNodeVO) {
        this.devopsClusterNodeVO = devopsClusterNodeVO;
        return this;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public DevopsClusterSshNodeInfoVO setClusterId(Long clusterId) {
        this.clusterId = clusterId;
        return this;
    }
}
