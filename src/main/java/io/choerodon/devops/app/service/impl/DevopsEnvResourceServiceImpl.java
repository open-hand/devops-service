package io.choerodon.devops.app.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.common.util.K8sUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.devops.infra.common.util.enums.ResourceType;

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

    @Override
    public DevopsEnvResourceDTO listResources(Long instanceId) {
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(instanceId);
        List<DevopsEnvResourceE> devopsEnvResourceES =
                devopsEnvResourceRepository.listByInstanceId(instanceId);
        DevopsEnvResourceDTO devopsEnvResourceDTO = new DevopsEnvResourceDTO();
        if (devopsEnvResourceES == null) {
            return devopsEnvResourceDTO;
        }
        devopsEnvResourceES.forEach(devopsInstanceResourceE -> {
            DevopsEnvResourceDetailE devopsEnvResourceDetailE =
                    devopsEnvResourceDetailRepository.query(
                            devopsInstanceResourceE.getDevopsEnvResourceDetailE().getId());
            ResourceType resourceType = ResourceType.forString(devopsInstanceResourceE.getKind());
            if (resourceType == null) {
                resourceType = ResourceType.forString("MissType");
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
                            devopsInstanceResourceE.getName(), applicationInstanceE.getDevopsEnvironmentE().getId());
                    if (devopsServiceE != null) {
                        List<String> domainNames =
                                devopsIngressRepository.queryIngressNameByServiceId(
                                        devopsServiceE.getId());
                        domainNames.stream().forEach(domainName -> {
                            DevopsEnvResourceE devopsEnvResourceE1 =
                                    devopsEnvResourceRepository.queryResource(
                                            null,
                                            null,
                                            applicationInstanceE.getDevopsEnvironmentE().getId(),
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
                                addIngressToResource(devopsEnvResourceDTO, v1beta1Ingress);
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
                        addIngressToResource(devopsEnvResourceDTO, v1beta1Ingress);
                    }
                    break;
                case REPLICASET:
                    V1beta2ReplicaSet v1beta2ReplicaSet = json.deserialize(
                            devopsEnvResourceDetailE.getMessage(),
                            V1beta2ReplicaSet.class);
                    addReplicaSetToResource(devopsEnvResourceDTO, v1beta2ReplicaSet);
                    break;
                default:
                    break;
            }
        });
        return devopsEnvResourceDTO;
    }


    @Override
    public List<InstanceEventDTO> listInstancePodEvent(Long instanceId) {
        List<InstanceEventDTO> instanceEventDTOS = new ArrayList<>();
        List<DevopsEnvCommandE> devopsEnvCommandES = devopsEnvCommandRepository
                .queryInstanceCommand(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandES.forEach(devopsEnvCommandE -> {
            InstanceEventDTO instanceEventDTO = new InstanceEventDTO();
            UserE userE = iamRepository.queryUserByUserId(devopsEnvCommandE.getCreatedBy());
            instanceEventDTO.setLoginName(userE == null ? null : userE.getLoginName());
            instanceEventDTO.setRealName(userE == null ? null : userE.getRealName());
            instanceEventDTO.setStatus(devopsEnvCommandE.getStatus());
            instanceEventDTO.setUserImage(userE == null ? null : userE.getImageUrl());
            instanceEventDTO.setCreateTime(devopsEnvCommandE.getCreationDate());
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
            List<DevopsEnvCommandLogE> devopsEnvCommandLogES = devopsEnvCommandLogRepository
                    .queryByDeployId(devopsEnvCommandE.getId());
            for (int i = 0; i < jobs.size(); i++) {
                DevopsEnvResourceE job = jobs.get(i);
                DevopsEnvResourceDetailE devopsEnvResourceDetailE =
                        devopsEnvResourceDetailRepository.query(
                                job.getDevopsEnvResourceDetailE().getId());
                V1Job v1Job = json.deserialize(devopsEnvResourceDetailE.getMessage(), V1Job.class);
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
            //获取实例中pod的event
            List<DevopsCommandEventE> devopsCommandPodEventES = devopsCommandEventRepository
                    .listByCommandIdAndType(devopsEnvCommandE.getId(), ResourceType.POD.getType());
            if (!devopsCommandPodEventES.isEmpty()) {
                LinkedHashMap<String, String> podEvents = getDevopsCommandEvent(devopsCommandPodEventES);
                podEvents.forEach((key, value) -> {
                    PodEventDTO podEventDTO = new PodEventDTO();
                    podEventDTO.setName(key);
                    podEventDTO.setEvent(value);
                    podEventDTOS.add(podEventDTO);
                });
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
        v1beta2Deployment.getStatus().getConditions().forEach(v1beta2DeploymentCondition -> {
            if ("NewReplicaSetAvailable".equals(v1beta2DeploymentCondition.getReason())) {
                deploymentDTO.setAge(v1beta2DeploymentCondition.getLastUpdateTime().toString());
            }
        });
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
     * @param devopsEnvResourceDTO 实例资源参数
     * @param v1beta1Ingress       ingress对象
     */
    public void addIngressToResource(DevopsEnvResourceDTO devopsEnvResourceDTO, V1beta1Ingress v1beta1Ingress) {
        IngressDTO ingressDTO = new IngressDTO();
        ingressDTO.setName(v1beta1Ingress.getMetadata().getName());
        ingressDTO.setHosts(K8sUtil.formatHosts(v1beta1Ingress.getSpec().getRules()));
        ingressDTO.setPorts(K8sUtil.formatPorts(v1beta1Ingress.getSpec().getTls()));
        ingressDTO.setAddress(K8sUtil.loadBalancerStatusStringer(v1beta1Ingress.getStatus().getLoadBalancer()));
        ingressDTO.setAge(v1beta1Ingress.getMetadata().getCreationTimestamp().toString());
        devopsEnvResourceDTO.getIngressDTOS().add(ingressDTO);
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
