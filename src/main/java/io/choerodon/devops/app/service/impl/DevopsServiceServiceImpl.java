package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.JSON;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper;
import io.choerodon.devops.infra.mapper.DevopsServiceMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.ResourceCreatorInfoUtil;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by Zenger on 2018/4/13.
 */
@Service
public class DevopsServiceServiceImpl implements DevopsServiceService {

    private static final Logger logger = LoggerFactory.getLogger(DevopsServiceServiceImpl.class);

    public static final String ENDPOINTS = "Endpoints";
    public static final String LOADBALANCER = "LoadBalancer";
    public static final String SERVICE = "Service";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String SERVICE_RREFIX = "svc-";
    private static final String SERVICE_LABLE = "choerodon.io/network";
    private static final String SERVICE_LABLE_VALUE = "service";
    private static final String MASTER = "master";
    private Gson gson = new Gson();
    private JSON json = new JSON();
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private AppServiceService applicationService;
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
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper;
    @Autowired
    AgentPodInfoServiceImpl agentPodInfoService;


    @Override
    public Boolean checkName(Long envId, String name) {
        return baseCheckName(envId, name);
    }


    @Override
    public PageInfo<DevopsServiceVO> pageByEnv(Long projectId, Long envId, PageRequest pageRequest, String searchParam, Long appServiceId) {
        return ConvertUtils.convertPage(basePageByOptions(
                projectId, envId, null, pageRequest, searchParam, appServiceId), this::queryDtoToVo);
    }

    @Override
    public PageInfo<DevopsServiceVO> pageByInstance(Long projectId, Long envId, Long instanceId, PageRequest pageRequest, Long appServiceId, String searchParam) {
        PageInfo<DevopsServiceVO> devopsServiceByPage = ConvertUtils.convertPage(basePageByOptions(
                projectId, envId, instanceId, pageRequest, null, appServiceId), this::queryDtoToVo);
        if (!devopsServiceByPage.getList().isEmpty()) {
            devopsServiceByPage.getList().forEach(devopsServiceVO -> {
                PageInfo<DevopsIngressVO> devopsIngressVOPageInfo = devopsIngressService
                        .basePageByOptions(projectId, null, devopsServiceVO.getId(), new PageRequest(0, 100), null);
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

        DevopsServiceQueryDTO devopsServiceQueryDTO = baseQueryById(id);
        if (devopsServiceQueryDTO == null) {
            return null;
        } else {
            List<DevopsServiceInstanceDTO> devopsServiceAppInstanceDTOS = devopsServiceInstanceService.baseListByServiceId(id);
            //网络多实例中存在删除实例时，给应用信息赋值
            if (!devopsServiceAppInstanceDTOS.isEmpty()) {
                for (DevopsServiceInstanceDTO devopsServiceAppInstanceDTO : devopsServiceAppInstanceDTOS) {
                    AppServiceInstanceDTO applicationInstanceDTO = appServiceInstanceService.baseQuery(devopsServiceAppInstanceDTO.getInstanceId());
                    if (applicationInstanceDTO != null) {
                        AppServiceDTO appServiceDTO = applicationService.baseQuery(applicationInstanceDTO.getAppServiceId());
                        devopsServiceQueryDTO.setAppServiceId(appServiceDTO.getId());
                        devopsServiceQueryDTO.setAppServiceName(appServiceDTO.getName());
                        devopsServiceQueryDTO.setAppServiceProjectId(devopsProjectService.queryProjectIdByAppId(appServiceDTO.getAppId()));
                    }
                }
            }
        }
        return querySingleServiceDtoToVo(devopsServiceQueryDTO);
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

        List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceDTOS = new ArrayList<>();


        //处理创建service对象数据
        DevopsServiceDTO devopsServiceDTO = handlerCreateService(devopsServiceReqVO, devopsServiceInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CREATE);

        //初始化V1Service对象
        V1Service v1Service = initV1Service(
                devopsServiceReqVO,
                gson.fromJson(devopsServiceDTO.getAnnotations(), Map.class));
        V1Endpoints v1Endpoints = null;
        if (devopsServiceReqVO.getEndPoints() != null) {
            v1Endpoints = initV1EndPoints(devopsServiceReqVO);
        }


        // 先创建网络纪录
        baseCreate(devopsServiceDTO);

        if (devopsServiceReqVO.getAppServiceId() != null) {
            // 应用下不能创建endpoints类型网络
            if (devopsServiceReqVO.getEndPoints() != null) {
                throw new CommonException("error.app.create.endpoints.service");
            }
        }

        devopsServiceDTO.setAppServiceId(devopsServiceReqVO.getAppServiceId());
        //在gitops库处理service文件
        operateEnvGitLabFile(v1Service, v1Endpoints, true, devopsServiceDTO, devopsServiceInstanceDTOS, beforeDevopsServiceAppInstanceDTOS, devopsEnvCommandDTO, userAttrDTO);
        return true;
    }


    @Override
    public Boolean insertDevopsServiceByGitOps(Long projectId, DevopsServiceReqVO devopsServiceReqVO, Long userId) {
        //校验环境是否链接
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceReqVO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());
        List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceDTOS = new ArrayList<>();

        //处理创建service对象数据
        DevopsServiceDTO devopsServiceDTO = handlerCreateService(devopsServiceReqVO, devopsServiceInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CREATE);

        //存储service对象到数据库
        devopsServiceDTO = baseCreate(devopsServiceDTO);

        //存储service和instance对象关系到数据库
        Long serviceId = devopsServiceDTO.getId();
        devopsEnvCommandDTO.setObjectId(serviceId);
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsServiceDTO);

        devopsServiceInstanceDTOS.forEach(devopsServiceAppInstanceDTO -> {
            devopsServiceAppInstanceDTO.setServiceId(serviceId);
            devopsServiceInstanceService.baseCreate(devopsServiceAppInstanceDTO);
        });

        //处理应用服务关联网络信息
        if (devopsServiceDTO.getAppServiceId() != null) {
            devopsApplicationResourceService.handleAppServiceResource(Arrays.asList(devopsServiceDTO.getAppServiceId()), devopsServiceDTO.getId(), ObjectType.SERVICE.getType());
        }
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
        List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceDTOS = devopsServiceInstanceService
                .baseListByServiceId(id).stream().map(DevopsServiceInstanceDTO::getCode).collect(Collectors.toList());
        DevopsServiceDTO devopsServiceDTO = baseQuery(id);
        devopsServiceDTO = handlerUpdateService(devopsServiceReqVO, devopsServiceDTO, devopsServiceInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);
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
            operateEnvGitLabFile(v1Service, v1Endpoints, false, devopsServiceDTO, devopsServiceInstanceDTOS, beforeDevopsServiceAppInstanceDTOS, devopsEnvCommandDTO, userAttrDTO);
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
        List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceDTOS = devopsServiceInstanceService
                .baseListByServiceId(id).stream().map(DevopsServiceInstanceDTO::getCode).collect(Collectors.toList());
        DevopsServiceDTO devopsServiceDTO = baseQuery(id);
        devopsServiceDTO = handlerUpdateService(devopsServiceReqVO, devopsServiceDTO, devopsServiceInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);
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
        devopsServiceInstanceDTOS.forEach(devopsServiceAppInstanceDTO -> {
            devopsServiceAppInstanceDTO.setServiceId(id);
            devopsServiceInstanceService.baseCreate(devopsServiceAppInstanceDTO);
        });

        //处理应用服务关联网络信息
        if (devopsServiceDTO.getAppServiceId() != null) {
            devopsApplicationResourceService.handleAppServiceResource(Arrays.asList(devopsServiceDTO.getAppServiceId()), devopsServiceDTO.getId(), ObjectType.SERVICE.getType());
        }
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
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
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
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
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
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
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

        //删除应用服务关联网络信息
        devopsApplicationResourceService.baseDeleteByResourceIdAndType(id, ObjectType.SERVICE.getType());


    }


    @Override
    public Boolean baseCheckName(Long envId, String name) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setEnvId(envId);
        devopsServiceDTO.setName(name);
        if (devopsServiceMapper.selectOne(devopsServiceDTO) != null) {
            return false;
        }
        return true;
    }

    @Override
    public PageInfo<DevopsServiceQueryDTO> basePageByOptions(Long projectId, Long envId, Long instanceId, PageRequest pageRequest,
                                                             String searchParam, Long appServiceId) {

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
                        } else if (property.equals("appServiceName")) {
                            property = "app_service_name";
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
        Map<String, Object> searchParamMap = TypeUtil.castMapParams(searchParam);
        count = devopsServiceMapper.selectCountByName(
                projectId, envId, instanceId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS)), appServiceId);

        result.setTotal(count);
        List<String> paramList = TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS));
        devopsServiceQueryDTOS = devopsServiceMapper.listDevopsServiceByPage(
                projectId, envId, instanceId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                paramList, sortResult, appServiceId);
        result.setList(devopsServiceQueryDTOS.subList(start, stop > devopsServiceQueryDTOS.size() ? devopsServiceQueryDTOS.size() : stop));
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

    @Override
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


    @Override
    public DevopsServiceDTO baseCreate(DevopsServiceDTO devopsServiceDTO) {
        if (devopsServiceMapper.insert(devopsServiceDTO) != 1) {
            throw new CommonException("error.k8s.service.create");
        }
        return devopsServiceDTO;
    }

    @Override
    public DevopsServiceDTO baseQuery(Long id) {
        return devopsServiceMapper.selectByPrimaryKey(id);
    }

    @Override
    public void baseDelete(Long id) {
        devopsServiceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void baseUpdate(DevopsServiceDTO devopsServiceDTO) {
        DevopsServiceDTO oldDevopsServiceDTO = devopsServiceMapper.selectByPrimaryKey(devopsServiceDTO.getId());
        if (devopsServiceDTO.getLabels() == null) {
            devopsServiceMapper.updateLabelsToNull(devopsServiceDTO.getId());
        }
        if (devopsServiceDTO.getExternalIp() == null) {
            devopsServiceMapper.setExternalIpNull(devopsServiceDTO.getId());
        }
        devopsServiceDTO.setObjectVersionNumber(oldDevopsServiceDTO.getObjectVersionNumber());
        if (devopsServiceMapper.updateByPrimaryKeySelective(devopsServiceDTO) != 1) {
            throw new CommonException("error.k8s.service.update");
        }
    }

    @Override
    public void baseUpdateLabels(Long id) {
        devopsServiceMapper.updateLabelsToNull(id);
    }


    @Override
    public void baseUpdateEndPoint(Long id) {
        devopsServiceMapper.updateEndPointToNull(id);
    }

    @Override
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
        return devopsServiceMapper.checkEnvContainingService(envId);
    }

    @Override
    public List<DevopsServiceDTO> baseList() {
        return devopsServiceMapper.selectAll();
    }

    @Override
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
                                                   List<DevopsServiceInstanceDTO> addDevopsServiceInstanceDTOS,
                                                   List<String> beforedevopsServiceAppInstanceDTOS) {
        StringBuilder stringBuffer = new StringBuilder();
        List<String> appServiceInstances = devopsServiceReqVO.getInstances();
        if (appServiceInstances != null) {
            appServiceInstances.forEach(appServiceInstance -> {
                AppServiceInstanceDTO appServiceInstanceDTO =
                        appServiceInstanceService.baseQueryByCodeAndEnv(appServiceInstance, devopsServiceReqVO.getEnvId());
                //资源视图创建网络类型为选择实例时，需要将网络和实例对应的应用服务相关联
                if (devopsServiceReqVO.getAppServiceId() == null) {
                    devopsServiceReqVO.setAppServiceId(appServiceInstanceDTO.getAppServiceId());
                }
                stringBuffer.append(appServiceInstance).append("+");
                if (beforedevopsServiceAppInstanceDTOS.contains(appServiceInstance)) {
                    beforedevopsServiceAppInstanceDTOS.remove(appServiceInstance);
                    return;
                }
                DevopsServiceInstanceDTO devopsServiceInstanceDTO = new DevopsServiceInstanceDTO();
                if (appServiceInstanceDTO != null) {
                    devopsServiceInstanceDTO.setInstanceId(appServiceInstanceDTO.getId());
                }
                devopsServiceInstanceDTO.setCode(appServiceInstance);
                addDevopsServiceInstanceDTOS.add(devopsServiceInstanceDTO);
            });
        }
        String instancesCode = stringBuffer.toString();
        if (instancesCode.endsWith("+")) {
            return instancesCode.substring(0, stringBuffer.toString().lastIndexOf('+'));
        }
        return instancesCode;
    }


    private DevopsServiceDTO handlerCreateService(DevopsServiceReqVO devopsServiceReqVO, List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS, List<String> beforeDevopsServiceAppInstanceDTOS) {

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
        return initDevopsService(devopsServiceDTO, devopsServiceReqVO, devopsServiceInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);

    }


    private DevopsServiceDTO voToDto(DevopsServiceReqVO devopsServiceReqVO) {
        if (devopsServiceReqVO == null) {
            return null;
        }
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        BeanUtils.copyProperties(devopsServiceReqVO, devopsServiceDTO);
        devopsServiceDTO.setPorts(gson.toJson(devopsServiceReqVO.getPorts()));
        return devopsServiceDTO;
    }


    private DevopsServiceVO queryDtoToVo(DevopsServiceQueryDTO devopsServiceQueryDTO) {
        if (devopsServiceQueryDTO == null) {
            return null;
        }
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

        if (devopsServiceQueryDTO.getInstances() != null) {
            devopsServiceQueryDTO.getInstances().forEach(i -> {
                if (i.getStatus() == null) {
                    i.setStatus(InstanceStatus.DELETED.getStatus());
                }
            });
        }

        devopsServiceTargetVO.setInstances(ConvertUtils.convertList(devopsServiceQueryDTO.getInstances(), AppServiceInstanceInfoVO.class));
        if (!StringUtils.isEmpty(devopsServiceQueryDTO.getMessage())) {
            V1Service v1Service = json.deserialize(devopsServiceQueryDTO.getMessage(), V1Service.class);
            devopsServiceTargetVO.setLabels(v1Service.getSpec().getSelector());
            devopsServiceVO.setLabels(v1Service.getMetadata().getLabels());
        }
        devopsServiceTargetVO.setLabels(gson.fromJson(devopsServiceQueryDTO.getLabels(), new TypeToken<Map<String, String>>() {
        }.getType()));
        devopsServiceTargetVO.setEndPoints(gson.fromJson(devopsServiceQueryDTO.getEndPoints(), new TypeToken<Map<String, List<EndPointPortVO>>>() {
        }.getType()));
        devopsServiceVO.setTarget(devopsServiceTargetVO);

        // service的dnsName为${serviceName.namespace}
        devopsServiceVO.setDns(devopsServiceVO.getName() + "." + devopsServiceQueryDTO.getEnvCode());

        if (devopsServiceQueryDTO.getCreatedBy() != null && devopsServiceQueryDTO.getCreatedBy() != 0) {
            devopsServiceVO.setCreatorName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsServiceQueryDTO.getCreatedBy()));
        }
        if (devopsServiceQueryDTO.getLastUpdatedBy() != null && devopsServiceQueryDTO.getLastUpdatedBy() != 0) {
            devopsServiceVO.setLastUpdaterName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsServiceQueryDTO.getLastUpdatedBy()));
        }
        return devopsServiceVO;
    }

    @Override
    public DevopsServiceVO querySingleServiceDtoToVo(DevopsServiceQueryDTO devopsServiceQueryDTO) {
        DevopsServiceVO devopsServiceVO = queryDtoToVo(devopsServiceQueryDTO);
        Long envId = devopsServiceQueryDTO.getEnvId();
        //获得pod实时信息
        if (devopsServiceQueryDTO.getInstances() != null) {
            List<Map<Long, List<PodLiveInfoVO>>> instancePodLiveInfoVOs = devopsServiceQueryDTO.getInstances()
                    .stream()
                    .map(instanceInfoVO -> getInstancePodLiveInfoVOs(instanceInfoVO.getId(), envId))
                    .collect(Collectors.toList());

            devopsServiceVO.setPodLiveInfos(instancePodLiveInfoVOs);
        }
        return devopsServiceVO;
    }

    private Map<Long, List<PodLiveInfoVO>> getInstancePodLiveInfoVOs(Long instanceId, Long envId) {
        PodLiveInfoVO podLiveInfoVO = new PodLiveInfoVO();

        //从数据库中获得pod已经存在的信息
        List<DevopsEnvPodDTO> devopsEnvPodDTOList = devopsEnvPodMapper.queryPodByEnvIdAndInstanceId(instanceId, envId);
        if (devopsEnvPodDTOList == null || devopsEnvPodDTOList.size() == 0) {
            return null;
        }
        List<PodLiveInfoVO> podLiveInfoVOList = devopsEnvPodDTOList.stream().map(devopsEnvPodDTO -> {
            BeanUtils.copyProperties(devopsEnvPodDTO, podLiveInfoVO);

            //反序列化json
            V1Pod v1Pod = json.deserialize(devopsEnvPodDTO.getMessage(), V1Pod.class);

            List<ContainerVO> containerVOS = v1Pod.getSpec().getContainers().stream().map(v1Container -> {
                ContainerVO containerVO = new ContainerVO();
                containerVO.setName(v1Container.getName());
                containerVO.setRegistry(v1Container.getImage());
                return containerVO;
            }).collect(Collectors.toList());

            //设置podName,containers
            podLiveInfoVO.setPodName(v1Pod.getMetadata().getName());
            podLiveInfoVO.setContainers(containerVOS);

            //设置实时CPU、内存信息
            List<AgentPodInfoVO> agentPodInfoVOS = agentPodInfoService.queryAllPodSnapshots(devopsEnvPodDTO.getName(), devopsEnvPodDTO.getNamespace());

            if (!agentPodInfoVOS.isEmpty()) {
                List<String> cpuUsedList = agentPodInfoVOS.stream().map(AgentPodInfoVO::getCpuUsed).collect(Collectors.toList());
                List<String> memoryUsedList = agentPodInfoVOS.stream().map(AgentPodInfoVO::getMemoryUsed).collect(Collectors.toList());
                List<Date> timeList = agentPodInfoVOS.stream().map(AgentPodInfoVO::getSnapshotTime).collect(Collectors.toList());

                podLiveInfoVO.setCpuUsedList(cpuUsedList);
                podLiveInfoVO.setMemoryUsedList(memoryUsedList);
                podLiveInfoVO.setTimeList(timeList);
                podLiveInfoVO.setNodeIp(agentPodInfoVOS.get(0).getNodeIp());
                podLiveInfoVO.setNodeName(agentPodInfoVOS.get(0).getNodeName());
                podLiveInfoVO.setPodIp(agentPodInfoVOS.get(0).getPodIp());
            }
            return podLiveInfoVO;
        }).collect(Collectors.toList());
        Map<Long, List<PodLiveInfoVO>> instancePodLiveInfoVOs = new HashMap<>();
        instancePodLiveInfoVOs.put(instanceId, podLiveInfoVOList);
        return instancePodLiveInfoVOs;
    }

    private DevopsServiceDTO initDevopsService(DevopsServiceDTO devopsServiceDTO, DevopsServiceReqVO devopsServiceReqVO, List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS, List<String> beforeDevopsServiceAppInstanceDTOS) {
        devopsServiceDTO.setAppServiceId(devopsServiceReqVO.getAppServiceId());
        AppServiceDTO applicationDTO = applicationService.baseQuery(devopsServiceReqVO.getAppServiceId());
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

        String serviceInstances = updateServiceInstanceAndGetCode(devopsServiceReqVO, devopsServiceInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);
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

    private DevopsServiceDTO handlerUpdateService(DevopsServiceReqVO devopsServiceReqVO, DevopsServiceDTO devopsServiceDTO, List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS, List<String> beforeDevopsServiceAppInstanceDTOS) {
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
        List<DevopsServiceInstanceDTO> oldDevopsServiceInstanceDTOS =
                devopsServiceInstanceService.baseListByServiceId(devopsServiceDTO.getId());
        //验证网络是否需要更新
        List<PortMapVO> oldPort = gson.fromJson(devopsServiceDTO.getPorts(), new TypeToken<ArrayList<PortMapVO>>() {
        }.getType());
        boolean isUpdate = false;

        //资源视图更新网络类型为选择实例时，需要将网络和实例对应的应用服务相关联
        if (devopsServiceReqVO.getInstances() != null && !devopsServiceReqVO.getInstances().isEmpty()) {
            AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(devopsServiceReqVO.getInstances().get(0), devopsServiceReqVO.getEnvId());
            if (devopsServiceReqVO.getAppServiceId() == null && appServiceInstanceDTO != null) {
                devopsServiceReqVO.setAppServiceId(appServiceInstanceDTO.getAppServiceId());
            }
        }

        if (devopsServiceReqVO.getAppServiceId() != null && devopsServiceDTO.getAppServiceId() != null && devopsServiceReqVO.getInstances() != null) {
            isUpdate = !devopsServiceReqVO.getInstances().stream()
                    .sorted().collect(Collectors.toList())
                    .equals(oldDevopsServiceInstanceDTOS.stream()
                            .map(DevopsServiceInstanceDTO::getCode).sorted()
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
        return initDevopsService(devopsServiceDTO, devopsServiceReqVO, devopsServiceInstanceDTOS, beforeDevopsServiceAppInstanceDTOS);
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
                                      List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS,
                                      List<String> beforeDevopsServiceAppInstanceDTOS,
                                      DevopsEnvCommandDTO devopsEnvCommandDTO,
                                      UserAttrDTO userAttrDTO) {

        DevopsEnvironmentDTO devopsEnvironmentDTO =
                devopsEnvironmentService.baseQueryById(devopsServiceDTO.getEnvId());

        //操作网络数据库操作
        Long serviceId;
        if (isCreate) {
            serviceId = devopsServiceDTO.getId();
            devopsEnvCommandDTO.setObjectId(serviceId);
            devopsServiceDTO.setId(serviceId);
            devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsServiceDTO);
            if (beforeDevopsServiceAppInstanceDTOS != null) {
                beforeDevopsServiceAppInstanceDTOS.forEach(instanCode ->
                        devopsServiceInstanceService.baseDeleteByOptions(serviceId, instanCode)
                );
            }
            devopsServiceInstanceDTOS.forEach(devopsServiceAppInstanceDTO -> {
                devopsServiceAppInstanceDTO.setServiceId(serviceId);
                devopsServiceInstanceService.baseCreate(devopsServiceAppInstanceDTO);
            });
        } else {
            devopsEnvCommandDTO.setObjectId(devopsServiceDTO.getId());
            devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsServiceDTO);
            serviceId = devopsServiceDTO.getId();
            if (beforeDevopsServiceAppInstanceDTOS != null) {
                beforeDevopsServiceAppInstanceDTOS.forEach(instanceCode ->
                        devopsServiceInstanceService.baseDeleteByOptions(serviceId, instanceCode)
                );
            }
            devopsServiceInstanceDTOS.forEach(devopsServiceAppInstanceDTO -> {
                devopsServiceAppInstanceDTO.setServiceId(serviceId);
                devopsServiceInstanceService.baseCreate(devopsServiceAppInstanceDTO);
            });
        }


        //处理应用服务关联网络信息
        if (devopsServiceDTO.getAppServiceId() != null) {
            devopsApplicationResourceService.handleAppServiceResource(Arrays.asList(devopsServiceDTO.getAppServiceId()), devopsServiceDTO.getId(), ObjectType.SERVICE.getType());
        }

        ServiceSagaPayLoad serviceSagaPayLoad = new ServiceSagaPayLoad(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId());
        serviceSagaPayLoad.setDevopsServiceDTO(devopsServiceDTO);
        serviceSagaPayLoad.setV1Service(service);
        serviceSagaPayLoad.setCreated(isCreate);
        serviceSagaPayLoad.setV1Endpoints(v1Endpoints);
        serviceSagaPayLoad.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_SERVICE),
                builder -> builder
                        .withJson(gson.toJson(serviceSagaPayLoad))
                        .withRefId(devopsEnvironmentDTO.getId().toString()));
    }


    @Override
    public void updateStatus(DevopsServiceDTO devopsServiceDTO) {
        devopsServiceMapper.updateStatus(devopsServiceDTO.getId(), devopsServiceDTO.getStatus());
    }

    @Override
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
            logger.info("create or update service failed", e);
            //有异常更新网络以及command的状态
            DevopsServiceDTO devopsServiceDTO = baseQuery(serviceSagaPayLoad.getDevopsServiceDTO().getId());
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndResourceId(serviceSagaPayLoad.getDevopsEnvironmentDTO().getId(), devopsServiceDTO.getId(), SERVICE);
            String filePath = devopsEnvFileResourceDTO == null ? "svc-" + devopsServiceDTO.getName() + ".yaml" : devopsEnvFileResourceDTO.getFilePath();
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(serviceSagaPayLoad.getDevopsEnvironmentDTO().getGitlabEnvProjectId()), MASTER,
                    filePath)) {
                devopsServiceDTO.setStatus(CommandStatus.FAILED.getStatus());
                baseUpdate(devopsServiceDTO);
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsServiceDTO.getCommandId());
                devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
                devopsEnvCommandDTO.setError("create or update service failed");
                devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
            }
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
