package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestResultVO;
import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestVO;

public interface DevopsClusterNodeService {
    /**
     * 测试当前节点连通性
     *
     * @param projectId                         项目id
     * @param devopsClusterNodeConnectionTestVO 连接信息
     * @return 测试结果 boolean
     */
    DevopsClusterNodeConnectionTestResultVO testConnection(Long projectId, DevopsClusterNodeConnectionTestVO devopsClusterNodeConnectionTestVO);
}
