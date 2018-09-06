package io.choerodon.devops.domain.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.kubernetes.client.models.V1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsServiceReqDTO;
import io.choerodon.devops.api.validator.DevopsServiceValidator;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository;
import io.choerodon.devops.domain.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;

@Service
public class HandlerServiceRelationsServiceImpl implements HandlerObjectFileRelationsService<V1Service> {

    public static final String SERVICE = "Service";

    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<V1Service> v1Services, Long envId, Long projectId, String path) {
        List<String> beforeService = beforeSync.parallelStream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(SERVICE))
                .map(devopsEnvFileResourceE -> {
                    DevopsServiceE devopsServiceE = devopsServiceRepository
                            .query(devopsEnvFileResourceE.getResourceId());
                    if (devopsServiceE == null) {
                        throw new CommonException("the service in the file is not exist in devops database");
                    }
                    return devopsServiceE.getName();
                }).collect(Collectors.toList());
        //比较已存在网络和新增要处理的网络,获取新增网络，更新网络，删除网络
        List<V1Service> addV1Service = new ArrayList<>();
        List<V1Service> updateV1Service = new ArrayList<>();
        v1Services.parallelStream().forEach(v1Service -> {
            if (beforeService.contains(v1Service.getMetadata().getName())) {
                updateV1Service.add(v1Service);
                beforeService.remove(v1Service.getMetadata().getName());
            } else {
                addV1Service.add(v1Service);
            }
        });
        //新增service
        addService(objectPath, envId, projectId, addV1Service);
        //更新service
        updateService(objectPath, envId, projectId, updateV1Service);
        //删除service,和文件对象关联关系
        beforeService.stream().forEach(serviceName -> {
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(serviceName, envId);
            devopsServiceService.deleteDevopsServiceByGitOps(devopsServiceE.getId());
            devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsServiceE.getId(), SERVICE);
        });
    }


    private void updateService(Map<String, String> objectPath, Long envId, Long projectId, List<V1Service> updateV1Service) {
        updateV1Service.stream()
                .forEach(v1Service -> {
                    String filePath = "";

                    try {
                        filePath = objectPath.get(TypeUtil.objToString(v1Service.hashCode()));

                        DevopsServiceE devopsServiceE = devopsServiceRepository
                                .selectByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                        checkServiceName(v1Service);
                        //初始化网络参数,更新网络和网络关联关系
                        DevopsServiceReqDTO devopsServiceReqDTO = getDevopsServiceDTO(
                                v1Service,
                                envId);
                        devopsServiceService.updateDevopsServiceByGitOps(
                                projectId, devopsServiceE.getId(), devopsServiceReqDTO);
                        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                                .queryByEnvIdAndResource(envId, devopsServiceE.getId(), v1Service.getKind());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceE,
                                v1Service.hashCode(), devopsServiceE.getId(), v1Service.getKind());
                    } catch (CommonException e) {
                        throw new GitOpsExplainException(e.getMessage(), filePath, e);
                    }
                });
    }

    private void addService(Map<String, String> objectPath, Long envId, Long projectId, List<V1Service> addV1Service) {
        addV1Service.stream()
                .forEach(v1Service -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(v1Service.hashCode()));

                        checkServiceName(v1Service);
                        DevopsServiceE devopsServiceE = devopsServiceRepository
                                .selectByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                        DevopsServiceReqDTO devopsServiceReqDTO;
                        //初始化网络参数,创建时判断网络是否存在，存在则直接创建文件对象关联关系
                        if (devopsServiceE == null) {
                            devopsServiceReqDTO = getDevopsServiceDTO(
                                    v1Service,
                                    envId);
                            devopsServiceService.insertDevopsServiceByGitOps(projectId, devopsServiceReqDTO);
                            devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                                    devopsServiceReqDTO.getName(), envId);
                        }
                        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                        devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                        devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(v1Service.hashCode())));
                        devopsEnvFileResourceE.setResourceId(devopsServiceE.getId());
                        devopsEnvFileResourceE.setResourceType(v1Service.getKind());
                        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
                    } catch (CommonException e) {
                        throw new GitOpsExplainException(e.getMessage(), filePath, e);
                    }
                });
    }


    private DevopsServiceReqDTO getDevopsServiceDTO(V1Service v1Service,
                                                    Long envId) {
        DevopsServiceReqDTO devopsServiceReqDTO = new DevopsServiceReqDTO();
        if (v1Service.getSpec().getExternalIPs() != null) {
            devopsServiceReqDTO.setExternalIp(String.join(",", v1Service.getSpec().getExternalIPs()));
        }
        devopsServiceReqDTO.setName(v1Service.getMetadata().getName());
        devopsServiceReqDTO.setType(v1Service.getSpec().getType());
        devopsServiceReqDTO.setEnvId(envId);

        List<PortMapE> portMapList = v1Service.getSpec().getPorts().parallelStream()
                .map(t -> {
                    PortMapE portMap = new PortMapE();
                    portMap.setName(t.getName());
                    if (t.getNodePort() != null) {
                        portMap.setNodePort(t.getNodePort().longValue());
                    }
                    portMap.setPort(t.getPort().longValue());
                    portMap.setProtocol(t.getProtocol());
                    portMap.setTargetPort(TypeUtil.objToString(t.getTargetPort()));
                    return portMap;
                }).collect(Collectors.toList());
        devopsServiceReqDTO.setPorts(portMapList);

        if (v1Service.getMetadata().getAnnotations() != null) {
            String instancesCode = v1Service.getMetadata().getAnnotations()
                    .get("choerodon.io/network-service-instances");
            if (!instancesCode.isEmpty()) {
                List<Long> instanceIdList = Arrays.stream(instancesCode.split("\\+")).parallel()
                        .map(t -> getInstanceId(t, envId, devopsServiceReqDTO))
                        .collect(Collectors.toList());
                devopsServiceReqDTO.setAppInstance(instanceIdList);
            }
        }
        if (v1Service.getSpec().getSelector() != null) {
            devopsServiceReqDTO.setLabel(v1Service.getSpec().getSelector());
        }
        return devopsServiceReqDTO;
    }

    private Long getInstanceId(String instanceCode, Long envId, DevopsServiceReqDTO devopsServiceReqDTO) {
        try {
            ApplicationInstanceE instanceE = applicationInstanceRepository.selectByCode(instanceCode, envId);
            if (devopsServiceReqDTO.getAppId() == null) {
                devopsServiceReqDTO.setAppId(instanceE.getApplicationE().getId());
            }
            if (!devopsServiceReqDTO.getAppId().equals(instanceE.getApplicationE().getId())) {
                throw new CommonException(GitOpsObjectError.INSTANCE_APP_ID_NOT_SAME.getError());
            }
            return instanceE.getId();
        } catch (Exception e) {
            throw new CommonException(GitOpsObjectError.INSTANCE_RELEATED_SERVICE_NOT_FOUND.getError() + instanceCode);
        }

    }

    private void checkServiceName(
            V1Service v1Service) {
        try {
            DevopsServiceValidator.checkName(v1Service.getMetadata().getName());
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }


}
