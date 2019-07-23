package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.V1Endpoints;
import io.kubernetes.client.models.V1beta1HTTPIngressPath;
import io.kubernetes.client.models.V1beta1Ingress;
import io.kubernetes.client.models.V1beta1IngressBackend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsIngressValidator;
import io.choerodon.devops.api.vo.DevopsIngressPathVO;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class HandlerIngressRelationsServiceImpl implements HandlerObjectFileRelationsService<V1beta1Ingress> {

    public static final String INGRESS = "Ingress";
    private static final String CREATE = "create";
    private static final String GIT_SUFFIX = "/.git";
    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
    private Gson gson = new Gson();
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;


    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync, List<V1beta1Ingress> v1beta1Ingresses, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeIngress = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(INGRESS))
                .map(devopsEnvFileResourceE -> {
                    DevopsIngressDTO devopsIngressDTO = devopsIngressService
                            .baseQuery(devopsEnvFileResourceE.getResourceId());
                    if (devopsIngressDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), INGRESS);
                        return null;
                    }
                    return devopsIngressDTO.getName();
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
            DevopsIngressDTO devopsIngressDTO = devopsIngressService.baseCheckByEnvAndName(envId, ingressName);
            if (devopsIngressDTO != null) {
                devopsIngressService.deleteIngressByGitOps(devopsIngressDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, devopsIngressDTO.getId(), INGRESS);
            }
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
                        DevopsIngressDTO devopsIngressDTO = devopsIngressService
                                .baseCheckByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                        DevopsIngressVO devopsIngressVO;
                        //初始化ingress对象参数,存在ingress则直接创建文件对象关联关系
                        if (devopsIngressDTO == null) {
                            devopsIngressVO = getDevopsIngressDTO(
                                    v1beta1Ingress,
                                    envId, filePath);
                            if (!devopsIngressVO.getPathList().stream()
                                    .allMatch(t ->
                                            devopsIngressService.baseCheckPath(envId, devopsIngressVO.getDomain(), t.getPath(), null))) {
                                throw new GitOpsExplainException(GitOpsObjectError.INGRESS_DOMAIN_PATH_IS_EXIST.getError(), filePath);
                            }
                            devopsIngressService.createIngressByGitOps(devopsIngressVO, projectId, userId);
                            devopsIngressDTO = devopsIngressService
                                    .baseCheckByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                        }
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsIngressDTO.getCommandId());

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, v1beta1Ingress.hashCode(), devopsIngressDTO.getId(),
                                v1beta1Ingress.getKind());
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
                        DevopsIngressDTO devopsIngressDTO = devopsIngressService
                                .baseCheckByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                        checkIngressAppVersion(v1beta1Ingress);
                        //初始化ingress对象参数,更新ingress并更新文件对象关联关系
                        DevopsIngressVO devopsIngressVO = getDevopsIngressDTO(
                                v1beta1Ingress,
                                envId, filePath);
                        DevopsIngressVO ingressDTO = devopsIngressService.queryIngress(projectId, devopsIngressDTO.getId());
                        if (devopsIngressVO.equals(ingressDTO)) {
                            isNotChange = true;
                        }
                        if (!devopsIngressVO.getPathList().stream()
                                .allMatch(t ->
                                        devopsIngressService.baseCheckPath(envId, devopsIngressVO.getDomain(),
                                                t.getPath(), devopsIngressDTO.getId()))) {
                            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_DOMAIN_PATH_IS_EXIST.getError(), filePath);
                        }
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsIngressDTO.getCommandId());
                        if (!isNotChange) {
                            devopsIngressService.updateIngressByGitOps(devopsIngressDTO.getId(), devopsIngressVO, projectId, userId);
                            DevopsIngressDTO newdevopsIngressDTO = devopsIngressService.baseCheckByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(newdevopsIngressDTO.getCommandId());
                        }

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, devopsIngressDTO.getId(), v1beta1Ingress.getKind());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceDTO,
                                v1beta1Ingress.hashCode(), devopsIngressDTO.getId(), v1beta1Ingress.getKind());

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

    private DevopsIngressVO getDevopsIngressDTO(V1beta1Ingress v1beta1Ingress,
                                                Long envId, String filePath) {
        DevopsIngressVO devopsIngressVO = new DevopsIngressVO();
        devopsIngressVO.setDomain(v1beta1Ingress.getSpec().getRules().get(0).getHost()
        );
        devopsIngressVO.setName(v1beta1Ingress.getMetadata().getName());
        devopsIngressVO.setEnvId(envId);
        List<String> pathCheckList = new ArrayList<>();
        List<DevopsIngressPathVO> devopsIngressPathVOS = new ArrayList<>();
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
            DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQueryByNameAndEnvId(
                    serviceName, envId);
            Long servicePort = null;
            IntOrString backendServicePort = backend.getServicePort();
            if (backendServicePort.isInteger() || pattern.matcher(TypeUtil.objToString(backendServicePort)).matches()) {
                servicePort = TypeUtil.objToLong(backendServicePort);
            } else {
                if (devopsServiceDTO != null) {
                    List<PortMapVO> listPorts = gson.fromJson(devopsServiceDTO.getPorts(), new TypeToken<ArrayList<PortMapVO>>() {
                    }.getType());
                    servicePort = listPorts.get(0).getPort();
                }
            }
            DevopsIngressPathVO devopsIngressPathVO = new DevopsIngressPathVO();
            devopsIngressPathVO.setPath(path);
            devopsIngressPathVO.setServicePort(servicePort);
            devopsIngressPathVO.setServiceName(serviceName);
            devopsIngressPathVO.setServiceId(devopsServiceDTO == null ? null : devopsServiceDTO.getId());
            devopsIngressPathVOS.add(devopsIngressPathVO);
        }
        devopsIngressVO.setPathList(devopsIngressPathVOS);
        return devopsIngressVO;
    }

    private DevopsEnvCommandDTO createDevopsEnvCommandE(String type) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        if (type.equals(CREATE)) {
            devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType());
        } else {
            devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        }
        devopsEnvCommandDTO.setObject(ObjectType.INGRESS.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
    }
}
