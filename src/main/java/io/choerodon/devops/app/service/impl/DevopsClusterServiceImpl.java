package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.service.impl.DevopsClusterNodeServiceImpl.CLUSTER_INFO_REDIS_KEY_TEMPLATE;
import static io.choerodon.devops.app.service.impl.DevopsClusterNodeServiceImpl.NODE_CHECK_STEP_REDIS_KEY_TEMPLATE;
import static io.choerodon.devops.infra.constant.ExceptionConstants.ClusterCode.DEVOPS_CLUSTER_NOT_EXIST;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.DevopsClusterValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.DevopsClusterInstallInfoVO;
import io.choerodon.devops.app.eventhandler.payload.DevopsClusterInstallPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ClusterCheckConstant;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;
import io.choerodon.devops.infra.mapper.DevopsPvProPermissionMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsClusterServiceImpl implements DevopsClusterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsClusterServiceImpl.class);
    private static final String CLUSTER_ACTIVATE_COMMAND_TEMPLATE;
    private static final String SAGA_INSTALL_K8S_REF_TYPE = "install-cluster";
    private static final String SAGA_RETRY_INSTALL_K8S_REF_TYPE = "retry-install";
    private static final String DIS_CONNECTION = "helm uninstall choerodon-cluster-agent-%s -n choerodon";

    /**
     * 存储集群基本信息的key: cluster-{clusterId}-info
     * 存储的结构为 {@link ClusterSummaryInfoVO}
     */
    private static final String CLUSTER_INFO_KEY_TEMPLATE = "cluster-%s-info";

    private static final String ERROR_UPDATE_CLUSTER_STATUS_FAILED = "devops.update.cluster.status.failed";
    private static final String ERROR_ORGANIZATION_CLUSTER_NUM_MAX = "devops.organization.cluster.num.max";

    @Value("${agent.version}")
    private String agentExpectVersion;
    @Value("${agent.serviceUrl}")
    private String agentServiceUrl;
    @Value("${agent.repoUrl}")
    private String agentRepoUrl;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private ClusterNodeInfoService clusterNodeInfoService;
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
    private DevopsClusterMapper devopsClusterMapper;
    @Autowired
    private DevopsClusterProPermissionService devopsClusterProPermissionService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsPvService devopsPvService;
    @Autowired
    private DevopsPvProPermissionMapper devopsPvProPermissionMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PolarisScanningService polarisScanningService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    @Lazy
    private SendNotificationService sendNotificationService;
    @Autowired
    private DevopsClusterNodeService devopsClusterNodeService;
    @Autowired
    private DevopsClusterValidator devopsClusterValidator;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private DevopsClusterOperationRecordService devopsClusterOperationRecordService;
    @Autowired
    private AgentCommandService agentCommandService;

    static {
        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream("/shell/cluster.sh")) {
            CLUSTER_ACTIVATE_COMMAND_TEMPLATE = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("devops.load.cluster.sh");
        }
    }


    @Override
    public void saveClusterSummaryInfo(Long clusterId, ClusterSummaryInfoVO clusterSummaryInfoVO) {
        if (clusterSummaryInfoVO == null || clusterSummaryInfoVO.getVersion() == null) {
            LOGGER.warn("Abandon Bad cluster info: {}", clusterSummaryInfoVO);
            return;
        }
        String redisKey = renderClusterInfoRedisKey(clusterId);
        stringRedisTemplate.opsForValue().set(redisKey, JSONObject.toJSONString(clusterSummaryInfoVO));
        LOGGER.info("Finish saving info about cluster with id {}. The redisKey is {}. the info object is: {} ", clusterId, redisKey, clusterSummaryInfoVO);
    }

    @Override
    public ClusterSummaryInfoVO queryClusterSummaryInfo(Long clusterId) {
        String redisKey = renderClusterInfoRedisKey(clusterId);
        String json = stringRedisTemplate.opsForValue().get(redisKey);
        return StringUtils.isEmpty(json) ? null : JSONObject.parseObject(json, ClusterSummaryInfoVO.class);
    }

    /**
     * 获取存储集群信息到redis的key
     *
     * @param clusterId 集群id
     * @return key
     */
    public static String renderClusterInfoRedisKey(Long clusterId) {
        return String.format(CLUSTER_INFO_KEY_TEMPLATE, Objects.requireNonNull(clusterId));
    }

    @Transactional
    @Override
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_INSTALL_K8S, description = "创建集群", inputSchema = "{}")
    public String createCluster(Long projectId, DevopsClusterReqVO devopsClusterReqVO) {
        String clusterInfoRedisKey = String.format(CLUSTER_INFO_REDIS_KEY_TEMPLATE, projectId, devopsClusterReqVO.getCode());
        String redisKey = String.format(NODE_CHECK_STEP_REDIS_KEY_TEMPLATE, projectId, devopsClusterReqVO.getCode());

        // 如果这个key存在，表明已经有相同的集群处于创建中，禁止重复创建
        Boolean exists = stringRedisTemplate.hasKey(redisKey);
        if (exists) {
            throw new CommonException("devops.cluster.installing");
        }

        // 判断组织下是否还能创建集群
        checkEnableCreateClusterOrThrowE(projectId);
        // 检查节点满足要求
        devopsClusterValidator.check(devopsClusterReqVO);

        // 提供外网信息节点
        DevopsClusterNodeVO devopsClusterOutterNodeVO = devopsClusterReqVO.getDevopsClusterOutterNodeVO();
        // 集群节点
        List<DevopsClusterNodeVO> devopsClusterInnerNodeVOList = devopsClusterReqVO.getDevopsClusterInnerNodeVOList();
        // 需要保存的节点
        List<DevopsClusterNodeDTO> devopsClusterNodeToSaveDTOList = new ArrayList<>();

        HostConnectionVO hostConnectionVO;
        // 选出进行ssh连接的节点
        if (devopsClusterOutterNodeVO != null && !StringUtils.isEmpty(devopsClusterOutterNodeVO.getHostIp())) {
            devopsClusterOutterNodeVO.setName(devopsClusterReqVO.getCode() + "-sshNode");
            devopsClusterOutterNodeVO.setType(ClusterNodeTypeEnum.OUTTER.getType());
            hostConnectionVO = ConvertUtils.convertObject(devopsClusterOutterNodeVO, HostConnectionVO.class);
            devopsClusterNodeToSaveDTOList.add(ConvertUtils.convertObject(devopsClusterOutterNodeVO, DevopsClusterNodeDTO.class));
        } else {
            hostConnectionVO = ConvertUtils.convertObject(devopsClusterInnerNodeVOList.get(0), HostConnectionVO.class);
            devopsClusterReqVO.setDevopsClusterOutterNodeVO(null);
        }

        devopsClusterNodeToSaveDTOList.addAll(ConvertUtils.convertList(devopsClusterInnerNodeVOList, DevopsClusterNodeDTO.class));

        DevopsClusterInstallInfoVO devopsClusterInstallInfoVO = new DevopsClusterInstallInfoVO()
                .setDevopsClusterReqVO(devopsClusterReqVO)
                .setProjectId(projectId)
                .setHostConnectionVO(hostConnectionVO)
                .setRedisKey(redisKey)
                .setDevopsClusterNodeToSaveDTOList(devopsClusterNodeToSaveDTOList);

        stringRedisTemplate.opsForValue().set(clusterInfoRedisKey, JsonHelper.marshalByJackson(devopsClusterInstallInfoVO));

        DevopsClusterInstallPayload devopsClusterInstallPayload = new DevopsClusterInstallPayload()
                .setProjectId(projectId)
                .setRedisKey(redisKey)
                .setClusterInfoRedisKey(clusterInfoRedisKey);

        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(projectId)
                        .withRefType(SAGA_INSTALL_K8S_REF_TYPE)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_INSTALL_K8S),
                builder -> builder
                        .withPayloadAndSerialize(devopsClusterInstallPayload)
                        .withRefId(devopsClusterReqVO.getCode())
                        .withSourceId(projectId));

        return redisKey;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_RETRY_INSTALL_K8S, description = "重试创建集群", inputSchema = "{}")
    public void retryInstallK8s(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        if (!devopsClusterDTO.getStatus().equalsIgnoreCase(ClusterStatusEnum.FAILED.value())) {
            throw new CommonException("devops.cluster.status");
        }
        CommonExAssertUtil.assertTrue(devopsClusterDTO.getProjectId().equals(projectId), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO = devopsClusterOperationRecordService.selectByClusterIdAndType(clusterId, ClusterOperationTypeEnum.INSTALL_K8S.getType());
        devopsClusterOperationRecordDTO.setErrorMsg("");
        devopsClusterOperationRecordDTO.setStatus(ClusterOperationStatusEnum.OPERATING.value());

        devopsClusterOperationRecordService.updateByPrimaryKeySelective(devopsClusterOperationRecordDTO);

        devopsClusterDTO.setStatus(ClusterStatusEnum.OPERATING.value());
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsClusterMapper, devopsClusterDTO, "devops.update.cluster");

        LOGGER.info("update cluster and cluster operation status");

        DevopsClusterInstallPayload devopsClusterInstallPayload = new DevopsClusterInstallPayload()
                .setProjectId(projectId)
                .setClusterId(clusterId)
                .setOperationRecordId(devopsClusterOperationRecordDTO.getId());

        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(projectId)
                        .withRefType(SAGA_RETRY_INSTALL_K8S_REF_TYPE)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_RETRY_INSTALL_K8S),
                builder -> builder
                        .withPayloadAndSerialize(devopsClusterInstallPayload)
                        .withRefId(devopsClusterDTO.getCode())
                        .withSourceId(projectId));
    }

    @Override
    public DevopsNodeCheckResultVO checkProgress(Long projectId, String redisKey) {
        String value = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isEmpty(value)) {
            return new DevopsNodeCheckResultVO();
        }
        DevopsNodeCheckResultVO devopsNodeCheckResultVO = JsonHelper.unmarshalByJackson(value, DevopsNodeCheckResultVO.class);
        // 如果不是OPERATING状态，表示节点检查完成，删除这个key
        if (!ClusterOperationStatusEnum.OPERATING.value().equalsIgnoreCase(devopsNodeCheckResultVO.getStatus())) {
            LOGGER.info(">>>>>>>>> [check node] key {}:check complete , result: {}", redisKey, value);
            stringRedisTemplate.delete(redisKey);
        }
        return devopsNodeCheckResultVO;
    }

    @Override
    @Transactional
    public String activateCluster(Long projectId, DevopsClusterReqVO devopsClusterReqVO) {
        ProjectDTO iamProject = null;
        DevopsClusterDTO devopsClusterDTO = null;
        IamUserDTO iamUserDTO;
        try {
            devopsClusterDTO = insertClusterInfo(projectId, devopsClusterReqVO, ClusterTypeEnum.IMPORTED.value());
            iamUserDTO = baseServiceClientOperator.queryUserByUserId(GitUserNameUtil.getUserId());
        } catch (Exception e) {
            //创建集群失败发送webhook json
            sendNotificationService.sendWhenCreateClusterFail(devopsClusterDTO, iamProject, e.getMessage());
            throw e;
        }

        //创建集群成功发送web_hook
        sendNotificationService.sendWhenCreateCluster(devopsClusterDTO, iamProject);
        return getInstallString(devopsClusterDTO, iamUserDTO == null ? "" : iamUserDTO.getEmail());
    }

    /**
     * 获得agent安装命令
     *
     * @return agent安装命令
     */
    @Override
    public String getInstallString(DevopsClusterDTO devopsClusterDTO, String userEmail) {
        Map<String, String> params = new HashMap<>();
        // 渲染激活环境的命令参数
        params.put("{VERSION}", agentExpectVersion);
        params.put("{NAME}", "choerodon-cluster-agent-" + devopsClusterDTO.getCode());
        params.put("{SERVICEURL}", agentServiceUrl);
        params.put("{TOKEN}", devopsClusterDTO.getToken());
        params.put("{EMAIL}", StringUtils.isEmpty(userEmail) ? "" : userEmail);
        params.put("{CHOERODONID}", devopsClusterDTO.getChoerodonId());
        params.put("{REPOURL}", agentRepoUrl);
        params.put("{CLUSTERID}", devopsClusterDTO
                .getId().toString());

        return FileUtil.replaceReturnString(CLUSTER_ACTIVATE_COMMAND_TEMPLATE, params);
    }

    private void checkEnableCreateClusterOrThrowE(Long projectId) {
        if (Boolean.FALSE.equals(checkEnableCreateCluster(projectId))) {
            throw new CommonException(ERROR_ORGANIZATION_CLUSTER_NUM_MAX);
        }
    }

    @Override
    public Boolean checkEnableCreateCluster(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Long organizationId = projectDTO.getOrganizationId();
        ResourceLimitVO resourceLimitVO = baseServiceClientOperator.queryResourceLimit(organizationId);
        if (resourceLimitVO != null) {
            DevopsClusterDTO example = new DevopsClusterDTO();
            example.setProjectId(projectId);
            int num = devopsClusterMapper.selectCount(example);
            return num < resourceLimitVO.getClusterMaxNumber();
        }
        return true;
    }

    @Override
    @Transactional
    public void updateCluster(Long projectId, Long clusterId, DevopsClusterUpdateVO devopsClusterUpdateVO) {
        if (StringUtils.isEmpty(devopsClusterUpdateVO.getName())) {
            devopsClusterUpdateVO.setName(null);
        }

        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(devopsClusterUpdateVO.getId());
        // 内部调用不需要校验
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        // 可以更新的字段：集群名称、集群描述
        devopsClusterDTO.setName(devopsClusterUpdateVO.getName());
        devopsClusterDTO.setDescription(devopsClusterUpdateVO.getDescription());
        devopsClusterMapper.updateByPrimaryKey(devopsClusterDTO);
    }

    @Override
    public boolean isNameUnique(Long projectId, String name) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setProjectId(Objects.requireNonNull(projectId));
        devopsClusterDTO.setName(Objects.requireNonNull(name));
        return devopsClusterMapper.selectCount(devopsClusterDTO) == 0;
    }

    @Override
    public String queryShell(Long clusterId) {
        DevopsClusterRepVO devopsClusterRepVO = getDevopsClusterStatus(clusterId);
        InputStream inputStream = this.getClass().getResourceAsStream("/shell/cluster.sh");

        //初始化渲染脚本
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(devopsClusterRepVO.getCreatedBy());
        Map<String, String> params = new HashMap<>();
        params.put("{VERSION}", agentExpectVersion);
        params.put("{NAME}", "choerodon-cluster-agent-" + devopsClusterRepVO.getCode());
        params.put("{SERVICEURL}", agentServiceUrl);
        params.put("{TOKEN}", devopsClusterRepVO.getToken());
        params.put("{EMAIL}", iamUserDTO == null ? "" : iamUserDTO.getEmail());
        params.put("{REPOURL}", agentRepoUrl);
        params.put("{CHOERODONID}", devopsClusterRepVO.getChoerodonId());
        params.put("{CLUSTERID}", devopsClusterRepVO
                .getId().toString());
        return FileUtil.replaceReturnString(inputStream, params);
    }

    @Override
    public boolean isCodeUnique(Long projectId, String code) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setOrganizationId(projectDTO.getOrganizationId());
        devopsClusterDTO.setCode(code);
        return devopsClusterMapper.selectCount(devopsClusterDTO) == 0;
    }

    @Override
    public Page<ClusterWithNodesVO> pageClusters(Long projectId, Boolean doPage, PageRequest pageable, String params) {
        Page<DevopsClusterRepVO> devopsClusterRepVOPageInfo = ConvertUtils.convertPage(basePageClustersByOptions(projectId, doPage, pageable, params), DevopsClusterRepVO.class);
        Page<ClusterWithNodesVO> devopsClusterRepDTOPage = ConvertUtils.convertPage(devopsClusterRepVOPageInfo, ClusterWithNodesVO.class);

        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();
        devopsClusterRepVOPageInfo.getContent().forEach(devopsClusterRepVO -> {
            if (updatedEnvList.contains(devopsClusterRepVO.getId())) {
                devopsClusterRepVO.setConnect(true);
                if (devopsClusterRepVO.getStatus().equalsIgnoreCase(ClusterStatusEnum.DISCONNECT.value())) {
                    devopsClusterRepVO.setStatus(ClusterStatusEnum.RUNNING.value());
                }

            }
        });

        devopsClusterRepDTOPage.setContent(fromClusterE2ClusterWithNodesDTO(devopsClusterRepVOPageInfo.getContent(), projectId));
        return devopsClusterRepDTOPage;
    }

    @Override
    public Page<ProjectReqVO> listNonRelatedProjects(Long projectId, Long clusterId, Long selectedProjectId, PageRequest pageable, String params) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        if (devopsClusterDTO == null) {
            throw new CommonException(DEVOPS_CLUSTER_NOT_EXIST, clusterId);
        }

        Map<String, String> searchParamMap = new HashMap<>();
        List<String> paramList = new ArrayList<>();
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = TypeUtil.castMapParams(params);
            searchParamMap = org.apache.commons.lang3.ObjectUtils.defaultIfNull(TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), Collections.emptyMap());
            paramList = org.apache.commons.lang3.ObjectUtils.defaultIfNull(TypeUtil.cast(maps.get(TypeUtil.PARAMS)), Collections.emptyList());
        }

        ProjectDTO iamProjectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        // 查出组织下所有符合条件的项目
        List<ProjectDTO> filteredProjects = baseServiceClientOperator.listIamProjectByOrgId(
                iamProjectDTO.getOrganizationId(),
                searchParamMap.get("name"),
                searchParamMap.get("code"),
                CollectionUtils.isEmpty(paramList) ? null : paramList.get(0));

        // 查出数据库中已经分配权限的项目
        List<Long> permitted = devopsClusterProPermissionService.baseListByClusterId(clusterId)
                .stream()
                .map(DevopsClusterProPermissionDTO::getProjectId)
                .collect(Collectors.toList());

        // 将已经分配权限的项目过滤
        List<ProjectReqVO> projectReqVOS = filteredProjects
                .stream()
                .filter(p -> !permitted.contains(p.getId()))
                .map(p -> new ProjectReqVO(p.getId(), p.getName(), p.getCode()))
                .collect(Collectors.toList());

        if (selectedProjectId != null) {
            ProjectDTO selectedProjectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(selectedProjectId);
            ProjectReqVO projectReqVO = new ProjectReqVO(selectedProjectDTO.getId(), selectedProjectDTO.getName(), selectedProjectDTO.getCode());
            if (!projectReqVOS.isEmpty()) {
                projectReqVOS.remove(projectReqVO);
                projectReqVOS.add(0, projectReqVO);
            } else {
                projectReqVOS.add(projectReqVO);
            }
        }
        return PageInfoUtil.createPageFromList(projectReqVOS, pageable);
    }

    @Transactional
    @Override
    public void assignPermission(Long projectId, DevopsClusterPermissionUpdateVO update) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(update.getClusterId());
        if (devopsClusterDTO == null) {
            throw new CommonException(DEVOPS_CLUSTER_NOT_EXIST, update.getClusterId());
        }
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        if (devopsClusterDTO.getSkipCheckProjectPermission()) {
            // 原来跳过，现在也跳过，不处理

            if (!update.getSkipCheckProjectPermission()) {
                // 原来跳过，现在不跳过，先更新字段，然后插入关联关系
                updateSkipPermissionCheck(
                        update.getClusterId(),
                        update.getSkipCheckProjectPermission(),
                        update.getObjectVersionNumber());

                devopsClusterProPermissionService.batchInsertIgnore(
                        update.getClusterId(),
                        update.getProjectIds());

                //如果在PV里面有未非配权限的项目，则删除
                List<DevopsPvProPermissionDTO> devopsPvProPermissionDTOList = devopsPvProPermissionMapper.listByClusterId(update.getClusterId());
                if (!devopsPvProPermissionDTOList.isEmpty()) {
                    List<DevopsPvProPermissionDTO> devopsPvProPermissionDTOToDeleteList = devopsPvProPermissionDTOList.stream()
                            .filter(e -> !update.getProjectIds().contains(e.getProjectId()))
                            .collect(Collectors.toList());
                    if (!devopsPvProPermissionDTOToDeleteList.isEmpty()) {
                        devopsPvProPermissionMapper.batchDelete(devopsPvProPermissionDTOToDeleteList);
                    }
                }

            }
        } else {
            // 原来不跳过，现在跳过，更新集群权限字段，再删除所有数据库中与该集群有关的关联关系
            if (update.getSkipCheckProjectPermission()) {
                updateSkipPermissionCheck(
                        update.getClusterId(),
                        update.getSkipCheckProjectPermission(),
                        update.getObjectVersionNumber());

                devopsClusterProPermissionService.baseDeleteByClusterId(update.getClusterId());
            } else {
                // 原来不跳过，现在也不跳过，批量添加权限
                devopsClusterProPermissionService.batchInsertIgnore(
                        update.getClusterId(),
                        update.getProjectIds());
            }
        }
    }

    /**
     * 更新集群的权限校验字段
     *
     * @param clusterId           集群id
     * @param skipCheckPermission 是否跳过权限校验
     * @param objectVersionNumber 版本号
     */
    private void updateSkipPermissionCheck(Long clusterId, Boolean skipCheckPermission, Long objectVersionNumber) {
        DevopsClusterDTO toUpdate = new DevopsClusterDTO();
        toUpdate.setId(clusterId);
        toUpdate.setObjectVersionNumber(objectVersionNumber);
        toUpdate.setSkipCheckProjectPermission(skipCheckPermission);
        devopsClusterMapper.updateByPrimaryKeySelective(toUpdate);
    }

    @Override
    public void deletePermissionOfProject(Long projectId, Long clusterId, Long relatedProjectId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        if (clusterId == null || relatedProjectId == null) {
            return;
        }
        //查出该集群关联的所有PV，删除与relatedProjectId的关联信息
        List<Long> pvIds = devopsPvService.queryByClusterId(clusterId).stream()
                .map(DevopsPvDTO::getId)
                .collect(Collectors.toList());
        if (!pvIds.isEmpty()) {
            devopsPvProPermissionMapper.batchDeleteByPvIdsAndProjectId(pvIds, relatedProjectId);
        }

        devopsClusterProPermissionService.baseDeletePermissionByClusterIdAndProjectId(clusterId, relatedProjectId);
    }

    @Override
    public List<DevopsClusterBasicInfoVO> queryClustersAndNodes(Long projectId) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setProjectId(projectId);
        List<DevopsClusterDTO> devopsClusterDTOList = devopsClusterMapper.select(devopsClusterDTO);
        List<DevopsClusterBasicInfoVO> devopsClusterBasicInfoVOList = ConvertUtils.convertList(devopsClusterDTOList, DevopsClusterBasicInfoVO.class);
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();

        // 连接的集群
        List<DevopsClusterBasicInfoVO> connectedClusters = new ArrayList<>();
        // 未连接的集群
        List<DevopsClusterBasicInfoVO> unconnectedClusters = new ArrayList<>();
        devopsClusterBasicInfoVOList.forEach(devopsClusterBasicInfoVO -> {

            boolean connect = updatedEnvList.contains(devopsClusterBasicInfoVO.getId());
            if (connect) {
                devopsClusterBasicInfoVO.setConnect(connect);
                // 如果在数据库中保存的状态是UNCONNECTED,则将状态置为CONNECTED
                if (devopsClusterBasicInfoVO.getStatus().equalsIgnoreCase(ClusterStatusEnum.DISCONNECT.value())) {
                    devopsClusterBasicInfoVO.setStatus(ClusterStatusEnum.RUNNING.value());
                }
                connectedClusters.add(devopsClusterBasicInfoVO);
            } else {
                unconnectedClusters.add(devopsClusterBasicInfoVO);
            }

            // 如果集群状态是失败，设置错误信息
            if (ClusterStatusEnum.FAILED.value().equalsIgnoreCase(devopsClusterBasicInfoVO.getStatus())) {
                DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO = devopsClusterOperationRecordService.selectByClusterIdAndType(devopsClusterBasicInfoVO.getId(), ClusterOperationTypeEnum.INSTALL_K8S.getType());
                if (devopsClusterOperationRecordDTO != null) {
                    devopsClusterBasicInfoVO.setErrorMessage(devopsClusterOperationRecordDTO.getErrorMsg());
                }
            }
        });

        // 将连接的集群放置在未连接的集群前
        connectedClusters.addAll(unconnectedClusters);
        connectedClusters.forEach(devopsClusterBasicInfoVO ->
                devopsClusterBasicInfoVO.setNodes(clusterNodeInfoService.queryNodeName(projectId, devopsClusterBasicInfoVO.getId())));

        return connectedClusters;
    }

    @Override
    public Page<ProjectReqVO> pageRelatedProjects(Long projectId, Long clusterId, PageRequest pageable, String params) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        if (devopsClusterDTO == null) {
            throw new CommonException(DEVOPS_CLUSTER_NOT_EXIST, clusterId);
        }

        Map<String, Object> map = TypeUtil.castMapParams(params);
        Map<String, Object> searchParamsMap = TypeUtil.cast(map.get(TypeUtil.SEARCH_PARAM));
        String name = null;
        String code = null;
        if (!CollectionUtils.isEmpty(searchParamsMap)) {
            name = TypeUtil.cast(searchParamsMap.get("name"));
            code = TypeUtil.cast(searchParamsMap.get("code"));
        }
        List<String> paramList = TypeUtil.cast(map.get(TypeUtil.PARAMS));
        if (CollectionUtils.isEmpty(paramList)) {
            //如果不分页
            if (pageable.getSize() == 0) {
                Set<Long> devopsProjectIds = devopsClusterProPermissionService.baseListByClusterId(clusterId).stream()
                        .map(DevopsClusterProPermissionDTO::getProjectId)
                        .collect(Collectors.toSet());
                List<ProjectReqVO> projectReqVOList = baseServiceClientOperator.queryProjectsByIds(devopsProjectIds).stream()
                        .map(i -> new ProjectReqVO(i.getId(), i.getName(), i.getCode()))
                        .collect(Collectors.toList());
                return PageInfoUtil.createPageFromList(projectReqVOList, pageable);
            } else {
                // 如果不搜索
                Page<DevopsClusterProPermissionDTO> relationPage = PageHelper.doPage(pageable, () -> devopsClusterProPermissionService.baseListByClusterId(clusterId));
                return ConvertUtils.convertPage(relationPage, permission -> {
                    if (permission.getProjectId() == null) {
                        return null;
                    }
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(permission.getProjectId());
                    return new ProjectReqVO(permission.getProjectId(), projectDTO.getName(), projectDTO.getCode());
                });
            }
        } else {
            // 如果要搜索，需要手动在程序内分页
            ProjectDTO iamProjectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

            // 手动查出所有组织下的项目
            List<ProjectDTO> filteredProjects = baseServiceClientOperator.listIamProjectByOrgId(
                    iamProjectDTO.getOrganizationId(),
                    name, code,
                    paramList.get(0));

            // 数据库中的有权限的项目
            List<Long> permissions = devopsClusterProPermissionService.baseListByClusterId(clusterId)
                    .stream()
                    .map(DevopsClusterProPermissionDTO::getProjectId)
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
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void deleteCluster(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        if (devopsClusterDTO == null) {
            return;
        }
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        // 校验集群是否能够删除
        checkConnectAndExistEnvsOrPV(clusterId);

        if (!ObjectUtils.isEmpty(devopsClusterDTO.getClientId())) {
            baseServiceClientOperator.deleteClient(devopsClusterDTO.getOrganizationId(), devopsClusterDTO.getClientId());
        }
        devopsEnvironmentService.deleteSystemEnv(devopsClusterDTO.getProjectId(), devopsClusterDTO.getId(), devopsClusterDTO.getCode(), devopsClusterDTO.getSystemEnvId());

        polarisScanningService.deleteAllByScopeAndScopeId(PolarisScopeType.CLUSTER, clusterId);

        baseDelete(clusterId);
        //删除集群后发送webhook
        sendNotificationService.sendWhenDeleteCluster(devopsClusterDTO);
    }

    private void checkConnectAndExistEnvsOrPV(Long clusterId) {
        List<Long> connectedEnvList = clusterConnectionHandler.getUpdatedClusterList();
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentService.baseListUserEnvByClusterId(clusterId);

        if (connectedEnvList.contains(clusterId)) {
            throw new CommonException("devops.cluster.connected");
        }
        if (!devopsEnvironmentDTOS.isEmpty()) {
            throw new CommonException("devops.cluster.delete");
        }
        //集群是否存在PV
        List<DevopsPvDTO> clusterDTOList = devopsPvService.queryByClusterId(clusterId);
        if (!Objects.isNull(clusterDTOList) && !clusterDTOList.isEmpty()) {
            throw new CommonException("devops.cluster.pv.exist");
        }
    }

    @Override
    public ClusterMsgVO checkConnectEnvsAndPV(Long clusterId) {
        ClusterMsgVO clusterMsgVO = new ClusterMsgVO(false, false);
        List<Long> connectedEnvList = clusterConnectionHandler.getUpdatedClusterList();
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentService.baseListUserEnvByClusterId(clusterId);

        if (connectedEnvList.contains(clusterId)) {
            clusterMsgVO.setCheckEnv(true);
        }
        if (!devopsEnvironmentDTOS.isEmpty()) {
            clusterMsgVO.setCheckEnv(true);
        }
        //集群是否存在PV
        List<DevopsPvDTO> clusterDTOList = devopsPvService.queryByClusterId(clusterId);
        if (!Objects.isNull(clusterDTOList) && !clusterDTOList.isEmpty()) {
            clusterMsgVO.setCheckPV(true);
        }
        return clusterMsgVO;
    }


    @Override
    public DevopsClusterRepVO query(Long clusterId) {
        DevopsClusterRepVO result = ConvertUtils.convertObject(baseQuery(clusterId), DevopsClusterRepVO.class);
        if (result == null) {
            return null;
        }
        List<Long> upToDateList = clusterConnectionHandler.getUpdatedClusterList();
        if (upToDateList.contains(clusterId)) {
            result.setConnect(true);
            if (result.getStatus().equalsIgnoreCase(ClusterStatusEnum.DISCONNECT.value())) {
                result.setStatus(ClusterStatusEnum.RUNNING.value());
            }
        }
        return result;
    }

    @Override
    public Page<DevopsEnvPodVO> pagePodsByNodeName(Long clusterId, String nodeName, PageRequest pageable, String searchParam) {
        Page<DevopsEnvPodDTO> devopsEnvPodDTOPageInfo = basePageQueryPodsByNodeName(clusterId, nodeName, pageable, searchParam);
        Page<DevopsEnvPodVO> envPodVOPageInfo = ConvertUtils.convertPage(devopsEnvPodDTOPageInfo, DevopsEnvPodVO.class);

        envPodVOPageInfo.setContent(devopsEnvPodDTOPageInfo.getContent().stream().map(this::podDTO2VO).collect(Collectors.toList()));
        return envPodVOPageInfo;
    }

    @Override
    public DevopsClusterRepVO queryByCode(Long projectId, String code) {
        return ConvertUtils.convertObject(baseQueryByCode(projectId, code), DevopsClusterRepVO.class);
    }


    @Override
    public DevopsClusterDTO baseCreateCluster(DevopsClusterDTO devopsClusterDTO) {
        List<DevopsClusterDTO> devopsClusterDTOS = devopsClusterMapper.selectAll();
        if (!devopsClusterDTOS.isEmpty()) {
            // 如果数据库有集群数据，就使用第一个集群的choerodonId作为新的集群的choerodonId
            devopsClusterDTO.setChoerodonId(devopsClusterDTOS.get(0).getChoerodonId());
        } else {
            // 加上a前缀(前缀是字母即可)是为了解决随机UUID生成纯数字字符串的问题, 这样会导致agent的安装失败，
            // 因为传入的参数会变为科学计数法，而这个值(转为科学计数法的值)又被用于chart中一个configMap的名称
            // 就会因为configMap的名称不规范导致agent安装失败
            String choerodonId = "a" + GenerateUUID.generateUUID().split("-")[0];
            devopsClusterDTO.setChoerodonId(choerodonId);
        }
        if (devopsClusterMapper.insert(devopsClusterDTO) != 1) {
            throw new CommonException("devops.cluster.insert");
        }
        return devopsClusterDTO;
    }

    @Override
    public List<DevopsClusterDTO> baseListByProjectId(Long projectId, Long organizationId) {
        return devopsClusterMapper.listByProjectId(projectId, organizationId);
    }

    @Override
    public DevopsClusterDTO baseQuery(Long clusterId) {
        return devopsClusterMapper.selectByPrimaryKey(clusterId);
    }

    @Override
    public void baseUpdate(Long projectId, DevopsClusterDTO inputClusterDTO) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(inputClusterDTO.getId());
        // 内部调用不需要校验
        if (projectId != null) {
            CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        }
        inputClusterDTO.setObjectVersionNumber(devopsClusterDTO.getObjectVersionNumber());
        devopsClusterMapper.updateByPrimaryKeySelective(inputClusterDTO);
    }

    @Override
    public Page<DevopsClusterDTO> basePageClustersByOptions(Long projectId, Boolean doPage, PageRequest pageable, String params) {
        Map<String, Object> searchParamMap = TypeUtil.castMapParams(params);
        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                () -> devopsClusterMapper.listClusters(
                        projectId,
                        TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))));
    }

    @Override
    public void baseDelete(Long clusterId) {
        devopsClusterMapper.deleteByPrimaryKey(clusterId);
        devopsClusterNodeService.deleteByClusterId(clusterId);
        devopsClusterOperationRecordService.deleteByClusterId(clusterId);
    }

    @Override
    public DevopsClusterDTO baseQueryByToken(String token) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setToken(token);
        return devopsClusterMapper.selectOne(devopsClusterDTO);
    }

    @Override
    public List<DevopsClusterDTO> baseList() {
        return devopsClusterMapper.selectAll();
    }

    @Override
    public Page<DevopsEnvPodDTO> basePageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageable, String searchParam) {
        Map<String, Object> paramMap = TypeUtil.castMapParams(searchParam);
        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> devopsClusterMapper.pageQueryPodsByNodeName(
                clusterId, nodeName,
                TypeUtil.cast(paramMap.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(paramMap.get(TypeUtil.PARAMS))));
    }

    @Override
    public DevopsClusterDTO baseQueryByCode(Long projectId, String code) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setProjectId(projectId);
        devopsClusterDTO.setCode(code);
        return devopsClusterMapper.selectOne(devopsClusterDTO);
    }

    @Override
    public void baseUpdateProjectId(Long orgId, Long proId) {
        devopsClusterMapper.updateProjectId(orgId, proId);
    }

    @Override
    public Boolean checkUserClusterPermission(Long clusterId, Long userId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        if (ObjectUtils.isEmpty(devopsClusterDTO)) {
            throw new CommonException("devops.devops.cluster.is.not.exist");
        }
        if (Boolean.TRUE.equals(permissionHelper.isRoot(userId)) || Boolean.TRUE.equals(permissionHelper.isOrganizationRoot(userId, devopsClusterDTO.getOrganizationId()))) {
            return true;
        }
        if (Boolean.TRUE.equals(permissionHelper.isGitlabProjectOwner(userId, devopsClusterDTO.getProjectId()))) {
            return true;
        }
        // 获取集群和集群分配的项目Ids
        List<DevopsClusterProPermissionDTO> devopsClusterProPermissionDTOS = devopsClusterProPermissionService.baseListByClusterId(clusterId);

        return devopsClusterProPermissionDTOS.stream()
                .anyMatch(devopsClusterProPermissionDTO ->
                        permissionHelper.isGitlabProjectOwner(userId, devopsClusterProPermissionDTO.getProjectId()));
    }

    @Override
    public ClusterOverViewVO getOrganizationClusterOverview(Long organizationId) {
        List<Long> updatedClusterList = clusterConnectionHandler.getUpdatedClusterList();
        List<DevopsClusterDTO> clusterDTOList = devopsClusterMapper.listByOrganizationId(organizationId);
        if (CollectionUtils.isEmpty(clusterDTOList)) {

            return new ClusterOverViewVO(0, 0);
        }
        if (CollectionUtils.isEmpty(updatedClusterList)) {
            return new ClusterOverViewVO(0, updatedClusterList.size());
        }
        int connectedCount = 0;
        for (DevopsClusterDTO v : clusterDTOList) {
            if (updatedClusterList.contains(v.getId())) {
                connectedCount++;
            }
        }
        return new ClusterOverViewVO(connectedCount, clusterDTOList.size() - connectedCount);
    }

    @Override
    public ClusterOverViewVO getSiteClusterOverview() {
        int allCount = devopsClusterMapper.countByOptions(null, null);
        int updatedCount = clusterConnectionHandler.getUpdatedClusterList().size();
        return new ClusterOverViewVO(updatedCount, allCount - updatedCount);
    }


    /**
     * pod dto to cluster pod vo
     *
     * @param devopsEnvPodDTO pod dto
     * @return the cluster pod vo
     */
    private DevopsEnvPodVO podDTO2VO(final DevopsEnvPodDTO devopsEnvPodDTO) {
        DevopsEnvPodVO devopsEnvPodVO = ConvertUtils.convertObject(devopsEnvPodDTO, DevopsEnvPodVO.class);
        devopsEnvPodService.fillContainers(devopsEnvPodVO);
        return devopsEnvPodVO;
    }

    /**
     * convert cluster entity to instances of {@link ClusterWithNodesVO}
     *
     * @param devopsClusterRepVOS the cluster entities
     * @param projectId           the project id
     * @return the instances of the return type
     */
    private List<ClusterWithNodesVO> fromClusterE2ClusterWithNodesDTO(List<DevopsClusterRepVO> devopsClusterRepVOS, Long projectId) {
        // default three records of nodes in the instance
        PageRequest pageable = new PageRequest(1, 3);

        return devopsClusterRepVOS.stream().map(cluster -> {
            ClusterWithNodesVO clusterWithNodesDTO = new ClusterWithNodesVO();
            BeanUtils.copyProperties(cluster, clusterWithNodesDTO);
            if (clusterWithNodesDTO.getStatus().equalsIgnoreCase(ClusterStatusEnum.RUNNING.value())) {
                clusterWithNodesDTO.setNodes(clusterNodeInfoService.pageClusterNodeInfo(cluster.getId(), projectId, pageable));
            }
            return clusterWithNodesDTO;
        }).collect(Collectors.toList());
    }

    private DevopsClusterRepVO getDevopsClusterStatus(Long clusterId) {
        DevopsClusterRepVO devopsClusterRepVO = ConvertUtils.convertObject(baseQuery(clusterId), DevopsClusterRepVO.class);
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();

        if (updatedEnvList.contains(clusterId)) {
            devopsClusterRepVO.setConnect(true);
            if (devopsClusterRepVO.getStatus().equalsIgnoreCase(ClusterStatusEnum.DISCONNECT.value())) {
                devopsClusterRepVO.setStatus(ClusterStatusEnum.RUNNING.value());
            }
        }
        return devopsClusterRepVO;
    }

    @Override
    public DevopsClusterDTO insertClusterInfo(Long projectId, DevopsClusterReqVO devopsClusterReqVO, String type) {
        // 判断组织下是否还能创建集群
        checkEnableCreateClusterOrThrowE(projectId);
        ProjectDTO iamProject;
        DevopsClusterDTO devopsClusterDTO;
        iamProject = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        // 插入记录
        devopsClusterDTO = ConvertUtils.convertObject(devopsClusterReqVO, DevopsClusterDTO.class);
        devopsClusterDTO.setToken(GenerateUUID.generateUUID());
        devopsClusterDTO.setProjectId(projectId);
        devopsClusterDTO.setOrganizationId(iamProject.getOrganizationId());
        devopsClusterDTO.setSkipCheckProjectPermission(true);
        devopsClusterDTO.setType(type);
        switch (ClusterTypeEnum.valueOf(devopsClusterDTO.getType().toUpperCase())) {
            case CREATED:
                devopsClusterDTO.setStatus(ClusterStatusEnum.OPERATING.value());
                break;
            case IMPORTED:
                devopsClusterDTO.setStatus(ClusterStatusEnum.DISCONNECT.value());
                break;
            default:
        }
        return baseCreateCluster(devopsClusterDTO);
    }

    @Override
    @Transactional
    public void updateClusterStatusToOperating(Long clusterId) {
        if (devopsClusterMapper.updateClusterStatusToOperating(clusterId) != 1) {
            throw new CommonException(ClusterCheckConstant.ERROR_CLUSTER_STATUS_IS_OPERATING);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateClusterStatusToOperatingInNewTrans(Long clusterId) {
        if (devopsClusterMapper.updateClusterStatusToOperating(clusterId) != 1) {
            throw new CommonException(ClusterCheckConstant.ERROR_CLUSTER_STATUS_IS_OPERATING);
        }
    }

    @Override
    @Transactional
    public void updateStatusById(Long clusterId, ClusterStatusEnum status) {
        Assert.notNull(clusterId, ClusterCheckConstant.ERROR_CLUSTER_ID_IS_NULL);
        Assert.notNull(status, ClusterCheckConstant.ERROR_CLUSTER_STATUS_IS_NULL);

        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        devopsClusterDTO.setStatus(status.value());
        if (devopsClusterMapper.updateByPrimaryKeySelective(devopsClusterDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_CLUSTER_STATUS_FAILED);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusByIdInNewTrans(Long clusterId, ClusterStatusEnum status) {
        Assert.notNull(clusterId, ClusterCheckConstant.ERROR_CLUSTER_ID_IS_NULL);
        Assert.notNull(status, ClusterCheckConstant.ERROR_CLUSTER_STATUS_IS_NULL);

        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        devopsClusterDTO.setStatus(status.value());
        if (devopsClusterMapper.updateByPrimaryKeySelective(devopsClusterDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_CLUSTER_STATUS_FAILED);
        }
    }

    @Override
    public Long countClusterByOptions(Long projectId) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setProjectId(projectId);
        int selectCount = devopsClusterMapper.selectCount(devopsClusterDTO);
        return Long.valueOf(selectCount);
    }

    @Override
    public String disconnectionHost(Long clusterId) {
        DevopsClusterDTO clusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        if (clusterDTO == null) {
            throw new CommonException("cluster.not.exist");
        }
        return String.format(DIS_CONNECTION, clusterDTO.getCode());
    }

    @Override
    public void restartAgent(Long projectId, Long clusterId) {
        clusterConnectionHandler.checkEnvConnection(clusterId);

        agentCommandService.sendRestartAgent(clusterId);
    }

    @Override
    public void refreshDeployKey(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        if (ObjectUtils.isEmpty(devopsClusterDTO)) {
            throw new CommonException("devops.devops.cluster.is.not.exist");
        }
        clusterConnectionHandler.checkEnvConnection(clusterId);
        Long userId = DetailsHelper.getUserDetails().getUserId();
        if (!Boolean.TRUE.equals(permissionHelper.isRoot(userId))
                && Boolean.TRUE.equals(permissionHelper.isOrganizationRoot(userId, devopsClusterDTO.getOrganizationId()))
                && Boolean.TRUE.equals(permissionHelper.isGitlabProjectOwner(userId, projectId))) {
            throw new CommonException(DEVOPS_CLUSTER_NOT_EXIST);
        }
        devopsClusterMapper.updateClusterStatusToOperating(clusterId);
        ApplicationContextHelper.getContext().getBean(DevopsClusterServiceImpl.class).refreshDeployKeyAndRestart(clusterId);
    }

    @Async
    public void refreshDeployKeyAndRestart(Long clusterId) {
        try {
            List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentService.baseListUserEnvByClusterId(clusterId);
            devopsEnvironmentDTOS.forEach(GitUtil::refreshDeployKey);
            agentCommandService.sendRestartAgent(clusterId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            devopsClusterMapper.updateClusterStatusToDisconnect(clusterId);
        }
    }
}
