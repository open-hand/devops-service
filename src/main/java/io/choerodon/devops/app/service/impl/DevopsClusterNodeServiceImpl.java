package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.schmizz.sshj.SSHClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
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
        // todo 删除集群中的node
        // 1. 查询集群节点信息
        DevopsClusterNodeDTO record = new DevopsClusterNodeDTO();
        record.setClusterId(devopsClusterNodeDTO.getClusterId());
        List<DevopsClusterNodeDTO> devopsClusterNodeDTOS = devopsClusterNodeMapper.select(record);

        InventoryVO inventoryVO = calculateGeneralInvertoryValue(devopsClusterNodeDTOS);
        inventoryVO.getDelNode().append(devopsClusterNodeDTO.getName());

        HostConnectionVO hostConnectionVO = ConvertUtils.convertObject(devopsClusterNodeDTO, HostConnectionVO.class);
        hostConnectionVO.setHostSource(HostSourceEnum.CUSTOMHOST.getValue());
        SSHClient sshClient = new SSHClient();
        try {
            sshUtil.sshConnect(hostConnectionVO, sshClient);
            sshUtil.uploadFile(sshClient, generateInventoryInI(inventoryVO), INVENTORY_CONFIG_FILE_PATH);
            sshUtil.execCommand(sshClient, DevopsClusterCommandConstants.ANSIBLE_COMMAND_TEMPLATE);
        } catch (IOException e) {

        } finally {

        }

        // 删除数据库中数据
        if (devopsClusterNodeMapper.deleteByPrimaryKey(nodeId) != 1) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_FAILED);
        }
    }

    private InventoryVO calculateGeneralInvertoryValue(List<DevopsClusterNodeDTO> devopsClusterNodeDTOS) {
        InventoryVO inventoryVO = new InventoryVO();
        for (DevopsClusterNodeDTO node : devopsClusterNodeDTOS) {
            if (CdHostAccountType.ACCOUNTPASSWORD.value().equals(node.getAuthType())) {
                inventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL, node.getName(), node.getHostIp(), node.getSshPort(), node.getUsername(), node.getPassword()))
                        .append(System.lineSeparator());
            } else {
                //todo 处理密钥认证方式
            }
            if (ClusterNodeRole.listMasterRoleSet().contains(node.getRole())) {
                inventoryVO.getKubeMaster().append(node.getName())
                        .append(System.lineSeparator());
            }
            if (ClusterNodeRole.listEtcdRoleSet().contains(node.getRole())) {
                inventoryVO.getEtcd().append(node.getName())
                        .append(System.lineSeparator());
            }
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
    public void deleteRole(Long projectId, Long nodeId, Integer role) {
        Assert.notNull(projectId, ResourceCheckConstant.ERROR_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);
        Assert.notNull(role, ClusterCheckConstant.ERROR_ROLE_ID_IS_NULL);

        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);

        // 删除校验
        checkEnableDeleteRole(devopsClusterNodeDTO, role);

        // 删除节点角色

        // 删除数据库数据
        int resultRole = devopsClusterNodeDTO.getRole() - role;

        devopsClusterNodeDTO.setRole(resultRole == 0 ? 1 : resultRole);
        if (devopsClusterNodeMapper.updateByPrimaryKey(devopsClusterNodeDTO) != 1) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
        }
    }

    private void checkEnableDeleteRole(DevopsClusterNodeDTO devopsClusterNodeDTO, Integer role) {
        if (ClusterNodeRole.listWorkerRoleSet().contains(role)) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
        }
        if (ClusterNodeRole.listEtcdRoleSet().contains(role)
                && Boolean.FALSE.equals(ClusterNodeRole.listEtcdRoleSet().contains(devopsClusterNodeDTO.getRole()))) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
        }
        if (ClusterNodeRole.listMasterRoleSet().contains(role)
                && Boolean.FALSE.equals(ClusterNodeRole.listMasterRoleSet().contains(devopsClusterNodeDTO.getRole()))) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
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
    public void checkNode(SSHClient ssh) {
    }

    @Override
    public void uploadNodeConfiguration(SSHClient ssh, List<DevopsClusterNodeVO> devopsClusterNodeVOList) {

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
        map.put("{{del-master}}", inventoryVO.getDelWorker().toString());
        map.put("{{del-etcd}}", inventoryVO.getDelEtcd().toString());
        map.put("{{del-node}}", inventoryVO.getDelNode().toString());

        return FileUtil.replaceReturnString(inventoryIniInputStream, map);
    }

//    private Map<String, String> generateNodeConfig(Map<String, List<DevopsClusterNodeVO>> devopsClusterNodeVOListConfigMap) {
//        Map<String, String> map = new HashMap<>();
//        List<DevopsClusterNodeVO> all = devopsClusterNodeVOListConfigMap.get(ALL);
//        if (CollectionUtils.isEmpty(all)) {
//            throw new CommonException("error.node.info.empty");
//        }
//    }
}
