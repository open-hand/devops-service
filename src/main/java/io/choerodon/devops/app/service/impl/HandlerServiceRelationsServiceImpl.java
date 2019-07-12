package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsServiceReqDTO;
import io.choerodon.devops.api.vo.EndPointPortDTO;
import io.choerodon.devops.api.validator.DevopsServiceValidator;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.app.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.kubernetes.client.models.V1Endpoints;
import io.kubernetes.client.models.V1Service;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<V1Service> v1Services, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
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
        addService(objectPath, envId, projectId, addV1Service, v1Endpoints, path, userId);
        //更新service
        updateService(objectPath, envId, projectId, updateV1Service, v1Endpoints, path, userId);
        //删除service,和文件对象关联关系
        beforeService.forEach(serviceName -> {
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(serviceName, envId);
            if (devopsServiceE != null) {
                devopsServiceService.deleteDevopsServiceByGitOps(devopsServiceE.getId());
                devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsServiceE.getId(), SERVICE);
            }
        });
    }


    private void updateService(Map<String, String> objectPath, Long envId, Long projectId, List<V1Service> updateV1Service, List<V1Endpoints> v1Endpoints, String path, Long userId) {
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
                                v1Endpoints,
                                envId);
                        Boolean isNotChange = checkIsNotChange(devopsServiceE, devopsServiceReqDTO);
                        DevopsEnvCommandVO devopsEnvCommandE = devopsEnvCommandRepository.query(devopsServiceE.getCommandId());
                        if (!isNotChange) {
                            devopsServiceService.updateDevopsServiceByGitOps(projectId, devopsServiceE.getId(), devopsServiceReqDTO, userId);
                            DevopsServiceE newDevopsServiceE = devopsServiceRepository
                                    .selectByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                            devopsEnvCommandE = devopsEnvCommandRepository.query(newDevopsServiceE.getCommandId());
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

    private void addService(Map<String, String> objectPath, Long envId, Long projectId, List<V1Service> addV1Service, List<V1Endpoints> v1Endpoints, String path, Long userId) {
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
                                    v1Endpoints,
                                    envId);
                            devopsServiceService.insertDevopsServiceByGitOps(projectId, devopsServiceReqDTO, userId);
                            devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                                    devopsServiceReqDTO.getName(), envId);
                        }
                        DevopsEnvCommandVO devopsEnvCommandE = devopsEnvCommandRepository.query(devopsServiceE.getCommandId());

                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, v1Service.hashCode(), devopsServiceE.getId(),
                                v1Service.getKind());
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
                                                    List<V1Endpoints> v1Endpoints,
                                                    Long envId) {
        DevopsServiceReqDTO devopsServiceReqDTO = new DevopsServiceReqDTO();
        if (v1Service.getSpec().getExternalIPs() != null) {
            devopsServiceReqDTO.setExternalIp(String.join(",", v1Service.getSpec().getExternalIPs()));
        }
        Map<String, List<EndPointPortDTO>> endPoints = new HashMap<>();
        v1Endpoints.stream().filter(v1Endpoints1 -> v1Endpoints1.getMetadata().getName().equals(v1Service.getMetadata().getName())).forEach(v1Endpoints1 -> {
            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 0; i < v1Endpoints1.getSubsets().get(0).getAddresses().size(); i++) {
                if (i == 0 || i == v1Endpoints1.getSubsets().get(0).getAddresses().size() - 1) {
                    keyBuilder.append(v1Endpoints1.getSubsets().get(0).getAddresses().get(i).getIp());
                } else {
                    keyBuilder.append(v1Endpoints1.getSubsets().get(0).getAddresses().get(i).getIp() + ",");
                }
            }
            endPoints.put(keyBuilder.toString(), v1Endpoints1.getSubsets().get(0).getPorts().stream().map(v1EndpointPort -> {
                EndPointPortDTO endPointPortDTO = new EndPointPortDTO();
                endPointPortDTO.setName(v1EndpointPort.getName());
                endPointPortDTO.setPort(v1EndpointPort.getPort());
                return endPointPortDTO;
            }).collect(Collectors.toList()));
            devopsServiceReqDTO.setEndPoints(endPoints);
        });

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
                List<String> instanceIdList = Arrays.stream(instancesCode.split("\\+")).parallel().map(t -> {
                    ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectByCode(t, envId);
                    if (applicationInstanceE != null) {
                        devopsServiceReqDTO.setAppId(applicationInstanceE.getApplicationE().getId());
                    }
                    return t;
                }).collect(Collectors.toList());
                devopsServiceReqDTO.setAppInstance(instanceIdList);
            }
        }
        if (v1Service.getSpec().getSelector() != null) {
            devopsServiceReqDTO.setLabel(v1Service.getSpec().getSelector());
        }
        return devopsServiceReqDTO;
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
        if (devopsServiceReqDTO.getAppId() != null && devopsServiceE.getAppId() != null && devopsServiceReqDTO.getAppInstance() != null) {
            List<String> newInstanceCode = devopsServiceReqDTO.getAppInstance();
            List<String> oldInstanceCode = devopsServiceInstanceEList.stream().map(DevopsServiceAppInstanceE::getCode).collect(Collectors.toList());
            for (String instanceCode : newInstanceCode) {
                if (!oldInstanceCode.contains(instanceCode)) {
                    isUpdate = true;
                }
            }
        }

        if (devopsServiceReqDTO.getAppId() == null && devopsServiceE.getAppId() == null) {
            if (devopsServiceReqDTO.getLabel() != null && devopsServiceE.getLabels() != null) {
                if (!gson.toJson(devopsServiceReqDTO.getLabel()).equals(devopsServiceE.getLabels())) {
                    isUpdate = true;
                }
            } else if (devopsServiceReqDTO.getEndPoints() != null && devopsServiceE.getEndPoints() != null) {
                if (!gson.toJson(devopsServiceReqDTO.getEndPoints()).equals(devopsServiceE.getEndPoints())) {
                    isUpdate = true;
                }
            } else {
                isUpdate = true;
            }
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

    private DevopsEnvCommandVO createDevopsEnvCommandE(String type) {
        DevopsEnvCommandVO devopsEnvCommandE = new DevopsEnvCommandVO();
        if (type.equals("create")) {
            devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
        } else {
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        }
        devopsEnvCommandE.setObject(ObjectType.SERVICE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandRepository.create(devopsEnvCommandE);
    }


}
