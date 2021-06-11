package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.kubernetes.client.models.V1Endpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsDeploymentVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsDeploymentDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsWorkloadResourceContentDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class HandlerDeploymentServiceImpl implements HandlerObjectFileRelationsService<DevopsDeploymentDTO> {
    private static final String CREATE_TYPE = "create";
    private static final String UPDATE_TYPE = "update";
    private static final String DEPLOYMENT = "Deployment";
    private static final String GIT_SUFFIX = "/.git";

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsDeploymentService deploymentService;
    @Autowired
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;


    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync,
                                 List<DevopsDeploymentDTO> devopsDeploymentDTOS, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeDeployment = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(DEPLOYMENT))
                .map(devopsEnvFileResourceE -> {
                    DevopsDeploymentDTO devopsDeploymentDTO = deploymentService
                            .selectByPrimaryKey(devopsEnvFileResourceE.getResourceId());
                    if (devopsDeploymentDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), DEPLOYMENT);
                        return null;
                    }
                    return devopsDeploymentDTO.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 比较已存在的秘钥和新增要处理的秘钥,获取新增秘钥，更新秘钥，删除秘钥
        List<DevopsDeploymentDTO> addDeployment = new ArrayList<>();
        List<DevopsDeploymentDTO> updateDeployment = new ArrayList<>();
        devopsDeploymentDTOS.forEach(devopsDeploymentDTO -> {
            if (beforeDeployment.contains(devopsDeploymentDTO.getName())) {
                updateDeployment.add(devopsDeploymentDTO);
                beforeDeployment.remove(devopsDeploymentDTO.getName());
            } else {
                addDeployment.add(devopsDeploymentDTO);
            }
        });
        //删除deployment,删除文件对象关联关系
        beforeDeployment.forEach(deploymentName -> {
            DevopsDeploymentDTO devopsDeploymentDTO = deploymentService.baseQueryByEnvIdAndName(envId, deploymentName);
            if (devopsDeploymentDTO != null) {
                deploymentService.deleteByGitOps(devopsDeploymentDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, devopsDeploymentDTO.getId(), DEPLOYMENT);
            }
        });

        // 新增deployment
        addDeployment(objectPath, envId, projectId, addDeployment, path, userId);
        // 更新deployment
        updateDeployment(objectPath, envId, projectId, updateDeployment, path, userId);
    }

    @Override
    public Class<DevopsDeploymentDTO> getTarget() {
        return DevopsDeploymentDTO.class;
    }

    private void addDeployment(Map<String, String> objectPath, Long envId, Long projectId, List<DevopsDeploymentDTO> devopsDeploymentDTOS, String path, Long userId) {
        devopsDeploymentDTOS
                .forEach(deploymentDTO -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(deploymentDTO.hashCode()));

                        DevopsDeploymentDTO devopsDeploymentDTO = deploymentService
                                .baseQueryByEnvIdAndName(envId, deploymentDTO.getName());
                        DevopsDeploymentVO devopsDeploymentVO = new DevopsDeploymentVO();
                        //初始化deployment对象参数,存在deployment则直接创建文件对象关联关系
                        if (devopsDeploymentDTO == null) {
                            devopsDeploymentVO = getDevopsDeploymentVO(
                                    deploymentDTO,
                                    projectId,
                                    envId, CREATE_TYPE);
                            devopsDeploymentVO = deploymentService.createOrUpdateByGitOps(devopsDeploymentVO, userId, deploymentDTO.getContent());
                        } else {
                            devopsDeploymentVO.setCommandId(devopsDeploymentDTO.getCommandId());
                            devopsDeploymentVO.setId(devopsDeploymentDTO.getId());
                        }
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsDeploymentVO.getCommandId());

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, deploymentDTO.hashCode(), devopsDeploymentVO.getId(),
                                ResourceType.DEPLOYMENT.getType());
                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private void updateDeployment(Map<String, String> objectPath, Long envId, Long projectId, List<DevopsDeploymentDTO> updateDevopsDeploymentDTO, String path, Long userId) {
        updateDevopsDeploymentDTO
                .forEach(deploymentDTO -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(deploymentDTO.hashCode()));
                        DevopsDeploymentDTO devopsDeploymentDTO = deploymentService
                                .baseQueryByEnvIdAndName(envId, deploymentDTO.getName());
                        // 初始化deployment对象参数,更新deployment并更新文件对象关联关系
                        DevopsDeploymentVO devopsDeploymentVO = getDevopsDeploymentVO(deploymentDTO, projectId, envId, UPDATE_TYPE);

                        //判断资源是否发生了改变
                        Yaml yaml = new Yaml();
                        DevopsWorkloadResourceContentDTO devopsWorkloadResourceContentDTO = devopsWorkloadResourceContentService.baseQuery(devopsDeploymentDTO.getId(), ResourceType.DEPLOYMENT.getType());
                        boolean isNotChange = deploymentDTO.getContent().equals(devopsWorkloadResourceContentDTO.getContent());
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsDeploymentDTO.getCommandId());

                        //发生改变走处理改变资源的逻辑
                        if (!isNotChange) {
                            devopsDeploymentVO = deploymentService.createOrUpdateByGitOps(devopsDeploymentVO, envId, deploymentDTO.getContent());
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsDeploymentVO.getCommandId());
                        }

                        // 更新之前的command为成功
                        devopsEnvCommandService.updateOperatingToSuccessBeforeDate(ObjectType.CUSTOM, devopsEnvCommandDTO.getObjectId(), devopsEnvCommandDTO.getCreationDate());
                        //没发生改变,更新commit记录，更新文件对应关系记录
                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, devopsDeploymentDTO.getId(), ResourceType.CUSTOM.getType());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceDTO,
                                deploymentDTO.hashCode(), devopsDeploymentDTO.getId(), ResourceType.CUSTOM.getType());


                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private DevopsDeploymentVO getDevopsDeploymentVO(DevopsDeploymentDTO deployment, Long projectId, Long envId, String operateType) {
        DevopsDeploymentVO devopsDeploymentVO = new DevopsDeploymentVO();
        devopsDeploymentVO.setName(deployment.getName());
        devopsDeploymentVO.setContent(deployment.getContent());
        devopsDeploymentVO.setProjectId(projectId);
        devopsDeploymentVO.setEnvId(envId);
        devopsDeploymentVO.setOperateType(operateType);

        return devopsDeploymentVO;
    }
}
