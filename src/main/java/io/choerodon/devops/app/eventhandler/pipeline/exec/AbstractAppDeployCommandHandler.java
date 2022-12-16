package io.choerodon.devops.app.eventhandler.pipeline.exec;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.pipeline.AppDeployConfigVO;
import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.util.PipelineAppDeployUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 16:03
 */
@Service
public abstract class AbstractAppDeployCommandHandler extends AbstractCiCommandHandler {
    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    @Autowired
    DevopsCiJobRecordService devopsCiJobRecordService;
    @Autowired
    protected PipelineAppDeployUtil pipelineAppDeployUtil;

    @Override
    public CiCommandTypeEnum getType() {
        return CiCommandTypeEnum.CHART_DEPLOY;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(AppServiceDTO appServiceDTO, Long gitlabPipelineId, Long gitlabJobId, Long configId, StringBuilder log, Map<String, Object> content) {
        log.append("Start pipeline auto deploy task.").append(System.lineSeparator());
        AppDeployConfigVO appDeployConfigVO = queryConfigById(configId);
        if (appDeployConfigVO == null) {
            throw new CommonException("devops.chart.deploy.config.not.found");
        }
        Long projectId = appServiceDTO.getProjectId();
        Long appServiceId = appServiceDTO.getId();
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        Long userId = userDetails.getUserId();
        Long envId = appDeployConfigVO.getEnvId();
        Boolean skipCheckPermission = appDeployConfigVO.getSkipCheckPermission();
        String appCode = appDeployConfigVO.getAppCode();
        String appName = appDeployConfigVO.getAppName();

        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryByAppServiceIdAndGitlabPipelineId(appServiceId, gitlabPipelineId);

        DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByAppServiceIdAndGitlabJobId(appServiceId, gitlabJobId);
        // 1. 校验环境是否开启一键关闭自动部署
        if (!pipelineAppDeployUtil.checkAutoMaticDeploy(log, envId)) return;
        // 2. 校验用户权限
        if (!pipelineAppDeployUtil.checkUserPermission(log, userId, envId, skipCheckPermission)) return;
        // 获取部署版本信息
        deployApp(appServiceDTO, log, appDeployConfigVO, projectId, appServiceId, envId, appCode, appName, devopsCiPipelineRecordDTO, devopsCiJobRecordDTO);
    }

    protected abstract void deployApp(AppServiceDTO appServiceDTO,
                                      StringBuilder log,
                                      AppDeployConfigVO appDeployConfigVO,
                                      Long projectId,
                                      Long appServiceId,
                                      Long envId,
                                      String appCode,
                                      String appName,
                                      DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO,
                                      DevopsCiJobRecordDTO devopsCiJobRecordDTO);


    protected abstract AppDeployConfigVO queryConfigById(Long configId);

}
