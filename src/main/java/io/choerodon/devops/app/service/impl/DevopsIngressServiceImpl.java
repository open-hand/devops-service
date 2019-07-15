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
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsIngressValidator;
import io.choerodon.devops.api.vo.DevopsIngressPathVO;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.dto.DevopsIngressDTO;
import io.choerodon.devops.infra.dto.DevopsIngressPathDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsIngressMapper;
import io.choerodon.devops.infra.mapper.DevopsIngressPathMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 16:01
 * Description:
 */
@Component
public class DevopsIngressServiceImpl implements DevopsIngressService {

    private static final String DOMAIN_NAME_EXIST_ERROR = "error.domain.name.exist";
    public static final String ERROR_DOMAIN_PATH_EXIST = "error.domain.path.exist";
    public static final String INGRESS = "Ingress";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    private static final String PATH_ERROR = "error.path.empty";
    private static final String PATH_DUPLICATED = "error.path.duplicated";
    private static final String ERROR_SERVICE_NOT_CONTAIN_PORT = "error.service.notContain.port";
    private static final String CERT_NOT_ACTIVE = "error.cert.notActive";
    public static final String INGRESS_NOT_EXIST = "ingress.not.exist";
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    private static final Gson gson = new Gson();

    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsAppResourceMapper devopsAppResourceMapper;
    @Autowired
    private DevopsApplicationResourceRepository appResourceRepository;
    @Autowired
    private DevopsIngressMapper devopsIngressMapper;
    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper;

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void addIngress(DevopsIngressVO devopsIngressVO, Long projectId) {

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(devopsIngressDTO.getEnvId()
        );

        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

        //校验port是否属于该网络
        devopsIngressVO.getPathList().forEach(devopsIngressPathDTO -> {
            DevopsServiceE devopsServiceE = devopsServiceRepository.baseQuery(devopsIngressPathDTO.getServiceId());
            if (devopsServiceE.getPorts().stream()
                    .map(PortMapE::getPort).noneMatch(port -> port.equals(devopsIngressPathDTO.getServicePort()))) {
                throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
            }
        });

        // 校验创建应用下域名时，所选的网络是否都是同一个应用下的
        if (devopsIngressDTO.getAppId() != null) {
            List<Long> serviceIds = devopsIngressDTO.getPathList().stream().map(DevopsIngressPathDTO::getServiceId).collect(Collectors.toList());
            if (!isAllServiceInApp(devopsIngressDTO.getAppId(), serviceIds)) {
                throw new CommonException("error.ingress.service.application");
            }
        }

        //初始化V1beta1Ingress对象
        String certName = getCertName(devopsIngressVO.getCertId());
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName);
        //处理创建域名数据
        DevopsIngressDTO devopsIngressDO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);

        DevopsEnvCommandVO devopsEnvCommandE = initDevopsEnvCommandE(CREATE);

        //在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), false, v1beta1Ingress, true, null, devopsIngressDO, userAttrE, devopsEnvCommandE, devopsIngressVO.getAppId());
    }


    /**
     * 查询传入的网络是否全是同一个应用下
     *
     * @param appId      应用id
     * @param serviceIds 网络id
     * @return false 如果某个网络不存在和应用的关系或所有网络不在同一个应用下
     */
    private boolean isAllServiceInApp(Long appId, List<Long> serviceIds) {
        return devopsAppResourceMapper.queryResourceIdsInApp(appId, ResourceType.SERVICE.getType(), serviceIds).size() == serviceIds.size();
    }


    private String getCertName(Long certId) {
        String certName = null;
        if (certId != null && certId != 0) {
            CertificationE certificationE = certificationRepository.baseQueryById(certId);
            if (!CertificationStatus.ACTIVE.getStatus().equals(certificationE.getStatus())) {
                throw new CommonException(CERT_NOT_ACTIVE);
            }
            certName = certificationE.getName();
        }
        return certName;
    }

    @Override
    public void addIngressByGitOps(DevopsIngressVO devopsIngressVO, Long projectId, Long userId) {
        //校验环境是否连接
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(devopsIngressDTO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        //初始化V1beta1Ingress对象
        String certName = getCertName(devopsIngressVO.getCertId());
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName);
        //处理域名数据
        DevopsIngressDTO devopsIngressDO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);

        DevopsEnvCommandVO devopsEnvCommandE = initDevopsEnvCommandE(CREATE);

        //创建域名
        Long ingressId = devopsIngressRepository.baseCreateIngressAndPath(devopsIngressDO).getId();
        devopsEnvCommandE.setObjectId(ingressId);
        devopsEnvCommandE.setCreatedBy(userId);
        devopsIngressDO.setId(ingressId);
        devopsIngressDO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsIngressRepository.baseUpdateIngress(devopsIngressDO);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void updateIngress(Long id, DevopsIngressVO devopsIngressVO, Long projectId) {

        Boolean deleteCert = false;

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(devopsIngressDTO.getEnvId()

        );

        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

        DevopsIngressDTO oldDevopsIngressDTO = devopsIngressRepository.basePageByOptions(id);
        if (oldDevopsIngressDTO.getCertId() != null && devopsIngressVO.getCertId() == null) {
            deleteCert = true;
        }

        //更新域名的时候校验gitops库文件是否存在,处理部署域名时，由于没有创gitops文件导致的部署失败
        resourceFileCheckHandler.check(devopsEnvironmentE, id, devopsIngressVO.getName(), INGRESS);


        //校验port是否属于该网络
        devopsIngressVO.getPathList().forEach(devopsIngressPathDTO -> {
            DevopsServiceE devopsServiceE = devopsServiceRepository.baseQuery(devopsIngressPathDTO.getServiceId());
            if (devopsServiceE.getPorts().stream()
                    .map(PortMapE::getPort).noneMatch(port -> port.equals(devopsIngressPathDTO.getServicePort()))) {
                throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
            }
        });


        // 校验创建应用下域名时，所选的网络是否都是同一个应用下的
        if (devopsIngressDTO.getAppId() != null) {
            List<Long> serviceIds = devopsIngressDTO.getPathList().stream().map(DevopsIngressPathDTO::getServiceId).collect(Collectors.toList());
            if (!isAllServiceInApp(devopsIngressDTO.getAppId(), serviceIds)) {
                throw new CommonException("error.ingress.service.application");
            }
        }

        //判断ingress有没有修改，没有修改直接返回
        DevopsIngressVO ingressDTO = devopsIngressRepository.basePageByOptions(projectId, id);
        if (devopsIngressVO.equals(ingressDTO)) {
            return;
        }

        DevopsEnvCommandVO devopsEnvCommandE = initDevopsEnvCommandE(UPDATE);

        //初始化V1beta1Ingress对象
        String certName = getCertName(devopsIngressVO.getCertId());
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName);

        //处理域名数据
        devopsIngressVO.setId(id);
        DevopsIngressDTO devopsIngressDO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentE.getProjectE().getId(), devopsEnvironmentE.getCode(), devopsEnvironmentE.getEnvIdRsa());

        //在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), deleteCert, v1beta1Ingress, false, path, devopsIngressDO, userAttrE, devopsEnvCommandE,null);
    }

    @Override
    public void updateIngressByGitOps(Long id, DevopsIngressVO devopsIngressVO, Long projectId, Long userId) {
        //校验环境是否连接
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(devopsIngressDTO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        //判断ingress有没有修改，没有修改直接返回
        DevopsIngressVO ingressDTO = devopsIngressRepository.basePageByOptions(projectId, id);
        if (devopsIngressVO.equals(ingressDTO)) {
            return;
        }

        //初始化V1beta1Ingress对象
        String certName = devopsIngressVO.getCertName();
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressVO.getDomain(), devopsIngressVO.getName(), certName);

        //处理域名数据
        devopsIngressVO.setId(id);
        DevopsIngressDTO devopsIngressDO = handlerIngress(devopsIngressVO, projectId, v1beta1Ingress);

        DevopsEnvCommandVO devopsEnvCommandE = initDevopsEnvCommandE(UPDATE);

        //更新域名域名
        devopsEnvCommandE.setObjectId(id);
        devopsEnvCommandE.setCreatedBy(userId);
        devopsIngressDO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsIngressRepository.baseUpdateIngressAndIngressPath(devopsIngressDO);
    }


    @Override
    public DevopsIngressVO getIngress(Long projectId, Long ingressId) {
        return devopsIngressRepository.basePageByOptions(projectId, ingressId);
    }

    @Override

    public PageInfo<DevopsIngressVO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String params) {
        PageInfo<DevopsIngressVO> devopsIngressDTOS = devopsIngressRepository
                .basePageByOptions(projectId, envId, null,  pageRequest, params);

        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        devopsIngressDTOS.getList().forEach(devopsIngressDTO -> {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(devopsIngressDTO.getEnvId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                devopsIngressDTO.setEnvStatus(true);
            }
        });
        return devopsIngressDTOS;
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void deleteIngress(Long ingressId) {

        DevopsIngressDTO ingressDO = devopsIngressRepository.basePageByOptions(ingressId);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(ingressDO.getEnvId()
        );

        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

        DevopsEnvCommandVO devopsEnvCommandE = initDevopsEnvCommandE(DELETE);

        //更新ingress
        devopsEnvCommandE.setObjectId(ingressId);
        DevopsIngressDTO devopsIngressDTO = devopsIngressRepository.basePageByOptions(ingressId);
        devopsIngressDTO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsIngressDTO.setStatus(IngressStatus.OPERATING.getStatus());
        devopsIngressRepository.baseUpdateIngress(devopsIngressDTO);


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentE.getProjectE().getId(), devopsEnvironmentE.getCode(), devopsEnvironmentE.getEnvIdRsa());

        //查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceVO devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentE.getId(), ingressId, INGRESS);
        if (devopsEnvFileResourceE == null) {
            devopsIngressRepository.baseDelete(ingressId);
            appResourceRepository.baseDeleteByResourceIdAndType(ingressId,ObjectType.INGRESS.getType());
            devopsIngressRepository.baseDeletePathByIngressId(ingressId);
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    "ing-" + ingressDO.getName() + ".yaml")) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        "ing-" + ingressDO.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
            return;

        } else {
            if (!gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceE.getFilePath())) {
                devopsIngressRepository.baseDelete(ingressId);
                appResourceRepository.baseDeleteByResourceIdAndType(ingressId,ObjectType.INGRESS.getType());

                devopsIngressRepository.baseDeletePathByIngressId(ingressId);
                devopsEnvFileResourceRepository.deleteFileResource(devopsEnvFileResourceE.getId());
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
            ResourceConvertToYamlHandler<V1beta1Ingress> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1beta1Ingress v1beta1Ingress = new V1beta1Ingress();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(ingressDO.getName());
            v1beta1Ingress.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1beta1Ingress);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    null,
                    projectId,
                    DELETE,
                    userAttrE.getGitlabUserId(),
                    ingressDO.getId(), INGRESS, null, false, devopsEnvironmentE.getId(), path);
        }

    }


    @Override
    public void deleteIngressByGitOps(Long ingressId) {
        DevopsIngressDTO devopsIngressDTO = devopsIngressRepository.basePageByOptions(ingressId);


        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsIngressDTO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        devopsEnvCommandRepository.baseListByObjectAll(ObjectType.INGRESS.getType(), ingressId).forEach(devopsEnvCommandE -> devopsEnvCommandRepository.baseDeleteCommandById(devopsEnvCommandE));
        devopsIngressRepository.baseDelete(ingressId);
        appResourceRepository.baseDeleteByResourceIdAndType(ingressId,ObjectType.INGRESS.getType());
    }


    @Override
    public Boolean checkName(Long envId, String name) {
        return devopsIngressRepository.baseCheckName(envId, name);
    }

    @Override
    public Boolean checkDomainAndPath(Long envId, String domain, String path, Long id) {
        return devopsIngressRepository.baseCheckPath(envId, domain, path, id);
    }

    @Override
    public V1beta1HTTPIngressPath createPath(String hostPath, String serviceName, Long port) {
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


    @Override
    public V1beta1Ingress initV1beta1Ingress(String host, String name, String certName) {
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
                                      UserAttrE userAttrE,
                                      DevopsEnvCommandVO devopsEnvCommandE,
                                      Long appId) {

        //操作域名数据库
        if (isCreate) {
            Long ingressId = devopsIngressRepository.baseCreateIngressAndPath(devopsIngressDTO).getId();
            if (appId != null) {
                DevopsAppResourceE resourceE = new DevopsAppResourceE();
                resourceE.setAppId(appId);
                resourceE.setResourceType(ObjectType.INSTANCE.getType());
                resourceE.setResourceId(ingressId);
                appResourceRepository.baseCreate(resourceE);
            }
            devopsEnvCommandE.setObjectId(ingressId);
            devopsIngressDTO.setId(ingressId);
            devopsIngressDTO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsIngressRepository.baseUpdateIngress(devopsIngressDTO);
        } else {
            devopsEnvCommandE.setObjectId(devopsIngressDTO.getId());
            devopsIngressDTO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsIngressRepository.baseUpdateIngressAndIngressPath(devopsIngressDTO);
        }

        ResourceConvertToYamlHandler<V1beta1Ingress> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(ingress);
        resourceConvertToYamlHandler.operationEnvGitlabFile("ing-" + devopsIngressDTO.getName(), envGitLabProjectId, isCreate ? CREATE : UPDATE,
                userAttrE.getGitlabUserId(), devopsIngressDTO.getId(), INGRESS, null, deleteCert, devopsIngressDTO.getEnvId(), path);

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
        if (devopsIngressPathDTOS.stream()
                .noneMatch(t ->
                        devopsIngressRepository.baseCheckPath(envId, devopsIngressDO.getDomain(),
                                t.getPath(), devopsIngressVO.getId()))) {
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
            DevopsServiceE devopsServiceE = devopsServiceRepository.baseQuery(serviceId);

            devopsIngressPathDTOS.add(new DevopsIngressPathDTO(
                    devopsIngressVO.getId(), hostPath,
                    devopsServiceE == null ? null : devopsServiceE.getId(), devopsServiceE == null ? t.getServiceName() : devopsServiceE.getName(), servicePort));
            v1beta1Ingress.getSpec().getRules().get(0).getHttp().addPathsItem(
                    createPath(hostPath, t.getServiceName(), servicePort));
        });
        return devopsIngressPathDTOS;
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
        devopsEnvCommandE.setObject(ObjectType.INGRESS.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandE;
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

    public void baseUpdateIngress(DevopsIngressDTO devopsIngressDTO) {
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

    public PageInfo<DevopsIngressVO> baseQueryIngressDTO(Long projectId, Long envId, Long serviceId, PageRequest pageRequest, String params) {
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
                PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), sortResult).doSelectPageInfo(
                        () -> devopsIngressMapper.selectIngress(projectId, envId, serviceId, maps == null ? null : TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), maps == null ? null : TypeUtil.cast(maps.get(TypeUtil.PARAM))));
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
            devopsIngressPathMapper.select(devopsIngressPathDTO).forEach(e -> getDevopsIngressDTO(devopsIngressVO, e));
            devopsIngressVOS.add(devopsIngressVO);
        });
        PageInfo<DevopsIngressVO> ingressVOPageInfo = new PageInfo<>();
        BeanUtils.copyProperties(devopsIngressDTOPageInfo, ingressVOPageInfo);
        ingressVOPageInfo.setList(devopsIngressVOS);
        return ingressVOPageInfo;
    }


    public DevopsIngressVO baseQueryIngressVO(Long projectId, Long ingressId) {
        DevopsIngressDTO devopsIngressDO = devopsIngressMapper.selectByPrimaryKey(ingressId);
        if (devopsIngressDO != null) {
            //待修改
//            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentService.queryById(devopsIngressDO.getEnvId());
            DevopsIngressVO devopsIngressVO = new DevopsIngressVO(
                    ingressId, devopsIngressDO.getDomain(), devopsIngressDO.getName(), devopsEnvironmentE.getId(),
                    devopsIngressDO.getUsable(), devopsEnvironmentE.getName());
            DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO(ingressId);
            devopsIngressPathMapper.select(devopsIngressPathDTO).forEach(e -> getDevopsIngressDTO(devopsIngressVO, e));
            devopsIngressVO.setStatus(devopsIngressDO.getStatus());
            setIngressDTOCert(devopsIngressDO.getCertId(), devopsIngressVO);
            return devopsIngressVO;
        }

        return null;
    }

    public DevopsIngressDTO baseQueryIngressDTO(Long ingressId) {
        return devopsIngressMapper.selectByPrimaryKey(ingressId);
    }

    private void setIngressDTOCert(Long certId, DevopsIngressVO devopsIngressVO) {
        if (certId != null) {
            devopsIngressVO.setCertId(certId);
            CertificationE certificationE = certificationRepository.baseQueryById(certId);
            if (certificationE != null) {
                devopsIngressVO.setCertName(certificationE.getName());
                devopsIngressVO.setCertStatus(certificationE.getStatus());
            }
        }
    }

    public void baseDelete(Long ingressId) {
        devopsIngressMapper.deleteByPrimaryKey(ingressId);
        devopsIngressPathMapper.delete(new DevopsIngressPathDTO(ingressId));
    }

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

    public List<String> baseListNameByServiceId(Long serviceId) {
        return devopsIngressMapper.listIngressNameByServiceId(serviceId);
    }

    public Boolean baseCheckName(Long envId, String name) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO(name);
        devopsIngressDTO.setEnvId(envId);
        return devopsIngressMapper.select(devopsIngressDTO).isEmpty();
    }

    public Boolean baseCheckPath(Long envId, String domain, String path, Long id) {
        return !devopsIngressPathMapper.checkDomainAndPath(envId, domain, path, id);
    }

    public DevopsIngressDTO baseCheckByEnvAndName(Long envId, String name) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setEnvId(envId);
        devopsIngressDTO.setName(name);
        return devopsIngressMapper.selectOne(devopsIngressDTO);
    }

    public DevopsIngressDTO baseCreateIngress(DevopsIngressDTO devopsIngressDTO) {
        if (devopsIngressMapper.insert(devopsIngressDTO) != 1) {
            throw new CommonException("error.domain.insert");
        }
        return devopsIngressDTO;
    }

    public void baseCreatePath(DevopsIngressPathDTO devopsIngressPathDTO) {
        if (devopsIngressPathMapper.insert(devopsIngressPathDTO) != 1) {
            throw new CommonException("error.domainAttr.insert");
        }
    }

    public List<DevopsIngressPathDTO> baseListPathByEnvIdAndServiceName(Long envId, String serviceName) {
        return devopsIngressPathMapper.listPathByEnvIdAndServiceName(envId, serviceName);
    }

    public List<DevopsIngressPathDTO> baseListPathByEnvIdAndServiceId(Long envId, Long serviceId) {
        return devopsIngressPathMapper.listPathByEnvIdAndServiceId(envId, serviceId);
    }

    public List<DevopsIngressPathDTO> baseListPathByIngressId(Long ingressId) {
        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        devopsIngressPathDTO.setIngressId(ingressId);
        return devopsIngressPathMapper.select(devopsIngressPathDTO);
    }

    public List<DevopsIngressDTO> baseListByEnvId(Long envId) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setEnvId(envId);
        return devopsIngressMapper.select(devopsIngressDTO);
    }

    public void baseUpdateIngressPath(DevopsIngressPathDTO devopsIngressPathDTO) {
        if (devopsIngressPathMapper.updateByPrimaryKey(devopsIngressPathDTO) != 1) {
            throw new CommonException("error.domainAttr.update");
        }
    }

    public void baseDeletePathByIngressId(Long ingressId) {
        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        devopsIngressPathDTO.setIngressId(ingressId);
        devopsIngressPathMapper.delete(devopsIngressPathDTO);
    }

    public Boolean baseCheckByEnv(Long envId) {
        return devopsIngressMapper.checkEnvHasIngress(envId);
    }

    public List<DevopsIngressDTO> baseList() {
        return devopsIngressMapper.selectAll();
    }

    private void getDevopsIngressDTO(DevopsIngressVO devopsIngressVO, DevopsIngressPathDTO devopsIngressPathDTO) {
        //待修改
//        DevopsServiceE devopsServiceE = devopsServiceRepository.query(devopsIngressPathDTO.getServiceId());
        DevopsIngressPathVO devopsIngressPathVO = new DevopsIngressPathVO(
                devopsIngressPathDTO.getPath(), devopsIngressPathDTO.getServiceId(), devopsIngressPathDTO.getServiceName(),
                devopsServiceE == null ? ServiceStatus.DELETED.getStatus() : devopsServiceE.getStatus());
        devopsIngressPathVO.setServicePort(devopsIngressPathDTO.getServicePort());
        devopsIngressVO.addDevopsIngressPathDTO(devopsIngressPathVO);
    }

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
