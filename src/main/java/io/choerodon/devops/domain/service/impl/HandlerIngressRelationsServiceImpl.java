package io.choerodon.devops.domain.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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

    private static final String CREATE = "create";
    public static final String INGRESS = "Ingress";
    private static final String GIT_SUFFIX = "/.git";
    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
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
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<V1beta1Ingress> v1beta1Ingresses, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeIngress = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(INGRESS))
                .map(devopsEnvFileResourceE -> {
                    DevopsIngressDO devopsIngressDO = devopsIngressRepository
                            .getIngress(devopsEnvFileResourceE.getResourceId());
                    if (devopsIngressDO == null) {
                        devopsEnvFileResourceRepository
                                .deleteByEnvIdAndResource(envId, devopsEnvFileResourceE.getResourceId(), INGRESS);
                        return null;
                    }
                    return devopsIngressDO.getName();
                }).collect(Collectors.toList());
        //比较已存在域名和新增要处理的域名,获取新增域名，更新域名，删除域名
        List<V1beta1Ingress> addV1beta1Ingress = new ArrayList<>();
        List<V1beta1Ingress> updateV1beta1Ingress = new ArrayList<>();
        v1beta1Ingresses.stream().forEach(v1beta1Ingress -> {
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
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsIngressE.getCommandId());
            if (devopsEnvCommandE == null || (!devopsEnvCommandE.getCommandType().equals(CommandType.DELETE.getType()))) {
                DevopsEnvCommandE devopsEnvCommandE1 = new DevopsEnvCommandE();
                devopsEnvCommandE1.setCommandType(CommandType.DELETE.getType());
                devopsEnvCommandE1.setObject(ObjectType.INGRESS.getType());
                devopsEnvCommandE1.setCreatedBy(userId);
                devopsEnvCommandE1.setStatus(CommandStatus.OPERATING.getStatus());
                devopsEnvCommandE1.setObjectId(devopsIngressE.getId());
                DevopsIngressDO devopsIngressDO = devopsIngressRepository.getIngress(devopsIngressE.getId());
                devopsIngressDO.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE1).getId());
                devopsIngressRepository.updateIngress(devopsIngressDO);
            }
            devopsIngressService.deleteIngressByGitOps(devopsIngressE.getId());
            devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsIngressE.getId(), INGRESS);
        });
        //新增ingress
        addIngress(objectPath, envId, projectId, addV1beta1Ingress, path, userId);
        //更新ingress
        updateIngress(objectPath, envId, projectId, updateV1beta1Ingress, path, userId);
    }

    private void addIngress(Map<String, String> objectPath, Long envId, Long projectId, List<V1beta1Ingress> addV1beta1Ingress, String path, Long userId) {
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
                                    envId, filePath);
                            if (!devopsIngressDTO.getPathList().stream()
                                    .allMatch(t ->
                                            devopsIngressRepository.checkIngressAndPath(envId, devopsIngressDTO.getDomain(), t.getPath(), null))) {
                                throw new GitOpsExplainException(GitOpsObjectError.INGRESS_DOMAIN_PATH_IS_EXIST.getError(), filePath);
                            }
                            devopsIngressService.addIngressByGitOps(devopsIngressDTO, projectId, userId);
                            devopsIngressE = devopsIngressRepository
                                    .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                        }
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsIngressE.getCommandId());
                        //0.9.0-0.10.0,新增commandId,如果gitops库如果只是移动对象到另外一个文件，避免npe
                        if (devopsEnvCommandE == null) {
                            devopsEnvCommandE = createDevopsEnvCommandE(CREATE);
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
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private void updateIngress(Map<String, String> objectPath, Long envId, Long projectId, List<V1beta1Ingress> updateV1beta1Ingress, String path, Long userId) {
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
                                envId, filePath);
                        DevopsIngressDTO ingressDTO = devopsIngressRepository.getIngress(projectId, devopsIngressE.getId());
                        if (devopsIngressDTO.equals(ingressDTO)) {
                            isNotChange = true;
                        }
                        if (!devopsIngressDTO.getPathList().stream()
                                .allMatch(t ->
                                        devopsIngressRepository.checkIngressAndPath(envId, devopsIngressDTO.getDomain(),
                                                t.getPath(), devopsIngressE.getId()))) {
                            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_DOMAIN_PATH_IS_EXIST.getError(), filePath);
                        }
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsIngressE.getCommandId());
                        if (!isNotChange) {
                            devopsIngressService.updateIngressByGitOps(devopsIngressE.getId(), devopsIngressDTO, projectId, userId);
                            DevopsIngressE newdevopsIngressE = devopsIngressRepository
                                    .selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                            devopsEnvCommandE = devopsEnvCommandRepository.query(newdevopsIngressE.getCommandId());
                        }
                        //0.9.0-0.10.0,新增commandId,如果gitops库如果一个文件里面有多个对象，只操作其中一个对象，其它对象更新commitsha避免npe
                        if (devopsEnvCommandE == null) {
                            devopsEnvCommandE = createDevopsEnvCommandE(CREATE);
                            devopsEnvCommandE.setObjectId(devopsIngressE.getId());
                            DevopsIngressDO devopsIngressDO = devopsIngressRepository.getIngress(devopsIngressE.getId());
                            devopsIngressDO.setCommandId(devopsEnvCommandE.getId());
                            devopsIngressRepository.updateIngress(devopsIngressDO);
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
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
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
                                                 Long envId, String filePath) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setDomain(v1beta1Ingress.getSpec().getRules().get(0).getHost()
        );
        devopsIngressDTO.setName(v1beta1Ingress.getMetadata().getName());
        devopsIngressDTO.setEnvId(envId);
        List<String> pathCheckList = new ArrayList<>();
        List<DevopsIngressPathDTO> devopsIngressPathDTOS = new ArrayList<>();
        List<V1beta1HTTPIngressPath> paths = v1beta1Ingress.getSpec().getRules().get(0).getHttp().getPaths();
        if (paths == null) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_PATH_IS_EMPTY.getError(), filePath);
        }
        for (V1beta1HTTPIngressPath v1beta1HTTPIngressPath : paths) {
            String path = v1beta1HTTPIngressPath.getPath();
            try {
                DevopsIngressValidator.checkPath(path);
                if (pathCheckList.contains(path)) {
                    throw new GitOpsExplainException(GitOpsObjectError.INGRESS_PATH_DUPLICATED.getError(), filePath);
                }
                pathCheckList.add(path);
            } catch (Exception e) {
                throw new GitOpsExplainException(e.getMessage(), filePath);
            }
            V1beta1IngressBackend backend = v1beta1HTTPIngressPath.getBackend();
            String serviceName = backend.getServiceName();
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                    serviceName, envId);
            Long servicePort = null;
            IntOrString backendServicePort = backend.getServicePort();
            if (backendServicePort.isInteger() || pattern.matcher(TypeUtil.objToString(backendServicePort)).matches()) {
                servicePort = TypeUtil.objToLong(backendServicePort);
            } else {
                if (devopsServiceE != null) {
                    servicePort = devopsServiceE.getPorts().get(0).getPort();
                }
            }
            DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
            devopsIngressPathDTO.setPath(path);
            devopsIngressPathDTO.setServicePort(servicePort);
            devopsIngressPathDTO.setServiceName(serviceName);
            devopsIngressPathDTO.setServiceId(devopsServiceE == null ? null : devopsServiceE.getId());
            devopsIngressPathDTOS.add(devopsIngressPathDTO);
        }
        devopsIngressDTO.setPathList(devopsIngressPathDTOS);
        return devopsIngressDTO;
    }

    private DevopsEnvCommandE createDevopsEnvCommandE(String type) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        if (type.equals(CREATE)) {
            devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
        } else {
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        }
        devopsEnvCommandE.setObject(ObjectType.INGRESS.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandRepository.create(devopsEnvCommandE);
    }
}
