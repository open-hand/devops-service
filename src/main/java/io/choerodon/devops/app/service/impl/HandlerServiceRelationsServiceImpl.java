package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.api.vo.EndPointPortVO;
import io.choerodon.devops.api.validator.DevopsServiceValidator;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ObjectType;

import com.google.gson.reflect.TypeToken;
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
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsServiceInstanceService devopsServiceInstanceService;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync, List<V1Service> v1Services, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeService = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(SERVICE))
                .map(devopsEnvFileResourceE -> {
                    DevopsServiceDTO devopsServiceDTO = devopsServiceService
                            .baseQuery(devopsEnvFileResourceE.getResourceId());
                    if (devopsServiceDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), SERVICE);
                        return null;
                    }
                    return devopsServiceDTO.getName();
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
            DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQueryByNameAndEnvId(serviceName, envId);
            if (devopsServiceDTO != null) {
                devopsServiceService.deleteDevopsServiceByGitOps(devopsServiceDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, devopsServiceDTO.getId(), SERVICE);
            }
        });
    }


    private void updateService(Map<String, String> objectPath, Long envId, Long projectId, List<V1Service> updateV1Service, List<V1Endpoints> v1Endpoints, String path, Long userId) {
        updateV1Service.stream()
                .forEach(v1Service -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(v1Service.hashCode()));

                        DevopsServiceDTO devopsServiceDTO = devopsServiceService
                                .baseQueryByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                        checkServiceName(v1Service);
                        //初始化网络参数,更新网络和网络关联关系
                        DevopsServiceReqVO devopsServiceReqVO = getDevopsServiceDTO(
                                v1Service,
                                v1Endpoints,
                                envId);
                        Boolean isNotChange = checkIsNotChange(devopsServiceDTO, devopsServiceReqVO);
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsServiceDTO.getCommandId());
                        if (!isNotChange) {
                            devopsServiceService.updateDevopsServiceByGitOps(projectId, devopsServiceDTO.getId(), devopsServiceReqVO, userId);
                            DevopsServiceDTO newDevopsServiceE = devopsServiceService
                                    .baseQueryByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(newDevopsServiceE.getCommandId());
                        }

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, devopsServiceDTO.getId(), v1Service.getKind());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceDTO,
                                v1Service.hashCode(), devopsServiceDTO.getId(), v1Service.getKind());
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
                        DevopsServiceDTO devopsServiceDTO = devopsServiceService
                                .baseQueryByNameAndEnvId(v1Service.getMetadata().getName(), envId);
                        DevopsServiceReqVO devopsServiceReqVO;
                        //初始化网络参数,创建时判断网络是否存在，存在则直接创建文件对象关联关系
                        if (devopsServiceDTO == null) {
                            devopsServiceReqVO = getDevopsServiceDTO(
                                    v1Service,
                                    v1Endpoints,
                                    envId);
                            devopsServiceService.insertDevopsServiceByGitOps(projectId, devopsServiceReqVO, userId);
                            devopsServiceDTO = devopsServiceService.baseQueryByNameAndEnvId(
                                    devopsServiceReqVO.getName(), envId);
                        }
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsServiceDTO.getCommandId());

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, v1Service.hashCode(), devopsServiceDTO.getId(),
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


    private DevopsServiceReqVO getDevopsServiceDTO(V1Service v1Service,
                                                   List<V1Endpoints> v1Endpoints,
                                                   Long envId) {
        DevopsServiceReqVO devopsServiceReqVO = new DevopsServiceReqVO();
        if (v1Service.getSpec().getExternalIPs() != null) {
            devopsServiceReqVO.setExternalIp(String.join(",", v1Service.getSpec().getExternalIPs()));
        }
        Map<String, List<EndPointPortVO>> endPoints = new HashMap<>();
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
                EndPointPortVO endPointPortVO = new EndPointPortVO();
                endPointPortVO.setName(v1EndpointPort.getName());
                endPointPortVO.setPort(v1EndpointPort.getPort());
                return endPointPortVO;
            }).collect(Collectors.toList()));
            devopsServiceReqVO.setEndPoints(endPoints);
        });

        devopsServiceReqVO.setName(v1Service.getMetadata().getName());
        devopsServiceReqVO.setType(v1Service.getSpec().getType());
        devopsServiceReqVO.setEnvId(envId);


        List<PortMapVO> portMapList = v1Service.getSpec().getPorts().stream()
                .map(t -> {
                    PortMapVO portMap = new PortMapVO();
                    portMap.setName(t.getName());
                    if (t.getNodePort() != null) {
                        portMap.setNodePort(t.getNodePort().longValue());
                    }
                    portMap.setPort(t.getPort().longValue());
                    portMap.setProtocol(t.getProtocol());
                    portMap.setTargetPort(TypeUtil.objToString(t.getTargetPort()));
                    return portMap;
                }).collect(Collectors.toList());
        devopsServiceReqVO.setPorts(portMapList);

        if (v1Service.getMetadata().getAnnotations() != null) {
            String instancesCode = v1Service.getMetadata().getAnnotations()
                    .get("choerodon.io/network-service-instances");
            if (instancesCode != null) {
                List<String> instanceIdList = Arrays.stream(instancesCode.split("\\+")).parallel().map(t -> {
                    AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(t, envId);
                    if (appServiceInstanceDTO != null) {
                        devopsServiceReqVO.setAppServiceId(appServiceInstanceDTO.getAppServiceId());
                    }
                    return t;
                }).collect(Collectors.toList());
                devopsServiceReqVO.setInstances(instanceIdList);
            }
        }
        if (v1Service.getSpec().getSelector() != null) {
            devopsServiceReqVO.setLabel(v1Service.getSpec().getSelector());
        }
        return devopsServiceReqVO;
    }


    private void checkServiceName(
            V1Service v1Service) {
        try {
            DevopsServiceValidator.checkName(v1Service.getMetadata().getName());
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    private Boolean checkIsNotChange(DevopsServiceDTO devopsServiceDTO, DevopsServiceReqVO devopsServiceReqVO) {
        List<PortMapVO> oldPort = gson.fromJson(devopsServiceDTO.getPorts(), new TypeToken<ArrayList<PortMapVO>>() {}.getType());
        //查询网络对应的实例
        List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS =
                devopsServiceInstanceService.baseListByServiceId(devopsServiceDTO.getId());
        Boolean isUpdate = false;
        if (devopsServiceReqVO.getAppServiceId() != null && devopsServiceDTO.getAppServiceId() != null && devopsServiceReqVO.getInstances() != null) {
            List<String> newInstanceCode = devopsServiceReqVO.getInstances();
            List<String> oldInstanceCode = devopsServiceInstanceDTOS.stream().map(DevopsServiceInstanceDTO::getCode).collect(Collectors.toList());
            for (String instanceCode : newInstanceCode) {
                if (!oldInstanceCode.contains(instanceCode)) {
                    isUpdate = true;
                }
            }
        }

        if (devopsServiceReqVO.getAppServiceId() == null && devopsServiceDTO.getAppServiceId() == null) {
            if (devopsServiceReqVO.getLabel() != null && devopsServiceDTO.getLabels() != null) {
                if (!gson.toJson(devopsServiceReqVO.getLabel()).equals(devopsServiceDTO.getLabels())) {
                    isUpdate = true;
                }
            } else if (devopsServiceReqVO.getEndPoints() != null && devopsServiceDTO.getEndPoints() != null) {
                if (!gson.toJson(devopsServiceReqVO.getEndPoints()).equals(devopsServiceDTO.getEndPoints())) {
                    isUpdate = true;
                }
            } else {
                isUpdate = true;
            }
        }
        if ((devopsServiceReqVO.getAppServiceId() == null && devopsServiceDTO.getAppServiceId() != null) || (devopsServiceReqVO.getAppServiceId() != null && devopsServiceDTO.getAppServiceId() == null)) {
            isUpdate = true;
        }
        return !isUpdate && oldPort.stream().sorted().collect(Collectors.toList())
                .equals(devopsServiceReqVO.getPorts().stream().sorted().collect(Collectors.toList()))
                && !isUpdateExternalIp(devopsServiceReqVO, devopsServiceDTO);
    }

    private Boolean isUpdateExternalIp(DevopsServiceReqVO devopsServiceReqVO, DevopsServiceDTO devopsServiceDTO) {
        return !((StringUtils.isEmpty(devopsServiceReqVO.getExternalIp())
                && StringUtils.isEmpty(devopsServiceDTO.getExternalIp()))
                || (!StringUtils.isEmpty(devopsServiceReqVO.getExternalIp())
                && !StringUtils.isEmpty(devopsServiceDTO.getExternalIp())
                && devopsServiceReqVO.getExternalIp().equals(devopsServiceDTO.getExternalIp())));
    }

    private DevopsEnvCommandDTO createDevopsEnvCommandE(String type) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        if (type.equals("create")) {
            devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType());
        } else {
            devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        }
        devopsEnvCommandDTO.setObject(ObjectType.SERVICE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
    }


}
