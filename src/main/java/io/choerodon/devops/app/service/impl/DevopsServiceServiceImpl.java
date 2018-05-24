package io.choerodon.devops.app.service.impl;

import java.util.*;

import io.kubernetes.client.JSON;
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
import io.choerodon.devops.domain.application.factory.DevopsEnvCommandFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;
import io.choerodon.devops.domain.service.IDevopsIngressService;
import io.choerodon.devops.domain.service.IDevopsServiceService;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.devops.infra.common.util.enums.ServiceStatus;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.session.EnvListener;

/**
 * Created by Zenger on 2018/4/13.
 */
@Service
@Transactional(rollbackFor = RuntimeException.class)
public class DevopsServiceServiceImpl implements DevopsServiceService {

    protected static final JSON json = new JSON();

    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnviromentRepository;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private IDevopsServiceService idevopsServiceService;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsServiceInstanceRepository devopsServiceInstanceRepository;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private IDevopsIngressService idevopsIngressService;
    @Autowired
    private EnvListener envListener;

    @Override
    public Boolean checkName(Long projectId, Long envId, String name) {
        return devopsServiceRepository.checkName(projectId, envId, name);
    }

    @Override
    public Page<DevopsServiceDTO> listDevopsServiceByPage(Long projectId, PageRequest pageRequest, String searchParam) {
        Page<DevopsServiceV> devopsServiceByPage = devopsServiceRepository.listDevopsServiceByPage(
                projectId, pageRequest, searchParam);
        Set<String> namespaces = envListener.connectedEnv();
        for (String ns : namespaces) {
            for (DevopsServiceV ds : devopsServiceByPage) {
                if (ns.equals(ds.getNamespace())) {
                    ds.setEnvStatus(true);
                }
            }
        }

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
        DevopsServiceValidator.checkAppVersion(devopsServiceReqDTO);
        DevopsEnvironmentE devopsEnvironmentE =
                devopsEnviromentRepository.queryById(devopsServiceReqDTO.getEnvId());
        if (devopsEnvironmentE == null) {
            throw new CommonException("error.env.query");
        }

        devopsServiceRepository.checkName(projectId, devopsEnvironmentE.getId(), devopsServiceReqDTO.getName());
        checkOptions(devopsServiceReqDTO.getEnvId(), devopsServiceReqDTO.getAppId(),
                null, null);

        ApplicationE applicationE = getApplicationE(devopsServiceReqDTO.getAppId());
        DevopsServiceE devopsServiceE = new DevopsServiceE();
        BeanUtils.copyProperties(devopsServiceReqDTO, devopsServiceE);
        devopsServiceE.setNamespace(devopsEnvironmentE.getCode());
        devopsServiceE.setTargetPort(devopsServiceReqDTO.getPort());
        devopsServiceE = devopsServiceRepository.insert(devopsServiceE);

        DevopsEnvCommandE devopsEnvCommandE = DevopsEnvCommandFactory.createDevopsEnvCommandE();
        devopsEnvCommandE.setObject(ObjectType.SERVICE.getObjectType());
        devopsEnvCommandE.setObjectId(devopsServiceE.getId());
        devopsEnvCommandE.setCommandType(CommandType.CREATE.getCommandType());
        devopsEnvCommandE.setStatus(CommandStatus.DOING.getCommandStatus());
        devopsEnvCommandRepository.create(devopsEnvCommandE);

        insertOrUpdateService(devopsServiceReqDTO, devopsServiceE,
                applicationE.getCode());
        return true;
    }

    @Override
    public Boolean updateDevopsService(Long projectId, Long id, DevopsServiceReqDTO devopsServiceReqDTO) {
        DevopsServiceValidator.checkAppVersion(devopsServiceReqDTO);
        DevopsServiceE devopsServiceE = getDevopsServiceE(id);
        if (!devopsServiceE.getEnvId().equals(devopsServiceReqDTO.getEnvId())) {
            throw new CommonException("error.env.equal");
        }
        ApplicationE applicationE = getApplicationE(devopsServiceReqDTO.getAppId());

        if (!devopsServiceE.getName().equals(devopsServiceReqDTO.getName())) {
            devopsServiceRepository.checkName(projectId, devopsServiceE.getEnvId(), devopsServiceReqDTO.getName());
            checkOptions(devopsServiceReqDTO.getEnvId(), devopsServiceReqDTO.getAppId(),
                    null, null);
            String oldServiceName = devopsServiceE.getName();

            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.SERVICE.getObjectType(), id);
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getCommandType());
            devopsEnvCommandE.setStatus(CommandStatus.DOING.getCommandStatus());
            devopsEnvCommandRepository.update(devopsEnvCommandE);

            updateService(devopsServiceE, devopsServiceReqDTO, applicationE.getCode(), true);
            idevopsServiceService.delete(oldServiceName, devopsServiceE.getNamespace());

            //更新域名
            List<DevopsIngressPathE> devopsIngressPathEList = devopsIngressRepository.selectByEnvIdAndServiceId(
                    devopsServiceE.getEnvId(), devopsServiceE.getId());
            for (DevopsIngressPathE dd : devopsIngressPathEList) {

                DevopsEnvCommandE newdevopsEnvCommandE = devopsEnvCommandRepository
                        .queryByObject(ObjectType.INGRESS.getObjectType(), dd.getDevopsIngressE().getId());
                newdevopsEnvCommandE.setCommandType(CommandType.CREATE.getCommandType());
                newdevopsEnvCommandE.setStatus(CommandStatus.DOING.getCommandStatus());
                devopsEnvCommandRepository.update(newdevopsEnvCommandE);

                dd.setServiceName(devopsServiceReqDTO.getName());
                devopsIngressRepository.updateIngressPath(dd);
                DevopsIngressDO devopsIngressDO = devopsIngressRepository
                        .getIngress(dd.getDevopsIngressE().getId());
                DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository
                        .queryById(devopsIngressDO.getEnvId());
                V1beta1Ingress v1beta1Ingress = devopsIngressService.createIngress(devopsIngressDO.getDomain(),
                        devopsIngressDO.getName(), devopsEnvironmentE.getCode());
                List<DevopsIngressPathE> devopsIngressPathEListTemp = devopsIngressRepository
                        .selectByIngressId(devopsIngressDO.getId());
                for (DevopsIngressPathE ddTemp : devopsIngressPathEListTemp) {
                    v1beta1Ingress.getSpec().getRules().get(0).getHttp().addPathsItem(
                            devopsIngressService.createPath(ddTemp.getPath(), ddTemp.getServiceId()));
                }
                idevopsIngressService.createIngress(json.serialize(v1beta1Ingress),
                        devopsIngressDO.getName(),
                        devopsEnvironmentE.getCode());
            }
        } else {
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.SERVICE.getObjectType(), id);
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getCommandType());
            devopsEnvCommandE.setStatus(CommandStatus.DOING.getCommandStatus());
            devopsEnvCommandRepository.update(devopsEnvCommandE);
            Long oldPort = devopsServiceE.getPort();
            if (devopsServiceE.getAppId().equals(devopsServiceReqDTO.getAppId())) {
                //查询网络对应的实例
                Boolean isUpdate = false;
                List<DevopsServiceAppInstanceE> devopsServiceInstanceEList;
                int intances = devopsServiceReqDTO.getAppInstance().size();
                final int[] instanceFlag = {0};
                devopsServiceInstanceEList = devopsServiceInstanceRepository.selectByServiceId(devopsServiceE.getId());
                devopsServiceInstanceEList.forEach(p -> {
                    instanceFlag[0] += devopsServiceReqDTO.getAppInstance().stream()
                            .filter(d -> p.getAppInstanceId().equals(d)).count();

                });
                if ((instanceFlag[0] != devopsServiceInstanceEList.size()
                        || (instanceFlag[0] != intances))) {
                    isUpdate = true;
                }


                if (!isUpdate && devopsServiceE.getPort().equals(devopsServiceReqDTO.getPort())
                        && !isUpdateExternalIp(devopsServiceReqDTO, devopsServiceE)) {
                    throw new CommonException("no change!");
                }
            } else {
                checkOptions(devopsServiceE.getEnvId(), devopsServiceReqDTO.getAppId(), null, null);
            }

            updateService(devopsServiceE, devopsServiceReqDTO, applicationE.getCode(), false);

            //更新域名
            if (!oldPort.equals(devopsServiceReqDTO.getPort())) {
                List<DevopsIngressPathE> devopsIngressPathEList = devopsIngressRepository.selectByEnvIdAndServiceId(
                        devopsServiceE.getEnvId(), devopsServiceE.getId());
                for (DevopsIngressPathE dd : devopsIngressPathEList) {
                    DevopsIngressDO devopsIngressDO = devopsIngressRepository
                            .getIngress(dd.getDevopsIngressE().getId());

                    DevopsEnvCommandE newdevopsEnvCommandE = devopsEnvCommandRepository.queryByObject(
                            ObjectType.INGRESS.getObjectType(), devopsIngressDO.getId());
                    newdevopsEnvCommandE.setCommandType(CommandType.CREATE.getCommandType());
                    newdevopsEnvCommandE.setStatus(CommandStatus.DOING.getCommandStatus());
                    devopsEnvCommandRepository.update(newdevopsEnvCommandE);

                    DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository
                            .queryById(devopsIngressDO.getEnvId());
                    V1beta1Ingress v1beta1Ingress = devopsIngressService.createIngress(devopsIngressDO.getDomain(),
                            devopsIngressDO.getName(), devopsEnvironmentE.getCode());
                    List<DevopsIngressPathE> devopsIngressPathEListTemp = devopsIngressRepository
                            .selectByIngressId(devopsIngressDO.getId());
                    for (DevopsIngressPathE ddTemp : devopsIngressPathEListTemp) {
                        v1beta1Ingress.getSpec().getRules().get(0).getHttp().addPathsItem(
                                devopsIngressService.createPath(ddTemp.getPath(), ddTemp.getServiceId()));
                    }
                    idevopsIngressService.createIngress(json.serialize(v1beta1Ingress),
                            devopsIngressDO.getName(),
                            devopsEnvironmentE.getCode());
                }
            }
        }

        return true;
    }

    @Override
    public void deleteDevopsService(Long id) {
        DevopsServiceE devopsServiceE = getDevopsServiceE(id);
        devopsServiceE.setStatus(ServiceStatus.OPERATIING.getStatus());
        devopsServiceRepository.update(devopsServiceE);
        //删除service
        idevopsServiceService.delete(devopsServiceE.getName(), devopsServiceE.getNamespace());

        DevopsEnvCommandE newdevopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.SERVICE.getObjectType(), id);
        newdevopsEnvCommandE.setCommandType(CommandType.DELETE.getCommandType());
        newdevopsEnvCommandE.setStatus(CommandStatus.DOING.getCommandStatus());
        devopsEnvCommandRepository.update(newdevopsEnvCommandE);
    }

    /**
     * 获取实例
     *
     * @param devopsServiceReqDTO 网络参数
     * @param serviceId           网络id
     * @return String
     */
    public String getInstanceCode(DevopsServiceReqDTO devopsServiceReqDTO,
                                  Long serviceId) {
        StringBuilder stringBuffer = new StringBuilder();
        Set<Long> appInstances = devopsServiceReqDTO.getAppInstance();
        if (appInstances.isEmpty()) {
            throw new CommonException("error.param.get");
        } else {
            for (Long appInstance : appInstances) {
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
            }
        }

        return stringBuffer.toString().substring(0, stringBuffer.toString().lastIndexOf('+'));
    }

    /**
     * 校验参数
     *
     * @param envId         环境id
     * @param appId         应用id
     * @param appVersionId  应用版本id
     * @param appInstanceId 应用实例id
     */
    public void checkOptions(Long envId, Long appId, Long appVersionId, Long appInstanceId) {
        if (applicationInstanceRepository.checkOptions(envId, appId, appVersionId, appInstanceId) == 0) {
            throw new CommonException("error.instance.query");
        }
    }

    /**
     * 获取k8s service的yaml格式
     */
    public String getServiceYaml(DevopsServiceReqDTO devopsServiceReqDTO, String namespace,
                                 Map<String, String> labels,
                                 Map<String, String> annotations) {
        V1Service service = new V1Service();
        service.setKind("Service");
        service.setApiVersion("v1");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsServiceReqDTO.getName());
        metadata.setNamespace(namespace);
        metadata.setLabels(labels);
        metadata.setAnnotations(annotations);
        service.setMetadata(metadata);

        V1ServiceSpec spec = new V1ServiceSpec();
        List<V1ServicePort> ports = new ArrayList<>();
        V1ServicePort v1ServicePort = new V1ServicePort();
        v1ServicePort.setName("http");
        v1ServicePort.setPort(devopsServiceReqDTO.getPort().intValue());
        v1ServicePort.setTargetPort(new IntOrString(devopsServiceReqDTO.getPort().intValue()));
        v1ServicePort.setProtocol("TCP");
        ports.add(v1ServicePort);

        if (!StringUtils.isEmpty(devopsServiceReqDTO.getExternalIp())) {
            List<String> externallIps = new ArrayList<>();
            externallIps.add(devopsServiceReqDTO.getExternalIp());
            spec.setExternalIPs(externallIps);
        }

        spec.setPorts(ports);
        spec.setSessionAffinity("None");
        spec.type("ClusterIP");
        service.setSpec(spec);

        return json.serialize(service);
    }

    /**
     * 更新service
     *
     * @param devopsServiceReqDTO 网络参数
     * @param devopsServiceE      网络实例
     * @param appCode             应用code
     */
    public void insertOrUpdateService(DevopsServiceReqDTO devopsServiceReqDTO,
                                      DevopsServiceE devopsServiceE,
                                      String appCode) {
        String serviceInstances = getInstanceCode(devopsServiceReqDTO, devopsServiceE.getId());
        Map<String, String> labels = new HashMap<>();
        Map<String, String> annotations = new HashMap<>();
        labels.put("choerodon.io/application", appCode);
        labels.put("choerodon.io/network", "service");
        annotations.put("choerodon.io/network-service-instances", serviceInstances);

        String serviceYaml = getServiceYaml(
                devopsServiceReqDTO,
                devopsServiceE.getNamespace(),
                labels,
                annotations);

        DevopsServiceE appDeploy = devopsServiceRepository.query(devopsServiceE.getId());
        appDeploy.setLabel(json.serialize(annotations));
        appDeploy.setObjectVersionNumber(appDeploy.getObjectVersionNumber());
        appDeploy.setStatus(ServiceStatus.OPERATIING.getStatus());
        devopsServiceRepository.update(appDeploy);
        idevopsServiceService.deploy(serviceYaml, devopsServiceReqDTO.getName(), appDeploy.getNamespace());
    }

    /**
     * 更新service
     *
     * @param devopsServiceE      网络实例
     * @param devopsServiceReqDTO 网络参数
     * @param appCode             应用code
     * @param flag                标记
     */
    public void updateService(DevopsServiceE devopsServiceE, DevopsServiceReqDTO devopsServiceReqDTO,
                              String appCode, Boolean flag) {
        if (flag) {
            devopsServiceE.setName(devopsServiceReqDTO.getName());
        }
        devopsServiceE.setAppId(devopsServiceReqDTO.getAppId());
        devopsServiceE.setPort(devopsServiceReqDTO.getPort());
        devopsServiceE.setTargetPort(devopsServiceReqDTO.getPort());
        devopsServiceE.setExternalIp(devopsServiceReqDTO.getExternalIp());
        devopsServiceRepository.update(devopsServiceE);
        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceEList = devopsServiceInstanceRepository
                .selectByServiceId(devopsServiceE.getId());
        for (DevopsServiceAppInstanceE s : devopsServiceAppInstanceEList) {
            devopsServiceInstanceRepository.deleteById(s.getId());
        }
        insertOrUpdateService(devopsServiceReqDTO,
                devopsServiceE,
                appCode);
    }

    /**
     * 判断外部ip是否更新
     */
    public Boolean isUpdateExternalIp(DevopsServiceReqDTO devopsServiceReqDTO, DevopsServiceE devopsServiceE) {
        return !((StringUtils.isEmpty(devopsServiceReqDTO.getExternalIp())
                && StringUtils.isEmpty(devopsServiceE.getExternalIp()))
                || (!StringUtils.isEmpty(devopsServiceReqDTO.getExternalIp())
                && !StringUtils.isEmpty(devopsServiceE.getExternalIp())
                && devopsServiceReqDTO.getExternalIp().equals(devopsServiceE.getExternalIp())));
    }

    /**
     * 查询网络信息
     */
    public DevopsServiceE getDevopsServiceE(Long id) {
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
     * @return
     */
    public ApplicationE getApplicationE(long id) {
        ApplicationE applicationE = applicationRepository.query(id);
        if (applicationE == null) {
            throw new CommonException("error.application.query");
        }
        return applicationE;
    }
}
