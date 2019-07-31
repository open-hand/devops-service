package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.DevopsServiceValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.ServiceSagaPayLoad;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ServiceStatus;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsServiceMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.JSON;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
    public static final String SERVICE_RREFIX = "svc-";
    private static final String SERVICE_LABLE = "choerodon.io/network";
    private static final String SERVICE_LABLE_VALUE = "service";
    private Gson gson = new Gson();
    private JSON json = new JSON();
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private ApplicationSevriceService applicationService;
    @Autowired
    private DevopsServiceInstanceService devopsServiceInstanceService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsApplicationResourceService devopsApplicationResourceService;
    @Autowired
    private DevopsServiceMapper devopsServiceMapper;
    @Autowired
    private TransactionalProducer producer;


    @Override
    public Boolean checkName(Long envId, String name) {
        return baseCheckName(envId, name);
    }


    @Override
    public PageInfo<DevopsServiceVO> pageByEnv(Long projectId, Long envId, PageRequest pageRequest, String searchParam, Long appServiceId) {
        PageInfo<DevopsServiceVO> devopsServiceByPage = ConvertUtils.convertPage(basePageByOptions(
                projectId, envId, null, pageRequest, searchParam, appServiceId), this::queryDtoToVo);
        return devopsServiceByPage;
    }

    @Override
    public PageInfo<DevopsServiceVO> pageByInstance(Long projectId, Long instanceId, PageRequest pageRequest, Long appId) {
        PageInfo<DevopsServiceVO> devopsServiceByPage = ConvertUtils.convertPage(basePageByOptions(
                projectId, null, instanceId, pageRequest, null, appId), this::queryDtoToVo);
        if (!devopsServiceByPage.getList().isEmpty()) {
            devopsServiceByPage.getList().forEach(devopsServiceVO -> {
                PageInfo<DevopsIngressVO> devopsIngressVOPageInfo = devopsIngressService
                        .basePageByOptions(projectId, null, devopsServiceVO.getId(), new PageRequest(0, 100), "");
                devopsServiceVO.setDevopsIngressVOS(devopsIngressVOPageInfo.getList());
            });
        }
        return devopsServiceByPage;
    }


    @Override
    public DevopsServiceVO queryByName(Long envId, String serviceName) {
        return ConvertUtils.convertObject(baseQueryByNameAndEnvId(serviceName, envId), DevopsServiceVO.class);
    }

    @Override
    public List<DevopsServiceVO> listByEnvId(Long envId) {
        return ConvertUtils.convertList(baseListRunningService(envId), this::queryDtoToVo);
    }

    @Override
    public DevopsServiceVO query(Long id) {
        List<DevopsServiceAppInstanceDTO> devopsServiceAppInstanceDTOS = devopsServiceInstanceService.baseListByServiceId(id);
        //网络多实例中存在删除实例时，给应用信息赋值
        if (!devopsServiceAppInstanceDTOS.isEmpty()) {
            for (DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO : devopsServiceAppInstanceDTOS) {
                ApplicationInstanceDTO applicationInstanceDTO = applicationInstanceService.baseQuery(devopsServiceAppInstanceDTO.getAppInstanceId());
                if (applicationInstanceDTO != null) {
                    ApplicationServiceDTO applicationDTO = applicationService.baseQuery(applicationInstanceDTO.getAppServiceId());
                    DevopsServiceQueryDTO devopsServiceQueryDTO = baseQueryById(id);
                    devopsServiceQueryDTO.setAppServiceId(applicationDTO.getId());
                    devopsServiceQueryDTO.setAppName(applicationDTO.getName());
                    devopsServiceQueryDTO.setAppProjectId(applicationDTO.getProjectId());
                    return queryDtoToVo(devopsServiceQueryDTO);
                }
            }
        }
        return queryDtoToVo(baseQueryById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_SERVICE,
            description = "Devops创建网络", inputSchema = "{}")
    public Boolean create(Long projectId, DevopsServiceReqVO devopsServiceReqVO) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceReqVO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        List<DevopsServiceAppInstanceDTO> devopsServiceAppInstanceDTOS = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceDTOS = new ArrayList<>();
        //处理创建service对象数据
        DevopsServiceDTO devopsServiceDTO = handlerCreateService(devopsServiceReqVO, devopsServiceAppInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CREATE);

        //初始化V1Service对象
        V1Service v1Service = initV1Service(
                devopsServiceReqVO,
                gson.fromJson(devopsServiceDTO.getAnnotations(), Map.class));
        V1Endpoints v1Endpoints = null;
        if (devopsServiceReqVO.getEndPoints() != null) {
            v1Endpoints = initV1EndPoints(devopsServiceReqVO);
        }

        //创建应用资源关系
        if (devopsServiceReqVO.getAppServiceId() != null) {

            // 应用下不能创建endpoints类型网络
            if (devopsServiceReqVO.getEndPoints().size() != 0) {
                throw new CommonException("error.app.create.endpoints.service");
            }
            DevopsApplicationResourceDTO devopsApplicationResourceDTO = new DevopsApplicationResourceDTO();
            devopsApplicationResourceDTO.setAppServiceId(devopsServiceReqVO.getAppServiceId());
            devopsApplicationResourceDTO.setResourceType(ObjectType.SERVICE.getType());
            devopsApplicationResourceDTO.setResourceId(devopsServiceDTO.getId());
            devopsApplicationResourceService.baseCreate(devopsApplicationResourceDTO);
        }

        //在gitops库处理service文件
        operateEnvGitLabFile(v1Service, v1Endpoints, true, devopsServiceDTO, devopsServiceAppInstanceDTOS, beforeDevopsServiceAppInstanceDTOS, devopsEnvCommandDTO, userAttrDTO);
        return true;
    }


    @Override
    public Boolean insertDevopsServiceByGitOps(Long projectId, DevopsServiceReqVO devopsServiceReqVO, Long userId) {
        //校验环境是否链接
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceReqVO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());
        List<DevopsServiceAppInstanceDTO> devopsServiceAppInstanceDTOS = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceDTOS = new ArrayList<>();

        //处理创建service对象数据
        DevopsServiceDTO devopsServiceDTO = handlerCreateService(devopsServiceReqVO, devopsServiceAppInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CREATE);

        //存储service对象到数据库
        devopsServiceDTO = baseCreate(devopsServiceDTO);

        //存储service和instance对象关系到数据库
        Long serviceId = devopsServiceDTO.getId();
        devopsEnvCommandDTO.setObjectId(serviceId);
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsServiceDTO);

        devopsServiceAppInstanceDTOS.forEach(devopsServiceAppInstanceDTO -> {
            devopsServiceAppInstanceDTO.setServiceId(serviceId);
            devopsServiceInstanceService.baseCreate(devopsServiceAppInstanceDTO);
        });
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(Long projectId, Long id,
                          DevopsServiceReqVO devopsServiceReqVO) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceReqVO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);


        //更新网络的时候校验gitops库文件是否存在,处理部署网络时，由于没有创gitops文件导致的部署失败
        resourceFileCheckHandler.check(devopsEnvironmentDTO, id, devopsServiceReqVO.getName(), SERVICE);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(UPDATE);

        //处理更新service对象数据
        List<DevopsServiceAppInstanceDTO> devopsServiceAppInstanceDTOS = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceDTOS = devopsServiceInstanceService
                .baseListByServiceId(id).stream().map(DevopsServiceAppInstanceDTO::getCode).collect(Collectors.toList());
        DevopsServiceDTO devopsServiceDTO = baseQuery(id);
        devopsServiceDTO = handlerUpdateService(devopsServiceReqVO, devopsServiceDTO, devopsServiceAppInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);
        V1Endpoints v1Endpoints = null;
        if (devopsServiceDTO == null) {
            return false;
        } else {
            //初始化V1Service对象
            V1Service v1Service = initV1Service(
                    devopsServiceReqVO,
                    gson.fromJson(devopsServiceDTO.getAnnotations(), Map.class));
            if (devopsServiceReqVO.getEndPoints() != null) {
                v1Endpoints = initV1EndPoints(devopsServiceReqVO);
            }
            //在gitops库处理service文件
            operateEnvGitLabFile(v1Service, v1Endpoints, false, devopsServiceDTO, devopsServiceAppInstanceDTOS, beforeDevopsServiceAppInstanceDTOS, devopsEnvCommandDTO, userAttrDTO);
        }
        return true;
    }


    @Override
    public Boolean updateDevopsServiceByGitOps(Long projectId, Long id,
                                               DevopsServiceReqVO devopsServiceReqVO, Long userId) {
        //校验环境是否链接
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceReqVO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());


        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(UPDATE);

        //处理更新service对象数据
        List<DevopsServiceAppInstanceDTO> devopsServiceAppInstanceDTOS = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceDTOS = devopsServiceInstanceService
                .baseListByServiceId(id).stream().map(DevopsServiceAppInstanceDTO::getCode).collect(Collectors.toList());
        DevopsServiceDTO devopsServiceDTO = baseQuery(id);
        devopsServiceDTO = handlerUpdateService(devopsServiceReqVO, devopsServiceDTO, devopsServiceAppInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);
        if (devopsServiceDTO == null) {
            return false;
        }
        //更新service对象到数据库
        devopsEnvCommandDTO.setObjectId(id);
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsServiceDTO);


        //更新service和instance关联关系数据到数据库
        beforeDevopsServiceAppInstanceDTOS.forEach(instanceCode ->
                devopsServiceInstanceService.baseDeleteByOptions(id, instanceCode)
        );
        devopsServiceAppInstanceDTOS.forEach(devopsServiceAppInstanceDTO -> {
            devopsServiceAppInstanceDTO.setServiceId(id);
            devopsServiceInstanceService.baseCreate(devopsServiceAppInstanceDTO);
        });


        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DevopsServiceDTO devopsServiceDTO = baseQuery(id);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(DELETE);

        devopsEnvCommandDTO.setObjectId(id);
        devopsServiceDTO.setStatus(ServiceStatus.OPERATIING.getStatus());
        devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsServiceDTO);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());

        //查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), id, SERVICE);
        if (devopsEnvFileResourceDTO == null) {
            baseDelete(id);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    "svc-" + devopsServiceDTO.getName() + ".yaml")) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        "svc-" + devopsServiceDTO.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
            //删除网络的关联关系
            devopsApplicationResourceService.baseDeleteByResourceIdAndType(id, ObjectType.SERVICE.getType());
            return;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceDTO.getFilePath())) {

                baseDelete(id);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                //删除网络的关联关系
                devopsApplicationResourceService.baseDeleteByResourceIdAndType(id, ObjectType.SERVICE.getType());
                return;
            }
        }
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService.baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceDTOS.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(),
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        } else {
            ResourceConvertToYamlHandler<V1Service> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1Service v1Service = new V1Service();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsServiceDTO.getName());
            v1Service.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1Service);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    "release-" + devopsServiceDTO.getName(),
                    projectId,
                    DELETE,
                    userAttrDTO.getGitlabUserId(),
                    devopsServiceDTO.getId(), SERVICE, null, false, devopsEnvironmentDTO.getId(), path);
        }

    }


    @Override
    public void deleteDevopsServiceByGitOps(Long id) {
        DevopsServiceDTO devopsServiceDTO = baseQuery(id);
        //校验环境是否链接
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceDTO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        //更新数据

        devopsEnvCommandService.baseListByObject(ObjectType.SERVICE.getType(), devopsServiceDTO.getId()).forEach(devopsEnvCommandDTO -> devopsEnvCommandService.baseDelete(devopsEnvCommandDTO.getId()));
        baseDelete(id);

        //删除网络的关联关系
        devopsApplicationResourceService.baseDeleteByResourceIdAndType(id, ObjectType.SERVICE.getType());
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
        PageInfo<DevopsServiceQueryDTO> result = new PageInfo<>();
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

    public List<DevopsServiceQueryDTO> baseListRunningService(Long envId) {
        List<DevopsServiceQueryDTO> devopsServiceQueryDTOList = devopsServiceMapper.listRunningService(envId);
        return devopsServiceQueryDTOList;
    }

    @Override
    public List<DevopsServiceDTO> baseListByEnvId(Long envId) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setEnvId(envId);
        return devopsServiceMapper.select(devopsServiceDTO);
    }

    public DevopsServiceQueryDTO baseQueryById(Long id) {
        return devopsServiceMapper.queryById(id);
    }

    public DevopsServiceDTO baseCreate(DevopsServiceDTO devopsServiceDTO) {
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
            devopsServiceMapper.setLabelsToNull(devopsServiceDTO.getId());
        }
        if (devopsServiceDTO.getExternalIp() == null) {
            devopsServiceMapper.setExternalIpNull(devopsServiceDTO.getId());
        }
        devopsServiceDTO.setObjectVersionNumber(oldDevopsServiceDTO.getObjectVersionNumber());
        if (devopsServiceMapper.updateByPrimaryKeySelective(devopsServiceDTO) != 1) {
            throw new CommonException("error.k8s.service.update");
        }
    }

    public void baseUpdateLabels(Long id) {
        devopsServiceMapper.setLabelsToNull(id);
    }

    public void baseUpdateEndPoint(Long id) {
        devopsServiceMapper.setEndPointToNull(id);
    }

    public List<Long> baseListEnvByRunningService() {
        return devopsServiceMapper.selectDeployedEnv();
    }

    @Override
    public DevopsServiceDTO baseQueryByNameAndEnvId(String name, Long envId) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setName(name);
        devopsServiceDTO.setEnvId(envId);
        return devopsServiceMapper.selectOne(devopsServiceDTO);
    }

    @Override
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


    private int getBegin(int page, int size) {
        page = page <= 1 ? 1 : page;
        return (page - 1) * size;
    }


    /**
     * 获取实例
     *
     * @param devopsServiceReqVO 网络参数
     * @return String
     */
    private String updateServiceInstanceAndGetCode(DevopsServiceReqVO devopsServiceReqVO,
                                                   List<DevopsServiceAppInstanceDTO> addDevopsServiceAppInstanceDTOS,
                                                   List<String> beforedevopsServiceAppInstanceDTOS) {
        StringBuilder stringBuffer = new StringBuilder();
        List<String> appInstances = devopsServiceReqVO.getAppInstance();
        if (appInstances != null) {
            appInstances.forEach(appInstance -> {
                ApplicationInstanceDTO applicationInstanceDTO =
                        applicationInstanceService.baseQueryByCodeAndEnv(appInstance, devopsServiceReqVO.getEnvId());
                stringBuffer.append(appInstance).append("+");
                if (beforedevopsServiceAppInstanceDTOS.contains(appInstance)) {
                    beforedevopsServiceAppInstanceDTOS.remove(appInstance);
                    return;
                }
                DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO = new DevopsServiceAppInstanceDTO();
                if (applicationInstanceDTO != null) {
                    devopsServiceAppInstanceDTO.setAppInstanceId(applicationInstanceDTO.getId());
                }
                devopsServiceAppInstanceDTO.setCode(appInstance);
                addDevopsServiceAppInstanceDTOS.add(devopsServiceAppInstanceDTO);
            });
        }
        String instancesCode = stringBuffer.toString();
        if (instancesCode.endsWith("+")) {
            return instancesCode.substring(0, stringBuffer.toString().lastIndexOf('+'));
        }
        return instancesCode;
    }


    private DevopsServiceDTO handlerCreateService(DevopsServiceReqVO devopsServiceReqVO, List<DevopsServiceAppInstanceDTO> devopsServiceAppInstanceDTOS, List<String> beforeDevopsServiceAppInstanceDTOS) {

        //校验service相关参数
        DevopsServiceValidator.checkService(devopsServiceReqVO);

        initDevopsServicePorts(devopsServiceReqVO);

        DevopsEnvironmentDTO devopsEnvironmentDTO =
                devopsEnvironmentService.baseQueryById(devopsServiceReqVO.getEnvId());
        if (!baseCheckName(devopsEnvironmentDTO.getId(), devopsServiceReqVO.getName())) {
            throw new CommonException("error.service.name.exist");
        }

        //初始化DevopsService对象
        DevopsServiceDTO devopsServiceDTO = voToDto(devopsServiceReqVO);
        return initDevopsService(devopsServiceDTO, devopsServiceReqVO, devopsServiceAppInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);

    }


    private DevopsServiceDTO voToDto(DevopsServiceReqVO devopsServiceReqVO) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        BeanUtils.copyProperties(devopsServiceReqVO, devopsServiceDTO);
        devopsServiceDTO.setPorts(gson.toJson(devopsServiceReqVO.getPorts()));
        return devopsServiceDTO;
    }


    private DevopsServiceVO queryDtoToVo(DevopsServiceQueryDTO devopsServiceQueryDTO) {
        DevopsServiceVO devopsServiceVO = new DevopsServiceVO();
        BeanUtils.copyProperties(devopsServiceQueryDTO, devopsServiceVO);

        DevopsServiceConfigVO devopsServiceConfigVO = new DevopsServiceConfigVO();
        devopsServiceConfigVO.setPorts(gson.fromJson(devopsServiceQueryDTO.getPorts(), new TypeToken<ArrayList<PortMapVO>>() {
        }.getType()));
        if (devopsServiceQueryDTO.getExternalIp() != null) {
            devopsServiceConfigVO.setExternalIps(new ArrayList<>(
                    Arrays.asList(devopsServiceQueryDTO.getExternalIp().split(","))));
        }
        devopsServiceVO.setConfig(devopsServiceConfigVO);

        DevopsServiceTargetVO devopsServiceTargetVO = new DevopsServiceTargetVO();
        devopsServiceTargetVO.setAppInstance(devopsServiceQueryDTO.getAppInstance());
        devopsServiceTargetVO.setLabels(gson.fromJson(devopsServiceQueryDTO.getEndPoints(), new TypeToken<Map<String, List<EndPointPortVO>>>() {
        }.getType()));
        devopsServiceTargetVO.setEndPoints(gson.fromJson(devopsServiceQueryDTO.getLabels(), new TypeToken<Map<String, String>>() {
        }.getType()));
        devopsServiceVO.setTarget(devopsServiceTargetVO);
        return devopsServiceVO;
    }

    private DevopsServiceDTO initDevopsService(DevopsServiceDTO devopsServiceDTO, DevopsServiceReqVO devopsServiceReqVO, List<DevopsServiceAppInstanceDTO> devopsServiceAppInstanceDTOS, List<String> beforeDevopsServiceAppInstanceDTOS) {
        devopsServiceDTO.setAppServiceId(devopsServiceReqVO.getAppServiceId());
        ApplicationServiceDTO applicationDTO = applicationService.baseQuery(devopsServiceReqVO.getAppServiceId());
        if (devopsServiceReqVO.getLabel() != null) {
            if (devopsServiceReqVO.getLabel().size() == 1 && devopsServiceReqVO.getLabel().containsKey(SERVICE_LABLE)) {
                baseUpdateLabels(devopsServiceDTO.getId());
                devopsServiceDTO.setLabels(null);
            } else {
                devopsServiceReqVO.getLabel().remove(SERVICE_LABLE);
                devopsServiceDTO.setLabels(gson.toJson(devopsServiceReqVO.getLabel()));
            }
        } else {
            baseUpdateLabels(devopsServiceDTO.getId());
            devopsServiceDTO.setLabels(null);
        }
        if (devopsServiceReqVO.getEndPoints() != null) {
            devopsServiceDTO.setEndPoints(gson.toJson(devopsServiceReqVO.getEndPoints()));
        } else {
            baseUpdateEndPoint(devopsServiceDTO.getId());
            devopsServiceDTO.setEndPoints(null);
        }
        devopsServiceDTO.setPorts(gson.toJson(devopsServiceReqVO.getPorts()));
        devopsServiceDTO.setType(devopsServiceReqVO.getType() == null ? "ClusterIP" : devopsServiceReqVO.getType());
        devopsServiceDTO.setExternalIp(devopsServiceReqVO.getExternalIp());

        String serviceInstances = updateServiceInstanceAndGetCode(devopsServiceReqVO, devopsServiceAppInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);
        Map<String, String> annotations = new HashMap<>();
        if (!serviceInstances.isEmpty()) {
            annotations.put("choerodon.io/network-service-instances", serviceInstances);
            if (applicationDTO != null) {
                annotations.put("choerodon.io/network-service-app", applicationDTO.getCode());
            }
        }

        devopsServiceDTO.setAnnotations(gson.toJson(annotations));
        devopsServiceDTO.setStatus(ServiceStatus.OPERATIING.getStatus());

        return devopsServiceDTO;

    }

    private DevopsServiceDTO handlerUpdateService(DevopsServiceReqVO devopsServiceReqVO, DevopsServiceDTO devopsServiceDTO, List<DevopsServiceAppInstanceDTO> devopsServiceAppInstanceDTOS, List<String> beforeDevopsServiceAppInstanceDTOS) {
        //service参数校验
        DevopsServiceValidator.checkService(devopsServiceReqVO);
        initDevopsServicePorts(devopsServiceReqVO);

        if (!devopsServiceDTO.getEnvId().equals(devopsServiceReqVO.getEnvId())) {
            throw new CommonException("error.env.notEqual");
        }
        String serviceName = devopsServiceReqVO.getName();
        if (!serviceName.equals(devopsServiceDTO.getName())) {
            throw new CommonException("error.name.notEqual");
        }
        //查询网络对应的实例
        List<DevopsServiceAppInstanceDTO> oldDevopsServiceAppInstanceDTOS =
                devopsServiceInstanceService.baseListByServiceId(devopsServiceDTO.getId());
        //验证网络是否需要更新
        List<PortMapVO> oldPort = gson.fromJson(devopsServiceDTO.getPorts(), new TypeToken<ArrayList<PortMapVO>>() {
        }.getType());
        boolean isUpdate = false;
        if (devopsServiceReqVO.getAppServiceId() != null && devopsServiceDTO.getAppServiceId() != null && devopsServiceReqVO.getAppInstance() != null) {
            isUpdate = !devopsServiceReqVO.getAppInstance().stream()
                    .sorted().collect(Collectors.toList())
                    .equals(oldDevopsServiceAppInstanceDTOS.stream()
                            .map(DevopsServiceAppInstanceDTO::getCode).sorted()
                            .collect(Collectors.toList()));
        }
        if ((devopsServiceReqVO.getAppServiceId() == null && devopsServiceDTO.getAppServiceId() != null) || (devopsServiceReqVO.getAppServiceId() != null && devopsServiceDTO.getAppServiceId() == null)) {
            isUpdate = true;
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
        if (!isUpdate && oldPort.stream().sorted().collect(Collectors.toList())
                .equals(devopsServiceReqVO.getPorts().stream().sorted().collect(Collectors.toList()))
                && !isUpdateExternalIp(devopsServiceReqVO, devopsServiceDTO)) {
            return null;
        }


        //初始化DevopsService对象
        return initDevopsService(devopsServiceDTO, devopsServiceReqVO, devopsServiceAppInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);
    }


    /**
     * 获取k8s service的yaml格式
     */
    private V1Service initV1Service(DevopsServiceReqVO devopsServiceReqVO, Map<String, String> annotations) {
        V1Service service = new V1Service();
        service.setKind(SERVICE);
        service.setApiVersion("v1");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsServiceReqVO.getName());
        metadata.setAnnotations(annotations);
        Map<String, String> label = new HashMap<>();
        label.put(SERVICE_LABLE, SERVICE_LABLE_VALUE);
        metadata.setLabels(label);
        service.setMetadata(metadata);

        V1ServiceSpec spec = new V1ServiceSpec();
        spec.setType(devopsServiceReqVO.getType() == null ? "ClusterIP" : devopsServiceReqVO.getType());
        spec.setSelector(devopsServiceReqVO.getLabel());
        final Integer[] serialNumber = {0};
        List<V1ServicePort> ports = devopsServiceReqVO.getPorts().stream()
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

        if (!StringUtils.isEmpty(devopsServiceReqVO.getExternalIp())) {
            List<String> externalIps = new ArrayList<>(
                    Arrays.asList(devopsServiceReqVO.getExternalIp().split(",")));
            spec.setExternalIPs(externalIps);
        }

        spec.setPorts(ports);
        spec.setSessionAffinity("None");
        service.setSpec(spec);

        return service;
    }

    private V1Endpoints initV1EndPoints(DevopsServiceReqVO devopsServiceReqVO) {
        V1Endpoints v1Endpoints = new V1Endpoints();
        v1Endpoints.setApiVersion("v1");
        v1Endpoints.setKind(ENDPOINTS);
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(devopsServiceReqVO.getName());
        v1Endpoints.setMetadata(v1ObjectMeta);
        List<V1EndpointSubset> v1EndpointSubsets = new ArrayList<>();
        V1EndpointSubset v1EndpointSubset = new V1EndpointSubset();
        devopsServiceReqVO.getEndPoints().forEach((key, value) -> {
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
    private Boolean isUpdateExternalIp(DevopsServiceReqVO devopsServiceReqVO, DevopsServiceDTO devopsServiceDTO) {
        return !((StringUtils.isEmpty(devopsServiceReqVO.getExternalIp())
                && StringUtils.isEmpty(devopsServiceDTO.getExternalIp()))
                || (!StringUtils.isEmpty(devopsServiceReqVO.getExternalIp())
                && !StringUtils.isEmpty(devopsServiceDTO.getExternalIp())
                && devopsServiceReqVO.getExternalIp().equals(devopsServiceDTO.getExternalIp())));
    }


    private void operateEnvGitLabFile(V1Service service, V1Endpoints v1Endpoints, Boolean isCreate,
                                      DevopsServiceDTO devopsServiceDTO,
                                      List<DevopsServiceAppInstanceDTO> devopsServiceAppInstanceDTOS,
                                      List<String> beforeDevopsServiceAppInstanceDTOS,
                                      DevopsEnvCommandDTO devopsEnvCommandDTO,
                                      UserAttrDTO userAttrDTO) {

        DevopsEnvironmentDTO devopsEnvironmentDTO =
                devopsEnvironmentService.baseQueryById(devopsServiceDTO.getEnvId());

        //操作网络数据库操作
        if (isCreate) {
            Long serviceId = baseCreate(devopsServiceDTO).getId();
            devopsEnvCommandDTO.setObjectId(serviceId);
            devopsServiceDTO.setId(serviceId);
            devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsServiceDTO);
            if (beforeDevopsServiceAppInstanceDTOS != null) {
                beforeDevopsServiceAppInstanceDTOS.forEach(instanCode ->
                        devopsServiceInstanceService.baseDeleteByOptions(serviceId, instanCode)
                );
            }
            devopsServiceAppInstanceDTOS.forEach(devopsServiceAppInstanceDTO -> {
                devopsServiceAppInstanceDTO.setServiceId(serviceId);
                devopsServiceInstanceService.baseCreate(devopsServiceAppInstanceDTO);
            });
        } else {
            devopsEnvCommandDTO.setObjectId(devopsServiceDTO.getId());
            devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsServiceDTO);
            Long serviceId = devopsServiceDTO.getId();
            if (beforeDevopsServiceAppInstanceDTOS != null) {
                beforeDevopsServiceAppInstanceDTOS.forEach(instanceCode ->
                        devopsServiceInstanceService.baseDeleteByOptions(serviceId, instanceCode)
                );
            }
            devopsServiceAppInstanceDTOS.forEach(devopsServiceAppInstanceDTO -> {
                devopsServiceAppInstanceDTO.setServiceId(serviceId);
                devopsServiceInstanceService.baseCreate(devopsServiceAppInstanceDTO);
            });
        }

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());

        //处理文件
        ResourceConvertToYamlHandler<V1Service> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(service);
        resourceConvertToYamlHandler.operationEnvGitlabFile(SERVICE_RREFIX + devopsServiceDTO.getName(), TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), isCreate ? CREATE : UPDATE,
                userAttrDTO.getGitlabUserId(), devopsServiceDTO.getId(), SERVICE, v1Endpoints, false, devopsServiceDTO.getEnvId(), path);

        ServiceSagaPayLoad serviceSagaPayLoad = new ServiceSagaPayLoad(devopsEnvironmentDTO.getProjectId(),userAttrDTO.getGitlabUserId());
        serviceSagaPayLoad.setDevopsServiceDTO(devopsServiceDTO);
        serviceSagaPayLoad.setV1Service(service);
        serviceSagaPayLoad.setCreated(serviceSagaPayLoad.getCreated());
        serviceSagaPayLoad.setV1Endpoints(v1Endpoints);
        serviceSagaPayLoad.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_SERVICE),
                builder -> builder
                        .withPayloadAndSerialize(serviceSagaPayLoad)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));
    }

    public void createServiceBySaga(ServiceSagaPayLoad serviceSagaPayLoad) {
        try {
            //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String filePath = clusterConnectionHandler.handDevopsEnvGitRepository(serviceSagaPayLoad.getProjectId(), serviceSagaPayLoad.getDevopsEnvironmentDTO().getCode(), serviceSagaPayLoad.getDevopsEnvironmentDTO().getEnvIdRsa());

            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<V1Service> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(serviceSagaPayLoad.getV1Service());

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    SERVICE_RREFIX + serviceSagaPayLoad.getDevopsServiceDTO().getName(),
                    serviceSagaPayLoad.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    serviceSagaPayLoad.getCreated() ? CREATE : UPDATE,
                    serviceSagaPayLoad.getGitlabUserId(),
                    serviceSagaPayLoad.getDevopsServiceDTO().getId(), SERVICE, serviceSagaPayLoad.getV1Endpoints(), false, serviceSagaPayLoad.getDevopsEnvironmentDTO().getId(), filePath);
        } catch (Exception e) {
            //有异常更新实例以及command的状态
            DevopsServiceDTO devopsServiceDTO = baseQuery(serviceSagaPayLoad.getDevopsServiceDTO().getId());
            devopsServiceDTO.setStatus(CommandStatus.FAILED.getStatus());
            baseUpdate(devopsServiceDTO);
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsServiceDTO.getCommandId());
            devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
            devopsEnvCommandDTO.setError("create or update gitOps file failed!");
            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
        }
    }


    private void initDevopsServicePorts(DevopsServiceReqVO devopsServiceReqVO) {
        final Integer[] serialNumber = {0};
        devopsServiceReqVO.setPorts(devopsServiceReqVO.getPorts().stream()
                .map(t -> {
                    PortMapVO portMapVO = new PortMapVO();
                    portMapVO.setNodePort(t.getNodePort());
                    portMapVO.setPort(t.getPort());
                    portMapVO.setTargetPort(t.getTargetPort());
                    portMapVO.setName(t.getName() == null ? "http" + ++serialNumber[0] : t.getName());
                    portMapVO.setProtocol(t.getProtocol() == null ? "TCP" : t.getProtocol());
                    return portMapVO;
                })
                .collect(Collectors.toList()));
    }


    private DevopsEnvCommandDTO initDevopsEnvCommandDTO(String type) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        if (type.equals(CREATE)) {
            devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType());
        } else if (type.equals(UPDATE)) {
            devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        } else {
            devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
        }
        devopsEnvCommandDTO.setObject(ObjectType.SERVICE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }

}
