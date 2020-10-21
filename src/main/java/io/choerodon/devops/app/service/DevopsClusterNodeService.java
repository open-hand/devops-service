package io.choerodon.devops.app.service;

import java.util.List;

import net.schmizz.sshj.SSHClient;

import io.choerodon.devops.api.vo.DevopsClusterNodeConnectionTestVO;
import io.choerodon.devops.api.vo.DevopsClusterNodeVO;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;

public interface DevopsClusterNodeService {
    /**
     * 测试当前节点连通性
     *
     * @param projectId                         项目id
     * @param devopsClusterNodeConnectionTestVO 连接信息
     * @return 测试结果 boolean
     */
    boolean testConnection(Long projectId, DevopsClusterNodeConnectionTestVO devopsClusterNodeConnectionTestVO);

    /**
     * 检查所有节点信息
     *
     * @param ssh ssh客户端连接信息
     */
    void checkNode(SSHClient ssh);

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
    Boolean checkEnableDelete(Long projectId, Long nodeId);

    /**
     * 登录指定节点执行命令
     *
     * @param ssh     ssh连接对象
     * @param command 命令
     */
    void execCommand(SSHClient ssh, String command);

    /**
     * 上传安装集群的节点配置信息
     * @param ssh
     * @param devopsClusterNodeVOList
     */
    void uploadNodeConfiguration(SSHClient ssh, List<DevopsClusterNodeVO> devopsClusterNodeVOList);

}
