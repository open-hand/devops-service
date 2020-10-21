package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestResultVO;
import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestVO;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;

public interface DevopsClusterNodeService {
    /**
     * 测试当前节点连通性
     *
     * @param projectId                         项目id
     * @param devopsClusterNodeConnectionTestVO 连接信息
     * @return 测试结果 boolean
     */
    DevopsClusterNodeConnectionTestResultVO testConnection(Long projectId, DevopsClusterNodeConnectionTestVO devopsClusterNodeConnectionTestVO);

    /**
     * 批量插入
     *
     * @param devopsClusterNodeDTOList 列表
     * @return
     */
    void batchInsert(List<DevopsClusterNodeDTO> devopsClusterNodeDTOList);

    /**
     * 校验是否能够删除节点
     * @param projectId
     * @param nodeId
     * @return
     */
    Boolean checkEnableDelete(Long projectId, Long nodeId);
}
