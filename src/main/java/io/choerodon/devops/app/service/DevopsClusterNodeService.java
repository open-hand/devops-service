package io.choerodon.devops.app.service;

import java.io.IOException;
import java.util.List;

import net.schmizz.sshj.SSHClient;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.DevopsClusterInstallPayload;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;
import io.choerodon.devops.infra.enums.ClusterNodeTypeEnum;

public interface DevopsClusterNodeService {
    void baseSave(DevopsClusterNodeDTO devopsClusterNodeDTO);

    void baseUpdateNodeRole(Long id, Integer role);

    /**
     * 测试当前节点连通性
     *
     * @param projectId               项目id
     * @param clusterHostConnectionVO 连接信息
     * @return 测试结果 boolean
     */
    boolean testConnection(Long projectId, ClusterHostConnectionVO clusterHostConnectionVO);

    /**
     * 检查所有节点信息
     *
     * @param devopsClusterInstallPayload payload
     * @return
     */
    DevopsClusterInstallPayload checkAndSaveNode(DevopsClusterInstallPayload devopsClusterInstallPayload);

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
     * @param suffix      目录后缀
     * @param inventoryVO 配置对应节点
     */
    void generateAndUploadNodeConfiguration(SSHClient ssh, String suffix, InventoryVO inventoryVO);

    /**
     * @param ssh          ssh连接对象
     * @param suffix       目录后缀
     * @param command      命令
     * @param logPath      日志输出路径
     * @param exitCodePath 退出码保存路径
     */
    void generateAndUploadAnsibleShellScript(SSHClient ssh, String suffix, String command, String logPath, String exitCodePath);

    /**
     * 删除node
     *
     * @param projectId
     * @param nodeId
     */
    Long delete(Long projectId, Long nodeId);

    /**
     * 删除节点角色
     *
     * @param projectId
     * @param nodeId
     * @param role
     */
    Long deleteRole(Long projectId, Long nodeId, Integer role);

    /**
     * 安装k8s
     *
     * @param devopsClusterInstallPayload
     */
    void executeInstallK8sInBackground(DevopsClusterInstallPayload devopsClusterInstallPayload);

    List<DevopsClusterNodeDTO> queryByClusterId(Long clusterId);

    /**
     * 添加节点
     *
     * @param projectId
     * @param clusterId
     * @param nodeVO
     */
    void addNode(Long projectId, Long clusterId, DevopsClusterNodeVO nodeVO);

    DevopsClusterNodeDTO queryByClusterIdAndNodeName(Long clusterId, String nodeName);

    List<DevopsClusterNodeDTO> queryNodeByClusterIdAndType(Long clusterId, ClusterNodeTypeEnum type);

    /**
     * 根据集群id删除node
     *
     * @param clusterId
     */
    void deleteByClusterId(Long clusterId);

    InventoryVO calculateGeneralInventoryValue(List<DevopsClusterNodeDTO> innerNodes);

    /**
     * 保存集群信息和节点信息
     *
     * @param devopsClusterDTOList 节点列表
     * @param projectId            项目id
     * @param devopsClusterReqVO   集群信息
     * @return 集群id
     */
    Long saveInfo(List<DevopsClusterNodeDTO> devopsClusterDTOList, Long projectId, DevopsClusterReqVO devopsClusterReqVO);

    /**
     * 保存privateKey到节点上
     *
     * @param ssh                      ssh连接对象
     * @param devopsClusterNodeDTOList 节点列表
     * @return 执行结果
     * @throws IOException
     */
    void generateAndUploadPrivateKey(SSHClient ssh, List<DevopsClusterNodeDTO> devopsClusterNodeDTOList) throws IOException;

    /**
     * 上传docker安装shell
     *
     * @param ssh
     * @param suffix
     * @throws Exception
     */
    void uploadPreKubeadmnHaShell(SSHClient ssh, String suffix) throws Exception;

    /**
     * 更新集群安装操作结果的方法
     */
    void update();

    List<DevopsClusterNodeDTO> listByClusterId(Long clusterId);

    void baseDelete(Long id);

    Boolean checkEnableDeleteRole(Long projectId, Long nodeId, Integer role);

    void baseAddNodeRole(Long id, Integer role);

    NodeOperatingResultVO checkOperatingResult(Long projectId, Long operationRecordId);
}
