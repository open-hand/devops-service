package io.choerodon.devops.domain.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.V1beta1HTTPIngressPath;
import io.kubernetes.client.models.V1beta1Ingress;
import io.kubernetes.client.models.V1beta1IngressBackend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsIngressDTO;
import io.choerodon.devops.api.dto.DevopsIngressPathDTO;
import io.choerodon.devops.api.validator.DevopsIngressValidator;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;

@Service
public class HandlerIngressRelationsServiceImpl implements HandlerObjectFileRelationsService<V1beta1Ingress> {


    public static final String INGRESS = "Ingress";
    private static final String GIT_SUFFIX = "/.git";
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;


    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<V1beta1Ingress> v1beta1Ingresses, Long envId, Long projectId, String path) {
        List<String> beforeIngress = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(INGRESS))
                .map(devopsEnvFileResourceE -> {
                    DevopsIngressDO devopsIngressDO = devopsIngressRepository
                            .getIngress(devopsEnvFileResourceE.getResourceId());
                    if (devopsIngressDO == null) {
                        throw new CommonException("the ingress in the file is not exist in devops database");
                    }
                    return devopsIngressDO.getName();
                }).collect(Collectors.toList());
        //比较已存在域名和新增要处理的域名,获取新增域名，更新域名，删除域名
        List<V1beta1Ingress> addV1beta1Ingress = new ArrayList<>();
        List<V1beta1Ingress> updateV1beta1Ingress = new ArrayList<>();
        v1beta1Ingresses.parallelStream().forEach(v1beta1Ingress -> {
            if (beforeIngress.contains(v1beta1Ingress.getMetadata().getName())) {
                updateV1beta1Ingress.add(v1beta1Ingress);
                beforeIngress.remove(v1beta1Ingress.getMetadata().getName());
            } else {
                addV1beta1Ingress.add(v1beta1Ingress);
            }
        });
        //删除ingress,删除文件对象关联关系
        beforeIngress.stream().forEach(ingressName -> {
            DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, ingressName);
            if (devopsIngressE != null) {
                DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
                devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
                devopsEnvCommandE.setObject(ObjectType.INGRESS.getType());
                devopsEnvCommandE.setStatus(CommandStatus.DOING.getStatus());
                devopsEnvCommandE.setObjectId(devopsIngressE.getId());
                devopsEnvCommandRepository.create(devopsEnvCommandE);
                devopsIngressService.deleteIngressByGitOps(devopsIngressE.getId());

            }
            devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsIngressE.getId(), INGRESS);
        });
        //新增ingress
        addIngress(objectPath, envId, projectId, addV1beta1Ingress, path);
        //更新ingress
        updateIngress(objectPath, envId, projectId, updateV1beta1Ingress, path);
    }


    private void addIngress(Map<String, String> objectPath, Long envId, Long projectId, List<V1beta1Ingress> addV1beta1Ingress, String path) {
        addV1beta1Ingress.stream()
                .forEach(v1beta1Ingress -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode()));

                        checkIngressAppVersion(v1beta1Ingress);
                        DevopsIngressE devopsIngressE = devopsIngressRepository
                                .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                        DevopsIngressDTO devopsIngressDTO;
                        //初始化ingress对象参数,存在ingress则直接创建文件对象关联关系
                        if (devopsIngressE == null) {
                            devopsIngressDTO = getDevopsIngressDTO(
                                    v1beta1Ingress,
                                    envId);
                            if (!devopsIngressDTO.getPathList().stream()
                                    .allMatch(t ->
                                            devopsIngressRepository.checkIngressAndPath(null, devopsIngressDTO.getDomain(), t.getPath()))) {
                                throw new CommonException(GitOpsObjectError.INGRESS_DOMAIN_PATH_IS_EXIST.getError());
                            }
                            devopsIngressService.addIngressByGitOps(devopsIngressDTO, projectId);
                            devopsIngressE = devopsIngressRepository
                                    .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                        }
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsIngressE.getCommandId());
                        if (devopsEnvCommandE == null) {
                            devopsEnvCommandE = createDevopsEnvCommandE("create");
                            devopsEnvCommandE.setObjectId(devopsIngressE.getId());
                            DevopsIngressDO devopsIngressDO = devopsIngressRepository.getIngress(devopsIngressE.getId());
                            devopsIngressDO.setCommandId(devopsEnvCommandE.getId());
                            devopsIngressRepository.updateIngress(devopsIngressDO);
                        }
                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);
                        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                        devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                        devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())));
                        devopsEnvFileResourceE.setResourceId(devopsIngressE.getId());
                        devopsEnvFileResourceE.setResourceType(v1beta1Ingress.getKind());
                        devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
                    } catch (CommonException e) {
                        throw new GitOpsExplainException(e.getMessage(), filePath, e);
                    }
                });
    }

    private void updateIngress(Map<String, String> objectPath, Long envId, Long projectId, List<V1beta1Ingress> updateV1beta1Ingress, String path) {
        updateV1beta1Ingress.stream()
                .forEach(v1beta1Ingress -> {
                    String filePath = "";
                    try {
                        Boolean isNotChange = false;
                        filePath = objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode()));
                        DevopsIngressE devopsIngressE = devopsIngressRepository
                                .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                        checkIngressAppVersion(v1beta1Ingress);
                        //初始化ingress对象参数,更新ingress并更新文件对象关联关系
                        DevopsIngressDTO devopsIngressDTO = getDevopsIngressDTO(
                                v1beta1Ingress,
                                envId);
                        DevopsIngressDTO ingressDTO = devopsIngressRepository.getIngress(projectId, devopsIngressE.getId());
                        if (devopsIngressDTO.equals(ingressDTO)) {
                            isNotChange = true;
                        }
                        if (!devopsIngressDTO.getPathList().stream()
                                .allMatch(t ->
                                        devopsIngressRepository.checkIngressAndPath(devopsIngressE.getId(), devopsIngressDTO.getDomain(), t.getPath()))) {
                            throw new CommonException(GitOpsObjectError.INGRESS_DOMAIN_PATH_IS_EXIST.getError());
                        }
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsIngressE.getCommandId());
                        if (devopsEnvCommandE == null) {
                            devopsEnvCommandE = createDevopsEnvCommandE("create");
                            devopsEnvCommandE.setObjectId(devopsIngressE.getId());
                            DevopsIngressDO devopsIngressDO = devopsIngressRepository.getIngress(devopsIngressE.getId());
                            devopsIngressDO.setCommandId(devopsEnvCommandE.getId());
                            devopsIngressRepository.updateIngress(devopsIngressDO);
                        }
                        if (!isNotChange) {
                            devopsIngressService.updateIngressByGitOps(devopsIngressE.getId(), devopsIngressDTO, projectId);
                            DevopsIngressE newdevopsIngressE = devopsIngressRepository
                                    .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                            devopsEnvCommandE = devopsEnvCommandRepository.query(newdevopsIngressE.getCommandId());

                        }
                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);
                        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                                .queryByEnvIdAndResource(envId, devopsIngressE.getId(), v1beta1Ingress.getKind());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceE,
                                v1beta1Ingress.hashCode(), devopsIngressE.getId(), v1beta1Ingress.getKind());

                    } catch (CommonException e) {
                        throw new GitOpsExplainException(e.getMessage(), filePath, e);
                    }
                });
    }


    private void checkIngressAppVersion(
            V1beta1Ingress v1beta1Ingress) {
        try {
            DevopsIngressValidator.checkIngressName(v1beta1Ingress.getMetadata().getName());
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }


    private DevopsIngressDTO getDevopsIngressDTO(V1beta1Ingress v1beta1Ingress,
                                                 Long envId) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setDomain(v1beta1Ingress.getSpec().getRules().get(0).getHost()
        );
        devopsIngressDTO.setName(v1beta1Ingress.getMetadata().getName());
        devopsIngressDTO.setEnvId(envId);
        List<String> pathCheckList = new ArrayList<>();
        List<DevopsIngressPathDTO> devopsIngressPathDTOS = new ArrayList<>();
        List<V1beta1HTTPIngressPath> paths = v1beta1Ingress.getSpec().getRules().get(0).getHttp().getPaths();
        if (paths == null) {
            throw new CommonException(GitOpsObjectError.INGRESS_PATH_IS_EMPTY.getError());
        }
        for (V1beta1HTTPIngressPath v1beta1HTTPIngressPath : paths) {
            String path = v1beta1HTTPIngressPath.getPath();
            try {
                DevopsIngressValidator.checkPath(path);
                if (pathCheckList.contains(path)) {
                    throw new CommonException(GitOpsObjectError.INGRESS_PATH_DUPLICATED.getError());
                } else {
                    pathCheckList.add(path);
                }
            } catch (Exception e) {
                throw new CommonException(e.getMessage());
            }
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
            V1beta1IngressBackend backend = v1beta1HTTPIngressPath.getBackend();
            String serviceName = backend.getServiceName();
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndNamespace(
                    serviceName, devopsEnvironmentE.getCode());
            if (devopsServiceE == null) {
                throw new CommonException(GitOpsObjectError.SERVICE_RELATED_INGRESS_NOT_FOUND.getError() + serviceName);
            }
            Long servicePort;
            IntOrString backendServicePort = backend.getServicePort();
            if (backendServicePort.isInteger()) {
                servicePort = backendServicePort.getIntValue().longValue();
                if (devopsServiceE.getPorts().parallelStream()
                        .map(PortMapE::getPort).noneMatch(t -> t.equals(servicePort))) {
                    throw new CommonException(GitOpsObjectError.INGRESS_PATH_PORT_NOT_BELONG_TO_SERVICE.getError(),
                            serviceName, servicePort);
                }
            } else {
                servicePort = devopsServiceE.getPorts().get(0).getPort();
            }
            DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
            devopsIngressPathDTO.setPath(path);
            devopsIngressPathDTO.setServicePort(servicePort);
            devopsIngressPathDTO.setServiceId(devopsServiceE.getId());
            devopsIngressPathDTOS.add(devopsIngressPathDTO);
        }
        devopsIngressDTO.setPathList(devopsIngressPathDTOS);
        return devopsIngressDTO;
    }


    private DevopsEnvCommandE createDevopsEnvCommandE(String type) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        if (type.equals("create")) {
            devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
        } else {
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        }
        devopsEnvCommandE.setObject(ObjectType.INGRESS.getType());
        devopsEnvCommandE.setStatus(CommandStatus.DOING.getStatus());
        return devopsEnvCommandRepository.create(devopsEnvCommandE);
    }

}
