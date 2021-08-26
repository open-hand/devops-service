package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.JSON;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.DevopsIngressValidator;
import io.choerodon.devops.api.vo.DevopsIngressPathVO;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.DevopsServiceVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.IngressSagaPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsIngressMapper;
import io.choerodon.devops.infra.mapper.DevopsIngressPathMapper;
import io.choerodon.devops.infra.mapper.DevopsServiceMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;

@Service
public class DevopsIngressServiceImpl implements DevopsIngressService, ChartResourceOperatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsIngressServiceImpl.class);

    public static final String ERROR_DOMAIN_PATH_EXIST = "error.domain.path.exist";
    public static final String INGRESS = "Ingress";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    private static final String DOMAIN_NAME_EXIST_ERROR = "error.domain.name.exist";
    private static final String PATH_ERROR = "error.path.empty";
    private static final String PATH_DUPLICATED = "error.path.duplicated";
    private static final String ERROR_SERVICE_NOT_CONTAIN_PORT = "error.service.notContain.port";
    private static final String CERT_NOT_ACTIVE = "error.cert.notActive";
    private static final String INGRESS_NOT_EXIST = "ingress.not.exist";
    private static final Gson gson = new Gson();
    private static final Pattern PATTERN = Pattern.compile("^[-+]?[\\d]*$");

    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsServiceMapper devopsServiceMapper;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private CertificationService certificationService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsIngressMapper devopsIngressMapper;
    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    @Lazy
    private SendNotificationService sendNotificationService;
    @Autowired
    PermissionHelper permissionHelper;

    private JSON json = new JSON();


    @Override
    @Transactional(rollbackFor = Exception.class)
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_INGRESS,
            description = "Devops创建域名", inputSchema = "{}")
    public void createIngress(Long projectId, DevopsIngressVO devopsIngressVO) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, devopsIngressVO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        // 校验port是否属于该网络
        Set<Long> appServiceIds = new HashSet<>();

        DevopsIngressValidator.checkAnnotations(devopsIngressVO.getAnnotations());
        DevopsIngressValidator.checkHost(devopsIngressVO.getDomain());
        devopsIngressVO.getPathList().forEach(devopsIngressPathDTO -> {
            DevopsServiceDTO devopsServiceDTO = devopsServiceMapper.selectByPrimaryKey(devopsIngressPathDTO.getServiceId());
            if (devopsServiceDTO.getAppServiceId() != null) {
                appServiceIds.add(devopsServiceDTO.getAppServiceId());
            }
            if (dealWithPorts(devopsServiceDTO.getPorts()).stream().map(PortMapVO::getPort).noneMatch(port -> port.equals(devopsIngressPathDTO.getServicePort()))) {
                throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
            }
        });

        // 校验创建应用下域名时，所选的网络是否都是同一个应用下的
        if (devopsIngressVO.getAppServiceId() != null) {
            Set<Long> serviceIds = devopsIngressVO.getPathList().stream().map(DevopsIngressPathVO::getServiceId).collect(Collectors.toSet());
            if (!isAllServiceInApp(devopsIngressVO.getAppServiceId(), serviceIds)) {
                throw new CommonException("error.ingress.service.application");
            }
        }

        // 初始化V1beta1Ingress对象
        String certName = getCertName(devopsIngressVO.getCertId());
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName, devopsIngressVO.getAnnotations());

        // 处理创建域名数据
        DevopsIngressDTO devopsIngressDO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CREATE);

        // 在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), false, v1beta1Ingress, true, null, devopsIngressDO, userAttrDTO, devopsEnvCommandDTO, appServiceIds);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public IngressSagaPayload createForBatchDeployment(DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO, Long projectId, DevopsIngressVO devopsIngressVO) {
        // 校验port是否属于该网络
        devopsIngressVO.getPathList().forEach(devopsIngressPathDTO -> {
            DevopsServiceDTO devopsServiceDTO = devopsServiceMapper.selectByPrimaryKey(devopsIngressPathDTO.getServiceId());
            if (dealWithPorts(devopsServiceDTO.getPorts()).stream().map(PortMapVO::getPort).noneMatch(port -> port.equals(devopsIngressPathDTO.getServicePort()))) {
                throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
            }
        });

        // 校验创建应用下域名时，所选的网络是否都是同一个应用下的
        if (devopsIngressVO.getAppServiceId() != null) {
            Set<Long> serviceIds = devopsIngressVO.getPathList().stream().map(DevopsIngressPathVO::getServiceId).collect(Collectors.toSet());
            if (!isAllServiceInApp(devopsIngressVO.getAppServiceId(), serviceIds)) {
                throw new CommonException("error.ingress.service.application");
            }
        }

        // 初始化V1beta1Ingress对象
        String certName = getCertName(devopsIngressVO.getCertId());
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName, devopsIngressVO.getAnnotations());

        // 处理创建域名数据
        DevopsIngressDTO devopsIngressDTO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CREATE);

        Long ingressId = baseCreateIngressAndPath(devopsIngressDTO).getId();
        devopsEnvCommandDTO.setObjectId(ingressId);
        devopsIngressDTO.setId(ingressId);
        devopsIngressDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsIngressDTO);

        IngressSagaPayload ingressSagaPayload = new IngressSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId());
        ingressSagaPayload.setDevopsIngressDTO(devopsIngressDTO);
        ingressSagaPayload.setCreated(true);
        ingressSagaPayload.setV1beta1Ingress(v1beta1Ingress);
        ingressSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);
        return ingressSagaPayload;
    }


    /**
     * 查询传入的网络是否全是同一个应用下(传入的网络id所对应的网络的app_service_id字段是否都是指定的值)
     *
     * @param appServiceId 应用id
     * @param serviceIds   网络id
     * @return false 如果某个网络不存在和应用的关系或所有网络不在同一个应用下
     */
    private boolean isAllServiceInApp(Long appServiceId, Set<Long> serviceIds) {
        return devopsServiceMapper.isAllServicesInTheAppService(serviceIds, appServiceId);
    }


    private String getCertName(Long certId) {
        String certName = null;
        if (certId != null && certId != 0) {
            CertificationDTO certificationDTO = certificationService.baseQueryById(certId);
            if (!CertificationStatus.ACTIVE.getStatus().equals(certificationDTO.getStatus())) {
                throw new CommonException(CERT_NOT_ACTIVE);
            }
            certName = certificationDTO.getName();
        }
        return certName;
    }

    @Override
    public void createIngressByGitOps(DevopsIngressVO devopsIngressVO, Long projectId, Long userId) {
        // 校验环境是否连接
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressVO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        // 初始化V1beta1Ingress对象
        String certName = getCertName(devopsIngressVO.getCertId());
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName, devopsIngressVO.getAnnotations());
        // 处理域名数据
        DevopsIngressDTO devopsIngressDO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CREATE);

        // 创建域名
        Long ingressId = baseCreateIngressAndPath(devopsIngressDO).getId();
        devopsEnvCommandDTO.setObjectId(ingressId);
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsIngressDO.setId(ingressId);
        devopsIngressDO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsIngressDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateIngress(Long id, DevopsIngressVO devopsIngressVO, Long projectId) {

        boolean deleteCert = false;

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, devopsIngressVO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        DevopsIngressValidator.checkHost(devopsIngressVO.getDomain());
        DevopsIngressValidator.checkAnnotations(devopsIngressVO.getAnnotations());
        DevopsIngressDTO oldDevopsIngressDTO = baseQuery(id);
        if (oldDevopsIngressDTO.getCertId() != null && devopsIngressVO.getCertId() == null) {
            deleteCert = true;
        }

        // 更新域名的时候校验gitops库文件是否存在,处理部署域名时，由于没有创gitops文件导致的部署失败
        resourceFileCheckHandler.check(devopsEnvironmentDTO, id, devopsIngressVO.getName(), INGRESS);


        Set<Long> appServiceIds = new HashSet<>();
        // 校验port是否属于该网络
        devopsIngressVO.getPathList().forEach(devopsIngressPathDTO -> {
            DevopsServiceDTO devopsServiceDTO = devopsServiceMapper.selectByPrimaryKey(devopsIngressPathDTO.getServiceId());
            if (devopsServiceDTO.getAppServiceId() != null) {
                appServiceIds.add(devopsServiceDTO.getAppServiceId());
            }
            if (dealWithPorts(devopsServiceDTO.getPorts()).stream()
                    .map(PortMapVO::getPort).noneMatch(port -> port.equals(devopsIngressPathDTO.getServicePort()))) {
                throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
            }
        });

        // 校验创建应用下域名时，所选的网络是否都是同一个应用下的
        if (devopsIngressVO.getAppServiceId() != null) {
            Set<Long> serviceIds = devopsIngressVO.getPathList().stream().map(DevopsIngressPathVO::getServiceId).collect(Collectors.toSet());
            if (!isAllServiceInApp(devopsIngressVO.getAppServiceId(), serviceIds)) {
                throw new CommonException("error.ingress.service.application");
            }
        }

        // 判断ingress有没有修改，没有修改直接返回
        DevopsIngressVO ingressDTO = ConvertUtils.convertObject(baseQuery(id), DevopsIngressVO.class);
        if (devopsIngressVO.equals(ingressDTO)) {
            return;
        }

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(UPDATE);

        // 初始化V1beta1Ingress对象
        String certName = getCertName(devopsIngressVO.getCertId());
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName, devopsIngressVO.getAnnotations());

        // 处理域名数据
        devopsIngressVO.setId(id);
        DevopsIngressDTO devopsIngressDO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);


        // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvironmentDTO.getType(), devopsEnvironmentDTO.getClusterCode());

        //在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), deleteCert, v1beta1Ingress, false, path, devopsIngressDO, userAttrDTO, devopsEnvCommandDTO, appServiceIds);
    }

    /**
     * 反序列化数据库中的port字段
     *
     * @param ports 数据库中的port字段
     * @return 反序列化的数据
     */
    private List<PortMapVO> dealWithPorts(String ports) {
        return gson.fromJson(ports, new TypeToken<ArrayList<PortMapVO>>() {
        }.getType());
    }

    @Override
    public void updateIngressByGitOps(Long id, DevopsIngressVO devopsIngressVO, Long projectId, Long userId) {
        // 校验环境是否连接
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressVO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        // 判断ingress有没有修改，没有修改直接返回
        DevopsIngressVO ingressDTO = ConvertUtils.convertObject(baseQuery(id), DevopsIngressVO.class);
        if (devopsIngressVO.equals(ingressDTO)) {
            return;
        }

        // 初始化V1beta1Ingress对象
        String certName = devopsIngressVO.getCertName();
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName, devopsIngressVO.getAnnotations());

        // 处理域名数据
        devopsIngressVO.setId(id);
        DevopsIngressDTO devopsIngressDO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(UPDATE);

        // 更新域名域名
        devopsEnvCommandDTO.setObjectId(id);
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsIngressDO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdateIngressAndIngressPath(devopsIngressDO);
    }


    @Override
    public DevopsIngressVO queryIngress(Long projectId, Long ingressId) {
        DevopsIngressDTO devopsIngressDTO = devopsIngressMapper.selectByPrimaryKey(ingressId);
        if (devopsIngressDTO != null) {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());
            DevopsIngressVO devopsIngressVO = new DevopsIngressVO(
                    ingressId, devopsIngressDTO.getDomain(), devopsIngressDTO.getName(), devopsEnvironmentDTO.getId(),
                    devopsIngressDTO.getUsable(), devopsEnvironmentDTO.getName(), devopsIngressDTO.getInstanceId());
            DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO(ingressId);
            devopsIngressPathMapper.select(devopsIngressPathDTO).forEach(e -> setDevopsIngressDTO(devopsIngressVO, e));
            devopsIngressDTO.setStatus(devopsIngressDTO.getStatus());

            if (devopsIngressDTO.getAnnotations() != null) {
                devopsIngressVO.setAnnotations(gson.fromJson(devopsIngressDTO.getAnnotations(), new TypeToken<Map<String, String>>() {
                }.getType()));
            }

            setIngressDTOCert(devopsIngressDTO.getCertId(), devopsIngressVO);
            return devopsIngressVO;
        }
        return null;
    }

    @Override
    public DevopsIngressVO queryIngressDetailById(Long projectId, Long ingressId) {
        DevopsIngressDTO devopsIngressDTO = devopsIngressMapper.queryById(ingressId);
        if (devopsIngressDTO == null) {
            return null;
        }

        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();

        DevopsIngressVO vo = new DevopsIngressVO();
        BeanUtils.copyProperties(devopsIngressDTO, vo, "annotations");
        if (devopsIngressDTO.getAnnotations() != null) {
            vo.setAnnotations(gson.fromJson(devopsIngressDTO.getAnnotations(), new TypeToken<Map<String, String>>() {
            }.getType()));
        }
        vo.setInstances(devopsIngressMapper.listInstanceNamesByIngressId(vo.getId()));

        if (devopsIngressDTO.getCertId() != null) {
            CertificationDTO certificationDTO = certificationService.baseQueryById(devopsIngressDTO.getCertId());
            if (certificationDTO != null) {
                vo.setCertName(certificationDTO.getName());
                vo.setCertStatus(certificationDTO.getStatus());
            }
        }

        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO(vo.getId());
        devopsIngressPathMapper.select(devopsIngressPathDTO).forEach(e -> setDevopsIngressDTO(vo, e));

        if (devopsIngressDTO.getCreatedBy() != null && devopsIngressDTO.getCreatedBy() != 0) {
            vo.setCreatorName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsIngressDTO.getCreatedBy()));
        }
        if (devopsIngressDTO.getLastUpdatedBy() != null && devopsIngressDTO.getLastUpdatedBy() != 0) {
            vo.setLastUpdaterName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsIngressDTO.getLastUpdatedBy()));
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());
        vo.setEnvStatus(updatedEnvList.contains(devopsEnvironmentDTO.getClusterId()));

        return vo;
    }

    @Override

    public Page<DevopsIngressVO> pageByEnv(Long projectId, Long envId, PageRequest pageable, String params) {
        Page<DevopsIngressVO> devopsIngressVOPage = basePageByOptions(projectId, envId, null, pageable, params);

        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();
        devopsIngressVOPage.getContent().forEach(devopsIngressVO -> {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressVO.getEnvId());
            devopsIngressVO.setEnvStatus(updatedEnvList.contains(devopsEnvironmentDTO.getClusterId()));
        });
        return devopsIngressVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIngress(Long projectId, Long ingressId) {
        DevopsIngressDTO ingressDO = baseQuery(ingressId);

        if (ingressDO == null) {
            return;
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, ingressDO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(DELETE);

        // 更新ingress
        devopsEnvCommandDTO.setObjectId(ingressId);
        DevopsIngressDTO devopsIngressDTO = baseQuery(ingressId);
        devopsIngressDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        devopsIngressDTO.setStatus(IngressStatus.OPERATING.getStatus());
        baseUpdate(devopsIngressDTO);


        // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvironmentDTO.getType(), devopsEnvironmentDTO.getClusterCode());

        // 查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), ingressId, INGRESS);
        if (devopsEnvFileResourceDTO == null) {
            baseDelete(ingressId);
            baseDeletePathByIngressId(ingressId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), GitOpsConstants.MASTER,
                    GitOpsConstants.INGRESS_PREFIX + ingressDO.getName() + GitOpsConstants.YAML_FILE_SUFFIX)) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        GitOpsConstants.INGRESS_PREFIX + ingressDO.getName() + GitOpsConstants.YAML_FILE_SUFFIX,
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
            return;

        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), GitOpsConstants.MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                baseDelete(ingressId);

                baseDeletePathByIngressId(ingressId);
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
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        } else {
            ResourceConvertToYamlHandler<V1beta1Ingress> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1beta1Ingress v1beta1Ingress = new V1beta1Ingress();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(ingressDO.getName());
            v1beta1Ingress.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1beta1Ingress);
            Integer gitlabEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    null,
                    gitlabEnvProjectId,
                    DELETE,
                    userAttrDTO.getGitlabUserId(),
                    ingressDO.getId(), INGRESS, null, false, devopsEnvironmentDTO.getId(), path);
        }

        //删除域名成功发送web hook json
        sendNotificationService.sendWhenIngressSuccessOrDelete(devopsIngressDTO, SendSettingEnum.DELETE_RESOURCE.value());
    }


    @Override
    public void deleteIngressByGitOps(Long ingressId) {
        DevopsIngressDTO devopsIngressDTO = baseQuery(ingressId);


        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        devopsEnvCommandService.baseListByObject(ObjectType.INGRESS.getType(), ingressId).forEach(devopsEnvCommandDTO -> devopsEnvCommandService.baseDelete(devopsEnvCommandDTO.getId()));
        baseDelete(ingressId);
    }


    @Override
    public Boolean checkName(Long envId, String name) {
        return baseCheckName(envId, name);
    }

    @Override
    public Boolean checkDomainAndPath(Long envId, String domain, String path, Long id) {
        return baseCheckPath(envId, domain, path, id);
    }

    private V1beta1HTTPIngressPath createPath(String hostPath, String serviceName, Long port) {
        V1beta1HTTPIngressPath path = new V1beta1HTTPIngressPath();
        V1beta1IngressBackend backend = new V1beta1IngressBackend();
        backend.setServiceName(serviceName.toLowerCase());
        if (port != null) {
            backend.setServicePort(new IntOrString(port.intValue()));
        }
        path.setBackend(backend);
        path.setPath(hostPath);
        return path;
    }


    private V1beta1Ingress initV1beta1Ingress(String host, String name, @Nullable String certName, @Nullable Map<String, String> annotations) {
        V1beta1Ingress ingress = new V1beta1Ingress();
        ingress.setKind(INGRESS);
        ingress.setApiVersion("extensions/v1beta1");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        Map<String, String> labels = new HashMap<>();
        labels.put("choerodon.io/network", "ingress");

        metadata.setLabels(labels);
        metadata.setAnnotations(annotations == null ? new HashMap<>() : annotations);
        ingress.setMetadata(metadata);
        V1beta1IngressSpec spec = new V1beta1IngressSpec();

        List<V1beta1IngressRule> rules = new ArrayList<>();
        V1beta1IngressRule rule = new V1beta1IngressRule();
        V1beta1HTTPIngressRuleValue http = new V1beta1HTTPIngressRuleValue();
        List<V1beta1HTTPIngressPath> paths = new ArrayList<>();
        http.setPaths(paths);
        rule.setHost(host);
        rule.setHttp(http);
        rules.add(rule);
        spec.setRules(rules);

        if (certName != null) {
            List<V1beta1IngressTLS> tlsList = new ArrayList<>();
            V1beta1IngressTLS tls = new V1beta1IngressTLS();
            tls.addHostsItem(host);
            tls.setSecretName(certName);
            tlsList.add(tls);
            spec.setTls(tlsList);
        }

        ingress.setSpec(spec);
        return ingress;
    }

    private void operateEnvGitLabFile(Integer envGitLabProjectId,
                                      Boolean deleteCert,
                                      V1beta1Ingress ingress,
                                      Boolean isCreate,
                                      String path,
                                      DevopsIngressDTO devopsIngressDTO,
                                      UserAttrDTO userAttrDTO,
                                      DevopsEnvCommandDTO devopsEnvCommandDTO,
                                      Set<Long> appServiceIds) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());

        Long ingressId;
        //操作域名数据库
        if (isCreate) {
            ingressId = baseCreateIngressAndPath(devopsIngressDTO).getId();
            devopsEnvCommandDTO.setObjectId(ingressId);
            devopsIngressDTO.setId(ingressId);
            devopsIngressDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsIngressDTO);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsIngressDTO.getId());
            devopsIngressDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdateIngressAndIngressPath(devopsIngressDTO);
        }

        IngressSagaPayload ingressSagaPayload = new IngressSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId());
        ingressSagaPayload.setDevopsIngressDTO(devopsIngressDTO);
        ingressSagaPayload.setCreated(isCreate);
        ingressSagaPayload.setV1beta1Ingress(ingress);
        ingressSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(devopsIngressDTO.getProjectId())
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INGRESS),
                builder -> builder
                        .withJson(gson.toJson(ingressSagaPayload))
                        .withRefId(devopsEnvironmentDTO.getId().toString()));


    }

    @Override
    public void operateIngressBySaga(IngressSagaPayload ingressSagaPayload) {
        try {
            //更新域名时判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String filePath = null;
            if (!ingressSagaPayload.getCreated()) {
                filePath = clusterConnectionHandler.handDevopsEnvGitRepository(
                        ingressSagaPayload.getProjectId(),
                        ingressSagaPayload.getDevopsEnvironmentDTO().getCode(),
                        ingressSagaPayload.getDevopsEnvironmentDTO().getId(),
                        ingressSagaPayload.getDevopsEnvironmentDTO().getEnvIdRsa(),
                        ingressSagaPayload.getDevopsEnvironmentDTO().getType(),
                        ingressSagaPayload.getDevopsEnvironmentDTO().getClusterCode());
            }
            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<V1beta1Ingress> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(ingressSagaPayload.getV1beta1Ingress());

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    GitOpsConstants.INGRESS_PREFIX + ingressSagaPayload.getDevopsIngressDTO().getName(),
                    ingressSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    ingressSagaPayload.getCreated() ? CREATE : UPDATE,
                    ingressSagaPayload.getGitlabUserId(),
                    ingressSagaPayload.getDevopsIngressDTO().getId(), INGRESS, null, false, ingressSagaPayload.getDevopsEnvironmentDTO().getId(), filePath);
            if (ingressSagaPayload.getCreated()) {
                sendNotificationService.sendWhenIngressSuccessOrDelete(ingressSagaPayload.getDevopsIngressDTO(), SendSettingEnum.CREATE_RESOURCE.value());
            }
        } catch (Exception e) {
            LOGGER.info("create or update Ingress failed!", e);
            //有异常更新实例以及command的状态
            DevopsIngressDTO devopsIngressDTO = baseQuery(ingressSagaPayload.getDevopsIngressDTO().getId());
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndResourceId(ingressSagaPayload.getDevopsEnvironmentDTO().getId(), devopsIngressDTO.getId(), INGRESS);
            String filePath = devopsEnvFileResourceDTO == null ? GitOpsConstants.INGRESS_PREFIX + devopsIngressDTO.getName() + GitOpsConstants.YAML_FILE_SUFFIX : devopsEnvFileResourceDTO.getFilePath();
            // 只处理创建时可能的超时情况
            if (ingressSagaPayload.getCreated() && !gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(ingressSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId()), GitOpsConstants.MASTER,
                    filePath)) {
                devopsIngressDTO.setStatus(IngressStatus.FAILED.getStatus());
                baseUpdate(devopsIngressDTO);
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsIngressDTO.getCommandId());
                devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
                devopsEnvCommandDTO.setError("create or update Ingress failed!");
                devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);

                // 发送创建失败通知
                sendNotificationService.sendWhenIngressCreationFailure(devopsIngressDTO, devopsIngressDTO.getCreatedBy(), null);
            } else {
                throw e;
            }
        }
    }


    private DevopsIngressDTO handlerIngress(DevopsIngressVO devopsIngressVO, Long projectId, V1beta1Ingress v1beta1Ingress) {
        Long envId = devopsIngressVO.getEnvId();
        String ingressName = devopsIngressVO.getName();
        DevopsIngressValidator.checkIngressName(ingressName);
        String domain = devopsIngressVO.getDomain();

        //初始化ingressDO对象
        DevopsIngressDTO devopsIngressDO = new DevopsIngressDTO(devopsIngressVO.getId(), projectId, envId, domain, ingressName, IngressStatus.OPERATING.getStatus());

        if (v1beta1Ingress.getMetadata().getAnnotations() != null) {
            String annotations = gson.toJson(v1beta1Ingress.getMetadata().getAnnotations());
            // 避免数据比数据库结构的size还大
            if (annotations.length() > 2000) {
                throw new CommonException("error.ingress.annotations.too.large");
            }
            devopsIngressDO.setAnnotations(annotations);
        }

        //处理pathlist,生成域名和service的关联对象列表
        List<DevopsIngressPathDTO> devopsIngressPathDTOS = handlerPathList(devopsIngressVO.getPathList(), devopsIngressVO, v1beta1Ingress);

        //校验域名的domain和path是否在数据库中已存在
        if (devopsIngressPathDTOS.stream().noneMatch(
                t -> baseCheckPath(envId, devopsIngressDO.getDomain(), t.getPath(), devopsIngressVO.getId()))) {
            throw new CommonException(ERROR_DOMAIN_PATH_EXIST);
        }
        devopsIngressDO.setDevopsIngressPathDTOS(devopsIngressPathDTOS);
        devopsIngressDO.setCertId(devopsIngressVO.getCertId());
        return devopsIngressDO;
    }


    private List<DevopsIngressPathDTO> handlerPathList(List<DevopsIngressPathVO> pathList, DevopsIngressVO devopsIngressVO, V1beta1Ingress v1beta1Ingress) {
        if (pathList == null || pathList.isEmpty()) {
            throw new CommonException(PATH_ERROR);
        }
        List<DevopsIngressPathDTO> devopsIngressPathDTOS = new ArrayList<>();
        List<String> pathCheckList = new ArrayList<>();
        pathList.forEach(t -> {
            Long serviceId = t.getServiceId();
            Long servicePort = t.getServicePort();
            String hostPath = t.getPath();

            if (hostPath == null) {
                throw new CommonException(PATH_ERROR);
            }
            DevopsIngressValidator.checkPath(hostPath);
            if (pathCheckList.contains(hostPath)) {
                throw new CommonException(PATH_DUPLICATED);
            } else {
                pathCheckList.add(hostPath);
            }
            DevopsServiceDTO devopsServiceDTO = devopsServiceMapper.selectByPrimaryKey(serviceId);

            devopsIngressPathDTOS.add(new DevopsIngressPathDTO(
                    devopsIngressVO.getId(), hostPath,
                    devopsServiceDTO == null ? null : devopsServiceDTO.getId(), devopsServiceDTO == null ? t.getServiceName() : devopsServiceDTO.getName(), servicePort));
            v1beta1Ingress.getSpec().getRules().get(0).getHttp().addPathsItem(
                    createPath(hostPath, t.getServiceName(), servicePort));
        });
        return devopsIngressPathDTOS;
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
        devopsEnvCommandDTO.setObject(ObjectType.INGRESS.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }

    public DevopsIngressDTO baseCreateIngressAndPath(DevopsIngressDTO devopsIngressDTO) {
        if (!baseCheckName(devopsIngressDTO.getEnvId(), devopsIngressDTO.getName())) {
            throw new CommonException(DOMAIN_NAME_EXIST_ERROR);
        }
        devopsIngressMapper.insert(devopsIngressDTO);
        devopsIngressDTO.getDevopsIngressPathDTOS().forEach(t -> {
            t.setIngressId(devopsIngressDTO.getId());
            devopsIngressPathMapper.insert(t);
        });
        return devopsIngressDTO;
    }

    public void baseUpdateIngressAndIngressPath(DevopsIngressDTO devopsIngressDTO) {
        Long id = devopsIngressDTO.getId();
        DevopsIngressDTO ingressDTO = devopsIngressMapper.selectByPrimaryKey(id);
        if (ingressDTO == null) {
            throw new CommonException(INGRESS_NOT_EXIST);
        }
        if (!devopsIngressDTO.getName().equals(ingressDTO.getName())
                && !baseCheckName(devopsIngressDTO.getEnvId(), devopsIngressDTO.getName())) {
            throw new CommonException(DOMAIN_NAME_EXIST_ERROR);
        }
        if (!ingressDTO.equals(devopsIngressDTO)) {
            devopsIngressDTO.setObjectVersionNumber(ingressDTO.getObjectVersionNumber());
            devopsIngressMapper.updateByPrimaryKey(devopsIngressDTO);
        }
        List<DevopsIngressPathDTO> ingressPathList = devopsIngressPathMapper.select(new DevopsIngressPathDTO(id));
        if (!devopsIngressDTO.getDevopsIngressPathDTOS().equals(ingressPathList)) {
            devopsIngressPathMapper.delete(new DevopsIngressPathDTO(id));
            devopsIngressDTO.getDevopsIngressPathDTOS().forEach(t -> {
                t.setIngressId(id);
                devopsIngressPathMapper.insert(t);
            });
        }
    }

    public void baseUpdate(DevopsIngressDTO devopsIngressDTO) {
        Long id = devopsIngressDTO.getId();
        DevopsIngressDTO ingressDTO = devopsIngressMapper.selectByPrimaryKey(id);
        if (ingressDTO == null) {
            throw new CommonException("domain.not.exist");
        }
        if (!devopsIngressDTO.getName().equals(ingressDTO.getName())
                && !baseCheckName(devopsIngressDTO.getEnvId(), devopsIngressDTO.getName())) {
            throw new CommonException(DOMAIN_NAME_EXIST_ERROR);
        }
        devopsIngressDTO.setObjectVersionNumber(ingressDTO.getObjectVersionNumber());
        devopsIngressMapper.updateByPrimaryKeySelective(devopsIngressDTO);
    }

    @Override
    public Page<DevopsIngressVO> basePageByOptions(Long projectId, Long envId, Long serviceId, PageRequest pageable, String params) {
        List<DevopsIngressVO> devopsIngressVOS = new ArrayList<>();

        Map<String, Object> maps = TypeUtil.castMapParams(params);

        Sort sort = pageable.getSort();
        if (sort != null) {
            List<Sort.Order> newOrders = new ArrayList<>();
            sort.iterator().forEachRemaining(s -> {
                String property = s.getProperty();
                if (property.equals("envName")) {
                    property = "de.name";
                } else if (property.equals("path")) {
                    property = "dip.path";
                }
                newOrders.add(new Sort.Order(s.getDirection(), property));
            });
            pageable.setSort(new Sort(newOrders));
        }

        Page<DevopsIngressDTO> devopsIngressDTOPageInfo =
                PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                        () -> devopsIngressMapper.listIngressByOptions(projectId, envId, serviceId, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(maps.get(TypeUtil.PARAMS))));
        devopsIngressDTOPageInfo.getContent().forEach(t -> {
            DevopsIngressVO devopsIngressVO =
                    new DevopsIngressVO(t.getId(), t.getDomain(), t.getName(),
                            t.getEnvId(), t.getUsable(), t.getEnvName(), t.getInstanceId());
            devopsIngressVO.setStatus(t.getStatus());
            devopsIngressVO.setCommandStatus(t.getCommandStatus());
            devopsIngressVO.setCommandType(t.getCommandType());
            devopsIngressVO.setError(t.getError());
            if (t.getAnnotations() != null) {
                devopsIngressVO.setAnnotations(gson.fromJson(t.getAnnotations(), new TypeToken<Map<String, String>>() {
                }.getType()));
            }
            setIngressDTOCert(t.getCertId(), devopsIngressVO);
            DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO(t.getId());
            devopsIngressPathMapper.select(devopsIngressPathDTO).forEach(e -> setDevopsIngressDTO(devopsIngressVO, e));
            devopsIngressVOS.add(devopsIngressVO);
        });
        Page<DevopsIngressVO> ingressVOPageInfo = new Page<>();
        BeanUtils.copyProperties(devopsIngressDTOPageInfo, ingressVOPageInfo);
        ingressVOPageInfo.setContent(devopsIngressVOS);
        return ingressVOPageInfo;
    }

    @Override
    public DevopsIngressDTO baseQuery(Long ingressId) {
        return devopsIngressMapper.selectByPrimaryKey(ingressId);
    }


    private void setIngressDTOCert(Long certId, DevopsIngressVO devopsIngressVO) {
        if (certId != null) {

            CertificationDTO certificationDTO = certificationService.baseQueryById(certId);
            if (certificationDTO != null) {
                devopsIngressVO.setCertId(certId);
                devopsIngressVO.setCertName(certificationDTO.getName());
                devopsIngressVO.setCertStatus(certificationDTO.getStatus());
            }
        }
    }

    @Override
    public void baseDelete(Long ingressId) {
        devopsIngressMapper.deleteByPrimaryKey(ingressId);
        devopsIngressPathMapper.delete(new DevopsIngressPathDTO(ingressId));
    }

    @Override
    public Long baseUpdateStatus(Long envId, String name, String status) {
        DevopsIngressDTO ingressDTO = new DevopsIngressDTO(name);
        ingressDTO.setEnvId(envId);
        DevopsIngressDTO ingress = devopsIngressMapper.selectOne(ingressDTO);
        ingress.setStatus(status);
        if (status.equals(IngressStatus.RUNNING.getStatus())) {
            ingress.setUsable(true);
        }
        devopsIngressMapper.updateByPrimaryKey(ingress);
        return ingress.getId();
    }

    @Override
    public void updateStatus(Long envId, String name, String status) {
        devopsIngressMapper.updateStatus(envId, name, status);
    }

    @Override
    public List<String> baseListNameByServiceId(Long serviceId) {
        return devopsIngressMapper.listIngressNameByServiceId(serviceId);
    }

    @Override
    public Boolean baseCheckName(Long envId, String name) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO(name);
        devopsIngressDTO.setEnvId(envId);
        return devopsIngressMapper.select(devopsIngressDTO).isEmpty();
    }

    @Override
    public Boolean baseCheckPath(Long envId, String domain, String path, Long id) {
        return !devopsIngressPathMapper.checkDomainAndPath(envId, domain, path, id);
    }

    @Override
    public DevopsIngressDTO baseCheckByEnvAndName(Long envId, String name) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setEnvId(envId);
        devopsIngressDTO.setName(name);
        return devopsIngressMapper.selectOne(devopsIngressDTO);
    }

    @Override
    public DevopsIngressDTO baseCreateIngress(DevopsIngressDTO devopsIngressDTO) {
        if (devopsIngressMapper.insert(devopsIngressDTO) != 1) {
            throw new CommonException("error.domain.insert");
        }
        return devopsIngressDTO;
    }

    @Override
    public List<DevopsIngressDTO> baseListByEnvId(Long envId) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setEnvId(envId);
        return devopsIngressMapper.select(devopsIngressDTO);
    }

    @Override
    public void baseDeletePathByIngressId(Long ingressId) {
        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        devopsIngressPathDTO.setIngressId(ingressId);
        devopsIngressPathMapper.delete(devopsIngressPathDTO);
    }

    @Override
    public Boolean baseCheckByEnv(Long envId) {
        return devopsIngressMapper.checkEnvHasIngress(envId);
    }

    @Override
    public List<DevopsIngressDTO> baseList() {
        return devopsIngressMapper.selectAll();
    }

    private void setDevopsIngressDTO(DevopsIngressVO devopsIngressVO, DevopsIngressPathDTO devopsIngressPathDTO) {
        //待修改
        DevopsServiceVO devopsServiceVO = devopsServiceService.query(devopsIngressPathDTO.getServiceId());
        DevopsIngressPathVO devopsIngressPathVO = new DevopsIngressPathVO();
        devopsIngressPathVO.setPath(devopsIngressPathDTO.getPath());
        devopsIngressPathVO.setServiceId(devopsIngressPathDTO.getServiceId());
        devopsIngressPathVO.setServiceName(devopsIngressPathDTO.getServiceName());
        devopsIngressPathVO.setServicePort(devopsIngressPathDTO.getServicePort());
        if (devopsServiceVO != null) {
            devopsIngressPathVO.setServiceStatus(devopsServiceVO.getStatus());
            devopsIngressPathVO.setServiceError(devopsServiceVO.getError());
        } else {
            devopsIngressPathVO.setServiceStatus(ServiceStatus.DELETED.getStatus());
        }
        devopsIngressVO.addDevopsIngressPathDTO(devopsIngressPathVO);
    }

    @Override
    public void deleteIngressAndIngressPathByEnvId(Long envId) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setEnvId(envId);
        // 获取环境下的所有域名ids
        List<Long> allIngressIds = devopsIngressMapper.select(devopsIngressDTO).stream().map(DevopsIngressDTO::getId)
                .collect(Collectors.toList());
        devopsIngressMapper.delete(devopsIngressDTO);
        if (!allIngressIds.isEmpty()) {
            devopsIngressPathMapper.deleteByIngressIds(allIngressIds);
        }
    }

    @Override
    @Transactional
    public void saveOrUpdateChartResource(String detailsJson, AppServiceInstanceDTO appServiceInstanceDTO) {
        V1beta1Ingress v1beta1Ingress = json.deserialize(detailsJson, V1beta1Ingress.class);
        DevopsIngressDTO devopsIngressDTO = getDevopsIngressDTO(v1beta1Ingress, appServiceInstanceDTO.getEnvId());
        DevopsIngressDTO oldDevopsIngressDTO = baseQueryByEnvIdAndName(appServiceInstanceDTO.getEnvId(), v1beta1Ingress.getMetadata().getName());
        // 更新ingress
        if (oldDevopsIngressDTO != null) {
            // 更新ingress记录
            oldDevopsIngressDTO.setCommandId(appServiceInstanceDTO.getCommandId());
            oldDevopsIngressDTO.setStatus(IngressStatus.RUNNING.getStatus());
            devopsIngressMapper.updateByPrimaryKeySelective(oldDevopsIngressDTO);

            // 删除旧的ingressPath记录
            devopsIngressPathMapper.deleteByIngressId(oldDevopsIngressDTO.getId());

            // 插入ingressPath记录
            devopsIngressDTO.getDevopsIngressPathDTOS().forEach(t -> {
                t.setIngressId(oldDevopsIngressDTO.getId());
                devopsIngressPathMapper.insert(t);
            });
        } else {
            // 添加ingress
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
            if (devopsEnvironmentDTO == null) {
                LOGGER.error("save chart resource failed! env not found! envId: {}", appServiceInstanceDTO.getEnvId());
                return;
            }

            // 插入ingress记录
            devopsIngressDTO.setStatus(IngressStatus.RUNNING.getStatus());
            devopsIngressDTO.setEnvId(appServiceInstanceDTO.getEnvId());
            devopsIngressDTO.setCommandId(appServiceInstanceDTO.getId());
            devopsIngressDTO.setProjectId(devopsEnvironmentDTO.getProjectId());
            devopsIngressDTO.setName(v1beta1Ingress.getMetadata().getName());
            devopsIngressDTO.setInstanceId(appServiceInstanceDTO.getId());
            devopsIngressMapper.insertSelective(devopsIngressDTO);

            // 插入ingressPath记录
            devopsIngressDTO.getDevopsIngressPathDTOS().forEach(t -> {
                t.setIngressId(devopsIngressDTO.getId());
                devopsIngressPathMapper.insert(t);
            });
        }
    }

    private DevopsIngressDTO baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setEnvId(envId);
        devopsIngressDTO.setName(name);
        return devopsIngressMapper.selectOne(devopsIngressDTO);
    }

    @Override
    @Transactional
    public void deleteByEnvIdAndName(Long envId, String name) {
        Assert.notNull(envId, ResourceCheckConstant.ERROR_ENV_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.ERROR_RESOURCE_NAME_IS_NULL);
        DevopsIngressDTO devopsIngressToSearchDTO = new DevopsIngressDTO();
        devopsIngressToSearchDTO.setEnvId(envId);
        devopsIngressToSearchDTO.setName(name);
        DevopsIngressDTO devopsIngressDTO = baseQueryByEnvIdAndName(envId, name);

        devopsIngressPathMapper.deleteByIngressId(devopsIngressDTO.getId());
        devopsIngressMapper.deleteByPrimaryKey(devopsIngressDTO.getId());
    }

    @Override
    public ResourceType getType() {
        return ResourceType.INGRESS;
    }

    private DevopsIngressDTO getDevopsIngressDTO(V1beta1Ingress v1beta1Ingress, Long envId) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setDomain(v1beta1Ingress.getSpec().getRules().get(0).getHost()
        );
        devopsIngressDTO.setName(v1beta1Ingress.getMetadata().getName());
        String annotations = gson.toJson(v1beta1Ingress.getMetadata().getAnnotations());
        // 避免数据比数据库结构的size还大
        if (annotations.length() > 2000) {
            throw new CommonException("error.ingress.annotations.too.large");
        }
        devopsIngressDTO.setAnnotations(annotations);
        devopsIngressDTO.setEnvId(envId);
        List<String> pathCheckList = new ArrayList<>();
        List<DevopsIngressPathDTO> devopsIngressPathDTOS = new ArrayList<>();
        List<V1beta1HTTPIngressPath> paths = v1beta1Ingress.getSpec().getRules().get(0).getHttp().getPaths();
        for (V1beta1HTTPIngressPath v1beta1HTTPIngressPath : paths) {
            String path = v1beta1HTTPIngressPath.getPath();
            DevopsIngressValidator.checkPath(path);
            pathCheckList.add(path);
            V1beta1IngressBackend backend = v1beta1HTTPIngressPath.getBackend();
            String serviceName = backend.getServiceName();
            DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQueryByNameAndEnvId(
                    serviceName, envId);

            Long servicePort = null;
            IntOrString backendServicePort = backend.getServicePort();
            if (backendServicePort.isInteger() || PATTERN.matcher(TypeUtil.objToString(backendServicePort)).matches()) {
                servicePort = TypeUtil.objToLong(backendServicePort);
            } else {
                if (devopsServiceDTO != null) {
                    List<PortMapVO> listPorts = gson.fromJson(devopsServiceDTO.getPorts(), new TypeToken<ArrayList<PortMapVO>>() {
                    }.getType());
                    servicePort = listPorts.get(0).getPort();
                }
            }
            DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
            devopsIngressPathDTO.setPath(path);
            devopsIngressPathDTO.setServicePort(servicePort);
            devopsIngressPathDTO.setServiceName(serviceName);
            devopsIngressPathDTO.setServiceId(devopsServiceDTO == null ? null : devopsServiceDTO.getId());
            devopsIngressPathDTOS.add(devopsIngressPathDTO);
        }
        devopsIngressDTO.setDevopsIngressPathDTOS(devopsIngressPathDTOS);
        return devopsIngressDTO;
    }
}
