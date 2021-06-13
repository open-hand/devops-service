package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiscConstants.CREATE_TYPE;
import static io.choerodon.devops.infra.constant.MiscConstants.UPDATE_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.kubernetes.client.models.V1Endpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.workload.DevopsCronjobVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsCronJobDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsWorkloadResourceContentDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Component
public class HandlerCronJobServiceImpl implements HandlerObjectFileRelationsService<DevopsCronJobDTO> {
    private static final String CRONJOB = "CronJob";
    private static final String GIT_SUFFIX = "/.git";

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsCronJobService devopsCronJobService;
    @Autowired
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;


    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync,
                                 List<DevopsCronJobDTO> devopsDeploymentDTOS, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeDeployment = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(CRONJOB))
                .map(devopsEnvFileResourceE -> {
                    DevopsCronJobDTO devopsDeploymentDTO = devopsCronJobService
                            .selectByPrimaryKey(devopsEnvFileResourceE.getResourceId());
                    if (devopsDeploymentDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), CRONJOB);
                        return null;
                    }
                    return devopsDeploymentDTO.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 比较已存在的秘钥和新增要处理的秘钥,获取新增秘钥，更新秘钥，删除秘钥
        List<DevopsCronJobDTO> addDeployment = new ArrayList<>();
        List<DevopsCronJobDTO> updateDeployment = new ArrayList<>();
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
            DevopsCronJobDTO devopsDeploymentDTO = devopsCronJobService.baseQueryByEnvIdAndName(envId, deploymentName);
            if (devopsDeploymentDTO != null) {
                devopsCronJobService.deleteByGitOps(devopsDeploymentDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, devopsDeploymentDTO.getId(), CRONJOB);
            }
        });

        // 新增deployment
        addDeployment(objectPath, envId, projectId, addDeployment, path, userId);
        // 更新deployment
        updateDeployment(objectPath, envId, projectId, updateDeployment, path, userId);
    }

    @Override
    public Class<DevopsCronJobDTO> getTarget() {
        return DevopsCronJobDTO.class;
    }

    private void addDeployment(Map<String, String> objectPath, Long envId, Long projectId, List<DevopsCronJobDTO> devopsDeploymentDTOS, String path, Long userId) {
        devopsDeploymentDTOS
                .forEach(deploymentDTO -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(deploymentDTO.hashCode()));

                        DevopsCronJobDTO devopsDeploymentDTO = devopsCronJobService
                                .baseQueryByEnvIdAndName(envId, deploymentDTO.getName());
                        DevopsCronjobVO devopsDeploymentVO = new DevopsCronjobVO();
                        //初始化deployment对象参数,存在deployment则直接创建文件对象关联关系
                        if (devopsDeploymentDTO == null) {
                            devopsDeploymentVO = getDevopsCronjobVO(
                                    deploymentDTO,
                                    projectId,
                                    envId, CREATE_TYPE);
                            devopsDeploymentVO = devopsCronJobService.createOrUpdateByGitOps(devopsDeploymentVO, userId, deploymentDTO.getContent());
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

    private void updateDeployment(Map<String, String> objectPath, Long envId, Long projectId, List<DevopsCronJobDTO> updateDevopsCronJobDTO, String path, Long userId) {
        updateDevopsCronJobDTO
                .forEach(cronJobDTO -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(cronJobDTO.hashCode()));
                        DevopsCronJobDTO devopsCronJobDTO = devopsCronJobService
                                .baseQueryByEnvIdAndName(envId, cronJobDTO.getName());
                        // 初始化cronjob对象参数,更新cronjob并更新文件对象关联关系
                        DevopsCronjobVO devopsCronjobVO = getDevopsCronjobVO(cronJobDTO, projectId, envId, UPDATE_TYPE);

                        //判断资源是否发生了改变
                        DevopsWorkloadResourceContentDTO devopsWorkloadResourceContentDTO = devopsWorkloadResourceContentService.baseQuery(devopsCronJobDTO.getId(), ResourceType.DEPLOYMENT.getType());
                        boolean isNotChange = cronJobDTO.getContent().equals(devopsWorkloadResourceContentDTO.getContent());
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsCronJobDTO.getCommandId());

                        //发生改变走处理改变资源的逻辑
                        if (!isNotChange) {
                            devopsCronjobVO = devopsCronJobService.createOrUpdateByGitOps(devopsCronjobVO, envId, cronJobDTO.getContent());
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsCronjobVO.getCommandId());
                        }

                        // 更新之前的command为成功
                        devopsEnvCommandService.updateOperatingToSuccessBeforeDate(ObjectType.CRONJOB, devopsEnvCommandDTO.getObjectId(), devopsEnvCommandDTO.getCreationDate());
                        //没发生改变,更新commit记录，更新文件对应关系记录
                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, devopsCronJobDTO.getId(), ResourceType.CRON_JOB.getType());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceDTO,
                                cronJobDTO.hashCode(), devopsCronJobDTO.getId(), ResourceType.CRON_JOB.getType());


                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private DevopsCronjobVO getDevopsCronjobVO(DevopsCronJobDTO devopsCronJobDTO, Long projectId, Long envId, String operateType) {
        DevopsCronjobVO devopsCronjobVO = new DevopsCronjobVO();
        devopsCronjobVO.setName(devopsCronJobDTO.getName());
        devopsCronjobVO.setContent(devopsCronJobDTO.getContent());
        devopsCronjobVO.setProjectId(projectId);
        devopsCronjobVO.setEnvId(envId);
        devopsCronjobVO.setOperateType(operateType);

        return devopsCronjobVO;
    }
}
