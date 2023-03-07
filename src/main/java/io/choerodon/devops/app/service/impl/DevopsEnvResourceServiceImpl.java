package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsEnvResourceMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonYamlConversionUtil;
import io.choerodon.devops.infra.util.K8sUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by younger on 2018/4/25.
 */
@Service
public class DevopsEnvResourceServiceImpl implements DevopsEnvResourceService {

    private static final String ERROR_COMMAND_ID_IS_NULL = "error.command.id.is.null";
    private static final String LINE_SEPARATOR = "line.separator";
    private static final String NONE_LABEL = "<none>";
    private static JSON json = new JSON();

    @Autowired
    private DevopsEnvResourceMapper devopsEnvResourceMapper;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsCommandEventService devopsCommandEventService;
    @Autowired
    private DevopsEnvCommandLogService devopsEnvCommandLogService;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;
    @Autowired
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;
    @Autowired
    private DevopsDeploymentService devopsDeploymentService;
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;

    @Override
    public DevopsEnvResourceVO listResourcesInHelmRelease(Long instanceId) {
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(instanceId);
        List<DevopsEnvResourceDTO> devopsEnvResourceDTOS = baseListByInstanceId(instanceId);
        DevopsEnvResourceVO devopsEnvResourceDTO = new DevopsEnvResourceVO();
        if (devopsEnvResourceDTOS == null) {
            return devopsEnvResourceDTO;
        }

        // 关联资源
        devopsEnvResourceDTOS.forEach(envResourceDTO -> {
                    DevopsEnvResourceDetailDTO envResourceDetailDTO = devopsEnvResourceDetailService.baseQueryByResourceDetailId(envResourceDTO.getResourceDetailId());
                    if (isReleaseGenerated(envResourceDetailDTO.getMessage())) {
                        dealWithResource(envResourceDetailDTO, envResourceDTO, devopsEnvResourceDTO, appServiceInstanceDTO.getEnvId());
                    }
                }
        );
        return devopsEnvResourceDTO;
    }

    @Override
    public DevopsEnvResourceVO listResourcesByDeploymentId(Long deploymentId) {
        DevopsDeploymentVO devopsDeploymentVO = devopsDeploymentService.queryByDeploymentIdWithResourceDetail(deploymentId);
        DevopsEnvResourceVO devopsEnvResourceVO = new DevopsEnvResourceVO();
        if (devopsDeploymentVO == null) {
            return devopsEnvResourceVO;
        }

        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = devopsEnvResourceDetailService.baseQueryByResourceDetailId(devopsDeploymentVO.getResourceDetailId());

        if (devopsEnvResourceDetailDTO != null) {
            // 获取相关的pod
            List<DevopsEnvPodVO> devopsEnvPodVOs = devopsEnvPodService.listWorkloadPod(ResourceType.DEPLOYMENT.getType(), devopsDeploymentVO.getName());

            V1beta2Deployment v1beta2Deployment = json.deserialize(
                    devopsEnvResourceDetailDTO.getMessage(),
                    V1beta2Deployment.class);

            addDeploymentToResource(devopsEnvResourceVO, v1beta2Deployment, deploymentId);

            devopsEnvResourceVO.getDeploymentVOS().forEach(deploymentVO -> deploymentVO.setDevopsEnvPodVOS(devopsEnvPodVOs));

            return devopsEnvResourceVO;
        } else {
            return devopsEnvResourceVO;
        }
    }

    /**
     * 判断该资源是否是应用chart包中定义而生成资源
     *
     * @param message 资源的信息
     * @return true 如果是chart包定义生成的
     */
    private boolean isReleaseGenerated(String message) {
        try {
            JsonNode info = new ObjectMapper().readTree(message);
            return info.get("metadata").get("labels").get("choerodon.io/release") != null;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 处理获取的资源详情，将资源详情根据类型进行数据处理填入 devopsEnvResourceDTO 中
     *
     * @param devopsEnvResourceDetailDTO 资源详情
     * @param devopsEnvResourceDTO       资源
     * @param devopsEnvResourceVO        存放处理结果的dto
     * @param envId                      环境id
     */
    private void dealWithResource(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO, DevopsEnvResourceDTO devopsEnvResourceDTO, DevopsEnvResourceVO devopsEnvResourceVO, Long envId) {
        ResourceType resourceType = ResourceType.forString(devopsEnvResourceDTO.getKind());
        if (resourceType == null) {
            resourceType = ResourceType.MISSTYPE;
        }
        switch (resourceType) {
            case POD:
                V1Pod v1Pod = json.deserialize(devopsEnvResourceDetailDTO.getMessage(), V1Pod.class);
                addPodToResource(devopsEnvResourceVO, v1Pod);
                break;
            case DEPLOYMENT:
                V1beta2Deployment v1beta2Deployment = json.deserialize(
                        devopsEnvResourceDetailDTO.getMessage(),
                        V1beta2Deployment.class);

                addDeploymentToResource(devopsEnvResourceVO, v1beta2Deployment, devopsEnvResourceDTO.getInstanceId());
                break;
            case SERVICE:
                V1Service v1Service = json.deserialize(devopsEnvResourceDetailDTO.getMessage(),
                        V1Service.class);
                addServiceToResource(devopsEnvResourceVO, v1Service);
                break;
            case INGRESS:
                if (devopsEnvResourceDTO.getInstanceId() != null) {
                    V1beta1Ingress v1beta1Ingress = json.deserialize(
                            devopsEnvResourceDetailDTO.getMessage(),
                            V1beta1Ingress.class);
                    devopsEnvResourceVO.getIngressVOS().add(addIngressToResource(v1beta1Ingress));
                }
                break;
            case REPLICASET:
                V1beta2ReplicaSet v1beta2ReplicaSet = json.deserialize(
                        devopsEnvResourceDetailDTO.getMessage(),
                        V1beta2ReplicaSet.class);
                addReplicaSetToResource(devopsEnvResourceVO, v1beta2ReplicaSet);
                break;
            case DAEMONSET:
                V1beta2DaemonSet v1beta2DaemonSet = json.deserialize(devopsEnvResourceDetailDTO.getMessage(), V1beta2DaemonSet.class);
                addDaemonSetToResource(devopsEnvResourceVO, v1beta2DaemonSet, devopsEnvResourceDTO.getInstanceId());
                break;
            case STATEFULSET:
                V1beta2StatefulSet v1beta2StatefulSet = json.deserialize(devopsEnvResourceDetailDTO.getMessage(), V1beta2StatefulSet.class);
                addStatefulSetSetToResource(devopsEnvResourceVO, v1beta2StatefulSet, devopsEnvResourceDTO.getInstanceId());
                break;
            case PERSISTENT_VOLUME_CLAIM:
                V1PersistentVolumeClaim persistentVolumeClaim = json.deserialize(devopsEnvResourceDetailDTO.getMessage(), V1PersistentVolumeClaim.class);
                addPersistentVolumeClaimToResource(devopsEnvResourceVO, persistentVolumeClaim);
                break;
            default:
                break;
        }
    }

    @Override
    public List<InstanceEventVO> listInstancePodEvent(Long instanceId) {
        return listEventByObjectId(instanceId, ObjectType.INSTANCE);
    }

    @Override
    public List<InstanceEventVO> listDeploymentPodEvent(Long deploymentId) {
        return listEventByObjectId(deploymentId, ObjectType.DEPLOYMENT);
    }

    private List<InstanceEventVO> listEventByObjectId(Long objectId, ObjectType objectType) {
        List<InstanceEventVO> instanceEventVOS = new ArrayList<>();
        List<DevopsEnvCommandDTO> devopsEnvCommandDTOS = devopsEnvCommandService
                .baseListInstanceCommand(objectType.getType(), objectId);
        List<Long> userIds = devopsEnvCommandDTOS.stream().filter(devopsEnvCommandDTO -> devopsEnvCommandDTO.getCreatedBy() != 0).map(DevopsEnvCommandDTO::getCreatedBy).collect(Collectors.toList());
        List<IamUserDTO> users = baseServiceClientOperator.listUsersByIds(userIds);

        // 查出所有的 DevopsCommandEventDTO 并根据commandId分组
        Set<Long> commandIds = devopsEnvCommandDTOS.stream().map(DevopsEnvCommandDTO::getId).collect(Collectors.toSet());
        List<DevopsCommandEventDTO> commandEventTypeJob = devopsCommandEventService.listLastByCommandIdsAndType(commandIds, ResourceType.JOB.getType());
        List<DevopsCommandEventDTO> commandEventTypePod = devopsCommandEventService.listLastByCommandIdsAndType(commandIds, ResourceType.POD.getType());
        Map<Long, List<DevopsCommandEventDTO>> commandEventTypeJobMap = commandEventTypeJob.stream().collect(Collectors.groupingBy(DevopsCommandEventDTO::getCommandId));
        Map<Long, List<DevopsCommandEventDTO>> commandEventTypePodJobMap = commandEventTypePod.stream().collect(Collectors.groupingBy(DevopsCommandEventDTO::getCommandId));

        devopsEnvCommandDTOS.forEach(devopsEnvCommandDTO -> {
            InstanceEventVO instanceEventVO = new InstanceEventVO();
            Optional<IamUserDTO> iamUserDTO = users.stream().filter(user -> user.getId().equals(devopsEnvCommandDTO.getCreatedBy())).findFirst();
            IamUserDTO iamUser = null;
            if (iamUserDTO.isPresent()) {
                iamUser = iamUserDTO.get();
                instanceEventVO.setLoginName(iamUser.getLdap() ? iamUser.getLoginName() : iamUser.getEmail());
                instanceEventVO.setRealName(iamUser.getRealName());
            }
            instanceEventVO.setCommandId(devopsEnvCommandDTO.getId());
            instanceEventVO.setStatus(devopsEnvCommandDTO.getStatus());
            instanceEventVO.setCommandError(devopsEnvCommandDTO.getError());
            instanceEventVO.setUserImage(iamUser == null ? null : iamUser.getImageUrl());
            instanceEventVO.setCreateTime(devopsEnvCommandDTO.getCreationDate());
            instanceEventVO.setType(devopsEnvCommandDTO.getCommandType());
            List<PodEventVO> podEventVOS = new ArrayList<>();
            //获取实例中job的event
            List<DevopsCommandEventDTO> devopsCommandEventDTOS = commandEventTypeJobMap.get(devopsEnvCommandDTO.getId());
            if (!CollectionUtils.isEmpty(devopsCommandEventDTOS)) {
                LinkedHashMap<String, String> jobEvents = getDevopsCommandEvent(devopsCommandEventDTOS);
                jobEvents.forEach((key, value) -> {
                    PodEventVO podEventVO = new PodEventVO();
                    podEventVO.setName(key);
                    podEventVO.setEvent(value);
                    podEventVOS.add(podEventVO);
                });
            }
            List<DevopsEnvResourceDTO> jobs = baseListByCommandId(devopsEnvCommandDTO.getId());
            List<DevopsEnvCommandLogDTO> devopsEnvCommandLogES = devopsEnvCommandLogService
                    .baseListByDeployId(devopsEnvCommandDTO.getId());
            for (int i = 0; i < jobs.size(); i++) {
                DevopsEnvResourceDTO job = jobs.get(i);
                DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO =
                        devopsEnvResourceDetailService.baseQueryByResourceDetailId(
                                job.getResourceDetailId());
                V1Job v1Job = json.deserialize(devopsEnvResourceDetailDTO.getMessage(), V1Job.class);
                if (podEventVOS.size() < 4) {
                    //job日志
                    if (i <= devopsEnvCommandLogES.size() - 1) {
                        if (podEventVOS.size() == i) {
                            PodEventVO podEventVO = new PodEventVO();
                            podEventVO.setName(v1Job.getMetadata().getName());
                            podEventVOS.add(podEventVO);
                        }
                        podEventVOS.get(i).setLog(devopsEnvCommandLogES.get(i).getLog());
                    }
                    //获取job状态
                    if (i <= podEventVOS.size() - 1) {
                        setJobStatus(v1Job, podEventVOS.get(i));
                    }
                }
            }
            //获取实例中pod的event
            List<DevopsCommandEventDTO> devopsCommandPodEventES = commandEventTypePodJobMap.get(devopsEnvCommandDTO.getId());
            if (!CollectionUtils.isEmpty(devopsCommandPodEventES)) {
                LinkedHashMap<String, String> podEvents = getDevopsCommandEvent(devopsCommandPodEventES);
                int index = 0;
                for (Map.Entry<String, String> entry : podEvents.entrySet()) {
                    PodEventVO podEventVO = new PodEventVO();
                    podEventVO.setName(entry.getKey());
                    podEventVO.setEvent(entry.getValue());
                    podEventVOS.add(podEventVO);
                    if (index++ >= 4) {
                        break;
                    }
                }
            }
            instanceEventVO.setPodEventVO(podEventVOS);
            instanceEventVOS.add(instanceEventVO);

        });
        return instanceEventVOS;
    }


    private void setJobStatus(V1Job v1Job, PodEventVO podEventVO) {
        if (v1Job.getStatus() != null) {
            if (v1Job.getStatus().getSucceeded() != null && v1Job.getStatus().getSucceeded() == 1) {
                podEventVO.setJobPodStatus("success");
            } else if (v1Job.getStatus().getFailed() != null) {
                podEventVO.setJobPodStatus("fail");
            } else {
                podEventVO.setJobPodStatus("running");
            }
        }
    }


    private LinkedHashMap<String, String> getDevopsCommandEvent(List<DevopsCommandEventDTO> devopsCommandEventDTOS) {
        devopsCommandEventDTOS.sort(Comparator.comparing(DevopsCommandEventDTO::getId));
        LinkedHashMap<String, String> event = new LinkedHashMap<>();
        for (DevopsCommandEventDTO devopsCommandEventDTO : devopsCommandEventDTOS) {
            if (!event.containsKey(devopsCommandEventDTO.getName())) {
                event.put(devopsCommandEventDTO.getName(), devopsCommandEventDTO.getMessage() + System.getProperty(LINE_SEPARATOR));
            } else {
                event.put(devopsCommandEventDTO.getName(), event.get(devopsCommandEventDTO.getName()) + devopsCommandEventDTO.getMessage() + System.getProperty(LINE_SEPARATOR));
            }
        }
        return event;
    }


    /**
     * 增加pod资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1Pod                pod对象
     */
    private void addPodToResource(DevopsEnvResourceVO devopsEnvResourceDTO, V1Pod v1Pod) {
        PodVO podVO = new PodVO();
        podVO.setName(v1Pod.getMetadata().getName());
        podVO.setDesire(TypeUtil.objToLong(v1Pod.getSpec().getContainers().size()));
        long ready = 0L;
        Long restart = 0L;
        if (v1Pod.getStatus().getContainerStatuses() != null) {
            for (V1ContainerStatus v1ContainerStatus : v1Pod.getStatus().getContainerStatuses()) {
                if (v1ContainerStatus.isReady() && v1ContainerStatus.getState().getRunning().getStartedAt() != null) {
                    ready = ready + 1;
                }
                restart = restart + v1ContainerStatus.getRestartCount();
            }
        }
        podVO.setReady(ready);
        podVO.setStatus(K8sUtil.changePodStatus(v1Pod));
        podVO.setRestarts(restart);
        podVO.setAge(v1Pod.getMetadata().getCreationTimestamp().toString());
        devopsEnvResourceDTO.getPodVOS().add(podVO);
    }

    /**
     * 增加deployment资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1beta2Deployment    deployment对象
     */
    public void addDeploymentToResource(DevopsEnvResourceVO devopsEnvResourceDTO, V1beta2Deployment v1beta2Deployment, Long instanceId) {
        DeploymentVO deploymentVO = new DeploymentVO();
        deploymentVO.setName(v1beta2Deployment.getMetadata().getName());
        deploymentVO.setDesired(TypeUtil.objToLong(v1beta2Deployment.getSpec().getReplicas()));
        deploymentVO.setCurrent(TypeUtil.objToLong(v1beta2Deployment.getStatus().getReplicas()));
        deploymentVO.setUpToDate(TypeUtil.objToLong(v1beta2Deployment.getStatus().getUpdatedReplicas()));
        deploymentVO.setAvailable(TypeUtil.objToLong(v1beta2Deployment.getStatus().getAvailableReplicas()));
        deploymentVO.setAge(v1beta2Deployment.getMetadata().getCreationTimestamp().toString());
        deploymentVO.setLabels(v1beta2Deployment.getSpec().getSelector().getMatchLabels());
        deploymentVO.setInstanceId(instanceId);
        List<Integer> portRes = new ArrayList<>();
        for (V1Container container : v1beta2Deployment.getSpec().getTemplate().getSpec().getContainers()) {
            List<V1ContainerPort> ports = container.getPorts();
            Optional.ofNullable(ports).ifPresent(portList -> {
                for (V1ContainerPort port : portList) {
                    portRes.add(port.getContainerPort());
                }
            });
        }
        deploymentVO.setPorts(portRes);
        if (v1beta2Deployment.getStatus() != null && v1beta2Deployment.getStatus().getConditions() != null) {
            v1beta2Deployment.getStatus().getConditions().forEach(v1beta2DeploymentCondition -> {
                if ("NewReplicaSetAvailable".equals(v1beta2DeploymentCondition.getReason())) {
                    deploymentVO.setAge(v1beta2DeploymentCondition.getLastUpdateTime().toString());
                }
            });
        }
        devopsEnvResourceDTO.getDeploymentVOS().add(deploymentVO);
    }

    /**
     * 增加service资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1Service            service对象
     */
    public void addServiceToResource(DevopsEnvResourceVO devopsEnvResourceDTO, V1Service v1Service) {
        ServiceVO serviceVO = new ServiceVO();
        serviceVO.setName(v1Service.getMetadata().getName());
        serviceVO.setType(v1Service.getSpec().getType());
        if (v1Service.getSpec().getClusterIP().length() == 0) {
            serviceVO.setClusterIp(NONE_LABEL);
        } else {
            serviceVO.setClusterIp(v1Service.getSpec().getClusterIP());
        }
        serviceVO.setExternalIp(K8sUtil.getServiceExternalIp(v1Service));
        String port = K8sUtil.makePortString(v1Service.getSpec().getPorts());
        if (port.length() == 0) {
            port = NONE_LABEL;
        }
        String targetPort = K8sUtil.makeTargetPortString(v1Service.getSpec().getPorts());
        if (targetPort.length() == 0) {
            targetPort = NONE_LABEL;
        }
        serviceVO.setPort(port);
        serviceVO.setTargetPort(targetPort);
        serviceVO.setAge(v1Service.getMetadata().getCreationTimestamp().toString());
        devopsEnvResourceDTO.getServiceVOS().add(serviceVO);
    }

    /**
     * 增加ingress资源
     *
     * @param v1beta1Ingress ingress对象
     */
    private IngressVO addIngressToResource(V1beta1Ingress v1beta1Ingress) {
        IngressVO ingressVO = new IngressVO();
        ingressVO.setName(v1beta1Ingress.getMetadata().getName());
        ingressVO.setHosts(K8sUtil.formatHosts(v1beta1Ingress.getSpec().getRules()));
        ingressVO.setPorts(K8sUtil.formatPorts(v1beta1Ingress.getSpec().getTls()));
        ingressVO.setAddress(K8sUtil.loadBalancerStatusStringer(v1beta1Ingress.getStatus().getLoadBalancer()));
        ingressVO.setAge(v1beta1Ingress.getMetadata().getCreationTimestamp().toString());
        ingressVO.setServices(K8sUtil.analyzeIngressServices(v1beta1Ingress));
        return ingressVO;
    }

    /**
     * 增加replicaSet资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1beta2ReplicaSet    replicaSet对象
     */
    public void addReplicaSetToResource(DevopsEnvResourceVO devopsEnvResourceDTO, V1beta2ReplicaSet v1beta2ReplicaSet) {
        if (v1beta2ReplicaSet.getSpec().getReplicas() == 0) {
            return;
        }
        ReplicaSetVO replicaSetVO = new ReplicaSetVO();
        replicaSetVO.setName(v1beta2ReplicaSet.getMetadata().getName());
        replicaSetVO.setCurrent(TypeUtil.objToLong(v1beta2ReplicaSet.getStatus().getReplicas()));
        replicaSetVO.setDesired(TypeUtil.objToLong(v1beta2ReplicaSet.getSpec().getReplicas()));
        replicaSetVO.setReady(TypeUtil.objToLong(v1beta2ReplicaSet.getStatus().getReadyReplicas()));
        replicaSetVO.setAge(v1beta2ReplicaSet.getMetadata().getCreationTimestamp().toString());
        devopsEnvResourceDTO.getReplicaSetVOS().add(replicaSetVO);
    }

    /**
     * 添加daemonSet类型资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1beta2DaemonSet     daemonSet对象
     */
    private void addDaemonSetToResource(DevopsEnvResourceVO devopsEnvResourceDTO, V1beta2DaemonSet v1beta2DaemonSet, Long instanceId) {
        DaemonSetVO daemonSetVO = new DaemonSetVO();
        daemonSetVO.setName(v1beta2DaemonSet.getMetadata().getName());
        daemonSetVO.setAge(v1beta2DaemonSet.getMetadata().getCreationTimestamp().toString());
        daemonSetVO.setCurrentScheduled(TypeUtil.objToLong(v1beta2DaemonSet.getStatus().getCurrentNumberScheduled()));
        daemonSetVO.setDesiredScheduled(TypeUtil.objToLong(v1beta2DaemonSet.getStatus().getDesiredNumberScheduled()));
        daemonSetVO.setNumberAvailable(TypeUtil.objToLong(v1beta2DaemonSet.getStatus().getNumberAvailable()));
        daemonSetVO.setInstanceId(instanceId);

        devopsEnvResourceDTO.getDaemonSetVOS().add(daemonSetVO);
    }

    /**
     * 添加statefulSet类型资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1beta2StatefulSet   statefulSet对象
     */
    private void addStatefulSetSetToResource(DevopsEnvResourceVO devopsEnvResourceDTO, V1beta2StatefulSet v1beta2StatefulSet, Long instanceId) {
        StatefulSetVO statefulSetVO = new StatefulSetVO();
        statefulSetVO.setName(v1beta2StatefulSet.getMetadata().getName());
        statefulSetVO.setDesiredReplicas(TypeUtil.objToLong(v1beta2StatefulSet.getSpec().getReplicas()));
        statefulSetVO.setAge(v1beta2StatefulSet.getMetadata().getCreationTimestamp().toString());
        statefulSetVO.setReadyReplicas(TypeUtil.objToLong(v1beta2StatefulSet.getStatus().getReadyReplicas()));
        statefulSetVO.setCurrentReplicas(TypeUtil.objToLong(v1beta2StatefulSet.getStatus().getCurrentReplicas()));
        statefulSetVO.setInstanceId(instanceId);

        devopsEnvResourceDTO.getStatefulSetVOS().add(statefulSetVO);
    }

    /**
     * 添加persistentVolumeClaim类型资源
     *
     * @param devopsEnvResourceDTO    实例资源参数
     * @param v1PersistentVolumeClaim persistentVolumeClaim对象
     */
    private void addPersistentVolumeClaimToResource(DevopsEnvResourceVO devopsEnvResourceDTO, V1PersistentVolumeClaim v1PersistentVolumeClaim) {
        PersistentVolumeClaimVO dto = new PersistentVolumeClaimVO();
        dto.setName(v1PersistentVolumeClaim.getMetadata().getName());
        dto.setStatus(v1PersistentVolumeClaim.getStatus().getPhase());
        // 当PVC是Pending状态时，status字段下只有phase字段
        if ("Pending".equals(dto.getStatus())) {
            dto.setCapacity("0Gi");
        } else {
            dto.setCapacity(v1PersistentVolumeClaim.getStatus().getCapacity().get("storage").toSuffixedString());
        }
        dto.setAccessModes(v1PersistentVolumeClaim.getSpec().getAccessModes().toString());
        dto.setAge(v1PersistentVolumeClaim.getMetadata().getCreationTimestamp().toString());

        devopsEnvResourceDTO.getPersistentVolumeClaimVOS().add(dto);
    }

    @Override
    public void baseCreate(DevopsEnvResourceDTO devopsEnvResourceDTO) {
        if (devopsEnvResourceMapper.insert(devopsEnvResourceDTO) != 1) {
            throw new CommonException("error.resource.insert");
        }
    }

    @Override
    public List<DevopsEnvResourceDTO> baseListByInstanceId(Long instanceId) {
        DevopsEnvResourceDTO devopsEnvResourceDTO = new DevopsEnvResourceDTO();
        devopsEnvResourceDTO.setInstanceId(instanceId);
        return devopsEnvResourceMapper.select(devopsEnvResourceDTO);
    }

    @Override
    public List<DevopsEnvResourceDTO> baseListByCommandId(Long commandId) {
        return devopsEnvResourceMapper.listJobs(commandId);
    }

    @Override
    public void baseUpdate(DevopsEnvResourceDTO devopsEnvResourceDTO) {
        devopsEnvResourceDTO.setObjectVersionNumber(
                devopsEnvResourceMapper.selectByPrimaryKey(
                        devopsEnvResourceDTO.getId()).getObjectVersionNumber());
        if (devopsEnvResourceMapper.updateByPrimaryKeySelective(devopsEnvResourceDTO) != 1) {
            throw new CommonException("error.resource.update");
        }
    }

    @Override
    public void deleteByEnvIdAndKindAndName(Long envId, String kind, String name) {
        Assert.notNull(envId, ResourceCheckConstant.ERROR_ENV_ID_IS_NULL);
        Assert.notNull(kind, ResourceCheckConstant.ERROR_KIND_NAME_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.ERROR_RESOURCE_NAME_IS_NULL);

        DevopsEnvResourceDTO devopsEnvResourceDO = new DevopsEnvResourceDTO();
        devopsEnvResourceDO.setEnvId(envId);
        devopsEnvResourceDO.setKind(kind);
        devopsEnvResourceDO.setName(name);
        devopsEnvResourceMapper.delete(devopsEnvResourceDO);
    }

    @Override
    public List<DevopsEnvResourceDTO> baseListByEnvAndType(Long envId, String type) {
        return devopsEnvResourceMapper.listByEnvAndType(envId, type);
    }

    @Override
    public DevopsEnvResourceDTO baseQueryByKindAndName(String kind, String name) {
        return devopsEnvResourceMapper.queryLatestJob(kind, name);
    }

    @Override
    public void deleteByKindAndNameAndInstanceId(String kind, String name, Long instanceId) {
        DevopsEnvResourceDTO devopsEnvResourceDTO = new DevopsEnvResourceDTO();
        devopsEnvResourceDTO.setKind(kind);
        devopsEnvResourceDTO.setName(name);
        devopsEnvResourceDTO.setInstanceId(instanceId);
        devopsEnvResourceMapper.delete(devopsEnvResourceDTO);
    }

    @Override
    public DevopsEnvResourceDTO baseQueryOptions(Long instanceId, Long commandId, Long envId, String kind, String name) {
        List<DevopsEnvResourceDTO> devopsEnvResourceDTOS = devopsEnvResourceMapper.queryResource(instanceId, commandId, envId, kind, name);
        if (devopsEnvResourceDTOS.isEmpty()) {
            return null;
        }
        return devopsEnvResourceDTOS.get(0);
    }


    @Override
    public String getResourceDetailByNameAndTypeAndInstanceId(Long instanceId, String name, ResourceType resourceType) {
        return devopsEnvResourceMapper.getResourceDetailByNameAndTypeAndInstanceId(instanceId, name, resourceType.getType());
    }

    @Override
    public List<DevopsEnvResourceDTO> listEnvResourceByOptions(Long envId, String type, List<String> names) {
        return devopsEnvResourceMapper.listEnvResourceByOptions(envId, type, names);
    }

    @Override
    public List<PodEventVO> listPodEventBycommandId(Long commandId) {

        Assert.notNull(commandId, ERROR_COMMAND_ID_IS_NULL);

        List<DevopsCommandEventDTO> devopsCommandEventDTOS = devopsCommandEventService.listByCommandId(commandId);
        List<DevopsCommandEventDTO> commandEventTypeJob = devopsCommandEventDTOS.stream().filter(v -> ResourceType.JOB.getType().equals(v.getType())).collect(Collectors.toList());
        List<DevopsCommandEventDTO> commandEventTypePod = devopsCommandEventDTOS.stream().filter(v -> ResourceType.POD.getType().equals(v.getType())).collect(Collectors.toList());

        //获取实例中job的event
        List<PodEventVO> podEventVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(commandEventTypeJob)) {
            LinkedHashMap<String, String> jobEvents = getDevopsCommandEvent(commandEventTypeJob);
            jobEvents.forEach((key, value) -> {
                PodEventVO podEventVO = new PodEventVO();
                podEventVO.setName(key);
                podEventVO.setEvent(value);
                podEventVOS.add(podEventVO);
            });
        }
        List<DevopsEnvResourceDTO> jobs = baseListByCommandId(commandId);
        List<DevopsEnvCommandLogDTO> devopsEnvCommandLogES = devopsEnvCommandLogService
                .baseListByDeployId(commandId);
        for (int i = 0; i < jobs.size(); i++) {
            DevopsEnvResourceDTO job = jobs.get(i);
            DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO =
                    devopsEnvResourceDetailService.baseQueryByResourceDetailId(
                            job.getResourceDetailId());
            V1Job v1Job = json.deserialize(devopsEnvResourceDetailDTO.getMessage(), V1Job.class);
            if (podEventVOS.size() < 4) {
                //job日志
                if (i <= devopsEnvCommandLogES.size() - 1) {
                    if (podEventVOS.size() == i) {
                        PodEventVO podEventVO = new PodEventVO();
                        podEventVO.setName(v1Job.getMetadata().getName());
                        podEventVOS.add(podEventVO);
                    }
                    podEventVOS.get(i).setLog(devopsEnvCommandLogES.get(i).getLog());
                }
                //获取job状态
                if (i <= podEventVOS.size() - 1) {
                    if (podEventVOS.size() == i) {
                        PodEventVO podEventVO = new PodEventVO();
                        podEventVOS.add(podEventVO);
                    }
                    setJobStatus(v1Job, podEventVOS.get(i));
                }
            }
        }
        //获取实例中pod的event
        if (!CollectionUtils.isEmpty(commandEventTypePod)) {
            LinkedHashMap<String, String> podEvents = getDevopsCommandEvent(commandEventTypePod);
            int index = 0;
            for (Map.Entry<String, String> entry : podEvents.entrySet()) {
                PodEventVO podEventVO = new PodEventVO();
                podEventVO.setName(entry.getKey());
                podEventVO.setEvent(entry.getValue());
                podEventVOS.add(podEventVO);
                if (index++ >= 4) {
                    break;
                }
            }
        }
        return podEventVOS;
    }

    @Override
    public String getResourceDetailByEnvIdAndKindAndName(Long envId, String name, ResourceType resourceType) {
        return devopsEnvResourceMapper.getResourceDetailByEnvIdAndKindAndName(envId, name, resourceType.getType());
    }

    @Override
    public Object queryDetailsByKindAndName(Long envId, String kind, String name) {
        String message = devopsEnvResourceMapper.queryDetailsByKindAndName(envId, kind, name);
        if (StringUtils.isEmpty(message)) {
            return null;
        }
        try {
            return new ObjectMapper().readTree(message);
        } catch (IOException e) {
            throw new CommonException("error.resource.json.read.failed", message);
        }
    }

    @Override
    public String queryYamlById(Long envId, Long workLoadId, String type) {
        DevopsWorkloadResourceContentDTO devopsWorkloadResourceContentDTO = devopsWorkloadResourceContentService.baseQuery(workLoadId, type);
        return devopsWorkloadResourceContentDTO.getContent();
    }

    @Override
    public String queryDetailsYamlByKindAndName(Long envId, String kind, String name) {
        String message = devopsEnvResourceMapper.queryDetailsByKindAndName(envId, kind, name);
        try {
            return JsonYamlConversionUtil.json2yaml(message);
        } catch (IOException e) {
            throw new CommonException(JsonYamlConversionUtil.ERROR_JSON_TO_YAML_FAILED, message);
        }
    }

    @Override
    public List<DevopsEnvPodVO> listPodResourceByInstanceId(Long instanceId) {
        List<DevopsEnvPodVO> devopsEnvPodVOS = ConvertUtils.convertList(devopsEnvPodService.baseListByInstanceId(instanceId), DevopsEnvPodVO.class);
        Map<String, DevopsEnvPodVO> nameDevopsEnvPodVOMap = devopsEnvPodVOS.stream().collect(Collectors.toMap(DevopsEnvPodVO::getName, Function.identity()));

        List<DevopsEnvResourceDTO> resourceWithDetailByInstanceId = devopsEnvResourceMapper.getResourceWithDetailByInstanceIdAndKind(instanceId, ResourceType.POD.getType());
        for (DevopsEnvResourceDTO r : resourceWithDetailByInstanceId) {
            try {
                DevopsEnvPodVO devopsEnvPodVO = nameDevopsEnvPodVOMap.get(r.getName());
                if (devopsEnvPodVO != null) {
                    JsonNode info = new ObjectMapper().readTree(r.getMessage());
                    JsonNode jsonNode = info.get("metadata").withArray("ownerReferences");
                    JsonNode ownerReference = jsonNode.get(0);
                    if (ownerReference != null) {
                        devopsEnvPodVO.setOwnerName(ownerReference.get("name").asText());
                        devopsEnvPodVO.setOwnerKind(ownerReference.get("kind").asText());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return devopsEnvPodVOS;
    }
}
