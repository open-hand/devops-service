package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.DevopsClusterCommandConstants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.schmizz.sshj.SSHClient;
import org.apache.commons.io.IOUtils;
import org.hzero.core.util.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.DevopsK8sInstallPayload;
import io.choerodon.devops.app.service.DevopsClusterNodeService;
import io.choerodon.devops.infra.constant.ClusterCheckConstant;
import io.choerodon.devops.infra.constant.DevopsClusterCommandConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;
import io.choerodon.devops.infra.enums.CdHostAccountType;
import io.choerodon.devops.infra.enums.ClusterNodeRole;
import io.choerodon.devops.infra.enums.HostSourceEnum;
import io.choerodon.devops.infra.mapper.DevopsClusterNodeMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.SshUtil;

@Service
public class DevopsClusterNodeServiceImpl implements DevopsClusterNodeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsClusterNodeServiceImpl.class);
    private static final InputStream inventoryIniInputStream = DevopsClusterNodeServiceImpl.class.getResourceAsStream("/template/inventory.ini");
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

    private static final String ERROR_DELETE_NODE_FAILED = "error.delete.node.failed";

    /**
     * devops中ansible文件保存目录模板
     */
    private static final String ANSIBLE_CONFIG_BASE_DIR_TEMPLATE = "/choerodon/ansible/%s";
    /**
     * 节点中ansible文件保存目录
     */
    private static final String ANSIBLE_CONFIG_TARGET_BASE_DIR = "/tmp/ansible";
    /**
     * inventory配置文件名称
     */
    private static final String INVENTORY_INI_FILE_NAME = "inventory.ini";
    private static final String INVENTORY_INI_TEMPLATE_FOR_NODE = "%s\n";
    private static final String[] configTypes = new String[]{ALL, ETCD, KUBE_MASTER, KUBE_WORKER, NEW_MASTER, NEW_ETCD, NEW_WORKER, DEL_ETCD, DEL_WORKER, DEL_MASTER, DEL_NODE};
    @Autowired
    private SshUtil sshUtil;
    @Autowired
    private DevopsClusterNodeMapper devopsClusterNodeMapper;

    @Override
    public boolean testConnection(Long projectId, DevopsClusterNodeConnectionTestVO devopsClusterNodeConnectionTestVO) {
        return sshUtil.sshConnect(devopsClusterNodeConnectionTestVO.getHostIp(),
                devopsClusterNodeConnectionTestVO.getSshPort(),
                devopsClusterNodeConnectionTestVO.getAuthType(),
                devopsClusterNodeConnectionTestVO.getUsername(),
                devopsClusterNodeConnectionTestVO.getPassword());
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
        if (ClusterNodeRole.listMasterRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRole.listWorkerRoleSet()) < 2) {
                nodeDeleteCheckVO.setEnableDeleteWorker(false);
            }
        }
        if (ClusterNodeRole.listEtcdRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRole.listEtcdRoleSet()) < 2) {
                nodeDeleteCheckVO.setEnableDeleteEtcd(false);
            }
        }
        if (ClusterNodeRole.listWorkerRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRole.listMasterRoleSet()) < 2) {
                nodeDeleteCheckVO.setEnableDeleteMaster(false);
            }
        }

        return nodeDeleteCheckVO;
    }

    private void checkNodeNumByRole(DevopsClusterNodeDTO devopsClusterNodeDTO) {
        if (ClusterNodeRole.listMasterRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRole.listMasterRoleSet()) < 2) {
                throw new CommonException(ClusterCheckConstant.ERROR_MASTER_NODE_ONLY_ONE);
            }
        }
        if (ClusterNodeRole.listWorkerRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRole.listWorkerRoleSet()) < 2) {
                throw new CommonException(ClusterCheckConstant.ERROR_WORKER_NODE_ONLY_ONE);
            }
        }
        if (ClusterNodeRole.listEtcdRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRole.listWorkerRoleSet()) < 2) {
                throw new CommonException(ClusterCheckConstant.ERROR_ETCD_NODE_ONLY_ONE);
            }
        }
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long nodeId) {
        Assert.notNull(projectId, ResourceCheckConstant.ERROR_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);
        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterNodeDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        checkNodeNumByRole(devopsClusterNodeDTO);

        // 删除数据库中数据
        if (devopsClusterNodeMapper.deleteByPrimaryKey(nodeId) != 1) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_FAILED);
        }

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

        SSHClient sshClient = new SSHClient();
        String configFilePath = UUIDUtils.generateUUID() + ".ini";
        try {
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
        } catch (Exception e) {
            throw new CommonException(ERROR_DELETE_NODE_FAILED, e);
        } finally {
            File file = new File(configFilePath);
            if (file.exists()) {
                file.delete();
            }
            sshUtil.sshDisconnect(sshClient);
        }


    }

    private InventoryVO calculateGeneralInventoryValue(List<DevopsClusterNodeDTO> devopsClusterNodeDTOS) {
        InventoryVO inventoryVO = new InventoryVO();
        for (DevopsClusterNodeDTO node : devopsClusterNodeDTOS) {
            // 设置所有节点
            if (CdHostAccountType.ACCOUNTPASSWORD.value().equals(node.getAuthType())) {
                inventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL, node.getName(), node.getHostIp(), node.getSshPort(), node.getUsername(), node.getPassword()))
                        .append(System.lineSeparator());
            } else {
                //todo 处理密钥认证方式
            }
            // 设置master节点
            if (ClusterNodeRole.listMasterRoleSet().contains(node.getRole())) {
                inventoryVO.getKubeMaster().append(node.getName())
                        .append(System.lineSeparator());
            }
            // 设置etcd节点
            if (ClusterNodeRole.listEtcdRoleSet().contains(node.getRole())) {
                inventoryVO.getEtcd().append(node.getName())
                        .append(System.lineSeparator());
            }
            // 设置worker节点
            if (ClusterNodeRole.listWorkerRoleSet().contains(node.getRole())) {
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
        if (Boolean.FALSE.equals(ClusterNodeRole.isWorker(devopsClusterNodeDTO.getRole()))) {
            nodeRoleDeleteCheckVO.setEnableDeleteRole(true);
        }

        if (Boolean.TRUE.equals(ClusterNodeRole.listMasterRoleSet().contains(devopsClusterNodeDTO.getRole()))) {
            nodeRoleDeleteCheckVO.setEnableDeleteMasterRole(true);
        }
        if (Boolean.TRUE.equals(ClusterNodeRole.listEtcdRoleSet().contains(devopsClusterNodeDTO.getRole()))) {
            nodeRoleDeleteCheckVO.setEnableDeleteEtcdRole(true);
        }
        return nodeRoleDeleteCheckVO;
    }

    @Override
    @Transactional
    public void deleteRole(Long projectId, Long nodeId, Set<Integer> roles) {
        Assert.notNull(projectId, ResourceCheckConstant.ERROR_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);
        Assert.notNull(roles, ClusterCheckConstant.ERROR_ROLE_ID_IS_NULL);

        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);

        // 删除校验
        checkEnableDeleteRole(devopsClusterNodeDTO, roles);
        // 删除数据库数据
        int resultRole = devopsClusterNodeDTO.getRole();
        for (Integer role : roles) {
            resultRole = resultRole - role;
        }

        devopsClusterNodeDTO.setRole(resultRole == 0 ? 1 : resultRole);
        if (devopsClusterNodeMapper.updateByPrimaryKey(devopsClusterNodeDTO) != 1) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
        }
        // 删除节点角色
        // 删除集群中的node
        // 1. 查询集群节点信息
        DevopsClusterNodeDTO record = new DevopsClusterNodeDTO();
        record.setClusterId(devopsClusterNodeDTO.getClusterId());
        List<DevopsClusterNodeDTO> devopsClusterNodeDTOS = devopsClusterNodeMapper.select(record);


        roles.forEach(role -> {
            // 计算invertory配置
            InventoryVO inventoryVO = calculateGeneralInventoryValue(devopsClusterNodeDTOS);
            String command = null;
            if (ClusterNodeRole.isMaster(role)) {
                inventoryVO.getDelMaster().append(devopsClusterNodeDTO.getName());
                command = DevopsClusterCommandConstants.REMOVE_MASTER_YAML;
            }
            if (ClusterNodeRole.isEtcd(role)) {
                inventoryVO.getDelEtcd().append(devopsClusterNodeDTO.getName());
                command = DevopsClusterCommandConstants.REMOVE_ETCD_YAML;
            }


            // 连接主机
            HostConnectionVO hostConnectionVO = ConvertUtils.convertObject(devopsClusterNodeDTOS.get(0), HostConnectionVO.class);
            hostConnectionVO.setHostSource(HostSourceEnum.CUSTOMHOST.getValue());

            SSHClient sshClient = new SSHClient();
            String configFilePath = UUIDUtils.generateUUID() + ".ini";
            try {
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
            } catch (Exception e) {
                throw new CommonException(ERROR_DELETE_NODE_FAILED, e);
            } finally {
                File file = new File(configFilePath);
                if (file.exists()) {
                    file.delete();
                }
                sshUtil.sshDisconnect(sshClient);
            }
        });


    }

    private void checkEnableDeleteRole(DevopsClusterNodeDTO devopsClusterNodeDTO, Set<Integer> roles) {
        roles.forEach(role -> {
            if (ClusterNodeRole.isWorker(role)) {
                throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
            }
            if (ClusterNodeRole.isEtcd(role)
                    && Boolean.FALSE.equals(ClusterNodeRole.listEtcdRoleSet().contains(devopsClusterNodeDTO.getRole()))) {
                throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
            }
            if (ClusterNodeRole.isMaster(role)
                    && Boolean.FALSE.equals(ClusterNodeRole.listMasterRoleSet().contains(devopsClusterNodeDTO.getRole()))) {
                throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
            }
        });
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
            // TODO 保存错误信息
        } catch (Exception e) {
            throw new CommonException("error.install.k8s", e.getMessage());
        }
    }

    @Override
    public void execCommand(SSHClient ssh, String command) {
        try {
            ExecResultInfoVO resultInfoVO = sshUtil.execCommand(ssh, command);
            if (resultInfoVO != null) {
                throw new CommonException("error.node.install.docker", ssh.getRemoteHostname(), resultInfoVO.getStdErr());
            }
        } catch (Exception e) {
            throw new CommonException("error.node.install.docker", ssh.getRemoteHostname(), e.getMessage());
        }
    }

    @Async
    @Override
    public void checkNode(Long clusterId, List<DevopsClusterNodeDTO> devopsClusterNodeDTOList, HostConnectionVO hostConnectionVO) {
        SSHClient ssh = new SSHClient();
        try {

            sshUtil.sshConnect(hostConnectionVO, ssh);
            // 安装docker
            execCommand(ssh, DOCKER_INSTALL_COMMAND);
            // 生成相关配置节点
            InventoryVO inventoryVO = calculateGeneralInventoryValue(devopsClusterNodeDTOList);
            // 上传配置文件
            generateAndUploadNodeConfiguration(ssh, clusterId, inventoryVO);
            // 异步执行检测命令
            asyncCommand(ssh, String.format(ANSIBLE_COMMAND_TEMPLATE, CHECK_NODE));
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
    }

    @Async
    public void asyncCommand(SSHClient ssh, String command) {
        try {
            sshUtil.execCommand(ssh, command);
        } catch (IOException e) {
            sshUtil.sshDisconnect(ssh);
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

        return FileUtil.replaceReturnString(inventoryIniInputStream, map);
    }
}
