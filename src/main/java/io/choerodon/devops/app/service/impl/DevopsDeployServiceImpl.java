//package io.choerodon.devops.app.service.impl;
//
//import static io.choerodon.devops.infra.constant.GitOpsConstants.DATE_PATTERN;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
//import io.choerodon.devops.api.vo.deploy.hzero.HzeroDeployPipelineVO;
//import io.choerodon.devops.api.vo.deploy.hzero.HzeroDeployVO;
//import io.choerodon.devops.app.service.*;
//import io.choerodon.devops.infra.constant.MiscConstants;
//import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
//import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
//import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployConfigDTO;
//import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;
//import io.choerodon.devops.infra.dto.iam.ProjectDTO;
//import io.choerodon.devops.infra.dto.market.MarketApplicationDTO;
//import io.choerodon.devops.infra.enums.AppSourceType;
//import io.choerodon.devops.infra.enums.CommandStatus;
//import io.choerodon.devops.infra.enums.DeployType;
//import io.choerodon.devops.infra.enums.HzeroDeployDetailsStatusEnum;
//import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
//import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
//import io.choerodon.devops.infra.enums.deploy.DeployResultEnum;
//import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
//import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
//import io.choerodon.devops.infra.util.GenerateUUID;
//import io.choerodon.devops.infra.util.JsonHelper;
//
///**
// * 〈功能简述〉
// * 〈〉
// *
// * @author wanghao
// * @since 2020/10/19 16:04
// */
//@Service
//public class DevopsDeployServiceImpl implements DevopsDeployService {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDeployServiceImpl.class);
//    public static final int THIRTY_MINUTE_MILLISECONDS = 3 * 60 * 1000;
//
//    @Autowired
//    private DevopsDeployRecordService devopsDeployRecordService;
//    @Autowired
//    private MarketServiceClientOperator marketServiceClientOperator;
//    @Autowired
//    private DevopsHzeroDeployConfigService devopsHzeroDeployConfigService;
//    @Autowired
//    private DevopsHzeroDeployDetailsService devopsHzeroDeployDetailsService;
//    @Autowired
//    private DevopsEnvironmentService devopsEnvironmentService;
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//    @Autowired
//    private DevopsEnvPodService devopsEnvPodService;
//    @Autowired
//    private BaseServiceClientOperator baseServiceClientOperator;
//
//    @Override
//    @Transactional
//    public Long deployHzeroApplication(Long projectId, HzeroDeployVO hzeroDeployVO) {
//        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
//        if (LOGGER.isInfoEnabled()) {
//            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>deployHzeroApplication project info is {}<<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(projectDTO));
//        }
//        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(hzeroDeployVO.getEnvId());
//        MarketApplicationDTO marketApplicationDTO = marketServiceClientOperator.queryApplication(hzeroDeployVO.getMktAppId(), projectDTO.getOrganizationId());
//        // 保存部署记录
//        DeploySourceVO deploySourceVO = new DeploySourceVO();
//        deploySourceVO.setType(AppSourceType.HZERO.getValue());
//        String businessKey = GenerateUUID.generateUUID();
//        Long deployRecordId = devopsDeployRecordService.saveDeployRecord(projectId,
//                DeployType.HZERO,
//                null,
//                DeployModeEnum.ENV,
//                devopsEnvironmentDTO.getId(),
//                devopsEnvironmentDTO.getName(),
//                CommandStatus.OPERATING.getStatus(),
//                DeployObjectTypeEnum.HZERO,
//                marketApplicationDTO.getName(),
//                hzeroDeployVO.getMktAppVersion(),
//                null,
//                deploySourceVO,
//                businessKey);
//        List<DevopsHzeroDeployDetailsDTO> devopsHzeroDeployDetailsList = new ArrayList<>();
//        String type = marketServiceClientOperator.queryHzeroAppType(hzeroDeployVO.getMktAppId());
//        hzeroDeployVO.getDeployDetailsVOList().forEach(instanceVO -> {
//            // 保存部署配置
//            DevopsHzeroDeployConfigDTO devopsHzeroDeployConfigDTO = devopsHzeroDeployConfigService.baseSave(new DevopsHzeroDeployConfigDTO(instanceVO.getValue(),
//                    instanceVO.getDevopsServiceReqVO() == null ? null : JsonHelper.marshalByJackson(instanceVO.getDevopsServiceReqVO()),
//                    instanceVO.getDevopsIngressVO() == null ? null : JsonHelper.marshalByJackson(instanceVO.getDevopsIngressVO())));
//            // 保存部署记录详情
//            DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO = devopsHzeroDeployDetailsService.baseSave(new DevopsHzeroDeployDetailsDTO(deployRecordId,
//                    devopsEnvironmentDTO.getId(),
//                    instanceVO.getMktServiceId(),
//                    instanceVO.getMktDeployObjectId(),
//                    devopsHzeroDeployConfigDTO.getId(),
//                    HzeroDeployDetailsStatusEnum.CREATED.value(),
//                    instanceVO.getAppCode(),
//                    instanceVO.getAppName(),
//                    instanceVO.getSequence(),
//                    type));
//            devopsHzeroDeployDetailsList.add(devopsHzeroDeployDetailsDTO);
//        });
//        // 构建工作流部署对象
//        HzeroDeployPipelineVO hzeroDeployPipelineVO = new HzeroDeployPipelineVO(businessKey, devopsHzeroDeployDetailsList);
//        // 启动流程实例
//        workFlowServiceOperator.createHzeroPipeline(projectId, hzeroDeployPipelineVO);
//
//        return deployRecordId;
//    }
//
//    @Override
//    public void updateStatus() {
//        // 添加redis锁，防止多个pod并发更新状态
//        if (!Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(MiscConstants.HZERO_DEPLOY_STATUS_SYNC_REDIS_KEY, "lock", 3, TimeUnit.MINUTES))) {
//            return;
//        }
//        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>[Hzero Deploy] Start check hzero deploying status<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
//
//        // 获取三分钟以前的时间
//        Date threeMinutesBefore = new Date(System.currentTimeMillis() - THIRTY_MINUTE_MILLISECONDS);
//        String date = simpleDateFormat.format(threeMinutesBefore);
//
//        List<DevopsHzeroDeployDetailsDTO> devopsHzeroDeployDetailsDTOS = devopsHzeroDeployDetailsService.listDeployingByDate(date);
//
//        devopsHzeroDeployDetailsDTOS.forEach(devopsHzeroDeployDetailsDTO -> {
//            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>[Hzero Deploy] Deploying timeout details id is {}<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsHzeroDeployDetailsDTO.getId());
//
//            // 1. 查询Pod是否全部启动成功
//            if (Boolean.TRUE.equals(devopsEnvPodService.checkInstancePodStatusAllReadyWithCommandId(devopsHzeroDeployDetailsDTO.getEnvId(),
//                    devopsHzeroDeployDetailsDTO.getAppId(),
//                    devopsHzeroDeployDetailsDTO.getCommandId()))) {
//                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>[Hzero Deploy] Deploying success details id is {}<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsHzeroDeployDetailsDTO.getId());
//
//                devopsHzeroDeployDetailsService.updateStatusById(devopsHzeroDeployDetailsDTO.getId(), HzeroDeployDetailsStatusEnum.SUCCESS);
//
//                DevopsDeployRecordDTO devopsDeployRecordDTO = devopsDeployRecordService.baseQueryById(devopsHzeroDeployDetailsDTO.getDeployRecordId());
//                if (Boolean.TRUE.equals(devopsHzeroDeployDetailsService.completed(devopsHzeroDeployDetailsDTO.getDeployRecordId()))) {
//                    devopsDeployRecordService.updateResultById(devopsHzeroDeployDetailsDTO.getDeployRecordId(), DeployResultEnum.SUCCESS);
//                } else {
//                    workFlowServiceOperator.approveUserTask(devopsDeployRecordDTO.getProjectId(),
//                            devopsDeployRecordDTO.getBusinessKey(),
//                            MiscConstants.WORKFLOW_ADMIN_NAME,
//                            MiscConstants.WORKFLOW_ADMIN_ID,
//                            MiscConstants.WORKFLOW_ADMIN_ORG_ID);
//                }
//            } else {
//                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>[Hzero Deploy] Deploying failed details id is {}<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsHzeroDeployDetailsDTO.getId());
//
//                devopsHzeroDeployDetailsService.updateStatusToFailed(devopsHzeroDeployDetailsDTO);
//            }
//        });
//    }
//
//
//
//}
