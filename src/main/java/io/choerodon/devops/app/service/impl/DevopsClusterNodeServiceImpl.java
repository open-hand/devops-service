package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.DevopsAnsibleCommandConstants.*;
import static org.hzero.core.util.StringPool.SLASH;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import org.hzero.core.util.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.DevopsAddNodePayload;
import io.choerodon.devops.app.eventhandler.payload.DevopsClusterInstallInfoVO;
import io.choerodon.devops.app.eventhandler.payload.DevopsClusterInstallPayload;
import io.choerodon.devops.app.service.DevopsClusterNodeOperatorService;
import io.choerodon.devops.app.service.DevopsClusterNodeService;
import io.choerodon.devops.app.service.DevopsClusterOperatingRecordService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.infra.constant.ClusterCheckConstant;
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
    /**
     * 节点检查进度redis的key
     */
    public static final String NODE_CHECK_STEP_REDIS_KEY_TEMPLATE = "node-check-step-%d-%s";
    /**
     * 集群安装信息保存在redis的key, cluster-info-${projectId}-${集群code}
     */
    public static final String CLUSTER_INFO_REDIS_KEY_TEMPLATE = "cluster-info-%d-%s";
    private static final String ERROR_DELETE_NODE_FAILED = "devops.delete.node.failed";
    private static final String ERROR_ADD_NODE_FAILED = "devops.add.node.failed";
    private static final String ERROR_ADD_NODE_ROLE_FAILED = "devops.add.node.role.failed";
    private static final String CLUSTER_STATUS_SYNC_REDIS_LOCK = "cluster-status-sync-lock";
    private static final Integer MAX_LOG_MSG_LENGTH = 65535;

    @Value(value = "${devops.helm.download-url}")
    private String helmDownloadUrl;
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
    @Lazy
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private DevopsClusterNodeOperatorService devopsClusterNodeOperatorService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private DevopsClusterOperatingRecordService devopsClusterOperatingRecordService;

    @Override
    @Transactional
    public void baseSave(DevopsClusterNodeDTO devopsClusterNodeDTO) {
        if (devopsClusterNodeMapper.insertSelective(devopsClusterNodeDTO) != 1) {
            throw new CommonException(ERROR_ADD_NODE_FAILED);
        }
    }

    @Override
    @Transactional
    public void baseUpdateNodeRole(Long id, Integer role) {
        Assert.notNull(id, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);
        Assert.notNull(role, ClusterCheckConstant.ERROR_ROLE_ID_IS_NULL);

        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(id);
        devopsClusterNodeDTO.setRole(role);
        if (devopsClusterNodeMapper.updateByPrimaryKeySelective(devopsClusterNodeDTO) != 1) {
            throw new CommonException(ERROR_ADD_NODE_ROLE_FAILED);
        }
    }

    @Override
    public boolean testConnection(Long projectId, ClusterHostConnectionVO hostConnectionVO) {
        String password = hostConnectionVO.getPassword();
        if (hostConnectionVO.getAuthType().equalsIgnoreCase(HostAuthType.PUBLICKEY.value())) {
            password = Base64Util.getBase64DecodedString(password);
        }
        return SshUtil.sshConnectForOK(hostConnectionVO.getHostIp(),
                hostConnectionVO.getHostPort(),
                hostConnectionVO.getAuthType(),
                hostConnectionVO.getUsername(),
                password);
    }

    @Override
    public void batchInsert(List<DevopsClusterNodeDTO> devopsClusterNodeDTOList) {
        int size = devopsClusterNodeDTOList.size();
        if (devopsClusterNodeMapper.batchInsert(devopsClusterNodeDTOList) != size) {
            throw new CommonException("devops.batch.insert.node");
        }
    }

    @Override
    public void deleteByClusterId(Long clusterId) {
        DevopsClusterNodeDTO devopsClusterNodeDTO = new DevopsClusterNodeDTO();
        devopsClusterNodeDTO.setClusterId(clusterId);
        devopsClusterNodeMapper.delete(devopsClusterNodeDTO);
    }

    @Override
    public NodeDeleteCheckVO checkEnableDelete(Long projectId, Long nodeId) {
        Assert.notNull(projectId, ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);

        NodeDeleteCheckVO nodeDeleteCheckVO = new NodeDeleteCheckVO();
        // 查询节点类型
        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);
        if (ClusterNodeRoleEnum.listMasterRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listMasterRoleSet()) < 2) {
                nodeDeleteCheckVO.setEnableDeleteMaster(false);
            }
        }
        if (ClusterNodeRoleEnum.listEtcdRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listEtcdRoleSet()) < 2) {
                nodeDeleteCheckVO.setEnableDeleteEtcd(false);
            }
        }
        if (ClusterNodeRoleEnum.listWorkerRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            if (devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listWorkerRoleSet()) < 2) {
                nodeDeleteCheckVO.setEnableDeleteWorker(false);
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
    @Transactional
    public Long delete(Long projectId, Long nodeId) {
        Assert.notNull(projectId, ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);


        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterNodeDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        checkNodeNumByRole(devopsClusterNodeDTO);

        // 更新集群操作状态为
        devopsClusterService.updateClusterStatusToOperating(devopsClusterNodeDTO.getClusterId());
        DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO = devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                devopsClusterNodeDTO.getId(),
                ClusterOperatingTypeEnum.DELETE_NODE.value(),
                ClusterOperationStatusEnum.OPERATING.value(),
                null);
        devopsClusterNodeOperatorService.deleteNode(projectId, devopsClusterNodeDTO, devopsClusterOperationRecordDTO.getId());

        return devopsClusterOperationRecordDTO.getId();
    }

    @Override
    public K8sInventoryVO calculateGeneralInventoryValue(List<DevopsClusterNodeDTO> devopsClusterNodeDTOS) {
        K8sInventoryVO k8sInventoryVO = new K8sInventoryVO();
        for (DevopsClusterNodeDTO node : devopsClusterNodeDTOS) {
            if (node.getType().equalsIgnoreCase(ClusterNodeTypeEnum.INNER.getType())) {// 设置所有节点
                if (HostAuthType.ACCOUNTPASSWORD.value().equals(node.getAuthType())) {
                    k8sInventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL_PSW_TYPE, node.getName(), node.getHostIp(), node.getHostPort(), node.getUsername(), node.getPassword()))
                            .append(System.lineSeparator());
                } else {
                    k8sInventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL_PRIVATE_KEY_TYPE, node.getName(), node.getHostIp(), node.getHostPort(), node.getUsername(), String.format(PRIVATE_KEY_SAVE_PATH_TEMPLATE, node.getName())))
                            .append(System.lineSeparator());
                }
                // 设置master节点
                if (ClusterNodeRoleEnum.listMasterRoleSet().contains(node.getRole())) {
                    k8sInventoryVO.getKubeMaster().append(node.getName())
                            .append(System.lineSeparator());
                }
                // 设置etcd节点
                if (ClusterNodeRoleEnum.listEtcdRoleSet().contains(node.getRole())) {
                    k8sInventoryVO.getEtcd().append(node.getName())
                            .append(System.lineSeparator());
                }
                // 设置worker节点
                if (ClusterNodeRoleEnum.listWorkerRoleSet().contains(node.getRole())) {
                    k8sInventoryVO.getKubeWorker().append(node.getName())
                            .append(System.lineSeparator());
                }
            }
        }
        return k8sInventoryVO;
    }

    @Override
    @Transactional
    public void baseDelete(Long id) {
        Assert.notNull(id, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);
        if (devopsClusterNodeMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(ERROR_DELETE_NODE_FAILED);
        }
    }

    @Override
    public Boolean checkEnableDeleteRole(Long projectId, Long nodeId, Integer role) {
        Assert.notNull(projectId, ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);
        Assert.notNull(role, ClusterCheckConstant.ERROR_ROLE_ID_IS_NULL);

        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);

        if (ClusterNodeRoleEnum.MASTER.getMask() == role
                && devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listMasterRoleSet()) > 1) {
            return true;
        } else if (ClusterNodeRoleEnum.ETCD.getMask() == role
                && devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listEtcdRoleSet()) > 1) {
            return true;
        } else if (ClusterNodeRoleEnum.WORKER.getMask() == role
                && devopsClusterNodeMapper.countByRoleSet(devopsClusterNodeDTO.getClusterId(), ClusterNodeRoleEnum.listWorkerRoleSet()) > 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public void baseAddNodeRole(Long id, Integer role) {
        Assert.notNull(id, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);
        Assert.notNull(id, ClusterCheckConstant.ERROR_ROLE_ID_IS_NULL);

        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(id);
        if (ClusterNodeRoleEnum.MASTER.getMask() == role
                && ClusterNodeRoleEnum.listMasterRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            return;
        }
        if (ClusterNodeRoleEnum.ETCD.getMask() == role
                && ClusterNodeRoleEnum.listEtcdRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            return;
        }

        if (ClusterNodeRoleEnum.WORKER.getMask() == role
                && ClusterNodeRoleEnum.listWorkerRoleSet().contains(devopsClusterNodeDTO.getRole())) {
            return;
        }
        devopsClusterNodeDTO.setRole(devopsClusterNodeDTO.getRole() + role);
        if (devopsClusterNodeMapper.updateByPrimaryKeySelective(devopsClusterNodeDTO) != 1) {
            throw new CommonException(ERROR_ADD_NODE_ROLE_FAILED);
        }

    }

    @Override
    public NodeOperatingResultVO checkOperatingResult(Long projectId, Long operationRecordId) {
        DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO = devopsClusterOperatingRecordService.queryById(operationRecordId);
        NodeOperatingResultVO nodeOperatingResultVO = new NodeOperatingResultVO();
        nodeOperatingResultVO.setStatus(devopsClusterOperationRecordDTO.getStatus());
        nodeOperatingResultVO.setErrorMsg(devopsClusterOperationRecordDTO.getErrorMsg());
        return nodeOperatingResultVO;
    }

    @Override
    @Transactional
    public Long deleteRole(Long projectId, Long nodeId, Integer role) {
        Assert.notNull(projectId, ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL);
        Assert.notNull(nodeId, ClusterCheckConstant.ERROR_NODE_ID_IS_NULL);
        Assert.notNull(role, ClusterCheckConstant.ERROR_ROLE_ID_IS_NULL);

        DevopsClusterNodeDTO devopsClusterNodeDTO = devopsClusterNodeMapper.selectByPrimaryKey(nodeId);

        // 删除校验
        checkEnableDeleteRole(devopsClusterNodeDTO, role);

        // 更新集群操作状态为operating
        devopsClusterService.updateClusterStatusToOperating(devopsClusterNodeDTO.getClusterId());
        DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO = devopsClusterOperatingRecordService.saveOperatingRecord(devopsClusterNodeDTO.getClusterId(),
                devopsClusterNodeDTO.getId(),
                ClusterOperatingTypeEnum.DELETE_NODE_ROLE.value(),
                ClusterOperationStatusEnum.OPERATING.value(),
                null);

        devopsClusterNodeOperatorService.deleteNodeRole(projectId, devopsClusterNodeDTO, role, devopsClusterOperationRecordDTO.getId());

        return devopsClusterOperationRecordDTO.getId();

    }

    private void checkEnableDeleteRole(DevopsClusterNodeDTO devopsClusterNodeDTO, Integer roleId) {
        if (ClusterNodeRoleEnum.WORKER.getMask() == roleId) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
        }
        if (ClusterNodeRoleEnum.ETCD.getMask() == roleId
                && Boolean.FALSE.equals(ClusterNodeRoleEnum.isEtcd(devopsClusterNodeDTO.getRole()))) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
        }
        if (ClusterNodeRoleEnum.MASTER.getMask() == roleId
                && Boolean.FALSE.equals(ClusterNodeRoleEnum.isMaster(devopsClusterNodeDTO.getRole()))) {
            throw new CommonException(ClusterCheckConstant.ERROR_DELETE_NODE_ROLE_FAILED);
        }
    }

    @Override
    public void executeInstallK8sInBackground(DevopsClusterInstallPayload devopsClusterInstallPayload) {
        DevopsClusterOperationRecordDTO record = devopsClusterOperationRecordMapper.selectByPrimaryKey(devopsClusterInstallPayload.getOperationRecordId());
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(devopsClusterInstallPayload.getClusterId());
        List<DevopsClusterNodeDTO> devopsClusterNodeDTOList = devopsClusterNodeMapper.listByClusterId(devopsClusterInstallPayload.getClusterId());

        HostConnectionVO hostConnectionVO;
        // 获得外部连接节点
        List<DevopsClusterNodeDTO> outterNode = devopsClusterNodeDTOList.stream().filter(n -> n.getType().equalsIgnoreCase(ClusterNodeTypeEnum.OUTTER.getType())).collect(Collectors.toList());
        // 如果外部连接节点存在，取外部节点。否则默认取第1个节点
        if (!CollectionUtils.isEmpty(outterNode)) {
            hostConnectionVO = ConvertUtils.convertObject(outterNode.get(0), HostConnectionVO.class);
        } else {
            hostConnectionVO = ConvertUtils.convertObject(devopsClusterNodeDTOList.get(0), HostConnectionVO.class);
        }

        SSHClient ssh = new SSHClient();
        try {
            LOGGER.info(">>>>>>>>> [install k8s] clusterId {} :start to create ssh connection object <<<<<<<<<", devopsClusterInstallPayload.getClusterId());
            sshUtil.sshConnect(ConvertUtils.convertObject(hostConnectionVO, HostConnectionVO.class), ssh);
            // 检查集群是否安装成功，该情况是如果集群安装成功，但是saga失败导致数据没有更新，防止saga重试使得集群被重新安装。如果成功，此次saga任务成功
            if (checkInstallSuccess(ssh, record, devopsClusterDTO)) {
                return;
            }
            // 生成并上传配置
            K8sInventoryVO k8sInventoryVO = calculateGeneralInventoryValue(devopsClusterNodeDTOList);
            generateAndUploadNodeConfiguration(ssh, devopsClusterDTO.getCode(), k8sInventoryVO);
            // 生成并上传k8s安装命令
            generateAndUploadAnsibleShellScript(ssh, devopsClusterDTO.getCode(), INSTALL_K8S, INSTALL_K8S_LOG, String.format(EXIT_CODE_FILE_TEMPLATE, devopsClusterDTO.getCode()));
            // 上传privateKey信息到节点
            generateAndUploadPrivateKey(ssh, devopsClusterNodeDTOList);
            LOGGER.info(">>>>>>>>> [install k8s] clusterId {} :execute install command in background <<<<<<<<<", devopsClusterInstallPayload.getClusterId());
            ExecResultInfoVO resultInfoVO = sshUtil.execCommand(ssh, String.format(BACKGROUND_COMMAND_TEMPLATE, INSTALL_K8S_SHELL, BASH_LOG_OUTPUT));
            // 集群安装出现错误，设置错误消息并更新集群状态
            if (resultInfoVO.getExitCode() != 0) {
                record.setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .appendErrorMsg(resultInfoVO.getStdOut() + "\n" + resultInfoVO.getStdErr());
                devopsClusterDTO.setStatus(ClusterStatusEnum.FAILED.value());
            }
            LOGGER.info(">>>>>>>>> [install k8s] clusterId {} :waiting for installing completed<<<<<<<<<", devopsClusterInstallPayload.getClusterId());
        } catch (Exception e) {
            record.setStatus(ClusterOperationStatusEnum.FAILED.value())
                    .appendErrorMsg(e.getMessage());
            devopsClusterDTO.setStatus(ClusterStatusEnum.FAILED.value());
            if (e instanceof TransportException) {
                LOGGER.info(">>>>>>>>> [install k8s] clusterId {} : ssh connection disconnect ,host: [ {} ] <<<<<<<<<", devopsClusterInstallPayload.getClusterId(), ssh.getRemoteHostname());
            } else {
                LOGGER.error(">>>>>>>>> [install k8s] clusterId {} :failed to install ,error: {}<<<<<<<<<", devopsClusterInstallPayload.getClusterId(), e);
            }
        } finally {
            devopsClusterOperationRecordMapper.updateByPrimaryKeySelective(record);
            devopsClusterMapper.updateByPrimaryKeySelective(devopsClusterDTO);
            sshUtil.sshDisconnect(ssh);
        }
    }

    private boolean checkInstallSuccess(SSHClient ssh, DevopsClusterOperationRecordDTO record, DevopsClusterDTO devopsClusterDTO) throws Exception {
        ExecResultInfoVO resultInfoVO = sshUtil.execCommand(ssh, String.format(CAT_FILE, String.format(EXIT_CODE_FILE_TEMPLATE, devopsClusterDTO.getCode())));
        if (resultInfoVO.getExitCode() != 0) {
            if (resultInfoVO.getStdErr().contains("No such file or directory")) {
                LOGGER.info(">>>>>>>>> [install k8s] installation of cluster 【{}】 is not completed <<<<<<<<<", devopsClusterDTO.getName());
            }
            return false;
        } else {
            if ("0".equals(resultInfoVO.getStdOut().replaceAll("\r|\n", ""))) {
                // k8s安装成功
                LOGGER.info(">>>>>>>>> [install k8s] cluster [ {} ] operation [ {} ] install success <<<<<<<<<", devopsClusterDTO.getId(), record.getId());
                record.setStatus(ClusterOperationStatusEnum.SUCCESS.value());
                devopsClusterDTO.setStatus(ClusterStatusEnum.DISCONNECT.value());
                // 安装agent, 第一步安装helm ，第二步安装agent。这一步骤如果出现错误,只保存错误信息
                installAgent(devopsClusterDTO, record, ssh);
                return true;
            } else {
                record.setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .appendErrorMsg(resultInfoVO.getStdOut() + "\n" + resultInfoVO.getStdErr());
                devopsClusterDTO.setStatus(ClusterStatusEnum.FAILED.value());
                return false;
            }
        }
    }

    private void installAgent(DevopsClusterDTO devopsClusterDTO, DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO, SSHClient ssh) {
        try {
            ExecResultInfoVO helmInstallResult = sshUtil.execCommand(ssh, String.format(INSTALL_HELM_TEMPLATE, helmDownloadUrl));
            if (helmInstallResult.getExitCode() != 0) {
                devopsClusterOperationRecordDTO.appendErrorMsg(helmInstallResult.getStdOut() + "\n" + helmInstallResult.getStdErr());
            }
            String agentInstallCommand = devopsClusterService.getInstallString(devopsClusterDTO, "");
            ExecResultInfoVO agentInstallResult = sshUtil.execCommand(ssh, agentInstallCommand);
            if (agentInstallResult.getExitCode() != 0) {
                devopsClusterOperationRecordDTO.appendErrorMsg(agentInstallResult.getStdOut() + "\n" + agentInstallResult.getStdErr());
            }
        } catch (Exception e) {
            devopsClusterOperationRecordDTO.appendErrorMsg(e.getMessage());
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
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_CLUSTER_ADD_NODE, description = "添加集群节点", inputSchemaClass = DevopsAddNodePayload.class)
    public void addNode(Long projectId, Long clusterId, DevopsClusterNodeVO nodeVO) {
        Assert.notNull(projectId, ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL);
        Assert.notNull(clusterId, ClusterCheckConstant.ERROR_CLUSTER_ID_IS_NULL);
        nodeVO.setProjectId(projectId);

        // 更新集群操作状态为operating
        DevopsAddNodePayload devopsAddNodePayload = new DevopsAddNodePayload();
        devopsAddNodePayload.setProjectId(projectId);
        devopsAddNodePayload.setClusterId(clusterId);
        devopsAddNodePayload.setNodeVO(nodeVO);
        devopsAddNodePayload.setOperatingId(UUIDUtils.generateUUID());
        devopsClusterService.updateClusterStatusToOperating(clusterId);
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(projectId)
                        .withRefType("cluster")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CLUSTER_ADD_NODE),
                builder -> builder
                        .withPayloadAndSerialize(devopsAddNodePayload)
                        .withRefId(String.valueOf(clusterId)));
    }

    @Override
    public DevopsClusterNodeDTO queryByClusterIdAndNodeName(Long clusterId, String nodeName) {
        Assert.notNull(clusterId, ClusterCheckConstant.ERROR_CLUSTER_ID_IS_NULL);
        Assert.notNull(nodeName, ClusterCheckConstant.ERROR_NODE_NAME_IS_NULL);
        DevopsClusterNodeDTO devopsClusterNodeDTO = new DevopsClusterNodeDTO();
        devopsClusterNodeDTO.setClusterId(clusterId);
        devopsClusterNodeDTO.setName(nodeName);
        return devopsClusterNodeMapper.selectOne(devopsClusterNodeDTO);
    }

    @Override
    public List<DevopsClusterNodeDTO> queryNodeByClusterIdAndType(Long clusterId, ClusterNodeTypeEnum type) {
        Assert.notNull(clusterId, ClusterCheckConstant.ERROR_CLUSTER_ID_IS_NULL);
        Assert.notNull(type, ClusterCheckConstant.ERROR_CLUSTER_TYPE_IS_OPERATING);
        DevopsClusterNodeDTO devopsClusterNodeDTO = new DevopsClusterNodeDTO();
        devopsClusterNodeDTO.setClusterId(clusterId);
        devopsClusterNodeDTO.setType(type.getType());
        return devopsClusterNodeMapper.select(devopsClusterNodeDTO);
    }


    @Override
    public Long saveInfo(List<DevopsClusterNodeDTO> devopsClusterDTOList, Long projectId, DevopsClusterReqVO devopsClusterReqVO) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.insertClusterInfo(projectId, devopsClusterReqVO, ClusterTypeEnum.CREATED.value());
        List<DevopsClusterNodeDTO> devopsClusterNodeDTOS = devopsClusterDTOList.stream()
                .peek(n -> {
                    n.setClusterId(devopsClusterDTO.getId());
                    n.setProjectId(projectId);
                })
                .collect(Collectors.toList());
        batchInsert(devopsClusterNodeDTOS);
        return devopsClusterDTO.getId();
    }

    @Override
    public DevopsClusterInstallPayload checkAndSaveNode(DevopsClusterInstallPayload devopsClusterInstallPayload) {
        // 项目id
        Long projectId = devopsClusterInstallPayload.getProjectId();
        // redisKey
        String redisKey = devopsClusterInstallPayload.getRedisKey();
        String clusterInfoRedisKey = devopsClusterInstallPayload.getClusterInfoRedisKey();

        String clusterInstallInfoRaw = stringRedisTemplate.opsForValue().get(clusterInfoRedisKey);
        if (StringUtils.isEmpty(clusterInstallInfoRaw)) {
            throw new CommonException("the cluster info in redis is missing");
        }

        DevopsClusterInstallInfoVO devopsClusterInstallInfoVO = JsonHelper.unmarshalByJackson(clusterInstallInfoRaw, DevopsClusterInstallInfoVO.class);

        SSHClient ssh = new SSHClient();
        DevopsNodeCheckResultVO devopsNodeCheckResultVO = new DevopsNodeCheckResultVO();
        try {
            try {
                LOGGER.info(">>>>>>>>> [check node] key {} :start to create ssh connection object <<<<<<<<<", redisKey);
                sshUtil.sshConnect(devopsClusterInstallInfoVO.getHostConnectionVO(), ssh);
            } catch (IOException e) {
                throw new Exception(String.format(">>>>>>>>> [check node] failed to connect to host: [ %s ] by ssh <<<<<<<<<", devopsClusterInstallInfoVO.getHostConnectionVO().getHostIp()));
            }
            // 安装基础环境、git、ansible
            try {
                LOGGER.info(">>>>>>>>> [check node] key {} :initialize the environment <<<<<<<<<", redisKey);
                sshUtil.uploadPreProcessShell(ssh, devopsClusterInstallInfoVO.getDevopsClusterReqVO().getCode());
                ExecResultInfoVO resultInfoVO = sshUtil.execCommand(ssh, String.format(BASH_COMMAND_TEMPLATE, PRE_KUBEADM_HA_SH));
                if (resultInfoVO != null && resultInfoVO.getExitCode() != 0) {
                    throw new Exception(String.format(">>>>>>>>> [check node] failed to initialize the environment on host: [ %s ],error is :%s <<<<<<<<<", ssh.getRemoteHostname(), resultInfoVO.getStdErr()));
                }
            } catch (IOException e) {
                throw new Exception(String.format(">>>>>>>>> [check node] failed to initialize the environment on host: [ %s ],error is :%s <<<<<<<<<", ssh.getRemoteHostname(), e.getMessage()));
            }
            // 生成相关配置节点
            K8sInventoryVO k8sInventoryVO = calculateGeneralInventoryValue(devopsClusterInstallInfoVO.getDevopsClusterNodeToSaveDTOList());
            // 上传配置文件
            generateAndUploadNodeConfiguration(ssh, devopsClusterInstallInfoVO.getDevopsClusterReqVO().getCode(), k8sInventoryVO);
            // 保存privateKey到节点
            generateAndUploadPrivateKey(ssh, devopsClusterInstallInfoVO.getDevopsClusterNodeToSaveDTOList());
            // 执行检测命令
            LOGGER.info(">>>>>>>>> [check node] start to check node <<<<<<<<<");
            // 检查节点，如果返回错误，抛出错误
            String errorMsg = checkAndSave(ssh, devopsNodeCheckResultVO, redisKey);
            if (!StringUtils.isEmpty(errorMsg)) {
                throw new Exception(errorMsg);
            }
            LOGGER.info(">>>>>>>>> [check node] check node complete <<<<<<<<<");
            // 节点检查通过，保存节点信息
            Long clusterId = saveInfo(devopsClusterInstallInfoVO.getDevopsClusterNodeToSaveDTOList(), projectId, devopsClusterInstallInfoVO.getDevopsClusterReqVO());
            devopsClusterInstallPayload.setClusterId(clusterId);
            return devopsClusterInstallPayload;
        } catch (Exception e) {
            devopsNodeCheckResultVO.setErrorMsg(e.getMessage())
                    .setStatus(ClusterOperationStatusEnum.FAILED.value());
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
            throw new CommonException(e.getMessage());
        } finally {
            // 这里不直接删除redisKey而是设置过期时间的原因是删除该key后，前端就无法通过/check_progress获取节点检查进度了
            stringRedisTemplate.expire(redisKey, 3, TimeUnit.MINUTES);
            stringRedisTemplate.delete(clusterInfoRedisKey);
            sshUtil.sshDisconnect(ssh);
        }
    }

    public String checkAndSave(SSHClient ssh, DevopsNodeCheckResultVO devopsNodeCheckResultVO, String redisKey) {
        try {
            String errorMsg;
            // 配置检查
            devopsNodeCheckResultVO.getConfiguration().setStatus(ClusterOperationStatusEnum.OPERATING.value());
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
            ExecResultInfoVO resultInfoVOForVariable = sshUtil.execCommand(ssh, String.format(K8S_ANSIBLE_COMMAND_TEMPLATE, VARIABLE));
            if (resultInfoVOForVariable.getExitCode() != 0) {
                errorMsg = resultInfoVOForVariable.getStdOut() + "\n" + resultInfoVOForVariable.getStdErr();
                devopsNodeCheckResultVO.setStatus(CommandStatus.FAILED.getStatus());
                devopsNodeCheckResultVO.getConfiguration().setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .setErrorMessage(errorMsg);
                stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
                return errorMsg;
            } else {
                devopsNodeCheckResultVO.getConfiguration().setStatus(ClusterOperationStatusEnum.SUCCESS.value());
            }
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));

            // 节点系统检查
            devopsNodeCheckResultVO.getSystem().setStatus(ClusterOperationStatusEnum.OPERATING.value());
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
            ExecResultInfoVO resultInfoVOForSystem = sshUtil.execCommand(ssh, String.format(K8S_ANSIBLE_COMMAND_TEMPLATE, SYSTEM));
            if (resultInfoVOForSystem.getExitCode() != 0) {
                errorMsg = resultInfoVOForSystem.getStdOut() + "\n" + resultInfoVOForSystem.getStdErr();
                devopsNodeCheckResultVO.setStatus(CommandStatus.FAILED.getStatus());
                devopsNodeCheckResultVO.getConfiguration().setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .setErrorMessage(errorMsg);
                stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
                return errorMsg;
            } else {
                devopsNodeCheckResultVO.getSystem().setStatus(ClusterOperationStatusEnum.SUCCESS.value());
            }
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));

            // 内存检查
            devopsNodeCheckResultVO.getMemory().setStatus(ClusterOperationStatusEnum.OPERATING.value());
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
            ExecResultInfoVO resultInfoVOForMemory = sshUtil.execCommand(ssh, String.format(K8S_ANSIBLE_COMMAND_TEMPLATE, MEMORY));
            if (resultInfoVOForMemory.getExitCode() != 0) {
                errorMsg = resultInfoVOForMemory.getStdOut() + "\n" + resultInfoVOForMemory.getStdErr();
                devopsNodeCheckResultVO.setStatus(CommandStatus.FAILED.getStatus());
                devopsNodeCheckResultVO.getConfiguration().setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .setErrorMessage(errorMsg);
                stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
                return errorMsg;
            } else {
                devopsNodeCheckResultVO.getMemory().setStatus(ClusterOperationStatusEnum.SUCCESS.value());
            }
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));

            // CPU检查
            devopsNodeCheckResultVO.getCpu().setStatus(ClusterOperationStatusEnum.OPERATING.value());
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
            ExecResultInfoVO resultInfoVOForCPU = sshUtil.execCommand(ssh, String.format(K8S_ANSIBLE_COMMAND_TEMPLATE, CPU));
            if (resultInfoVOForCPU.getExitCode() != 0) {
                errorMsg = resultInfoVOForCPU.getStdOut() + "\n" + resultInfoVOForCPU.getStdErr();
                devopsNodeCheckResultVO.setStatus(CommandStatus.FAILED.getStatus());
                devopsNodeCheckResultVO.getConfiguration().setStatus(ClusterOperationStatusEnum.FAILED.value())
                        .setErrorMessage(errorMsg);
                stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
                return errorMsg;
            } else {
                devopsNodeCheckResultVO.getCpu().setStatus(ClusterOperationStatusEnum.SUCCESS.value());
                // CPU作为最后一步检查成功，代表整个variable、system、memory、CPU检查成功，暂时将状态置为SUCCESS，因为后续处理可能会失败，将状态重新置为FAILED
                devopsNodeCheckResultVO.setStatus(ClusterOperationStatusEnum.SUCCESS.value());
            }
            stringRedisTemplate.opsForValue().getAndSet(redisKey, JsonHelper.marshalByJackson(devopsNodeCheckResultVO));
            return null;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public void generateAndUploadPrivateKey(SSHClient ssh, List<DevopsClusterNodeDTO> devopsClusterNodeDTOList) throws IOException {
        // 创建目录
        sshUtil.execCommand(ssh, "mkdir -p /tmp/ansible/ssh-key");

        List<String> commands = new ArrayList<>();
        for (DevopsClusterNodeDTO node : devopsClusterNodeDTOList) {
            if (ClusterNodeTypeEnum.INNER.getType().equalsIgnoreCase(node.getType()) && HostAuthType.PUBLICKEY.value().equalsIgnoreCase(node.getAuthType())) {
                commands.add(String.format(SAVE_PRIVATE_KEY_TEMPLATE, Base64Util.getBase64DecodedString(node.getPassword()), String.format(PRIVATE_KEY_SAVE_PATH_TEMPLATE, node.getName())));
            }
        }
        sshUtil.execCommands(ssh, commands);
    }

    @Override
    public void generateAndUploadNodeConfiguration(SSHClient ssh, String suffix, K8sInventoryVO k8sInventoryVO) {
        String configValue = generateInventoryInI(k8sInventoryVO);
        String filePath = String.format(ANSIBLE_CONFIG_BASE_DIR_TEMPLATE, suffix) + SLASH + "k8s-inventory.ini";
        String targetFilePath = BASE_DIR + SLASH + "k8s-inventory.ini";
        FileUtil.saveDataToFile(filePath, configValue);
        sshUtil.uploadFile(ssh, filePath, targetFilePath);
    }

    @Override
    public void generateAndUploadAnsibleShellScript(SSHClient ssh, String suffix, String command, String logPath, String exitCodePath) {
        String configValue = generateShellScript(command, logPath, exitCodePath);
        String filePath = String.format(ANSIBLE_CONFIG_BASE_DIR_TEMPLATE, suffix) + SLASH + command;
        String targetFilePath = BASE_DIR + SLASH + command;
        FileUtil.saveDataToFile(filePath, configValue);
        sshUtil.uploadFile(ssh, filePath, targetFilePath);
    }

    @Override
    public void update() {
        // 添加redis锁，防止多个pod重复执行
        try {
            if (!Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(CLUSTER_STATUS_SYNC_REDIS_LOCK, "lock", 1, TimeUnit.MINUTES))) {
                return;
            }
            DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO = new DevopsClusterOperationRecordDTO()
                    .setStatus(ClusterOperationStatusEnum.OPERATING.value())
                    .setType(ClusterOperationTypeEnum.INSTALL_K8S.getType());
            List<DevopsClusterOperationRecordDTO> devopsClusterOperationRecordDTOList = devopsClusterOperationRecordMapper.select(devopsClusterOperationRecordDTO);
            if (CollectionUtils.isEmpty(devopsClusterOperationRecordDTOList)) {
                return;
            }
            List<Long> clusterIds = devopsClusterOperationRecordDTOList.stream().map(DevopsClusterOperationRecordDTO::getClusterId).collect(Collectors.toList());
            Map<Long, DevopsClusterDTO> devopsClusterDTOMap = devopsClusterMapper.listByClusterIds(clusterIds)
                    .stream()
                    .collect(Collectors.toMap(DevopsClusterDTO::getId, d -> d));
            for (DevopsClusterOperationRecordDTO record : devopsClusterOperationRecordDTOList) {
                Long clusterId = record.getClusterId();
                LOGGER.info(">>>>>>>>> [update cluster status] clusterId:{} operationId:{} <<<<<<<<<", clusterId, record.getId());
                DevopsClusterDTO devopsClusterDTO = devopsClusterDTOMap.get(clusterId);
                if (devopsClusterDTO == null) {
                    devopsClusterOperationRecordMapper.deleteByPrimaryKey(record.getId());
                    continue;
                }
                if (!ClusterStatusEnum.OPERATING.value().equalsIgnoreCase(devopsClusterDTO.getStatus())) {
                    if (ClusterStatusEnum.FAILED.value().equalsIgnoreCase(devopsClusterDTO.getStatus())) {
                        record.setStatus(ClusterOperationStatusEnum.FAILED.value());
                    } else {
                        record.setStatus(ClusterOperationStatusEnum.SUCCESS.value());
                    }
                    devopsClusterOperationRecordMapper.updateByPrimaryKeySelective(record);
                    continue;
                }
                SSHClient ssh = new SSHClient();
                try {
                    List<DevopsClusterNodeDTO> devopsClusterNodeDTOList = devopsClusterNodeMapper.listByClusterId(clusterId);
                    List<DevopsClusterNodeDTO> devopsClusterOutterNodeDTOList = devopsClusterNodeDTOList.stream().filter(n -> ClusterNodeTypeEnum.OUTTER.getType().equalsIgnoreCase(n.getType())).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(devopsClusterOutterNodeDTOList)) {
                        sshUtil.sshConnect(ConvertUtils.convertObject(devopsClusterOutterNodeDTOList.get(0), HostConnectionVO.class), ssh);
                    } else {
                        sshUtil.sshConnect(ConvertUtils.convertObject(devopsClusterNodeDTOList.get(0), HostConnectionVO.class), ssh);
                    }
                    ExecResultInfoVO resultInfoVO = sshUtil.execCommand(ssh, String.format(CAT_FILE, String.format(EXIT_CODE_FILE_TEMPLATE, devopsClusterDTO.getCode())));
                    if (resultInfoVO.getExitCode() != 0) {
                        if (resultInfoVO.getStdErr().contains("No such file or directory")) {
                            LOGGER.info(">>>>>>>>> [update cluster status] cluster [ {} ] operation [ {} ] is installing <<<<<<<<<", clusterId, record.getId());
                        } else {
                            LOGGER.info(">>>>>>>>> [update cluster status] Failed to get install status of host [ {} ],error is: {} <<<<<<<<<", ssh.getRemoteHostname(), resultInfoVO.getStdErr());
                            record.setStatus(ClusterOperationStatusEnum.FAILED.value())
                                    .appendErrorMsg(resultInfoVO.getStdErr());
                            devopsClusterDTO.setStatus(ClusterStatusEnum.FAILED.value());
                        }
                    } else {
                        if ("0".equals(resultInfoVO.getStdOut().replaceAll("\r|\n", ""))) {
                            // k8s安装成功
                            LOGGER.info(">>>>>>>>> [update cluster status] cluster [ {} ] operation [ {} ] install success <<<<<<<<<", clusterId, record.getId());
                            record.setStatus(ClusterOperationStatusEnum.SUCCESS.value());
                            devopsClusterDTO.setStatus(ClusterStatusEnum.DISCONNECT.value());
                            // 安装agent, 第一步安装helm ，第二步安装agent。这一步骤如果出现错误,只保存错误信息
                            installAgent(devopsClusterDTO, record, ssh);
                        } else {
                            LOGGER.info(">>>>>>>>> [update cluster status] cluster [ {} ] operation [ {} ] install failed <<<<<<<<<", clusterId, record.getId());
                            String installLog = sshUtil.execCommand(ssh, String.format(CAT_FILE, INSTALL_K8S_LOG)).getStdOut();
                            int length = installLog.length();
                            installLog = length > MAX_LOG_MSG_LENGTH ? installLog.substring(length - MAX_LOG_MSG_LENGTH, length) : installLog;
                            record.setStatus(ClusterOperationStatusEnum.FAILED.value());
                            devopsClusterDTO.setStatus(ClusterStatusEnum.FAILED.value());
                            record.appendErrorMsg(String.format("install failed.%s for more detail ,login in node [ %s ] and cat %s", installLog, ssh.getRemoteHostname(), INSTALL_K8S_LOG));
                            // 删除执行状态文件，防止重试安装后，该方法读取错误的状态
                            sshUtil.execCommand(ssh, String.format(DELETE_FILE, String.format(EXIT_CODE_FILE_TEMPLATE, devopsClusterDTO.getCode())));
                        }
                    }
                } catch (Exception e) {
                    record.setStatus(ClusterOperationStatusEnum.FAILED.value())
                            .appendErrorMsg(e.getMessage());
                    devopsClusterDTO.setStatus(ClusterStatusEnum.FAILED.value());
                    LOGGER.error("update fail", e);
                } finally {
                    devopsClusterMapper.updateByPrimaryKeySelective(devopsClusterDTO);
                    devopsClusterOperationRecordMapper.updateByPrimaryKeySelective(record);
                    sshUtil.sshDisconnect(ssh);
                }
            }
        } finally {
            stringRedisTemplate.delete(CLUSTER_STATUS_SYNC_REDIS_LOCK);
        }
    }

    @Override
    public List<DevopsClusterNodeDTO> listByClusterId(Long clusterId) {
        if (clusterId == null) {
            return new ArrayList<>();
        }
        return devopsClusterNodeMapper.listByClusterId(clusterId);
    }

    private String generateInventoryInI(K8sInventoryVO k8sInventoryVO) {
        Map<String, String> map = new HashMap<>();
        map.put("{{all}}", k8sInventoryVO.getAll().toString());
        map.put("{{etcd}}", k8sInventoryVO.getEtcd().toString());
        map.put("{{kube-master}}", k8sInventoryVO.getKubeMaster().toString());
        map.put("{{kube-worker}}", k8sInventoryVO.getKubeWorker().toString());
        map.put("{{new-master}}", k8sInventoryVO.getNewMaster().toString());
        map.put("{{new-worker}}", k8sInventoryVO.getNewWorker().toString());
        map.put("{{new-etcd}}", k8sInventoryVO.getNewEtcd().toString());
        map.put("{{del-worker}}", k8sInventoryVO.getDelWorker().toString());
        map.put("{{del-master}}", k8sInventoryVO.getDelMaster().toString());
        map.put("{{del-etcd}}", k8sInventoryVO.getDelEtcd().toString());
        map.put("{{del-node}}", k8sInventoryVO.getDelNode().toString());
        InputStream inventoryIniInputStream = DevopsClusterNodeServiceImpl.class.getResourceAsStream("/template/k8s-inventory.ini");

        return FileUtil.replaceReturnString(inventoryIniInputStream, map);
    }

    private String generateShellScript(String command, String logPath, String exitCodePath) {
        Map<String, String> param = new HashMap<>();
        param.put("{{command}}", command);
        param.put("{{log-path}}", logPath);
        param.put("{{exit-code-path}}", exitCodePath);
        InputStream shellInputStream = DevopsClusterNodeServiceImpl.class.getResourceAsStream("/shell/ansible.sh");
        return FileUtil.replaceReturnString(shellInputStream, param);
    }
}
