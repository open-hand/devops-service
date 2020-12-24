package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.kubernetes.client.models.V1Endpoints;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsPvcReqVO;
import io.choerodon.devops.app.service.DevopsEnvCommandService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsPvcService;
import io.choerodon.devops.app.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsPvcDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.mapper.DevopsPvcMapper;
import io.choerodon.devops.infra.util.GitOpsUtil;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * @author zmf
 * @since 11/4/19
 */
@Component
public class HandlePVCFileRelationServiceImpl implements HandlerObjectFileRelationsService<V1PersistentVolumeClaim> {
    private static final String GIT_SUFFIX = "/.git";
    private static final Logger LOGGER = LoggerFactory.getLogger(HandlePVCFileRelationServiceImpl.class);

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsPvcService devopsPvcService;
    @Autowired
    private DevopsPvcMapper devopsPvcMapper;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;


    private void updatePersistentVolumeClaims(Map<String, String> objectPath, Long envId, List<V1PersistentVolumeClaim> updatePvcs, String path, Long userId) {
        updatePvcs.forEach(pvc -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(pvc.hashCode()));
                DevopsPvcDTO devopsPvcDTO = devopsPvcService
                        .queryByEnvIdAndName(envId, pvc.getMetadata().getName());
                //初始化pvc对象参数,更新pvc并更新文件对象关联关系
                DevopsPvcReqVO devopsPvcReqVO = constructPvc(
                        pvc,
                        envId, "update");

                boolean isNotChange = isIdentical(devopsPvcDTO, devopsPvcReqVO);
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsPvcDTO.getCommandId());
                devopsPvcReqVO.setId(devopsPvcDTO.getId());
                if (!isNotChange) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("The pvc {} is changed", devopsPvcReqVO.getName());
                        LOGGER.info("And the record in db is {}", JsonHelper.marshalByJackson(devopsPvcDTO));
                        LOGGER.info("And the pvc from GitOps is {}", JsonHelper.marshalByJackson(devopsPvcReqVO));
                    }
                    // PVC不允许更改
                    throw new GitOpsExplainException(GitOpsObjectError.PERSISTENT_VOLUME_CLAIM_UNMODIFIED.getError(), filePath, new Object[]{devopsPvcDTO.getName()});
                }
                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, devopsPvcDTO.getId(), pvc.getKind());
                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                        envId,
                        devopsEnvFileResourceDTO,
                        pvc.hashCode(), devopsPvcDTO.getId(), pvc.getKind());

            } catch (GitOpsExplainException ex) {
                throw ex;
            } catch (CommonException e) {
                throw new GitOpsExplainException(e.getMessage(), filePath, "", e.getParameters());
            }
        });
    }

    private boolean isIdentical(DevopsPvcDTO dbRecord, DevopsPvcReqVO update) {
        return Objects.equals(dbRecord.getAccessModes(), update.getAccessModes())
                && Objects.equals(dbRecord.getRequestResource(), update.getRequestResource())
                && Objects.equals(dbRecord.getPvName(), update.getPvName());
    }

    private void addPersistentVolumeClaims(Map<String, String> objectPath, Long envId, List<V1PersistentVolumeClaim> pvcs, String path, Long userId) {
        pvcs.forEach(pvc -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(pvc.hashCode()));
                DevopsPvcDTO devopsPvcDTO = devopsPvcService
                        .queryByEnvIdAndName(envId, pvc.getMetadata().getName());
                DevopsPvcReqVO devopsPvcReqVO;

                DevopsPvcDTO newDevopsPvcDTO = new DevopsPvcDTO();
                //初始化pvc参数,创建时判断pvc是否存在，存在则直接创建文件对象关联关系
                if (devopsPvcDTO == null) {
                    devopsPvcReqVO = constructPvc(
                            pvc,
                            envId,
                            "create");
                    newDevopsPvcDTO = devopsPvcService.createOrUpdateByGitOps(userId, devopsPvcReqVO);
                } else {
                    newDevopsPvcDTO.setId(devopsPvcDTO.getId());
                    newDevopsPvcDTO.setCommandId(devopsPvcDTO.getCommandId());
                }
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(newDevopsPvcDTO.getCommandId());
                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, pvc.hashCode(), newDevopsPvcDTO.getId(),
                        pvc.getKind());
            } catch (GitOpsExplainException ex) {
                throw ex;
            } catch (CommonException e) {
                throw new GitOpsExplainException(e.getMessage(), filePath, "", e.getParameters());
            }
        });
    }


    private DevopsPvcReqVO constructPvc(V1PersistentVolumeClaim claim, Long envId, String type) {
        DevopsPvcReqVO devopsPvcReqVO = new DevopsPvcReqVO();
        devopsPvcReqVO.setEnvId(envId);
        devopsPvcReqVO.setName(claim.getMetadata().getName());
        devopsPvcReqVO.setCommandType(type);
        // 暂时只设计为支持一种模式
        devopsPvcReqVO.setAccessModes(claim.getSpec().getAccessModes().get(0));
        devopsPvcReqVO.setRequestResource(claim.getSpec().getResources().getRequests().get(KubernetesConstants.STORAGE).toSuffixedString());
        if (claim.getSpec().getVolumeName() != null) {
            devopsPvcReqVO.setPvName(claim.getSpec().getVolumeName());
        }
        return devopsPvcReqVO;
    }

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync, List<V1PersistentVolumeClaim> claims, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforePvcs = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(ResourceType.PERSISTENT_VOLUME_CLAIM.getType()))
                .map(devopsEnvFileResourceE -> {
                    DevopsPvcDTO devopsPvcDTO = devopsPvcMapper
                            .selectByPrimaryKey(devopsEnvFileResourceE.getResourceId());
                    if (devopsPvcDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), ResourceType.PERSISTENT_VOLUME_CLAIM.getType());
                        return null;
                    }
                    return devopsPvcDTO.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        List<V1PersistentVolumeClaim> pvcToAdd = new ArrayList<>();
        List<V1PersistentVolumeClaim> pvcToUpdate = new ArrayList<>();

        GitOpsUtil.pickCUDResource(beforePvcs, claims, pvcToAdd, pvcToUpdate, pvc -> pvc.getMetadata().getName());

        //新增pvc
        addPersistentVolumeClaims(objectPath, envId, pvcToAdd, path, userId);
        //更新pvc
        updatePersistentVolumeClaims(objectPath, envId, pvcToUpdate, path, userId);
        //删除pvc,和文件对象关联关系
        beforePvcs.forEach(pvcName -> {
            DevopsPvcDTO devopsPvcDTO = devopsPvcService.queryByEnvIdAndName(envId, pvcName);
            if (devopsPvcDTO != null) {
                devopsPvcService.deleteByGitOps(devopsPvcDTO.getId());
                devopsEnvFileResourceService
                        .baseDeleteByEnvIdAndResourceId(envId, devopsPvcDTO.getId(), ResourceType.PERSISTENT_VOLUME_CLAIM.getType());
            }
        });
    }


    @Override
    public Class<V1PersistentVolumeClaim> getTarget() {
        return V1PersistentVolumeClaim.class;
    }
}
