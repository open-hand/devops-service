package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsPvcReqVO;
import io.choerodon.devops.api.vo.DevopsPvcRespVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.PvcStatus;
import io.choerodon.devops.infra.enums.ResourceUnitLevelEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsPvMapper;
import io.choerodon.devops.infra.mapper.DevopsPvcMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.ResourceCreatorInfoUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1PersistentVolumeClaimSpec;
import io.kubernetes.client.models.V1ResourceRequirements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DevopsPvcServiceImpl implements DevopsPvcService {

    private static final String CREATE = "create";
    private static final String DELETE = "delete";
    private static final String MASTER = "master";

    private static final String PERSISTENTVOLUMECLAIM = "PersistentVolumeClaim";

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsPvcMapper devopsPvcMapper;
    @Autowired
    private DevopsPvMapper devopsPvMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DevopsPvcRespVO create(Long projectId, DevopsPvcReqVO devopsPvcReqVO) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsPvcReqVO.getEnvId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        // 处理PVC对象
        DevopsPvcDTO devopsPvcDTO = handlePvc(devopsPvcReqVO, projectId);
        //初始化V1PersistentVolumeClaim对象
        V1PersistentVolumeClaim v1PVC = initV1PersistentVolumeClaim(devopsPvcDTO);

        DevopsEnvCommandDTO devopsEnvCommandE = initDevopsEnvCommandDTO(CREATE);

        // 在gitops库处理pvc文件
        operateEnvGitLabFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), v1PVC, devopsPvcDTO,
                devopsEnvCommandE, userAttrDTO);
        DevopsPvcRespVO devopsPvcRespVO = ConvertUtils.convertObject(devopsPvcDTO.getId(), DevopsPvcRespVO.class);
        if (devopsPvcDTO.getCreatedBy() != null && devopsPvcDTO.getCreatedBy() != 0) {
            devopsPvcRespVO.setCreatorName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsPvcDTO.getCreatedBy()));
        }
        if (devopsPvcDTO.getLastUpdatedBy() != null && devopsPvcDTO.getLastUpdatedBy() != 0) {
            devopsPvcRespVO.setLastUpdaterName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsPvcDTO.getLastUpdatedBy()));
        }
        return devopsPvcRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long envId, Long pvcId) {
        DevopsPvcDTO devopsPvcDTO = devopsPvcMapper.selectByPrimaryKey(pvcId);

        if (devopsPvcDTO == null) {
            return false;
        }

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(DELETE);

        // 更新pvc
        devopsEnvCommandDTO.setObjectId(pvcId);
        devopsPvcDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsPvcDTO);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());

        // 查询对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), pvcId, PERSISTENTVOLUMECLAIM);
        if (devopsEnvFileResourceDTO == null) {
            devopsPvcMapper.deleteByPrimaryKey(pvcId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    "pvc-" + devopsPvcDTO.getName() + ".yaml")) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        "pvc-" + devopsPvcDTO.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
            return true;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                devopsPvcMapper.deleteByPrimaryKey(pvcId);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return true;
            }
        }
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService
                .baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());

        // 如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceDTOS.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(), "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        } else {
            ResourceConvertToYamlHandler<V1PersistentVolumeClaim> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1PersistentVolumeClaim v1PersistentVolumeClaim = new V1PersistentVolumeClaim();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsPvcDTO.getName());
            v1PersistentVolumeClaim.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1PersistentVolumeClaim);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(null, projectId, DELETE, userAttrDTO.getGitlabUserId(), pvcId,
                    PERSISTENTVOLUMECLAIM, null, false, devopsEnvironmentDTO.getId(), path);
        }
        return true;
    }

    @Override
    public void baseCheckName(String pvcName, Long envId) {
        if (queryByEnvIdAndName(envId, pvcName) != null) {
            throw new CommonException("error.pvc.name.already.exists");
        }
    }

    @Override
    public DevopsPvcDTO queryByEnvIdAndName(Long envId, String name) {
        DevopsPvcDTO devopsPvcDTO = new DevopsPvcDTO();
        devopsPvcDTO.setName(name);
        devopsPvcDTO.setEnvId(envId);
        return devopsPvcMapper.selectOne(devopsPvcDTO);
    }

    @Override
    public DevopsPvcDTO createOrUpdateByGitOps(Long envId, DevopsPvcReqVO devopsPvcReqVO) {
        // TODO by zmf
        return null;
    }

    @Override
    public void deleteByGitOps(Long pvcId) {
        // TODO by zmf
    }

    private static DevopsPvcDTO voToDto(DevopsPvcReqVO devopsPvcReqVO, Long projectId) {
        DevopsPvcDTO devopsPvcDTO = new DevopsPvcDTO();
        BeanUtils.copyProperties(devopsPvcReqVO, devopsPvcDTO);
        devopsPvcDTO.setProjectId(projectId);
        return devopsPvcDTO;
    }

    public void baseUpdate(DevopsPvcDTO devopsPvcDTO) {
        DevopsPvcDTO oldDevopsPvcDTO = devopsPvcMapper.selectByPrimaryKey(devopsPvcDTO.getId());
        if (oldDevopsPvcDTO == null) {
            throw new CommonException("pvc.not.exists");
        }
        devopsPvcDTO.setObjectVersionNumber(oldDevopsPvcDTO.getObjectVersionNumber());
        if (devopsPvcMapper.updateByPrimaryKeySelective(devopsPvcDTO) != 1) {
            throw new CommonException("pvc.update.error");
        }
    }

    private DevopsPvcDTO handlePvc(DevopsPvcReqVO devopsPvcReqVO, Long projectId) {
        baseCheckName(devopsPvcReqVO.getName(), devopsPvcReqVO.getEnvId());

        DevopsPvcDTO devopsPvcDTO = voToDto(devopsPvcReqVO, projectId);

        devopsPvcDTO.setCommandStatus(PvcStatus.PENDING.getStatus());

        DevopsPvDTO devopsPvDTO = devopsPvMapper.selectByPrimaryKey(devopsPvcReqVO.getPvId());
        if (devopsPvDTO == null) {
            throw new CommonException("error.pv.exists");
        }
        devopsPvcDTO.setPvName(devopsPvDTO.getName());
        return devopsPvcDTO;
    }

    private void operateEnvGitLabFile(Integer gitlabEnvGroupProjectId, V1PersistentVolumeClaim v1PersistentVolumeClaim, DevopsPvcDTO devopsPvcDTO,
                                      DevopsEnvCommandDTO devopsEnvCommandDTO, UserAttrDTO userAttrDTO) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsPvcDTO.getEnvId());

        devopsPvcMapper.insert(devopsPvcDTO);

        Long pvcId = devopsPvcDTO.getId();

        devopsEnvCommandDTO.setObjectId(pvcId);

        devopsPvcDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());

        baseUpdate(devopsPvcDTO);

        // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());

        ResourceConvertToYamlHandler<V1PersistentVolumeClaim> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(v1PersistentVolumeClaim);
        //TODO gitlabOperation
        resourceConvertToYamlHandler.operationEnvGitlabFile("pvc-" + devopsPvcDTO.getName(), gitlabEnvGroupProjectId,
                CREATE, userAttrDTO.getGitlabUserId(), devopsPvcDTO.getId(), PERSISTENTVOLUMECLAIM, null, false,
                devopsPvcDTO.getEnvId(), path);
    }

    private V1PersistentVolumeClaim initV1PersistentVolumeClaim(DevopsPvcDTO devopsPvcDTO) {
        V1PersistentVolumeClaim v1PersistentVolumeClaim = new V1PersistentVolumeClaim();
        v1PersistentVolumeClaim.setApiVersion("v1");
        v1PersistentVolumeClaim.setKind(PERSISTENTVOLUMECLAIM);

        //设置PVC名称
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsPvcDTO.getName());

        //设置pvc需要绑定的PV名称和资源
        v1PersistentVolumeClaim.setMetadata(metadata);
        V1PersistentVolumeClaimSpec v1PersistentVolumeClaimSpec = new V1PersistentVolumeClaimSpec();
        v1PersistentVolumeClaimSpec.setVolumeName(devopsPvcDTO.getPvName());
        v1PersistentVolumeClaimSpec.setResources(getResource(devopsPvcDTO.getRequestResource()));

        return v1PersistentVolumeClaim;
    }

    private V1ResourceRequirements getResource(String resourceString) {

        long size = Long.parseLong(resourceString.substring(0, resourceString.length() - 2));
        String unit = resourceString.substring(resourceString.length() - 2);
        int level = ResourceUnitLevelEnum.valueOf(unit).ordinal();

        for (int i = 0; i < level; i++) {
            size *= 1024;
        }

        BigDecimal bigDecimal = new BigDecimal(size);
        Quantity quantity = new Quantity(bigDecimal, Quantity.Format.BINARY_SI);

        V1ResourceRequirements resourceRequirements = new V1ResourceRequirements();
        Map<String, Quantity> requestResource = new HashMap<>();
        requestResource.put("storage", quantity);
        resourceRequirements.setRequests(requestResource);
        return resourceRequirements;
    }

    private DevopsEnvCommandDTO initDevopsEnvCommandDTO(String type) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        devopsEnvCommandDTO.setCommandType(type);
        devopsEnvCommandDTO.setObject(ObjectType.PERSISTENTVOLUMECLAIM.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }
}
