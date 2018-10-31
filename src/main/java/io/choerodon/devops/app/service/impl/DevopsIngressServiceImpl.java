package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsIngressDTO;
import io.choerodon.devops.api.dto.DevopsIngressPathDTO;
import io.choerodon.devops.api.validator.DevopsIngressValidator;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.*;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import io.choerodon.devops.infra.dataobject.DevopsIngressPathDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.helper.EnvListener;


/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 16:01
 * Description:
 */
@Component
public class DevopsIngressServiceImpl implements DevopsIngressService {
    public static final String ERROR_DOMAIN_PATH_EXIST = "error.domain.path.exist";
    public static final String INGRESS = "Ingress";
    public static final String ERROR_FILE_RESOURCE_NOT_EXIST = "error.fileResource.not.exist";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    private static final String PATH_ERROR = "error.path.empty";
    private static final String PATH_DUPLICATED = "error.path.duplicated";
    private static final String ERROR_SERVICE_NOT_CONTAIN_PORT = "error.service.notContain.port";
    private static final String CERT_NOT_ACTIVE = "error.cert.notActive";
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsEnvironmentRepository environmentRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private EnvListener envListener;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private UserAttrRepository userAttrRepository;
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
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;

    @Override
    public void addIngress(DevopsIngressDTO devopsIngressDTO, Long projectId) {

        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), devopsIngressDTO.getEnvId());

        //校验环境是否连接
        envUtil.checkEnvConnection(devopsIngressDTO.getEnvId(), envListener);

        //初始化V1beta1Ingress对象
        String certName = getCertName(devopsIngressDTO.getCertId());
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressDTO.getDomain(), devopsIngressDTO.getName(), certName);
        //处理创建域名数据
        DevopsIngressDO devopsIngressDO = handlerIngress(devopsIngressDTO, projectId, v1beta1Ingress);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(CREATE);
        DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(devopsIngressDTO.getEnvId());
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        //在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), v1beta1Ingress, true, null, devopsIngressDO, userAttrE, devopsEnvCommandE);
    }

    private String getCertName(Long certId) {
        String certName = null;
        if (certId != null) {
            CertificationE certificationE = certificationRepository.queryById(certId);
            if (!CertificationStatus.ACTIVE.getStatus().equals(certificationE.getStatus())) {
                throw new CommonException(CERT_NOT_ACTIVE);
            }
            certName = certificationE.getName();
        }
        return certName;
    }

    @Override
    public void addIngressByGitOps(DevopsIngressDTO devopsIngressDTO, Long projectId, Long userId) {
        //校验环境是否连接
        envUtil.checkEnvConnection(devopsIngressDTO.getEnvId(), envListener);

        //初始化V1beta1Ingress对象
        String certName = getCertName(devopsIngressDTO.getCertId());
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressDTO.getDomain(), devopsIngressDTO.getName(), certName);
        //处理域名数据
        DevopsIngressDO devopsIngressDO = handlerIngress(devopsIngressDTO, projectId, v1beta1Ingress);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(CREATE);

        //创建域名
        Long ingressId = devopsIngressRepository.createIngress(devopsIngressDO).getId();
        devopsEnvCommandE.setObjectId(ingressId);
        devopsEnvCommandE.setCreatedBy(userId);
        devopsIngressDO.setId(ingressId);
        devopsIngressDO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsIngressRepository.updateIngress(devopsIngressDO);
    }

    @Override
    public void updateIngress(Long id, DevopsIngressDTO devopsIngressDTO, Long projectId) {

        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), devopsIngressDTO.getEnvId());

        //校验环境是否连接
        envUtil.checkEnvConnection(devopsIngressDTO.getEnvId(), envListener);

        //判断ingress有没有修改，没有修改直接返回
        DevopsIngressDTO ingressDTO = devopsIngressRepository.getIngress(projectId, id);
        if (devopsIngressDTO.equals(ingressDTO)) {
            return;
        }

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(UPDATE);

        //初始化V1beta1Ingress对象
        String certName = devopsIngressDTO.getCertName();
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressDTO.getDomain(), devopsIngressDTO.getName(), certName);

        //处理域名数据
        devopsIngressDTO.setId(id);
        DevopsIngressDO devopsIngressDO = handlerIngress(devopsIngressDTO, projectId, v1beta1Ingress);

        DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(devopsIngressDTO.getEnvId());
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        //在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), v1beta1Ingress, false, path, devopsIngressDO, userAttrE, devopsEnvCommandE);
    }

    @Override
    public void updateIngressByGitOps(Long id, DevopsIngressDTO devopsIngressDTO, Long projectId, Long userId) {
        //校验环境是否连接
        envUtil.checkEnvConnection(devopsIngressDTO.getEnvId(), envListener);

        //判断ingress有没有修改，没有修改直接返回
        DevopsIngressDTO ingressDTO = devopsIngressRepository.getIngress(projectId, id);
        if (devopsIngressDTO.equals(ingressDTO)) {
            return;
        }

        //初始化V1beta1Ingress对象
        String certName = devopsIngressDTO.getCertName();
        V1beta1Ingress v1beta1Ingress = initV1beta1Ingress(devopsIngressDTO.getDomain(), devopsIngressDTO.getName(), certName);

        //处理域名数据
        devopsIngressDTO.setId(id);
        DevopsIngressDO devopsIngressDO = handlerIngress(devopsIngressDTO, projectId, v1beta1Ingress);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(UPDATE);

        //更新域名域名
        devopsEnvCommandE.setObjectId(id);
        devopsEnvCommandE.setCreatedBy(userId);
        devopsIngressDO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsIngressRepository.updateIngressAndIngressPath(devopsIngressDO);
    }


    @Override
    public DevopsIngressDTO getIngress(Long projectId, Long ingressId) {
        return devopsIngressRepository.getIngress(projectId, ingressId);
    }

    @Override
    public Page<DevopsIngressDTO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String params) {
        Page<DevopsIngressDTO> devopsIngressDTOS = devopsIngressRepository
                .getIngress(projectId, envId, pageRequest, params);
        List<Long> connectedEnvList = envUtil.getConnectedEnvList(envListener);
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList(envListener);
        devopsIngressDTOS.stream().forEach(devopsIngressDTO -> {
            if (connectedEnvList.contains(devopsIngressDTO.getEnvId())
                    && updatedEnvList.contains(devopsIngressDTO.getEnvId())) {
                devopsIngressDTO.setEnvStatus(true);
            }
        });
        return devopsIngressDTOS;
    }

    @Override
    public void deleteIngress(Long ingressId) {

        DevopsIngressDO ingressDO = devopsIngressRepository.getIngress(ingressId);

        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), ingressDO.getEnvId());

        //校验环境是否连接
        envUtil.checkEnvConnection(ingressDO.getEnvId(), envListener);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(DELETE);

        DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(ingressDO.getEnvId());
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        //更新ingress
        devopsEnvCommandE.setObjectId(ingressId);
        DevopsIngressDO devopsIngressDO = devopsIngressRepository.getIngress(ingressId);
        devopsIngressDO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsIngressDO.setStatus(IngressStatus.OPERATING.getStatus());
        devopsIngressRepository.updateIngress(devopsIngressDO);


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        //查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(devopsEnvironmentE.getId(), ingressId, INGRESS);
        if (devopsEnvFileResourceE == null) {
            throw new CommonException(ERROR_FILE_RESOURCE_NOT_EXIST);
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
            ObjectOperation<V1beta1Ingress> objectOperation = new ObjectOperation<>();
            V1beta1Ingress v1beta1Ingress = new V1beta1Ingress();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(ingressDO.getName());
            v1beta1Ingress.setMetadata(v1ObjectMeta);
            objectOperation.setType(v1beta1Ingress);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(
                    null,
                    projectId,
                    DELETE,
                    userAttrE.getGitlabUserId(),
                    ingressDO.getId(), INGRESS, devopsEnvironmentE.getId(), path);
        }

    }


    @Override
    public void deleteIngressByGitOps(Long ingressId) {
        envUtil.checkEnvConnection(devopsIngressRepository.getIngress(ingressId).getEnvId(), envListener);
        DevopsIngressDO devopsIngressDO = devopsIngressRepository.getIngress(ingressId);

        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsIngressDO.getCommandId());

        devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        devopsIngressRepository.deleteIngress(ingressId);
    }


    @Override
    public Boolean checkName(Long envId, String name) {
        return devopsIngressRepository.checkIngressName(envId, name);
    }

    @Override
    public Boolean checkDomainAndPath(Long id, String domain, String path) {
        return devopsIngressRepository.checkIngressAndPath(id, domain, path);
    }

    @Override
    public V1beta1HTTPIngressPath createPath(String hostPath, Long serviceId, Long port) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.query(serviceId);
        V1beta1HTTPIngressPath path = new V1beta1HTTPIngressPath();
        V1beta1IngressBackend backend = new V1beta1IngressBackend();
        backend.setServiceName(devopsServiceE.getName().toLowerCase());
        Integer servicePort;
        if (port == null) {
            servicePort = devopsServiceE.getPorts().get(0).getPort().intValue();
        } else {
            if (devopsServiceE.getPorts().stream()
                    .map(PortMapE::getPort).anyMatch(t -> t.equals(port))) {
                servicePort = port.intValue();
            } else {
                throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
            }
        }
        backend.setServicePort(new IntOrString(servicePort));
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

    /**
     * 获取服务
     */
    private DevopsServiceE getDevopsService(Long id) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.query(id);
        if (devopsServiceE == null) {
            throw new CommonException("error.service.select");
        }
        return devopsServiceE;
    }

    private void operateEnvGitLabFile(Integer envGitLabProjectId,
                                      V1beta1Ingress ingress,
                                      Boolean isCreate,
                                      String path,
                                      DevopsIngressDO devopsIngressDO,
                                      UserAttrE userAttrE, DevopsEnvCommandE devopsEnvCommandE) {

        //创建或更新数据
        if (isCreate) {
            Long ingressId = devopsIngressRepository.createIngress(devopsIngressDO).getId();
            devopsEnvCommandE.setObjectId(ingressId);
            devopsIngressDO.setId(ingressId);
            devopsIngressDO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsIngressRepository.updateIngress(devopsIngressDO);
        } else {
            devopsEnvCommandE.setObjectId(devopsIngressDO.getId());
            devopsIngressDO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsIngressRepository.updateIngressAndIngressPath(devopsIngressDO);
        }

        ObjectOperation<V1beta1Ingress> objectOperation = new ObjectOperation<>();
        objectOperation.setType(ingress);
        objectOperation.operationEnvGitlabFile("ing-" + devopsIngressDO.getName(), envGitLabProjectId, isCreate ? CREATE : UPDATE,
                userAttrE.getGitlabUserId(), devopsIngressDO.getId(), INGRESS, devopsIngressDO.getEnvId(), path);

    }


    private DevopsIngressDO handlerIngress(DevopsIngressDTO devopsIngressDTO, Long projectId, V1beta1Ingress v1beta1Ingress) {
        Long envId = devopsIngressDTO.getEnvId();
        String ingressName = devopsIngressDTO.getName();
        DevopsIngressValidator.checkIngressName(ingressName);
        String domain = devopsIngressDTO.getDomain();

        //处理pathlist,生成域名和service的关联对象列表
        List<DevopsIngressPathDO> devopsIngressPathDOS = handlerPathList(devopsIngressDTO.getPathList(), devopsIngressDTO, v1beta1Ingress);

        //初始化ingressDO对象
        DevopsIngressDO devopsIngressDO = new DevopsIngressDO(devopsIngressDTO.getId(), projectId, envId, domain, ingressName, IngressStatus.OPERATING.getStatus());

        //校验域名的domain和path是否在数据库中已存在
        if (devopsIngressPathDOS.stream()
                .noneMatch(t ->
                        devopsIngressRepository.checkIngressAndPath(devopsIngressDTO.getId(), devopsIngressDO.getDomain(), t.getPath()))) {
            throw new CommonException(ERROR_DOMAIN_PATH_EXIST);
        }
        devopsIngressDO.setDevopsIngressPathDOS(devopsIngressPathDOS);
        devopsIngressDO.setCertId(devopsIngressDTO.getCertId());
        return devopsIngressDO;
    }


    private List<DevopsIngressPathDO> handlerPathList(List<DevopsIngressPathDTO> pathList, DevopsIngressDTO devopsIngressDTO, V1beta1Ingress v1beta1Ingress) {
        if (pathList == null || pathList.isEmpty()) {
            throw new CommonException(PATH_ERROR);
        }
        List<DevopsIngressPathDO> devopsIngressPathDOS = new ArrayList<>();
        List<String> pathCheckList = new ArrayList<>();
        pathList.forEach(t -> {
            Long serviceId = t.getServiceId();
            Long servicePort = t.getServicePort();
            String hostPath = t.getPath();

            if (hostPath == null || serviceId == null) {
                throw new CommonException(PATH_ERROR);
            }
            DevopsIngressValidator.checkPath(hostPath);
            if (pathCheckList.contains(hostPath)) {
                throw new CommonException(PATH_DUPLICATED);
            } else {
                pathCheckList.add(hostPath);
            }
            DevopsServiceE devopsServiceE = getDevopsService(serviceId);

            if (devopsServiceE.getPorts().stream()
                    .map(PortMapE::getPort).noneMatch(port -> port.equals(servicePort))) {
                throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
            }

            devopsIngressPathDOS.add(new DevopsIngressPathDO(
                    devopsIngressDTO.getId(), hostPath,
                    devopsServiceE.getId(), devopsServiceE.getName(), servicePort));
            v1beta1Ingress.getSpec().getRules().get(0).getHttp().addPathsItem(
                    createPath(hostPath, serviceId, servicePort));
        });
        return devopsIngressPathDOS;
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
        devopsEnvCommandE.setObject(ObjectType.INGRESS.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandE;
    }
}
