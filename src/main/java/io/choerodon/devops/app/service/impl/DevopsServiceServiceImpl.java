package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.service.AppServiceInstanceService.PARENT_WORK_LOAD_LABEL;
import static io.choerodon.devops.app.service.AppServiceInstanceService.PARENT_WORK_LOAD_NAME_LABEL;
import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_FIELD_NOT_SUPPORTED_FOR_SORT;
import static io.choerodon.devops.infra.enums.ResourceType.DEPLOYMENT;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netflix.servo.util.Strings;
import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.DevopsServiceValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.ServiceSagaPayLoad;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.k8s.ServiceTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper;
import io.choerodon.devops.infra.mapper.DevopsServiceMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;


/**
 * Created by Zenger on 2018/4/13.
 */
@Service
public class DevopsServiceServiceImpl implements DevopsServiceService, ChartResourceOperatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsServiceServiceImpl.class);

    public static final String ENDPOINTS = "Endpoints";
    public static final String LOADBALANCER = "LoadBalancer";
    public static final String SERVICE = "Service";
    private static final String SERVICE_LABLE = "choerodon.io/network";
    private static final String SERVICE_LABLE_VALUE = "service";


    private Gson gson = new Gson();
    private JSON json = new JSON();
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    @Lazy
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    @Lazy
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
    private DevopsServiceMapper devopsServiceMapper;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper;
    @Autowired
    private AgentPodInfoServiceImpl agentPodInfoService;
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    @Lazy
    private SendNotificationService sendNotificationService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private DevopsDeploymentService devopsDeploymentService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;

    @Override
    public Boolean checkName(Long envId, String name) {
        return baseCheckName(envId, name);
    }


    @Override
    public Page<DevopsServiceVO> pageByEnv(Long projectId, Long envId, PageRequest pageable, String searchParam, Long appServiceId) {
        return ConvertUtils.convertPage(basePageByOptions(
                projectId, envId, null, pageable, searchParam, appServiceId), this::queryDtoToVo);
    }

    @Override
    public Page<DevopsServiceVO> pageByInstance(Long projectId, Long envId, Long instanceId, PageRequest pageable, Long appServiceId, String searchParam) {
        Page<DevopsServiceVO> devopsServiceByPage = ConvertUtils.convertPage(basePageByOptions(
                projectId, envId, instanceId, pageable, null, appServiceId), this::queryDtoToVo);
        if (!devopsServiceByPage.getContent().isEmpty()) {
            Set<Long> appServiceIds = devopsServiceByPage.stream().map(DevopsServiceVO::getAppServiceId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, List<DevopsDeployAppCenterVO>> appServiceDeployAppCenterVOMap = devopsDeployAppCenterService.listByAppServiceIds(envId, appServiceIds).stream().collect(Collectors.groupingBy(DevopsDeployAppCenterVO::getAppServiceId));

            devopsServiceByPage.getContent().forEach(devopsServiceVO -> {
                // 如果网络是关联指定的应用，查出该应用生成的所有容器应用
                if (devopsServiceVO.getAppServiceId() != null) {
                    List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOS = appServiceDeployAppCenterVOMap.get(devopsServiceVO.getAppServiceId());
                    if (!CollectionUtils.isEmpty(devopsDeployAppCenterVOS)) {
                        List<String> names = devopsDeployAppCenterVOS.stream().filter(a -> !a.getObjectId().equals(instanceId)).map(DevopsDeployAppCenterVO::getName).collect(Collectors.toList());
                        devopsServiceVO.setRelatedApplicationName(names);
                    }
                }
            });
        }
        return devopsServiceByPage;
    }


    @Override
    public DevopsServiceVO queryByName(Long envId, String serviceName) {
        return ConvertUtils.convertObject(baseQueryByNameAndEnvId(serviceName, envId), DevopsServiceVO.class);
    }

    @Override
    public List<DevopsServiceVO> listByEnvIdAndAppServiceId(Long envId, Long appServiceId) {
        return ConvertUtils.convertList(devopsServiceMapper.listRunningService(envId, appServiceId), this::queryDtoToVo);
    }

    @Override
    public DevopsServiceVO query(Long id) {
        DevopsServiceQueryDTO devopsServiceQueryDTO = baseQueryById(id);

        if (devopsServiceQueryDTO == null) {
            return null;
        }

        fillAppServiceInfo(devopsServiceQueryDTO);

        return queryDtoToVo(devopsServiceQueryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_CREATE_SERVICE,
            description = "Devops创建网络", inputSchema = "{}")
    public Boolean create(Long projectId, DevopsServiceReqVO devopsServiceReqVO) {
        //校验部署方式是否唯一
        permissionHelper.checkDeploymentWay(devopsServiceReqVO);
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, devopsServiceReqVO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        List<String> beforeDevopsServiceAppInstanceDTOS = new ArrayList<>();


        //处理创建service对象数据
        DevopsServiceDTO devopsServiceDTO = handlerCreateService(devopsServiceReqVO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CommandType.CREATE.getType());

        //初始化V1Service对象
        V1Service v1Service = initV1Service(devopsServiceReqVO);
        V1Endpoints v1Endpoints = null;
        if (devopsServiceReqVO.getEndPoints() != null) {
            // 应用服务下不能创建endpoints类型网络
            if (devopsServiceReqVO.getAppServiceId() != null) {
                throw new CommonException("devops.app.create.endpoints.service");
            }
            v1Endpoints = initV1EndPoints(devopsServiceReqVO);
        }

        // 先创建网络纪录
        baseCreate(devopsServiceDTO);

        //在gitops库处理service文件
        operateEnvGitLabFile(v1Service, v1Endpoints, true, devopsServiceDTO, beforeDevopsServiceAppInstanceDTOS, devopsEnvCommandDTO, userAttrDTO, devopsServiceReqVO.getDevopsIngressVO());
        return true;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public ServiceSagaPayLoad createForBatchDeployment(DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO, Long projectId, DevopsServiceReqVO devopsServiceReqVO) {
        //处理创建service对象数据
        DevopsServiceDTO devopsServiceDTO = handlerCreateService(devopsServiceReqVO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CommandType.CREATE.getType());

        //初始化V1Service对象
        V1Service v1Service = initV1Service(devopsServiceReqVO);

        // 先创建网络纪录
        baseCreate(devopsServiceDTO);

        Long serviceId = devopsServiceDTO.getId();
        devopsEnvCommandDTO.setObjectId(serviceId);
        devopsServiceDTO.setId(serviceId);
        devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsServiceDTO);

        ServiceSagaPayLoad serviceSagaPayLoad = new ServiceSagaPayLoad(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId());
        serviceSagaPayLoad.setDevopsServiceDTO(devopsServiceDTO);
        serviceSagaPayLoad.setV1Service(v1Service);
        serviceSagaPayLoad.setCreated(true);
        serviceSagaPayLoad.setDevopsEnvironmentDTO(devopsEnvironmentDTO);
        return serviceSagaPayLoad;
    }


    @Override
    public Boolean insertDevopsServiceByGitOps(Long projectId, DevopsServiceReqVO devopsServiceReqVO, Long userId) {
        //校验环境是否链接
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceReqVO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        //处理创建service对象数据
        DevopsServiceDTO devopsServiceDTO = handlerCreateService(devopsServiceReqVO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CommandType.CREATE.getType());

        //存储service对象到数据库
        devopsServiceDTO = baseCreate(devopsServiceDTO);

        //存储service和instance对象关系到数据库
        Long serviceId = devopsServiceDTO.getId();
        devopsEnvCommandDTO.setObjectId(serviceId);
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsServiceDTO);

        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(Long projectId, Long id,
                          DevopsServiceReqVO devopsServiceReqVO) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, devopsServiceReqVO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);


        //更新网络的时候校验gitops库文件是否存在,处理部署网络时，由于没有创gitops文件导致的部署失败
        resourceFileCheckHandler.check(devopsEnvironmentDTO, id, devopsServiceReqVO.getName(), SERVICE);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CommandType.UPDATE.getType());

        //处理更新service对象数据
        List<String> beforeDevopsServiceAppInstanceDTOS = devopsServiceInstanceService
                .baseListByServiceId(id).stream().map(DevopsServiceInstanceDTO::getCode).collect(Collectors.toList());
        DevopsServiceDTO devopsServiceDTO = baseQuery(id);
        devopsServiceDTO = handlerUpdateService(devopsServiceReqVO, devopsServiceDTO);
        V1Endpoints v1Endpoints = null;
        if (devopsServiceDTO == null) {
            return false;
        } else {
            //初始化V1Service对象
            V1Service v1Service = initV1Service(devopsServiceReqVO);
            if (devopsServiceReqVO.getEndPoints() != null) {
                // 应用服务下的网络更新为EndPoints类型时，应用服务id更新为null
                if (devopsServiceDTO.getTargetAppServiceId() != null) {
                    devopsServiceMapper.updateAppServiceIdToNull(devopsServiceDTO.getId());
                }
                v1Endpoints = initV1EndPoints(devopsServiceReqVO);
            }
            //在gitops库处理service文件
            operateEnvGitLabFile(v1Service, v1Endpoints, false, devopsServiceDTO, beforeDevopsServiceAppInstanceDTOS, devopsEnvCommandDTO, userAttrDTO, devopsServiceReqVO.getDevopsIngressVO());
            devopsServiceInstanceService.deleteByServiceId(devopsServiceDTO.getId());
        }
        return true;
    }


    @Override
    public Boolean updateDevopsServiceByGitOps(Long projectId, Long id,
                                               DevopsServiceReqVO devopsServiceReqVO, Long userId) {
        //校验环境是否链接
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceReqVO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());


        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CommandType.UPDATE.getType());

        //处理更新service对象数据
        List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS = new ArrayList<>();
        List<String> beforeDevopsServiceAppInstanceDTOS = devopsServiceInstanceService
                .baseListByServiceId(id).stream().map(DevopsServiceInstanceDTO::getCode).collect(Collectors.toList());
        DevopsServiceDTO devopsServiceDTO = baseQuery(id);
        devopsServiceDTO = handlerUpdateService(devopsServiceReqVO, devopsServiceDTO);
        if (devopsServiceDTO == null) {
            return false;
        }

        if (!ObjectUtils.isEmpty(devopsServiceDTO.getEndPoints())) {
            devopsServiceMapper.updateAppServiceIdToNull(devopsServiceDTO.getId());
            devopsServiceDTO.setAppServiceId(null);
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
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long projectId, Long id) {
        DevopsServiceDTO devopsServiceDTO = baseQuery(id);

        if (devopsServiceDTO == null) {
            return;
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, devopsServiceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CommandType.DELETE.getType());

        devopsEnvCommandDTO.setObjectId(id);
        devopsServiceDTO.setStatus(ServiceStatus.OPERATIING.getStatus());
        devopsServiceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsServiceDTO);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvironmentDTO.getType(), devopsEnvironmentDTO.getClusterCode());

        //查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), id, SERVICE);
        if (devopsEnvFileResourceDTO == null) {
            baseDelete(id);
            devopsServiceInstanceService.baseDeleteByOptions(id, null);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), GitOpsConstants.MASTER,
                    GitOpsConstants.SERVICE_PREFIX + devopsServiceDTO.getName() + GitOpsConstants.YAML_FILE_SUFFIX)) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        GitOpsConstants.SERVICE_PREFIX + devopsServiceDTO.getName() + GitOpsConstants.YAML_FILE_SUFFIX,
                        String.format("【DELETE】%s", GitOpsConstants.SERVICE_PREFIX + devopsServiceDTO.getName() + GitOpsConstants.YAML_FILE_SUFFIX),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "master");
            }
            return;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), GitOpsConstants.MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {

                baseDelete(id);
                devopsServiceInstanceService.baseDeleteByOptions(id, null);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return;
            }
        }
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService.baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceDTOS.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), GitOpsConstants.MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(),
                        String.format("【DELETE】%s", devopsEnvFileResourceDTO.getFilePath()),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "master");
            }
        } else {
            ResourceConvertToYamlHandler<V1Service> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1Service v1Service = new V1Service();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsServiceDTO.getName());
            v1Service.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1Service);
            Integer gitlabEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    "release-" + devopsServiceDTO.getName(),
                    gitlabEnvProjectId,
                    CommandType.DELETE.getType(),
                    userAttrDTO.getGitlabUserId(),
                    devopsServiceDTO.getId(), SERVICE, null, false, devopsEnvironmentDTO.getId(), path);
        }
        //删除成功后发送webhook json
        sendNotificationService.sendWhenServiceCreationSuccessOrDelete(devopsServiceDTO, devopsEnvironmentDTO, SendSettingEnum.DELETE_RESOURCE.value());
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
        devopsServiceInstanceService.baseDeleteByOptions(id, null);
    }


    @Override
    public Boolean baseCheckName(Long envId, String name) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setEnvId(envId);
        devopsServiceDTO.setName(name);
        return devopsServiceMapper.selectOne(devopsServiceDTO) == null;
    }

    @Override
    public Page<DevopsServiceQueryDTO> basePageByOptions(Long projectId, Long envId, Long instanceId, PageRequest pageable,
                                                         String searchParam, Long appServiceId) {

        Sort sort = pageable.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageable.getSort().iterator()).stream()
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
                        } else if ("id".equals(property)) {
                            property = "ds.id";
                        } else {
                            throw new CommonException(DEVOPS_FIELD_NOT_SUPPORTED_FOR_SORT, t.getProperty());
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        //分页组件暂不支持级联查询，只能手写分页
        Map<String, Object> searchParamMap = TypeUtil.castMapParams(searchParam);
        List<String> paramList = TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS));
        // todo 遗留逻辑混乱，待梳理
        return PageInfoUtil.createPageFromList(devopsServiceMapper.listDevopsServiceByPage(
                projectId, envId, instanceId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                paramList, sortResult, appServiceId), pageable);
    }

    @Override
    public List<DevopsServiceDTO> baseListByEnvId(Long envId) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setEnvId(envId);
        return devopsServiceMapper.select(devopsServiceDTO);
    }

    @Override
    public Integer countInstanceService(Long projectId, Long envId, Long objectId) {
        List<DevopsServiceQueryDTO> devopsServiceQueryDTOS = devopsServiceMapper.listDevopsServiceByPage(
                projectId, envId, objectId, null,
                null, null, null);
        return devopsServiceQueryDTOS.size();
    }


    public DevopsServiceQueryDTO baseQueryById(Long id) {
        return devopsServiceMapper.queryById(id);
    }


    @Override
    public DevopsServiceDTO baseCreate(DevopsServiceDTO devopsServiceDTO) {
        if (devopsServiceMapper.insert(devopsServiceDTO) != 1) {
            throw new CommonException("devops.k8s.service.create");
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
        if (devopsServiceDTO.getSelectors() == null) {
            devopsServiceMapper.updateSelectorsToNull(devopsServiceDTO.getId());
        }
        if (devopsServiceDTO.getExternalIp() == null) {
            devopsServiceMapper.setExternalIpNull(devopsServiceDTO.getId());
        }
        devopsServiceDTO.setObjectVersionNumber(oldDevopsServiceDTO.getObjectVersionNumber());
        if (devopsServiceMapper.updateByPrimaryKeySelective(devopsServiceDTO) != 1) {
            throw new CommonException("devops.k8s.service.update");
        }
    }

    @Override
    public void baseUpdateSelectors(Long id) {
        devopsServiceMapper.updateSelectorsToNull(id);
    }


    @Override
    public void baseUpdateEndPoint(Long id) {
        devopsServiceMapper.updateEndPointToNull(id);
    }

    @Override
    public void baseUpdateAnnotations(Long id) {
        devopsServiceMapper.updateAnnotationsToNull(id);
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


    private DevopsServiceDTO handlerCreateService(DevopsServiceReqVO devopsServiceReqVO) {

        //校验service相关参数
        DevopsServiceValidator.checkService(devopsServiceReqVO, null);

        initDevopsServicePorts(devopsServiceReqVO);

        DevopsEnvironmentDTO devopsEnvironmentDTO =
                devopsEnvironmentService.baseQueryById(devopsServiceReqVO.getEnvId());
        if (!baseCheckName(devopsEnvironmentDTO.getId(), devopsServiceReqVO.getName())) {
            throw new CommonException("devops.service.name.exist");
        }

        //初始化DevopsService对象
        DevopsServiceDTO devopsServiceDTO = voToDto(devopsServiceReqVO);
        return initDevopsService(devopsServiceDTO, devopsServiceReqVO);

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
        devopsServiceVO.setSelectors(gson.fromJson(devopsServiceQueryDTO.getSelectors(), new TypeToken<Map<String, String>>() {
        }.getType()));
        DevopsServiceConfigVO devopsServiceConfigVO = new DevopsServiceConfigVO();
        devopsServiceConfigVO.setPorts(gson.fromJson(devopsServiceQueryDTO.getPorts(), new TypeToken<ArrayList<PortMapVO>>() {
        }.getType()));
        if (devopsServiceQueryDTO.getExternalIp() != null) {
            devopsServiceConfigVO.setExternalIps(new ArrayList<>(
                    Arrays.asList(devopsServiceQueryDTO.getExternalIp().split(","))));
        }
        if (devopsServiceQueryDTO.getClusterIp() != null) {
            devopsServiceConfigVO.setClusterIp(devopsServiceQueryDTO.getClusterIp());
        }
        if (!ObjectUtils.isEmpty(devopsServiceQueryDTO.getAnnotations())) {
            devopsServiceVO.setAnnotations(JsonHelper.unmarshalByJackson(devopsServiceQueryDTO.getAnnotations(), new TypeReference<Map<String, String>>() {
            }));
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
        if (!ObjectUtils.isEmpty(devopsServiceQueryDTO.getMessage())) {
            V1Service v1Service = json.deserialize(devopsServiceQueryDTO.getMessage(), V1Service.class);
            devopsServiceTargetVO.setSelectors(v1Service.getSpec().getSelector());
            devopsServiceVO.setLabels(v1Service.getMetadata().getLabels());
        }
        devopsServiceTargetVO.setSelectors(gson.fromJson(devopsServiceQueryDTO.getSelectors(), new TypeToken<Map<String, String>>() {
        }.getType()));
        devopsServiceTargetVO.setEndPoints(gson.fromJson(devopsServiceQueryDTO.getEndPoints(), new TypeToken<Map<String, List<EndPointPortVO>>>() {
        }.getType()));
        if (devopsServiceQueryDTO.getTargetAppServiceId() != null) {
            devopsServiceTargetVO.setTargetAppServiceId(devopsServiceQueryDTO.getTargetAppServiceId());
            AppServiceDTO appServiceDTO = applicationService.baseQuery(devopsServiceQueryDTO.getTargetAppServiceId());
            if (appServiceDTO != null) {
                devopsServiceTargetVO.setTargetAppServiceName(appServiceDTO.getName());
            } else {
                MarketServiceVO marketServiceVO = marketServiceClientOperator.queryMarketService(0L, devopsServiceQueryDTO.getTargetAppServiceId());
                if (marketServiceVO != null) {
                    devopsServiceTargetVO.setTargetAppServiceName(marketServiceVO.getMarketServiceName());
                } else {
                    devopsServiceTargetVO.setTargetAppServiceName(MiscConstants.UNKNOWN_SERVICE);
                }
            }
        }

        if (devopsServiceQueryDTO.getTargetDeploymentId() != null) {
            devopsServiceTargetVO.setTargetDeploymentId(devopsServiceQueryDTO.getTargetDeploymentId());
            DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.selectByPrimaryKey(devopsServiceQueryDTO.getTargetDeploymentId());
            if (devopsDeploymentDTO != null) {
                devopsServiceTargetVO.setTargetDeploymentName(devopsDeploymentDTO.getName());
            }
        }
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
    public DevopsServiceVO querySingleService(Long id) {
        DevopsServiceQueryDTO devopsServiceQueryDTO = baseQueryById(id);

        if (devopsServiceQueryDTO == null) {
            return null;
        }

        fillAppServiceInfo(devopsServiceQueryDTO);

        return querySingleServiceDtoToVo(devopsServiceQueryDTO);
    }

    /**
     * 为网络对象填充关联的实例对应的应用服务信息
     *
     * @param devopsServiceQueryDTO 网络
     */
    private void fillAppServiceInfo(@Nonnull DevopsServiceQueryDTO devopsServiceQueryDTO) {
        List<DevopsServiceInstanceDTO> instances = devopsServiceInstanceService.baseListByServiceId(devopsServiceQueryDTO.getId());
        //网络多实例中存在删除实例时，给应用信息赋值
        if (!instances.isEmpty()) {
            for (DevopsServiceInstanceDTO devopsServiceAppInstanceDTO : instances) {
                AppServiceInstanceDTO applicationInstanceDTO = appServiceInstanceService.baseQuery(devopsServiceAppInstanceDTO.getInstanceId());
                if (applicationInstanceDTO != null) {
                    AppServiceDTO appServiceDTO = applicationService.baseQuery(applicationInstanceDTO.getAppServiceId());
                    devopsServiceQueryDTO.setAppServiceId(appServiceDTO.getId());
                    devopsServiceQueryDTO.setAppServiceName(appServiceDTO.getName());
                    devopsServiceQueryDTO.setAppServiceProjectId(appServiceDTO.getProjectId());
                }
            }
        }
    }

    private DevopsServiceVO querySingleServiceDtoToVo(@Nonnull DevopsServiceQueryDTO devopsServiceQueryDTO) {
        DevopsServiceVO devopsServiceVO = queryDtoToVo(devopsServiceQueryDTO);
        Long envId = devopsServiceQueryDTO.getEnvId();
        //获得pod实时信息
        if (devopsServiceQueryDTO.getInstances() != null) {
            List<PodLiveInfoVO> instancePodLiveInfoVOs = devopsServiceQueryDTO.getInstances()
                    .stream()
                    .map(instanceInfoVO -> getInstancePodLiveInfoVOs(instanceInfoVO.getId(), envId))
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            //去除CPIU和内存的信息
            instancePodLiveInfoVOs.stream().forEach(e -> {
                e.setCpuUsedList(Collections.emptyList());
                e.setMemoryUsedList(Collections.emptyList());
            });
            devopsServiceVO.setPodLiveInfos(instancePodLiveInfoVOs);
        }
        return devopsServiceVO;
    }

    private List<PodLiveInfoVO> getInstancePodLiveInfoVOs(Long instanceId, Long envId) {
        PodLiveInfoVO podLiveInfoVO = new PodLiveInfoVO();
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(devopsEnvironmentDTO.getClusterId());


        //从数据库中获得pod已经存在的信息
        List<DevopsEnvPodDTO> devopsEnvPodDTOList = devopsEnvPodMapper.queryPodByEnvIdAndInstanceId(instanceId, envId);
        if (CollectionUtils.isEmpty(devopsEnvPodDTOList)) {
            return new ArrayList<>();
        }
        List<PodLiveInfoVO> podLiveInfoVOList;
        podLiveInfoVOList = devopsEnvPodDTOList.stream().map(devopsEnvPodDTO -> {
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
            List<PodMetricsRedisInfoVO> agentPodInfoVOS = agentPodInfoService.queryAllPodSnapshots(devopsEnvPodDTO.getName(), devopsEnvPodDTO.getNamespace(), devopsClusterDTO.getCode());

            if (!agentPodInfoVOS.isEmpty()) {
                List<Long> cpuUsedList = agentPodInfoVOS.stream()
                        .map(info -> {
                            if (info.getCpu().equals("0")) {
                                return 0L;
                            } else {
                                return TypeUtil.objToLong(info.getCpu().substring(0, (info.getCpu().length() - 1)));
                            }
                        }).collect(Collectors.toList());
                List<Long> memoryUsedList = agentPodInfoVOS.stream()
                        .map(info -> {
                            if (info.getMemory().equals("0")) {
                                return 0L;
                            } else {
                                return TypeUtil.objToLong(info.getMemory().substring(0, (info.getMemory()).length() - 2));
                            }
                        })
                        .collect(Collectors.toList());
                List<Date> timeList = agentPodInfoVOS.stream().map(PodMetricsRedisInfoVO::getSnapShotTime).collect(Collectors.toList());

                podLiveInfoVO.setCpuUsedList(cpuUsedList);
                podLiveInfoVO.setMemoryUsedList(memoryUsedList);
                podLiveInfoVO.setTimeList(timeList);
                podLiveInfoVO.setNodeIp(v1Pod.getStatus().getHostIP());
                podLiveInfoVO.setNodeName(v1Pod.getSpec().getNodeName());
                podLiveInfoVO.setPodIp(v1Pod.getStatus().getPodIP());
            }
            return podLiveInfoVO;
        }).collect(Collectors.toList());
        return podLiveInfoVOList;
    }

    private DevopsServiceDTO initDevopsService(DevopsServiceDTO devopsServiceDTO, DevopsServiceReqVO devopsServiceReqVO) {
        BeanUtils.copyProperties(devopsServiceReqVO, devopsServiceDTO);
        if (devopsServiceReqVO.getSelectors() != null) {
            // 容错逻辑，可能是以前版本将label写入当做选择器写入了selector字段中
            if (devopsServiceReqVO.getSelectors().size() == 1 && devopsServiceReqVO.getSelectors().containsKey(SERVICE_LABLE)) {
                baseUpdateSelectors(devopsServiceDTO.getId());
                devopsServiceDTO.setSelectors(null);
            } else {
                devopsServiceReqVO.getSelectors().remove(SERVICE_LABLE);
                devopsServiceDTO.setSelectors(gson.toJson(devopsServiceReqVO.getSelectors()));
            }
        } else {
            baseUpdateSelectors(devopsServiceDTO.getId());
            devopsServiceDTO.setSelectors(null);
        }
        if (!ObjectUtils.isEmpty(devopsServiceReqVO.getAnnotations())) {
            devopsServiceDTO.setAnnotations(JsonHelper.marshalByJackson(devopsServiceReqVO.getAnnotations()));
        } else {
            baseUpdateAnnotations(devopsServiceDTO.getId());
            devopsServiceDTO.setAnnotations(null);
        }
        if (devopsServiceReqVO.getEndPoints() != null) {
            devopsServiceDTO.setEndPoints(gson.toJson(devopsServiceReqVO.getEndPoints()));
        } else {
            baseUpdateEndPoint(devopsServiceDTO.getId());
            devopsServiceDTO.setEndPoints(null);
        }
        if (devopsServiceReqVO.getTargetAppServiceId() != null) {
            devopsServiceDTO.setTargetAppServiceId(devopsServiceReqVO.getTargetAppServiceId());
        } else {
            devopsServiceDTO.setTargetAppServiceId(null);
            baseUpdateTargetAppServiceId(devopsServiceDTO.getId());
        }
        if (devopsServiceReqVO.getTargetInstanceCode() != null) {
            devopsServiceDTO.setTargetInstanceCode(devopsServiceReqVO.getTargetInstanceCode());
        } else {
            devopsServiceDTO.setTargetInstanceCode(null);
            baseUpdateTargetInstanceCode(devopsServiceDTO.getId());
        }
        if (devopsServiceReqVO.getTargetDeploymentId() != null) {
            devopsServiceDTO.setTargetDeploymentId(devopsServiceReqVO.getTargetDeploymentId());
        } else {
            devopsServiceDTO.setTargetDeploymentId(null);
            baseUpdateTargetDeploymentId(devopsServiceDTO.getId());
        }
        if (devopsServiceReqVO.getAppServiceId() == null) {
            if (devopsServiceReqVO.getTargetAppServiceId() != null) {
                devopsServiceDTO.setAppServiceId(devopsServiceReqVO.getTargetAppServiceId());
            }
            if (devopsServiceReqVO.getTargetInstanceCode() != null) {
                AppServiceInstanceDTO instanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(devopsServiceReqVO.getTargetInstanceCode(), devopsServiceReqVO.getEnvId());
                if (instanceDTO != null) {
                    devopsServiceDTO.setAppServiceId(instanceDTO.getAppServiceId());
                }
            }
        }
        devopsServiceDTO.setPorts(gson.toJson(devopsServiceReqVO.getPorts()));
        devopsServiceDTO.setType(devopsServiceReqVO.getType() == null ? "ClusterIP" : devopsServiceReqVO.getType());
        devopsServiceDTO.setStatus(ServiceStatus.OPERATIING.getStatus());
        return devopsServiceDTO;
    }

    private void baseUpdateTargetAppServiceId(Long devopsServiceId) {
        devopsServiceMapper.updateTargetAppServiceIdToNull(devopsServiceId);
    }

    private void baseUpdateTargetInstanceCode(Long devopsServiceId) {
        devopsServiceMapper.updateTargetInstanceCodeToNull(devopsServiceId);
    }

    private void baseUpdateTargetDeploymentId(Long devopsServiceId) {
        devopsServiceMapper.updateTargetDeploymentIdToNull(devopsServiceId);
    }

    private DevopsServiceDTO handlerUpdateService(DevopsServiceReqVO devopsServiceReqVO, DevopsServiceDTO devopsServiceDTO) {
        //service参数校验
        DevopsServiceValidator.checkService(devopsServiceReqVO, devopsServiceDTO.getId());
        initDevopsServicePorts(devopsServiceReqVO);

        if (!devopsServiceDTO.getEnvId().equals(devopsServiceReqVO.getEnvId())) {
            throw new CommonException("devops.env.notEqual");
        }
        String serviceName = devopsServiceReqVO.getName();
        if (!serviceName.equals(devopsServiceDTO.getName())) {
            throw new CommonException("devops.name.notEqual");
        }
        //验证网络是否需要更新
        List<PortMapVO> oldPort = gson.fromJson(devopsServiceDTO.getPorts(), new TypeToken<ArrayList<PortMapVO>>() {
        }.getType());
        boolean isUpdate = false;

        if (!Objects.equals(devopsServiceReqVO.getTargetAppServiceId(), devopsServiceDTO.getTargetAppServiceId())) {
            isUpdate = true;
        }
        if (!isUpdate && !Objects.equals(devopsServiceReqVO.getTargetInstanceCode(), devopsServiceDTO.getTargetInstanceCode())) {
            isUpdate = true;
        }
        if (!isUpdate && devopsServiceReqVO.getSelectors() != null && devopsServiceDTO.getSelectors() != null) {
            if (!gson.toJson(devopsServiceReqVO.getSelectors()).equals(devopsServiceDTO.getSelectors())) {
                isUpdate = true;
            }
        }
        if (!isUpdate) {
            // 将annotations去掉
            if (ObjectUtils.isEmpty(devopsServiceReqVO.getAnnotations()) && !ObjectUtils.isEmpty(devopsServiceDTO.getAnnotations())) {
                isUpdate = true;
            } else if (!ObjectUtils.isEmpty(devopsServiceReqVO.getAnnotations()) && ObjectUtils.isEmpty(devopsServiceDTO.getAnnotations())) {
                // 添加annotations
                isUpdate = true;
            } else if (!ObjectUtils.isEmpty(devopsServiceReqVO.getAnnotations()) && !ObjectUtils.isEmpty(devopsServiceDTO.getAnnotations()) && !JsonHelper.marshalByJackson(devopsServiceReqVO.getAnnotations()).equals(devopsServiceDTO.getAnnotations())) {
                // 修改annotations
                isUpdate = true;
            }
        }

        if (!isUpdate && devopsServiceReqVO.getEndPoints() != null && devopsServiceDTO.getEndPoints() != null) {
            if (!gson.toJson(devopsServiceReqVO.getEndPoints()).equals(devopsServiceDTO.getEndPoints())) {
                isUpdate = true;
            }
        }
        if (!isUpdate && devopsServiceReqVO.getTargetDeploymentId() != null && devopsServiceDTO.getTargetDeploymentId() != null) {
            if (!devopsServiceReqVO.getTargetDeploymentId().equals(devopsServiceDTO.getTargetDeploymentId())) {
                isUpdate = true;
            }
        }

        if (!isUpdate && oldPort.stream().sorted().collect(Collectors.toList())
                .equals(devopsServiceReqVO.getPorts().stream().sorted().collect(Collectors.toList()))
                && !isUpdateExternalIp(devopsServiceReqVO, devopsServiceDTO)) {
            return null;
        }


        //初始化DevopsService对象
        return initDevopsService(devopsServiceDTO, devopsServiceReqVO);
    }


    /**
     * 获取k8s service的yaml格式
     */
    private V1Service initV1Service(DevopsServiceReqVO devopsServiceReqVO) {
        V1Service service = new V1Service();
        service.setKind(SERVICE);
        service.setApiVersion("v1");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsServiceReqVO.getName());
        metadata.setAnnotations(devopsServiceReqVO.getAnnotations());
        Map<String, String> label = new HashMap<>();
        label.put(SERVICE_LABLE, SERVICE_LABLE_VALUE);
        metadata.setLabels(label);
        service.setMetadata(metadata);

        V1ServiceSpec spec = new V1ServiceSpec();
        spec.setType(devopsServiceReqVO.getType() == null ? "ClusterIP" : devopsServiceReqVO.getType());

        Map<String, String> instanceSelector = buildSelectorForInstance(
                devopsServiceReqVO.getTargetInstanceCode(),
                devopsServiceReqVO.getTargetAppServiceId(),
                devopsServiceReqVO.getTargetDeploymentId());
        if (instanceSelector.isEmpty()) {
            spec.setSelector(devopsServiceReqVO.getSelectors());
        } else {
            spec.setSelector(instanceSelector);
        }

        final Integer[] serialNumber = {0};
        List<V1ServicePort> ports = devopsServiceReqVO.getPorts().stream()
                .map(t -> {
                    V1ServicePort v1ServicePort = new V1ServicePort();
                    if (t.getNodePort() != null) {
                        v1ServicePort.setNodePort(t.getNodePort());
                    }
                    if (t.getPort() != null) {
                        v1ServicePort.setPort(t.getPort());
                    }
                    if (t.getTargetPort() != null) {
                        v1ServicePort.setTargetPort(new IntOrString(t.getTargetPort()));
                    }
                    serialNumber[0] = serialNumber[0] + 1;
                    v1ServicePort.setName(t.getName() == null ? "http" + serialNumber[0] : t.getName());
                    v1ServicePort.setProtocol(t.getProtocol() == null ? "TCP" : t.getProtocol());
                    return v1ServicePort;
                }).collect(Collectors.toList());

        if (!ObjectUtils.isEmpty(devopsServiceReqVO.getExternalIp())) {
            List<String> externalIps = new ArrayList<>(
                    Arrays.asList(devopsServiceReqVO.getExternalIp().split(",")));
            spec.setExternalIPs(externalIps);
        }

        spec.setPorts(ports);
        spec.setSessionAffinity("None");
        service.setSpec(spec);

        return service;
    }

    /**
     * 根据实例的code或者应用服务的id构建网络的选择器
     *
     * @param targetInstanceCode 实例Id
     * @param targetAppServiceId 目标应用服务id
     * @param targetDeploymentId 目标部署组id
     * @return 选择器
     */
    @Nonnull
    private Map<String, String> buildSelectorForInstance(String targetInstanceCode,
                                                         Long targetAppServiceId,
                                                         Long targetDeploymentId) {
        Map<String, String> selectors = new HashMap<>();
        if (targetInstanceCode != null) {
            selectors.put(AppServiceInstanceService.INSTANCE_LABEL_RELEASE, targetInstanceCode);
        }
        if (targetAppServiceId != null) {
            selectors.put(AppServiceInstanceService.INSTANCE_LABEL_APP_SERVICE_ID, targetAppServiceId.toString());
        }
        if (targetDeploymentId != null) {
            DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.selectByPrimaryKey(targetDeploymentId);
            selectors.put(PARENT_WORK_LOAD_NAME_LABEL, devopsDeploymentDTO.getName());
            selectors.put(PARENT_WORK_LOAD_LABEL, DEPLOYMENT.getType());
        }
        return selectors;
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
                CoreV1EndpointPort coreV1EndpointPort = new CoreV1EndpointPort();
                coreV1EndpointPort.setPort(port.getPort());
                serialNumber[0] = serialNumber[0] + 1;
                coreV1EndpointPort.setName(port.getName() == null ? "http" + serialNumber[0] : port.getName());
                return coreV1EndpointPort;
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
        return !((ObjectUtils.isEmpty(devopsServiceReqVO.getExternalIp())
                && ObjectUtils.isEmpty(devopsServiceDTO.getExternalIp()))
                || (!ObjectUtils.isEmpty(devopsServiceReqVO.getExternalIp())
                && !ObjectUtils.isEmpty(devopsServiceDTO.getExternalIp())
                && devopsServiceReqVO.getExternalIp().equals(devopsServiceDTO.getExternalIp())));
    }


    private void operateEnvGitLabFile(V1Service service, V1Endpoints v1Endpoints, Boolean isCreate,
                                      DevopsServiceDTO devopsServiceDTO,
                                      List<String> beforeDevopsServiceAppInstanceDTOS,
                                      DevopsEnvCommandDTO devopsEnvCommandDTO,
                                      UserAttrDTO userAttrDTO,
                                      DevopsIngressVO devopsIngressVO) {

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
        }


        ServiceSagaPayLoad serviceSagaPayLoad = new ServiceSagaPayLoad(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId());
        serviceSagaPayLoad.setDevopsServiceDTO(devopsServiceDTO);
        serviceSagaPayLoad.setV1Service(service);
        serviceSagaPayLoad.setCreated(isCreate);
        serviceSagaPayLoad.setV1Endpoints(v1Endpoints);
        serviceSagaPayLoad.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        if (devopsIngressVO != null) {
            devopsIngressVO.setEnvId(devopsServiceDTO.getEnvId());
        }
        serviceSagaPayLoad.setDevopsIngressVO(devopsIngressVO);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(devopsEnvironmentDTO.getProjectId())
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_SERVICE),
                builder -> builder
                        .withJson(gson.toJson(serviceSagaPayLoad))
                        .withRefId(devopsEnvironmentDTO.getId().toString()));
    }


    @Override
    public void updateStatus(DevopsServiceDTO devopsServiceDTO) {
        if (devopsServiceDTO.getSelectors() == null) {
            devopsServiceMapper.updateSelectorsToNull(devopsServiceDTO.getId());
        }
        if (devopsServiceDTO.getExternalIp() == null) {
            devopsServiceMapper.setExternalIpNull(devopsServiceDTO.getId());
        }
        devopsServiceMapper.updateStatus(devopsServiceDTO.getId(), devopsServiceDTO.getStatus());
    }

    @Override
    public void createServiceBySaga(ServiceSagaPayLoad serviceSagaPayLoad) {
        try {
            //更新网络的时候判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String filePath = null;
            if (!serviceSagaPayLoad.getCreated()) {
                filePath = clusterConnectionHandler.handDevopsEnvGitRepository(
                        serviceSagaPayLoad.getProjectId(),
                        serviceSagaPayLoad.getDevopsEnvironmentDTO().getCode(),
                        serviceSagaPayLoad.getDevopsEnvironmentDTO().getId(),
                        serviceSagaPayLoad.getDevopsEnvironmentDTO().getEnvIdRsa(),
                        serviceSagaPayLoad.getDevopsEnvironmentDTO().getType(),
                        serviceSagaPayLoad.getDevopsEnvironmentDTO().getClusterCode());
            }
            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<V1Service> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(serviceSagaPayLoad.getV1Service());

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    GitOpsConstants.SERVICE_PREFIX + serviceSagaPayLoad.getDevopsServiceDTO().getName(),
                    serviceSagaPayLoad.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    serviceSagaPayLoad.getCreated() ? CommandType.CREATE.getType() : CommandType.UPDATE.getType(),
                    serviceSagaPayLoad.getGitlabUserId(),
                    serviceSagaPayLoad.getDevopsServiceDTO().getId(), SERVICE, serviceSagaPayLoad.getV1Endpoints(), false, serviceSagaPayLoad.getDevopsEnvironmentDTO().getId(), filePath);

            if (Boolean.FALSE.equals(serviceSagaPayLoad.getCreated())) {
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService.baseQueryByEnvIdAndResourceId(serviceSagaPayLoad.getDevopsEnvironmentDTO().getId(), serviceSagaPayLoad.getDevopsServiceDTO().getId(), ObjectType.SERVICE.getType());
                // 更新对应的command的sha值
                if (devopsEnvFileResourceDTO != null) {
                    RepositoryFileDTO repositoryFile = gitlabServiceClientOperator.getWholeFile(TypeUtil.objToInteger(serviceSagaPayLoad.getDevopsEnvironmentDTO().getGitlabEnvProjectId()), GitOpsConstants.MASTER, devopsEnvFileResourceDTO.getFilePath());
                    devopsEnvCommandService.baseUpdateSha(serviceSagaPayLoad.getDevopsServiceDTO().getCommandId(), repositoryFile.getCommitId());
                }
            }

            //创建实例时，如果选了创建域名
            if (serviceSagaPayLoad.getDevopsIngressVO() != null) {
                serviceSagaPayLoad.getDevopsIngressVO().setAppServiceId(serviceSagaPayLoad.getDevopsServiceDTO().getTargetAppServiceId());
                List<DevopsIngressPathVO> devopsIngressPathVOS = serviceSagaPayLoad.getDevopsIngressVO().getPathList();
                devopsIngressPathVOS.forEach(devopsIngressPathVO -> {
                    DevopsServiceDTO devopsServiceDTO = baseQueryByNameAndEnvId(devopsIngressPathVO.getServiceName(), serviceSagaPayLoad.getDevopsEnvironmentDTO().getId());
                    if (devopsServiceDTO != null) {
                        devopsIngressPathVO.setServiceId(devopsServiceDTO.getId());
                    }
                });
                serviceSagaPayLoad.getDevopsIngressVO().setPathList(devopsIngressPathVOS);
                devopsIngressService.createIngress(serviceSagaPayLoad.getDevopsEnvironmentDTO().getProjectId(), serviceSagaPayLoad.getDevopsIngressVO());
            }
            //创建网络成功，发送webhook json
            sendNotificationService.sendWhenServiceCreationSuccessOrDelete(serviceSagaPayLoad.getDevopsServiceDTO(), serviceSagaPayLoad.getDevopsEnvironmentDTO(), SendSettingEnum.CREATE_RESOURCE.value());

        } catch (Exception e) {
            LOGGER.info("create or update service failed", e);
            //有异常更新网络以及command的状态
            DevopsServiceDTO devopsServiceDTO = baseQuery(serviceSagaPayLoad.getDevopsServiceDTO().getId());
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndResourceId(serviceSagaPayLoad.getDevopsEnvironmentDTO().getId(), devopsServiceDTO.getId(), SERVICE);
            String filePath = devopsEnvFileResourceDTO == null ? GitOpsConstants.SERVICE_PREFIX + devopsServiceDTO.getName() + GitOpsConstants.YAML_FILE_SUFFIX : devopsEnvFileResourceDTO.getFilePath();
            // 只有创建时判断并处理超时
            if (serviceSagaPayLoad.getCreated() && !gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(serviceSagaPayLoad.getDevopsEnvironmentDTO().getGitlabEnvProjectId()), GitOpsConstants.MASTER,
                    filePath)) {
                devopsServiceDTO.setStatus(CommandStatus.FAILED.getStatus());
                baseUpdate(devopsServiceDTO);
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsServiceDTO.getCommandId());
                devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
                devopsEnvCommandDTO.setError("create or update service failed");
                devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);

                // 发送创建失败通知 加上webhook json
                sendNotificationService.sendWhenServiceCreationFailure(devopsServiceDTO, devopsServiceDTO.getCreatedBy(), serviceSagaPayLoad.getDevopsEnvironmentDTO(), null);
            } else {
                throw e;
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


    private DevopsEnvCommandDTO initDevopsEnvCommandDTO(final String type) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        String actualType;
        if (CommandType.CREATE.getType().equals(type) || CommandType.UPDATE.getType().equals(type)) {
            actualType = type;
        } else {
            actualType = CommandType.DELETE.getType();
        }
        devopsEnvCommandDTO.setCommandType(actualType);
        devopsEnvCommandDTO.setObject(ObjectType.SERVICE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }


    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void saveOrUpdateChartResource(String detailsJson, AppServiceInstanceDTO appServiceInstanceDTO) {
        V1Service v1Service = json.deserialize(detailsJson, V1Service.class);

        DevopsServiceDTO oldDevopsServiceDTO = baseQueryByEnvIdAndName(appServiceInstanceDTO.getEnvId(), v1Service.getMetadata().getName());
        if (oldDevopsServiceDTO != null) {
            oldDevopsServiceDTO.setCommandId(appServiceInstanceDTO.getCommandId());
            oldDevopsServiceDTO.setAppServiceId(appServiceInstanceDTO.getAppServiceId());

            fillDevopsServiceInfo(oldDevopsServiceDTO, v1Service);

            oldDevopsServiceDTO.setLastUpdatedBy(appServiceInstanceDTO.getLastUpdatedBy());

            devopsServiceMapper.updateByPrimaryKeySelective(oldDevopsServiceDTO);
        } else {

            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
            if (devopsEnvironmentDTO == null) {
                LOGGER.error("save chart resource failed! env not found! envId: {}", appServiceInstanceDTO.getEnvId());
                return;
            }
            DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
            devopsServiceDTO.setEnvId(appServiceInstanceDTO.getEnvId());
            devopsServiceDTO.setCommandId(appServiceInstanceDTO.getId());
            devopsServiceDTO.setName(v1Service.getMetadata().getName());
            devopsServiceDTO.setInstanceId(appServiceInstanceDTO.getId());
            devopsServiceDTO.setAppServiceId(appServiceInstanceDTO.getAppServiceId());
            fillDevopsServiceInfo(devopsServiceDTO, v1Service);
            devopsServiceDTO.setCreatedBy(appServiceInstanceDTO.getCreatedBy());
            devopsServiceDTO.setLastUpdatedBy(appServiceInstanceDTO.getLastUpdatedBy());
            devopsServiceMapper.insertSelective(devopsServiceDTO);
        }
    }

    private void fillDevopsServiceInfo(DevopsServiceDTO oldDevopsServiceDTO, V1Service v1Service) {
        oldDevopsServiceDTO.setType(v1Service.getSpec().getType());
        if (!ObjectUtils.isEmpty(v1Service.getSpec().getExternalIPs())) {
            oldDevopsServiceDTO.setExternalIp(Strings.join(",", v1Service.getSpec().getExternalIPs().iterator()));
        }

        // 添加service类型
        if (ServiceTypeEnum.LOAD_BALANCER.value().equals(v1Service.getSpec().getType())) {
            oldDevopsServiceDTO.setLoadBalanceIp(v1Service.getStatus().getLoadBalancer().getIngress().get(0).getIp());
        } else if (ServiceTypeEnum.CLUSTER_IP.value().equals(v1Service.getSpec().getType())) {
            oldDevopsServiceDTO.setClusterIp(v1Service.getSpec().getClusterIP());
        } else if (ServiceTypeEnum.NODE_PORT.value().equals(v1Service.getSpec().getType())) {
            // do nothing
        } else {
            // 其他类型不保存
            throw new CommonException("devops.unknown.service.type");
        }

        // 添加选择器
        String appServiceId = v1Service.getMetadata().getLabels().get(AppServiceInstanceService.INSTANCE_LABEL_APP_SERVICE_ID);
        String instanceCode = v1Service.getMetadata().getLabels().get(AppServiceInstanceService.INSTANCE_LABEL_RELEASE);
        Map<String, String> selector = v1Service.getSpec().getSelector();
        if (appServiceId != null) {
            oldDevopsServiceDTO.setTargetAppServiceId(Long.parseLong(appServiceId));
        } else if (org.apache.commons.lang3.StringUtils.isNoneBlank(instanceCode)) {
            oldDevopsServiceDTO.setTargetInstanceCode(instanceCode);
        } else if (!CollectionUtils.isEmpty(selector)) {
            oldDevopsServiceDTO.setSelectors(gson.toJson(selector));
        }

        oldDevopsServiceDTO.setPorts(gson.toJson(v1Service.getSpec().getPorts()));
        oldDevopsServiceDTO.setStatus(ServiceStatus.RUNNING.getStatus());
    }

    private DevopsServiceDTO baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setEnvId(envId);
        devopsServiceDTO.setName(name);
        return devopsServiceMapper.selectOne(devopsServiceDTO);
    }

    @Override
    @Transactional
    public void deleteByEnvIdAndName(Long envId, String name) {
        Assert.notNull(envId, ResourceCheckConstant.DEVOPS_ENV_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.DEVOPS_RESOURCE_NAME_IS_NULL);
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setEnvId(envId);
        devopsServiceDTO.setName(name);
        devopsServiceMapper.delete(devopsServiceDTO);
    }

    @Override
    public ResourceType getType() {
        return ResourceType.SERVICE;
    }
}
