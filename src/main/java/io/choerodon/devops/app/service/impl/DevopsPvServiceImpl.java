package io.choerodon.devops.app.service.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.DevopsPvValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.LocalPvResource;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.PersistentVolumePayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;
import io.choerodon.devops.infra.mapper.DevopsPrometheusMapper;
import io.choerodon.devops.infra.mapper.DevopsPvMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsPvServiceImpl implements DevopsPvService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsPvServiceImpl.class);
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
    @Autowired
    DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    DevopsPrometheusMapper devopsPrometheusMapper;
    @Autowired
    PermissionHelper permissionHelper;

    private final Gson gson = new Gson();

    @Override
    public Page<DevopsPvDTO> basePagePvByOptions(Long projectId, PageRequest pageable, String params) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        // search_param 根据确定的键值对查询
        // params 是遍历字段模糊查询
        Map<String, Object> searchParamMap = TypeUtil.castMapParams(params);
        String orderBy = PageRequestUtil.getOrderBy(pageable);
        Page<DevopsPvDTO> pvDTOPageInfo = PageHelper.doPage(pageable, () -> devopsPvMapper.listPvByOptions(
                projectDTO.getOrganizationId(),
                projectId,
                null,
                orderBy,
                TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))
        ));

        List<Long> updatedClusterList = clusterConnectionHandler.getUpdatedClusterList();
        pvDTOPageInfo.getContent().forEach(i -> i.setClusterConnect(updatedClusterList.contains(i.getClusterId())));
        return pvDTOPageInfo;
    }

    @Override
    public Page<DevopsPvVO> pageByOptions(Long projectId, PageRequest pageable, String params) {
        return ConvertUtils.convertPage(basePagePvByOptions(projectId, pageable, params), DevopsPvVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPv(Long projectId, DevopsPvReqVO devopsPvReqVo) {
        // valueConfig直接复制了，在initV1PersistentVolume方法中对其进行再次处理并设置对应类型的值
        DevopsPvDTO devopsPvDTO = ConvertUtils.convertObject(devopsPvReqVo, DevopsPvDTO.class);
        devopsPvDTO.setProjectId(projectId);
        devopsPvDTO.setStatus(PvStatus.OPERATING.getStatus());
        // 创建pv的环境是所选集群关联的系统环境
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(devopsPvDTO.getClusterId());
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        // 如果系统环境id为空那么先去创建系统环境,更新集群关联的系统环境
        if (devopsClusterDTO.getSystemEnvId() == null) {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.createSystemEnv(devopsClusterDTO.getId());
            devopsClusterDTO.setSystemEnvId(devopsEnvironmentDTO.getId());
            devopsClusterService.baseUpdate(null, devopsClusterDTO);
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsClusterDTO.getSystemEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        //校验环境信息
        checkEnv(devopsEnvironmentDTO);

        // 根据pv对象初始化Kubernetes对象，同时对传入的对象的valueConfig字段根据type更改
        V1PersistentVolume v1PersistentVolume = initV1PersistentVolume(devopsPvDTO);

        //创建pv命令
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(CREATE);

        //创建pv
        operatePVGitlabFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                v1PersistentVolume, devopsPvDTO, devopsEnvCommandDTO, devopsEnvironmentDTO, userAttrDTO);

        DevopsPvPermissionUpdateVO permissionUpdateVO = new DevopsPvPermissionUpdateVO();
        permissionUpdateVO.setPvId(devopsPvDTO.getId());
        permissionUpdateVO.setProjectIds(devopsPvReqVo.getProjectIds());
        permissionUpdateVO.setSkipCheckProjectPermission(devopsPvReqVo.getSkipCheckProjectPermission());
        permissionUpdateVO.setObjectVersionNumber(devopsPvReqVo.getObjectVersionNumber());
        assignPermission(null, permissionUpdateVO);
    }

    private void checkEnv(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        //校验用户是否有环境的权限
        devopsEnvUserPermissionService.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), devopsEnvironmentDTO.getId());

        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePvById(Long projectId, Long pvId) {

        DevopsPvDTO devopsPvDTO = baseQueryById(pvId);

        if (devopsPvDTO == null) {
            return false;
        }

        // 如果删除的PV与Prometheus进行了绑定且PV状态为Available，则抛出异常，终止删除操作
        List<Long> boundPvIds = getBoundPvIds(devopsPrometheusMapper, 1);

        if ("Available".equals(devopsPvDTO.getStatus()) && boundPvIds.contains(pvId)) {
            throw new CommonException("error.pv.bound.with.prometheus");
        }

        // 创建pv的环境是所选集群关联的系统环境
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(devopsPvDTO.getClusterId());

        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsClusterDTO.getSystemEnvId());


        //校验环境信息
        checkEnv(devopsEnvironmentDTO);

        //创建删除命令
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(DELETE);

        devopsEnvCommandDTO.setObjectId(pvId);
        devopsPvDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        devopsPvDTO.setStatus(PvStatus.DELETING.getStatus());
        baseupdatePv(devopsPvDTO);

        // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(
                devopsEnvironmentDTO,
                devopsEnvironmentDTO.getProjectId(),
                devopsEnvironmentDTO.getCode(),
                devopsEnvironmentDTO.getId(),
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
                    PERSISTENTVOLUME_PREFIX + devopsPvDTO.getName() + YAML_SUFFIX)) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        PERSISTENTVOLUME_PREFIX + devopsPvDTO.getName() + YAML_SUFFIX,
                        "DELETE FILE",
                        TypeUtil.objToInteger(GitUserNameUtil.getAdminId()), "master");
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
                        TypeUtil.objToInteger(GitUserNameUtil.getAdminId()), "master");
            }
        } else {
            ResourceConvertToYamlHandler<V1PersistentVolume> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1PersistentVolume v1PersistentVolume = new V1PersistentVolume();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsPvDTO.getName());
            v1PersistentVolume.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1PersistentVolume);
            Integer gitlabEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(null, gitlabEnvProjectId, DELETE, (long) GitUserNameUtil.getAdminId(), pvId,
                    PERSISTENTVOLUME, null, false, devopsEnvironmentDTO.getId(), path);
        }
        return true;
    }

    @Override
    public boolean isNameUnique(Long clusterId, String pvName) {
        DevopsPvDTO devopsPvDTO = new DevopsPvDTO();
        devopsPvDTO.setClusterId(clusterId);
        devopsPvDTO.setName(pvName);
        return devopsPvMapper.selectCount(devopsPvDTO) == 0;
    }

    @Override
    @Transactional
    public void assignPermission(Long projectId, DevopsPvPermissionUpdateVO update) {

        DevopsPvDTO devopsPvDTO = devopsPvMapper.queryWithEnvByPrimaryKey(update.getPvId());
        // 内部调用时，projectId为null，不校验
        if (projectId != null) {
            permissionHelper.checkEnvBelongToProject(projectId, devopsPvDTO.getEnvId());
        }

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

        devopsPvDTO.setObjectVersionNumber(oldDevopsPvDTO.getObjectVersionNumber());

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
    public Page<ProjectReqVO> listNonRelatedProjects(Long projectId, Long pvId, Long selectedProjectId, PageRequest pageable, String params) {
        DevopsPvDTO devopsPvDTO = baseQueryById(pvId);
        if (devopsPvDTO == null) {
            throw new CommonException("error.pv.not.exists");
        }

        PageRequest customPageRequest = new PageRequest(1, 0);

        List<ProjectReqVO> projectReqVOList = Optional.ofNullable(pageProjects(projectId, pvId, customPageRequest, params))
                .map(Page::getContent)
                .orElseThrow(() -> new CommonException("error.project.get"));

        //根据PvId查权限表中关联的projectId
        List<Long> permitted = devopsPvProPermissionService.baseListByPvId(pvId)
                .stream()
                .map(DevopsPvProPermissionDTO::getProjectId)
                .collect(Collectors.toList());

        //把组织下有权限的项目过滤掉再返回
        List<ProjectReqVO> projectReqVOS = projectReqVOList.stream()
                .filter(i -> !permitted.contains(i.getId()))
                .collect(Collectors.toList());

        if (selectedProjectId != null) {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(selectedProjectId);
            ProjectReqVO projectReqVO = new ProjectReqVO(projectDTO.getId(), projectDTO.getName(), projectDTO.getCode());
            if (!projectReqVOS.isEmpty()) {
                projectReqVOS.remove(projectReqVO);
                projectReqVOS.add(0, projectReqVO);
            } else {
                projectReqVOS.add(projectReqVO);
            }
        }

        return PageInfoUtil.createPageFromList(projectReqVOS, pageable);
    }

    @Override
    public void deleteRelatedProjectById(Long projectId, Long pvId, Long relatedProjectId) {
        DevopsPvDTO devopsPvDTO = devopsPvMapper.queryWithEnvByPrimaryKey(pvId);
        permissionHelper.checkEnvBelongToProject(projectId, devopsPvDTO.getEnvId());
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
        devopsPvDTO.setProjectId(environmentDTO.getProjectId());
        devopsPvDTO.setStatus(PvcStatus.OPERATING.getStatus());
        devopsPvDTO.setClusterId(environmentDTO.getClusterId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(devopsPvReqVO.getCommandType());
        devopsEnvCommandDTO.setLastUpdatedBy(userId);

        if (CommandType.CREATE.getType().equals(devopsPvReqVO.getCommandType())) {
            devopsEnvCommandDTO.setCreatedBy(userId);
            devopsPvDTO = createPvRecord(devopsPvDTO);
            devopsEnvCommandDTO.setObjectId(devopsPvDTO.getId());
            devopsPvDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            LOGGER.debug("Create Pv: to update... the pv is {}", devopsPvDTO);
            baseUpdate(devopsPvDTO);
        } else {
            LOGGER.warn("Pv GitOps: unexpected pv update operation");
            // 不应该有更新的操作, pv不允许更新
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
    public Page<ProjectReqVO> pageProjects(Long projectId, Long pvId, PageRequest pageable, String params) {
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
    public Page<ProjectReqVO> pageRelatedProjects(Long projectId, Long pvId, PageRequest pageable, String params) {
        DevopsPvDTO devopsPvDTO = baseQueryById(pvId);
        if (devopsPvDTO == null) {
            throw new CommonException("error.pv.not.exists");
        }


        Map<String, Object> searchMap = TypeUtil.castMapParams(params);
        Map<String, Object> searchParamsMap = TypeUtil.cast(searchMap.get(TypeUtil.SEARCH_PARAM));
        String name = null;
        String code = null;
        if (!CollectionUtils.isEmpty(searchParamsMap)) {
            name = TypeUtil.cast(searchParamsMap.get("name"));
            code = TypeUtil.cast(searchParamsMap.get("code"));
        }
        List<String> paramList = TypeUtil.cast(searchMap.get(TypeUtil.PARAMS));
        //接收模糊查询参数列表

        if (CollectionUtils.isEmpty(paramList) && StringUtils.isEmpty(name) && StringUtils.isEmpty(code)) {
            // 如果不搜索
            Page<DevopsPvProPermissionDTO> relationPage = PageHelper.doPage(pageable, () -> devopsPvProPermissionService.baseListByPvId(pvId));
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
                    name, code,
                    CollectionUtils.isEmpty(paramList) ? null : paramList.get(0));

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

    /**
     * 根据pv对象初始化Kubernetes对象，同时对传入的对象的valueConfig字段根据type更改
     *
     * @param devopsPvDTO pv对象
     * @return Kubernetes对象
     */
    private V1PersistentVolume initV1PersistentVolume(DevopsPvDTO devopsPvDTO) {
        V1PersistentVolume v1PersistentVolume = new V1PersistentVolume();
        v1PersistentVolume.setApiVersion("v1");
        v1PersistentVolume.setKind(PERSISTENTVOLUME);

        //设置pv名称
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(devopsPvDTO.getName());

        // 如果label不为空，设置label
        if (!StringUtils.isEmpty(devopsPvDTO.getLabels())) {
            v1ObjectMeta.setLabels(JsonHelper.unmarshalByJackson(devopsPvDTO.getLabels(), new TypeReference<Map<String, String>>() {
            }));
        }
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
                devopsPvDTO.setValueConfig(JSONObject.toJSONString(nfs));
                break;
            case HOSTPATH:
                V1HostPathVolumeSource hostPath = gson.fromJson(devopsPvDTO.getValueConfig(), V1HostPathVolumeSource.class);
                //反序列成对象之后进行校验
                DevopsPvValidator.checkConfigValue(hostPath, volumeTypeEnum);
                v1PersistentVolumeSpec.setHostPath(hostPath);
                devopsPvDTO.setValueConfig(JSONObject.toJSONString(hostPath));
                break;
            case LOCALPV:
                LocalPvResource localPvResource = gson.fromJson(devopsPvDTO.getValueConfig(), LocalPvResource.class);
                //反序列化成对象之后校验
                DevopsPvValidator.checkConfigValue(localPvResource, volumeTypeEnum);

                V1LocalVolumeSource v1LocalVolumeSource = new V1LocalVolumeSource();
                v1LocalVolumeSource.setPath(localPvResource.getPath());

                v1PersistentVolumeSpec.setLocal(v1LocalVolumeSource);

                V1VolumeNodeAffinity nodeAffinity = new V1VolumeNodeAffinity();
                V1NodeSelector v1NodeSelector = new V1NodeSelector();
                V1NodeSelectorTerm v1NodeSelectorTerm = new V1NodeSelectorTerm();

                V1NodeSelectorRequirement v1NodeSelectorRequirement = new V1NodeSelectorRequirement();
                v1NodeSelectorRequirement.setKey("kubernetes.io/hostname");
                v1NodeSelectorRequirement.setOperator("In");
                v1NodeSelectorRequirement.setValues(Collections.singletonList(localPvResource.getNodeName()));

                v1NodeSelectorTerm.setMatchExpressions(Collections.singletonList(v1NodeSelectorRequirement));

                v1NodeSelector.setNodeSelectorTerms(Collections.singletonList(v1NodeSelectorTerm));

                nodeAffinity.setRequired(v1NodeSelector);


                v1PersistentVolumeSpec.setNodeAffinity(nodeAffinity);
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
        // 1ki=1024
        // 1Mi=1024*1024
        size = (long) (size * Math.pow(1024, (double) level + 2));

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
                        .withSourceId(devopsEnvironmentDTO.getProjectId())
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
            String path = clusterConnectionHandler.handDevopsEnvGitRepository(
                    persistentVolumePayload.getDevopsEnvironmentDTO(),
                    persistentVolumePayload.getDevopsEnvironmentDTO().getProjectId(),
                    persistentVolumePayload.getDevopsEnvironmentDTO().getCode(),
                    persistentVolumePayload.getDevopsEnvironmentDTO().getId(),
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
            LOGGER.info("create or update PersistentVolume failed! {}", e.getMessage());
            //有异常更新实例以及command的状态
            DevopsPvDTO devopsPvDTO = devopsPvMapper.selectByPrimaryKey(persistentVolumePayload.getDevopsPvDTO().getId());
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndResourceId(persistentVolumePayload.getDevopsEnvironmentDTO().getId(), devopsPvDTO.getId(), PERSISTENTVOLUME);
            String filePath = devopsEnvFileResourceDTO == null ? PERSISTENTVOLUME_PREFIX + devopsPvDTO.getName() + YAML_SUFFIX : devopsEnvFileResourceDTO.getFilePath();
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(persistentVolumePayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId()), MASTER,
                    filePath)) {
                devopsPvDTO.setStatus(PvStatus.FAILED.getStatus());
                baseUpdate(devopsPvDTO);
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsPvDTO.getCommandId());
                devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
                devopsEnvCommandDTO.setError("create or update PV failed!");
                devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
            }
        }
    }

    @Override
    public List<DevopsPvLabelVO> listLabels(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        List<String> labelsInString = devopsPvMapper.listLabelsByClusterId(clusterId);
        List<DevopsPvLabelVO> labels = new ArrayList<>();
        labelsInString.stream()
                .filter(s -> !StringUtils.isEmpty(s))
                .map(s -> JsonHelper.unmarshalByJackson(s, new TypeReference<Map<String, String>>() {
                }))
                .forEach(l -> l.forEach((k, v) -> {
                    DevopsPvLabelVO devopsPvLabelVO = new DevopsPvLabelVO();
                    devopsPvLabelVO.setKey(k);
                    devopsPvLabelVO.setValue(v);
                    if (!labels.contains(devopsPvLabelVO)) {
                        labels.add(devopsPvLabelVO);
                    }
                }));
        return labels;
    }

    @Override
    public List<DevopsPvVO> queryPvcRelatedPv(Long projectId, Long envId, Long clusterId, String params, Integer mode) {
        if (clusterId == null) {
            if (envId != null) {
                DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
                clusterId = devopsEnvironmentDTO.getClusterId();
            } else {
                throw new CommonException("error.envId.and.clusterId.null");
            }
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Map<String, Object> searchParamMap = TypeUtil.castMapParams(params);
        Map<String, String> map = (Map) searchParamMap.get(TypeUtil.SEARCH_PARAM);

        List<DevopsPvVO> devopsPvVOList = ConvertUtils.convertList(devopsPvMapper.listPvByOptions(
                projectDTO.getOrganizationId(),
                null,
                clusterId,
                null,
                TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))), DevopsPvVO.class);
        List<Long> devopsPvVOIdList = devopsPvVOList.stream().map(DevopsPvVO::getId).collect(Collectors.toList());
        List<Long> projectRelatedPvIdsList;

        // 获得不跳过权限的与本项目有关联的pv并过滤掉不符合条件的pv
        projectRelatedPvIdsList = devopsPvProPermissionService.baseListPvIdsByProjectId(projectId)
                .stream()
                .filter(devopsPvVOIdList::contains)
                .collect(Collectors.toList());
        devopsPvVOList.forEach(pv -> {
            PageRequest customPageRequest = new PageRequest(1, 0);
            //获得跳过权限的与本项目有关联的pv
            if (pv.getSkipCheckProjectPermission()) {
                List<ProjectReqVO> list = new ArrayList<>(Optional.ofNullable(pageProjects(pv.getProjectId(), pv.getId(), customPageRequest, params).getContent()).orElse(new ArrayList<>()));
                if (list.stream().map(ProjectReqVO::getId).collect(Collectors.toList()).contains(projectId)) {
                    projectRelatedPvIdsList.add(pv.getId());
                }
            }
        });

        List<DevopsPvVO> projectRelatedPvList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(projectRelatedPvIdsList)) {
            projectRelatedPvList = ConvertUtils.convertList(devopsPvMapper.listByPvIds(projectRelatedPvIdsList), DevopsPvVO.class);
        }

        List<Long> updatedClusterList = clusterConnectionHandler.getUpdatedClusterList();

        String pvcStorage = map.get("requestResource");
        // 筛选容量大于或等于pvc容量且集群agent处于连接状态且未与Prometheus进行绑定
        List<Long> boundPvIds = getBoundPvIds(devopsPrometheusMapper, mode);
        if (pvcStorage != null) {
            return projectRelatedPvList.stream()
                    .filter(e -> compareResource(e.getRequestResource(), pvcStorage) > 0 && e.getPvcName() == null)
                    .filter(e -> updatedClusterList.contains(e.getClusterId()))
                    .filter(e -> !boundPvIds.contains(e.getId()))
                    .collect(Collectors.toList());
        } else {
            return projectRelatedPvList.stream()
                    .filter(e -> !boundPvIds.contains(e.getId()))
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

    private List<Long> getBoundPvIds(DevopsPrometheusMapper devopsPrometheusMapper, Integer mode) {
        // 0表示不需要查出与Prometheus绑定过的PV,直接返回空链表
        if (mode == 0) {
            return new ArrayList<>();
        }
        List<DevopsPrometheusDTO> devopsPrometheusDTOList = devopsPrometheusMapper.selectAll();
        List<Long> boundPvIds = new ArrayList<>();
        for (DevopsPrometheusDTO prometheusDTO : devopsPrometheusDTOList) {
            boundPvIds.add(prometheusDTO.getGrafanaPvId());
            boundPvIds.add(prometheusDTO.getPrometheusPvId());
            boundPvIds.add(prometheusDTO.getAlertmanagerPvId());
        }
        return boundPvIds;
    }
}
