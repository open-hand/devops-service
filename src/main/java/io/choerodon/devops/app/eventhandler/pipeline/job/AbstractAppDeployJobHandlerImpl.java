package io.choerodon.devops.app.eventhandler.pipeline.job;

import static io.choerodon.devops.infra.constant.ExceptionConstants.AppDeploy.DEVOPS_APP_DEPLOY_CONFIG_EMPTY;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.AppDeployConfigVO;
import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.CiJob;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 14:47
 */
public abstract class AbstractAppDeployJobHandlerImpl extends AbstractJobHandler {

    @Autowired
    protected DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    @Lazy
    private DevopsCiPipelineService devopsCiPipelineService;

    @Override
    protected void checkConfigInfo(Long projectId, DevopsCiJobVO devopsCiJobVO) {
        AppDeployConfigVO appDeployConfigVO = getDeployConfig(devopsCiJobVO);
        if (appDeployConfigVO == null) {
            throw new CommonException(DEVOPS_APP_DEPLOY_CONFIG_EMPTY, devopsCiJobVO.getName());
        }
        if (appDeployConfigVO.getEnvId() == null) {
            throw new CommonException(DEVOPS_APP_DEPLOY_CONFIG_EMPTY, devopsCiJobVO.getName());
        }
        if (StringUtils.isEmpty(appDeployConfigVO.getDeployType())) {
            throw new CommonException(ExceptionConstants.AppCode.DEVOPS_APP_DEPLOY_TYPE_IS_EMPTY);
        }
        if (DeployTypeEnum.CREATE.value().equals(appDeployConfigVO.getDeployType())) {
            if (StringUtils.isEmpty(appDeployConfigVO.getAppName())) {
                throw new CommonException(ExceptionConstants.AppCode.DEVOPS_APP_NAME_IS_EMPTY);
            }
            if (StringUtils.isEmpty(appDeployConfigVO.getAppCode())) {
                throw new CommonException(ExceptionConstants.AppCode.DEVOPS_APP_CODE_IS_EMPTY);
            }
            appDeployConfigVO.setAppId(null);
        } else {
            if (appDeployConfigVO.getAppId() == null) {
                throw new CommonException(ExceptionConstants.AppCode.DEVOPS_APP_ID_IS_EMPTY);
            }
        }
    }

    protected abstract AppDeployConfigVO getDeployConfig(DevopsCiJobVO devopsCiJobVO);

    @Override
    public void fillJobAdditionalInfo(DevopsCiJobVO devopsCiJobVO) {
        CiCdPipelineDTO ciCdPipelineDTO = devopsCiPipelineService.baseQueryById(devopsCiJobVO.getCiPipelineId());
        Long projectId = ciCdPipelineDTO.getProjectId();
        DevopsEnvironmentDTO devopsEnvironmentDTO = queryEnvironmentByJobConfigId(devopsCiJobVO.getConfigId());
        devopsCiJobVO.setEdit(devopsEnvironmentService.hasEnvironmentPermission(devopsEnvironmentDTO, projectId));
    }

    protected abstract DevopsEnvironmentDTO queryEnvironmentByJobConfigId(Long configId);

    @Override
    public void setCiJobConfig(DevopsCiJobDTO job, CiJob ciJob) {
        Map<String, String> variables = new HashMap<>();
        variables.put("GIT_STRATEGY", "none");
        ciJob.setVariables(variables);
    }
}
