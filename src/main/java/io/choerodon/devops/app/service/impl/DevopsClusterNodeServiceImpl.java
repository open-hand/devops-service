package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.DevopsClusterCommandConstants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import org.apache.commons.io.IOUtils;
import org.hzero.core.util.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.DevopsK8sInstallPayload;
import io.choerodon.devops.app.service.DevopsClusterNodeService;
import io.choerodon.devops.app.service.DevopsClusterOperatingRecordService;
import io.choerodon.devops.infra.constant.ClusterCheckConstant;
import io.choerodon.devops.infra.constant.DevopsClusterCommandConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;
import io.choerodon.devops.infra.dto.DevopsClusterOperationRecordDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;
import io.choerodon.devops.infra.mapper.DevopsClusterNodeMapper;
import io.choerodon.devops.infra.mapper.DevopsClusterOperationRecordMapper;
import io.choerodon.devops.infra.util.*;

@Service
public class DevopsClusterNodeServiceImpl implements DevopsClusterNodeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsClusterNodeServiceImpl.class);
    private static final String INVENTORY_INI_TEMPLATE_FOR_ALL = "%s ansible_host=%s ansible_port=%s ansible_user=%s ansible_ssh_pass=%s";
    private static final String INVENTORY_INI_TEMPLATE_FOR_NODE_IP = "%s\n";
    private static final String ALL = "all";
    private static final String ETCD = "etcd";
    private static final String KUBE_MASTER = "kube-master";
    private static final String KUBE_WORKER = "kube-worker";
    private static final String NEW_MASTER = "new-master";
    private static final String NEW_ETCD = "new-etcd";
    private static final String NEW_WORKER = "new-worker";
    private static final String DEL_WORKER = "del-worker";
    private static final String DEL_MASTER = "del-master";
    private static final String DEL_ETCD = "del-etcd";
    private static final String DEL_NODE = "del-node";
    private static final String INVENTORY_CONFIG_FILE_PATH = "/tmp/inventory.ini";
    private static final String CLUSTER_LOCK_KEY = "cluster:lock:key:%s:Long";
    private static final String CLUSTER_OPERATING_KEY = "cluster:operating:key:%s:DevopsClusterOperatorVO";
    /**
     * 节点检查进度redis的key
     */
    public static final String NODE_CHECK_STEP_REDIS_KEY_TEMPLATE = "node-check-step-%d-%d";
    /**
     * 集群安装信息redis的key
     */
    public static final String CLUSTER_INSTALL_LOG_REDIS_KEY_TEMPLATE = "cluster-install-log-%d%d";

    private static final String ERROR_DELETE_NODE_FAILED = "error.delete.node.failed";
    /**
     * inventory配置文件名称
     */
    private static final String INVENTORY_INI_FILE_NAME = "inventory.ini";
    private static final String INVENTORY_INI_TEMPLATE_FOR_NODE = "%s\n";
    private static final String[] configTypes = new String[]{ALL, ETCD, KUBE_MASTER, KUBE_WORKER, NEW_MASTER, NEW_ETCD, NEW_WORKER, DEL_ETCD, DEL_WORKER, DEL_MASTER, DEL_NODE};
    @Autowired
    private SshUtil sshUtil;
    @Autowired
    private DevopsClusterMapper devopsClusterMapper;
    @Autowired
    private DevopsClusterNodeMapper devopsClusterNodeMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private DevopsClusterOperationRecordMapper devopsClusterOperationRecordMapper;
    @Autowired
    private DevopsClusterOperatingRecordService devopsClusterOperatingRecordService;

    @Override
    public boolean testConnection(Long projectId, HostConnectionVO hostConnectionVO) {
        return SshUtil.sshConnectForOK(hostConnectionVO.getHostIp(),
                hostConnectionVO.getHostPort(),
                hostConnectionVO.getAccountType(),
                hostConnectionVO.getUsername(),
                hostConnectionVO.getPassword());
    }

    @Override
    public void batchInsert(List<DevopsClusterNodeDTO> devopsClusterNodeDTOList) {
        int size = devopsClusterNodeDTOList.size();
        if (devopsClusterNodeMapper.batchInsert(devopsClusterNodeDTOList) != size) {
            throw new CommonException("error.batch.insert.node");
        }
    }

    @Override
    public NodeDeleteCheckVO checkEnableDelete(Long projectId, Long nodeId) {
        Assert.notNull(projectId, ResourceCheckConstant.ERROR_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);

        NodeDeleteCheckVO nodeDeleteCheckVO = new NodeDeleteCheckVO();
        // 查询节点类型
        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);
        if (ClusterNodeRoleEnum.listMasterRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listWorkerRoleSet()) < 2) {
                nodeDeleteCheckVO.setEnableDeleteWorker(false);
            }
        }
        if (ClusterNodeRoleEnum.listEtcdRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listEtcdRoleSet()) < 2) {
                nodeDeleteCheckVO.setEnableDeleteEtcd(false);
            }
        }
        if (ClusterNodeRoleEnum.listWorkerRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listMasterRoleSet()) < 2) {
                nodeDeleteCheckVO.setEnableDeleteMaster(false);
            }
        }

        return nodeDeleteCheckVO;
    }

    private void checkNodeNumByRole(DevopsClusterNodeDTO devopsClusterNodeDTO) {
        if (ClusterNodeRoleEnum.listMasterRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listMasterRoleSet()) < 2) {
                throw new CommonException(ClusterCheckConstant.ERROR_MASTER_NODE_ONLY_ONE);
            }
        }
        if (ClusterNodeRoleEnum.listWorkerRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listWorkerRoleSet()) < 2) {
                throw new CommonException(ClusterCheckConstant.ERROR_WORKER_NODE_ONLY_ONE);
            }
        }
        if (ClusterNodeRoleEnum.listEtcdRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listWorkerRoleSet()) < 2) {
                throw new CommonException(ClusterCheckConstant.ERROR_ETCD_NODE_ONLY_ONE);
            }
        }
    }

    @Override
    @Async
    @Transactional
    public void delete(Long projectId, Long nodeId) {
        Assert.notNull(projectId, ResourceCheckConstant.ERROR_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);


        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterNodeDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        checkNodeNumByRole(devopsClusterNodeDTO);

        // 获取锁,失败则抛出异常，成功则程序继续
        String lockKey = String.format(CLUSTER_LOCK_KEY, devopsClusterNodeDTO.getClusterId());
        if (!Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "lock", 10, TimeUnit.MINUTES))) {
            throw new CommonException(ClusterCheckConstant.ERROR_CLUSTER_STATUS_IS_OPERATING);
        }
        // 更新redis集群操作状态
        DevopsClusterOperatorVO devopsClusterOperatorVO = new DevopsClusterOperatorVO();
        devopsClusterOperatorVO.setClusterId(devopsClusterNodeDTO.getClusterId());
        devopsClusterOperatorVO.setOperating(ClusterOperatingTypeEnum.DELETE_NODE.value());
        devopsClusterOperatorVO.setNodeId(nodeId);
        devopsClusterOperatorVO.setStatus(ClusterStatusEnum.OPERATING.value());
        String operatingKey = String.format(CLUSTER_OPERATING_KEY, devopsClusterNodeDTO.getClusterId());
        stringRedisTemplate.opsForValue().set(operatingKey, JsonHelper.marshalByJackson(devopsClusterOperatorVO), 10, TimeUnit.MINUTES);

        String configFilePath = UUIDUtils.generateUUID() + ".ini";
        SSHClient sshClient = new SSHClient();
        try {

            // 删除集群中的node
            // 1. 查询集群节点信息
            DevopsClusterNodeDTO record = new DevopsClusterNodeDTO();
            record.setClusterId(devopsClusterNodeDTO.getClusterId());
            List<DevopsClusterNodeDTO> devopsClusterNodeDTOS = devopsClusterNodeMapper.select(record);

            // 计算inventory配置
            InventoryVO inventoryVO = calculateGeneralInventoryValue(devopsClusterNodeDTOS);
            inventoryVO.getDelNode().append(devopsClusterNodeDTO.getName());

            // 连接主机
            HostConnectionVO hostConnectionVO = ConvertUtils.convertObject(devopsClusterNodeDTOS.get(0), HostConnectionVO.class);
            hostConnectionVO.setHostSource(HostSourceEnum.CUSTOMHOST.getValue());

            sshUtil.sshConnect(hostConnectionVO, sshClient);
            // 上传配置文件
            IOUtils.write(generateInventoryInI(inventoryVO).getBytes(), new FileOutputStream(configFilePath));
            sshUtil.uploadFile(sshClient, configFilePath, INVENTORY_CONFIG_FILE_PATH);
            // 执行删除节点操作
            ExecResultInfoVO execResultInfoVO = sshUtil.execCommand(sshClient, String.format(DevopsClusterCommandConstants.ANSIBLE_COMMAND_TEMPLATE, DevopsClusterCommandConstants.REMOVE_NODE_YAML));
            LOGGER.info("delete node {} result is, {}", nodeId, execResultInfoVO);
            if (execResultInfoVO.getExitCode() == 1) {
                throw new CommonException(ERROR_DELETE_NODE_FAILED);
            }
            // 删除数据库中数据
            if (devopsClusterNodeMapper.deleteByPrimaryKey(nodeId) != 1) {
                throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_FAILED);
            }
            devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                    nodeId,
                    ClusterOperatingTypeEnum.DELETE_NODE.value(),
                    ClusterOperationStatusEnum.SUCCESS.value(),
                    null);
        } catch (Exception e) {
            // 操作失败，记录失败数据
            devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                    nodeId,
                    ClusterOperatingTypeEnum.DELETE_NODE.value(),
                    ClusterOperationStatusEnum.FAILED.value(),
                    e.getMessage());
        } finally {
            // 删除锁
            stringRedisTemplate.delete(lockKey);
            stringRedisTemplate.delete(operatingKey);
            File file = new File(configFilePath);
            if (file.exists()) {
                file.delete();
            }
            sshUtil.sshDisconnect(sshClient);
        }

    }

    private String cutErrorMsg(String message, int i) {
        if (message.length() > i) {
            return message.substring(0, i);
        } else {
            return message;
        }
    }

    private InventoryVO calculateGeneralInventoryValue(List<DevopsClusterNodeDTO> devopsClusterNodeDTOS) {
        InventoryVO inventoryVO = new InventoryVO();
        for (DevopsClusterNodeDTO node : devopsClusterNodeDTOS) {
            // 设置所有节点
            if (CdHostAccountType.ACCOUNTPASSWORD.value().equals(node.getAccountType())) {
                inventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL, node.getName(), node.getHostIp(), node.getHostPort(), node.getUsername(), node.getPassword()))
                        .append(System.lineSeparator());
            } else {
                //todo 处理密钥认证方式
            }
            // 设置master节点
            if (ClusterNodeRoleEnum.listMasterRoleSet().contains(node.getRole())) {
                inventoryVO.getKubeMaster().append(node.getName())
                        .append(System.lineSeparator());
            }
            // 设置etcd节点
            if (ClusterNodeRoleEnum.listEtcdRoleSet().contains(node.getRole())) {
                inventoryVO.getEtcd().append(node.getName())
                        .append(System.lineSeparator());
            }
            // 设置worker节点
            if (ClusterNodeRoleEnum.listWorkerRoleSet().contains(node.getRole())) {
                inventoryVO.getKubeWorker().append(node.getName())
                        .append(System.lineSeparator());
            }
        }
        return inventoryVO;
    }

    @Override
    public NodeRoleDeleteCheckVO checkEnableDeleteRole(Long projectId, Long nodeId) {
        Assert.notNull(projectId, ResourceCheckConstant.ERROR_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);

        NodeRoleDeleteCheckVO nodeRoleDeleteCheckVO = new NodeRoleDeleteCheckVO();
        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);
        if (ClusterNodeRoleEnum.isMaster(devopsClusterNodeDTO.getRole())
                || ClusterNodeRoleEnum.isMasterAndWorker(devopsClusterNodeDTO.getRole())) {
            nodeRoleDeleteCheckVO.setEnableDeleteMasterRole(true);
        }
        if (ClusterNodeRoleEnum.isMasterAndEtcdAndWorker(devopsClusterNodeDTO.getRole())) {
            nodeRoleDeleteCheckVO.setEnableDeleteMasterRole(true);
            nodeRoleDeleteCheckVO.setEnableDeleteEtcdRole(true);
        }
        if (ClusterNodeRoleEnum.isEtcdAndWorker(devopsClusterNodeDTO.getRole())) {
            nodeRoleDeleteCheckVO.setEnableDeleteEtcdRole(true);
        }
        return nodeRoleDeleteCheckVO;
    }

    @Override
    @Async
    @Transactional
    public void deleteRole(Long projectId, Long nodeId, Integer role) {
        Assert.notNull(projectId, ResourceCheckConstant.ERROR_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);
        Assert.notNull(role, ClusterCheckConstant.ERROR_ROLE_ID_IS_NULL);

        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);

        // 删除校验
        checkEnableDeleteRole(devopsClusterNodeDTO, role);

        // 获取锁,失败则抛出异常，成功则程序继续
        String lockKey = String.format(CLUSTER_LOCK_KEY, devopsClusterNodeDTO.getClusterId());
        if (!Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "lock", 10, TimeUnit.MINUTES))) {
            throw new CommonException(ClusterCheckConstant.ERROR_CLUSTER_STATUS_IS_OPERATING);
        }
        // 更新redis集群操作状态
        DevopsClusterOperatorVO devopsClusterOperatorVO = new DevopsClusterOperatorVO();
        devopsClusterOperatorVO.setClusterId(devopsClusterNodeDTO.getClusterId());
        devopsClusterOperatorVO.setOperating(ClusterOperatingTypeEnum.DELETE_NODE_ROLE.value());
        devopsClusterOperatorVO.setNodeId(nodeId);
        devopsClusterOperatorVO.setStatus(ClusterStatusEnum.OPERATING.value());
        String operatingKey = String.format(CLUSTER_OPERATING_KEY, devopsClusterNodeDTO.getClusterId());
        stringRedisTemplate.opsForValue().set(operatingKey, JsonHelper.marshalByJackson(devopsClusterOperatorVO), 10, TimeUnit.MINUTES);


        // 删除节点角色
        // 删除集群中的node
        // 1. 查询集群节点信息
        DevopsClusterNodeDTO record = new DevopsClusterNodeDTO();
        record.setClusterId(devopsClusterNodeDTO.getClusterId());
        List<DevopsClusterNodeDTO> devopsClusterNodeDTOS = devopsClusterNodeMapper.select(record);

        // 计算invertory配置
        InventoryVO inventoryVO = calculateGeneralInventoryValue(devopsClusterNodeDTOS);
        String command = null;
        if (ClusterNodeRoleEnum.isMaster(role)) {
            inventoryVO.getDelMaster().append(devopsClusterNodeDTO.getName());
            command = DevopsClusterCommandConstants.REMOVE_MASTER_YAML;
        }
        if (ClusterNodeRoleEnum.isEtcd(role)) {
            inventoryVO.getDelEtcd().append(devopsClusterNodeDTO.getName());
            command = DevopsClusterCommandConstants.REMOVE_ETCD_YAML;
        }

        SSHClient sshClient = new SSHClient();
        String configFilePath = UUIDUtils.generateUUID() + ".ini";
        try {
            // 连接主机
            HostConnectionVO hostConnectionVO = ConvertUtils.convertObject(devopsClusterNodeDTOS.get(0), HostConnectionVO.class);
            hostConnectionVO.setHostSource(HostSourceEnum.CUSTOMHOST.getValue());
            sshUtil.sshConnect(hostConnectionVO, sshClient);
            // 上传配置文件
            IOUtils.write(generateInventoryInI(inventoryVO).getBytes(), new FileOutputStream(configFilePath));
            sshUtil.uploadFile(sshClient, configFilePath, INVENTORY_CONFIG_FILE_PATH);
            // 执行删除节点操作
            ExecResultInfoVO execResultInfoVO = sshUtil.execCommand(sshClient, String.format(DevopsClusterCommandConstants.ANSIBLE_COMMAND_TEMPLATE, command));
            LOGGER.info("operating cluster failed. node id {} result is, {}", nodeId, execResultInfoVO);
            if (execResultInfoVO.getExitCode() == 1) {
                throw new CommonException(ERROR_DELETE_NODE_FAILED);
            }

            // 删除数据库数据
            int resultRole = 0;
            if (ClusterNodeRoleEnum.isMaster(devopsClusterNodeDTO.getRole())) {
                resultRole = 1;
            } else {
                resultRole = devopsClusterNodeDTO.getRole() - role;
            }

            devopsClusterNodeDTO.setRole(resultRole == 0 ? 1 : resultRole);
            if (devopsClusterNodeMapper.updateByPrimaryKey(devopsClusterNodeDTO) != 1) {
                throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
            }
            devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                    nodeId,
                    ClusterOperatingTypeEnum.DELETE_NODE_ROLE.value(),
                    ClusterOperationStatusEnum.SUCCESS.value(),
                    null);
        } catch (Exception e) {
            // 操作失败，记录失败数据
            devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                    nodeId,
                    ClusterOperatingTypeEnum.DELETE_NODE_ROLE.value(),
                    ClusterOperationStatusEnum.FAILED.value(),
                    e.getMessage());
        } finally {
            // 删除锁
            stringRedisTemplate.delete(lockKey);
            stringRedisTemplate.delete(operatingKey);
            File file = new File(configFilePath);
            if (file.exists()) {
                file.delete();
            }
            sshUtil.sshDisconnect(sshClient);
        }


    }

    private void checkEnableDeleteRole(DevopsClusterNodeDTO devopsClusterNodeDTO, Integer roleId) {
        if (ClusterNodeRoleEnum.isWorker(roleId)) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
        }
        if (ClusterNodeRoleEnum.isEtcd(roleId)
                && Boolean.FALSE.equals(ClusterNodeRoleEnum.isEtcdAndWorker(devopsClusterNodeDTO.getRole()))
                && Boolean.FALSE.equals(ClusterNodeRoleEnum.isMasterAndEtcdAndWorker(devopsClusterNodeDTO.getRole()))
                && Boolean.FALSE.equals(ClusterNodeRoleEnum.isMasterAndEtcd(devopsClusterNodeDTO.getRole()))) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
        }
        if (ClusterNodeRoleEnum.isMaster(roleId)
                && Boolean.FALSE.equals(ClusterNodeRoleEnum.isMasterAndEtcd(devopsClusterNodeDTO.getRole()))
                && Boolean.FALSE.equals(ClusterNodeRoleEnum.isMaster(devopsClusterNodeDTO.getRole()))
                && Boolean.FALSE.equals(ClusterNodeRoleEnum.isMasterAndWorker(devopsClusterNodeDTO.getRole()))
                && Boolean.FALSE.equals(ClusterNodeRoleEnum.isMasterAndEtcdAndWorker(devopsClusterNodeDTO.getRole()))) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
        }
    }

    @Override
    public void installK8s(DevopsK8sInstallPayload devopsK8sInstallPayload) {
        try {
            List<DevopsClusterNodeDTO> devopsClusterNodeDTOList = devopsClusterNodeMapper.listByClusterId(devopsK8sInstallPayload.getClusterId());
            InventoryVO inventoryVO = calculateGeneralInventoryValue(devopsClusterNodeDTOList);
            SSHClient ssh = new SSHClient();
            sshUtil.sshConnect(ConvertUtils.convertObject(devopsK8sInstallPayload.getDevopsClusterSshNodeInfoVO(), HostConnectionVO.class), ssh);
            generateAndUploadNodeConfiguration(ssh, devopsK8sInstallPayload.getClusterId(), inventoryVO);
            ExecResultInfoVO resultInfoVO = sshUtil.execCommand(ssh, String.format(ANSIBLE_COMMAND_TEMPLATE, INSTALL_K8S));
            DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO = new DevopsClusterOperationRecordDTO()
                    .setId(devopsK8sInstallPayload.getOperationRecordId());
            DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
            devopsClusterDTO.setId(devopsK8sInstallPayload.getClusterId());
            if (resultInfoVO.getExitCode() != 0) {
                devopsClusterOperationRecordDTO.setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .setErrorMsg(resultInfoVO.getStdOut());
                devopsClusterOperationRecordMapper.updateByPrimaryKeySelective(devopsClusterOperationRecordDTO);
                devopsClusterDTO.setStatus(ClusterOperationStatusEnum.FAILED.value());
            } else {
                devopsClusterOperationRecordDTO.setStatus(ClusterOperationStatusEnum.SUCCESS.value());
                devopsClusterDTO.setStatus(ClusterOperationStatusEnum.SUCCESS.value());
            }
            devopsClusterOperationRecordMapper.updateByPrimaryKeySelective(devopsClusterOperationRecordDTO);
            devopsClusterMapper.updateByPrimaryKeySelective(devopsClusterDTO);
        } catch (Exception e) {
            throw new CommonException("error.install.k8s", e.getMessage());
        }
    }

    @Override
    public List<DevopsClusterNodeDTO> queryByClusterId(Long clusterId) {
        Assert.notNull(clusterId, ClusterCheckConstant.ERROR_CLUSTER_ID_IS_NULL);
        DevopsClusterNodeDTO devopsClusterNodeDTO = new DevopsClusterNodeDTO();
        devopsClusterNodeDTO.setClusterId(clusterId);
        return devopsClusterNodeMapper.select(devopsClusterNodeDTO);
    }

    @Override
    public void checkNode(Long projectId, Long clusterId, List<DevopsClusterNodeDTO> devopsClusterNodeDTOList, HostConnectionVO hostConnectionVO) {
        SSHClient ssh = new SSHClient();
        try {
            sshUtil.sshConnect(hostConnectionVO, ssh);
        } catch (IOException e) {
            throw new CommonException("error.node.ssh.connect", hostConnectionVO.getHostId());
        }
        // 安装docker
        try {
            ExecResultInfoVO resultInfoVO = sshUtil.execCommand(ssh, INSTALL_DOCKER_COMMAND);
            if (resultInfoVO != null && resultInfoVO.getExitCode() != 0) {
                throw new CommonException("error.node.install.docker", ssh.getRemoteHostname(), resultInfoVO.getStdErr());
            }
        } catch (IOException e) {
            throw new CommonException("error.node.install.docker", ssh.getRemoteHostname(), e.getMessage());
        }
        // 生成相关配置节点
        InventoryVO inventoryVO = calculateGeneralInventoryValue(devopsClusterNodeDTOList);
        // 上传配置文件
        generateAndUploadNodeConfiguration(ssh, clusterId, inventoryVO);
        // 异步执行检测命令
        asyncCheck(ssh, projectId, clusterId);
    }

    @Async
    public void asyncCheck(SSHClient ssh, Long projectId, Long clusterId) {
        String redisKey = String.format(NODE_CHECK_STEP_REDIS_KEY_TEMPLATE, projectId, clusterId);
        try {
            DevopsNodeCheckResultVO devopsNodeCheckResultVO = new DevopsNodeCheckResultVO();
            // 配置检查
            ExecResultInfoVO resultInfoVOForVariable = sshUtil.execCommand(ssh, String.format(ANSIBLE_COMMAND_TEMPLATE, VARIABLE));
            if (resultInfoVOForVariable.getExitCode() != 0) {
                devopsNodeCheckResultVO.setStatus(CommandStatus.FAILED.getStatus());
                devopsNodeCheckResultVO.getConfiguration().setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .setErrorMessage(resultInfoVOForVariable.getStdOut());
                stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
                return;
            } else {
                devopsNodeCheckResultVO.getConfiguration().setStatus(ClusterOperationStatusEnum.SUCCESS.value());
            }
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));

            // 节点系统检查
            ExecResultInfoVO resultInfoVOForSystem = sshUtil.execCommand(ssh, String.format(ANSIBLE_COMMAND_TEMPLATE, SYSTEM));
            if (resultInfoVOForSystem.getExitCode() != 0) {
                devopsNodeCheckResultVO.setStatus(CommandStatus.FAILED.getStatus());
                devopsNodeCheckResultVO.getSystem().setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .setErrorMessage(resultInfoVOForVariable.getStdOut());
                stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
                return;
            } else {
                devopsNodeCheckResultVO.getSystem().setStatus(ClusterOperationStatusEnum.SUCCESS.value());
            }
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));

            // CPU检查
            ExecResultInfoVO resultInfoVOForCPU = sshUtil.execCommand(ssh, String.format(ANSIBLE_COMMAND_TEMPLATE, CPU));
            if (resultInfoVOForCPU.getExitCode() != 0) {
                devopsNodeCheckResultVO.setStatus(CommandStatus.FAILED.getStatus());
                devopsNodeCheckResultVO.getCpu().setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .setErrorMessage(resultInfoVOForVariable.getStdOut());
                stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
                return;
            } else {
                devopsNodeCheckResultVO.getCpu().setStatus(ClusterOperationStatusEnum.SUCCESS.value());
            }
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));

            // 内存检查
            ExecResultInfoVO resultInfoVOForMemory = sshUtil.execCommand(ssh, String.format(ANSIBLE_COMMAND_TEMPLATE, MEMORY));
            if (resultInfoVOForMemory.getExitCode() != 0) {
                devopsNodeCheckResultVO.setStatus(CommandStatus.FAILED.getStatus());
                devopsNodeCheckResultVO.getMemory().setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .setErrorMessage(resultInfoVOForVariable.getStdOut());
                stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
                return;
            } else {
                devopsNodeCheckResultVO.getMemory().setStatus(ClusterOperationStatusEnum.SUCCESS.value());
            }
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
        } catch (IOException e) {
            sshUtil.sshDisconnect(ssh);
        } catch (Exception e) {
            devopsClusterMapper.deleteByPrimaryKey(clusterId);
            devopsClusterNodeMapper.deleteByClusterId(clusterId);
            throw new CommonException("error.node.check");
        } finally {
            stringRedisTemplate.expire(redisKey, 72L, TimeUnit.DAYS);
        }
    }

    @Override
    public void generateAndUploadNodeConfiguration(SSHClient ssh, Long clusterId, InventoryVO inventoryVO) {
        String configValue = generateInventoryInI(inventoryVO);
        String filePath = String.format(ANSIBLE_CONFIG_BASE_DIR_TEMPLATE, clusterId) + System.getProperty("file.separator") + "inventory.ini";
        String targetFilePath = ANSIBLE_CONFIG_TARGET_BASE_DIR + System.getProperty("file.separator") + "inventory.ini";
        FileUtil.saveDataToFile(filePath, configValue);
        sshUtil.uploadFile(ssh, filePath, targetFilePath);
    }

    private String generateInventoryInI(InventoryVO inventoryVO) {
        Map<String, String> map = new HashMap<>();
        map.put("{{all}}", inventoryVO.getAll().toString());
        map.put("{{etcd}}", inventoryVO.getEtcd().toString());
        map.put("{{kube-master}}", inventoryVO.getKubeMaster().toString());
        map.put("{{kube-worker}}", inventoryVO.getKubeWorker().toString());
        map.put("{{new-master}}", inventoryVO.getNewMaster().toString());
        map.put("{{new-worker}}", inventoryVO.getNewWorker().toString());
        map.put("{{new-etcd}}", inventoryVO.getNewEtcd().toString());
        map.put("{{del-worker}}", inventoryVO.getDelWorker().toString());
        map.put("{{del-master}}", inventoryVO.getDelMaster().toString());
        map.put("{{del-etcd}}", inventoryVO.getDelEtcd().toString());
        map.put("{{del-node}}", inventoryVO.getDelNode().toString());
        InputStream inventoryIniInputStream = DevopsClusterNodeServiceImpl.class.getResourceAsStream("/template/inventory.ini");

        return FileUtil.replaceReturnString(inventoryIniInputStream, map);
    }
}
