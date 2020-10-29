package io.choerodon.devops.app.service;

import java.util.List;

import net.schmizz.sshj.SSHClient;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.DevopsClusterOperationPayload;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;
import io.choerodon.devops.infra.enums.ClusterNodeTypeEnum;

public interface DevopsClusterNodeService {
    /**
     * 测试当前节点连通性
     *
     * @param projectId               项目id
     * @param clusterHostConnectionVO 连接信息
     * @return 测试结果 boolean
     */
    boolean testConnection(Long projectId, ClusterHostConnectionVO clusterHostConnectionVO);

    /**
     * 保存节点信息
     *
     * @param devopsClusterDTOList 节点列表
     * @param projectId            项目id
     * @param clusterId            集群id
     */
    void saveNode(List<DevopsClusterNodeDTO> devopsClusterDTOList, Long projectId, Long clusterId);

    /**
     * 检查所有节点信息
     *
     * @param projectId                项目id
     * @param clusterId                集群id
     * @param devopsClusterNodeDTOList 节点列表
     * @param hostConnectionVO         ssh信息
     */
    void checkAndSaveNode(Long projectId, Long clusterId, List<DevopsClusterNodeDTO> devopsClusterNodeDTOList, HostConnectionVO hostConnectionVO);

    /**
     * 批量插入
     *
     * @param devopsClusterNodeDTOList 列表
     * @return
     */
    void batchInsert(List<DevopsClusterNodeDTO> devopsClusterNodeDTOList);

    /**
     * 校验是否能够删除节点
     *
     * @param projectId
     * @param nodeId
     * @return
     */
    NodeDeleteCheckVO checkEnableDelete(Long projectId, Long nodeId);

    /**
     * 生成并上传集群的节点配置信息
     *
     * @param ssh         ssh连接对象
     * @param inventoryVO 配置对应节点
     */
    void generateAndUploadNodeConfiguration(SSHClient ssh, Long clusterId, InventoryVO inventoryVO, String operation);


    /**
     * 删除node
     *
     * @param projectId
     * @param nodeId
     */
    void delete(Long projectId, Long nodeId);

    /**
     * 校验是否能够删除节点角色
     *
     * @param projectId
     * @param nodeId
     * @return
     */
    NodeRoleDeleteCheckVO checkEnableDeleteRole(Long projectId, Long nodeId);

    /**
     * 删除节点角色
     *
     * @param projectId
     * @param nodeId
     * @param role
     */
    void deleteRole(Long projectId, Long nodeId, Integer role);

    /**
     * 安装k8s
     *
     * @param devopsClusterOperationPayload
     */
    void installK8s(DevopsClusterOperationPayload devopsClusterOperationPayload);

    List<DevopsClusterNodeDTO> queryByClusterId(Long clusterId);

    /**
     * 添加节点
     *
     * @param projectId
     * @param clusterId
     * @param nodeVO
     */
    void addNode(Long projectId, Long clusterId, DevopsClusterNodeVO nodeVO);

    List<DevopsClusterNodeDTO> queryNodeByClusterIdAndType(Long clusterId, ClusterNodeTypeEnum type);

    /**
     * 定时更新集群的安装状态
     */
    void update();

    /**
     * 根据集群id删除node
     *
     * @param clusterId
     */
    void deleteByClusterId(Long clusterId);
}
