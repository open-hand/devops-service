package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.choerodon.base.domain.Sort;
import io.choerodon.devops.api.vo.DevopsServiceVO;
import io.choerodon.devops.infra.dto.DevopsServiceDTO;
import io.choerodon.devops.infra.dto.DevopsServiceQueryDTO;
import io.choerodon.devops.infra.mapper.DevopsServiceMapper;
import io.kubernetes.client.JSON;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.DevopsServiceReqDTO;
import io.choerodon.devops.api.validator.DevopsServiceValidator;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ServiceStatus;


/**
 * Created by Zenger on 2018/4/13.
 */
@Service
public class DevopsServiceServiceImpl implements DevopsServiceService {

    public static final String ENDPOINTS = "Endpoints";
    public static final String LOADBALANCER = "LoadBalancer";
    public static final String SERVICE = "Service";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    private static final String SERVICE_LABLE = "choerodon.io/network";
    private static final String SERVICE_LABLE_VALUE = "service";
    private Gson gson = new Gson();
    private JSON json = new JSON();
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnviromentRepository;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DevopsServiceInstanceRepository devopsServiceInstanceRepository;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private DevopsEnvironmentRepository environmentRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsApplicationResourceRepository appResourceRepository;\
    @Autowired
    private DevopsServiceMapper devopsServiceMapper;


    @Override
    public Boolean checkName(Long envId, String name) {
        return devopsServiceRepository.baseCheckName(envId, name);
    }


    @Override
    public PageInfo<DevopsServiceVO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String searchParam) {
        PageInfo<DevopsServiceV> devopsServiceByPage = devopsServiceRepository.basePageByOptions(
                projectId, envId, null, pageRequest, searchParam,null);
        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        devopsServiceByPage.getList().forEach(devopsServiceV -> {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(devopsServiceV.getEnvId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                devopsServiceV.setEnvStatus(true);
            }
        });
        return ConvertPageHelper.convertPageInfo(devopsServiceByPage, DevopsServiceVO.class);
    }


    @Override
    public PageInfo<DevopsServiceVO> listByInstanceId(Long projectId, Long instanceId, PageRequest pageRequest, Long appId) {
        PageInfo<DevopsServiceV> devopsServiceByPage = devopsServiceRepository.basePageByOptions(
                projectId, null, instanceId, pageRequest, null,appId);
        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        if (!devopsServiceByPage.getList().isEmpty()) {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(devopsServiceByPage.getList().get(0).getEnvId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                devopsServiceByPage.getList().stream().forEach(devopsServiceV -> {
                    devopsServiceV.setEnvStatus(true);
                });
            }
            devopsServiceByPage.getList().stream().forEach(devopsServiceV -> {
                PageInfo<DevopsIngressVO> devopsIngressDTOS = devopsIngressRepository
                        .basePageByOptions(projectId, null, devopsServiceV.getId(), new PageRequest(0, 100), "");
                if (devopsServiceV.getEnvStatus() != null && devopsServiceV.getEnvStatus()) {
                    devopsIngressDTOS.getList().stream().forEach(devopsIngressDTO -> devopsIngressDTO.setEnvStatus(true));

                }
                devopsServiceV.setDevopsIngressVOS(devopsIngressDTOS.getList());
            });

        }


        return ConvertPageHelper.convertPageInfo(devopsServiceByPage, DevopsServiceVO.class);
    }

    @Override
    public DevopsServiceVO queryByName(Long envId, String serviceName) {
        return ConvertHelper.convert(devopsServiceRepository.baseQueryByNameAndEnvId(serviceName, envId), DevopsServiceVO.class);
    }

    @Override
    public List<DevopsServiceVO> listDevopsService(Long envId) {
        return ConvertHelper.convertList(
                devopsServiceRepository.baseListByEnvId(envId), DevopsServiceVO.class);
    }

    @Override
    public DevopsServiceVO query(Long id) {
        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = devopsServiceInstanceRepository.baseListByServiceId(id);
        //网络多实例中存在删除实例时，给应用信息赋值
        if (!devopsServiceAppInstanceES.isEmpty()) {
            for (DevopsServiceAppInstanceE devopsServiceAppInstanceE : devopsServiceAppInstanceES) {
                ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(devopsServiceAppInstanceE.getAppInstanceId());
                if (applicationInstanceE != null) {
                    ApplicationE applicationE = applicationRepository.query(applicationInstanceE.getApplicationE().getId());
                    DevopsServiceV devopsServiceV = devopsServiceRepository.baseQueryById(id);
                    devopsServiceV.setAppId(applicationE.getId());
                    devopsServiceV.setAppName(applicationE.getName());
                    devopsServiceV.setAppProjectId(applicationE.getProjectE().getId());
                    return ConvertHelper.convert(devopsServiceV, DevopsServiceVO.class);
                }
            }
        }
        return ConvertHelper.convert(devopsServiceRepository.baseQueryById(id), DevopsServiceVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertDevopsService(Long projectId, DevopsServiceReqDTO devopsServiceReqDTO) {

        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(devopsServiceReqDTO.getEnvId());

        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);


        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceES = new ArrayList<>();
        //处理创建service对象数据
        DevopsServiceE devopsServiceE = handlerCreateService(devopsServiceReqDTO, projectId, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);

        DevopsEnvCommandVO devopsEnvCommandE = initDevopsEnvCommandE(CREATE);

        //初始化V1Service对象
        V1Service v1Service = initV1Service(
                devopsServiceReqDTO,
                gson.fromJson(devopsServiceE.getAnnotations(), Map.class));
        V1Endpoints v1Endpoints = null;
        if (devopsServiceReqDTO.getEndPoints() != null) {
            v1Endpoints = initV1EndPoints(devopsServiceReqDTO);
        }
        //在gitops库处理service文件
        operateEnvGitLabFile(v1Service, v1Endpoints, true, devopsServiceE, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES, devopsEnvCommandE, userAttrE);

        //创建应用资源关系
        if (devopsServiceReqDTO.getAppId() != null) {
            // 应用下不能创建endpoints类型网络
            if (devopsServiceReqDTO.getEndPoints().size() != 0) {
                throw new CommonException("error.app.create.endpoints.service");
            }

            DevopsAppResourceE resourceE = new DevopsAppResourceE();
            resourceE.setAppId(devopsServiceReqDTO.getAppId());
            resourceE.setResourceType(ObjectType.SERVICE.getType());
            resourceE.setResourceId(devopsServiceE.getId());
            appResourceRepository.baseCreate(resourceE);
        }
        return true;
    }

    @Override
    public Boolean insertDevopsServiceByGitOps(Long projectId, DevopsServiceReqDTO devopsServiceReqDTO, Long userId) {
        //校验环境是否链接
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(devopsServiceReqDTO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());
        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceES = new ArrayList<>();

        //处理创建service对象数据
        DevopsServiceE devopsServiceE = handlerCreateService(devopsServiceReqDTO, projectId, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);

        DevopsEnvCommandVO devopsEnvCommandE = initDevopsEnvCommandE(CREATE);

        //存储service对象到数据库
        devopsServiceE = devopsServiceRepository.baseCreate(devopsServiceE);

        //存储service和instance对象关系到数据库
        Long serviceEId = devopsServiceE.getId();
        devopsEnvCommandE.setObjectId(serviceEId);
        devopsEnvCommandE.setCreatedBy(userId);
        devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsServiceRepository.baseUpdate(devopsServiceE);

        devopsServiceAppInstanceES.forEach(devopsServiceAppInstanceE -> {
            devopsServiceAppInstanceE.setServiceId(serviceEId);
            devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
        });
        return true;
    }

    private DevopsServiceE handlerCreateService(DevopsServiceReqDTO devopsServiceReqDTO, Long projectId, List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES, List<String> beforeDevopsServiceAppInstanceES) {

        //校验service相关参数
        DevopsServiceValidator.checkService(devopsServiceReqDTO);

        initDevopsServicePorts(devopsServiceReqDTO);

        DevopsEnvironmentE devopsEnvironmentE =
                devopsEnviromentRepository.baseQueryById(devopsServiceReqDTO.getEnvId());
        if (!devopsServiceRepository.checkName(devopsEnvironmentE.getId(), devopsServiceReqDTO.getName())) {
            throw new CommonException("error.service.name.exist");
        }

        //初始化DevopsService对象
        DevopsServiceE devopsServiceE = new DevopsServiceE();
        BeanUtils.copyProperties(devopsServiceReqDTO, devopsServiceE);
        return initDevopsService(devopsServiceE, devopsServiceReqDTO, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);

    }

    private DevopsServiceE initDevopsService(DevopsServiceE devopsServiceE, DevopsServiceReqDTO devopsServiceReqDTO, List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES, List<String> beforeDevopsServiceAppInstanceES) {
        devopsServiceE.setAppId(devopsServiceReqDTO.getAppId());
        ApplicationE applicationE = applicationRepository.query(devopsServiceReqDTO.getAppId());
        if (devopsServiceReqDTO.getLabel() != null) {
            if (devopsServiceReqDTO.getLabel().size() == 1 && devopsServiceReqDTO.getLabel().containsKey(SERVICE_LABLE)) {
                devopsServiceRepository.baseUpdateLables(devopsServiceE.getId());
                devopsServiceE.setLabels(null);
            } else {
                devopsServiceReqDTO.getLabel().remove(SERVICE_LABLE);
                devopsServiceE.setLabels(gson.toJson(devopsServiceReqDTO.getLabel()));
            }
        } else {
            devopsServiceRepository.baseUpdateLables(devopsServiceE.getId());
            devopsServiceE.setLabels(null);
        }
        if (devopsServiceReqDTO.getEndPoints() != null) {
            devopsServiceE.setEndPoints(gson.toJson(devopsServiceReqDTO.getEndPoints()));
        } else {
            devopsServiceRepository.baseUpdateEndPoint(devopsServiceE.getId());
            devopsServiceE.setEndPoints(null);
        }
        devopsServiceE.setPorts(devopsServiceReqDTO.getPorts());
        devopsServiceE.setType(devopsServiceReqDTO.getType() == null ? "ClusterIP" : devopsServiceReqDTO.getType());
        devopsServiceE.setExternalIp(devopsServiceReqDTO.getExternalIp());

        String serviceInstances = updateServiceInstanceAndGetCode(devopsServiceReqDTO, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);
        Map<String, String> annotations = new HashMap<>();
        if (!serviceInstances.isEmpty()) {
            annotations.put("choerodon.io/network-service-instances", serviceInstances);
            if (applicationE != null) {
                annotations.put("choerodon.io/network-service-app", applicationE.getCode());
            }
        }

        devopsServiceE.setAnnotations(gson.toJson(annotations));
        devopsServiceE.setStatus(ServiceStatus.OPERATIING.getStatus());

        return devopsServiceE;

    }

    private DevopsServiceE handlerUpdateService(DevopsServiceReqDTO devopsServiceReqDTO, DevopsServiceE devopsServiceE, List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES, List<String> beforeDevopsServiceAppInstanceES) {
        //service参数校验
        DevopsServiceValidator.checkService(devopsServiceReqDTO);
        initDevopsServicePorts(devopsServiceReqDTO);

        if (!devopsServiceE.getEnvId().equals(devopsServiceReqDTO.getEnvId())) {
            throw new CommonException("error.env.notEqual");
        }
        String serviceName = devopsServiceReqDTO.getName();
        if (!serviceName.equals(devopsServiceE.getName())) {
            throw new CommonException("error.name.notEqual");
        }
        //查询网络对应的实例
        List<DevopsServiceAppInstanceE> devopsServiceInstanceEList =
                devopsServiceInstanceRepository.baseListByServiceId(devopsServiceE.getId());
        //验证网络是否需要更新
        List<PortMapE> oldPort = devopsServiceE.getPorts();
        boolean isUpdate = false;
        if (devopsServiceReqDTO.getAppId() != null && devopsServiceE.getAppId() != null && devopsServiceReqDTO.getAppInstance() != null) {
            isUpdate = !devopsServiceReqDTO.getAppInstance().stream()
                    .sorted().collect(Collectors.toList())
                    .equals(devopsServiceInstanceEList.stream()
                            .map(DevopsServiceAppInstanceE::getCode).sorted()
                            .collect(Collectors.toList()));
        }
        if ((devopsServiceReqDTO.getAppId() == null && devopsServiceE.getAppId() != null) || (devopsServiceReqDTO.getAppId() != null && devopsServiceE.getAppId() == null)) {
            isUpdate = true;
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
        if (!isUpdate && oldPort.stream().sorted().collect(Collectors.toList())
                .equals(devopsServiceReqDTO.getPorts().stream().sorted().collect(Collectors.toList()))
                && !isUpdateExternalIp(devopsServiceReqDTO, devopsServiceE)) {
            return null;
        }


        //初始化DevopsService对象
        return initDevopsService(devopsServiceE, devopsServiceReqDTO, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDevopsService(Long projectId, Long id,
                                       DevopsServiceReqDTO devopsServiceReqDTO) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(devopsServiceReqDTO.getEnvId()
        );

        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);


        //更新网络的时候校验gitops库文件是否存在,处理部署网络时，由于没有创gitops文件导致的部署失败
        resourceFileCheckHandler.check(devopsEnvironmentE, id, devopsServiceReqDTO.getName(), SERVICE);

        DevopsEnvCommandVO devopsEnvCommandE = initDevopsEnvCommandE(UPDATE);

        //处理更新service对象数据
        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceES = devopsServiceInstanceRepository
                .baseListByServiceId(id).stream().map(DevopsServiceAppInstanceE::getCode).collect(Collectors.toList());
        DevopsServiceE devopsServiceE = devopsServiceRepository.baseQuery(id);
        devopsServiceE = handlerUpdateService(devopsServiceReqDTO, devopsServiceE, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);
        V1Endpoints v1Endpoints = null;
        if (devopsServiceE == null) {
            return false;
        } else {
            //初始化V1Service对象
            V1Service v1Service = initV1Service(
                    devopsServiceReqDTO,
                    gson.fromJson(devopsServiceE.getAnnotations(), Map.class));
            if (devopsServiceReqDTO.getEndPoints() != null) {
                v1Endpoints = initV1EndPoints(devopsServiceReqDTO);
            }
            //在gitops库处理service文件
            operateEnvGitLabFile(v1Service, v1Endpoints, false, devopsServiceE, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES, devopsEnvCommandE, userAttrE);
        }
        return true;
    }


    @Override
    public Boolean updateDevopsServiceByGitOps(Long projectId, Long id,
                                               DevopsServiceReqDTO devopsServiceReqDTO, Long userId) {
        //校验环境是否链接
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(devopsServiceReqDTO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());


        DevopsEnvCommandVO devopsEnvCommandE = initDevopsEnvCommandE(UPDATE);

        //处理更新service对象数据
        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceES = devopsServiceInstanceRepository
                .baseListByServiceId(id).stream().map(DevopsServiceAppInstanceE::getCode).collect(Collectors.toList());
        DevopsServiceE devopsServiceE = devopsServiceRepository.baseQuery(id);
        devopsServiceE = handlerUpdateService(devopsServiceReqDTO, devopsServiceE, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);
        if (devopsServiceE == null) {
            return false;
        }
        //更新service对象到数据库
        devopsEnvCommandE.setObjectId(id);
        devopsEnvCommandE.setCreatedBy(userId);
        devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsServiceRepository.baseUpdate(devopsServiceE);


        //更新service和instance关联关系数据到数据库
        beforeDevopsServiceAppInstanceES.forEach(instanceCode ->
                devopsServiceInstanceRepository.baseDeleteByOptions(id, instanceCode)
        );
        devopsServiceAppInstanceES.forEach(devopsServiceAppInstanceE -> {
            devopsServiceAppInstanceE.setServiceId(id);
            devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
        });


        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevopsService(Long id) {
        DevopsServiceE devopsServiceE = getDevopsServiceE(id);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(devopsServiceE.getEnvId()
        );

        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

        DevopsEnvCommandVO devopsEnvCommandE = initDevopsEnvCommandE(DELETE);

        devopsEnvCommandE.setObjectId(id);
        devopsServiceE.setStatus(ServiceStatus.OPERATIING.getStatus());
        devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsServiceRepository.baseUpdate(devopsServiceE);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentE.getProjectE().getId(), devopsEnvironmentE.getCode(), devopsEnvironmentE.getEnvIdRsa());

        //查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceVO devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentE.getId(), id, SERVICE);
        if (devopsEnvFileResourceE == null) {
            devopsServiceRepository.baseDelete(id);
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    "svc-" + devopsServiceE.getName() + ".yaml")) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        "svc-" + devopsServiceE.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
            //删除网络的关联关系
            appResourceRepository.baseDeleteByResourceIdAndType(id, ObjectType.SERVICE.getType());
            return;
        } else {
            if (!gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceE.getFilePath())) {

                devopsServiceRepository.baseDelete(id);
                devopsEnvFileResourceRepository.deleteFileResource(devopsEnvFileResourceE.getId());
                //删除网络的关联关系
                appResourceRepository.baseDeleteByResourceIdAndType(id, ObjectType.SERVICE.getType());
                return;
            }
        }
        List<DevopsEnvFileResourceVO> devopsEnvFileResourceES = devopsEnvFileResourceRepository.baseQueryByEnvIdAndPath(devopsEnvironmentE.getId(), devopsEnvFileResourceE.getFilePath());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceES.size() == 1) {
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceE.getFilePath())) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        devopsEnvFileResourceE.getFilePath(),
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
        } else {
            ResourceConvertToYamlHandler<V1Service> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1Service v1Service = new V1Service();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsServiceE.getName());
            v1Service.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1Service);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    "release-" + devopsServiceE.getName(),
                    projectId,
                    DELETE,
                    userAttrE.getGitlabUserId(),
                    devopsServiceE.getId(), SERVICE, null, false, devopsEnvironmentE.getId(), path);
        }

    }


    @Override
    public void deleteDevopsServiceByGitOps(Long id) {
        DevopsServiceE devopsServiceE = getDevopsServiceE(id);
        //校验环境是否链接
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(devopsServiceE.getEnvId());
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        //更新数据

        devopsEnvCommandRepository.baseListByObjectAll(ObjectType.SERVICE.getType(), devopsServiceE.getId()).forEach(devopsEnvCommandE -> devopsEnvCommandRepository.baseDeleteCommandById(devopsEnvCommandE));
        devopsServiceRepository.baseDelete(id);

        //删除网络的关联关系
        appResourceRepository.baseDeleteByResourceIdAndType(id, ObjectType.SERVICE.getType());
    }

    /**
     * 获取实例
     *
     * @param devopsServiceReqDTO 网络参数
     * @return String
     */
    private String updateServiceInstanceAndGetCode(DevopsServiceReqDTO devopsServiceReqDTO,
                                                   List<DevopsServiceAppInstanceE> addDevopsServiceAppInstanceES,
                                                   List<String> beforedevopsServiceAppInstanceES) {
        StringBuilder stringBuffer = new StringBuilder();
        List<String> appInstances = devopsServiceReqDTO.getAppInstance();
        if (appInstances != null) {
            appInstances.forEach(appInstance -> {
                ApplicationInstanceE applicationInstanceE =
                        applicationInstanceRepository.selectByCode(appInstance, devopsServiceReqDTO.getEnvId());
                stringBuffer.append(appInstance).append("+");
                if (beforedevopsServiceAppInstanceES.contains(appInstance)) {
                    beforedevopsServiceAppInstanceES.remove(appInstance);
                    return;
                }
                DevopsServiceAppInstanceE devopsServiceAppInstanceE = new DevopsServiceAppInstanceE();
                if (applicationInstanceE != null) {
                    devopsServiceAppInstanceE.setAppInstanceId(applicationInstanceE.getId());
                }
                devopsServiceAppInstanceE.setCode(appInstance);
                addDevopsServiceAppInstanceES.add(devopsServiceAppInstanceE);
            });
        }
        String instancesCode = stringBuffer.toString();
        if (instancesCode.endsWith("+")) {
            return instancesCode.substring(0, stringBuffer.toString().lastIndexOf('+'));
        }
        return instancesCode;
    }


    /**
     * 获取k8s service的yaml格式
     */
    private V1Service initV1Service(DevopsServiceReqDTO devopsServiceReqDTO, Map<String, String> annotations) {
        V1Service service = new V1Service();
        service.setKind(SERVICE);
        service.setApiVersion("v1");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsServiceReqDTO.getName());
        metadata.setAnnotations(annotations);
        Map<String, String> label = new HashMap<>();
        label.put(SERVICE_LABLE, SERVICE_LABLE_VALUE);
        metadata.setLabels(label);
        service.setMetadata(metadata);

        V1ServiceSpec spec = new V1ServiceSpec();
        spec.setType(devopsServiceReqDTO.getType() == null ? "ClusterIP" : devopsServiceReqDTO.getType());
        spec.setSelector(devopsServiceReqDTO.getLabel());
        final Integer[] serialNumber = {0};
        List<V1ServicePort> ports = devopsServiceReqDTO.getPorts().stream()
                .map(t -> {
                    V1ServicePort v1ServicePort = new V1ServicePort();
                    if (t.getNodePort() != null) {
                        v1ServicePort.setNodePort(t.getNodePort().intValue());
                    }
                    if (t.getPort() != null) {
                        v1ServicePort.setPort(t.getPort().intValue());
                    }
                    if (t.getTargetPort() != null) {
                        v1ServicePort.setTargetPort(new IntOrString(t.getTargetPort()));
                    }
                    serialNumber[0] = serialNumber[0] + 1;
                    v1ServicePort.setName(t.getName() == null ? "http" + serialNumber[0] : t.getName());
                    v1ServicePort.setProtocol(t.getProtocol() == null ? "TCP" : t.getProtocol());
                    return v1ServicePort;
                }).collect(Collectors.toList());

        if (!StringUtils.isEmpty(devopsServiceReqDTO.getExternalIp())) {
            List<String> externalIps = new ArrayList<>(
                    Arrays.asList(devopsServiceReqDTO.getExternalIp().split(",")));
            spec.setExternalIPs(externalIps);
        }

        spec.setPorts(ports);
        spec.setSessionAffinity("None");
        service.setSpec(spec);

        return service;
    }

    private V1Endpoints initV1EndPoints(DevopsServiceReqDTO devopsServiceReqDTO) {
        V1Endpoints v1Endpoints = new V1Endpoints();
        v1Endpoints.setApiVersion("v1");
        v1Endpoints.setKind(ENDPOINTS);
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(devopsServiceReqDTO.getName());
        v1Endpoints.setMetadata(v1ObjectMeta);
        List<V1EndpointSubset> v1EndpointSubsets = new ArrayList<>();
        V1EndpointSubset v1EndpointSubset = new V1EndpointSubset();
        devopsServiceReqDTO.getEndPoints().forEach((key, value) -> {
            List<String> ips = Arrays.asList(key.split(","));
            v1EndpointSubset.setAddresses(ips.stream().map(ip -> {
                V1EndpointAddress v1EndpointAddress = new V1EndpointAddress();
                v1EndpointAddress.setIp(ip);
                return v1EndpointAddress;

            }).collect(Collectors.toList()));
            final Integer[] serialNumber = {0};
            v1EndpointSubset.setPorts(value.stream().map(port -> {
                V1EndpointPort v1EndpointPort = new V1EndpointPort();
                v1EndpointPort.setPort(port.getPort());
                serialNumber[0] = serialNumber[0] + 1;
                v1EndpointPort.setName(port.getName() == null ? "http" + serialNumber[0] : port.getName());
                return v1EndpointPort;
            }).collect(Collectors.toList()));
            v1EndpointSubsets.add(v1EndpointSubset);
        });
        v1Endpoints.setSubsets(v1EndpointSubsets);
        return v1Endpoints;
    }


    /**
     * 判断外部ip是否更新
     */
    private Boolean isUpdateExternalIp(DevopsServiceReqDTO devopsServiceReqDTO, DevopsServiceE devopsServiceE) {
        return !((StringUtils.isEmpty(devopsServiceReqDTO.getExternalIp())
                && StringUtils.isEmpty(devopsServiceE.getExternalIp()))
                || (!StringUtils.isEmpty(devopsServiceReqDTO.getExternalIp())
                && !StringUtils.isEmpty(devopsServiceE.getExternalIp())
                && devopsServiceReqDTO.getExternalIp().equals(devopsServiceE.getExternalIp())));
    }

    /**
     * 查询网络信息
     */
    private DevopsServiceE getDevopsServiceE(Long id) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.baseQuery(id);
        if (devopsServiceE == null) {
            throw new CommonException("error.service.query");
        }
        return devopsServiceE;
    }

    /**
     * 查询应用
     *
     * @param id 应用id
     * @return app
     */
    public ApplicationE getApplicationE(long id) {
        ApplicationE applicationE = applicationRepository.query(id);
        if (applicationE == null) {
            throw new CommonException("error.application.query");
        }
        return applicationE;
    }


    private void operateEnvGitLabFile(V1Service service, V1Endpoints v1Endpoints, Boolean isCreate,
                                      DevopsServiceE devopsServiceE,
                                      List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES,
                                      List<String> beforeDevopsServiceAppInstanceES,
                                      DevopsEnvCommandVO devopsEnvCommandE,
                                      UserAttrE userAttrE) {

        DevopsEnvironmentE devopsEnvironmentE =
                devopsEnviromentRepository.baseQueryById(devopsServiceE.getEnvId());

        //操作网络数据库操作
        if (isCreate) {
            Long serviceId = devopsServiceRepository.baseCreate(devopsServiceE).getId();
            devopsEnvCommandE.setObjectId(serviceId);
            devopsServiceE.setId(serviceId);
            devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsServiceRepository.baseUpdate(devopsServiceE);
            if (beforeDevopsServiceAppInstanceES != null) {
                beforeDevopsServiceAppInstanceES.forEach(instanCode ->
                        devopsServiceInstanceRepository.baseDeleteByOptions(serviceId, instanCode)
                );
            }
            devopsServiceAppInstanceES.forEach(devopsServiceAppInstanceE -> {
                devopsServiceAppInstanceE.setServiceId(serviceId);
                devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
            });
        } else {
            devopsEnvCommandE.setObjectId(devopsServiceE.getId());
            devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsServiceRepository.baseUpdate(devopsServiceE);
            Long serviceId = devopsServiceE.getId();
            if (beforeDevopsServiceAppInstanceES != null) {
                beforeDevopsServiceAppInstanceES.forEach(instanceCode ->
                        devopsServiceInstanceRepository.baseDeleteByOptions(serviceId, instanceCode)
                );
            }
            devopsServiceAppInstanceES.forEach(devopsServiceAppInstanceE -> {
                devopsServiceAppInstanceE.setServiceId(serviceId);
                devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
            });
        }

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentE.getProjectE().getId(), devopsEnvironmentE.getCode(), devopsEnvironmentE.getEnvIdRsa());

        //处理文件
        ResourceConvertToYamlHandler<V1Service> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(service);
        resourceConvertToYamlHandler.operationEnvGitlabFile("svc-" + devopsServiceE.getName(), TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), isCreate ? CREATE : UPDATE,
                userAttrE.getGitlabUserId(), devopsServiceE.getId(), SERVICE, v1Endpoints, false, devopsServiceE.getEnvId(), path);


    }


    private void initDevopsServicePorts(DevopsServiceReqDTO devopsServiceReqDTO) {
        final Integer[] serialNumber = {0};
        devopsServiceReqDTO.setPorts(devopsServiceReqDTO.getPorts().stream()
                .map(t -> {
                    PortMapE portMapE = new PortMapE();
                    portMapE.setNodePort(t.getNodePort());
                    portMapE.setPort(t.getPort());
                    portMapE.setTargetPort(t.getTargetPort());
                    portMapE.setName(t.getName() == null ? "http" + ++serialNumber[0] : t.getName());
                    portMapE.setProtocol(t.getProtocol() == null ? "TCP" : t.getProtocol());
                    return portMapE;
                })
                .collect(Collectors.toList()));
    }


    private DevopsEnvCommandVO initDevopsEnvCommandE(String type) {
        DevopsEnvCommandVO devopsEnvCommandE = new DevopsEnvCommandVO();
        if (type.equals(CREATE)) {
            devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
        } else if (type.equals(UPDATE)) {
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        } else {
            devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
        }
        devopsEnvCommandE.setObject(ObjectType.SERVICE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandE;
    }



    public Boolean baseCheckName(Long envId, String name) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setEnvId(envId);
        devopsServiceDTO.setName(name);
        if (devopsServiceMapper.selectOne(devopsServiceDTO) != null) {
            return false;
        }
        return true;
    }

    public PageInfo<DevopsServiceQueryDTO> basePageByOptions(Long projectId, Long envId, Long instanceId, PageRequest pageRequest,
                                                      String searchParam, Long appId) {

        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("name")) {
                            property = "ds.`name`";
                        } else if (property.equals("envName")) {
                            property = "env_name";
                        } else if (property.equals("externalIp")) {
                            property = "ds.external_ip";
                        } else if (property.equals("targetPort")) {
                            property = "ds.target_port";
                        } else if (property.equals("appName")) {
                            property = "app_name";
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        int start = getBegin(pageRequest.getPage(), pageRequest.getSize());
        int stop = start + pageRequest.getSize();
        //分页组件暂不支持级联查询，只能手写分页
        PageInfo<DevopsServiceQueryDTO> result = new PageInfo();
        result.setPageSize(pageRequest.getSize());
        result.setPageNum(pageRequest.getPage());
        int count;
        List<DevopsServiceQueryDTO> devopsServiceQueryDTOS;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            count = devopsServiceMapper.selectCountByName(
                    projectId, envId, instanceId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), appId);

            result.setTotal(count);
            devopsServiceQueryDTOS = devopsServiceMapper.listDevopsServiceByPage(
                    projectId, envId, instanceId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), sortResult, appId);
            result.setList(devopsServiceQueryDTOS.subList(start, stop > devopsServiceQueryDTOS.size() ? devopsServiceQueryDTOS.size() : stop));
        } else {
            count = devopsServiceMapper
                    .selectCountByName(projectId, envId, instanceId, null, null, appId);
            result.setTotal(count);
            devopsServiceQueryDTOS =
                    devopsServiceMapper.listDevopsServiceByPage(
                            projectId, envId, instanceId, null, null, sortResult, appId);
            result.setList(devopsServiceQueryDTOS.subList(start, stop > devopsServiceQueryDTOS.size() ? devopsServiceQueryDTOS.size() : stop));
        }
        if (devopsServiceQueryDTOS.size() < pageRequest.getSize() * pageRequest.getPage()) {
            result.setSize(TypeUtil.objToInt(devopsServiceQueryDTOS.size()) - (pageRequest.getSize() * (pageRequest.getPage() - 1)));
        } else {
            result.setSize(pageRequest.getSize());
        }
        return result;
    }

    private Boolean checkServiceParam(String key) {
        return key.equals("id") || key.equals("name") || key.equals("status");
    }

    public List<DevopsServiceQueryDTO> baseListByEnvId(Long envId) {
        List<DevopsServiceQueryDTO> devopsServiceQueryDTOList = devopsServiceMapper.listByEnvId(envId);
        return devopsServiceQueryDTOList;
    }

    public DevopsServiceQueryDTO baseQueryById(Long id) {
        return devopsServiceMapper.queryById(id);
    }

    public DevopsServiceDTO insert(DevopsServiceDTO devopsServiceDTO) {
        if (devopsServiceMapper.insert(devopsServiceDTO) != 1) {
            throw new CommonException("error.k8s.service.create");
        }
        return devopsServiceDTO;
    }

    public DevopsServiceDTO baseQuery(Long id) {
        return devopsServiceMapper.selectByPrimaryKey(id);
    }

    public void baseDelete(Long id) {
        devopsServiceMapper.deleteByPrimaryKey(id);
    }

    public void baseUpdate(DevopsServiceDTO devopsServiceDTO) {
        DevopsServiceDTO oldDevopsServiceDTO = devopsServiceMapper.selectByPrimaryKey(devopsServiceDTO.getId());
        if (devopsServiceDTO.getLabels() == null) {
            devopsServiceMapper.updateLables(devopsServiceDTO.getId());
        }
        if (devopsServiceDTO.getExternalIp() == null) {
            devopsServiceMapper.setExternalIpNull(devopsServiceDTO.getId());
        }
        devopsServiceDTO.setObjectVersionNumber(oldDevopsServiceDTO.getObjectVersionNumber());
        if (devopsServiceMapper.updateByPrimaryKeySelective(devopsServiceDTO) != 1) {
            throw new CommonException("error.k8s.service.update");
        }
    }

    public void baseUpdateLables(Long id) {
        devopsServiceMapper.updateLables(id);
    }

    public void baseUpdateEndPoint(Long id) {
        devopsServiceMapper.updateEndPoint(id);
    }

    public List<Long> baseListEnvByRunningService() {
        return devopsServiceMapper.selectDeployedEnv();
    }

    public DevopsServiceDTO baseQueryByNameAndEnvId(String name, Long envId) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setName(name);
        devopsServiceDTO.setEnvId(envId);
        return devopsServiceMapper.selectOne(devopsServiceDTO);
    }

    public Boolean baseCheckServiceByEnv(Long envId) {
        return devopsServiceMapper.checkServiceByEnv(envId);
    }

    public List<DevopsServiceDTO> baseList() {
        return devopsServiceMapper.selectAll();
    }

    public void baseDeleteServiceAndInstanceByEnvId(Long envId) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setEnvId(envId);
        // 环境下的serviceIds
        List<Long> serviceIds = devopsServiceMapper.select(devopsServiceDTO).stream().map(DevopsServiceDTO::getId)
                .collect(Collectors.toList());
        devopsServiceMapper.delete(devopsServiceDTO);
        if (!serviceIds.isEmpty()) {
            devopsServiceMapper.deleteServiceInstance(serviceIds);
        }
    }


    public static int getBegin(int page, int size) {
        page = page <= 1 ? 1 : page;
        return (page - 1) * size;
    }
}
