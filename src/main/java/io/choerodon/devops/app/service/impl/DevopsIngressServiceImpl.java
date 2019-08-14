package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import io.kubernetes.client.JSON;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.DevopsIngressValidator;
import io.choerodon.devops.api.vo.DevopsIngressPathVO;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.DevopsServiceVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.IngressSagaPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsAppServiceResourceMapper;
import io.choerodon.devops.infra.mapper.DevopsIngressMapper;
import io.choerodon.devops.infra.mapper.DevopsIngressPathMapper;
import io.choerodon.devops.infra.mapper.DevopsServiceMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.ResourceCreatorInfoUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Component
public class DevopsIngressServiceImpl implements DevopsIngressService {

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
    public static final String INGRESS_PREFIX = "ing-";
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;
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
    private DevopsApplicationResourceService devopsApplicationResourceService;
    @Autowired
    private DevopsIngressMapper devopsIngressMapper;
    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper;
    @Autowired
    private DevopsAppServiceResourceMapper devopsAppResourceMapper;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    private JSON json = new JSON();

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_INGRESS,
            description = "Devops创建域名", inputSchema = "{}")
    public void createIngress(Long projectId, DevopsIngressVO devopsIngressVO) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressVO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        // 校验port是否属于该网络
        devopsIngressVO.getPathList().forEach(devopsIngressPathDTO -> {
            DevopsServiceDTO devopsServiceDTO = devopsServiceMapper.selectByPrimaryKey(devopsIngressPathDTO.getServiceId());
            if (dealWithPorts(devopsServiceDTO.getPorts()).stream().map(PortMapVO::getPort).noneMatch(port -> port.equals(devopsIngressPathDTO.getServicePort()))) {
                throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
            }
        });

        // 校验创建应用下域名时，所选的网络是否都是同一个应用下的
        if (devopsIngressVO.getAppServiceId() != null) {
            List<Long> serviceIds = devopsIngressVO.getPathList().stream().map(DevopsIngressPathVO::getServiceId).collect(Collectors.toList());
            if (!isAllServiceInApp(devopsIngressVO.getAppServiceId(), serviceIds)) {
                throw new CommonException("error.ingress.service.application");
            }
        }

        // 初始化V1beta1Ingress对象
        String certName = getCertName(devopsIngressVO.getCertId());
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName);

        // 处理创建域名数据
        DevopsIngressDTO devopsIngressDO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CREATE);

        // 在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), false, v1beta1Ingress, true, null, devopsIngressDO, userAttrDTO, devopsEnvCommandDTO, devopsIngressVO.getAppServiceId());
    }


    /**
     * 查询传入的网络是否全是同一个应用下
     *
     * @param appServiceId 应用id
     * @param serviceIds   网络id
     * @return false 如果某个网络不存在和应用的关系或所有网络不在同一个应用下
     */
    private boolean isAllServiceInApp(Long appServiceId, List<Long> serviceIds) {
        return devopsAppResourceMapper.queryResourceIdsInApp(appServiceId, ResourceType.SERVICE.getType(), serviceIds).size() == serviceIds.size();
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
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName);
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

        Boolean deleteCert = false;

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressVO.getEnvId()

        );

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsIngressDTO oldDevopsIngressDTO = baseQuery(id);
        if (oldDevopsIngressDTO.getCertId() != null && devopsIngressVO.getCertId() == null) {
            deleteCert = true;
        }

        // 更新域名的时候校验gitops库文件是否存在,处理部署域名时，由于没有创gitops文件导致的部署失败
        resourceFileCheckHandler.check(devopsEnvironmentDTO, id, devopsIngressVO.getName(), INGRESS);


        // 校验port是否属于该网络
        devopsIngressVO.getPathList().forEach(devopsIngressPathDTO -> {
            DevopsServiceDTO devopsServiceDTO = devopsServiceMapper.selectByPrimaryKey(devopsIngressPathDTO.getServiceId());
            if (dealWithPorts(devopsServiceDTO.getPorts()).stream()
                    .map(PortMapVO::getPort).noneMatch(port -> port.equals(devopsIngressPathDTO.getServicePort()))) {
                throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
            }
        });

        // 校验创建应用下域名时，所选的网络是否都是同一个应用下的
        if (devopsIngressVO.getAppServiceId() != null) {
            List<Long> serviceIds = devopsIngressVO.getPathList().stream().map(DevopsIngressPathVO::getServiceId).collect(Collectors.toList());
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
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName);

        // 处理域名数据
        devopsIngressVO.setId(id);
        DevopsIngressDTO devopsIngressDO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);


        // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());

        //在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), deleteCert, v1beta1Ingress, false, path, devopsIngressDO, userAttrDTO, devopsEnvCommandDTO, null);
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
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName);

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
        return ConvertUtils.convertObject(baseQuery(ingressId), DevopsIngressVO.class);
    }

    @Override
    public DevopsIngressVO queryIngressDetailById(Long projectId, Long ingressId) {
        DevopsIngressDTO devopsIngressDTO = devopsIngressMapper.queryById(ingressId);
        if (devopsIngressDTO == null) {
            return null;
        }

        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();

        DevopsIngressVO vo = new DevopsIngressVO();
        BeanUtils.copyProperties(devopsIngressDTO, vo);
        vo.setInstances(devopsIngressMapper.listInstanceNamesByIngressId(vo.getId()));

        if (!StringUtils.isEmpty(devopsIngressDTO.getMessage())) {
            V1beta1Ingress ingress = json.deserialize(devopsIngressDTO.getMessage(), V1beta1Ingress.class);
            vo.setAnnotations(ingress.getMetadata().getAnnotations());
        }

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
            vo.setCreatorName(ResourceCreatorInfoUtil.getOperatorName(iamServiceClientOperator, devopsIngressDTO.getCreatedBy()));
        }
        if (devopsIngressDTO.getLastUpdatedBy() != null && devopsIngressDTO.getLastUpdatedBy() != 0) {
            vo.setLastUpdaterName(ResourceCreatorInfoUtil.getOperatorName(iamServiceClientOperator, devopsIngressDTO.getLastUpdatedBy()));
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());
        vo.setEnvStatus(updatedEnvList.contains(devopsEnvironmentDTO.getClusterId()));

        return vo;
    }

    @Override

    public PageInfo<DevopsIngressVO> pageByEnv(Long projectId, Long envId, PageRequest pageRequest, String params) {
        PageInfo<DevopsIngressVO> devopsIngressVOPage = basePageByOptions(projectId, envId, null, pageRequest, params);

        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        devopsIngressVOPage.getList().forEach(devopsIngressVO -> {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressVO.getEnvId());
            devopsIngressVO.setEnvStatus(updatedEnvList.contains(devopsEnvironmentDTO.getClusterId()));
        });
        return devopsIngressVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIngress(Long ingressId) {

        DevopsIngressDTO ingressDO = baseQuery(ingressId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(ingressDO.getEnvId()
        );

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
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());

        // 查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), ingressId, INGRESS);
        if (devopsEnvFileResourceDTO == null) {
            baseDelete(ingressId);
            devopsApplicationResourceService.baseDeleteByResourceIdAndType(ingressId, ObjectType.INGRESS.getType());
            baseDeletePathByIngressId(ingressId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    "ing-" + ingressDO.getName() + ".yaml")) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        "ing-" + ingressDO.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
            return;

        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceDTO.getFilePath())) {
                baseDelete(ingressId);
                devopsApplicationResourceService.baseDeleteByResourceIdAndType(ingressId, ObjectType.INGRESS.getType());

                baseDeletePathByIngressId(ingressId);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
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
            ResourceConvertToYamlHandler<V1beta1Ingress> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1beta1Ingress v1beta1Ingress = new V1beta1Ingress();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(ingressDO.getName());
            v1beta1Ingress.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1beta1Ingress);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    null,
                    projectId,
                    DELETE,
                    userAttrDTO.getGitlabUserId(),
                    ingressDO.getId(), INGRESS, null, false, devopsEnvironmentDTO.getId(), path);
        }

    }


    @Override
    public void deleteIngressByGitOps(Long ingressId) {
        DevopsIngressDTO devopsIngressDTO = baseQuery(ingressId);


        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        devopsEnvCommandService.baseListByObject(ObjectType.INGRESS.getType(), ingressId).forEach(devopsEnvCommandDTO -> devopsEnvCommandService.baseDelete(devopsEnvCommandDTO.getId()));
        baseDelete(ingressId);
        devopsApplicationResourceService.baseDeleteByResourceIdAndType(ingressId, ObjectType.INGRESS.getType());
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


    private V1beta1Ingress initV1beta1Ingress(String host, String name, String certName) {
        V1beta1Ingress ingress = new V1beta1Ingress();
        ingress.setKind(INGRESS);
        ingress.setApiVersion("extensions/v1beta1");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        Map<String, String> labels = new HashMap<>();
        labels.put("choerodon.io/network", "ingress");

        metadata.setLabels(labels);
        metadata.setAnnotations(new HashMap<>());
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
                                      Long appServiceId) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsIngressDTO.getEnvId());

        //操作域名数据库
        if (isCreate) {
            Long ingressId = baseCreateIngressAndPath(devopsIngressDTO).getId();
            if (appServiceId != null) {
                DevopsApplicationResourceDTO resourceDTO = new DevopsApplicationResourceDTO();
                resourceDTO.setAppServiceId(appServiceId);
                resourceDTO.setResourceType(ObjectType.INSTANCE.getType());
                resourceDTO.setResourceId(ingressId);
                devopsApplicationResourceService.baseCreate(resourceDTO);
            }
            devopsEnvCommandDTO.setObjectId(ingressId);
            devopsIngressDTO.setId(ingressId);
            devopsIngressDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsIngressDTO);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsIngressDTO.getId());
            devopsIngressDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdateIngressAndIngressPath(devopsIngressDTO);
        }

        ResourceConvertToYamlHandler<V1beta1Ingress> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(ingress);
        resourceConvertToYamlHandler.operationEnvGitlabFile(INGRESS_PREFIX + devopsIngressDTO.getName(), envGitLabProjectId, isCreate ? CREATE : UPDATE,
                userAttrDTO.getGitlabUserId(), devopsIngressDTO.getId(), INGRESS, null, deleteCert, devopsIngressDTO.getEnvId(), path);

        IngressSagaPayload ingressSagaPayload = new IngressSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId());
        ingressSagaPayload.setDevopsIngressDTO(devopsIngressDTO);
        ingressSagaPayload.setCreated(isCreate);
        ingressSagaPayload.setV1beta1Ingress(ingress);
        ingressSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INGRESS),
                builder -> builder
                        .withPayloadAndSerialize(ingressSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));


    }

    @Override
    public void createIngressBySaga(IngressSagaPayload ingressSagaPayload) {
        try {
            //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String filePath = clusterConnectionHandler.handDevopsEnvGitRepository(ingressSagaPayload.getProjectId(), ingressSagaPayload.getDevopsEnvironmentDTO().getCode(), ingressSagaPayload.getDevopsEnvironmentDTO().getEnvIdRsa());

            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<V1beta1Ingress> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(ingressSagaPayload.getV1beta1Ingress());

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    INGRESS_PREFIX + ingressSagaPayload.getDevopsIngressDTO().getName(),
                    ingressSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    ingressSagaPayload.getCreated() ? CREATE : UPDATE,
                    ingressSagaPayload.getGitlabUserId(),
                    ingressSagaPayload.getDevopsIngressDTO().getId(), INGRESS, null, false, ingressSagaPayload.getDevopsEnvironmentDTO().getId(), filePath);
        } catch (Exception e) {
            //有异常更新实例以及command的状态
            DevopsIngressDTO devopsIngressDTO = baseQuery(ingressSagaPayload.getDevopsIngressDTO().getId());
            devopsIngressDTO.setStatus(CommandStatus.FAILED.getStatus());
            baseUpdate(devopsIngressDTO);
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsIngressDTO.getCommandId());
            devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
            devopsEnvCommandDTO.setError("create or update gitOps file failed!");
            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
        }
    }


    private DevopsIngressDTO handlerIngress(DevopsIngressVO devopsIngressVO, Long projectId, V1beta1Ingress v1beta1Ingress) {
        Long envId = devopsIngressVO.getEnvId();
        String ingressName = devopsIngressVO.getName();
        DevopsIngressValidator.checkIngressName(ingressName);
        String domain = devopsIngressVO.getDomain();

        //处理pathlist,生成域名和service的关联对象列表
        List<DevopsIngressPathDTO> devopsIngressPathDTOS = handlerPathList(devopsIngressVO.getPathList(), devopsIngressVO, v1beta1Ingress);

        //初始化ingressDO对象
        DevopsIngressDTO devopsIngressDO = new DevopsIngressDTO(devopsIngressVO.getId(), projectId, envId, domain, ingressName, IngressStatus.OPERATING.getStatus());

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
    public PageInfo<DevopsIngressVO> basePageByOptions(Long projectId, Long envId, Long serviceId, PageRequest pageRequest, String params) {
        List<DevopsIngressVO> devopsIngressVOS = new ArrayList<>();

        Map<String, Object> maps = gson.fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());

        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("envName")) {
                            property = "de.name";
                        } else if (property.equals("path")) {
                            property = "dip.path";
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        PageInfo<DevopsIngressDTO> devopsIngressDTOPageInfo =
                PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(
                        () -> devopsIngressMapper.listIngressByOptions(projectId, envId, serviceId, maps == null ? null : TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), maps == null ? null : TypeUtil.cast(maps.get(TypeUtil.PARAMS))));
        devopsIngressDTOPageInfo.getList().forEach(t -> {
            DevopsIngressVO devopsIngressVO =
                    new DevopsIngressVO(t.getId(), t.getDomain(), t.getName(),
                            t.getEnvId(), t.getUsable(), t.getEnvName());
            devopsIngressVO.setStatus(t.getStatus());
            devopsIngressVO.setCommandStatus(t.getCommandStatus());
            devopsIngressVO.setCommandType(t.getCommandType());
            devopsIngressVO.setError(t.getError());
            setIngressDTOCert(t.getCertId(), devopsIngressVO);
            DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO(t.getId());
            devopsIngressPathMapper.select(devopsIngressPathDTO).forEach(e -> setDevopsIngressDTO(devopsIngressVO, e));
            devopsIngressVOS.add(devopsIngressVO);
        });
        PageInfo<DevopsIngressVO> ingressVOPageInfo = new PageInfo<>();
        BeanUtils.copyProperties(devopsIngressDTOPageInfo, ingressVOPageInfo);
        ingressVOPageInfo.setList(devopsIngressVOS);
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
    public void baseCreatePath(DevopsIngressPathDTO devopsIngressPathDTO) {
        if (devopsIngressPathMapper.insert(devopsIngressPathDTO) != 1) {
            throw new CommonException("error.domainAttr.insert");
        }
    }

    @Override
    public List<DevopsIngressPathDTO> baseListPathByEnvIdAndServiceName(Long envId, String serviceName) {
        return devopsIngressPathMapper.listPathByEnvIdAndServiceName(envId, serviceName);
    }

    @Override
    public List<DevopsIngressPathDTO> baseListPathByEnvIdAndServiceId(Long envId, Long serviceId) {
        return devopsIngressPathMapper.listPathByEnvIdAndServiceId(envId, serviceId);
    }

    @Override
    public List<DevopsIngressPathDTO> baseListPathByIngressId(Long ingressId) {
        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        devopsIngressPathDTO.setIngressId(ingressId);
        return devopsIngressPathMapper.select(devopsIngressPathDTO);
    }

    @Override
    public List<DevopsIngressDTO> baseListByEnvId(Long envId) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setEnvId(envId);
        return devopsIngressMapper.select(devopsIngressDTO);
    }

    @Override
    public void baseUpdateIngressPath(DevopsIngressPathDTO devopsIngressPathDTO) {
        if (devopsIngressPathMapper.updateByPrimaryKey(devopsIngressPathDTO) != 1) {
            throw new CommonException("error.domainAttr.update");
        }
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
        DevopsIngressPathVO devopsIngressPathVO = new DevopsIngressPathVO(
                devopsIngressPathDTO.getPath(), devopsIngressPathDTO.getServiceId(), devopsIngressPathDTO.getServiceName(),
                devopsServiceVO == null ? ServiceStatus.DELETED.getStatus() : devopsServiceVO.getStatus());
        devopsIngressPathVO.setServicePort(devopsIngressPathDTO.getServicePort());
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
}
