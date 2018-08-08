package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsServiceDTO;
import io.choerodon.devops.api.dto.DevopsServiceReqDTO;
import io.choerodon.devops.api.validator.DevopsServiceValidator;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.ServiceStatus;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.helper.EnvListener;

/**
 * Created by Zenger on 2018/4/13.
 */
@Service
@Transactional(rollbackFor = RuntimeException.class)
public class DevopsServiceServiceImpl implements DevopsServiceService {

    private static final String SERVICE_LABLE = "choerodon.io/network";
    private static final String SERVICE = "service";
    private Gson gson = new Gson();

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
        devopsServiceByPage.parallelStream().forEach(devopsServiceV -> {
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
    public Boolean insertDevopsService(Long projectId, DevopsServiceReqDTO devopsServiceReqDTO, Boolean isGitOps) {
        devopsServiceReqDTO.addLabel(SERVICE_LABLE, SERVICE);
        envUtil.checkEnvConnection(devopsServiceReqDTO.getEnvId(), envListener);
        DevopsServiceValidator.checkService(devopsServiceReqDTO);
        DevopsEnvironmentE devopsEnvironmentE =
                devopsEnviromentRepository.queryById(devopsServiceReqDTO.getEnvId());
        if (devopsEnvironmentE == null) {
            throw new CommonException("error.env.query");
        }

        if (!devopsServiceRepository.checkName(projectId, devopsEnvironmentE.getId(), devopsServiceReqDTO.getName())) {
            throw new CommonException("error.service.name.exist");
        }
        checkOptions(devopsServiceReqDTO.getEnvId(), devopsServiceReqDTO.getAppId(),
                null, null);

        DevopsServiceE devopsServiceE = new DevopsServiceE();
        BeanUtils.copyProperties(devopsServiceReqDTO, devopsServiceE);
        devopsServiceE.setType(devopsServiceReqDTO.getType() == null ? "ClusterIP" : devopsServiceReqDTO.getType());
        devopsServiceE.setNamespace(devopsEnvironmentE.getCode());
        devopsServiceE.setLabels(gson.toJson(devopsServiceReqDTO.getLabel()));
        devopsServiceE = devopsServiceRepository.insert(devopsServiceE);

        insertOrUpdateService(devopsServiceReqDTO,
                devopsServiceE,
                devopsServiceReqDTO.getEnvId(), isGitOps, true);
        return true;
    }

    @Override
    public Boolean updateDevopsService(Long projectId, Long id,
                                       DevopsServiceReqDTO devopsServiceReqDTO,
                                       Boolean isGitOps) {
        devopsServiceReqDTO.addLabel(SERVICE_LABLE, SERVICE);
        envUtil.checkEnvConnection(devopsServiceReqDTO.getEnvId(), envListener);
        DevopsServiceValidator.checkService(devopsServiceReqDTO);
        DevopsServiceE devopsServiceE = getDevopsServiceE(id);
        if (!devopsServiceE.getEnvId().equals(devopsServiceReqDTO.getEnvId())) {
            throw new CommonException("error.env.notEqual");
        }
        String serviceName = devopsServiceReqDTO.getName();
        if (!devopsServiceE.getName().equals(serviceName)) {
            if (!devopsServiceRepository.checkName(
                    projectId, devopsServiceE.getEnvId(), serviceName)) {
                throw new CommonException("error.service.name.check");
            }
            checkOptions(devopsServiceReqDTO.getEnvId(), devopsServiceReqDTO.getAppId(), null, null);

            updateService(devopsServiceE, devopsServiceReqDTO, true, isGitOps);

            //更新域名
            List<DevopsIngressPathE> devopsIngressPathEList = devopsIngressRepository.selectByEnvIdAndServiceId(
                    devopsServiceE.getEnvId(), devopsServiceE.getId());
            devopsIngressPathEList.forEach((DevopsIngressPathE dd) ->
                    updateIngressPath(dd, serviceName));
        } else {
            List<PortMapE> oldPort = devopsServiceE.getPorts();
            if (devopsServiceE.getAppId().equals(devopsServiceReqDTO.getAppId())) {
                //查询网络对应的实例
                List<DevopsServiceAppInstanceE> devopsServiceInstanceEList =
                        devopsServiceInstanceRepository.selectByServiceId(devopsServiceE.getId());
                Boolean isUpdate = !devopsServiceReqDTO.getAppInstance()
                        .retainAll(devopsServiceInstanceEList.stream()
                                .map(DevopsServiceAppInstanceE::getAppInstanceId)
                                .collect(Collectors.toList()));

                if (!isUpdate && oldPort.retainAll(devopsServiceReqDTO.getPorts())
                        && !isUpdateExternalIp(devopsServiceReqDTO, devopsServiceE)) {
                    throw new CommonException("no change!");
                }
            } else {
                checkOptions(devopsServiceE.getEnvId(), devopsServiceReqDTO.getAppId(), null, null);
            }

            updateService(devopsServiceE,
                    devopsServiceReqDTO,
                    false, isGitOps);

            //更新域名
            if (!oldPort.equals(devopsServiceReqDTO.getPorts())) {
                List<DevopsIngressPathE> devopsIngressPathEList = devopsIngressRepository.selectByEnvIdAndServiceId(
                        devopsServiceE.getEnvId(), devopsServiceE.getId());
                devopsIngressPathEList.forEach(t -> updateIngressPath(t, null));
            }
        }
        return true;
    }

    @Override
    public void deleteDevopsService(Long id, Boolean isGitOps) {
        DevopsServiceE devopsServiceE = getDevopsServiceE(id);
        envUtil.checkEnvConnection(devopsServiceE.getEnvId(), envListener);
        devopsServiceE.setStatus(ServiceStatus.OPERATIING.getStatus());
        devopsServiceRepository.update(devopsServiceE);

        if (!isGitOps) {
            DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(devopsServiceE.getEnvId());
            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                    .queryByEnvIdAndResource(devopsEnvironmentE.getId(), id, "Service");
            gitlabRepository.deleteFile(
                    TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                    devopsEnvFileResourceE.getFilePath(),
                    "DELETE FILE",
                    TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
    }

    /**
     * 获取实例
     *
     * @param devopsServiceReqDTO 网络参数
     * @param serviceId           网络id
     * @return String
     */
    private String updateServiceInstanceAndGetCode(DevopsServiceReqDTO devopsServiceReqDTO,
                                                   Long serviceId) {
        StringBuilder stringBuffer = new StringBuilder();
        List<Long> appInstances = devopsServiceReqDTO.getAppInstance();
        if (appInstances != null) {
            appInstances.forEach(appInstance -> {
                checkOptions(devopsServiceReqDTO.getEnvId(), devopsServiceReqDTO.getAppId(),
                        null, appInstance);
                ApplicationInstanceE applicationInstanceE =
                        applicationInstanceRepository.selectById(appInstance);
                if (applicationInstanceE == null) {
                    throw new CommonException("error.instance.query");
                }
                DevopsServiceAppInstanceE devopsServiceAppInstanceE = new DevopsServiceAppInstanceE();
                devopsServiceAppInstanceE.setServiceId(serviceId);
                devopsServiceAppInstanceE.setAppInstanceId(appInstance);
                devopsServiceAppInstanceE.setCode(applicationInstanceE.getCode());
                devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
                stringBuffer.append(applicationInstanceE.getCode()).append("+");
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
     * @param appVersionId  应用版本id
     * @param appInstanceId 应用实例id
     */
    private void checkOptions(Long envId, Long appId, Long appVersionId, Long appInstanceId) {
        if (applicationInstanceRepository.checkOptions(envId, appId, appVersionId, appInstanceId) == 0) {
            throw new CommonException("error.instances.query");
        }
    }

    /**
     * 获取k8s service的yaml格式
     */
    private V1Service getService(DevopsServiceReqDTO devopsServiceReqDTO, String namespace,
                                 Map<String, String> annotations) {
        V1Service service = new V1Service();
        service.setKind("Service");
        service.setApiVersion("v1");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsServiceReqDTO.getName());
        metadata.setNamespace(namespace);
        metadata.setAnnotations(annotations);
        metadata.setLabels(devopsServiceReqDTO.getLabel());
        service.setMetadata(metadata);

        V1ServiceSpec spec = new V1ServiceSpec();
        spec.setType(devopsServiceReqDTO.getType() == null ? "ClusterIP" : devopsServiceReqDTO.getType());
        spec.setSelector(devopsServiceReqDTO.getLabel());
        final Integer[] serialNumber = {0};
        List<V1ServicePort> ports = devopsServiceReqDTO.getPorts().parallelStream()
                .map(t -> {
                    V1ServicePort v1ServicePort = new V1ServicePort();
                    if (t.getNodePort() != null) {
                        v1ServicePort.setNodePort(t.getNodePort().intValue());
                    }
                    if (t.getPort() != null) {
                        v1ServicePort.setPort(t.getPort().intValue());
                    }
                    if (t.getTargetPort() != null) {
                        v1ServicePort.setTargetPort(new IntOrString(t.getTargetPort().intValue()));
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
     * 更新service
     *
     * @param devopsServiceReqDTO 网络参数
     * @param devopsServiceE      网络实例
     * @param envId               环境Id
     */
    private void insertOrUpdateService(DevopsServiceReqDTO devopsServiceReqDTO,
                                       DevopsServiceE devopsServiceE,
                                       Long envId,
                                       Boolean isGitOps, Boolean isCreate) {
        String serviceInstances = updateServiceInstanceAndGetCode(devopsServiceReqDTO, devopsServiceE.getId());
        Map<String, String> annotations = new HashMap<>();
        if (!serviceInstances.isEmpty()) {
            annotations.put("choerodon.io/network-service-instances", serviceInstances);
        }
        DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(envId);

        V1Service service = getService(
                devopsServiceReqDTO,
                devopsServiceE.getNamespace(),
                annotations);

        DevopsServiceE appDeploy = devopsServiceRepository.query(devopsServiceE.getId());
        appDeploy.setAnnotations(gson.toJson(annotations));
        appDeploy.setObjectVersionNumber(appDeploy.getObjectVersionNumber());
        appDeploy.setStatus(ServiceStatus.OPERATIING.getStatus());
        devopsServiceRepository.update(appDeploy);
        operateEnvGitLabFile(devopsServiceReqDTO.getName(), isGitOps,
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), service, isCreate);
    }

    /**
     * 更新service
     *
     * @param devopsServiceE      网络实例
     * @param devopsServiceReqDTO 网络参数
     * @param isGitOps            是否是 GitOps 操作
     * @param flag                标记
     */
    private void updateService(DevopsServiceE devopsServiceE, DevopsServiceReqDTO devopsServiceReqDTO,
                               Boolean flag, Boolean isGitOps) {
        if (flag) {
            devopsServiceE.setName(devopsServiceReqDTO.getName());
        }
        devopsServiceE.setAppId(devopsServiceReqDTO.getAppId());
        devopsServiceE.setLabels(gson.toJson(devopsServiceReqDTO.getLabel()));
        devopsServiceE.setPorts(devopsServiceReqDTO.getPorts());
        devopsServiceE.setType(devopsServiceReqDTO.getType() == null ? "ClusterIP" : devopsServiceReqDTO.getType());
        devopsServiceE.setExternalIp(devopsServiceReqDTO.getExternalIp());
        devopsServiceRepository.update(devopsServiceE);
        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceEList = devopsServiceInstanceRepository
                .selectByServiceId(devopsServiceE.getId());
        devopsServiceAppInstanceEList.parallelStream()
                .forEach(s -> devopsServiceInstanceRepository.deleteById(s.getId()));
        insertOrUpdateService(devopsServiceReqDTO,
                devopsServiceE,
                devopsServiceReqDTO.getEnvId(), isGitOps, false);
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

    private void updateIngressPath(DevopsIngressPathE devopsIngressPathE, String serviceName) {
        DevopsIngressDO devopsIngressDO = devopsIngressRepository
                .getIngress(devopsIngressPathE.getDevopsIngressE().getId());

        if (serviceName != null) {
            devopsIngressPathE.setServiceName(serviceName);
            devopsIngressRepository.updateIngressPath(devopsIngressPathE);
        }

        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository
                .queryById(devopsIngressDO.getEnvId());
        V1beta1Ingress v1beta1Ingress = devopsIngressService.createIngress(devopsIngressDO.getDomain(),
                devopsIngressDO.getName(), devopsEnvironmentE.getCode());
        List<DevopsIngressPathE> devopsIngressPathEListTemp = devopsIngressRepository
                .selectByIngressId(devopsIngressDO.getId());
        devopsIngressPathEListTemp.forEach(ddTemp ->
                v1beta1Ingress.getSpec().getRules().get(0).getHttp().addPathsItem(
                        devopsIngressService.createPath(ddTemp.getPath(), ddTemp.getServiceId())));
    }

    private void operateEnvGitLabFile(String serviceName,
                                      Boolean gitOps,
                                      Integer gitLabEnvProjectId,
                                      V1Service service, Boolean isCreate) {
        if (!gitOps) {
            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            ObjectOperation<V1Service> objectOperation = new ObjectOperation<>();
            objectOperation.setType(service);
            objectOperation.operationEnvGitlabFile(serviceName, gitLabEnvProjectId, isCreate ? "create" : "update",
                    userAttrE.getGitlabUserId());
        }
    }
}
