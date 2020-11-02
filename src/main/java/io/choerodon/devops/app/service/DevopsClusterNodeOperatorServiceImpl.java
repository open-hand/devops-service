package io.choerodon.devops.app.service;

import static io.choerodon.devops.infra.constant.ClusterCheckConstant.ERROR_DELETE_NODE_FAILED;
import static io.choerodon.devops.infra.constant.DevopsClusterCommandConstants.*;

import java.io.IOException;
import java.util.List;

import net.schmizz.sshj.SSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsClusterNodeVO;
import io.choerodon.devops.api.vo.ExecResultInfoVO;
import io.choerodon.devops.api.vo.HostConnectionVO;
import io.choerodon.devops.api.vo.InventoryVO;
import io.choerodon.devops.infra.constant.DevopsClusterCommandConstants;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.mapper.DevopsClusterNodeMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.SshUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/30 10:15
 */
@Service
public class DevopsClusterNodeOperatorServiceImpl implements DevopsClusterNodeOperatorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsClusterNodeOperatorServiceImpl.class);
    private static final String ERROR_ADD_NODE_FAILED = "error.add.node.failed";


    @Autowired
    private SshUtil sshUtil;
    @Autowired
    @Lazy
    private DevopsClusterNodeService devopsClusterNodeService;
    @Autowired
    private DevopsClusterOperatingRecordService devopsClusterOperatingRecordService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private DevopsClusterNodeMapper devopsClusterNodeMapper;

    @Override
    public void addNode(Long projectId, Long clusterId, DevopsClusterNodeVO nodeVO) {
        SSHClient sshClient = new SSHClient();
        try {
            // 保存数据库记录
            LOGGER.info(">>>>>>>>> [add node] save cluster {} node to db. <<<<<<<<<<<<<<<", clusterId);
            // 1. 查询集群节点信息
            List<DevopsClusterNodeDTO> outerNodes = devopsClusterNodeService.queryNodeByClusterIdAndType(clusterId, ClusterNodeTypeEnum.OUTTER);
            List<DevopsClusterNodeDTO> innerNodes = devopsClusterNodeService.queryNodeByClusterIdAndType(clusterId, ClusterNodeTypeEnum.INNER);


            DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeService.queryByClusterIdAndNodeName(clusterId, nodeVO.getName());
            if (devopsClusterNodeDTO == null) {
                devopsClusterNodeDTO = ConvertUtils.convertObject(nodeVO, DevopsClusterNodeDTO.class);
                devopsClusterNodeDTO.setClusterId(clusterId);
                devopsClusterNodeService.baseSave(devopsClusterNodeDTO);
            } else {
                devopsClusterNodeService.baseUpdateNodeRole(devopsClusterNodeDTO.getId(), devopsClusterNodeDTO.getRole() + nodeVO.getRole());
            }
            // 计算inventory配置
            InventoryVO inventoryVO = devopsClusterNodeService.calculateGeneralInventoryValue(innerNodes);
            addNodeIniConfig(inventoryVO, nodeVO);
            // 连接主机
            DevopsClusterNodeDTO linkNode;
            if (!CollectionUtils.isEmpty(outerNodes)) {
                linkNode = outerNodes.get(0);
            } else {
                linkNode = innerNodes.get(0);
            }
            HostConnectionVO hostConnectionVO = ConvertUtils.convertObject(linkNode, HostConnectionVO.class);
            hostConnectionVO.setHostSource(HostSourceEnum.CUSTOMHOST.getValue());
            LOGGER.info(">>>>>>>>> [add node]  cluster {} ssh connect. <<<<<<<<<<<<<<<", clusterId);
            sshUtil.sshConnect(hostConnectionVO, sshClient);

            // 上传配置文件
            devopsClusterNodeService.generateAndUploadNodeConfiguration(sshClient, String.valueOf(clusterId), inventoryVO);
            // 执行添加节点操作
            String command;
            if (ClusterNodeRoleEnum.isMaster(devopsClusterNodeDTO.getRole())) {
                command = ADD_MASTER_YML;
            } else if (ClusterNodeRoleEnum.isWorker(devopsClusterNodeDTO.getRole())) {
                command = ADD_WORKER_YML;
            } else {
                throw new CommonException(ERROR_ADD_NODE_FAILED);
            }
            ExecResultInfoVO execResultInfoVO = sshUtil.execCommand(sshClient, String.format(DevopsClusterCommandConstants.ANSIBLE_COMMAND_TEMPLATE, command));
            LOGGER.info("add node {} result is, {}", devopsClusterNodeDTO.getName(), execResultInfoVO);
            if (execResultInfoVO.getExitCode() != 0) {
                throw new CommonException(ERROR_ADD_NODE_FAILED, execResultInfoVO.getStdErr());
            }
            devopsClusterService.updateStatusById(clusterId, ClusterStatusEnum.DISCONNECT);
            devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                    null,
                    ClusterOperatingTypeEnum.ADD_NODE.value(),
                    ClusterOperationStatusEnum.SUCCESS.value(),
                    null);
        } catch (Exception e) {
            throw new CommonException(ERROR_ADD_NODE_FAILED, e);
        } finally {
            sshUtil.sshDisconnect(sshClient);
        }
    }

    @Override
    public void deleteNode(Long projectId, DevopsClusterNodeDTO devopsClusterNodeDTO) {
        SSHClient sshClient = new SSHClient();
        try {
            // 删除集群中的node
            // 1. 查询集群节点信息
            List<DevopsClusterNodeDTO> outerNodes = devopsClusterNodeService.queryNodeByClusterIdAndType(devopsClusterNodeDTO.getClusterId(), ClusterNodeTypeEnum.OUTTER);
            List<DevopsClusterNodeDTO> innerNodes = devopsClusterNodeService.queryNodeByClusterIdAndType(devopsClusterNodeDTO.getClusterId(), ClusterNodeTypeEnum.INNER);


            // 计算inventory配置
            InventoryVO inventoryVO = devopsClusterNodeService.calculateGeneralInventoryValue(innerNodes);
            inventoryVO.getDelNode().append(devopsClusterNodeDTO.getName());

            // 连接主机
            DevopsClusterNodeDTO linkNode;
            if (!CollectionUtils.isEmpty(outerNodes)) {
                linkNode = outerNodes.get(0);
            } else {
                linkNode = innerNodes.get(0);
            }
            HostConnectionVO hostConnectionVO = ConvertUtils.convertObject(linkNode, HostConnectionVO.class);
            hostConnectionVO.setHostSource(HostSourceEnum.CUSTOMHOST.getValue());

            sshUtil.sshConnect(hostConnectionVO, sshClient);
            // 上传配置文件
            devopsClusterNodeService.generateAndUploadNodeConfiguration(sshClient, String.valueOf(devopsClusterNodeDTO.getClusterId()), inventoryVO);
            // 执行删除节点操作
            ExecResultInfoVO execResultInfoVO = sshUtil.execCommand(sshClient, String.format(DevopsClusterCommandConstants.ANSIBLE_COMMAND_TEMPLATE, DevopsClusterCommandConstants.REMOVE_NODE_YAML));
            LOGGER.info("delete node {} result is, {}", devopsClusterNodeDTO.getId(), execResultInfoVO);
            if (execResultInfoVO.getExitCode() != 0) {
                throw new CommonException(ERROR_DELETE_NODE_FAILED, execResultInfoVO.getStdErr());
            }
            // 删除数据库中数据
            devopsClusterNodeService.baseDelete(devopsClusterNodeDTO.getId());
            devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                    devopsClusterNodeDTO.getId(),
                    ClusterOperatingTypeEnum.DELETE_NODE.value(),
                    ClusterOperationStatusEnum.SUCCESS.value(),
                    null);
        } catch (Exception e) {
            devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                    devopsClusterNodeDTO.getId(),
                    ClusterOperatingTypeEnum.DELETE_NODE.value(),
                    ClusterOperationStatusEnum.FAILED.value(),
                    e.getMessage());
            throw new CommonException(ERROR_DELETE_NODE_FAILED, e);
        } finally {
            devopsClusterService.updateStatusById(devopsClusterNodeDTO.getClusterId(), ClusterStatusEnum.DISCONNECT);
            sshUtil.sshDisconnect(sshClient);
        }
    }

    @Override
    public void deleteNodeRole(Long projectId, DevopsClusterNodeDTO devopsClusterNodeDTO, Integer role) {
        SSHClient sshClient = new SSHClient();
        try {
            // 删除节点角色
            // 删除集群中的node
            // 1. 查询集群节点信息
            List<DevopsClusterNodeDTO> outerNodes = devopsClusterNodeService.queryNodeByClusterIdAndType(devopsClusterNodeDTO.getClusterId(), ClusterNodeTypeEnum.OUTTER);
            List<DevopsClusterNodeDTO> innerNodes = devopsClusterNodeService.queryNodeByClusterIdAndType(devopsClusterNodeDTO.getClusterId(), ClusterNodeTypeEnum.INNER);


            // 计算invertory配置
            InventoryVO inventoryVO = devopsClusterNodeService.calculateGeneralInventoryValue(innerNodes);
            String command = null;
            if (ClusterNodeRoleEnum.isMaster(role)) {
                inventoryVO.getDelMaster().append(devopsClusterNodeDTO.getName());
                command = DevopsClusterCommandConstants.REMOVE_MASTER_YAML;
            }
            if (ClusterNodeRoleEnum.isEtcd(role)) {
                inventoryVO.getDelEtcd().append(devopsClusterNodeDTO.getName());
                command = DevopsClusterCommandConstants.REMOVE_ETCD_YAML;
            }
            // 连接主机
            DevopsClusterNodeDTO linkNode;
            if (!CollectionUtils.isEmpty(outerNodes)) {
                linkNode = outerNodes.get(0);
            } else {
                linkNode = innerNodes.get(0);
            }
            HostConnectionVO hostConnectionVO = ConvertUtils.convertObject(linkNode, HostConnectionVO.class);
            hostConnectionVO.setHostSource(HostSourceEnum.CUSTOMHOST.getValue());
            sshUtil.sshConnect(hostConnectionVO, sshClient);
            // 上传配置文件
            devopsClusterNodeService.generateAndUploadNodeConfiguration(sshClient, String.valueOf(devopsClusterNodeDTO.getClusterId()), inventoryVO);
            // 执行删除节点操作
            ExecResultInfoVO execResultInfoVO = sshUtil.execCommand(sshClient, String.format(DevopsClusterCommandConstants.ANSIBLE_COMMAND_TEMPLATE, command));
            LOGGER.info("operating cluster failed. node id {} result is, {}", devopsClusterNodeDTO.getId(), execResultInfoVO);
            if (execResultInfoVO.getExitCode() != 0) {
                throw new CommonException(ERROR_DELETE_NODE_FAILED, execResultInfoVO.getStdErr());
            }

            // 删除数据库数据
            int resultRole = 0;
            if (ClusterNodeRoleEnum.isMaster(devopsClusterNodeDTO.getRole())) {
                resultRole = 1;
            } else {
                resultRole = devopsClusterNodeDTO.getRole() - role;
            }

            devopsClusterNodeService.baseUpdateNodeRole(devopsClusterNodeDTO.getId(), resultRole);
            devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                    devopsClusterNodeDTO.getId(),
                    ClusterOperatingTypeEnum.DELETE_NODE_ROLE.value(),
                    ClusterOperationStatusEnum.SUCCESS.value(),
                    null);
        } catch (Exception e) {
            // 操作失败，记录失败数据
            devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                    devopsClusterNodeDTO.getId(),
                    ClusterOperatingTypeEnum.DELETE_NODE_ROLE.value(),
                    ClusterOperationStatusEnum.FAILED.value(),
                    e.getMessage());
            throw new CommonException(ERROR_DELETE_NODE_FAILED, e);
        } finally {
            devopsClusterService.updateStatusById(devopsClusterNodeDTO.getClusterId(), ClusterStatusEnum.DISCONNECT);
            sshUtil.sshDisconnect(sshClient);
        }
    }

    private void addNodeIniConfig(InventoryVO inventoryVO, DevopsClusterNodeVO node) {
        if (HostAuthType.ACCOUNTPASSWORD.value().equals(node.getAuthType())) {
            inventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL_PASSWORD_TYPE, node.getName(), node.getHostIp(), node.getHostPort(), node.getUsername(), node.getPassword()))
                    .append(System.lineSeparator());
        } else {
            inventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL_PRIVATE_KEY_TYPE, node.getName(), node.getHostIp(), node.getHostPort(), node.getUsername(), String.format(PRIVATE_KEY_SAVE_PATH_TEMPLATE, node.getName())))
                    .append(System.lineSeparator());
        }
        // 设置master节点
        if (ClusterNodeRoleEnum.listMasterRoleSet().contains(node.getRole())) {
            inventoryVO.getNewMaster().append(node.getName())
                    .append(System.lineSeparator());
        }
        // 设置worker节点
        if (ClusterNodeRoleEnum.listWorkerRoleSet().contains(node.getRole())) {
            inventoryVO.getNewWorker().append(node.getName())
                    .append(System.lineSeparator());
        }
    }
}
