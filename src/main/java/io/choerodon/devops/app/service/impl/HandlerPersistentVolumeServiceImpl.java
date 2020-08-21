package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsPvReqVO;
import io.choerodon.devops.api.vo.kubernetes.LocalPvResource;
import io.choerodon.devops.app.service.DevopsEnvCommandService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsPvService;
import io.choerodon.devops.app.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.PersistentVolumeType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.mapper.DevopsPvMapper;
import io.choerodon.devops.infra.util.GitOpsUtil;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Endpoints;
import io.kubernetes.client.models.V1PersistentVolume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zmf
 * @since 11/7/19
 */
@Service
public class HandlerPersistentVolumeServiceImpl implements HandlerObjectFileRelationsService<V1PersistentVolume> {
    private static final String GIT_SUFFIX = "/.git";

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsPvService devopsPvService;
    @Autowired
    private DevopsPvMapper devopsPvMapper;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync, List<V1PersistentVolume> pvs, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforePvs = beforeSync.stream()
                .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType().equals(ResourceType.PERSISTENT_VOLUME.getType()))
                .map(devopsEnvFileResourceDTO -> {
                    DevopsPvDTO devopsPvDTO = devopsPvMapper
                            .selectByPrimaryKey(devopsEnvFileResourceDTO.getResourceId());
                    if (devopsPvDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceDTO.getResourceId(), ResourceType.PERSISTENT_VOLUME.getType());
                        return null;
                    }
                    return devopsPvDTO.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        List<V1PersistentVolume> pvToAdd = new ArrayList<>();
        List<V1PersistentVolume> pvToUpdate = new ArrayList<>();

        GitOpsUtil.pickCUDResource(beforePvs, pvs, pvToAdd, pvToUpdate, pv -> pv.getMetadata().getName());

        //新增pv
        addPersistentVolumes(objectPath, envId, pvToAdd, path, userId);
        //更新pv
        updatePersistentVolumes(objectPath, envId, pvToUpdate, path, userId);
        //删除pv,和文件对象关联关系
        beforePvs.forEach(pvName -> {
            DevopsPvDTO devopsPvDTO = devopsPvService.queryByEnvIdAndName(envId, pvName);
            if (devopsPvDTO != null) {
                devopsPvService.deleteByGitOps(devopsPvDTO.getId());
                devopsEnvFileResourceService
                        .baseDeleteByEnvIdAndResourceId(envId, devopsPvDTO.getId(), ResourceType.PERSISTENT_VOLUME.getType());
            }
        });
    }

    private void updatePersistentVolumes(Map<String, String> objectPath, Long envId, List<V1PersistentVolume> updatePvs, String path, Long userId) {
        updatePvs.forEach(pv -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(pv.hashCode()));
                DevopsPvDTO devopsPvDTO = devopsPvService
                        .queryByEnvIdAndName(envId, pv.getMetadata().getName());
                //初始化pv对象参数,更新pv并更新文件对象关联关系
                DevopsPvReqVO devopsPvReqVO = constructPv(
                        pv,
                        envId, "update");

                boolean isNotChange = isIdentical(devopsPvDTO, devopsPvReqVO);
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsPvDTO.getCommandId());
                devopsPvReqVO.setId(devopsPvDTO.getId());
                if (!isNotChange) {
                    // PV不允许更改
                    throw new GitOpsExplainException(GitOpsObjectError.PERSISTENT_VOLUME_UNMODIFIED.getError(), filePath, new Object[]{devopsPvReqVO.getName()});
                }
                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, devopsPvDTO.getId(), pv.getKind());
                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                        envId,
                        devopsEnvFileResourceDTO,
                        pv.hashCode(), devopsPvDTO.getId(), pv.getKind());

            } catch (GitOpsExplainException ex) {
                throw ex;
            } catch (CommonException e) {
                throw new GitOpsExplainException(e.getMessage(), filePath, "", e.getParameters());
            }
        });
    }

    private boolean isIdentical(DevopsPvDTO dbRecord, DevopsPvReqVO update) {
        return Objects.equals(dbRecord.getAccessModes(), update.getAccessModes())
                && Objects.equals(dbRecord.getRequestResource(), update.getRequestResource())
                && Objects.equals(dbRecord.getType(), update.getType())
                && Objects.equals(dbRecord.getValueConfig(), update.getValueConfig());
    }

    private void addPersistentVolumes(Map<String, String> objectPath, Long envId, List<V1PersistentVolume> pvs, String path, Long userId) {
        pvs.forEach(pv -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(pv.hashCode()));
                DevopsPvDTO devopsPvDTO = devopsPvService
                        .queryByEnvIdAndName(envId, pv.getMetadata().getName());
                DevopsPvReqVO devopsPvReqVo;

                DevopsPvDTO newDevopsPvDTO = new DevopsPvDTO();
                //初始化pv参数,创建时判断pv是否存在，存在则直接创建文件对象关联关系
                if (devopsPvDTO == null) {
                    devopsPvReqVo = constructPv(
                            pv,
                            envId,
                            "create");
                    newDevopsPvDTO = devopsPvService.createOrUpdateByGitOps(devopsPvReqVo, userId);
                } else {
                    newDevopsPvDTO.setId(devopsPvDTO.getId());
                    newDevopsPvDTO.setCommandId(devopsPvDTO.getCommandId());
                }
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(newDevopsPvDTO.getCommandId());
                // TODO 考虑并发
                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, pv.hashCode(), newDevopsPvDTO.getId(),
                        pv.getKind());
            } catch (GitOpsExplainException ex) {
                throw ex;
            } catch (CommonException e) {
                throw new GitOpsExplainException(e.getMessage(), filePath, "", e.getParameters());
            }
        });
    }


    private DevopsPvReqVO constructPv(V1PersistentVolume pv, Long envId, String type) {
        DevopsPvReqVO devopsPvReqVO = new DevopsPvReqVO();
        devopsPvReqVO.setEnvId(envId);
        devopsPvReqVO.setName(pv.getMetadata().getName());
        devopsPvReqVO.setCommandType(type);
        // 暂时只设计为支持一种模式
        devopsPvReqVO.setAccessModes(pv.getSpec().getAccessModes().get(0));
        devopsPvReqVO.setRequestResource(pv.getSpec().getCapacity().get(KubernetesConstants.STORAGE).toSuffixedString());
        setTypeAndConfig(devopsPvReqVO, pv);
        return devopsPvReqVO;
    }

    private void setTypeAndConfig(DevopsPvReqVO devopsPvReqVO, V1PersistentVolume pv) {
        if (pv.getSpec().getHostPath() != null) {
            devopsPvReqVO.setType(PersistentVolumeType.HOST_PATH.getType());
            devopsPvReqVO.setValueConfig(new JSON().serialize(pv.getSpec().getHostPath()));
        } else if (pv.getSpec().getNfs() != null) {
            devopsPvReqVO.setType(PersistentVolumeType.NFS.getType());
            devopsPvReqVO.setValueConfig(new JSON().serialize(pv.getSpec().getNfs()));
        }
        if (pv.getSpec().getLocal() != null) {
            devopsPvReqVO.setType(PersistentVolumeType.LOCAL_PV.getType());
            LocalPvResource localPvResource = new LocalPvResource();
            localPvResource.setPath(pv.getSpec().getLocal().getPath());
            localPvResource.setNodeName(pv.getSpec()
                    .getNodeAffinity()
                    .getRequired()
                    .getNodeSelectorTerms()
                    .get(0)
                    .getMatchExpressions()
                    .get(0)
                    .getValues()
                    .get(0));
            devopsPvReqVO.setValueConfig(new JSON().serialize(localPvResource));
        }
    }

    @Override
    public Class<V1PersistentVolume> getTarget() {
        return V1PersistentVolume.class;
    }
}
