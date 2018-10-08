package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServicePort;
import io.kubernetes.client.models.V1ServiceSpec;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsServiceDTO;
import io.choerodon.devops.api.dto.DevopsServiceReqDTO;
import io.choerodon.devops.api.validator.DevopsServiceValidator;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.devops.infra.common.util.enums.ServiceStatus;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.helper.EnvListener;

/**
 * Created by Zenger on 2018/4/13.
 */
@Service
@Transactional(rollbackFor = RuntimeException.class)
public class DevopsServiceServiceImpl implements DevopsServiceService {

    public static final String SERVICE = "Service";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    private static final String SERVICE_LABLE = "choerodon.io/network";
    private static final String SERVICE_LABLE_VALUE = "service";
    private Gson gson = new Gson();
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
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private EnvListener envListener;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private DevopsEnvironmentRepository environmentRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DevopsEnvCommitRepository devopsEnvCommitRepository;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;

    @Override
    public Boolean checkName(Long projectId, Long envId, String name) {
        return devopsServiceRepository.checkName(projectId, envId, name);
    }

    @Override
    public Page<DevopsServiceDTO> listDevopsServiceByPage(Long projectId, PageRequest pageRequest, String searchParam) {
        return listByEnv(projectId, null, pageRequest, searchParam);
    }

    @Override
    public Page<DevopsServiceDTO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String searchParam) {
        Page<DevopsServiceV> devopsServiceByPage = devopsServiceRepository.listDevopsServiceByPage(
                projectId, envId, pageRequest, searchParam);
        List<Long> connectedEnvList = envUtil.getConnectedEnvList(envListener);
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList(envListener);
        devopsServiceByPage.stream().forEach(devopsServiceV -> {
            if (connectedEnvList.contains(devopsServiceV.getEnvId())
                    && updatedEnvList.contains(devopsServiceV.getEnvId())) {
                devopsServiceV.setEnvStatus(true);
            }
        });
        return ConvertPageHelper.convertPage(devopsServiceByPage, DevopsServiceDTO.class);
    }

    @Override
    public List<DevopsServiceDTO> listDevopsService(Long envId) {
        return ConvertHelper.convertList(
                devopsServiceRepository.listDevopsService(envId), DevopsServiceDTO.class);
    }

    @Override
    public DevopsServiceDTO query(Long id) {
        return ConvertHelper.convert(devopsServiceRepository.selectById(id), DevopsServiceDTO.class);
    }

    @Override
    public Boolean insertDevopsService(Long projectId, DevopsServiceReqDTO devopsServiceReqDTO) {
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsServiceReqDTO.getEnvId(), envListener);

        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = new ArrayList<>();
        List<Long> beforeDevopsServiceAppInstanceES = new ArrayList<>();
        //处理创建service对象数据
        DevopsServiceE devopsServiceE = handlerCreateService(devopsServiceReqDTO, projectId, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(CREATE);

        //初始化V1Service对象
        V1Service v1Service = initV1Service(
                devopsServiceReqDTO,
                gson.fromJson(devopsServiceE.getAnnotations(), Map.class));

        //在gitops库处理service文件
        operateEnvGitLabFile(v1Service, true, devopsServiceE, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES, devopsEnvCommandE);
        return true;
    }

    @Override
    public Boolean insertDevopsServiceByGitOps(Long projectId, DevopsServiceReqDTO devopsServiceReqDTO, Long userId) {
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsServiceReqDTO.getEnvId(), envListener);
        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = new ArrayList<>();
        List<Long> beforeDevopsServiceAppInstanceES = new ArrayList<>();

        //处理创建service对象数据
        DevopsServiceE devopsServiceE = handlerCreateService(devopsServiceReqDTO, projectId, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(CREATE);

        //存储service对象到数据库
        devopsServiceE = devopsServiceRepository.insert(devopsServiceE);

        //存储service和instance对象关系到数据库
        Long serviceEId = devopsServiceE.getId();
        devopsEnvCommandE.setObjectId(serviceEId);
        devopsEnvCommandE.setCreatedBy(userId);
        devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsServiceRepository.update(devopsServiceE);

        devopsServiceAppInstanceES.stream().forEach(devopsServiceAppInstanceE -> {
            devopsServiceAppInstanceE.setServiceId(serviceEId);
            devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
        });
        return true;
    }

    private DevopsServiceE handlerCreateService(DevopsServiceReqDTO devopsServiceReqDTO, Long projectId, List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES, List<Long> beforeDevopsServiceAppInstanceES) {

        //校验service相关参数
        DevopsServiceValidator.checkService(devopsServiceReqDTO);

        initDevopsServicePorts(devopsServiceReqDTO);
        DevopsEnvironmentE devopsEnvironmentE =
                devopsEnviromentRepository.queryById(devopsServiceReqDTO.getEnvId());
        if (!devopsServiceRepository.checkName(projectId, devopsEnvironmentE.getId(), devopsServiceReqDTO.getName())) {
            throw new CommonException("error.service.name.exist");
        }
        if (devopsServiceReqDTO.getAppId() != null) {
            checkOptions(devopsServiceReqDTO.getEnvId(), devopsServiceReqDTO.getAppId(),
                    null);
        }

        //初始化DevopsService对象
        DevopsServiceE devopsServiceE = new DevopsServiceE();
        BeanUtils.copyProperties(devopsServiceReqDTO, devopsServiceE);
        return initDevopsService(devopsServiceE, devopsServiceReqDTO, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);

    }

    private DevopsServiceE initDevopsService(DevopsServiceE devopsServiceE, DevopsServiceReqDTO devopsServiceReqDTO, List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES, List<Long> beforeDevopsServiceAppInstanceES) {
        devopsServiceE.setAppId(devopsServiceReqDTO.getAppId());

        if (devopsServiceReqDTO.getLabel() != null) {
            devopsServiceE.setLabels(gson.toJson(devopsServiceReqDTO.getLabel()));
        } else {
            devopsServiceRepository.setLablesToNull(devopsServiceE.getId());
            devopsServiceE.setLabels(null);
        }
        devopsServiceE.setPorts(devopsServiceReqDTO.getPorts());
        devopsServiceE.setType(devopsServiceReqDTO.getType() == null ? "ClusterIP" : devopsServiceReqDTO.getType());
        devopsServiceE.setExternalIp(devopsServiceReqDTO.getExternalIp());

        String serviceInstances = updateServiceInstanceAndGetCode(devopsServiceReqDTO, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);
        Map<String, String> annotations = new HashMap<>();
        if (!serviceInstances.isEmpty()) {
            annotations.put("choerodon.io/network-service-instances", serviceInstances);
        }

        devopsServiceE.setAnnotations(gson.toJson(annotations));
        devopsServiceE.setStatus(ServiceStatus.OPERATIING.getStatus());

        return devopsServiceE;

    }

    private DevopsServiceE handlerUpdateService(DevopsServiceReqDTO devopsServiceReqDTO, DevopsServiceE devopsServiceE, List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES, List<Long> beforeDevopsServiceAppInstanceES) {
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

        //验证网络是否需要更新
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
                isUpdate = !devopsServiceReqDTO.getAppInstance().stream()
                        .sorted().collect(Collectors.toList())
                        .equals(devopsServiceInstanceEList.stream()
                                .map(DevopsServiceAppInstanceE::getAppInstanceId).sorted()
                                .collect(Collectors.toList()));
            }
        }
        if ((devopsServiceReqDTO.getAppId() == null && devopsServiceE.getAppId() != null) || (devopsServiceReqDTO.getAppId() != null && devopsServiceE.getAppId() == null)) {
            isUpdate = true;
        }
        if (devopsServiceReqDTO.getAppId() == null && devopsServiceE.getAppId() == null) {
            isUpdate = !gson.toJson(devopsServiceReqDTO.getLabel()).equals(devopsServiceE.getLabels());
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
    public Boolean updateDevopsService(Long projectId, Long id,
                                       DevopsServiceReqDTO devopsServiceReqDTO) {
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsServiceReqDTO.getEnvId(), envListener);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(UPDATE);

        //处理更新service对象数据
        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = new ArrayList<>();
        List<Long> beforeDevopsServiceAppInstanceES = devopsServiceInstanceRepository
                .selectByServiceId(id).stream().map(DevopsServiceAppInstanceE::getAppInstanceId).collect(Collectors.toList());
        DevopsServiceE devopsServiceE = devopsServiceRepository.query(id);
        devopsServiceE = handlerUpdateService(devopsServiceReqDTO, devopsServiceE, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);
        if (devopsServiceE == null) {
            return false;
        } else {
            //初始化V1Service对象
            V1Service v1Service = initV1Service(
                    devopsServiceReqDTO,
                    gson.fromJson(devopsServiceE.getAnnotations(), Map.class));
            //在gitops库处理service文件
            operateEnvGitLabFile(v1Service, false, devopsServiceE, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES, devopsEnvCommandE);
        }
        return true;
    }


    @Override
    public Boolean updateDevopsServiceByGitOps(Long projectId, Long id,
                                               DevopsServiceReqDTO devopsServiceReqDTO, Long userId) {
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsServiceReqDTO.getEnvId(), envListener);


        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(UPDATE);

        //处理更新service对象数据
        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = new ArrayList<>();
        List<Long> beforeDevopsServiceAppInstanceES = devopsServiceInstanceRepository
                .selectByServiceId(id).stream().map(DevopsServiceAppInstanceE::getAppInstanceId).collect(Collectors.toList());
        DevopsServiceE devopsServiceE = devopsServiceRepository.query(id);
        devopsServiceE = handlerUpdateService(devopsServiceReqDTO, devopsServiceE, devopsServiceAppInstanceES, beforeDevopsServiceAppInstanceES);
        if (devopsServiceE == null) {
            return false;
        }
        //更新service对象到数据库
        devopsEnvCommandE.setObjectId(id);
        devopsEnvCommandE.setCreatedBy(userId);
        devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsServiceRepository.update(devopsServiceE);


        //更新service和instance关联关系数据到数据库
        beforeDevopsServiceAppInstanceES.stream().forEach(instanceId ->
                devopsServiceInstanceRepository.deleteByOptions(id, instanceId)
        );
        devopsServiceAppInstanceES.stream().forEach(devopsServiceAppInstanceE -> {
            devopsServiceAppInstanceE.setServiceId(id);
            devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
        });


        return true;
    }

    @Override
    public void deleteDevopsService(Long id) {
        DevopsServiceE devopsServiceE = getDevopsServiceE(id);

        //校验环境是否链接
        envUtil.checkEnvConnection(devopsServiceE.getEnvId(), envListener);

        DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(devopsServiceE.getEnvId());
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(DELETE);

        devopsEnvCommandE.setObjectId(id);
        devopsServiceE.setStatus(ServiceStatus.OPERATIING.getStatus());
        devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsServiceRepository.update(devopsServiceE);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        //查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(devopsEnvironmentE.getId(), id, SERVICE);
        if (devopsEnvFileResourceE == null) {
            throw new CommonException("error.fileResource.not.exist");
        }
        List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository.queryByEnvIdAndPath(devopsEnvironmentE.getId(), devopsEnvFileResourceE.getFilePath());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceES.size() == 1) {
            gitlabRepository.deleteFile(
                    TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                    devopsEnvFileResourceE.getFilePath(),
                    "DELETE FILE",
                    TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        } else {
            ObjectOperation<V1Service> objectOperation = new ObjectOperation<>();
            V1Service v1Service = new V1Service();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsServiceE.getName());
            v1Service.setMetadata(v1ObjectMeta);
            objectOperation.setType(v1Service);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(
                    "release-" + devopsServiceE.getName(),
                    projectId,
                    DELETE,
                    userAttrE.getGitlabUserId(),
                    devopsServiceE.getId(), SERVICE, devopsEnvironmentE.getId(), path);
        }

    }


    @Override
    public void deleteDevopsServiceByGitOps(Long id) {
        DevopsServiceE devopsServiceE = getDevopsServiceE(id);
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsServiceE.getEnvId(), envListener);

        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsServiceE.getCommandId());

        //更新数据
        devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        devopsServiceRepository.delete(id);
    }

    /**
     * 获取实例
     *
     * @param devopsServiceReqDTO 网络参数
     * @return String
     */
    private String updateServiceInstanceAndGetCode(DevopsServiceReqDTO devopsServiceReqDTO,
                                                   List<DevopsServiceAppInstanceE> addDevopsServiceAppInstanceES,
                                                   List<Long> beforedevopsServiceAppInstanceES) {
        StringBuilder stringBuffer = new StringBuilder();
        List<Long> appInstances = devopsServiceReqDTO.getAppInstance();
        if (appInstances != null) {
            appInstances.forEach(appInstance -> {
                checkOptions(devopsServiceReqDTO.getEnvId(), devopsServiceReqDTO.getAppId(),
                        appInstance);
                ApplicationInstanceE applicationInstanceE =
                        applicationInstanceRepository.selectById(appInstance);
                stringBuffer.append(applicationInstanceE.getCode()).append("+");
                if (beforedevopsServiceAppInstanceES.contains(appInstance)) {
                    beforedevopsServiceAppInstanceES.remove(appInstance);
                    return;
                }
                if (applicationInstanceE == null) {
                    throw new CommonException("error.instance.query");
                }
                DevopsServiceAppInstanceE devopsServiceAppInstanceE = new DevopsServiceAppInstanceE();
                devopsServiceAppInstanceE.setAppInstanceId(appInstance);
                devopsServiceAppInstanceE.setCode(applicationInstanceE.getCode());
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
     * 校验参数
     *
     * @param envId         环境id
     * @param appId         应用id
     * @param appInstanceId 应用实例id
     */
    private void checkOptions(Long envId, Long appId, Long appInstanceId) {
        if (applicationInstanceRepository.checkOptions(envId, appId, appInstanceId) == 0) {
            throw new CommonException("error.instances.query");
        }
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
                    v1ServicePort.setName(t.getName() == null ? "http" + serialNumber[0]++ : t.getName());
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
        DevopsServiceE devopsServiceE = devopsServiceRepository.query(id);
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


    private void operateEnvGitLabFile(V1Service service, Boolean isCreate,
                                      DevopsServiceE devopsServiceE,
                                      List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES,
                                      List<Long> beforeDevopsServiceAppInstanceES,
                                      DevopsEnvCommandE devopsEnvCommandE) {

        DevopsEnvironmentE devopsEnvironmentE =
                devopsEnviromentRepository.queryById(devopsServiceE.getEnvId());
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        //进行Service的数据库操作
        if (isCreate) {
            Long serviceId = devopsServiceRepository.insert(devopsServiceE).getId();
            devopsEnvCommandE.setObjectId(serviceId);
            devopsServiceE.setId(serviceId);
            devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsServiceRepository.update(devopsServiceE);
        } else {
            devopsEnvCommandE.setObjectId(devopsServiceE.getId());
            devopsServiceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsServiceRepository.update(devopsServiceE);
        }
        Long serviceId = devopsServiceE.getId();
        if (beforeDevopsServiceAppInstanceES != null) {
            beforeDevopsServiceAppInstanceES.stream().forEach(instanceId ->
                    devopsServiceInstanceRepository.deleteByOptions(serviceId, instanceId)
            );
        }
        devopsServiceAppInstanceES.stream().forEach(devopsServiceAppInstanceE -> {
            devopsServiceAppInstanceE.setServiceId(serviceId);
            devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
        });


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        //处理文件
        ObjectOperation<V1Service> objectOperation = new ObjectOperation<>();
        objectOperation.setType(service);
        objectOperation.operationEnvGitlabFile("svc-" + devopsServiceE.getName(), TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), isCreate ? CREATE : UPDATE,
                userAttrE.getGitlabUserId(), devopsServiceE.getId(), SERVICE, devopsServiceE.getEnvId(), path);

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


    private DevopsEnvCommandE initDevopsEnvCommandE(String type) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
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
}
