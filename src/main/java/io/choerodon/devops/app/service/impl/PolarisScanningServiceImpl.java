package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.ClusterCode.DEVOPS_CLUSTER_NOT_EXIST;
import static io.choerodon.devops.infra.constant.ExceptionConstants.EnvironmentCode.DEVOPS_ENV_ID_NOT_EXIST;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.polaris.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.dashboard.ProjectScoreDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;

/**
 * @author zmf
 * @since 2/17/20
 */
@Service
public class PolarisScanningServiceImpl implements PolarisScanningService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisScanningServiceImpl.class);

    private static final String ERROR_PROJECT_NOT_FOUND = "devops.project.not.found";

    /**
     * polaris扫描的超时时间
     */
    @Value("${polaris.scanning.timeout.seconds:300}")
    private Long scanningTimeoutSeconds;

    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    @Lazy
    private DevopsClusterService devopsClusterService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsPolarisRecordMapper devopsPolarisRecordMapper;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private DevopsPolarisCategoryResultMapper devopsPolarisCategoryResultMapper;
    @Autowired
    private DevopsPolarisNamespaceResultMapper devopsPolarisNamespaceResultMapper;
    @Autowired
    private DevopsPolarisNamespaceDetailMapper devopsPolarisNamespaceDetailMapper;
    @Autowired
    private DevopsPolarisCategoryDetailMapper devopsPolarisCategoryDetailMapper;
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private ClusterNodeInfoService clusterNodeInfoService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private DevopsClusterProPermissionService devopsClusterProPermissionService;

    @Override
    public DevopsPolarisRecordRespVO queryRecordByScopeAndScopeId(Long projectId, String scope, Long scopeId) {
        PolarisScopeType scopeType = PolarisScopeType.forValue(scope);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        if (projectDTO == null) {
            throw new CommonException(ERROR_PROJECT_NOT_FOUND);
        }
        if (scopeType == null) {
            return null;
        }

        // 校验权限
        Long userId = DetailsHelper.getUserDetails().getUserId();
        if (PolarisScopeType.ENV == scopeType) {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(scopeId);
            if (devopsEnvironmentDTO == null) {
                throw new CommonException(DEVOPS_ENV_ID_NOT_EXIST, scopeId);
            }
            devopsEnvUserPermissionService.checkEnvDeployPermission(DetailsHelper.getUserDetails().getUserId(), scopeId);
        } else {
            // 如果是组织管理员，则跳过项目层权限校验
            if (Boolean.FALSE.equals(permissionHelper.isOrganizationRoot(userId, projectDTO.getOrganizationId()))) {
                permissionHelper.checkProjectOwnerOrGitlabAdmin(projectId, userId);
            }
        }

        // 以上是预检
        DevopsPolarisRecordDTO devopsPolarisRecordDTO = queryRecordByScopeIdAndScope(scopeId, scope);

        // 检查是否超时
        if (devopsPolarisRecordDTO != null && checkTimeout(devopsPolarisRecordDTO.getId())) {
            devopsPolarisRecordDTO = devopsPolarisRecordMapper.selectByPrimaryKey(devopsPolarisRecordDTO.getId());
        }

        if (devopsPolarisRecordDTO == null
                || !PolarisScanningStatus.FINISHED.getStatus().equals(devopsPolarisRecordDTO.getStatus())) {
            return handleNullOrUnfinishedRecord(projectId, scopeType, scopeId, devopsPolarisRecordDTO);
        }
        DevopsPolarisRecordRespVO devopsPolarisRecordRespVO = ConvertUtils.convertObject(devopsPolarisRecordDTO, DevopsPolarisRecordRespVO.class);
        if (PolarisScopeType.ENV.getValue().equals(scope)) {
            int instanceCount = appServiceInstanceMapper.countByOptions(scopeId, null, null);
            devopsPolarisRecordRespVO.setInstanceCount((long) instanceCount);
        }
        return devopsPolarisRecordRespVO;
    }

    /**
     * 处理未扫描过时(或者未扫描完成时)需要给的数据
     *
     * @param projectId 项目id
     * @param scope     扫描范围
     * @param scopeId   envId或clusterId
     * @param recordDTO record，可以为空
     * @return 基础的数据
     */
    private DevopsPolarisRecordRespVO handleNullOrUnfinishedRecord(Long projectId, PolarisScopeType scope, Long scopeId, @Nullable DevopsPolarisRecordDTO recordDTO) {
        DevopsPolarisRecordRespVO devopsPolarisRecordRespVO = new DevopsPolarisRecordRespVO();
        if (recordDTO != null) {
            BeanUtils.copyProperties(recordDTO, devopsPolarisRecordRespVO);
        }

        if (PolarisScopeType.ENV == Objects.requireNonNull(scope)) {
            int instanceCount = appServiceInstanceMapper.countByOptions(scopeId, null, null);
            devopsPolarisRecordRespVO.setInstanceCount((long) instanceCount);
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(scopeId);
            if (devopsEnvironmentDTO == null) {
                return devopsPolarisRecordRespVO;
            }
            ClusterSummaryInfoVO clusterSummaryInfoVO = devopsClusterService.queryClusterSummaryInfo(devopsEnvironmentDTO.getClusterId());
            int podCount = devopsEnvPodMapper.countByOptions(null, devopsEnvironmentDTO.getCode(), null, null);
            devopsPolarisRecordRespVO.setPods((long) podCount);
            if (clusterSummaryInfoVO != null) {
                devopsPolarisRecordRespVO.setKubernetesVersion(clusterSummaryInfoVO.getVersion());
            }
        } else {
            ClusterSummaryInfoVO clusterSummaryInfoVO = devopsClusterService.queryClusterSummaryInfo(scopeId);
            if (clusterSummaryInfoVO == null) {
                int envCount = devopsEnvironmentMapper.countByOptions(scopeId, null, null, EnvironmentType.USER.getValue());
                devopsPolarisRecordRespVO.setNamespaces((long) envCount);
                devopsPolarisRecordRespVO.setNodes(clusterNodeInfoService.countNodes(projectId, scopeId));
            } else {
                devopsPolarisRecordRespVO.setKubernetesVersion(clusterSummaryInfoVO.getVersion());
                devopsPolarisRecordRespVO.setPods(clusterSummaryInfoVO.getPods());
                devopsPolarisRecordRespVO.setNamespaces(clusterSummaryInfoVO.getNamespaces());
                devopsPolarisRecordRespVO.setNodes(clusterSummaryInfoVO.getNodes());
            }
        }
        return devopsPolarisRecordRespVO;
    }

    @Override
    public String queryEnvPolarisResult(Long projectId, Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        if (devopsEnvironmentDTO == null) {
            throw new CommonException(DEVOPS_ENV_ID_NOT_EXIST, envId);
        }

        // 校验用户权限
        devopsEnvUserPermissionService.checkEnvDeployPermission(DetailsHelper.getUserDetails().getUserId(), envId);

        DevopsPolarisRecordDTO recordDTO = queryRecordByScopeIdAndScope(envId, PolarisScopeType.ENV.getValue());
        if (recordDTO != null) {
            Long recordId = recordDTO.getId();
            return devopsPolarisNamespaceResultMapper.queryNamespaceResultDetail(recordId, envId);
        } else {
            return JSONObject.toJSONString(devopsPolarisNamespaceResultMapper.queryInstanceWithoutResult(envId));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public DevopsPolarisRecordVO scanEnv(Long projectId, Long envId) {
        LOGGER.info("Scanning env {}", envId);
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        if (devopsEnvironmentDTO == null) {
            throw new CommonException(DEVOPS_ENV_ID_NOT_EXIST, envId);
        }
        CommonExAssertUtil.assertTrue(projectId.equals(devopsEnvironmentDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        Long clusterId = devopsEnvironmentDTO.getClusterId();

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());

        // 校验用户是否有环境的权限并且集群是否连接
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsPolarisRecordDTO devopsPolarisRecordDTO = createOrUpdateRecord(PolarisScopeType.ENV.getValue(), envId);

        agentCommandService.scanCluster(clusterId, devopsPolarisRecordDTO.getId(), devopsEnvironmentDTO.getCode());
        LOGGER.info("Finish scanning env {}", envId);
        LOGGER.info("record: {}", devopsPolarisRecordDTO);
        return ConvertUtils.convertObject(devopsPolarisRecordDTO, DevopsPolarisRecordVO.class);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public DevopsPolarisRecordVO scanCluster(Long projectId, Long clusterId) {
        LOGGER.info("scanning cluster  {}", clusterId);
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        if (devopsClusterDTO == null) {
            throw new CommonException(DEVOPS_CLUSTER_NOT_EXIST, clusterId);
        }

        // 校验项目是否拥有集群权限
        if (!projectId.equals(devopsClusterDTO.getProjectId())) {
            List<DevopsClusterProPermissionDTO> devopsClusterProPermissionDTOS = devopsClusterProPermissionService.baseListByClusterId(clusterId);
            if (CollectionUtils.isEmpty(devopsClusterProPermissionDTOS)
                    || devopsClusterProPermissionDTOS.stream().map(DevopsClusterProPermissionDTO::getProjectId).noneMatch(v -> v.equals(projectId))) {
                throw new CommonException(MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
            }
        }
        // 校验集群是否连接
        clusterConnectionHandler.checkEnvConnection(clusterId);

        DevopsPolarisRecordDTO devopsPolarisRecordDTO = createOrUpdateRecord(PolarisScopeType.CLUSTER.getValue(), clusterId);

        agentCommandService.scanCluster(clusterId, devopsPolarisRecordDTO.getId(), null);
        LOGGER.info("Finish scanning cluster {}", clusterId);
        LOGGER.info("record: {}", devopsPolarisRecordDTO);
        return ConvertUtils.convertObject(devopsPolarisRecordDTO, DevopsPolarisRecordVO.class);
    }

    @Override
    public DevopsPolarisSummaryVO clusterPolarisSummary(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        if (devopsClusterDTO == null || !Objects.equals(devopsClusterDTO.getProjectId(), projectId)) {
            throw new CommonException(DEVOPS_CLUSTER_NOT_EXIST, clusterId);
        }

        DevopsPolarisRecordDTO devopsPolarisRecordDTO = queryRecordByScopeIdAndScope(clusterId, PolarisScopeType.CLUSTER.getValue());
        // 如果没检测过
        if (devopsPolarisRecordDTO == null
                || !PolarisScanningStatus.FINISHED.getStatus().equals(devopsPolarisRecordDTO.getStatus())) {
            return new DevopsPolarisSummaryVO(Boolean.FALSE);
        }

        DevopsPolarisSummaryVO summaryVO = new DevopsPolarisSummaryVO(Boolean.TRUE);
        List<ClusterPolarisSummaryItemVO> items = devopsPolarisCategoryResultMapper.queryPolarisSummary(devopsPolarisRecordDTO.getId());
        Map<PolarisItemCategory, ClusterPolarisSummaryItemVO> map = new HashMap<>();

        items.forEach(i -> {
            PolarisItemCategory category = PolarisItemCategory.forValue(i.getCategory());
            if (category != null) {
                map.put(category, i);
            }
        });

        summaryVO.setHealthCheck(map.get(PolarisItemCategory.HEALTH_CHECK));
        summaryVO.setImageCheck(map.get(PolarisItemCategory.IMAGES));
        summaryVO.setNetworkCheck(map.get(PolarisItemCategory.NETWORKING));
        summaryVO.setResourceCheck(map.get(PolarisItemCategory.RESOURCES));
        summaryVO.setSecurityCheck(map.get(PolarisItemCategory.SECURITY));
        return summaryVO;
    }

    @Override
    public ClusterPolarisEnvDetailsVO clusterPolarisEnvDetail(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        if (devopsClusterDTO == null || !Objects.equals(devopsClusterDTO.getProjectId(), projectId)) {
            throw new CommonException(DEVOPS_CLUSTER_NOT_EXIST, clusterId);
        }


        DevopsPolarisRecordDTO recordDTO = queryRecordByScopeIdAndScope(clusterId, PolarisScopeType.CLUSTER.getValue());

        if (recordDTO == null || !PolarisScanningStatus.FINISHED.getStatus().equals(recordDTO.getStatus())) {
            return handleEnvWithoutPolaris(devopsPolarisNamespaceResultMapper.queryEnvWithoutPolarisResult(clusterId), devopsClusterDTO.getNamespaces());
        }

        return handleEnvWithPolaris(devopsPolarisNamespaceResultMapper.queryEnvWithPolarisResult(recordDTO.getId(), recordDTO.getScopeId()), devopsClusterDTO.getNamespaces());
    }

    /**
     * 处理未扫描时获取集群的namespace信息
     * 内部环境数据从传入的results参数取
     * 外部环境数据从allNamespaces参数解析得到
     *
     * @param results       内部环境结果
     * @param allNamespaces 从cluster纪录的namespaces纪录获取的内容
     * @return 处理后的结果
     */
    private ClusterPolarisEnvDetailsVO handleEnvWithoutPolaris(List<DevopsEnvWithPolarisResultVO> results, String allNamespaces) {
        Set<Long> projectIds = results.stream()
                .map(DevopsEnvWithPolarisResultVO::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<ProjectDTO> projectDTOS = baseServiceClientOperator.queryProjectsByIds(projectIds);
        List<String> internalNamespaces = new ArrayList<>();

        Map<Long, ProjectDTO> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(projectDTOS)) {
            projectDTOS.forEach(p -> map.put(p.getId(), p));
        }

        results.forEach(result -> {
            internalNamespaces.add(result.getNamespace());
            if (result.getProjectId() != null) {
                ProjectDTO projectDTO = map.get(result.getProjectId());
                if (projectDTO != null) {
                    result.setProjectCode(projectDTO.getCode());
                    result.setProjectName(projectDTO.getName());
                }
            }
        });

        List<DevopsEnvWithPolarisResultVO> externalNamespaces = generateExternalNamespaces(internalNamespaces, allNamespaces, Boolean.FALSE);

        return new ClusterPolarisEnvDetailsVO(results, externalNamespaces);
    }

    /**
     * 主要是填充项目信息
     * 和填充空的外部环境
     *
     * @param results       数据的查询结果
     * @param allNamespaces cluster纪录的namespaces字段
     */
    private ClusterPolarisEnvDetailsVO handleEnvWithPolaris(List<DevopsEnvWithPolarisResultVO> results, String allNamespaces) {
        Set<Long> projectIds = results.stream()
                .map(DevopsEnvWithPolarisResultVO::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<ProjectDTO> projectDTOS = baseServiceClientOperator.queryProjectsByIds(projectIds);
        Map<Long, ProjectDTO> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(projectDTOS)) {
            projectDTOS.forEach(p -> map.put(p.getId(), p));
        }

        List<String> existedNamespaces = new ArrayList<>();

        List<DevopsEnvWithPolarisResultVO> internals = new ArrayList<>();
        List<DevopsEnvWithPolarisResultVO> externals = new ArrayList<>();
        results.forEach(result -> {
            existedNamespaces.add(result.getNamespace());
            if (Boolean.TRUE.equals(result.getInternal())) {
                internals.add(result);
            } else {
                externals.add(result);
            }
            if (result.getProjectId() != null) {
                ProjectDTO projectDTO = map.get(result.getProjectId());
                if (projectDTO != null) {
                    result.setProjectName(projectDTO.getName());
                    result.setProjectCode(projectDTO.getCode());
                }
            }
        });

        externals.addAll(generateExternalNamespaces(existedNamespaces, allNamespaces, Boolean.TRUE));

        return new ClusterPolarisEnvDetailsVO(internals, externals);
    }

    /**
     * 生成空的外部环境的纪录
     *
     * @param existedNamespaces 数据库查询已经有的namespace纪录
     * @param allNamespaces     集群的所有namespace的json数组字符串
     * @param checked           是否扫描过
     * @return 空的外部环境的纪录
     */
    private List<DevopsEnvWithPolarisResultVO> generateExternalNamespaces(List<String> existedNamespaces, String allNamespaces, Boolean checked) {
        List<DevopsEnvWithPolarisResultVO> externalEmptyNamespaces = Collections.emptyList();
        if (!StringUtils.isEmpty(allNamespaces)) {
            List<String> namespaces = null;
            try {
                namespaces = JSONArray.parseArray(allNamespaces, String.class);
            } catch (Exception ex) {
                // Do nothing
            } finally {
                if (!CollectionUtils.isEmpty(namespaces)) {
                    externalEmptyNamespaces = namespaces.stream()
                            .filter(n -> !existedNamespaces.contains(n))
                            .map(n -> new DevopsEnvWithPolarisResultVO(n, Boolean.FALSE, checked, checked ? "[]" : null))
                            .collect(Collectors.toList());
                }
            }
        }
        return externalEmptyNamespaces;
    }

    @Override
    public DevopsPolarisRecordDTO queryRecordByScopeIdAndScope(Long scopeId, String scope) {
        return devopsPolarisRecordMapper.queryRecordByScopeIdAndScope(scopeId, scope);
    }

    private DevopsPolarisRecordDTO createOrUpdateRecord(String scope, Long scopeId) {
        DevopsPolarisRecordDTO devopsPolarisRecordDTO = new DevopsPolarisRecordDTO();
        devopsPolarisRecordDTO.setScope(scope);
        devopsPolarisRecordDTO.setScopeId(scopeId);

        // 查看数据库是否有现有纪录
        DevopsPolarisRecordDTO existedRecord = queryRecordByScopeIdAndScope(scopeId, scope);

        if (existedRecord != null) {
            // 看看是否是应该超时了
            if (checkTimeout(existedRecord.getId())) {
                existedRecord = devopsPolarisRecordMapper.selectByPrimaryKey(existedRecord.getId());
            }

            // 上一条纪录处理中时不允许再次扫描
            if (PolarisScanningStatus.OPERATING.getStatus().equals(existedRecord.getStatus())) {
                throw new CommonException("devops.polaris.scanning.operating");
            }

            // 更新扫描纪录前先清除上一次扫描相关的数据
            deleteAssociatedData(existedRecord.getId());

            // 更新纪录
            devopsPolarisRecordDTO.setId(existedRecord.getId());
            devopsPolarisRecordDTO.setLastScanDateTime(existedRecord.getLastScanDateTime());
            devopsPolarisRecordDTO.setScanDateTime(new Date());
            devopsPolarisRecordDTO.setObjectVersionNumber(existedRecord.getObjectVersionNumber());
            devopsPolarisRecordDTO.setStatus(PolarisScanningStatus.OPERATING.getStatus());
            // 更新纪录
            checkedUpdate(devopsPolarisRecordDTO);
            devopsPolarisRecordDTO = devopsPolarisRecordMapper.selectByPrimaryKey(devopsPolarisRecordDTO.getId());
        } else {
            // 没有就设置属性新增纪录
            devopsPolarisRecordDTO.setStatus(PolarisScanningStatus.OPERATING.getStatus());
            devopsPolarisRecordDTO.setScanDateTime(new Date());
            devopsPolarisRecordDTO = checkedInsert(devopsPolarisRecordDTO);
        }
        return devopsPolarisRecordDTO;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void handleAgentPolarisMessage(PolarisResponsePayloadVO message) {
        LOGGER.info("Polaris: Unhandled polaris message...");
        if (message == null) {
            LOGGER.warn("Polaris: Null message for polaris from agent.");
            return;
        }

        Long recordId = message.getRecordId();
        if (recordId == null) {
            LOGGER.warn("Polaris: Null record id for polaris message from agent.");
            return;
        }

        DevopsPolarisRecordDTO recordDTO = devopsPolarisRecordMapper.selectByPrimaryKey(recordId);
        if (recordDTO == null) {
            LogUtil.loggerInfoObjectNullWithId("DevopsPolarisRecordDTO", recordId, LOGGER);
            return;
        }

        Long clusterId;
        if (PolarisScopeType.ENV.getValue().equals(recordDTO.getScope())) {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(recordDTO.getScopeId());
            if (devopsEnvironmentDTO == null) {
                LogUtil.loggerWarnObjectNullWithId("env", recordDTO.getScopeId(), LOGGER);
                return;
            } else {
                clusterId = devopsEnvironmentDTO.getClusterId();
            }
        } else {
            clusterId = recordDTO.getScopeId();
        }


        PolarisScanResultVO polarisScanResultVO = message.getPolarisResult();
        PolarisScanSummaryVO summaryVO = polarisScanResultVO.getSummary();

        // 存储集群信息
        devopsClusterService.saveClusterSummaryInfo(clusterId, polarisScanResultVO.getAuditData().getClusterInfo());

        // 处理record信息
        LOGGER.info("Polaris: auditTime: {}", polarisScanResultVO.getAuditData().getAuditTime());
        recordDTO.setKubernetesVersion(polarisScanResultVO.getAuditData().getClusterInfo().getVersion());
        recordDTO.setLastScanDateTime(polarisScanResultVO.getAuditData().getAuditTime());
        recordDTO.setSuccesses(summaryVO.getSuccesses());
        recordDTO.setWarnings(summaryVO.getWarnings());
        recordDTO.setErrors(summaryVO.getErrors());
        recordDTO.setStatus(PolarisScanningStatus.FINISHED.getStatus());
        recordDTO.setScore(countScore(recordDTO.getSuccesses(), recordDTO.getWarnings(), recordDTO.getErrors()));
        recordDTO.setKubernetesVersion(polarisScanResultVO.getAuditData().getClusterInfo().getVersion());
        recordDTO.setPods(polarisScanResultVO.getAuditData().getClusterInfo().getPods());
        recordDTO.setNamespaces(polarisScanResultVO.getAuditData().getClusterInfo().getNamespaces());
        recordDTO.setNodes(polarisScanResultVO.getAuditData().getClusterInfo().getNodes());
        checkedUpdate(recordDTO);

        if (PolarisScopeType.CLUSTER.getValue().equals(recordDTO.getScope())) {
            // 处理扫描结果项
            handleClusterResults(recordId, polarisScanResultVO.getAuditData().getResults());
        } else {
            handleEnvResults(recordId, polarisScanResultVO.getAuditData().getResults());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void handleAgentPolarisMessageFromHttp(String token, Long clusterId, PolarisResponsePayloadVO message) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        if (devopsClusterDTO == null) {
            LogUtil.loggerWarnObjectNullWithId("Cluster", TypeUtil.objToLong(clusterId), LOGGER);
            return;
        }
        if (!token.equals(devopsClusterDTO.getToken())) {
            LOGGER.warn("Cluster with id {} exists but its token doesn't match the token that agent offers as {}", clusterId, token);
            return;
        }

        LOGGER.info("Receive polaris result from agent with cluster id {}", clusterId);
        handleAgentPolarisMessage(message);
    }


    /**
     * 处理环境扫描的结果
     * 设计两个表：
     * devops_polaris_namespace_result
     * devops_polaris_namespace_detail
     *
     * @param recordId 扫描环境的扫描纪录id
     * @param results  扫描结果
     */
    private void handleEnvResults(Long recordId, List<PolarisControllerResultVO> results) {
        if (CollectionUtils.isEmpty(results)) {
            LOGGER.info("Polaris: env controller results empty... {}", results);
            return;
        }

        DevopsPolarisRecordDTO devopsPolarisRecordDTO = devopsPolarisRecordMapper.selectByPrimaryKey(recordId);
        Long envId = devopsPolarisRecordDTO.getScopeId();
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        if (devopsEnvironmentDTO == null) {
            LogUtil.loggerInfoObjectNullWithId("env", envId, LOGGER);
            LOGGER.info("Polaris: skip env polaris result for env with id {}", envId);
            return;
        }

        // 以上是预检
        // 以下是查出环境下的实例然后和检测项关联起来存成json放入数据库
        DevopsPolarisNamespaceResultDTO devopsPolarisNamespaceResultDTO = new DevopsPolarisNamespaceResultDTO(envId, devopsEnvironmentDTO.getCode(), recordId, Boolean.FALSE);

        Map<Long, List<PolarisStorageControllerResultVO>> map = new HashMap<>();
        results.forEach(result -> {
            Long instanceId = findAssociatedInstanceId(envId, result.getName(), result.getKind());
            if (instanceId == null) {
                return;
            }
            // 挑选出各个层级未通过的检测项放入到要作为json传出到数据库的对象中
            map.computeIfAbsent(instanceId, id -> new ArrayList<>()).add(analyzePolarisResult(result));
        });

        // 查出实例信息
        List<InstanceWithPolarisStorageVO> instances = appServiceInstanceMapper.queryInstancesWithAppServiceByIds(new ArrayList<>(map.keySet()));
        // 这里是只将有实例对应的扫描结果取出来
        instances.forEach(ins -> {
            ins.setItems(map.get(ins.getInstanceId()));
            ins.getItems()
                    .stream()
                    .map(PolarisStorageControllerResultVO::getHasErrors)
                    .reduce((one, another) -> one || another)
                    .ifPresent(ins::setHasErrors);
        });
        DevopsPolarisNamespaceDetailDTO detailDTO = new DevopsPolarisNamespaceDetailDTO(JSONObject.toJSONString(instances));
        checkedInsertNamespaceDetail(detailDTO);
        devopsPolarisNamespaceResultDTO.setDetailId(detailDTO.getId());
        checkedInsertNamespaceResult(devopsPolarisNamespaceResultDTO);
    }

    /**
     * 挑选出各个层级未通过的检测项放入到要作为json传出到数据库的对象中
     */
    private PolarisStorageControllerResultVO analyzePolarisResult(PolarisControllerResultVO result) {
        // 这个是存储到数据库的json结构
        PolarisStorageControllerResultVO storageControllerResultVO = new PolarisStorageControllerResultVO();
        storageControllerResultVO.setHasErrors(Boolean.FALSE);
        storageControllerResultVO.setPodResult(new PolarisStoragePodResultVO());
        BeanUtils.copyProperties(result, storageControllerResultVO, "podResult", "results");
        storageControllerResultVO.getPodResult().setName(result.getPodResult().getName());
        storageControllerResultVO.getPodResult().setContainerResults(new ArrayList<>());

        // 挑选出各个层级未通过的检测项放入到要作为json传出到数据库的对象中
        storageControllerResultVO.setResults(pickNonPassedItems(convertItemFromMap(result.getResults())));
        if (!storageControllerResultVO.getResults().isEmpty()
                && hasErrors(storageControllerResultVO.getResults())) {
            storageControllerResultVO.setHasErrors(Boolean.TRUE);
        }

        storageControllerResultVO.getPodResult().setResults(pickNonPassedItems(convertItemFromMap(result.getPodResult().getResults())));
        if (!storageControllerResultVO.getPodResult().getResults().isEmpty()
                && !storageControllerResultVO.getHasErrors()
                && hasErrors(storageControllerResultVO.getPodResult().getResults())) {
            storageControllerResultVO.setHasErrors(Boolean.TRUE);
        }

        result.getPodResult()
                .getContainerResults()
                .forEach(c -> {
                    PolarisStorageContainerResultVO containerResultVO = new PolarisStorageContainerResultVO();
                    containerResultVO.setName(c.getName());
                    List<PolarisResultItemVO> containerResults = convertItemFromMap(c.getResults());
                    containerResultVO.setResults(pickNonPassedItems(containerResults));
                    if (!containerResultVO.getResults().isEmpty()
                            && !storageControllerResultVO.getHasErrors()
                            && hasErrors(containerResultVO.getResults())) {
                        storageControllerResultVO.setHasErrors(Boolean.TRUE);
                    }
                    storageControllerResultVO.getPodResult().getContainerResults().add(containerResultVO);
                });
        return storageControllerResultVO;
    }

    /**
     * 这列表中是否有error级别的检测项
     *
     * @param items 检测项列表
     * @return true表示有
     */
    private static boolean hasErrors(List<PolarisResultItemVO> items) {
        for (PolarisResultItemVO item : items) {
            if (hasError(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是error级别的检测项
     *
     * @param item 检测项结果
     * @return true表示是
     */
    private static boolean hasError(PolarisResultItemVO item) {
        return Boolean.FALSE.equals(item.getSuccess())
                && PolarisSeverity.ERROR.getValue().equals(item.getSeverity());
    }

    /**
     * 处理集群扫描详细的数据
     * 涉及四个表:
     * devops_polaris_namespace_detail
     * devops_polaris_namespace_record
     * devops_polaris_category_record
     * devops_polaris_category_detail
     *
     * @param recordId 扫描纪录id
     * @param results  详细的扫描数据
     */
    private void handleClusterResults(Long recordId, List<PolarisControllerResultVO> results) {
        if (CollectionUtils.isEmpty(results)) {
            LOGGER.info("Polaris: controller results empty... {}", results);
            return;
        }

        DevopsPolarisRecordDTO devopsPolarisRecordDTO = devopsPolarisRecordMapper.selectByPrimaryKey(recordId);
        Long finalClusterId = devopsPolarisRecordDTO.getScopeId();

        // 这两个map分别是从环境和检测项类别角度对数据进行处理的结果
        Map<String, List<PolarisStorageControllerResultVO>> namespaceMap = new HashMap<>();
        Map<String, List<PolarisSummaryItemContentVO>> categoryMap = new HashMap<>();

        results.forEach(controllerResult -> {
            // 有公共的资源的信息的模板
            PolarisSummaryItemContentVO template = new PolarisSummaryItemContentVO(controllerResult.getNamespace(), controllerResult.getKind(), controllerResult.getName(), null, Boolean.FALSE);


            // 这个是存储到数据库的json结构
            PolarisStorageControllerResultVO storageControllerResultVO = new PolarisStorageControllerResultVO(Boolean.FALSE);
            storageControllerResultVO.setPodResult(new PolarisStoragePodResultVO());
            BeanUtils.copyProperties(controllerResult, storageControllerResultVO, "podResult", "results");
            storageControllerResultVO.getPodResult().setName(controllerResult.getPodResult().getName());
            storageControllerResultVO.getPodResult().setContainerResults(new ArrayList<>());

            // 挑选出各个层级未通过的检测项放入到storageControllerResultVO
            List<PolarisResultItemVO> allItems = new ArrayList<>();

            List<PolarisResultItemVO> controllerItems = convertItemFromMap(controllerResult.getResults());
            storageControllerResultVO.setResults(pickNonPassedItems(controllerItems));

            List<PolarisResultItemVO> podItems = convertItemFromMap(controllerResult.getPodResult().getResults());
            storageControllerResultVO.getPodResult().setResults(pickNonPassedItems(podItems));

            List<PolarisResultItemVO> containerItems = new ArrayList<>();
            controllerResult.getPodResult()
                    .getContainerResults()
                    .forEach(c -> {
                        PolarisStorageContainerResultVO containerResultVO = new PolarisStorageContainerResultVO();
                        containerResultVO.setName(c.getName());
                        List<PolarisResultItemVO> containerResults = convertItemFromMap(c.getResults());
                        containerResultVO.setResults(pickNonPassedItems(containerResults));
                        containerItems.addAll(containerResults);
                        storageControllerResultVO.getPodResult().getContainerResults().add(containerResultVO);
                    });

            allItems.addAll(controllerItems);
            allItems.addAll(podItems);
            allItems.addAll(containerItems);
            if (hasErrors(allItems)) {
                storageControllerResultVO.setHasErrors(Boolean.TRUE);
            }

            Map<String, List<PolarisResultItemVO>> itemMap = allItems.stream().collect(Collectors.groupingBy(PolarisResultItemVO::getCategory, HashMap::new, Collectors.toList()));
            itemMap.forEach((category, cItems) -> {
                PolarisSummaryItemContentVO actual = new PolarisSummaryItemContentVO();
                BeanUtils.copyProperties(template, actual);
                actual.setItems(cItems.stream().map(this::convert).collect(Collectors.toList()));
                // 不论是否通过都放入map，用于之后计算分数
                categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(actual);
            });

            namespaceMap.computeIfAbsent(storageControllerResultVO.getNamespace(), n -> new ArrayList<>()).add(storageControllerResultVO);
        });


        LOGGER.info("Polaris: finished analyzing message...");
        LOGGER.info("Polaris: the namespaceMap size is {}", namespaceMap.size());
        LOGGER.info("Polaris: the categoryMap size is {}", categoryMap.size());
        // 处理 devops_polaris_namespace_result 数据
        handleNamespaceResultList(analyzeNamespaceResults(namespaceMap, finalClusterId, recordId));
        // 批量插入 devops_polaris_category_record 纪录
        handleCategoryResultList(analyzeCategoryResults(categoryMap, recordId));
    }

    private List<DevopsPolarisNamespaceResultDTO> analyzeNamespaceResults(Map<String, List<PolarisStorageControllerResultVO>> namespaceMap, Long clusterId, Long recordId) {
        List<DevopsPolarisNamespaceResultDTO> namespaceResults = new ArrayList<>();
        namespaceMap.forEach((namespace, controllers) -> {
            Long envId = queryEnvIdByClusterIdAndNamespace(clusterId, namespace);
            DevopsPolarisNamespaceResultDTO result = new DevopsPolarisNamespaceResultDTO(envId, namespace, recordId, Boolean.FALSE);
            result.setDetail(JSONObject.toJSONString(controllers));
            controllers.stream()
                    .map(PolarisStorageControllerResultVO::getHasErrors)
                    .reduce((one, another) -> one || another)
                    .ifPresent(result::setHasErrors);
            namespaceResults.add(result);
        });
        return namespaceResults;
    }

    private List<DevopsPolarisCategoryResultDTO> analyzeCategoryResults(Map<String, List<PolarisSummaryItemContentVO>> categoryMap, Long recordId) {
        List<DevopsPolarisCategoryResultDTO> categoryResults = new ArrayList<>();
        categoryMap.forEach((category, cResults) -> {
            DevopsPolarisCategoryResultDTO cResult = new DevopsPolarisCategoryResultDTO(category, recordId, null, null, Boolean.FALSE, null, cResults);
            countItemScore(cResult);
            cResult.setDetail(JSONObject.toJSONString(cResult.getItems()));
            categoryResults.add(cResult);
        });
        return categoryResults;
    }

    /**
     * 计算分值并移除通过的检测项
     *
     * @param categoryItems 数据
     */
    private static void countItemScore(DevopsPolarisCategoryResultDTO categoryItems) {
        long errors = 0;
        long warnings = 0;
        long successes = 0;
        // TODO 想办法优化
        Iterator<PolarisSummaryItemContentVO> iterator = categoryItems.getItems().iterator();
        while (iterator.hasNext()) {
            PolarisSummaryItemContentVO content = iterator.next();
            content.setHasErrors(Boolean.FALSE);
            Iterator<PolarisSummaryItemDetailVO> detailIterator = content.getItems().iterator();

            while (detailIterator.hasNext()) {
                PolarisSummaryItemDetailVO detail = detailIterator.next();
                if (Boolean.TRUE.equals(detail.getApproved())) {
                    successes++;
                    detailIterator.remove();
                } else {
                    if (PolarisSeverity.IGNORE.getValue().equals(detail.getSeverity())) {
                        detailIterator.remove();
                        successes++;
                    } else if (PolarisSeverity.WARNING.getValue().equals(detail.getSeverity())) {
                        warnings++;
                    } else if (PolarisSeverity.ERROR.getValue().equals(detail.getSeverity())) {
                        errors++;
                        content.setHasErrors(Boolean.TRUE);
                        categoryItems.setHasErrors(Boolean.TRUE);
                    }
                }
            }

            if (content.getItems().isEmpty()) {
                iterator.remove();
            }
        }
        categoryItems.setScore(countScore(successes, warnings, errors));
    }

    private PolarisSummaryItemDetailVO convert(PolarisResultItemVO item) {
        PolarisSummaryItemDetailVO polarisSummaryItemDetailVO = new PolarisSummaryItemDetailVO();
        polarisSummaryItemDetailVO.setApproved(item.getSuccess());
        polarisSummaryItemDetailVO.setMessage(item.getMessage());
        polarisSummaryItemDetailVO.setType(item.getId());
        polarisSummaryItemDetailVO.setSeverity(item.getSeverity());
        return polarisSummaryItemDetailVO;
    }

    @Nullable
    private Long queryEnvIdByClusterIdAndNamespace(Long clusterId, String namespace) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryByClusterIdAndCode(clusterId, namespace);
        if (devopsEnvironmentDTO != null && EnvironmentType.USER.getValue().equals(devopsEnvironmentDTO.getType())) {
            return devopsEnvironmentDTO.getId();
        }
        return null;
    }

    /**
     * 将未通过的item挑出来，放在新的列表
     *
     * @param items 检测项
     * @return 未通过检测的
     */
    private static List<PolarisResultItemVO> pickNonPassedItems(List<PolarisResultItemVO> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<PolarisResultItemVO> results = new ArrayList<>();
        items.forEach(item -> {
            if (!judgePassed(item)) {
                results.add(item);
            }
        });
        return results;
    }

    /**
     * 判断检测项是通过的吗
     *
     * @param item 检测项
     * @return true表示通过
     */
    private static boolean judgePassed(PolarisResultItemVO item) {
        if (item == null) {
            return false;
        }
        return Boolean.TRUE.equals(item.getSuccess()) || PolarisSeverity.IGNORE.getValue().equals(item.getSeverity());
    }

    /**
     * 计算分值
     *
     * @param successes 通过项
     * @param warnings  警告项
     * @param errors    错误项
     * @return 分值
     */
    private static long countScore(Long successes, Long warnings, Long errors) {
        // 分值： pass项数量/（pass项数量+1/2warning项数量+error项数量）
        // 分母
        double denominator = (successes + warnings / 2.0 + errors);
        // 分子
        long numerator = successes;
        // 四舍五入
        return Math.round(numerator / denominator * 100);
    }

    /**
     * 处理 devops_polaris_namespace_result 数据
     *
     * @param polarisNamespaceResultDTOS 待插入的数据
     */
    private void handleNamespaceResultList(List<DevopsPolarisNamespaceResultDTO> polarisNamespaceResultDTOS) {
        LOGGER.info("Polaris: handleNamespaceResultList");
        if (CollectionUtils.isEmpty(polarisNamespaceResultDTOS)) {
            return;
        }
        LOGGER.info("Polaris: handleNamespaceResultList: the size is {}", polarisNamespaceResultDTOS.size());
        DevopsPolarisNamespaceDetailDTO detailDTO = new DevopsPolarisNamespaceDetailDTO();
        // 这里无法将detail纪录批量插入
        polarisNamespaceResultDTOS.forEach(i -> {
            detailDTO.setId(null);
            detailDTO.setDetail(i.getDetail());
            MapperUtil.resultJudgedInsertSelective(devopsPolarisNamespaceDetailMapper, detailDTO, "devops.insert.polaris.namespace.detail");
            i.setDetailId(detailDTO.getId());
        });
        devopsPolarisNamespaceResultMapper.batchInsert(polarisNamespaceResultDTOS);
        LOGGER.info("Polaris: handleNamespaceResultList: finished...");
    }

    /**
     * 处理 devops_polaris_category_result 数据
     *
     * @param categoryResultDTOList 待插入的数据
     */
    private void handleCategoryResultList(List<DevopsPolarisCategoryResultDTO> categoryResultDTOList) {
        if (CollectionUtils.isEmpty(categoryResultDTOList)) {
            return;
        }
        DevopsPolarisCategoryDetailDTO detailDTO = new DevopsPolarisCategoryDetailDTO();
        // 这里无法将detail纪录批量插入
        categoryResultDTOList.forEach(i -> {
            detailDTO.setId(null);
            detailDTO.setDetail(i.getDetail());
            MapperUtil.resultJudgedInsertSelective(devopsPolarisCategoryDetailMapper, detailDTO, "devops.insert.polaris.category.detail");
            i.setDetailId(detailDTO.getId());
        });
        devopsPolarisCategoryResultMapper.batchInsert(categoryResultDTOList);
    }

    /**
     * 从map中读取{@link PolarisResultItemVO}列表
     *
     * @param resultMap 特定结构的map
     * @return 读取的列表
     */
    private List<PolarisResultItemVO> convertItemFromMap(Map<String, Object> resultMap) {
        if (CollectionUtils.isEmpty(resultMap)) {
            return Collections.emptyList();
        }

        List<PolarisResultItemVO> itemList = new ArrayList<>();
        resultMap.values().forEach(value -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> itemMap = (Map<String, Object>) value;
            PolarisResultItemVO item = new PolarisResultItemVO();
            item.setCategory(String.valueOf(itemMap.get("category")));
            item.setId(String.valueOf(itemMap.get("id")));
            item.setMessage(String.valueOf(itemMap.get("message")));
            item.setSeverity(String.valueOf(itemMap.get("severity")));
            item.setSuccess(Boolean.valueOf(String.valueOf(itemMap.get("success"))));
            itemList.add(item);
        });
        return itemList;
    }

    /**
     * 找到这个资源关联的实例id
     *
     * @param envId        环境id
     * @param resourceName 资源名称
     * @param resourceKind 资源类型
     * @return 实例id，可为null
     */
    @Nullable
    private Long findAssociatedInstanceId(Long envId, String resourceName, String resourceKind) {
        DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceService.baseQueryOptions(null, null, envId, resourceKind, resourceName);
        return devopsEnvResourceDTO == null ? null : devopsEnvResourceDTO.getInstanceId();
    }

    @Override
    public boolean checkTimeout(Long recordId) {
        DevopsPolarisRecordDTO devopsPolarisRecordDTO = devopsPolarisRecordMapper.selectByPrimaryKey(recordId);
        if (devopsPolarisRecordDTO == null) {
            return false;
        }
        if (!PolarisScanningStatus.OPERATING.getStatus().equals(devopsPolarisRecordDTO.getStatus())) {
            return false;
        }

        Long startMills = devopsPolarisRecordDTO.getScanDateTime().getTime();
        Long currentMills = System.currentTimeMillis();

        // 计算是否超时
        if ((currentMills - startMills) > this.scanningTimeoutSeconds * 1000) {
            devopsPolarisRecordDTO.setStatus(PolarisScanningStatus.TIMEOUT.getStatus());
            checkedUpdate(devopsPolarisRecordDTO);
            return true;
        } else {
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void deleteAssociatedData(Long recordId) {
        deleteDevopsPolarisCategoryDetailByRecordId(recordId);
        deleteDevopsPolarisCategoryResultId(recordId);
        deleteDevopsPolarisResultDetailByRecordId(recordId);
        deleteDevopsPolarisInstanceResultByRecordId(recordId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void deleteAllByScopeAndScopeId(PolarisScopeType scope, Long scopeId) {
        DevopsPolarisRecordDTO devopsPolarisRecordDTO = queryRecordByScopeIdAndScope(scopeId, scope.getValue());
        if (devopsPolarisRecordDTO != null) {
            deleteAssociatedData(devopsPolarisRecordDTO.getId());
            devopsPolarisRecordMapper.deleteByPrimaryKey(devopsPolarisRecordDTO.getId());
        }
    }

    @Override
    public Map<Long, Double> listProjectScores(List<Long> actualPids) {
        List<ProjectScoreDTO> devopsPolarisRecordDTOS = devopsPolarisRecordMapper.listProjectScores(actualPids);
        if (CollectionUtils.isEmpty(devopsPolarisRecordDTOS)) {
            return new HashMap<>();
        }
        return devopsPolarisRecordDTOS.stream().collect(Collectors.groupingBy(ProjectScoreDTO::getProjectId, Collectors.averagingDouble(ProjectScoreDTO::getScore)));
    }

    private void deleteDevopsPolarisCategoryResultId(Long recordId) {
        DevopsPolarisCategoryResultDTO deleteCondition = new DevopsPolarisCategoryResultDTO();
        deleteCondition.setRecordId(Objects.requireNonNull(recordId));
        devopsPolarisCategoryResultMapper.delete(deleteCondition);
    }

    private void deleteDevopsPolarisCategoryDetailByRecordId(Long recordId) {
        List<Long> detailIds = devopsPolarisCategoryDetailMapper.queryDetailIdsByRecordId(Objects.requireNonNull(recordId));
        if (detailIds.isEmpty()) {
            return;
        }
        devopsPolarisCategoryDetailMapper.batchDelete(detailIds);
    }

    private void deleteDevopsPolarisInstanceResultByRecordId(Long recordId) {
        DevopsPolarisNamespaceResultDTO deleteCondition = new DevopsPolarisNamespaceResultDTO();
        deleteCondition.setRecordId(Objects.requireNonNull(recordId));
        devopsPolarisNamespaceResultMapper.delete(deleteCondition);
    }

    private void deleteDevopsPolarisResultDetailByRecordId(Long recordId) {
        List<Long> detailIds = devopsPolarisNamespaceDetailMapper.queryDetailIdsByRecordId(Objects.requireNonNull(recordId));
        if (detailIds.isEmpty()) {
            return;
        }
        devopsPolarisNamespaceDetailMapper.batchDelete(detailIds);
    }

    /**
     * 插入纪录
     *
     * @param devopsPolarisRecordDTO 纪录
     * @return 插入的纪录
     */
    private DevopsPolarisRecordDTO checkedInsert(DevopsPolarisRecordDTO devopsPolarisRecordDTO) {
        return MapperUtil.resultJudgedInsertSelective(devopsPolarisRecordMapper, devopsPolarisRecordDTO, "devops.insert.polaris.record");
    }

    /**
     * 更新纪录
     *
     * @param devopsPolarisRecordDTO 纪录
     */
    private void checkedUpdate(DevopsPolarisRecordDTO devopsPolarisRecordDTO) {
        Objects.requireNonNull(devopsPolarisRecordDTO.getId());
        MapperUtil.resultJudgedUpdateByPrimaryKey(devopsPolarisRecordMapper, devopsPolarisRecordDTO, "devops.update.polaris.record");
    }

    private DevopsPolarisNamespaceDetailDTO checkedInsertNamespaceDetail(DevopsPolarisNamespaceDetailDTO detailDTO) {
        Objects.requireNonNull(detailDTO.getDetail());
        return MapperUtil.resultJudgedInsertSelective(devopsPolarisNamespaceDetailMapper, detailDTO, "devops.insert.polaris.namespace.detail");
    }

    private DevopsPolarisNamespaceResultDTO checkedInsertNamespaceResult(DevopsPolarisNamespaceResultDTO resultDTO) {
        return MapperUtil.resultJudgedInsertSelective(devopsPolarisNamespaceResultMapper, resultDTO, "devops.insert.polaris.namespace.record");
    }
}
