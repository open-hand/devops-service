package io.choerodon.devops.app.eventhandler.pipeline.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.AppDeployConfigVO;
import io.choerodon.devops.api.vo.pipeline.CiDeployDeployCfgVO;
import io.choerodon.devops.app.service.CiDeployDeployCfgService;
import io.choerodon.devops.app.service.CiTplDeployDeployCfgService;
import io.choerodon.devops.app.service.DevopsDeployAppCenterService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiDeployDeployCfgDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 14:47
 */
@Service
public class DeploymentDeployJobHandlerImpl extends AbstractAppDeployJobHandlerImpl {

    @Autowired
    private CiDeployDeployCfgService ciDeployDeployCfgService;
    @Autowired
    private CiTplDeployDeployCfgService ciTplDeployDeployCfgService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;


    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.DEPLOYMENT_DEPLOY;
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
        CiDeployDeployCfgVO ciDeployDeployCfg = devopsCiJobVO.getCiDeployDeployCfg();
        if (DeployTypeEnum.CREATE.value().equals(ciDeployDeployCfg.getDeployType())) {
            // 校验应用编码和应用名称
            devopsDeployAppCenterService.checkNameAndCodeUniqueAndThrow(ciDeployDeployCfg.getEnvId(),
                    RdupmTypeEnum.DEPLOYMENT.value(),
                    null,
                    ciDeployDeployCfg.getAppName(),
                    ciDeployDeployCfg.getAppCode());
        } else {
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(ciDeployDeployCfg.getAppId());
            ciDeployDeployCfg.setAppCode(devopsDeployAppCenterEnvDTO.getCode());
            ciDeployDeployCfg.setAppName(devopsDeployAppCenterEnvDTO.getName());
            if (!devopsDeployAppCenterEnvDTO.getEnvId().equals(ciDeployDeployCfg.getEnvId())) {
                throw new CommonException(PipelineCheckConstant.DEVOPS_APP_EXIST_IN_OTHER_ENV, devopsCiJobVO.getName());
            }
        }
    }

    @Override
    public void deleteCdInfo(DevopsCiJobVO devopsCiJobVO) {
        CiDeployDeployCfgVO ciDeployDeployCfg = devopsCiJobVO.getCiDeployDeployCfg();
        if (ciDeployDeployCfg != null) {
            CiDeployDeployCfgVO ciChartDeployConfigVO = new CiDeployDeployCfgVO();
            ciChartDeployConfigVO.setSkipCheckPermission(ciDeployDeployCfg.getSkipCheckPermission());
            devopsCiJobVO.setCiDeployDeployCfg(ciChartDeployConfigVO);
        }
        devopsCiJobVO.setCompleted(false);
    }

    @Override
    protected AppDeployConfigVO getDeployConfig(DevopsCiJobVO devopsCiJobVO) {
        return devopsCiJobVO.getCiDeployDeployCfg();
    }

    @Override
    protected Long saveConfig(Long ciPipelineId, DevopsCiJobVO devopsCiJobVO) {
        CiDeployDeployCfgVO ciDeployDeployCfg = devopsCiJobVO.getCiDeployDeployCfg();
        CiDeployDeployCfgDTO ciDeployDeployCfgDTO = ConvertUtils.convertObject(ciDeployDeployCfg, CiDeployDeployCfgDTO.class);
        if (ciDeployDeployCfg.getAppConfig() != null) {
            ciDeployDeployCfgDTO.setAppConfigJson(JsonHelper.marshalByJackson(ciDeployDeployCfg.getAppConfig()));
        }
        if (ciDeployDeployCfg.getContainerConfig() != null) {
            ciDeployDeployCfgDTO.setContainerConfigJson(JsonHelper.marshalByJackson(ciDeployDeployCfg.getContainerConfig()));
        }
        ciDeployDeployCfgDTO.setId(null);
        ciDeployDeployCfgDTO.setCiPipelineId(ciPipelineId);

        ciDeployDeployCfgService.baseCreate(ciDeployDeployCfgDTO);
        return ciDeployDeployCfgDTO.getId();
    }

    @Override
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        CiDeployDeployCfgVO ciDeployDeployCfgVO = ciDeployDeployCfgService.queryConfigVoById(devopsCiJobVO.getConfigId());
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(ciDeployDeployCfgVO.getEnvId());
        ciDeployDeployCfgVO.setEnvName(devopsEnvironmentDTO.getName());
        devopsCiJobVO.setCiDeployDeployCfg(ciDeployDeployCfgVO);
    }

    @Override
    public void fillJobTemplateConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setCiDeployDeployCfg(ciTplDeployDeployCfgService.queryConfigVoById(devopsCiJobVO.getConfigId()));
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        CiDeployDeployCfgVO ciDeployDeployCfgVO = ciDeployDeployCfgService.queryConfigVoById(devopsCiJobDTO.getConfigId());
        List<String> cmds = new ArrayList<>();
        cmds.add(String.format("deployment_deploy %s", ciDeployDeployCfgVO.getId()));
        return cmds;
    }

    @Override
    protected DevopsEnvironmentDTO queryEnvironmentByJobConfigId(Long configId) {
        CiDeployDeployCfgDTO ciDeployDeployCfgDTO = ciDeployDeployCfgService.queryConfigById(configId);
        return devopsEnvironmentService.baseQueryById(ciDeployDeployCfgDTO.getEnvId());
    }
}
