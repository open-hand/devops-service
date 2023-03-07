package io.choerodon.devops.app.eventhandler.pipeline.job;

import static io.choerodon.devops.infra.constant.ExceptionConstants.DeployValueCode.DEVOPS_DEPLOY_VALUE_ID_NULL;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.AppDeployConfigVO;
import io.choerodon.devops.api.vo.pipeline.CiChartDeployConfigVO;
import io.choerodon.devops.app.service.CiChartDeployConfigService;
import io.choerodon.devops.app.service.CiTplChartDeployCfgService;
import io.choerodon.devops.app.service.DevopsDeployAppCenterService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiChartDeployConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 14:47
 */
@Service
public class ChartDeployJobHandlerImpl extends AbstractAppDeployJobHandlerImpl {

    @Autowired
    private CiChartDeployConfigService ciChartDeployConfigService;
    @Autowired
    private CiTplChartDeployCfgService ciTplChartDeployCfgService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;


    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.CHART_DEPLOY;
    }

    /**
     * 校验任务配置信息
     *
     * @param projectId
     * @param devopsCiJobVO
     */
    @Override
    protected void checkConfigInfo(Long projectId, DevopsCiJobVO devopsCiJobVO) {
        super.checkConfigInfo(projectId, devopsCiJobVO);
        CiChartDeployConfigVO ciChartDeployConfig = devopsCiJobVO.getCiChartDeployConfig();
        if (DeployTypeEnum.CREATE.value().equals(ciChartDeployConfig.getDeployType())) {
            // 校验应用编码和应用名称
            devopsDeployAppCenterService.checkNameAndCodeUniqueAndThrow(ciChartDeployConfig.getEnvId(),
                    RdupmTypeEnum.CHART.value(),
                    null,
                    ciChartDeployConfig.getAppName(),
                    ciChartDeployConfig.getAppCode());
        } else {
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(ciChartDeployConfig.getAppId());
            if (devopsDeployAppCenterEnvDTO == null) {
                throw new CommonException(PipelineCheckConstant.DEVOPS_APP_NOT_EXIST);
            }
            ciChartDeployConfig.setAppCode(devopsDeployAppCenterEnvDTO.getCode());
            ciChartDeployConfig.setAppName(devopsDeployAppCenterEnvDTO.getName());
            if (!devopsDeployAppCenterEnvDTO.getEnvId().equals(ciChartDeployConfig.getEnvId())) {
                throw new CommonException(PipelineCheckConstant.DEVOPS_APP_EXIST_IN_OTHER_ENV, devopsCiJobVO.getName());
            }
        }
        if (ciChartDeployConfig.getValueId() == null) {
            throw new CommonException(DEVOPS_DEPLOY_VALUE_ID_NULL);
        }
    }

    @Override
    public void deleteCdInfo(DevopsCiJobVO devopsCiJobVO) {
        CiChartDeployConfigVO ciChartDeployConfig = devopsCiJobVO.getCiChartDeployConfig();
        if (ciChartDeployConfig != null) {
            CiChartDeployConfigVO ciChartDeployConfigVO = new CiChartDeployConfigVO();
            ciChartDeployConfigVO.setSkipCheckPermission(ciChartDeployConfig.getSkipCheckPermission());
            devopsCiJobVO.setCiChartDeployConfig(ciChartDeployConfigVO);
        }
        devopsCiJobVO.setCompleted(false);

    }

    @Override
    protected AppDeployConfigVO getDeployConfig(DevopsCiJobVO devopsCiJobVO) {
        return devopsCiJobVO.getCiChartDeployConfig();
    }

    @Override
    protected Long saveConfig(Long ciPipelineId, DevopsCiJobVO devopsCiJobVO) {
        CiChartDeployConfigVO ciChartDeployConfig = devopsCiJobVO.getCiChartDeployConfig();
        CiChartDeployConfigDTO ciChartDeployConfigDTO = ConvertUtils.convertObject(ciChartDeployConfig, CiChartDeployConfigDTO.class);
        ciChartDeployConfigDTO.setId(null);
        ciChartDeployConfigDTO.setCiPipelineId(ciPipelineId);

        ciChartDeployConfigService.baseCreate(ciChartDeployConfigDTO);
        return ciChartDeployConfigDTO.getId();
    }

    @Override
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setCiChartDeployConfig(ciChartDeployConfigService.queryConfigVoById(devopsCiJobVO.getConfigId()));
    }

    @Override
    public void fillJobTemplateConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setCiChartDeployConfig(ciTplChartDeployCfgService.queryConfigVoById(devopsCiJobVO.getConfigId()));
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        CiChartDeployConfigVO ciChartDeployConfigVO = ciChartDeployConfigService.queryConfigVoById(devopsCiJobDTO.getConfigId());
        List<String> cmds = new ArrayList<>();
        cmds.add(String.format("chart_deploy %s", ciChartDeployConfigVO.getId()));
        return cmds;
    }

    @Override
    protected DevopsEnvironmentDTO queryEnvironmentByJobConfigId(Long configId) {
        CiChartDeployConfigDTO ciChartDeployConfigDTO = ciChartDeployConfigService.queryConfigById(configId);
        return devopsEnvironmentService.baseQueryById(ciChartDeployConfigDTO.getEnvId());
    }
}
