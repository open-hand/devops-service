package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.PVCode.DEVOPS_PV_NOT_EXISTS;

import java.math.BigDecimal;
import java.util.*;

import com.google.gson.Gson;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimSpec;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsPvcReqVO;
import io.choerodon.devops.api.vo.DevopsPvcRespVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.PersistentVolumeClaimPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvFileResourceMapper;
import io.choerodon.devops.infra.mapper.DevopsPvMapper;
import io.choerodon.devops.infra.mapper.DevopsPvcMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsPvcServiceImpl implements DevopsPvcService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsPvcServiceImpl.class);
    private static final String PERSISTENTVOLUMECLAIM = "PersistentVolumeClaim";
    private static final String PERSISTENTVOLUMECLAIM_PREFIX = "pvc-";
    private static final String YAML_SUFFIX = ".yaml";
    private static final String MASTER = "master";
    private final Gson gson = new Gson();
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsPvcMapper devopsPvcMapper;
    @Autowired
    private DevopsPvMapper devopsPvMapper;
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper;
    @Autowired
    private SendNotificationService sendNotificationService;
    @Autowired
    private PermissionHelper permissionHelper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_PERSISTENTVOLUMECLAIM,
            description = "Devops创建PVC", inputSchema = "{}")
    public DevopsPvcRespVO create(Long projectId, DevopsPvcReqVO devopsPvcReqVO) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, devopsPvcReqVO.getEnvId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        // 处理PVC对象
        DevopsPvcDTO devopsPvcDTO = handlePvc(devopsPvcReqVO, projectId);
        //初始化V1PersistentVolumeClaim对象
        V1PersistentVolumeClaim v1PVC = initV1PersistentVolumeClaim(devopsPvcDTO);

        DevopsEnvCommandDTO devopsEnvCommand = initDevopsEnvCommandDTO(CommandType.CREATE.getType());

        // 在gitops库处理pvc文件
        operateEnvGitLabFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), v1PVC, devopsPvcDTO,
                devopsEnvCommand, userAttrDTO);
        DevopsPvcRespVO devopsPvcRespVO = ConvertUtils.convertObject(devopsPvcDTO, DevopsPvcRespVO.class);
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
    public boolean delete(Long projectId, Long envId, Long pvcId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);

        DevopsPvcDTO devopsPvcDTO = devopsPvcMapper.selectByPrimaryKey(pvcId);

        if (devopsPvcDTO == null) {
            return false;
        }

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CommandType.DELETE.getType());

        // 更新pvc
        devopsEnvCommandDTO.setObjectId(pvcId);
        devopsPvcDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        devopsPvcDTO.setStatus(PvcStatus.DELETING.getStatus());
        baseUpdate(devopsPvcDTO);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvironmentDTO.getType(), devopsEnvironmentDTO.getClusterCode());

        // 查询对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(),
                        pvcId, ResourceType.PERSISTENT_VOLUME_CLAIM.getType());
        if (devopsEnvFileResourceDTO == null) {
            devopsPvcMapper.deleteByPrimaryKey(pvcId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), GitOpsConstants.MASTER,
                    PERSISTENTVOLUMECLAIM_PREFIX + devopsPvcDTO.getName() + YAML_SUFFIX)) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        PERSISTENTVOLUMECLAIM_PREFIX + devopsPvcDTO.getName() + YAML_SUFFIX,
                        String.format("delete: %s", PERSISTENTVOLUMECLAIM_PREFIX + devopsPvcDTO.getName() + YAML_SUFFIX),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "master");
            }
            return true;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), GitOpsConstants.MASTER,
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
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), GitOpsConstants.MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(), String.format("delete: %s", devopsEnvFileResourceDTO.getFilePath()),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "master");
            }
        } else {
            ResourceConvertToYamlHandler<V1PersistentVolumeClaim> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1PersistentVolumeClaim v1PersistentVolumeClaim = new V1PersistentVolumeClaim();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsPvcDTO.getName());
            v1PersistentVolumeClaim.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1PersistentVolumeClaim);
            Integer gitlabEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(null, gitlabEnvProjectId,
                    CommandType.DELETE.getType(), userAttrDTO.getGitlabUserId(),
                    pvcId, ResourceType.PERSISTENT_VOLUME_CLAIM.getType(),
                    null, false, devopsEnvironmentDTO.getId(), path);
        }
        //删除成功发送webhook josn
        sendNotificationService.sendWhenPVCResource(devopsPvcDTO, devopsEnvironmentDTO, SendSettingEnum.DELETE_RESOURCE.value());
        return true;
    }

    @Override
    public Page<DevopsPvcRespVO> pageByOptions(Long projectId, Long envId, PageRequest pageable, String params) {
        Map maps = gson.fromJson(params, Map.class);
        return ConvertUtils.convertPage(PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> devopsPvcMapper.listByOption(envId,
                TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(maps.get(TypeUtil.PARAMS)))), DevopsPvcRespVO.class);

    }

    @Override
    public void baseCheckName(String pvcName, Long envId) {
        if (!isNameUnique(pvcName, envId)) {
            throw new CommonException("devops.pvc.name.already.exists");
        }
    }

    @Override
    public boolean isNameUnique(String pvcName, Long envId) {
        DevopsPvcDTO devopsPvcDTO = new DevopsPvcDTO();
        devopsPvcDTO.setName(pvcName);
        devopsPvcDTO.setEnvId(envId);
        return devopsPvcMapper.selectCount(devopsPvcDTO) == 0;
    }

    @Override
    public DevopsPvcDTO queryByEnvIdAndName(Long envId, String name) {
        DevopsPvcDTO devopsPvcDTO = new DevopsPvcDTO();
        devopsPvcDTO.setName(name);
        devopsPvcDTO.setEnvId(envId);
        return devopsPvcMapper.selectOne(devopsPvcDTO);
    }

    @Override
    public DevopsPvcDTO createOrUpdateByGitOps(Long userId, DevopsPvcReqVO devopsPvcReqVO) {
        // 校验环境是否连接
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsPvcReqVO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        LOGGER.debug("Create or update pvc. name: {}", devopsPvcReqVO.getName());

        // 处理创建数据
        DevopsPvcDTO devopsPvcDTO = voToDto(devopsPvcReqVO, environmentDTO.getProjectId());
        devopsPvcDTO.setStatus(PvcStatus.OPERATING.getStatus());
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(devopsPvcReqVO.getCommandType());
        devopsEnvCommandDTO.setLastUpdatedBy(userId);

        if (CommandType.CREATE.getType().equals(devopsPvcReqVO.getCommandType())) {
            devopsEnvCommandDTO.setCreatedBy(userId);
            devopsPvcDTO.setUsed(0);
            Long pvcId = createPvcRecord(devopsPvcDTO).getId();
            devopsEnvCommandDTO.setObjectId(pvcId);
            devopsPvcDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsPvcDTO);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsPvcDTO.getId());
            devopsPvcDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsPvcDTO);
        }
        return devopsPvcDTO;
    }

    @Override
    public void deleteByGitOps(Long pvcId) {
        DevopsPvcDTO devopsPvcDTO = devopsPvcMapper.selectByPrimaryKey(pvcId);
        if (devopsPvcDTO == null) {
            return;
        }
        // 校验环境是否连接
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsPvcDTO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        devopsPvcMapper.deleteByPrimaryKey(pvcId);
        devopsEnvCommandMapper.deleteByObjectTypeAndObjectId(ObjectType.PERSISTENTVOLUMECLAIM.getType(), pvcId);
    }

    private DevopsPvcDTO createPvcRecord(DevopsPvcDTO devopsPvcDTO) {
        if (devopsPvcMapper.insert(devopsPvcDTO) != 1) {
            throw new CommonException("devops.insert.pvc", devopsPvcDTO.getName());
        }
        return devopsPvcDTO;
    }

    private static DevopsPvcDTO voToDto(DevopsPvcReqVO devopsPvcReqVO, Long projectId) {
        DevopsPvcDTO devopsPvcDTO = new DevopsPvcDTO();
        BeanUtils.copyProperties(devopsPvcReqVO, devopsPvcDTO);
        devopsPvcDTO.setProjectId(projectId);
        return devopsPvcDTO;
    }

    @Override
    public void baseUpdate(DevopsPvcDTO devopsPvcDTO) {
        DevopsPvcDTO oldDevopsPvcDTO = devopsPvcMapper.selectByPrimaryKey(devopsPvcDTO.getId());
        if (oldDevopsPvcDTO == null) {
            throw new CommonException("devops.pvc.not.exists");
        }
        devopsPvcDTO.setObjectVersionNumber(oldDevopsPvcDTO.getObjectVersionNumber());
        if (devopsPvcMapper.updateByPrimaryKeySelective(devopsPvcDTO) != 1) {
            throw new CommonException("devops.pvc.update.error");
        }
    }

    @Override
    public void setUsed(Long envId, String pvcName) {
        DevopsPvcDTO searchDTO = new DevopsPvcDTO();
        searchDTO.setEnvId(envId);
        searchDTO.setName(pvcName);
        DevopsPvcDTO devopsPvcDTO = devopsPvcMapper.selectOne(searchDTO);
        devopsPvcDTO.setUsed(1);
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsPvcMapper, devopsPvcDTO, "devops.update.pvc.used");
    }

    @Override
    public DevopsPvcDTO queryById(Long pvcId) {
        return devopsPvcMapper.selectByPrimaryKey(pvcId);
    }

    @Override
    public List<DevopsPvcDTO> baseListByEnvId(Long envId) {
        DevopsPvcDTO searchCondition = new DevopsPvcDTO();
        searchCondition.setEnvId(Objects.requireNonNull(envId));
        return devopsPvcMapper.select(searchCondition);
    }

    @Override
    public DevopsPvcDTO queryByPvId(Long pvId) {
        if (pvId == null) {
            throw new CommonException("devops.pv.id.null");
        }
        DevopsPvcDTO devopsPvcDTO = new DevopsPvcDTO();
        devopsPvcDTO.setPvId(pvId);
        return devopsPvcMapper.selectOne(devopsPvcDTO);
    }

    private DevopsPvcDTO handlePvc(DevopsPvcReqVO devopsPvcReqVO, Long projectId) {
        baseCheckName(devopsPvcReqVO.getName(), devopsPvcReqVO.getEnvId());

        DevopsPvcDTO devopsPvcDTO = voToDto(devopsPvcReqVO, projectId);

        devopsPvcDTO.setStatus(PvcStatus.OPERATING.getStatus());
        DevopsPvDTO devopsPvDTO;
        // 根据PV的id查询PV，若果PV不存在，就根据PV的name和集群id查询，如果PV对象为空，抛出异常
        if (devopsPvcReqVO.getPvId() != null) {
            devopsPvDTO = Optional.ofNullable(devopsPvMapper.selectByPrimaryKey(devopsPvcReqVO.getPvId())).
                    orElseThrow(() -> new CommonException(DEVOPS_PV_NOT_EXISTS));
        } else {
            devopsPvDTO = Optional.ofNullable(devopsPvMapper.queryByNameAndClusterId(devopsPvcReqVO.getPvName(), devopsPvcReqVO.getClusterId()))
                    .orElseThrow(() -> new CommonException(DEVOPS_PV_NOT_EXISTS));
        }

        if (devopsPvDTO.getPvcName() != null) {
            throw new CommonException("devops.pv.bound");
        }

        devopsPvcDTO.setPvName(devopsPvDTO.getName());
        devopsPvcDTO.setAccessModes(devopsPvDTO.getAccessModes());
        // 未使用状态
        devopsPvcDTO.setUsed(0);
        return devopsPvcDTO;
    }

    private void operateEnvGitLabFile(Integer gitlabEnvGroupProjectId, V1PersistentVolumeClaim v1PersistentVolumeClaim, DevopsPvcDTO devopsPvcDTO,
                                      DevopsEnvCommandDTO devopsEnvCommandDTO, UserAttrDTO userAttrDTO) {

        //获得环境
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsPvcDTO.getEnvId());

        //插入pvcDTO对象
        devopsPvcMapper.insert(devopsPvcDTO);

        Long pvcId = devopsPvcDTO.getId();

        devopsEnvCommandDTO.setObjectId(pvcId);

        devopsPvcDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());

        baseUpdate(devopsPvcDTO);

        PersistentVolumeClaimPayload persistentVolumeClaimPayload = new PersistentVolumeClaimPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId());
        persistentVolumeClaimPayload.setDevopsPvcDTO(devopsPvcDTO);
        persistentVolumeClaimPayload.setCreated(true);
        persistentVolumeClaimPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);
        persistentVolumeClaimPayload.setV1PersistentVolumeClaim(v1PersistentVolumeClaim);
        persistentVolumeClaimPayload.setType(devopsEnvCommandDTO.getCommandType());

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(devopsEnvironmentDTO.getProjectId())
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_PERSISTENTVOLUMECLAIM),
                builder -> builder
                        .withJson(gson.toJson(persistentVolumeClaimPayload))
                        .withRefId(devopsEnvironmentDTO.getId().toString()));
    }

    @Override
    public void operatePvcBySaga(PersistentVolumeClaimPayload persistentVolumeClaimPayload) {
        try {
            // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String path = clusterConnectionHandler.handDevopsEnvGitRepository(persistentVolumeClaimPayload.getProjectId(),
                    persistentVolumeClaimPayload.getDevopsEnvironmentDTO().getCode(),
                    persistentVolumeClaimPayload.getDevopsEnvironmentDTO().getId(),
                    persistentVolumeClaimPayload.getDevopsEnvironmentDTO().getEnvIdRsa(),
                    persistentVolumeClaimPayload.getDevopsEnvironmentDTO().getType(),
                    persistentVolumeClaimPayload.getDevopsEnvironmentDTO().getClusterCode());

            ResourceConvertToYamlHandler<V1PersistentVolumeClaim> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(persistentVolumeClaimPayload.getV1PersistentVolumeClaim());
            resourceConvertToYamlHandler.operationEnvGitlabFile(PERSISTENTVOLUMECLAIM_PREFIX + persistentVolumeClaimPayload.getDevopsPvcDTO().getName(), persistentVolumeClaimPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    CommandType.CREATE.getType(), persistentVolumeClaimPayload.getGitlabUserId(), persistentVolumeClaimPayload.getDevopsPvcDTO().getId(),
                    ResourceType.PERSISTENT_VOLUME_CLAIM.getType(), null, false,
                    persistentVolumeClaimPayload.getDevopsPvcDTO().getEnvId(), path);
            //创建PVC资源成功发送webhook json
            sendNotificationService.sendWhenPVCResource(persistentVolumeClaimPayload.getDevopsPvcDTO(), persistentVolumeClaimPayload.getDevopsEnvironmentDTO(), SendSettingEnum.CREATE_RESOURCE.value());
        } catch (Exception e) {
            LOGGER.info("create or update PersistentVolumeClaim failed! {}", e.getMessage());
            //有异常更新实例以及command的状态
            DevopsPvcDTO devopsPvcDTO = devopsPvcMapper.selectByPrimaryKey(persistentVolumeClaimPayload.getDevopsPvcDTO().getId());
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndResourceId(persistentVolumeClaimPayload.getDevopsEnvironmentDTO().getId(), devopsPvcDTO.getId(), PERSISTENTVOLUMECLAIM);
            String filePath = devopsEnvFileResourceDTO == null ? PERSISTENTVOLUMECLAIM_PREFIX + devopsPvcDTO.getName() + YAML_SUFFIX : devopsEnvFileResourceDTO.getFilePath();
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(persistentVolumeClaimPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId()), MASTER,
                    filePath)) {
                devopsPvcDTO.setStatus(PvcStatus.FAILED.getStatus());
                baseUpdate(devopsPvcDTO);
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsPvcDTO.getCommandId());
                devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
                devopsEnvCommandDTO.setError("create or update PVC failed!");
                devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
            }
            //创建PVC资源失败，发送webhook json
            sendNotificationService.sendWhenPVCResource(persistentVolumeClaimPayload.getDevopsPvcDTO(), persistentVolumeClaimPayload.getDevopsEnvironmentDTO(), SendSettingEnum.CREATE_RESOURCE_FAILED.value());
        }
    }

    private V1PersistentVolumeClaim initV1PersistentVolumeClaim(DevopsPvcDTO devopsPvcDTO) {
        V1PersistentVolumeClaim v1PersistentVolumeClaim = new V1PersistentVolumeClaim();
        v1PersistentVolumeClaim.setApiVersion("v1");
        v1PersistentVolumeClaim.setKind(ResourceType.PERSISTENT_VOLUME_CLAIM.getType());

        //设置PVC名称
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsPvcDTO.getName());

        //设置pvc需要绑定的PV名称和资源
        v1PersistentVolumeClaim.setMetadata(metadata);

        //设置PVC的访问模式
        List<String> accessModesList = new ArrayList<>();
        accessModesList.add(devopsPvcDTO.getAccessModes());

        V1PersistentVolumeClaimSpec v1PersistentVolumeClaimSpec = new V1PersistentVolumeClaimSpec();
        v1PersistentVolumeClaimSpec.setVolumeName(devopsPvcDTO.getPvName());
        v1PersistentVolumeClaimSpec.setAccessModes(accessModesList);
        v1PersistentVolumeClaimSpec.setResources(getResource(devopsPvcDTO.getRequestResource()));
        v1PersistentVolumeClaim.setSpec(v1PersistentVolumeClaimSpec);

        return v1PersistentVolumeClaim;
    }

    private V1ResourceRequirements getResource(String resourceString) {

        long size = Long.parseLong(resourceString.substring(0, resourceString.length() - 2));
        String unit = resourceString.substring(resourceString.length() - 2);
        int level = ResourceUnitLevelEnum.valueOf(unit.toUpperCase()).ordinal();

        // 1024的一次方 对应ki 1024的2次方 对应Mi 以此类推
        size = (long) (size * Math.pow(1024, (double) level + 2));

        BigDecimal bigDecimal = new BigDecimal(size);
        Quantity quantity = new Quantity(bigDecimal, Quantity.Format.BINARY_SI);

        V1ResourceRequirements resourceRequirements = new V1ResourceRequirements();
        Map<String, Quantity> requestResource = new HashMap<>();
        requestResource.put(KubernetesConstants.STORAGE, quantity);
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

    @Override
    public void baseDeleteByEnvId(Long envId) {
        DevopsPvcDTO devopsPvcDTO = new DevopsPvcDTO();
        devopsPvcDTO.setEnvId(envId);
        devopsPvcMapper.delete(devopsPvcDTO);
    }

    // 新启动一个事务
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void retryPushPvcToGitLab(Long pvcId) {
        DevopsPvcDTO devopsPvcDTO = devopsPvcMapper.selectByPrimaryKey(pvcId);
        if (devopsPvcDTO == null) {
            LOGGER.info("The pvc {} to be retried doesn't exist in database", pvcId);
            return;
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsPvcDTO.getEnvId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        // 判断是否解析过了
        if (devopsEnvFileResourceMapper.countRecords(devopsEnvironmentDTO.getId(), ResourceType.PERSISTENT_VOLUME_CLAIM.getType(), pvcId) > 0) {
            LOGGER.info("Retry pushing instance: the instance with code {} has passed the GitOps flow since the env-file-resource record exists", devopsPvcDTO.getName());
            return;
        }

        // 判断远程是否存在pvc对应的文件，存在就return
        String remoteFileName = PERSISTENTVOLUMECLAIM_PREFIX + devopsPvcDTO.getName() + GitOpsConstants.YAML_FILE_SUFFIX;
        if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), GitOpsConstants.MASTER, remoteFileName)) {
            LOGGER.info("Pvc {} isn't necessary to retry to push to gitlab since corresponding remote file {} exists.", devopsPvcDTO.getName(), remoteFileName);
            return;
        }

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsPvcDTO.getCommandId());
        if (StringUtils.hasText(devopsEnvCommandDTO.getSha())) {
            LOGGER.info("Retry pushing pvc: it seems that this pvc had passed the GitOps flow due to the command sha {}", devopsEnvCommandDTO.getSha());
            return;
        }

        // 将PVC的状态置为初始状态
        if (!Objects.equals(PvcStatus.OPERATING.getStatus(), devopsPvcDTO.getStatus())
                && !Objects.equals(PvcStatus.FAILED.getStatus(), devopsPvcDTO.getStatus())) {
            LOGGER.info("Retry pushing pvc: unexpected status {} for pvc", devopsPvcDTO.getStatus());
            return;
        } else {
            devopsPvcMapper.updateStatusById(pvcId, PvcStatus.OPERATING.getStatus());
        }

        // 将command的状态置为初始状态
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setError(null);
        devopsEnvCommandMapper.updateByPrimaryKey(devopsEnvCommandDTO);

        // 初始化V1PersistentVolumeClaim对象
        V1PersistentVolumeClaim v1PersistentVolumeClaim = initV1PersistentVolumeClaim(devopsPvcDTO);

        PersistentVolumeClaimPayload persistentVolumeClaimPayload = new PersistentVolumeClaimPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId());
        persistentVolumeClaimPayload.setDevopsPvcDTO(devopsPvcDTO);
        persistentVolumeClaimPayload.setCreated(true);
        persistentVolumeClaimPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);
        persistentVolumeClaimPayload.setV1PersistentVolumeClaim(v1PersistentVolumeClaim);

        // 重试不需要考虑saga在事务回滚的情况下仍然发出的情况
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(devopsEnvironmentDTO.getProjectId())
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_PERSISTENTVOLUMECLAIM),
                builder -> builder
                        .withPayloadAndSerialize(persistentVolumeClaimPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));
    }
}
