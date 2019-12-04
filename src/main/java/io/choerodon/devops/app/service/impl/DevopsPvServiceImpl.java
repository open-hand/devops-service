package io.choerodon.devops.app.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageSerializable;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.DevopsPvValidator;
import io.choerodon.devops.api.vo.DevopsPvPermissionUpdateVO;
import io.choerodon.devops.api.vo.DevopsPvReqVO;
import io.choerodon.devops.api.vo.DevopsPvVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.PersistentVolumePayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;
import io.choerodon.devops.infra.mapper.DevopsPvMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.PageInfoUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.autoconfigure.CustomPageRequest;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DevopsPvServiceImpl implements DevopsPvService {
    private static Logger LOGGER = LoggerFactory.getLogger(DevopsPvServiceImpl.class);
    private static final String PERSISTENTVOLUME = "PersistentVolume";
    private static final String PERSISTENTVOLUME_PREFIX = "pv-";
    private static final String YAML_SUFFIX = ".yaml";
    private static final String CREATE = "create";
    private static final String DELETE = "delete";
    private static final String MASTER = "master";

    @Autowired
    DevopsPvMapper devopsPvMapper;
    @Autowired
    BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    DevopsPvProPermissionService devopsPvProPermissionService;
    @Autowired
    ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    DevopsClusterService devopsClusterService;
    @Autowired
    UserAttrService userAttrService;
    @Autowired
    DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;
    @Autowired
    GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    DevopsProjectService devopsProjectService;
    @Autowired
    DevopsClusterMapper devopsClusterMapper;
    @Autowired
    TransactionalProducer producer;

    private Gson gson = new Gson();

    @Override
    public PageInfo<DevopsPvDTO> basePagePvByOptions(Long projectId, Boolean doPage, Pageable pageable, String params) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        // search_param 根据确定的键值对查询
        // params 是遍历字段模糊查询
        Map<String, Object> searchParamMap = TypeUtil.castMapParams(params);
        return PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize())
                .doSelectPageInfo(() -> devopsPvMapper.listPvByOptions(
                        projectDTO.getOrganizationId(),
                        TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))
                ));
    }

    @Override
    public PageInfo<DevopsPvVO> pageByOptions(Long projectId, Boolean doPage, Pageable pageable, String params) {
        return ConvertUtils.convertPage(basePagePvByOptions(projectId, doPage, pageable, params), DevopsPvVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPv(Long projectId, DevopsPvReqVO devopsPvReqVo) {
        DevopsPvDTO devopsPvDTO = ConvertUtils.convertObject(devopsPvReqVo, DevopsPvDTO.class);
        devopsPvDTO.setProjectId(projectId);
        devopsPvDTO.setStatus(PvStatus.OPERATING.getStatus());
        // 创建pv的环境是所选集群关联的系统环境
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(devopsPvDTO.getClusterId());

        // 如果系统环境id为空那么先去创建系统环境,更新集群关联的系统环境
        if (devopsClusterDTO.getSystemEnvId() == null) {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.createSystemEnv(devopsClusterDTO.getId());
            devopsClusterDTO.setSystemEnvId(devopsEnvironmentDTO.getId());
            devopsClusterService.baseUpdate(devopsClusterDTO);
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsClusterDTO.getSystemEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        //校验环境信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        //将pvDTO转换成pv文件对象
        V1PersistentVolume v1PersistentVolume = initV1PersistentVolume(devopsPvDTO);

        //创建pv命令
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CREATE);

        //创建pv
        operatePVGitlabFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                v1PersistentVolume, devopsPvDTO, devopsEnvCommandDTO, devopsEnvironmentDTO, userAttrDTO);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePvById(Long pvId) {
        DevopsPvDTO devopsPvDTO = baseQueryById(pvId);

        if (devopsPvDTO == null) {
            return false;
        }

        // 创建pv的环境是所选集群关联的系统环境
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(devopsPvDTO.getClusterId());
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsClusterDTO.getSystemEnvId());


        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        //校验环境信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        //创建删除命令
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(DELETE);

        devopsEnvCommandDTO.setObjectId(pvId);
        devopsPvDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        devopsPvDTO.setStatus(PvStatus.Deleting.getStatus());
        baseupdatePv(devopsPvDTO);

        // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(
                devopsEnvironmentDTO.getProjectId(),
                devopsEnvironmentDTO.getCode(),
                devopsEnvironmentDTO.getEnvIdRsa(),
                devopsEnvironmentDTO.getType(),
                devopsEnvironmentDTO.getClusterCode());


        // 查询对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), pvId, PERSISTENTVOLUME);
        if (devopsEnvFileResourceDTO == null) {
            //删除pv
            devopsPvMapper.deleteByPrimaryKey(pvId);

            //级联删除权限表中的数据
            devopsPvProPermissionService.baseDeleteByPvId(pvId);

            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    "pv-" + devopsPvDTO.getName() + ".yaml")) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        "pv-" + devopsPvDTO.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(GitUserNameUtil.getAdminId()));
            }
            return true;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                devopsPvMapper.deleteByPrimaryKey(pvId);
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
                        TypeUtil.objToInteger(GitUserNameUtil.getAdminId()));
            }
        } else {
            ResourceConvertToYamlHandler<V1PersistentVolume> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1PersistentVolume v1PersistentVolume = new V1PersistentVolume();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsPvDTO.getName());
            v1PersistentVolume.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1PersistentVolume);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(null, projectId, DELETE, (long) GitUserNameUtil.getAdminId(), pvId,
                    PERSISTENTVOLUME, null, false, devopsEnvironmentDTO.getId(), path);
        }
        return true;
    }

    @Override
    public void checkName(Long clusterId, String pvName) {
        DevopsPvDTO devopsPvDTO = new DevopsPvDTO();
        devopsPvDTO.setClusterId(clusterId);
        devopsPvDTO.setName(pvName);
        baseCheckPv(devopsPvDTO);
    }

    @Override
    public void baseCheckPv(DevopsPvDTO devopsPvDTO) {
        if (devopsPvMapper.selectOne(devopsPvDTO) != null) {
            throw new CommonException("error.pv.name.exists");
        }
    }

    @Override
    @Transactional
    public void assignPermission(DevopsPvPermissionUpdateVO update) {
        DevopsPvDTO devopsPvDTO = devopsPvMapper.selectByPrimaryKey(update.getPvId());

        if (devopsPvDTO.getSkipCheckProjectPermission()) {
            // 原来对组织下所有项目公开,更新之后依然公开，则不做任何处理
            // 更新之后对特定项目公开则忽略之前的更新权限表
            if (!update.getSkipCheckProjectPermission()) {
                // 更新相关字段
                updateCheckPermission(update);

                //批量插入
                if (!update.getProjectIds().isEmpty()) {
                    devopsPvProPermissionService.batchInsertIgnore(update.getPvId(), update.getProjectIds());
                }
            }
        } else {
            // 原来不公开,现在设置公开，更新版本号，直接删除原来的权限表中的数据
            if (update.getSkipCheckProjectPermission()) {
                // 先更新相关字段
                updateCheckPermission(update);

                //批量删除
                DevopsPvProPermissionDTO devopsPvProPermissionDTO = new DevopsPvProPermissionDTO();
                devopsPvProPermissionDTO.setPvId(update.getPvId());
                devopsPvProPermissionService.baseDeletePermission(devopsPvProPermissionDTO);
            } else {
                //原来不公开，现在也不公开，继续添加新的项目
                if (!update.getProjectIds().isEmpty()) {
                    devopsPvProPermissionService.batchInsertIgnore(update.getPvId(), update.getProjectIds());
                }
            }

        }
    }

    @Override
    public void updateCheckPermission(DevopsPvPermissionUpdateVO update) {
        DevopsPvDTO devopsPvDTO = new DevopsPvDTO();
        devopsPvDTO.setId(update.getPvId());
        devopsPvDTO.setSkipCheckProjectPermission(update.getSkipCheckProjectPermission());
        devopsPvDTO.setObjectVersionNumber(update.getObjectVersionNumber());
        devopsPvMapper.updateByPrimaryKeySelective(devopsPvDTO);
    }

    @Override
    public void baseupdatePv(DevopsPvDTO devopsPvDTO) {
        DevopsPvDTO oldDevopsPvDTO = devopsPvMapper.selectByPrimaryKey(devopsPvDTO.getId());
        if (oldDevopsPvDTO == null) {
            throw new CommonException("error.pv.not.exists");
        }

        if (devopsPvMapper.updateByPrimaryKeySelective(devopsPvDTO) != 1) {
            throw new CommonException("error.pv.update.error");
        }
    }

    @Override
    public DevopsPvVO queryById(Long pvId) {
        return ConvertUtils.convertObject(devopsPvMapper.queryById(pvId), DevopsPvVO.class);
    }

    @Override
    public DevopsPvDTO baseQueryById(Long pvId) {
        return devopsPvMapper.selectByPrimaryKey(pvId);
    }

    @Override
    public List<ProjectReqVO> listNonRelatedProjects(Long projectId, Long pvId) {
        DevopsPvDTO devopsPvDTO = baseQueryById(pvId);
        if (devopsPvDTO == null) {
            throw new CommonException("error.pv.not.exists");
        }

        CustomPageRequest customPageRequest = CustomPageRequest.of(1, 0);

        List<ProjectReqVO> projectReqVOList = Optional.ofNullable(pageProjects(projectId, pvId, customPageRequest, "{\"params\":[],\"searchParam\":{}}"))
                .map(PageSerializable::getList)
                .orElseThrow(() -> new CommonException("error.project.get"));

        //根据PvId查权限表中关联的projectId
        List<Long> permitted = devopsPvProPermissionService.baseListByPvId(pvId)
                .stream()
                .map(DevopsPvProPermissionDTO::getProjectId)
                .collect(Collectors.toList());

        //把组织下有权限的项目过滤掉再返回
        return projectReqVOList.stream()
                .filter(i -> !permitted.contains(i.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRelatedProjectById(Long pvId, Long relatedProjectId) {
        DevopsPvProPermissionDTO devopsPvProPermissionDTO = new DevopsPvProPermissionDTO();
        devopsPvProPermissionDTO.setPvId(pvId);
        devopsPvProPermissionDTO.setProjectId(relatedProjectId);
        devopsPvProPermissionService.baseDeletePermission(devopsPvProPermissionDTO);
    }

    @Override
    public DevopsPvDTO queryByEnvIdAndName(Long envId, String name) {
        return devopsPvMapper.queryByEnvIdAndName(envId, name);
    }

    @Override
    public DevopsPvDTO queryWithEnvByClusterIdAndName(Long clusterId, String name) {
        return devopsPvMapper.queryWithEnvByClusterIdAndName(clusterId, name);
    }

    @Override
    public DevopsPvDTO createOrUpdateByGitOps(DevopsPvReqVO devopsPvReqVO, Long userId) {
        // 校验环境是否连接
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsPvReqVO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        // 处理创建数据
        DevopsPvDTO devopsPvDTO = ConvertUtils.convertObject(devopsPvReqVO, DevopsPvDTO.class);
        devopsPvDTO.setStatus(PvcStatus.OPERATING.getStatus());
        devopsPvDTO.setClusterId(environmentDTO.getClusterId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(devopsPvReqVO.getCommandType());
        devopsEnvCommandDTO.setLastUpdatedBy(userId);

        if (CommandType.CREATE.getType().equals(devopsPvReqVO.getCommandType())) {
            devopsEnvCommandDTO.setCreatedBy(userId);
            Long pvId = createPvRecord(devopsPvDTO).getId();
            devopsEnvCommandDTO.setObjectId(pvId);
            devopsPvDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsPvDTO);
        } else {
            DevopsPvDTO dbRecord = devopsPvMapper.selectByPrimaryKey(devopsPvReqVO.getId());
            devopsPvDTO.setObjectVersionNumber(dbRecord.getObjectVersionNumber());
            devopsEnvCommandDTO.setObjectId(devopsPvDTO.getId());
            devopsPvDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsPvDTO);
        }

        return devopsPvDTO;
    }

    private DevopsPvDTO createPvRecord(DevopsPvDTO devopsPvDTO) {
        if (1 != devopsPvMapper.insertSelective(Objects.requireNonNull(devopsPvDTO))) {
            throw new CommonException("error.pv.insert", devopsPvDTO.getName());
        }
        return devopsPvMapper.selectByPrimaryKey(devopsPvDTO.getId());
    }

    @Override
    public void deleteByGitOps(Long pvId) {
        DevopsPvDTO devopsPvcDTO = devopsPvMapper.queryWithEnvByPrimaryKey(pvId);
        if (devopsPvcDTO == null) {
            return;
        }
        // 校验环境是否连接
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsPvcDTO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        devopsPvMapper.deleteByPrimaryKey(pvId);
        devopsEnvCommandMapper.deleteByObjectTypeAndObjectId(ObjectType.PERSISTENTVOLUMECLAIM.getType(), pvId);
        //级联删除权限表中的数据
        devopsPvProPermissionService.baseDeleteByPvId(pvId);
    }

    //跳过权限校验,PV分配的有权限的项目和集群下有权限的项目一样
    @Override
    public PageInfo<ProjectReqVO> pageProjects(Long projectId, Long pvId, Pageable pageable, String params) {
        DevopsPvDTO devopsPvDTO = baseQueryById(pvId);
        if (devopsPvDTO == null) {
            throw new CommonException("error.pv.not.exists");
        }

        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(devopsPvDTO.getClusterId());
        //集群跳过权限校验
        if (devopsClusterDTO.getSkipCheckProjectPermission()) {
            return devopsProjectService.pageProjects(projectId, pageable, params);
        } else {
            //集群不跳过权限校验
            return devopsClusterService.pageRelatedProjects(projectId, devopsClusterDTO.getId(), pageable, params);
        }
    }

    @Override
    public PageInfo<ProjectReqVO> pageRelatedProjects(Long projectId, Long pvId, Pageable pageable, String params) {
        DevopsPvDTO devopsPvDTO = baseQueryById(pvId);
        if (devopsPvDTO == null) {
            throw new CommonException("error.pv.not.exists");
        }

        Map<String, Object> map = TypeUtil.castMapParams(params);
        //接收模糊查询参数列表
        List<String> paramList = TypeUtil.cast(map.get(TypeUtil.PARAMS));

        if (CollectionUtils.isEmpty(paramList)) {
            // 如果不搜索
            PageInfo<DevopsPvProPermissionDTO> relationPage = PageHelper.startPage(
                    pageable.getPageNumber(), pageable.getPageSize())
                    .doSelectPageInfo(() -> devopsPvProPermissionService.baseListByPvId(pvId));
            return ConvertUtils.convertPage(relationPage, permission -> {
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(permission.getProjectId());
                return new ProjectReqVO(permission.getProjectId(), projectDTO.getName(), projectDTO.getCode());
            });
        } else {
            // 如果要搜索，需要手动在程序内分页
            ProjectDTO iamProjectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

            // 手动查出所有组织下的项目
            List<ProjectDTO> filteredProjects = baseServiceClientOperator.listIamProjectByOrgId(
                    iamProjectDTO.getOrganizationId(),
                    null, null,
                    paramList.get(0));

            // 数据库中的有权限的项目
            List<Long> permissions = devopsPvProPermissionService.baseListByPvId(pvId)
                    .stream()
                    .map(DevopsPvProPermissionDTO::getProjectId)
                    .collect(Collectors.toList());

            // 过滤出在数据库中有权限的项目信息
            List<ProjectReqVO> allMatched = filteredProjects
                    .stream()
                    .filter(p -> permissions.contains(p.getId()))
                    .map(p -> ConvertUtils.convertObject(p, ProjectReqVO.class))
                    .collect(Collectors.toList());

            return PageInfoUtil.createPageFromList(allMatched, pageable);
        }
    }

    @Override
    public List<DevopsPvDTO> baseListByEnvId(Long envId) {
        DevopsPvDTO searchCondition = new DevopsPvDTO();
        searchCondition.setClusterId(Objects.requireNonNull(devopsEnvironmentService.baseQueryById(envId).getClusterId()));
        return devopsPvMapper.select(searchCondition);
    }

    @Override
    public void baseUpdate(DevopsPvDTO devopsPvDTO) {
        if (devopsPvMapper.updateByPrimaryKeySelective(Objects.requireNonNull(devopsPvDTO)) != 1) {
            throw new CommonException("error.update.pv", devopsPvDTO.getName());
        }
    }

    private V1PersistentVolume initV1PersistentVolume(DevopsPvDTO devopsPvDTO) {
        V1PersistentVolume v1PersistentVolume = new V1PersistentVolume();
        v1PersistentVolume.setApiVersion("v1");
        v1PersistentVolume.setKind(PERSISTENTVOLUME);

        //设置pv名称
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(devopsPvDTO.getName());
        v1PersistentVolume.setMetadata(v1ObjectMeta);

        //设置Specification下面的字段
        V1PersistentVolumeSpec v1PersistentVolumeSpec = new V1PersistentVolumeSpec();

        //设置访问模式,目前不支持多种访问模式，所以直接从dto取值
        List<String> accessMode = new ArrayList<>();
        accessMode.add(devopsPvDTO.getAccessModes());
        v1PersistentVolumeSpec.setAccessModes(accessMode);

        //设置容量大小
        Map<String, Quantity> capacity = new HashMap<>();
        capacity.put(KubernetesConstants.STORAGE, convertResource(devopsPvDTO.getRequestResource()));
        v1PersistentVolumeSpec.setCapacity(capacity);

        //设置pv存储类型
        String volumeType = devopsPvDTO.getType().toLowerCase();
        VolumeTypeEnum volumeTypeEnum = VolumeTypeEnum.forValue(volumeType);

        //pv类型不存在抛异常
        if (volumeTypeEnum == null) {
            throw new CommonException("error.py.type.not.exist");
        }

        switch (volumeTypeEnum) {
            // 选择pv类型之后需要获取详细设置后反序列化成对象
            case NFS:
                V1NFSVolumeSource nfs = gson.fromJson(devopsPvDTO.getValueConfig(), V1NFSVolumeSource.class);
                //反序列成对象之后进行校验
                DevopsPvValidator.checkConfigValue(nfs, volumeTypeEnum);
                v1PersistentVolumeSpec.setNfs(nfs);
                break;
            case HOSTPATH:
                V1HostPathVolumeSource hostPath = gson.fromJson(devopsPvDTO.getValueConfig(), V1HostPathVolumeSource.class);
                //反序列成对象之后进行校验
                DevopsPvValidator.checkConfigValue(hostPath, volumeTypeEnum);
                v1PersistentVolumeSpec.setHostPath(hostPath);
                break;
        }
        v1PersistentVolume.setSpec(v1PersistentVolumeSpec);
        return v1PersistentVolume;
    }


    private BigDecimal convertResourceToDigits(String resourceString) {
        long size = Long.parseLong(resourceString.substring(0, resourceString.length() - 2));
        String unit = resourceString.substring(resourceString.length() - 2);
        int level = ResourceUnitLevelEnum.valueOf(unit.toUpperCase()).ordinal();

        // 1024的一次方 对应ki 1024的2次方 对应Mi 以此类推
        size = (long) (size * Math.pow(1024, level + 2));

        return new BigDecimal(size);
    }

    private Quantity convertResource(String resourceString) {

        return new Quantity(convertResourceToDigits(resourceString), Quantity.Format.BINARY_SI);
    }

    private int compareResource(String pvStorage, String pvcStorage) {
        long pvSize = Long.parseLong(pvStorage.substring(0, pvStorage.length() - 2));
        long pvcSize = Long.parseLong(pvcStorage.substring(0, pvcStorage.length() - 2));

        String pvUnit = pvStorage.substring(pvStorage.length() - 2);
        String pvcUnit = pvcStorage.substring(pvcStorage.length() - 2);

        int pvLevel = ResourceUnitLevelEnum.valueOf(pvUnit.toUpperCase()).ordinal();
        int pvcLevel = ResourceUnitLevelEnum.valueOf(pvcUnit.toUpperCase()).ordinal();

        if (pvLevel == pvcLevel) {
            return pvSize >= pvcSize ? 1 : -1;
        }
        return pvLevel > pvcLevel ? 1 : -1;
    }

    private DevopsEnvCommandDTO initDevopsEnvCommandDTO(String type) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        devopsEnvCommandDTO.setCommandType(type);
        devopsEnvCommandDTO.setObject(ObjectType.PERSISTENTVOLUME.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }


    private void operatePVGitlabFile(Integer gitlabEnvGroupProjectId, V1PersistentVolume v1PersistentVolume, DevopsPvDTO devopsPvDTO,
                                     DevopsEnvCommandDTO devopsEnvCommandDTO, DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO) {

        //数据库创建pv
        if (devopsPvMapper.insert(devopsPvDTO) != 1) {
            throw new CommonException("error.pv.create.error");
        }

        Long pvId = devopsPvDTO.getId();

        devopsEnvCommandDTO.setObjectId(pvId);

        //设置操作指令id,创建指令记录
        devopsPvDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());

        baseupdatePv(devopsPvDTO);

        PersistentVolumePayload persistentVolumePayload = new PersistentVolumePayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId());
        persistentVolumePayload.setDevopsPvDTO(devopsPvDTO);
        persistentVolumePayload.setCreated(true);
        persistentVolumePayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);
        persistentVolumePayload.setV1PersistentVolume(v1PersistentVolume);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_PERSISTENTVOLUME),
                builder -> builder
                        .withJson(gson.toJson(persistentVolumePayload))
                        .withRefId(devopsEnvironmentDTO.getId().toString()));
    }

    @Override
    public void operatePvBySaga(PersistentVolumePayload persistentVolumePayload) {
        try {
            // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String path = clusterConnectionHandler.handDevopsEnvGitRepository(persistentVolumePayload.getDevopsEnvironmentDTO().getProjectId(),
                    persistentVolumePayload.getDevopsEnvironmentDTO().getCode(),
                    persistentVolumePayload.getDevopsEnvironmentDTO().getEnvIdRsa(),
                    persistentVolumePayload.getDevopsEnvironmentDTO().getType(),
                    persistentVolumePayload.getDevopsEnvironmentDTO().getClusterCode());


            //创建文件
            ResourceConvertToYamlHandler<V1PersistentVolume> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(persistentVolumePayload.getV1PersistentVolume());
            resourceConvertToYamlHandler.operationEnvGitlabFile(PERSISTENTVOLUME_PREFIX + persistentVolumePayload.getDevopsPvDTO().getName(), persistentVolumePayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    CREATE, (long) GitUserNameUtil.getAdminId(), persistentVolumePayload.getDevopsPvDTO().getId(), PERSISTENTVOLUME, null, false,
                    persistentVolumePayload.getDevopsEnvironmentDTO().getId(), path);
        } catch (Exception e) {
            LOGGER.info("create or update PersistentVolume failed! {}", e);
            //有异常更新实例以及command的状态
            DevopsPvDTO devopsPvDTO = devopsPvMapper.selectByPrimaryKey(persistentVolumePayload.getDevopsPvDTO().getId());
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndResourceId(persistentVolumePayload.getDevopsEnvironmentDTO().getId(), devopsPvDTO.getId(), PERSISTENTVOLUME);
            String filePath = devopsEnvFileResourceDTO == null ? PERSISTENTVOLUME_PREFIX + devopsPvDTO.getName() + YAML_SUFFIX : devopsEnvFileResourceDTO.getFilePath();
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(persistentVolumePayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId()), MASTER,
                    filePath)) {
                devopsPvDTO.setStatus(CommandStatus.FAILED.getStatus());
                baseUpdate(devopsPvDTO);
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsPvDTO.getCommandId());
                devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
                devopsEnvCommandDTO.setError("create or update PV failed!");
                devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
            }
        }
    }

    @Override
    public List<DevopsPvVO> queryPvcRelatedPv(Long projectId, String params) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Map<String, Object> searchParamMap = TypeUtil.castMapParams(params);
        Map<String, String> map = (Map) searchParamMap.get(TypeUtil.SEARCH_PARAM);

        List<DevopsPvVO> devopsPvVOList = ConvertUtils.convertList(devopsPvMapper.listPvByOptions(
                projectDTO.getOrganizationId(),
                TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))), DevopsPvVO.class);

        if (devopsPvVOList == null) {
            throw new CommonException("error.pv.query");
        }

        Map<Long, List<Long>> projectIdAndPvIdsMap = new HashMap<>();

        //获得不跳过权限的与本项目有关联的pv
        projectIdAndPvIdsMap.put(projectId, devopsPvProPermissionService.baseListPvIdsByProjectId(projectId));

        devopsPvVOList.forEach(pv -> {
            CustomPageRequest customPageRequest = CustomPageRequest.of(1, 0);
            //获得跳过权限的与本项目有关联的pv
            if (pv.getSkipCheckProjectPermission()) {
                List<ProjectReqVO> list = new ArrayList<>(Optional.ofNullable(pageProjects(pv.getProjectId(), pv.getId(), customPageRequest, params).getList()).orElse(new ArrayList<>()));
                if (list.stream().map(ProjectReqVO::getId).collect(Collectors.toList()).contains(projectId)) {
                    List<Long> pvIds = Optional.ofNullable(projectIdAndPvIdsMap.get(projectId)).orElse(new ArrayList<>());
                    pvIds.add(pv.getId());
                    projectIdAndPvIdsMap.put(projectId, pvIds);
                }
            }
        });

        devopsPvVOList = ConvertUtils.convertList(devopsPvMapper.listByPvIds(projectIdAndPvIdsMap.get(projectId)), DevopsPvVO.class);

        String pvcStorage = map.get("requestResource");
        // 筛选容量大于或等于pvc容量
        if (pvcStorage != null) {
            return devopsPvVOList.stream()
                    .filter((e) -> compareResource(e.getRequestResource(), pvcStorage) > 0 && e.getPvcName() == null)
                    .collect(Collectors.toList());
        } else {
            return devopsPvVOList.stream()
                    .filter(e -> e.getPvcName() == null)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<DevopsPvDTO> queryByClusterId(Long clusterId) {
        DevopsPvDTO devopsPvDTO = new DevopsPvDTO();
        devopsPvDTO.setClusterId(clusterId);
        return devopsPvMapper.select(devopsPvDTO);
    }
}
