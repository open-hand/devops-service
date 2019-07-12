package io.choerodon.devops.app.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.util.K8sUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;

/**
 * Created by younger on 2018/4/25.
 */
@Service
public class DevopsEnvResourceServiceImpl implements DevopsEnvResourceService {

    private static final String LINE_SEPARATOR = "line.separator";
    private static final String NONE_LABEL = "<none>";
    private static JSON json = new JSON();

    @Autowired
    private DevopsEnvResourceRepository devopsEnvResourceRepository;
    @Autowired
    private DevopsEnvResourceDetailRepository devopsEnvResourceDetailRepository;
    @Autowired
    private DevopsEnvCommandLogRepository devopsEnvCommandLogRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsCommandEventRepository devopsCommandEventRepository;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DevopsServiceInstanceRepository devopsServiceInstanceRepository;


    @Override
    public DevopsEnvResourceDTO listResourcesInHelmRelease(Long instanceId) {
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(instanceId);
        List<DevopsEnvResourceE> devopsEnvResourceES =
                devopsEnvResourceRepository.listByInstanceId(instanceId);
        DevopsEnvResourceDTO devopsEnvResourceDTO = new DevopsEnvResourceDTO();
        if (devopsEnvResourceES == null) {
            return devopsEnvResourceDTO;
        }

        // 关联资源
        devopsEnvResourceES.forEach(devopsInstanceResourceE -> {
                    DevopsEnvResourceDetailE detailE = devopsEnvResourceDetailRepository.query(devopsInstanceResourceE.getDevopsEnvResourceDetailE().getId());
                    if (isReleaseGenerated(detailE.getMessage())) {
                        dealWithResource(detailE, devopsInstanceResourceE, devopsEnvResourceDTO, applicationInstanceE.getDevopsEnvironmentE().getId());
                    }
                }
        );
        return devopsEnvResourceDTO;
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
     * @param devopsEnvResourceDetailE 资源详情
     * @param devopsInstanceResourceE  资源
     * @param devopsEnvResourceDTO     存放处理结果的dto
     * @param envId                    环境id
     */
    private void dealWithResource(DevopsEnvResourceDetailE devopsEnvResourceDetailE, DevopsEnvResourceE devopsInstanceResourceE, DevopsEnvResourceDTO devopsEnvResourceDTO, Long envId) {
        ResourceType resourceType = ResourceType.forString(devopsInstanceResourceE.getKind());
        if (resourceType == null) {
            resourceType = ResourceType.MISSTYPE;
        }
        switch (resourceType) {
            case POD:
                V1Pod v1Pod = json.deserialize(devopsEnvResourceDetailE.getMessage(), V1Pod.class);
                addPodToResource(devopsEnvResourceDTO, v1Pod);
                break;
            case DEPLOYMENT:
                V1beta2Deployment v1beta2Deployment = json.deserialize(
                        devopsEnvResourceDetailE.getMessage(),
                        V1beta2Deployment.class);

                addDeploymentToResource(devopsEnvResourceDTO, v1beta2Deployment);
                break;
            case SERVICE:
                V1Service v1Service = json.deserialize(devopsEnvResourceDetailE.getMessage(),
                        V1Service.class);
                DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                        devopsInstanceResourceE.getName(), envId);
                if (devopsServiceE != null) {
                    List<String> domainNames =
                            devopsIngressRepository.queryIngressNameByServiceId(
                                    devopsServiceE.getId());
                    domainNames.forEach(domainName -> {
                        DevopsEnvResourceE devopsEnvResourceE1 =
                                devopsEnvResourceRepository.queryResource(
                                        null,
                                        null,
                                        envId,
                                        "Ingress",
                                        domainName);
                        //升级0.11.0-0.12.0,资源表新增envId,修复以前的域名数据
                        if (devopsEnvResourceE1 == null) {
                            devopsEnvResourceE1 = devopsEnvResourceRepository.queryResource(
                                    null,
                                    null,
                                    null,
                                    "Ingress",
                                    domainName);
                        }
                        if (devopsEnvResourceE1 != null) {
                            DevopsEnvResourceDetailE devopsEnvResourceDetailE1 =
                                    devopsEnvResourceDetailRepository.query(
                                            devopsEnvResourceE1.getDevopsEnvResourceDetailE().getId());
                            V1beta1Ingress v1beta1Ingress = json.deserialize(
                                    devopsEnvResourceDetailE1.getMessage(),
                                    V1beta1Ingress.class);
                            devopsEnvResourceDTO.getIngressDTOS().add(addIngressToResource(v1beta1Ingress));
                        }
                    });
                }
                addServiceToResource(devopsEnvResourceDTO, v1Service);
                break;
            case INGRESS:
                if (devopsInstanceResourceE.getApplicationInstanceE() != null) {
                    V1beta1Ingress v1beta1Ingress = json.deserialize(
                            devopsEnvResourceDetailE.getMessage(),
                            V1beta1Ingress.class);
                    devopsEnvResourceDTO.getIngressDTOS().add(addIngressToResource(v1beta1Ingress));
                }
                break;
            case REPLICASET:
                V1beta2ReplicaSet v1beta2ReplicaSet = json.deserialize(
                        devopsEnvResourceDetailE.getMessage(),
                        V1beta2ReplicaSet.class);
                addReplicaSetToResource(devopsEnvResourceDTO, v1beta2ReplicaSet);
                break;
            case DAEMONSET:
                V1beta2DaemonSet v1beta2DaemonSet = json.deserialize(devopsEnvResourceDetailE.getMessage(), V1beta2DaemonSet.class);
                addDaemonSetToResource(devopsEnvResourceDTO, v1beta2DaemonSet);
                break;
            case STATEFULSET:
                V1beta2StatefulSet v1beta2StatefulSet = json.deserialize(devopsEnvResourceDetailE.getMessage(), V1beta2StatefulSet.class);
                addStatefulSetSetToResource(devopsEnvResourceDTO, v1beta2StatefulSet);
                break;
            case PERSISTENT_VOLUME_CLAIM:
                V1PersistentVolumeClaim persistentVolumeClaim = json.deserialize(devopsEnvResourceDetailE.getMessage(), V1PersistentVolumeClaim.class);
                addPersistentVolumeClaimToResource(devopsEnvResourceDTO, persistentVolumeClaim);
                break;
            default:
                break;
        }
    }


    @Override
    public List<InstanceEventDTO> listInstancePodEvent(Long instanceId) {
        List<InstanceEventDTO> instanceEventDTOS = new ArrayList<>();
        List<DevopsEnvCommandVO> devopsEnvCommandES = devopsEnvCommandRepository
                .baseQueryInstanceCommand(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandES.forEach(devopsEnvCommandE -> {
            InstanceEventDTO instanceEventDTO = new InstanceEventDTO();
            UserE userE = iamRepository.queryUserByUserId(devopsEnvCommandE.getCreatedBy());
            instanceEventDTO.setLoginName(userE == null ? null : userE.getLoginName());
            instanceEventDTO.setRealName(userE == null ? null : userE.getRealName());
            instanceEventDTO.setStatus(devopsEnvCommandE.getStatus());
            instanceEventDTO.setUserImage(userE == null ? null : userE.getImageUrl());
            instanceEventDTO.setCreateTime(devopsEnvCommandE.getCreationDate());
            instanceEventDTO.setType(devopsEnvCommandE.getCommandType());
            List<PodEventDTO> podEventDTOS = new ArrayList<>();
            //获取实例中job的event
            List<DevopsCommandEventE> devopsCommandJobEventES = devopsCommandEventRepository
                    .listByCommandIdAndType(devopsEnvCommandE.getId(), ResourceType.JOB.getType());
            if (!devopsCommandJobEventES.isEmpty()) {
                LinkedHashMap<String, String> jobEvents = getDevopsCommandEvent(devopsCommandJobEventES);
                jobEvents.forEach((key, value) -> {
                    PodEventDTO podEventDTO = new PodEventDTO();
                    podEventDTO.setName(key);
                    podEventDTO.setEvent(value);
                    podEventDTOS.add(podEventDTO);
                });
            }
            List<DevopsEnvResourceE> jobs = devopsEnvResourceRepository.listJobs(devopsEnvCommandE.getId());
            List<DevopsEnvCommandLogVO> devopsEnvCommandLogES = devopsEnvCommandLogRepository
                    .baseQueryByDeployId(devopsEnvCommandE.getId());
            for (int i = 0; i < jobs.size(); i++) {
                DevopsEnvResourceE job = jobs.get(i);
                DevopsEnvResourceDetailE devopsEnvResourceDetailE =
                        devopsEnvResourceDetailRepository.query(
                                job.getDevopsEnvResourceDetailE().getId());
                V1Job v1Job = json.deserialize(devopsEnvResourceDetailE.getMessage(), V1Job.class);
                if (podEventDTOS.size() < 4) {
                    //job日志
                    if (i <= devopsEnvCommandLogES.size() - 1) {
                        if (podEventDTOS.size() == i) {
                            PodEventDTO podEventDTO = new PodEventDTO();
                            podEventDTO.setName(v1Job.getMetadata().getName());
                            podEventDTOS.add(podEventDTO);
                        }
                        podEventDTOS.get(i).setLog(devopsEnvCommandLogES.get(i).getLog());
                    }
                    //获取job状态
                    if (i <= podEventDTOS.size() - 1) {
                        if (podEventDTOS.size() == i) {
                            PodEventDTO podEventDTO = new PodEventDTO();
                            podEventDTOS.add(podEventDTO);
                        }
                        setJobStatus(v1Job, podEventDTOS.get(i));
                    }
                }
            }
            //获取实例中pod的event
            List<DevopsCommandEventE> devopsCommandPodEventES = devopsCommandEventRepository
                    .listByCommandIdAndType(devopsEnvCommandE.getId(), ResourceType.POD.getType());
            if (!devopsCommandPodEventES.isEmpty()) {
                LinkedHashMap<String, String> podEvents = getDevopsCommandEvent(devopsCommandPodEventES);
                int index = 0;
                for (Map.Entry<String, String> entry : podEvents.entrySet()) {
                    PodEventDTO podEventDTO = new PodEventDTO();
                    podEventDTO.setName(entry.getKey());
                    podEventDTO.setEvent(entry.getValue());
                    podEventDTOS.add(podEventDTO);
                    if (index++ >= 4) {
                        break;
                    }
                }
            }
            instanceEventDTO.setPodEventDTO(podEventDTOS);
            if (!instanceEventDTO.getPodEventDTO().isEmpty()) {
                instanceEventDTOS.add(instanceEventDTO);
            }
        });
        return instanceEventDTOS;
    }


    private void setJobStatus(V1Job v1Job, PodEventDTO podEventDTO) {
        if (v1Job.getStatus() != null) {
            if (v1Job.getStatus().getSucceeded() != null && v1Job.getStatus().getSucceeded() == 1) {
                podEventDTO.setJobPodStatus("success");
            } else if (v1Job.getStatus().getFailed() != null) {
                podEventDTO.setJobPodStatus("fail");
            } else {
                podEventDTO.setJobPodStatus("running");
            }
        }
    }


    private LinkedHashMap<String, String> getDevopsCommandEvent(List<DevopsCommandEventE> devopsCommandEventES) {
        devopsCommandEventES.sort(Comparator.comparing(DevopsCommandEventE::getId));
        LinkedHashMap<String, String> event = new LinkedHashMap<>();
        for (DevopsCommandEventE devopsCommandEventE : devopsCommandEventES) {
            if (!event.containsKey(devopsCommandEventE.getName())) {
                event.put(devopsCommandEventE.getName(), devopsCommandEventE.getMessage() + System.getProperty(LINE_SEPARATOR));
            } else {
                event.put(devopsCommandEventE.getName(), event.get(devopsCommandEventE.getName()) + devopsCommandEventE.getMessage() + System.getProperty(LINE_SEPARATOR));
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
    private void addPodToResource(DevopsEnvResourceDTO devopsEnvResourceDTO, V1Pod v1Pod) {
        PodDTO podDTO = new PodDTO();
        podDTO.setName(v1Pod.getMetadata().getName());
        podDTO.setDesire(TypeUtil.objToLong(v1Pod.getSpec().getContainers().size()));
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
        podDTO.setReady(ready);
        podDTO.setStatus(K8sUtil.changePodStatus(v1Pod));
        podDTO.setRestarts(restart);
        podDTO.setAge(v1Pod.getMetadata().getCreationTimestamp().toString());
        devopsEnvResourceDTO.getPodDTOS().add(podDTO);
    }

    /**
     * 增加deployment资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1beta2Deployment    deployment对象
     */
    public void addDeploymentToResource(DevopsEnvResourceDTO devopsEnvResourceDTO, V1beta2Deployment v1beta2Deployment) {
        DeploymentDTO deploymentDTO = new DeploymentDTO();
        deploymentDTO.setName(v1beta2Deployment.getMetadata().getName());
        deploymentDTO.setDesired(TypeUtil.objToLong(v1beta2Deployment.getSpec().getReplicas()));
        deploymentDTO.setCurrent(TypeUtil.objToLong(v1beta2Deployment.getStatus().getReplicas()));
        deploymentDTO.setUpToDate(TypeUtil.objToLong(v1beta2Deployment.getStatus().getUpdatedReplicas()));
        deploymentDTO.setAvailable(TypeUtil.objToLong(v1beta2Deployment.getStatus().getAvailableReplicas()));
        deploymentDTO.setAge(v1beta2Deployment.getMetadata().getCreationTimestamp().toString());
        deploymentDTO.setLabels(v1beta2Deployment.getSpec().getSelector().getMatchLabels());
        List<Integer> portRes = new ArrayList<>();
        for (V1Container container : v1beta2Deployment.getSpec().getTemplate().getSpec().getContainers()) {
            List<V1ContainerPort> ports = container.getPorts();
            Optional.ofNullable(ports).ifPresent(portList -> {
                for (V1ContainerPort port : portList) {
                    portRes.add(port.getContainerPort());
                }
            });
        }
        deploymentDTO.setPorts(portRes);
        if (v1beta2Deployment.getStatus() != null && v1beta2Deployment.getStatus().getConditions() != null) {
            v1beta2Deployment.getStatus().getConditions().forEach(v1beta2DeploymentCondition -> {
                if ("NewReplicaSetAvailable".equals(v1beta2DeploymentCondition.getReason())) {
                    deploymentDTO.setAge(v1beta2DeploymentCondition.getLastUpdateTime().toString());
                }
            });
        }
        devopsEnvResourceDTO.getDeploymentDTOS().add(deploymentDTO);
    }

    /**
     * 增加service资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1Service            service对象
     */
    public void addServiceToResource(DevopsEnvResourceDTO devopsEnvResourceDTO, V1Service v1Service) {
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setName(v1Service.getMetadata().getName());
        serviceDTO.setType(v1Service.getSpec().getType());
        if (v1Service.getSpec().getClusterIP().length() == 0) {
            serviceDTO.setClusterIp(NONE_LABEL);
        } else {
            serviceDTO.setClusterIp(v1Service.getSpec().getClusterIP());
        }
        serviceDTO.setExternalIp(K8sUtil.getServiceExternalIp(v1Service));
        String port = K8sUtil.makePortString(v1Service.getSpec().getPorts());
        if (port.length() == 0) {
            port = NONE_LABEL;
        }
        String targetPort = K8sUtil.makeTargetPortString(v1Service.getSpec().getPorts());
        if (targetPort.length() == 0) {
            targetPort = NONE_LABEL;
        }
        serviceDTO.setPort(port);
        serviceDTO.setTargetPort(targetPort);
        serviceDTO.setAge(v1Service.getMetadata().getCreationTimestamp().toString());
        devopsEnvResourceDTO.getServiceDTOS().add(serviceDTO);
    }

    /**
     * 增加ingress资源
     *
     * @param v1beta1Ingress ingress对象
     */
    public IngressDTO addIngressToResource(V1beta1Ingress v1beta1Ingress) {
        IngressDTO ingressDTO = new IngressDTO();
        ingressDTO.setName(v1beta1Ingress.getMetadata().getName());
        ingressDTO.setHosts(K8sUtil.formatHosts(v1beta1Ingress.getSpec().getRules()));
        ingressDTO.setPorts(K8sUtil.formatPorts(v1beta1Ingress.getSpec().getTls()));
        ingressDTO.setAddress(K8sUtil.loadBalancerStatusStringer(v1beta1Ingress.getStatus().getLoadBalancer()));
        ingressDTO.setAge(v1beta1Ingress.getMetadata().getCreationTimestamp().toString());
        return ingressDTO;
    }

    /**
     * 增加replicaSet资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1beta2ReplicaSet    replicaSet对象
     */
    public void addReplicaSetToResource(DevopsEnvResourceDTO devopsEnvResourceDTO, V1beta2ReplicaSet v1beta2ReplicaSet) {
        if (v1beta2ReplicaSet.getSpec().getReplicas() == 0) {
            return;
        }
        ReplicaSetDTO replicaSetDTO = new ReplicaSetDTO();
        replicaSetDTO.setName(v1beta2ReplicaSet.getMetadata().getName());
        replicaSetDTO.setCurrent(TypeUtil.objToLong(v1beta2ReplicaSet.getStatus().getReplicas()));
        replicaSetDTO.setDesired(TypeUtil.objToLong(v1beta2ReplicaSet.getSpec().getReplicas()));
        replicaSetDTO.setReady(TypeUtil.objToLong(v1beta2ReplicaSet.getStatus().getReadyReplicas()));
        replicaSetDTO.setAge(v1beta2ReplicaSet.getMetadata().getCreationTimestamp().toString());
        devopsEnvResourceDTO.getReplicaSetDTOS().add(replicaSetDTO);
    }

    /**
     * 添加daemonSet类型资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1beta2DaemonSet     daemonSet对象
     */
    private void addDaemonSetToResource(DevopsEnvResourceDTO devopsEnvResourceDTO, V1beta2DaemonSet v1beta2DaemonSet) {
        DaemonSetDTO daemonSetDTO = new DaemonSetDTO();
        daemonSetDTO.setName(v1beta2DaemonSet.getMetadata().getName());
        daemonSetDTO.setAge(v1beta2DaemonSet.getMetadata().getCreationTimestamp().toString());
        daemonSetDTO.setCurrentScheduled(TypeUtil.objToLong(v1beta2DaemonSet.getStatus().getCurrentNumberScheduled()));
        daemonSetDTO.setDesiredScheduled(TypeUtil.objToLong(v1beta2DaemonSet.getStatus().getDesiredNumberScheduled()));
        daemonSetDTO.setNumberAvailable(TypeUtil.objToLong(v1beta2DaemonSet.getStatus().getNumberAvailable()));

        devopsEnvResourceDTO.getDaemonSetDTOS().add(daemonSetDTO);
    }

    /**
     * 添加statefulSet类型资源
     *
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1beta2StatefulSet   statefulSet对象
     */
    private void addStatefulSetSetToResource(DevopsEnvResourceDTO devopsEnvResourceDTO, V1beta2StatefulSet v1beta2StatefulSet) {
        StatefulSetDTO statefulSetDTO = new StatefulSetDTO();
        statefulSetDTO.setName(v1beta2StatefulSet.getMetadata().getName());
        statefulSetDTO.setDesiredReplicas(TypeUtil.objToLong(v1beta2StatefulSet.getSpec().getReplicas()));
        statefulSetDTO.setAge(v1beta2StatefulSet.getMetadata().getCreationTimestamp().toString());
        statefulSetDTO.setReadyReplicas(TypeUtil.objToLong(v1beta2StatefulSet.getStatus().getReadyReplicas()));
        statefulSetDTO.setCurrentReplicas(TypeUtil.objToLong(v1beta2StatefulSet.getStatus().getCurrentReplicas()));

        devopsEnvResourceDTO.getStatefulSetDTOS().add(statefulSetDTO);
    }

    /**
     * 添加persistentVolumeClaim类型资源
     *
     * @param devopsEnvResourceDTO    实例资源参数
     * @param v1PersistentVolumeClaim persistentVolumeClaim对象
     */
    private void addPersistentVolumeClaimToResource(DevopsEnvResourceDTO devopsEnvResourceDTO, V1PersistentVolumeClaim v1PersistentVolumeClaim) {
        PersistentVolumeClaimDTO dto = new PersistentVolumeClaimDTO();
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

        devopsEnvResourceDTO.getPersistentVolumeClaimDTOS().add(dto);
    }

    /**
     * 获取时间间隔
     *
     * @param ttime1 起始时间
     * @param ttime2 结束时间
     * @return long[]
     */
    public Long[] getStageTime(Timestamp ttime1, Timestamp ttime2) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long time1 = ttime1.getTime();
        long time2 = ttime2.getTime();
        long diff;
        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        return new Long[]{day, hour, min, sec};
    }
}
