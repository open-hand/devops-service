package io.choerodon.devops.app.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.AppServiceInstanceForRecordVO;
import io.choerodon.devops.api.vo.DeployRecordCountVO;
import io.choerodon.devops.api.vo.DeployRecordVO;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.hzero.DevopsHzeroDeployDetailsVO;
import io.choerodon.devops.api.vo.deploy.hzero.HzeroDeployPipelineVO;
import io.choerodon.devops.api.vo.deploy.hzero.HzeroDeployRecordVO;
import io.choerodon.devops.api.vo.deploy.hzero.HzeroDeployVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DeployDTO;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployConfigDTO;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployResultEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDeployRecordMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/29.
 */
@Service
public class DevopsDeployRecordServiceImpl implements DevopsDeployRecordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDeployRecordServiceImpl.class);


    private static final String UPDATE_DEPLOY_RECORD_STATUS_FAILED = "update.deploy.record.status.failed";
    private static final String ERROR_UPDATE_DEPLOY_RECORD_FAILED = "devops.update.deploy.record.failed";

    @Autowired
    private DevopsDeployRecordMapper devopsDeployRecordMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private MarketUseRecordService marketUseRecordService;
    @Autowired
    private WorkFlowServiceOperator workFlowServiceOperator;
    @Autowired
    @Lazy
    private DevopsHzeroDeployDetailsService devopsHzeroDeployDetailsService;
    @Autowired
    @Lazy
    private DevopsHzeroDeployConfigService devopsHzeroDeployConfigService;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;

    @Override
    public Long saveRecord(Long projectId,
                           DeployType type,
                           Long deployId,
                           DeployModeEnum deployMode,
                           Long deployPayloadId,
                           String deployPayloadName,
                           String deployResult,
                           DeployObjectTypeEnum deployObjectType,
                           String deployObjectName,
                           String deployVersion,
                           String appName,
                           String appCode,
                           Long appId,
                           DeploySourceVO deploySource) {
        DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(
                projectId,
                type.getType(),
                deployId,
                deployMode.value(),
                deployPayloadId,
                deployPayloadName,
                deployResult,
                new Date(),
                deployObjectType.value(),
                deployObjectName,
                deployVersion,
                appName,
                appCode,
                appId,
                JsonHelper.marshalByJackson(deploySource));
        try {
            baseCreate(devopsDeployRecordDTO);
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(AppSourceType.MARKET.getValue(), deploySource.getType())) {
                marketUseRecordService.saveMarketUseRecord(UseRecordType.DEPLOY.getValue(), projectId, deploySource, DetailsHelper.getUserDetails().getUserId());
            }
        } catch (Exception e) {
            LOGGER.info(">>>>>>>>>>>>>>[deploy record] save deploy record failed.<<<<<<<<<<<<<<<<<< \n, devopsDeployRecordDTO: {}, errorMsg: {}", devopsDeployRecordDTO, e.getMessage());
        }
        return devopsDeployRecordDTO.getId();
    }

    @Override
    @Transactional
    public Long saveDeployRecord(Long projectId,
                                 DeployType type,
                                 Long deployId,
                                 DeployModeEnum deployMode,
                                 Long deployPayloadId,
                                 String deployPayloadName,
                                 String deployResult,
                                 DeployObjectTypeEnum deployObjectType,
                                 String deployObjectName,
                                 String deployVersion,
                                 String instanceName,
                                 DeploySourceVO deploySource,
                                 @Nullable String businessKey) {
        DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(
                projectId,
                type.getType(),
                deployId,
                deployMode.value(),
                deployPayloadId,
                deployPayloadName,
                deployResult,
                new Date(),
                deployObjectType.value(),
                deployObjectName,
                deployVersion,
                instanceName,
                JsonHelper.marshalByJackson(deploySource),
                businessKey);
        try {
            baseCreate(devopsDeployRecordDTO);
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(AppSourceType.MARKET.getValue(), deploySource.getType())) {
                marketUseRecordService.saveMarketUseRecord(UseRecordType.DEPLOY.getValue(), projectId, deploySource, DetailsHelper.getUserDetails().getUserId());
            }
        } catch (Exception e) {
            LOGGER.info(">>>>>>>>>>>>>>[deploy record] save deploy record failed.<<<<<<<<<<<<<<<<<< \n, devopsDeployRecordDTO: {}, errorMsg: {}", devopsDeployRecordDTO, e.getMessage());
        }
        return devopsDeployRecordDTO.getId();
    }

    @Override
    public Long saveFailRecord(Long projectId, DeployType type, Long deployId, DeployModeEnum deployMode, Long deployPayloadId, String deployPayloadName, String deployResult, DeployObjectTypeEnum deployObjectType, String deployObjectName, String deployVersion, String instanceName, DeploySourceVO deploySourceVO, Long userId, String errorMessage) {
        DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(
                projectId,
                type.getType(),
                deployId,
                deployMode.value(),
                deployPayloadId,
                deployPayloadName,
                deployResult,
                new Date(),
                deployObjectType.value(),
                deployObjectName,
                deployVersion,
                instanceName,
                null,
                null,
                JsonHelper.marshalByJackson(deploySourceVO));
        devopsDeployRecordDTO.setErrorMessage(errorMessage);
        try {
            baseCreate(devopsDeployRecordDTO);
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(AppSourceType.MARKET.getValue(), deploySourceVO.getType())) {
                marketUseRecordService.saveMarketUseRecord(UseRecordType.DEPLOY.getValue(), projectId, deploySourceVO, userId);
            }
        } catch (Exception e) {
            LOGGER.info(">>>>>>>>>>>>>>[deploy record] save deploy record failed.<<<<<<<<<<<<<<<<<< \n, devopsDeployRecordDTO: {}, errorMsg: {}", devopsDeployRecordDTO, e.getMessage());
        }
        return devopsDeployRecordDTO.getId();
    }

    @Override
    public void baseCreate(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        Objects.requireNonNull(devopsDeployRecordDTO.getDeployTime(), "Deploy time can't be null");
        if (devopsDeployRecordMapper.insert(devopsDeployRecordDTO) != 1) {
            throw new CommonException("devops.deploy.record.insert");
        }
    }

    @Override
    public List<DevopsDeployRecordDTO> baseList(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        return devopsDeployRecordMapper.select(devopsDeployRecordDTO);
    }


    @Override
    public void baseDelete(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        devopsDeployRecordMapper.delete(devopsDeployRecordDTO);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void deleteRecordByEnv(Long envId) {
        DevopsDeployRecordDTO deleteCondition = new DevopsDeployRecordDTO();
        // 删除手动部署的纪录
        deleteCondition.setDeployPayloadId(envId);
        deleteCondition.setDeployMode(DeployModeEnum.ENV.value());
        devopsDeployRecordMapper.delete(deleteCondition);
    }

    @Override
    @Transactional
    public void deleteRelatedRecordOfInstance(Long instanceId) {
        devopsDeployRecordMapper.deleteRelatedRecordOfInstance(instanceId);
    }

    @Override
    public DeployRecordCountVO countByDate(Long projectId, Date startTime, Date endTime) {

        List<DeployDTO> deployDTOS = appServiceInstanceService.baseListDeployFrequency(projectId, null, null, startTime, endTime);


        DeployRecordCountVO deployRecordCountVO = new DeployRecordCountVO();
        deployRecordCountVO.setId(projectId);

        // 按日期分组
        Map<String, List<DeployDTO>> map = deployDTOS.stream()
                .collect(Collectors.groupingBy(t -> new java.sql.Date(t.getCreationDate().getTime()).toString()));

        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate startDate = startTime.toInstant().atZone(zoneId).toLocalDate();
        LocalDate endDate = endTime.toInstant().atZone(zoneId).toLocalDate();

        while (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
            String date = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            long countNum = 0;
            // 计算每天部署数量
            List<DeployDTO> deployDTOList = map.get(date);
            if (!CollectionUtils.isEmpty(deployDTOList)) {
                countNum = deployDTOList.size();
            }

            deployRecordCountVO.getData().add(countNum);
            startDate = startDate.plusDays(1);
        }
        return deployRecordCountVO;
    }

    @Override
    public List<AppServiceInstanceForRecordVO> queryByBatchDeployRecordId(Long recordId) {
        // 这里不校验recordId是不是批量部署类型的部署纪录的id
        return devopsDeployRecordMapper.queryByBatchDeployRecordId(recordId);
    }


    @Override
    public Page<DeployRecordVO> paging(Long projectId, PageRequest pageRequest, String deployType, String deployMode, String deployPayloadName, String deployResult, String deployObjectName, String deployObjectVersion) {
        Page<DeployRecordVO> deployRecordVOPage = pageRecord(projectId, pageRequest, deployType, deployMode, deployPayloadName, deployResult, deployObjectName, deployObjectVersion);

        if (CollectionUtils.isEmpty(deployRecordVOPage.getContent())) {
            return deployRecordVOPage;
        }
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();

        // 添加用户信息
        UserDTOFillUtil.fillUserInfo(deployRecordVOPage.getContent(), "createdBy", "executeUser");

        deployRecordVOPage.getContent().forEach(v -> {
            try {
                v.setDeploySourceVO(JsonHelper.unmarshalByJackson(v.getDeploySource(), DeploySourceVO.class));
            } catch (Exception e) {
                LOGGER.info("deploy source is unknown ");
            }
            v.setViewId(CiCdPipelineUtils.handleId(v.getId()));

            if (DeployModeEnum.ENV.value().equals(v.getDeployMode()) && !DeployType.HZERO.getType().equals(v.getDeployType())) {
                // 计算部署结果
                v.setDeployResult(v.getCommandStatus());

                DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(v.getDeployPayloadId());
                if (devopsEnvironmentDTO != null) {
                    // 计算集群状态
                    v.setConnect(upgradeClusterList.contains(devopsEnvironmentDTO.getClusterId()));
                    // 添加环境id
                    v.setEnvId(devopsEnvironmentDTO.getId());
                }
                // todo 结构调整后，不确定是否还需要这个字段，先注释
//                AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(v.getAppId());
//                if (appServiceInstanceDTO != null) {
//                    // 添加应用服务id
//                    v.setAppServiceId(appServiceInstanceDTO.getAppServiceId());
//                }
            }

        });

        return deployRecordVOPage;
    }

    protected Page<DeployRecordVO> pageRecord(Long projectId, PageRequest pageRequest, String deployType, String deployMode, String deployPayloadName, String deployResult, String deployObjectName, String deployObjectVersion) {
        Page<DeployRecordVO> deployRecordVOPage = PageHelper
                .doPageAndSort(pageRequest,
                        () -> devopsDeployRecordMapper.listByParams(
                                projectId,
                                deployType,
                                deployMode,
                                deployPayloadName,
                                deployResult,
                                deployObjectName,
                                deployObjectVersion,
                                null));
        return deployRecordVOPage;
    }

    @Override
    public DeployRecordVO queryEnvDeployRecordByCommandId(Long commandId) {
        Assert.notNull(commandId, ResourceCheckConstant.DEVOPS_COMMAND_ID_IS_NULL);

        DeployRecordVO deployRecordVO = devopsDeployRecordMapper.queryEnvDeployRecordByCommandId(commandId);
        if (deployRecordVO == null) {
            return null;
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(deployRecordVO.getDeployPayloadId());
        if (devopsEnvironmentDTO != null) {
            // 计算集群状态
            // 添加环境id
            deployRecordVO.setEnvId(devopsEnvironmentDTO.getId());
        }
        return deployRecordVO;
    }

    @Override
    public DeployRecordVO queryHostDeployRecordByCommandId(Long commandId) {
        Assert.notNull(commandId, ResourceCheckConstant.DEVOPS_COMMAND_ID_IS_NULL);

        return devopsDeployRecordMapper.queryHostDeployRecordByCommandId(commandId);
    }

    @Override
    @Transactional
    public void updateResultById(Long deployRecordId, DeployResultEnum status) {
        DevopsDeployRecordDTO devopsDeployRecordDTO = devopsDeployRecordMapper.selectByPrimaryKey(deployRecordId);
        devopsDeployRecordDTO.setDeployResult(status.value());
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDeployRecordMapper, devopsDeployRecordDTO, UPDATE_DEPLOY_RECORD_STATUS_FAILED);
    }

    @Override
    public DevopsDeployRecordDTO baseQueryById(Long deployRecordId) {
        return devopsDeployRecordMapper.selectByPrimaryKey(deployRecordId);
    }

    @Override
    @Transactional
    public void stop(Long projectId, Long recordId) {
        DevopsDeployRecordDTO devopsDeployRecordDTO = devopsDeployRecordMapper.selectByPrimaryKey(recordId);
        workFlowServiceOperator.stopInstance(projectId, devopsDeployRecordDTO.getBusinessKey());
        updateResultById(recordId, DeployResultEnum.CANCELED);
        List<DevopsHzeroDeployDetailsDTO> devopsHzeroDeployDetailsDTOS = devopsHzeroDeployDetailsService.listNotSuccessRecordId(recordId);

        devopsHzeroDeployDetailsDTOS.forEach(devopsHzeroDeployDetailsDTO -> devopsHzeroDeployDetailsService.updateStatusById(devopsHzeroDeployDetailsDTO.getId(), HzeroDeployDetailsStatusEnum.CANCELED));
    }

    @Override
    @Transactional
    public void retry(Long projectId, Long recordId, HzeroDeployVO hzeroDeployVO) {
        // 1. 更新记录状态
        String businessKey = GenerateUUID.generateUUID();
        DevopsDeployRecordDTO devopsDeployRecordDTO = baseQueryById(recordId);
        devopsDeployRecordDTO.setDeployResult(DeployResultEnum.OPERATING.value());
        devopsDeployRecordDTO.setBusinessKey(businessKey);
        baseUpdate(devopsDeployRecordDTO);
        // 2. 更新部署明细
        // 2.1 查询部署未成功的明细
        List<DevopsHzeroDeployDetailsDTO> devopsHzeroDeployDetailsDTOS = devopsHzeroDeployDetailsService.listNotSuccessRecordId(recordId);
        // 2.2 更新记录
        if (CollectionUtils.isEmpty(devopsHzeroDeployDetailsDTOS)) {
            return;
        }
        List<DevopsHzeroDeployDetailsVO> deployDetailsVOList = hzeroDeployVO.getDeployDetailsVOList();

        if (CollectionUtils.isEmpty(deployDetailsVOList)) {
            devopsHzeroDeployDetailsDTOS.forEach(devopsHzeroDeployDetailsDTO -> {
                // 2.4 对于未成功的记录更新状态为未执行
                devopsHzeroDeployDetailsService.updateStatusById(devopsHzeroDeployDetailsDTO.getId(), HzeroDeployDetailsStatusEnum.CREATED);
            });
        } else {
            Map<Long, DevopsHzeroDeployDetailsVO> devopsHzeroDeployDetailsVOMap = deployDetailsVOList.stream().collect(Collectors.toMap(DevopsHzeroDeployDetailsVO::getId, Function.identity()));
            devopsHzeroDeployDetailsDTOS.forEach(devopsHzeroDeployDetailsDTO -> {
                // 2.3 更新部署配置
                DevopsHzeroDeployDetailsVO devopsHzeroDeployDetailsVO = devopsHzeroDeployDetailsVOMap.get(devopsHzeroDeployDetailsDTO.getId());
                if (devopsHzeroDeployDetailsVO != null) {
                    devopsHzeroDeployConfigService.updateById(devopsHzeroDeployDetailsDTO.getValueId(),
                            devopsHzeroDeployDetailsVO.getValue(),
                            devopsHzeroDeployDetailsVO.getDevopsServiceReqVO(),
                            devopsHzeroDeployDetailsVO.getDevopsIngressVO());
                }
                // 2.4 对于未成功的记录更新状态为未执行
                devopsHzeroDeployDetailsService.updateStatusById(devopsHzeroDeployDetailsDTO.getId(), HzeroDeployDetailsStatusEnum.CREATED);

            });
        }

        // 启动流程实例
        HzeroDeployPipelineVO hzeroDeployPipelineVO = new HzeroDeployPipelineVO(businessKey, devopsHzeroDeployDetailsDTOS);
        workFlowServiceOperator.createHzeroPipeline(projectId, hzeroDeployPipelineVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDeployRecordMapper, devopsDeployRecordDTO, ERROR_UPDATE_DEPLOY_RECORD_FAILED);
    }

    @Override
    public HzeroDeployRecordVO queryHzeroDetailsById(Long projectId, Long recordId) {
        HzeroDeployRecordVO hzeroDeployRecordVO = new HzeroDeployRecordVO();

        DevopsDeployRecordDTO devopsDeployRecordDTO = devopsDeployRecordMapper.selectByPrimaryKey(recordId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsDeployRecordDTO.getDeployPayloadId());

        List<DevopsHzeroDeployDetailsDTO> devopsHzeroDeployDetailsDTOS = devopsHzeroDeployDetailsService.listByDeployRecordId(recordId);
        List<DevopsHzeroDeployDetailsVO> devopsHzeroDeployDetailsVOS = ConvertUtils.convertList(devopsHzeroDeployDetailsDTOS, DevopsHzeroDeployDetailsVO.class);


        devopsHzeroDeployDetailsVOS.forEach(devopsHzeroDeployDetailsVO -> {
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(projectId, devopsHzeroDeployDetailsVO.getMktDeployObjectId());
            // 添加市场服务名和版本
            devopsHzeroDeployDetailsVO.setMktServiceName(marketServiceDeployObjectVO != null ? marketServiceDeployObjectVO.getMarketServiceName() : "none");
            devopsHzeroDeployDetailsVO.setMktServiceVersion(marketServiceDeployObjectVO != null ? marketServiceDeployObjectVO.getMarketServiceVersion(): "none");
            // 添加values
            DevopsHzeroDeployConfigDTO devopsHzeroDeployConfigDTO = devopsHzeroDeployConfigService.baseQueryById(devopsHzeroDeployDetailsVO.getValueId());
            if (devopsHzeroDeployConfigDTO != null) {
                devopsHzeroDeployDetailsVO.setValue(devopsHzeroDeployConfigDTO.getValue());
            }

        });
        //添加是否实例存在字段的值
        batchSetAppStatus(devopsHzeroDeployDetailsVOS, devopsHzeroDeployDetailsDTOS.get(0).getEnvId());
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(projectId, devopsHzeroDeployDetailsDTOS.get(0).getMktDeployObjectId());

        hzeroDeployRecordVO.setEnvironmentDTO(devopsEnvironmentDTO);
        hzeroDeployRecordVO.setMktApplication(marketServiceDeployObjectVO != null ? marketServiceDeployObjectVO.getMarketAppName() : "none");
        hzeroDeployRecordVO.setMktAppVersion(marketServiceDeployObjectVO != null ? marketServiceDeployObjectVO.getMarketAppVersion() : "none");
        hzeroDeployRecordVO.setDeployDetailsVOList(devopsHzeroDeployDetailsVOS);
        hzeroDeployRecordVO.setType(devopsHzeroDeployDetailsVOS.get(0).getHzeroType());

        return hzeroDeployRecordVO;
    }

    private void batchSetAppStatus(List<DevopsHzeroDeployDetailsVO> devopsHzeroDeployDetailsVOS, Long envId) {
        devopsHzeroDeployDetailsVOS.forEach(devopsHzeroDeployDetailsVO -> {
            if (devopsHzeroDeployDetailsVO.getAppId() != null) {
                DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(envId, devopsHzeroDeployDetailsVO.getAppCode());
                devopsHzeroDeployDetailsVO.setAppStatus(!ObjectUtils.isEmpty(devopsDeployAppCenterEnvDTO) ? AppStatus.EXIST.getStatus() : AppStatus.DELETED.getStatus());
            } else {
                devopsHzeroDeployDetailsVO.setAppStatus(AppStatus.NOT_EXIST.getStatus());
            }
        });
    }
}
