package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.JSON;
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
import io.choerodon.devops.infra.common.util.enums.CertificationStatus;
import io.choerodon.devops.infra.common.util.enums.IngressStatus;
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
    private static final String PATH_ERROR = "error.path.empty";
    private static final String PATH_DUPLICATED = "error.path.duplicated";
    private static final String ERROR_SERVICE_NOT_CONTAIN_PORT = "error.service.notContain.port";
    private static final String CERT_NOT_ACTIVE = "error.cert.notActive";
    private static JSON json = new JSON();

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

    @Override
    public void addIngress(DevopsIngressDTO devopsIngressDTO, Long projectId, boolean gitOps) {
        Long envId = devopsIngressDTO.getEnvId();
        envUtil.checkEnvConnection(envId, envListener);
        String ingressName = devopsIngressDTO.getName();
        DevopsIngressValidator.checkIngressName(ingressName);
        String domain = devopsIngressDTO.getDomain();

        Long certId = devopsIngressDTO.getCertId();
        String certName = getCertName(certId);

        List<DevopsIngressPathDTO> pathList = devopsIngressDTO.getPathList();
        if (pathList == null || pathList.isEmpty()) {
            throw new CommonException(PATH_ERROR);
        }

        V1beta1Ingress ingress = createIngress(domain, ingressName, certName);
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

            if (devopsServiceE.getPorts().parallelStream()
                    .map(PortMapE::getPort).noneMatch(port -> port.equals(servicePort))) {
                throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
            }

            devopsIngressPathDOS.add(new DevopsIngressPathDO(
                    devopsIngressDTO.getId(), hostPath,
                    devopsServiceE.getId(), devopsServiceE.getName(), servicePort));
            ingress.getSpec().getRules().get(0).getHttp().addPathsItem(
                    createPath(hostPath, serviceId, servicePort));
        });

        DevopsIngressDO devopsIngressDO = new DevopsIngressDO(projectId, envId, domain, ingressName);
        devopsIngressDO.setStatus(IngressStatus.OPERATING.getStatus());
        devopsIngressDO.setCertId(certId);
        if (!devopsIngressPathDOS.stream()
                .allMatch(t ->
                        devopsIngressRepository.checkIngressAndPath(null, devopsIngressDO.getDomain(), t.getPath()))) {
            throw new CommonException("error.domain.path.exist");
        }
        if (gitOps) {
            devopsIngressRepository.createIngress(devopsIngressDO, devopsIngressPathDOS);
        } else {
            DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(envId);
            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);
            String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);
            operateEnvGitLabFile(ingressName,
                    TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), ingress, true, null,
                    devopsEnvironmentE.getId(), path, devopsIngressDO, devopsIngressPathDOS, userAttrE);
        }
    }

    private String getCertName(Long certId) {
        String certName = null;
        if (certId != null) {
            CertificationE certificationE = certificationRepository.queryById(certId);
            if (!certificationE.getStatus().equals(CertificationStatus.ACTIVE.getStatus())) {
                throw new CommonException(CERT_NOT_ACTIVE);
            }
            certName = certificationE.getName();
        }
        return certName;
    }

    @Override
    public void updateIngress(Long id, DevopsIngressDTO devopsIngressDTO, Long projectId, boolean gitOps) {
        Long domainEnvId = devopsIngressDTO.getEnvId();
        envUtil.checkEnvConnection(domainEnvId, envListener);
        String name = devopsIngressDTO.getName();
        DevopsIngressValidator.checkIngressName(name);

        Long certId = devopsIngressDTO.getCertId();
        String certName = getCertName(certId);

        DevopsIngressDTO ingressDTO = devopsIngressRepository.getIngress(projectId, id);
        if (!devopsIngressDTO.equals(ingressDTO)) {

            List<DevopsIngressPathDTO> pathList = devopsIngressDTO.getPathList();
            if (pathList == null || pathList.isEmpty()) {
                throw new CommonException(PATH_ERROR);
            }
            String domain = devopsIngressDTO.getDomain();
            V1beta1Ingress ingress = createIngress(domain, name, certName);
            List<DevopsIngressPathDO> devopsIngressPathDOS = new ArrayList<>();
            List<String> pathCheckList = new ArrayList<>();
            pathList.forEach(t -> {
                Long servicePort = t.getServicePort();
                Long serviceId = t.getServiceId();
                String path = t.getPath();

                if (path == null) {
                    throw new CommonException(PATH_ERROR);
                } else if (serviceId == null) {
                    throw new CommonException("error.service.id.get");
                }
                DevopsIngressValidator.checkPath(path);
                if (pathCheckList.contains(path)) {
                    throw new CommonException(PATH_DUPLICATED);
                } else {
                    pathCheckList.add(path);
                }

                DevopsServiceE devopsServiceE = getDevopsService(serviceId);

                if (devopsServiceE.getPorts().parallelStream()
                        .map(PortMapE::getPort).noneMatch(port -> port.equals(servicePort))) {
                    throw new CommonException(ERROR_SERVICE_NOT_CONTAIN_PORT);
                }
                DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO(
                        id, path, devopsServiceE.getId(), devopsServiceE.getName(), servicePort);
                devopsIngressPathDOS.add(devopsIngressPathDO);
                ingress.getSpec().getRules().get(0).getHttp()
                        .addPathsItem(createPath(path, serviceId, servicePort));
            });

            DevopsIngressDO devopsIngressDO = new DevopsIngressDO(
                    id, projectId, domainEnvId, domain, name);
            devopsIngressDO.setCertId(certId);
            devopsIngressDO.setStatus(IngressStatus.OPERATING.getStatus());
            if (!devopsIngressPathDOS.stream()
                    .allMatch(t -> (t.getId() != null && id.equals(t.getId()))
                            || devopsIngressRepository.checkIngressAndPath(
                                    devopsIngressDO.getId(), devopsIngressDO.getDomain(), t.getPath()))) {
                throw new CommonException("error.domain.path.exist");
            }
            if (gitOps) {
                devopsIngressRepository.updateIngress(devopsIngressDO, devopsIngressPathDOS);
            } else {
                DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(domainEnvId);
                UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);
                String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);
                operateEnvGitLabFile(name,
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), ingress, false, id,
                        devopsEnvironmentE.getId(), path, devopsIngressDO, devopsIngressPathDOS, userAttrE);
            }
        }

    }

    @Override
    public Page<DevopsIngressDTO> getIngress(Long projectId, PageRequest pageRequest, String params) {
        return listByEnv(projectId, null, pageRequest, params);
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
        devopsIngressDTOS.parallelStream().forEach(devopsIngressDTO -> {
            if (connectedEnvList.contains(devopsIngressDTO.getEnvId())
                    && updatedEnvList.contains(devopsIngressDTO.getEnvId())) {
                devopsIngressDTO.setEnvStatus(true);
            }
        });
        return devopsIngressDTOS;
    }

    @Override
    public void deleteIngress(Long ingressId, boolean gitOps) {
        envUtil.checkEnvConnection(devopsIngressRepository.getIngress(ingressId).getEnvId(), envListener);
        DevopsIngressDO ingressDO = devopsIngressRepository.getIngress(ingressId);
        DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(ingressDO.getEnvId());
        if (!gitOps) {
            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);
            String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                    .queryByEnvIdAndResource(devopsEnvironmentE.getId(), ingressId, "Ingress");
            if (devopsEnvFileResourceE == null) {
                throw new CommonException("error.fileResource.not.exist");
            }
            List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository.queryByEnvIdAndPath(devopsEnvironmentE.getId(), devopsEnvFileResourceE.getFilePath());
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
                        "delete",
                        userAttrE.getGitlabUserId(),
                        ingressDO.getId(), "Ingress", devopsEnvironmentE.getId(), path);
            }
            ingressDO.setStatus(IngressStatus.OPERATING.getStatus());
            devopsIngressRepository.updateIngress(ingressDO);
        }
        if (gitOps) {
            devopsIngressRepository.deleteIngress(ingressId);
        }
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
            if (devopsServiceE.getPorts().parallelStream()
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
    public V1beta1Ingress createIngress(String host, String name, String certName) {
        V1beta1Ingress ingress = new V1beta1Ingress();
        ingress.setKind("Ingress");
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
        json.serialize(ingress);
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

    private void operateEnvGitLabFile(String ingressName,
                                      Integer envGitLabProjectId,
                                      V1beta1Ingress ingress,
                                      Boolean isCreate,
                                      Long objectId,
                                      Long envId,
                                      String path,
                                      DevopsIngressDO devopsIngressDO,
                                      List<DevopsIngressPathDO> devopsIngressPathDOS,
                                      UserAttrE userAttrE) {
        ObjectOperation<V1beta1Ingress> objectOperation = new ObjectOperation<>();
        objectOperation.setType(ingress);
        objectOperation.operationEnvGitlabFile("ing-" + ingressName, envGitLabProjectId, isCreate ? "create" : "update",
                userAttrE.getGitlabUserId(), objectId, "Ingress", envId, path);
        if (isCreate) {
            devopsIngressRepository.createIngress(devopsIngressDO, devopsIngressPathDOS);
        } else {
            devopsIngressRepository.updateIngress(devopsIngressDO, devopsIngressPathDOS);
        }
    }

}
