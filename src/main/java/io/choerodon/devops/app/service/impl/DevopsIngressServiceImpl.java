package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.JSON;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsIngressDTO;
import io.choerodon.devops.api.validator.DevopsIngressValidator;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.DevopsServiceE;
import io.choerodon.devops.domain.application.factory.DevopsEnvCommandFactory;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.DevopsIngressRepository;
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository;
import io.choerodon.devops.domain.service.IDevopsIngressService;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.IngressStatus;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
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


    private static JSON json = new JSON();


    @Autowired
    private IDevopsIngressService idevopsIngressService;
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

    @Override
    public void addIngress(DevopsIngressDTO devopsIngressDTO, Long projectId) {
        envUtil.checkEnvConnection(devopsIngressDTO.getEnvId(), envListener);
        DevopsIngressValidator.checkAppVersion(devopsIngressDTO.getName());
        String name = devopsIngressDTO.getName();
        String domain = devopsIngressDTO.getDomain();
        Long envId = devopsIngressDTO.getEnvId();
        DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(envId);
        DevopsIngressDO devopsIngressDO = new DevopsIngressDO(projectId, envId, domain, name);
        List<DevopsIngressPathDO> devopsIngressPathDOS = new ArrayList<>();
        V1beta1Ingress ingress = createIngress(domain, name, devopsEnvironmentE.getCode());
        if (devopsIngressDTO.getPathList().isEmpty()) {
            throw new CommonException(PATH_ERROR);
        }
        List<String> pathCheckList = new ArrayList<>();
        devopsIngressDTO.getPathList().forEach(t -> {
            if (t.getPath() == null || t.getServiceId() == null) {
                throw new CommonException(PATH_ERROR);
            }
            DevopsIngressValidator.checkPath(t.getPath());
            if (pathCheckList.contains(t.getPath())) {
                throw new CommonException(PATH_DUPLICATED);
            } else {
                pathCheckList.add(t.getPath());
            }
            DevopsServiceE devopsServiceE = getDevopsService(t.getServiceId());

            devopsIngressPathDOS.add(new DevopsIngressPathDO(
                    devopsIngressDTO.getId(), t.getPath(), devopsServiceE.getId(), devopsServiceE.getName()));
            ingress.getSpec().getRules().get(0).getHttp().addPathsItem(createPath(t.getPath(), t.getServiceId()));
        });
        devopsIngressDO.setStatus(IngressStatus.OPERATING.getStatus());
        devopsIngressRepository.createIngress(devopsIngressDO, devopsIngressPathDOS);
        DevopsEnvCommandE devopsEnvCommandE = DevopsEnvCommandFactory.createDevopsEnvCommandE();
        devopsEnvCommandE.setObject(ObjectType.INGRESS.getType());
        devopsEnvCommandE.setObjectId(devopsIngressDO.getId());
        devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.DOING.getStatus());
        idevopsIngressService.createIngress(json.serialize(ingress),
                name,
                devopsEnvironmentE.getCode(),
                devopsIngressDTO.getEnvId(),
                devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
    }

    @Override
    public void updateIngress(Long id, DevopsIngressDTO devopsIngressDTO, Long projectId) {
        envUtil.checkEnvConnection(devopsIngressDTO.getEnvId(), envListener);
        DevopsIngressValidator.checkAppVersion(devopsIngressDTO.getName());
        DevopsIngressDTO ingressDTO = devopsIngressRepository.getIngress(projectId, id);
        if (!devopsIngressDTO.equals(ingressDTO)) {
            String name = devopsIngressDTO.getName();
            String domain = devopsIngressDTO.getDomain();
            Long domainEnvId = devopsIngressDTO.getEnvId();
            DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(domainEnvId);

            if (devopsIngressDTO.getPathList().isEmpty()) {
                throw new CommonException(PATH_ERROR);
            }
            V1beta1Ingress ingress = createIngress(domain, name, devopsEnvironmentE.getCode());
            DevopsIngressDO devopsIngressDO = new DevopsIngressDO(
                    id, projectId, domainEnvId, domain, name);
            List<DevopsIngressPathDO> devopsIngressPathDOS = new ArrayList<>();
            List<String> pathCheckList = new ArrayList<>();
            devopsIngressDTO.getPathList().forEach(t -> {
                if (t.getPath() == null) {
                    throw new CommonException(PATH_ERROR);
                } else if (t.getServiceId() == null) {
                    throw new CommonException("error.service.id.get");
                }
                DevopsIngressValidator.checkPath(t.getPath());
                if (pathCheckList.contains(t.getPath())) {
                    throw new CommonException(PATH_DUPLICATED);
                } else {
                    pathCheckList.add(t.getPath());
                }

                DevopsServiceE devopsServiceE = getDevopsService(t.getServiceId());

                devopsIngressPathDOS.add(new DevopsIngressPathDO(
                        id, t.getPath(), devopsServiceE.getId(), devopsServiceE.getName()));
                ingress.getSpec().getRules().get(0).getHttp()
                        .addPathsItem(createPath(t.getPath(), t.getServiceId()));
            });
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.INGRESS.getType(), id);
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
            devopsEnvCommandE.setStatus(CommandStatus.DOING.getStatus());
            devopsEnvCommandRepository.update(devopsEnvCommandE);
            if (!ingressDTO.getName().equals(name)) {
                idevopsIngressService.deleteIngress(
                        ingressDTO.getName(),
                        devopsEnvironmentE.getCode(),
                        devopsEnvironmentE.getId(),
                        devopsEnvCommandE.getId());
            }
            devopsIngressDO.setStatus(IngressStatus.OPERATING.getStatus());
            devopsIngressRepository.updateIngress(devopsIngressDO, devopsIngressPathDOS);
            idevopsIngressService.createIngress(json.serialize(ingress),
                    name, devopsEnvironmentE.getCode(), devopsEnvironmentE.getId(), devopsEnvCommandE.getId());

        }
    }

    @Override
    public Page<DevopsIngressDTO> getIngress(Long projectId, PageRequest pageRequest, String params) {
        return listByEnv(projectId, null, pageRequest, params);
    }

    @Override
    public Page<DevopsIngressDTO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String params) {
        Page<DevopsIngressDTO> devopsIngressDTOS = devopsIngressRepository.getIngress(projectId, envId, pageRequest, params);
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
    public DevopsIngressDTO getIngress(Long projectId, Long ingressId) {
        return devopsIngressRepository.getIngress(projectId, ingressId);
    }

    @Override
    public void deleteIngress(Long ingressId) {
        envUtil.checkEnvConnection(devopsIngressRepository.getIngress(ingressId).getEnvId(), envListener);
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INGRESS.getType(), ingressId);
        devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.DOING.getStatus());
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        DevopsIngressDO ingressDO = devopsIngressRepository.getIngress(ingressId);
        DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(ingressDO.getEnvId());
        idevopsIngressService.deleteIngress(
                ingressDO.getName(),
                devopsEnvironmentE.getCode(),
                devopsEnvironmentE.getId(),
                devopsEnvCommandE.getId());
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
    public V1beta1HTTPIngressPath createPath(String hostPath, Long serviceId) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.query(serviceId);
        V1beta1HTTPIngressPath path = new V1beta1HTTPIngressPath();
        V1beta1IngressBackend backend = new V1beta1IngressBackend();
        backend.setServiceName(devopsServiceE.getName().toLowerCase());
        backend.setServicePort(new IntOrString(devopsServiceE.getPorts().get(0).getTargetPort().intValue()));
        path.setBackend(backend);
        path.setPath(hostPath);
        return path;
    }

    @Override
    public V1beta1Ingress createIngress(String host, String name, String namspace) {
        V1beta1Ingress ingress = new V1beta1Ingress();
        ingress.setKind("Ingress");
        ingress.setApiVersion("extensions/v1beta1");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        metadata.setNamespace(namspace);
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
        ingress.setSpec(spec);
        json.serialize(ingress);
        return ingress;
    }

    /**
     * 获取服务
     */
    public DevopsServiceE getDevopsService(Long id) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.query(id);
        if (devopsServiceE == null) {
            throw new CommonException("error.service.select");
        }
        return devopsServiceE;
    }

}
