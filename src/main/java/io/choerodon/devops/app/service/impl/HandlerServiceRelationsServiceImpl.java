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
    private AppServiceService appServiceService;
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
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
        //删除service,和文件对象关联关系
        beforeService.forEach(serviceName -> {
            DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQueryByNameAndEnvId(serviceName, envId);
            if (devopsServiceDTO != null) {
                devopsServiceService.deleteDevopsServiceByGitOps(devopsServiceDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, devopsServiceDTO.getId(), SERVICE);
            }
        });
        //新增service
        addService(objectPath, envId, projectId, addV1Service, v1Endpoints, path, userId);
        //更新service
        updateService(objectPath, envId, projectId, updateV1Service, v1Endpoints, path, userId);
    }

    @Override
    public Class<V1Service> getTarget() {
        return V1Service.class;
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
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsServiceDTO.getCommandId());
                        String currentFileCommit = GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath);
                        // 如果相等，就代表是由界面更新的service，不需要再更新service
                        if (!currentFileCommit.equals(devopsEnvCommandDTO.getSha())) {
                            devopsServiceService.updateDevopsServiceByGitOps(projectId, devopsServiceDTO.getId(), devopsServiceReqVO, userId);
                            devopsServiceDTO = devopsServiceService.baseQuery(devopsServiceDTO.getId());
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsServiceDTO.getCommandId());
                            devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), currentFileCommit);
                        }


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
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

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
                    keyBuilder.append(v1Endpoints1.getSubsets().get(0).getAddresses().get(i).getIp()).append(",");
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

        Map<String, String> selector = v1Service.getSpec().getSelector();
        if (selector != null) {
            // 判断是实例的选择器还是其它
            String value;
            if (selector.size() == 1) {
                // 如果有且只有一个具体实例的选择器
                if ((value = selector.get(AppServiceInstanceService.INSTANCE_LABEL_RELEASE)) != null) {
                    devopsServiceReqVO.setTargetInstanceCode(value);
                    // 如果有且只有一个具体应用的选择器
                } else if ((value = selector.get(AppServiceInstanceService.INSTANCE_LABEL_APP_SERVICE_ID)) != null) {
                    devopsServiceReqVO.setTargetAppServiceId(TypeUtil.objToLong(value));
                    // 如果是一个选择器但不是具体应用，网络为选择器类型
                } else {
                    devopsServiceReqVO.setSelectors(v1Service.getSpec().getSelector());
                }
            } else {
                // 如果超过一个选择器，即使包含实例和应用服务的选择器也判断为选择器类型
                devopsServiceReqVO.setSelectors(v1Service.getSpec().getSelector());
            }
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
        // 如果都是实例类型的网络，比较实例对象是否有区别
        if (devopsServiceReqVO.getAppServiceId() != null && devopsServiceDTO.getAppServiceId() != null && devopsServiceReqVO.getTargetInstanceCode() != null) {
            List<String> newInstanceCode = new ArrayList<>();
            newInstanceCode.add(devopsServiceReqVO.getTargetInstanceCode());
            List<String> oldInstanceCode = new ArrayList<>();
            if (devopsServiceDTO.getTargetInstanceCode() == null && devopsServiceDTO.getTargetAppServiceId() == null) {
                oldInstanceCode = devopsServiceInstanceDTOS.stream().map(DevopsServiceInstanceDTO::getCode).collect(Collectors.toList());
            } else {
                oldInstanceCode.add(devopsServiceDTO.getTargetInstanceCode());
            }
            for (String instanceCode : newInstanceCode) {
                if (!oldInstanceCode.contains(instanceCode)) {
                    isUpdate = true;
                }
            }
        }

        if (devopsServiceReqVO.getAppServiceId() == null && devopsServiceDTO.getAppServiceId() == null) {
            if (devopsServiceReqVO.getSelectors() != null && devopsServiceDTO.getSelectors() != null) {
                if (!gson.toJson(devopsServiceReqVO.getSelectors()).equals(devopsServiceDTO.getSelectors())) {
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
