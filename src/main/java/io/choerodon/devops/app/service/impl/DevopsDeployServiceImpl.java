package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.deploy.DeployConfigVO;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.hzero.HzeroDeployPipelineVO;
import io.choerodon.devops.api.vo.deploy.hzero.HzeroDeployVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployConfigDTO;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;
import io.choerodon.devops.infra.dto.market.MarketApplicationDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.HzeroDeployDetailsStatusEnum;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/19 16:04
 */
@Service
public class DevopsDeployServiceImpl implements DevopsDeployService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDeployServiceImpl.class);
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private JarAndImageDeployService jarAndImageDeployService;
    @Autowired
    private DevopsHzeroDeployConfigService devopsHzeroDeployConfigService;
    @Autowired
    private DevopsHzeroDeployDetailsService devopsHzeroDeployDetailsService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private WorkFlowServiceOperator workFlowServiceOperator;

    @Override
    public void hostDeploy(Long projectId, DeployConfigVO deployConfigVO) {
        if (DeployModeEnum.ENV.value().equals(deployConfigVO.getDeployType())) {
            AppServiceDeployVO appServiceDeployVO = deployConfigVO.getAppServiceDeployVO();
            appServiceDeployVO.setType("create");
            appServiceInstanceService.createOrUpdate(projectId, appServiceDeployVO, false);
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            jarAndImageDeployService.jarAndImageDeploy(projectId, deployConfigVO, authentication);
        }
    }

    @Override
    @Transactional
    public Long deployHzeroApplication(Long projectId, HzeroDeployVO hzeroDeployVO) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(hzeroDeployVO.getEnvId());
        MarketApplicationDTO marketApplicationDTO = marketServiceClientOperator.queryApplication(hzeroDeployVO.getMktAppId());
        // 保存部署记录
        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(AppSourceType.HZERO.getValue());
        String businessKey = GenerateUUID.generateUUID();
        Long deployRecordId = devopsDeployRecordService.saveDeployRecord(projectId,
                DeployType.HZERO,
                null,
                DeployModeEnum.ENV,
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getName(),
                CommandStatus.OPERATING.getStatus(),
                DeployObjectTypeEnum.HZERO,
                marketApplicationDTO.getName(),
                hzeroDeployVO.getMktAppVersion(),
                null,
                deploySourceVO,
                businessKey);
        List<DevopsHzeroDeployDetailsDTO> devopsHzeroDeployDetailsList = new ArrayList<>();
        hzeroDeployVO.getDeployDetailsVOList().forEach(instanceVO -> {
            // 保存部署配置
            DevopsHzeroDeployConfigDTO devopsHzeroDeployConfigDTO = devopsHzeroDeployConfigService.baseSave(new DevopsHzeroDeployConfigDTO(instanceVO.getValue(),
                    instanceVO.getDevopsServiceReqVO() == null ? null : JsonHelper.marshalByJackson(instanceVO.getDevopsServiceReqVO()),
                    instanceVO.getDevopsIngressVO() == null ? null : JsonHelper.marshalByJackson(instanceVO.getDevopsIngressVO())));
            // 保存部署记录详情
            DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO = devopsHzeroDeployDetailsService.baseSave(new DevopsHzeroDeployDetailsDTO(deployRecordId,
                    devopsEnvironmentDTO.getId(),
                    instanceVO.getMktServiceId(),
                    instanceVO.getMktDeployObjectId(),
                    devopsHzeroDeployConfigDTO.getId(),
                    HzeroDeployDetailsStatusEnum.CREATED.value(),
                    instanceVO.getInstanceCode(),
                    instanceVO.getSequence()));
            devopsHzeroDeployDetailsList.add(devopsHzeroDeployDetailsDTO);
        });

        // 构建工作流部署对象
        HzeroDeployPipelineVO hzeroDeployPipelineVO = new HzeroDeployPipelineVO(businessKey, devopsHzeroDeployDetailsList);
        // 启动流程实例
        workFlowServiceOperator.createHzeroPipeline(projectId, hzeroDeployPipelineVO);

        return deployRecordId;
    }

}
