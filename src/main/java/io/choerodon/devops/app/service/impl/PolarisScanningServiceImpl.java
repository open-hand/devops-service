package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.polaris.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.LogUtil;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * @author zmf
 * @since 2/17/20
 */
@Service
public class PolarisScanningServiceImpl implements PolarisScanningService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisScanningServiceImpl.class);

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
    private DevopsPolarisItemMapper devopsPolarisItemMapper;
    @Autowired
    private DevopsPolarisInstanceResultMapper devopsPolarisInstanceResultMapper;
    @Autowired
    private DevopsPolarisResultDetailMapper devopsPolarisResultDetailMapper;
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

    @Override
    public DevopsPolarisRecordRespVO queryRecordByScopeAndScopeId(Long projectId, String scope, Long scopeId) {
        PolarisScopeType scopeType = PolarisScopeType.forValue(scope);
        if (scopeType == null) {
            return null;
        }
        DevopsPolarisRecordDTO devopsPolarisRecordDTO = queryRecordByScopeIdAndScope(scopeId, scope);
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
    public List<InstanceWithPolarisResultVO> queryEnvPolarisResult(Long projectId, Long envId) {
        DevopsPolarisRecordDTO recordDTO = queryRecordByScopeIdAndScope(envId, PolarisScopeType.ENV.getValue());
        Long recordId = null;
        if (recordDTO != null) {
            recordId = recordDTO.getId();
        }

        return devopsPolarisInstanceResultMapper.queryInstanceWithResult(recordId, envId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public DevopsPolarisRecordVO scanEnv(Long projectId, Long envId) {
        LOGGER.info("Scanning env {}", envId);
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        if (devopsEnvironmentDTO == null) {
            throw new CommonException("error.env.id.not.exist", envId);
        }

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
            throw new CommonException("error.cluster.not.exist", clusterId);
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
            throw new CommonException("error.cluster.not.exist", clusterId);
        }

        DevopsPolarisRecordDTO devopsPolarisRecordDTO = queryRecordByScopeIdAndScope(clusterId, PolarisScopeType.CLUSTER.getValue());
        // 如果没检测过
        if (devopsPolarisRecordDTO == null
                || !PolarisScanningStatus.FINISHED.getStatus().equals(devopsPolarisRecordDTO.getStatus())) {
            return new DevopsPolarisSummaryVO(Boolean.FALSE);
        }

        DevopsPolarisSummaryVO summaryVO = new DevopsPolarisSummaryVO(Boolean.TRUE);
        List<ClusterPolarisSummaryItemVO> items = devopsPolarisItemMapper.queryPolarisSummary(devopsPolarisRecordDTO.getId());
        LOGGER.info("Polaris: item size: {}", items.size());
        Map<PolarisItemCategory, ClusterPolarisSummaryItemVO> map = new HashMap<>();

        items.forEach(i -> {
            LOGGER.info("Polaris: category: {}", i.getCategory());
            PolarisItemCategory category = PolarisItemCategory.forValue(i.getCategory());
            if (category != null) {
                map.put(category, i);
                countItemScore(i);
            }
        });

        summaryVO.setHealthCheck(map.get(PolarisItemCategory.HEALTH_CHECK));
        summaryVO.setImageCheck(map.get(PolarisItemCategory.IMAGES));
        summaryVO.setNetworkCheck(map.get(PolarisItemCategory.NETWORKING));
        summaryVO.setResourceCheck(map.get(PolarisItemCategory.RESOURCES));
        summaryVO.setSecurityCheck(map.get(PolarisItemCategory.SECURITY));
        return summaryVO;
    }

    private void countItemScore(ClusterPolarisSummaryItemVO item) {
        long errors = 0;
        long warnings = 0;
        long successes = 0;
        // TODO 想办法优化
        item.setHasErrors(Boolean.FALSE);
        for (ClusterPolarisSummaryItemContentVO content : item.getItems()) {
            for (ClusterPolarisSummaryItemDetailVO detail : content.getItems()) {
                if (Boolean.TRUE.equals(detail.getApproved())) {
                    successes++;
                } else {
                    if (PolarisSeverity.IGNORE.getValue().equals(detail.getSeverity())) {
                        successes++;
                    } else if (PolarisSeverity.WARNING.getValue().equals(detail.getSeverity())) {
                        warnings++;
                    } else if (PolarisSeverity.ERROR.getValue().equals(detail.getSeverity())) {
                        errors++;
                        item.setHasErrors(Boolean.TRUE);
                    }
                }
            }
        }
        item.setScore(countScore(successes, warnings, errors));
    }

    @Override
    public List<DevopsEnvWithPolarisResultVO> clusterPolarisEnvDetail(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        if (devopsClusterDTO == null || !Objects.equals(devopsClusterDTO.getProjectId(), projectId)) {
            throw new CommonException("error.cluster.not.exist", clusterId);
        }


        DevopsPolarisRecordDTO recordDTO = queryRecordByScopeIdAndScope(clusterId, PolarisScopeType.CLUSTER.getValue());

        if (recordDTO == null || !PolarisScanningStatus.FINISHED.getStatus().equals(recordDTO.getStatus())) {
            return handleEnvWithoutPolaris(devopsPolarisInstanceResultMapper.queryEnvWithoutPolarisResult(clusterId));
        }

        return handleEnvWithPolaris(devopsPolarisInstanceResultMapper.queryEnvWithPolarisResult(recordDTO.getId()));
    }

    private List<DevopsEnvWithPolarisResultVO> handleEnvWithoutPolaris(List<DevopsEnvWithPolarisResultVO> results) {
        Set<Long> projectIds = results.stream()
                .map(DevopsEnvWithPolarisResultVO::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<ProjectDTO> projectDTOS = baseServiceClientOperator.queryProjectsByIds(projectIds);

        Map<Long, ProjectDTO> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(projectDTOS)) {
            projectDTOS.forEach(p -> map.put(p.getId(), p));
        }

        results.forEach(result -> {
            if (result.getProjectId() != null) {
                ProjectDTO projectDTO = map.get(result.getProjectId());
                if (projectDTO != null) {
                    result.setProjectCode(projectDTO.getCode());
                    result.setProjectName(projectDTO.getName());
                }
            }
        });

        return results;
    }

    private List<DevopsEnvWithPolarisResultVO> handleEnvWithPolaris(List<DevopsEnvWithPolarisResultVO> results) {
        Set<Long> projectIds = results.stream()
                .map(DevopsEnvWithPolarisResultVO::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<ProjectDTO> projectDTOS = baseServiceClientOperator.queryProjectsByIds(projectIds);
        Map<Long, ProjectDTO> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(projectDTOS)) {
            projectDTOS.forEach(p -> map.put(p.getId(), p));
        }

        results.forEach(result -> {
            if (result.getProjectId() != null) {
                ProjectDTO projectDTO = map.get(result.getProjectId());
                if (projectDTO != null) {
                    result.setProjectName(projectDTO.getName());
                    result.setProjectCode(projectDTO.getCode());
                }
            }
            if (result.getItemHasErrors() != null) {
                Optional<Boolean> hasErrors = result.getItemHasErrors().stream().reduce((one, another) -> mapNullToFalse(one) && mapNullToFalse(another));
                hasErrors.ifPresent(result::setHasErrors);
            }
        });

        return results;
    }

    private static Boolean mapNullToFalse(Boolean value) {
        return value == null ? Boolean.FALSE : value;
    }

    @Override
    public DevopsPolarisRecordDTO queryRecordByScopeIdAndScope(Long scopeId, String scope) {
        // TODO 待使用索引优化
        DevopsPolarisRecordDTO devopsPolarisRecordDTO = new DevopsPolarisRecordDTO();
        devopsPolarisRecordDTO.setScope(Objects.requireNonNull(scope));
        devopsPolarisRecordDTO.setScopeId(Objects.requireNonNull(scopeId));
        return devopsPolarisRecordMapper.selectOne(devopsPolarisRecordDTO);
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
                throw new CommonException("error.polaris.scanning.operating");
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

        // 处理扫描结果项
        handleResult(recordId, polarisScanResultVO.getAuditData().getResults());
    }

    /**
     * 处理详细的数据
     *
     * @param recordId 扫描纪录id
     * @param results  详细的扫描数据
     */
    private void handleResult(Long recordId, List<PolarisControllerResultVO> results) {
        if (CollectionUtils.isEmpty(results)) {
            LOGGER.info("Polaris: controller results empty... {}", results);
            return;
        }

        DevopsPolarisRecordDTO devopsPolarisRecordDTO = devopsPolarisRecordMapper.selectByPrimaryKey(recordId);
        Long envId = null;
        Long clusterId = null;
        if (PolarisScopeType.ENV.getValue().equals(devopsPolarisRecordDTO.getScope())) {
            envId = devopsPolarisRecordDTO.getScopeId();
        } else {
            clusterId = devopsPolarisRecordDTO.getScopeId();
        }

        List<DevopsPolarisItemDTO> items = new ArrayList<>();
        // 没有detailId而有detail的纪录列表
        List<DevopsPolarisInstanceResultDTO> rawInstanceResultList = new ArrayList<>();

        final Long finalEnvId = envId;
        final Long finalClusterId = clusterId;
        results.forEach(controllerResult -> {
            int errorCount = 0;
            Long confirmedEnvId = null;
            if (finalEnvId == null) {
                DevopsEnvironmentDTO controllerEnv = devopsEnvironmentService.baseQueryByClusterIdAndCode(finalClusterId, controllerResult.getNamespace());
                // 只考虑用户创建的环境
                if (controllerEnv != null && EnvironmentType.USER.getValue().equals(controllerEnv.getType())) {
                    confirmedEnvId = controllerEnv.getId();
                }
            } else {
                confirmedEnvId = finalEnvId;
            }

            Long instanceId = null;
            if (confirmedEnvId != null) {
                instanceId = findAssociatedInstanceId(confirmedEnvId, controllerResult.getName(), controllerResult.getKind());
            }

            // 收集 devops_polaris_instance_result 表相关数据
            DevopsPolarisInstanceResultDTO instanceResult = new DevopsPolarisInstanceResultDTO(
                    confirmedEnvId,
                    instanceId,
                    controllerResult.getNamespace(),
                    controllerResult.getName(),
                    controllerResult.getKind(),
                    recordId, null,
                    JSONObject.toJSONString(controllerResult));
            rawInstanceResultList.add(instanceResult);

            // 以下是处理 devops_polaris_item 表相关数据
            DevopsPolarisItemDTO template = new DevopsPolarisItemDTO(confirmedEnvId, controllerResult.getNamespace(), controllerResult.getName(), controllerResult.getKind(), recordId);
            List<PolarisResultItemVO> allItems = new ArrayList<>();
            List<PolarisResultItemVO> controllerItems = convertItemFromMap(controllerResult.getResults());
            List<PolarisResultItemVO> podItems = convertItemFromMap(controllerResult.getPodResult().getResults());
            List<PolarisResultItemVO> containerItems = new ArrayList<>();
            controllerResult.getPodResult().getContainerResults().forEach(c -> containerItems.addAll(convertItemFromMap(c.getResults())));
            allItems.addAll(controllerItems);
            allItems.addAll(podItems);
            allItems.addAll(containerItems);
            for (PolarisResultItemVO i : allItems) {
                DevopsPolarisItemDTO item = new DevopsPolarisItemDTO();
                BeanUtils.copyProperties(template, item);
                BeanUtils.copyProperties(i, item);
                item.setType(i.getId());
                item.setApproved(i.getSuccess());
                if (Boolean.FALSE.equals(i.getSuccess())
                        && PolarisSeverity.ERROR.getValue().equals(i.getSeverity())) {
                    errorCount++;
                }
                items.add(item);
            }
            instanceResult.setHasErrors(errorCount > 0);
        });

        // 处理 devops_polaris_instance_result 数据
        handleInstanceResultList(rawInstanceResultList);
        // 批量插入 devops_polaris_item 纪录
        devopsPolarisItemMapper.batchInsert(items);
    }

    /**
     * 计算分值
     *
     * @param successes 通过项
     * @param warnings  警告项
     * @param errors    错误项
     * @return 分值
     */
    private long countScore(Long successes, Long warnings, Long errors) {
        // 分值： pass项数量/（pass项数量+1/2warning项数量+error项数量）
        // 分母
        double denominator = (successes + warnings / 2.0 + errors);
        // 分子
        long numerator = successes;
        // 四舍五入
        return Math.round(numerator / denominator * 100);
    }

    /**
     * 处理 devops_polaris_instance_result 数据
     *
     * @param instanceResultDTOList 待插入的数据
     */
    private void handleInstanceResultList(List<DevopsPolarisInstanceResultDTO> instanceResultDTOList) {
        if (CollectionUtils.isEmpty(instanceResultDTOList)) {
            return;
        }
        DevopsPolarisResultDetailDTO detailDTO = new DevopsPolarisResultDetailDTO();
        // 这里无法将detail纪录批量插入
        instanceResultDTOList.forEach(i -> {
            detailDTO.setId(null);
            detailDTO.setDetail(i.getDetail());
            MapperUtil.resultJudgedInsertSelective(devopsPolarisResultDetailMapper, detailDTO, "error.insert.polaris.result.detail");
            i.setDetailId(detailDTO.getId());
        });
        devopsPolarisInstanceResultMapper.batchInsert(instanceResultDTOList);
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

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
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
        deleteDevopsPolarisItemByRecordId(recordId);
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

    private void deleteDevopsPolarisItemByRecordId(Long recordId) {
        DevopsPolarisItemDTO deleteCondition = new DevopsPolarisItemDTO();
        deleteCondition.setRecordId(Objects.requireNonNull(recordId));
        devopsPolarisItemMapper.delete(deleteCondition);
    }

    private void deleteDevopsPolarisInstanceResultByRecordId(Long recordId) {
        DevopsPolarisInstanceResultDTO deleteCondition = new DevopsPolarisInstanceResultDTO();
        deleteCondition.setRecordId(Objects.requireNonNull(recordId));
        devopsPolarisInstanceResultMapper.delete(deleteCondition);
    }

    private void deleteDevopsPolarisResultDetailByRecordId(Long recordId) {
        List<Long> detailIds = devopsPolarisResultDetailMapper.queryDetailIdsByRecordId(Objects.requireNonNull(recordId));
        if (detailIds.isEmpty()) {
            return;
        }
        devopsPolarisResultDetailMapper.batchDelete(detailIds);
    }

    /**
     * 插入纪录
     *
     * @param devopsPolarisRecordDTO 纪录
     * @return 插入的纪录
     */
    private DevopsPolarisRecordDTO checkedInsert(DevopsPolarisRecordDTO devopsPolarisRecordDTO) {
        return MapperUtil.resultJudgedInsertSelective(devopsPolarisRecordMapper, devopsPolarisRecordDTO, "error.insert.polaris.record");
    }

    /**
     * 更新纪录
     *
     * @param devopsPolarisRecordDTO 纪录
     */
    private void checkedUpdate(DevopsPolarisRecordDTO devopsPolarisRecordDTO) {
        Objects.requireNonNull(devopsPolarisRecordDTO.getId());
        MapperUtil.resultJudgedUpdateByPrimaryKey(devopsPolarisRecordMapper, devopsPolarisRecordDTO, "error.update.polaris.record");
    }
}
