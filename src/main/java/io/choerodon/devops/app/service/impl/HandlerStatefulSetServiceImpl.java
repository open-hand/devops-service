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
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsStatefulSetVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsStatefulSetDTO;
import io.choerodon.devops.infra.dto.DevopsWorkloadResourceContentDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class HandlerStatefulSetServiceImpl implements HandlerObjectFileRelationsService<DevopsStatefulSetDTO> {
    private static final String STATEFUL_SET = "StatefulSet";
    private static final String GIT_SUFFIX = "/.git";

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsStatefulSetService devopsStatefulSetService;
    @Autowired
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;


    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync,
                                 List<DevopsStatefulSetDTO> devopsStatefulSetDTOS, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeStatefulSet = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(STATEFUL_SET))
                .map(devopsEnvFileResourceE -> {
                    DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetService
                            .selectByPrimaryKey(devopsEnvFileResourceE.getResourceId());
                    if (devopsStatefulSetDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), STATEFUL_SET);
                        return null;
                    }
                    return devopsStatefulSetDTO.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 比较已存在的秘钥和新增要处理的秘钥,获取新增秘钥，更新秘钥，删除秘钥
        List<DevopsStatefulSetDTO> addStatefulSet = new ArrayList<>();
        List<DevopsStatefulSetDTO> updateStatefulSet = new ArrayList<>();
        devopsStatefulSetDTOS.forEach(statefulSetDTO -> {
            if (beforeStatefulSet.contains(statefulSetDTO.getName())) {
                updateStatefulSet.add(statefulSetDTO);
                beforeStatefulSet.remove(statefulSetDTO.getName());
            } else {
                addStatefulSet.add(statefulSetDTO);
            }
        });
        //删除statefulSet,删除文件对象关联关系
        beforeStatefulSet.forEach(statefulSetName -> {
            DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetService.baseQueryByEnvIdAndName(envId, statefulSetName);
            if (devopsStatefulSetDTO != null) {
                devopsStatefulSetService.deleteByGitOps(devopsStatefulSetDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, devopsStatefulSetDTO.getId(), STATEFUL_SET);
            }
        });

        // 新增statefulSet
        addStatefulSet(objectPath, envId, projectId, addStatefulSet, path, userId);
        // 更新statefulSet
        updateStatefulSet(objectPath, envId, projectId, updateStatefulSet, path, userId);
    }

    @Override
    public Class<DevopsStatefulSetDTO> getTarget() {
        return DevopsStatefulSetDTO.class;
    }

    private void addStatefulSet(Map<String, String> objectPath, Long envId, Long projectId, List<DevopsStatefulSetDTO> devopsStatefulSetDTOS, String path, Long userId) {
        devopsStatefulSetDTOS
                .forEach(statefulSetDTO -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(statefulSetDTO.hashCode()));

                        DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetService
                                .baseQueryByEnvIdAndName(envId, statefulSetDTO.getName());
                        DevopsStatefulSetVO devopsStatefulSetVO = new DevopsStatefulSetVO();
                        //初始化statefulSet对象参数,存在statefulSet则直接创建文件对象关联关系
                        if (devopsStatefulSetDTO == null) {
                            devopsStatefulSetVO = getDevopsStatefulSetVO(
                                    statefulSetDTO,
                                    projectId,
                                    envId, CREATE_TYPE);
                            devopsStatefulSetVO = devopsStatefulSetService.createOrUpdateByGitOps(devopsStatefulSetVO, userId, statefulSetDTO.getContent());
                        } else {
                            devopsStatefulSetVO.setCommandId(devopsStatefulSetDTO.getCommandId());
                            devopsStatefulSetVO.setId(devopsStatefulSetDTO.getId());
                        }
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsStatefulSetVO.getCommandId());

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, statefulSetDTO.hashCode(), devopsStatefulSetVO.getId(),
                                ResourceType.STATEFULSET.getType());
                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private void updateStatefulSet(Map<String, String> objectPath, Long envId, Long projectId, List<DevopsStatefulSetDTO> updateDevopsStatefulSetDTOs, String path, Long userId) {
        updateDevopsStatefulSetDTOs
                .forEach(statefulSetDTO -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(statefulSetDTO.hashCode()));
                        DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetService
                                .baseQueryByEnvIdAndName(envId, statefulSetDTO.getName());
                        // 初始化statefulSet对象参数,更新statefulSet并更新文件对象关联关系
                        DevopsStatefulSetVO devopsStatefulSetVO = getDevopsStatefulSetVO(statefulSetDTO, projectId, envId, UPDATE_TYPE);

                        //判断资源是否发生了改变
                        DevopsWorkloadResourceContentDTO devopsWorkloadResourceContentDTO = devopsWorkloadResourceContentService.baseQuery(devopsStatefulSetDTO.getId(), ResourceType.STATEFULSET.getType());
                        boolean isNotChange = statefulSetDTO.getContent().equals(devopsWorkloadResourceContentDTO.getContent());
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsStatefulSetDTO.getCommandId());

                        //发生改变走处理改变资源的逻辑
                        if (!isNotChange) {
                            devopsStatefulSetVO = devopsStatefulSetService.createOrUpdateByGitOps(devopsStatefulSetVO, envId, statefulSetDTO.getContent());
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsStatefulSetVO.getCommandId());
                        }

                        // 更新之前的command为成功
                        devopsEnvCommandService.updateOperatingToSuccessBeforeDate(ObjectType.STATEFULSET, devopsEnvCommandDTO.getObjectId(), devopsEnvCommandDTO.getCreationDate());
                        //没发生改变,更新commit记录，更新文件对应关系记录
                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, devopsStatefulSetDTO.getId(), ResourceType.STATEFULSET.getType());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceDTO,
                                statefulSetDTO.hashCode(), devopsStatefulSetDTO.getId(), ResourceType.STATEFULSET.getType());


                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private DevopsStatefulSetVO getDevopsStatefulSetVO(DevopsStatefulSetDTO statefulSetDTO, Long projectId, Long envId, String operateType) {
        DevopsStatefulSetVO devopsStatefulSetVO = new DevopsStatefulSetVO();
        devopsStatefulSetVO.setName(statefulSetDTO.getName());
        devopsStatefulSetVO.setContent(statefulSetDTO.getContent());
        devopsStatefulSetVO.setProjectId(projectId);
        devopsStatefulSetVO.setEnvId(envId);
        devopsStatefulSetVO.setOperateType(operateType);

        return devopsStatefulSetVO;
    }
}
