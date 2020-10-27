package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import net.schmizz.sshj.SSHClient;

import io.choerodon.devops.api.vo.HostConnectionVO;
import io.choerodon.devops.api.vo.InventoryVO;
import io.choerodon.devops.api.vo.NodeDeleteCheckVO;
import io.choerodon.devops.api.vo.NodeRoleDeleteCheckVO;
import io.choerodon.devops.app.eventhandler.payload.DevopsK8sInstallPayload;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;

public interface DevopsClusterNodeService {
    /**
     * 测试当前节点连通性
     *
     * @param projectId        项目id
     * @param hostConnectionVO 连接信息
     * @return 测试结果 boolean
     */
    boolean testConnection(Long projectId, HostConnectionVO hostConnectionVO);

    /**
     * 检查所有节点信息
     *
     * @param projectId                项目id
     * @param clusterId                集群id
     * @param devopsClusterNodeDTOList 节点列表
     * @param hostConnectionVO         ssh信息
     */
    void checkNode(Long projectId, Long clusterId, List<DevopsClusterNodeDTO> devopsClusterNodeDTOList, HostConnectionVO hostConnectionVO);

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
    void generateAndUploadNodeConfiguration(SSHClient ssh, Long clusterId, InventoryVO inventoryVO);


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
     * @param devopsK8sInstallPayload
     */
    void installK8s(DevopsK8sInstallPayload devopsK8sInstallPayload);

    List<DevopsClusterNodeDTO> queryByClusterId(Long clusterId);
}
