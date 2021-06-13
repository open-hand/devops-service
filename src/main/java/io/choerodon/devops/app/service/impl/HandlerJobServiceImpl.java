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

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsJobVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsJobDTO;
import io.choerodon.devops.infra.dto.DevopsWorkloadResourceContentDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

public class HandlerJobServiceImpl implements HandlerObjectFileRelationsService<DevopsJobDTO> {
    private static final String JOB = "job";
    private static final String GIT_SUFFIX = "/.git";

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsJobService devopsJobService;
    @Autowired
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;


    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync,
                                 List<DevopsJobDTO> devopsJobDTOS, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeJob = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(JOB))
                .map(devopsEnvFileResourceE -> {
                    DevopsJobDTO devopsJobDTO = devopsJobService
                            .selectByPrimaryKey(devopsEnvFileResourceE.getResourceId());
                    if (devopsJobDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), JOB);
                        return null;
                    }
                    return devopsJobDTO.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 比较已存在的秘钥和新增要处理的秘钥,获取新增秘钥，更新秘钥，删除秘钥
        List<DevopsJobDTO> addJob = new ArrayList<>();
        List<DevopsJobDTO> updateJob = new ArrayList<>();
        devopsJobDTOS.forEach(devopsJobDTO -> {
            if (beforeJob.contains(devopsJobDTO.getName())) {
                updateJob.add(devopsJobDTO);
                beforeJob.remove(devopsJobDTO.getName());
            } else {
                addJob.add(devopsJobDTO);
            }
        });
        //删除job,删除文件对象关联关系
        beforeJob.forEach(jobName -> {
            DevopsJobDTO devopsJobDTO = devopsJobService.baseQueryByEnvIdAndName(envId, jobName);
            if (devopsJobDTO != null) {
                devopsJobService.deleteByGitOps(devopsJobDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, devopsJobDTO.getId(), JOB);
            }
        });

        // 新增job
        addJob(objectPath, envId, projectId, addJob, path, userId);
        // 更新job
        updateJob(objectPath, envId, projectId, updateJob, path, userId);
    }

    @Override
    public Class<DevopsJobDTO> getTarget() {
        return DevopsJobDTO.class;
    }

    private void addJob(Map<String, String> objectPath, Long envId, Long projectId, List<DevopsJobDTO> devopsJobDTOS, String path, Long userId) {
        devopsJobDTOS
                .forEach(jobDTO -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(jobDTO.hashCode()));

                        DevopsJobDTO devopsJobDTO = devopsJobService
                                .baseQueryByEnvIdAndName(envId, jobDTO.getName());
                        DevopsJobVO devopsJobVO = new DevopsJobVO();
                        //初始化job对象参数,存在job则直接创建文件对象关联关系
                        if (devopsJobDTO == null) {
                            devopsJobVO = getDevopsJobVO(
                                    jobDTO,
                                    projectId,
                                    envId, CREATE_TYPE);
                            devopsJobVO = devopsJobService.createOrUpdateByGitOps(devopsJobVO, userId, jobDTO.getContent());
                        } else {
                            devopsJobVO.setCommandId(devopsJobDTO.getCommandId());
                            devopsJobVO.setId(devopsJobDTO.getId());
                        }
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsJobVO.getCommandId());

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, jobDTO.hashCode(), devopsJobVO.getId(),
                                ResourceType.JOB.getType());
                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private void updateJob(Map<String, String> objectPath, Long envId, Long projectId, List<DevopsJobDTO> updateDevopsJobDTO, String path, Long userId) {
        updateDevopsJobDTO
                .forEach(jobDTO -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(jobDTO.hashCode()));
                        DevopsJobDTO devopsJobDTO = devopsJobService
                                .baseQueryByEnvIdAndName(envId, jobDTO.getName());
                        // 初始化job对象参数,更新job并更新文件对象关联关系
                        DevopsJobVO devopsJobVO = getDevopsJobVO(jobDTO, projectId, envId, UPDATE_TYPE);

                        //判断资源是否发生了改变
                        DevopsWorkloadResourceContentDTO devopsWorkloadResourceContentDTO = devopsWorkloadResourceContentService.baseQuery(devopsJobDTO.getId(), ResourceType.JOB.getType());
                        boolean isNotChange = jobDTO.getContent().equals(devopsWorkloadResourceContentDTO.getContent());
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsJobDTO.getCommandId());

                        //发生改变走处理改变资源的逻辑
                        if (!isNotChange) {
                            devopsJobVO = devopsJobService.createOrUpdateByGitOps(devopsJobVO, envId, jobDTO.getContent());
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsJobVO.getCommandId());
                        }

                        // 更新之前的command为成功
                        devopsEnvCommandService.updateOperatingToSuccessBeforeDate(ObjectType.JOB, devopsEnvCommandDTO.getObjectId(), devopsEnvCommandDTO.getCreationDate());
                        //没发生改变,更新commit记录，更新文件对应关系记录
                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, devopsJobDTO.getId(), ResourceType.JOB.getType());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceDTO,
                                jobDTO.hashCode(), devopsJobDTO.getId(), ResourceType.JOB.getType());


                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private DevopsJobVO getDevopsJobVO(DevopsJobDTO devopsJobDTO, Long projectId, Long envId, String operateType) {
        DevopsJobVO devopsJobVO = new DevopsJobVO();
        devopsJobVO.setName(devopsJobDTO.getName());
        devopsJobVO.setContent(devopsJobDTO.getContent());
        devopsJobVO.setProjectId(projectId);
        devopsJobVO.setEnvId(envId);
        devopsJobVO.setOperateType(operateType);

        return devopsJobVO;
    }
}
