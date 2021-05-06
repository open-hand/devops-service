package io.choerodon.devops.app.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.AppServiceInstanceForRecordVO;
import io.choerodon.devops.api.vo.DeployRecordCountVO;
import io.choerodon.devops.api.vo.DeployRecordVO;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.MarketUseRecordService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DeployDTO;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.UseRecordType;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDeployRecordMapper;
import io.choerodon.devops.infra.util.CiCdPipelineUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/29.
 */
@Service
public class DevopsDeployRecordServiceImpl implements DevopsDeployRecordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDeployRecordServiceImpl.class);

    private static final String DEPLOY_STATUS = "deployStatus";
    private static final String DEPLOY_TYPE = "deployType";
    private static final String PIPELINE_ID = "pipelineId";
    private static final String RUNNING = "running";
    private static final String ENV_ID = "env";

    @Autowired
    private DevopsDeployRecordMapper devopsDeployRecordMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private MarketUseRecordService marketUseRecordService;


    @Override
    public void saveRecord(Long projectId,
                           DeployType type,
                           Long deployId,
                           DeployModeEnum deployMode,
                           Long deployPayloadId,
                           String deployPayloadName,
                           String deployResult,
                           DeployObjectTypeEnum deployObjectType,
                           String deployObjectName,
                           String deployVersion,
                           String instanceName) {
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
                instanceName);
        try {
            baseCreate(devopsDeployRecordDTO);
        } catch (Exception e) {
            LOGGER.info(">>>>>>>>>>>>>>[deploy record] save deploy record failed.<<<<<<<<<<<<<<<<<< \n, devopsDeployRecordDTO: {}", devopsDeployRecordDTO);
            LOGGER.info("And the ex is", e);
        }
    }

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
                           String instanceName,
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
                instanceName,
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
    public void baseCreate(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        Objects.requireNonNull(devopsDeployRecordDTO.getDeployTime(), "Deploy time can't be null");
        if (devopsDeployRecordMapper.insert(devopsDeployRecordDTO) != 1) {
            throw new CommonException("error.deploy.record.insert");
        }
    }

    /**
     * 这里使用REQUIRES_NEW 是为了在catch中不会被回滚
     *
     * @param recordId
     * @param status
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void updateRecord(Long recordId, String status) {
        DevopsDeployRecordDTO devopsDeployRecordDTO = devopsDeployRecordMapper.selectByPrimaryKey(recordId);
        devopsDeployRecordDTO.setDeployResult(status);
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDeployRecordMapper, devopsDeployRecordDTO, "error.deploy.record.insert");
    }

    /**
     * 不关注更新结果
     * @param devopsDeployRecordDTO
     */
    @Override
    public void updateRecord(DevopsDeployRecordDTO devopsDeployRecordDTO){
        devopsDeployRecordMapper.updateByPrimaryKey(devopsDeployRecordDTO);
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
        Page<DeployRecordVO> deployRecordVOPage = PageHelper
                .doPageAndSort(pageRequest,
                        () -> devopsDeployRecordMapper.listByParams(
                                projectId,
                                deployType,
                                deployMode,
                                deployPayloadName,
                                deployResult,
                                deployObjectName,
                                deployObjectVersion));
        // 添加用户信息
        if (CollectionUtils.isEmpty(deployRecordVOPage.getContent())) {
            return deployRecordVOPage;
        }
        List<Long> uIds = deployRecordVOPage.getContent().stream().map(DeployRecordVO::getCreatedBy).collect(Collectors.toList());

        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(uIds);

        Map<Long, IamUserDTO> userMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));

        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();

        deployRecordVOPage.getContent().forEach(v -> {
            try {
                v.setDeploySourceVO(JsonHelper.unmarshalByJackson(v.getDeploySource(), DeploySourceVO.class));
            } catch (Exception e) {
                LOGGER.info("deploy source is unknown ");
            }
            IamUserDTO iamUserDTO = userMap.get(v.getCreatedBy());
            if (iamUserDTO != null) {
                v.setExecuteUser(iamUserDTO);
            }
            v.setViewId(CiCdPipelineUtils.handleId(v.getId()));


            if (DeployModeEnum.ENV.value().equals(v.getDeployMode())) {
                // 计算部署结果
                v.setDeployResult(v.getCommandStatus());

                DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(v.getDeployPayloadId());
                if (devopsEnvironmentDTO != null) {
                    // 计算集群状态
                    v.setConnect(upgradeClusterList.contains(devopsEnvironmentDTO.getClusterId()));
                    // 添加环境id
                    v.setEnvId(devopsEnvironmentDTO.getId());
                }
                AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(v.getInstanceId());
                if (appServiceInstanceDTO != null) {
                    // 添加应用服务id
                    v.setAppServiceId(appServiceInstanceDTO.getAppServiceId());
                }
            }

        });

        return deployRecordVOPage;
    }

    @Override
    public DeployRecordVO queryEnvDeployRecordByCommandId(Long commandId) {
        Assert.notNull(commandId, ResourceCheckConstant.ERROR_COMMAND_ID_IS_NULL);

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
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(deployRecordVO.getInstanceId());
        if (appServiceInstanceDTO != null) {
            // 添加应用服务id
            deployRecordVO.setAppServiceId(appServiceInstanceDTO.getAppServiceId());
        }
        return deployRecordVO;
    }
}
