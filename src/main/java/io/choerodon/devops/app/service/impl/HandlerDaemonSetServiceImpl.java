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
import io.choerodon.devops.api.vo.DevopsDaemonSetVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsDaemonSetDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsWorkloadResourceContentDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

public class HandlerDaemonSetServiceImpl implements HandlerObjectFileRelationsService<DevopsDaemonSetDTO> {
    private static final String DAEMONSET = "daemonset";
    private static final String GIT_SUFFIX = "/.git";

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsDaemonSetService devopsDaemonSetService;
    @Autowired
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;


    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync,
                                 List<DevopsDaemonSetDTO> devopsDaemonSetDTOS, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeDaemonSet = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(DAEMONSET))
                .map(devopsEnvFileResourceE -> {
                    DevopsDaemonSetDTO devopsDaemonSetDTO = devopsDaemonSetService
                            .selectByPrimaryKey(devopsEnvFileResourceE.getResourceId());
                    if (devopsDaemonSetDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), DAEMONSET);
                        return null;
                    }
                    return devopsDaemonSetDTO.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 比较已存在的秘钥和新增要处理的秘钥,获取新增秘钥，更新秘钥，删除秘钥
        List<DevopsDaemonSetDTO> addDaemonSet = new ArrayList<>();
        List<DevopsDaemonSetDTO> updateDaemonSet = new ArrayList<>();
        devopsDaemonSetDTOS.forEach(devopsDaemonSetDTO -> {
            if (beforeDaemonSet.contains(devopsDaemonSetDTO.getName())) {
                updateDaemonSet.add(devopsDaemonSetDTO);
                beforeDaemonSet.remove(devopsDaemonSetDTO.getName());
            } else {
                addDaemonSet.add(devopsDaemonSetDTO);
            }
        });
        //删除daemonSet,删除文件对象关联关系
        beforeDaemonSet.forEach(daemonSetName -> {
            DevopsDaemonSetDTO devopsDaemonSetDTO = devopsDaemonSetService.baseQueryByEnvIdAndName(envId, daemonSetName);
            if (devopsDaemonSetDTO != null) {
                devopsDaemonSetService.deleteByGitOps(devopsDaemonSetDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, devopsDaemonSetDTO.getId(), DAEMONSET);
            }
        });

        // 新增daemonSet
        addDaemonSet(objectPath, envId, projectId, addDaemonSet, path, userId);
        // 更新daemonSet
        updateDaemonSet(objectPath, envId, projectId, updateDaemonSet, path, userId);
    }

    @Override
    public Class<DevopsDaemonSetDTO> getTarget() {
        return DevopsDaemonSetDTO.class;
    }

    private void addDaemonSet(Map<String, String> objectPath, Long envId, Long projectId, List<DevopsDaemonSetDTO> devopsDaemonSetDTOS, String path, Long userId) {
        devopsDaemonSetDTOS
                .forEach(daemonSetDTO -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(daemonSetDTO.hashCode()));

                        DevopsDaemonSetDTO devopsDaemonSetDTO = devopsDaemonSetService
                                .baseQueryByEnvIdAndName(envId, daemonSetDTO.getName());
                        DevopsDaemonSetVO devopsDaemonSetVO = new DevopsDaemonSetVO();
                        //初始化daemonSet对象参数,存在daemonSet则直接创建文件对象关联关系
                        if (devopsDaemonSetDTO == null) {
                            devopsDaemonSetVO = getDevopsDaemonSetVO(
                                    daemonSetDTO,
                                    projectId,
                                    envId, CREATE_TYPE);
                            devopsDaemonSetVO = devopsDaemonSetService.createOrUpdateByGitOps(devopsDaemonSetVO, userId, daemonSetDTO.getContent());
                        } else {
                            devopsDaemonSetVO.setCommandId(devopsDaemonSetDTO.getCommandId());
                            devopsDaemonSetVO.setId(devopsDaemonSetDTO.getId());
                        }
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsDaemonSetVO.getCommandId());

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, daemonSetDTO.hashCode(), devopsDaemonSetVO.getId(),
                                ResourceType.DAEMONSET.getType());
                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private void updateDaemonSet(Map<String, String> objectPath, Long envId, Long projectId, List<DevopsDaemonSetDTO> updateDevopsDaemonSetDTO, String path, Long userId) {
        updateDevopsDaemonSetDTO
                .forEach(daemonSetDTO -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(daemonSetDTO.hashCode()));
                        DevopsDaemonSetDTO devopsDaemonSetDTO = devopsDaemonSetService
                                .baseQueryByEnvIdAndName(envId, daemonSetDTO.getName());
                        // 初始化daemonSet对象参数,更新daemonSet并更新文件对象关联关系
                        DevopsDaemonSetVO devopsDaemonSetVO = getDevopsDaemonSetVO(daemonSetDTO, projectId, envId, UPDATE_TYPE);

                        //判断资源是否发生了改变
                        DevopsWorkloadResourceContentDTO devopsWorkloadResourceContentDTO = devopsWorkloadResourceContentService.baseQuery(devopsDaemonSetDTO.getId(), ResourceType.DAEMONSET.getType());
                        boolean isNotChange = daemonSetDTO.getContent().equals(devopsWorkloadResourceContentDTO.getContent());
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsDaemonSetDTO.getCommandId());

                        //发生改变走处理改变资源的逻辑
                        if (!isNotChange) {
                            devopsDaemonSetVO = devopsDaemonSetService.createOrUpdateByGitOps(devopsDaemonSetVO, envId, daemonSetDTO.getContent());
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsDaemonSetVO.getCommandId());
                        }

                        // 更新之前的command为成功
                        devopsEnvCommandService.updateOperatingToSuccessBeforeDate(ObjectType.DAEMONSET, devopsEnvCommandDTO.getObjectId(), devopsEnvCommandDTO.getCreationDate());
                        //没发生改变,更新commit记录，更新文件对应关系记录
                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, devopsDaemonSetDTO.getId(), ResourceType.DEPLOYMENT.getType());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceDTO,
                                daemonSetDTO.hashCode(), devopsDaemonSetDTO.getId(), ResourceType.DAEMONSET.getType());


                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private DevopsDaemonSetVO getDevopsDaemonSetVO(DevopsDaemonSetDTO devopsDaemonSetDTO, Long projectId, Long envId, String operateType) {
        DevopsDaemonSetVO devopsDaemonSetVO = new DevopsDaemonSetVO();
        devopsDaemonSetVO.setName(devopsDaemonSetDTO.getName());
        devopsDaemonSetVO.setContent(devopsDaemonSetDTO.getContent());
        devopsDaemonSetVO.setProjectId(projectId);
        devopsDaemonSetVO.setEnvId(envId);
        devopsDaemonSetVO.setOperateType(operateType);

        return devopsDaemonSetVO;
    }
}
