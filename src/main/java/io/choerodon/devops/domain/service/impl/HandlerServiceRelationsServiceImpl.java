package io.choerodon.devops.domain.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import io.kubernetes.client.models.V1Service;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsServiceReqDTO;
import io.choerodon.devops.api.validator.DevopsServiceValidator;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;
import io.choerodon.devops.infra.common.util.enums.ObjectType;

@Service
public class HandlerServiceRelationsServiceImpl implements HandlerObjectFileRelationsService<V1Service> {

    public static final String SERVICE = "Service";
    private static final String GIT_SUFFIX = "/.git";
    private Gson gson = new Gson();


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
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsServiceInstanceRepository devopsServiceInstanceRepository;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<V1Service> v1Services, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeService = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(SERVICE))
                .map(devopsEnvFileResourceE -> {
                    DevopsServiceE devopsServiceE = devopsServiceRepository
                            .query(devopsEnvFileResourceE.getResourceId());
                    if (devopsServiceE == null) {
                        devopsEnvFileResourceRepository
                                .deleteByEnvIdAndResource(envId, devopsEnvFileResourceE.getResourceId(), SERVICE);
                        return null;
                    }
                    return devopsServiceE.getName();
                }).collect(Collectors.toList());
        //比较已存在网络和新增要处理的网络,获取新增网络，更新网络，删除网络
        List<V1Service> addV1Service = new ArrayList<>();
        List<V1Service> updateV1Service = new ArrayList<>();
        v1Services.stream().forEach(v1Service -> {
            if (beforeService.contains(v1Service.getMetadata().getName())) {
                updateV1Service.add(v1Service);
                beforeService.remove(v1Service.getMetadata().getName());
            } else {
                addV1Service.add(v1Service);
            }
        });
        //新增service
        addService(objectPath, envId, projectId, addV1Service, path, userId);
        //更新service
        updateService(objectPath, envId, projectId, updateV1Service, path, userId);
        //删除service,和文件对象关联关系
        beforeService.forEach(serviceName -> {
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(serviceName, envId);
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsServiceE.getCommandId());
            if (!devopsEnvCommandE.getCommandType().equals(CommandType.DELETE.getType())) {
                DevopsEnvCommandE devopsEnvCommandE1 = new DevopsEnvCommandE();
                devopsEnvCommandE1.setCommandType(CommandType.DELETE.getType());
                devopsEnvCommandE1.setCreatedBy(userId);
                devopsEnvCommandE1.setObject(ObjectType.SERVICE.getType());
                devopsEnvCommandE1.setStatus(CommandStatus.OPERATING.getStatus());
                devopsEnvCommandE1.setObjectId(devopsServiceE.getId());
                devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE1).getId());
                devopsServiceRepository.update(devopsServiceE);
            }
            devopsServiceService.deleteDevopsServiceByGitOps(devopsServiceE.getId());
            devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsServiceE.getId(), SERVICE);
        });
    }


    private void updateService(Map<String, String> objectPath, Long envId, Long projectId, List<V1Service> updateV1Service, String path, Long userId) {
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
                                envId,
                                filePath);
                        Boolean isNotChange = checkIsNotChange(devopsServiceE, devopsServiceReqDTO);
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsServiceE.getCommandId());
                        if (!isNotChange) {
                            devopsServiceService.updateDevopsServiceByGitOps(projectId, devopsServiceE.getId(), devopsServiceReqDTO, userId);
                            DevopsServiceE newDevopsServiceE = devopsServiceRepository
                                    .selectByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                            devopsEnvCommandE = devopsEnvCommandRepository.query(newDevopsServiceE.getCommandId());
                        }
                        //0.9.0-0.10.0,新增commandId,如果gitops库如果一个文件里面有多个对象，只操作其中一个对象，其它对象更新commitsha避免npe
                        if (devopsEnvCommandE == null) {
                            devopsEnvCommandE = createDevopsEnvCommandE("update");
                            devopsEnvCommandE.setObjectId(devopsServiceE.getId());
                            devopsServiceE.setCommandId(devopsEnvCommandE.getId());
                            devopsServiceRepository.update(devopsServiceE);
                        }
                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);
                        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                                .queryByEnvIdAndResource(envId, devopsServiceE.getId(), v1Service.getKind());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceE,
                                v1Service.hashCode(), devopsServiceE.getId(), v1Service.getKind());
                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private void addService(Map<String, String> objectPath, Long envId, Long projectId, List<V1Service> addV1Service, String path, Long userId) {
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
                                    envId,
                                    filePath);
                            devopsServiceService.insertDevopsServiceByGitOps(projectId, devopsServiceReqDTO, userId);
                            devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                                    devopsServiceReqDTO.getName(), envId);
                        }
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsServiceE.getCommandId());
                        //0.9.0-0.10.0,新增commandId,如果gitops库如果只是移动对象到另外一个文件，避免npe
                        if (devopsEnvCommandE == null) {
                            devopsEnvCommandE = createDevopsEnvCommandE("create");
                            devopsEnvCommandE.setObjectId(devopsServiceE.getId());
                            devopsServiceE.setCommandId(devopsEnvCommandE.getId());
                            devopsServiceRepository.update(devopsServiceE);
                        }
                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);
                        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                        devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                        devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(v1Service.hashCode())));
                        devopsEnvFileResourceE.setResourceId(devopsServiceE.getId());
                        devopsEnvFileResourceE.setResourceType(v1Service.getKind());
                        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }


    private DevopsServiceReqDTO getDevopsServiceDTO(V1Service v1Service,
                                                    Long envId, String filePath) {
        DevopsServiceReqDTO devopsServiceReqDTO = new DevopsServiceReqDTO();
        if (v1Service.getSpec().getExternalIPs() != null) {
            devopsServiceReqDTO.setExternalIp(String.join(",", v1Service.getSpec().getExternalIPs()));
        }
        devopsServiceReqDTO.setName(v1Service.getMetadata().getName());
        devopsServiceReqDTO.setType(v1Service.getSpec().getType());
        devopsServiceReqDTO.setEnvId(envId);

        List<PortMapE> portMapList = v1Service.getSpec().getPorts().stream()
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
            if (instancesCode != null) {
                List<Long> instanceIdList = Arrays.stream(instancesCode.split("\\+")).parallel()
                        .map(t -> getInstanceId(t, envId, devopsServiceReqDTO, filePath))
                        .collect(Collectors.toList());
                devopsServiceReqDTO.setAppInstance(instanceIdList);
            }
        }
        if (v1Service.getSpec().getSelector() != null) {
            devopsServiceReqDTO.setLabel(v1Service.getSpec().getSelector());
        }
        return devopsServiceReqDTO;
    }

    private Long getInstanceId(String instanceCode, Long envId, DevopsServiceReqDTO devopsServiceReqDTO, String filePath) {
        try {
            ApplicationInstanceE instanceE = applicationInstanceRepository.selectByCode(instanceCode, envId);
            if (devopsServiceReqDTO.getAppId() == null) {
                devopsServiceReqDTO.setAppId(instanceE.getApplicationE().getId());
            }
            if (!devopsServiceReqDTO.getAppId().equals(instanceE.getApplicationE().getId())) {
                throw new GitOpsExplainException(GitOpsObjectError.INSTANCE_APP_ID_NOT_SAME.getError(), filePath);
            }
            return instanceE.getId();
        } catch (Exception e) {
            throw new GitOpsExplainException(GitOpsObjectError.INSTANCE_RELATED_SERVICE_NOT_FOUND.getError(), filePath, instanceCode, e);
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

    private Boolean checkIsNotChange(DevopsServiceE devopsServiceE, DevopsServiceReqDTO devopsServiceReqDTO) {
        List<PortMapE> oldPort = devopsServiceE.getPorts();
        //查询网络对应的实例
        List<DevopsServiceAppInstanceE> devopsServiceInstanceEList =
                devopsServiceInstanceRepository.selectByServiceId(devopsServiceE.getId());
        Boolean isUpdate = false;
        if (devopsServiceReqDTO.getAppId() != null && devopsServiceE.getAppId() != null) {
            if (!devopsServiceE.getAppId().equals(devopsServiceReqDTO.getAppId())) {
                checkOptions(devopsServiceE.getEnvId(), devopsServiceReqDTO.getAppId(), null);
            }
            if (devopsServiceReqDTO.getAppInstance() != null) {
                List<String> newInstanceCode = devopsServiceReqDTO.getAppInstance().stream().map(instanceId -> applicationInstanceRepository.selectById(instanceId).getCode()).collect(Collectors.toList());
                List<String> oldInstanceCode = devopsServiceInstanceEList.stream().map(DevopsServiceAppInstanceE::getCode).collect(Collectors.toList());
                for (String instanceCode : newInstanceCode) {
                    if (!oldInstanceCode.contains(instanceCode)) {
                        isUpdate = true;
                    }
                }
            }
        }

        if (devopsServiceReqDTO.getAppId() == null && devopsServiceE.getAppId() == null) {
            isUpdate = !gson.toJson(devopsServiceReqDTO.getLabel()).equals(devopsServiceE.getLabels());
        }
        if ((devopsServiceReqDTO.getAppId() == null && devopsServiceE.getAppId() != null) || (devopsServiceReqDTO.getAppId() != null && devopsServiceE.getAppId() == null)) {
            isUpdate = true;
        }
        return !isUpdate && oldPort.stream().sorted().collect(Collectors.toList())
                .equals(devopsServiceReqDTO.getPorts().stream().sorted().collect(Collectors.toList()))
                && !isUpdateExternalIp(devopsServiceReqDTO, devopsServiceE);
    }

    private Boolean isUpdateExternalIp(DevopsServiceReqDTO devopsServiceReqDTO, DevopsServiceE devopsServiceE) {
        return !((StringUtils.isEmpty(devopsServiceReqDTO.getExternalIp())
                && StringUtils.isEmpty(devopsServiceE.getExternalIp()))
                || (!StringUtils.isEmpty(devopsServiceReqDTO.getExternalIp())
                && !StringUtils.isEmpty(devopsServiceE.getExternalIp())
                && devopsServiceReqDTO.getExternalIp().equals(devopsServiceE.getExternalIp())));
    }

    private DevopsEnvCommandE createDevopsEnvCommandE(String type) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        if (type.equals("create")) {
            devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
        } else {
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        }
        devopsEnvCommandE.setObject(ObjectType.SERVICE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandRepository.create(devopsEnvCommandE);
    }


    private void checkOptions(Long envId, Long appId, Long appInstanceId) {
        if (applicationInstanceRepository.checkOptions(envId, appId, appInstanceId) == 0) {
            throw new CommonException("error.instances.query");
        }
    }
}
