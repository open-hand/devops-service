package io.choerodon.devops.app.service.impl;

import java.util.*;

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
import io.choerodon.devops.api.vo.ClusterSummaryInfoVO;
import io.choerodon.devops.api.vo.DevopsEnvironmentRepVO;
import io.choerodon.devops.api.vo.polaris.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.PolarisScanningStatus;
import io.choerodon.devops.infra.enums.PolarisScopeType;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.*;
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

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public DevopsPolarisRecordDTO scanEnv(Long envId) {
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
        return devopsPolarisRecordDTO;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public DevopsPolarisRecordDTO scanCluster(Long clusterId) {
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
        return devopsPolarisRecordDTO;
    }

    private DevopsPolarisRecordDTO createOrUpdateRecord(String scope, Long scopeId) {
        DevopsPolarisRecordDTO devopsPolarisRecordDTO = new DevopsPolarisRecordDTO();
        devopsPolarisRecordDTO.setScope(scope);
        devopsPolarisRecordDTO.setScopeId(scopeId);

        // 查看数据库是否有现有纪录
        DevopsPolarisRecordDTO existedRecord = devopsPolarisRecordMapper.selectOne(devopsPolarisRecordDTO);

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

        PolarisScanResultVO polarisScanResultVO = message.getPolarisResult();
        recordDTO.setSuccesses(polarisScanResultVO.getSummary().getSuccesses());
        recordDTO.setWarnings(polarisScanResultVO.getSummary().getWarnings());
        recordDTO.setErrors(polarisScanResultVO.getSummary().getErrors());
        recordDTO.setStatus(PolarisScanningStatus.FINISHED.getStatus());
        checkedUpdate(recordDTO);

        handleAuditData(recordId, polarisScanResultVO.getAuditData());
    }

    /**
     * 处理详细的数据
     *
     * @param recordId               扫描纪录id
     * @param polarisScanAuditDataVO 详细的扫描数据
     */
    private void handleAuditData(Long recordId, PolarisScanAuditDataVO polarisScanAuditDataVO) {
        handleClusterInfo(polarisScanAuditDataVO.getClusterInfo());
        List<PolarisControllerResultVO> results = polarisScanAuditDataVO.getResults();
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
            Long confirmedEnvId = null;
            if (finalEnvId == null) {
                DevopsEnvironmentRepVO controllerEnv = devopsEnvironmentService.queryByCode(finalClusterId, controllerResult.getNamespace());
                if (controllerEnv != null) {
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
            allItems.forEach(i -> {
                DevopsPolarisItemDTO item = new DevopsPolarisItemDTO();
                BeanUtils.copyProperties(template, item);
                BeanUtils.copyProperties(i, item);
                item.setType(i.getId());
                item.setApproved(i.getSuccess());
                items.add(item);
            });
        });

        // 处理 devops_polaris_instance_result 数据
        handleInstanceResultList(rawInstanceResultList);
        // 批量插入 devops_polaris_item 纪录
        devopsPolarisItemMapper.batchInsert(items);
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

    private void handleClusterInfo(ClusterSummaryInfoVO clusterSummaryInfoVO) {
        // TODO
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
        deleteDevopsPolarisInstanceResultByRecordId(recordId);
        deleteDevopsPolarisResultDetailByRecordId(recordId);
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
