package io.choerodon.devops.app.eventhandler.pipeline.job;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.CiDeployDeployCfgVO;
import io.choerodon.devops.app.service.CiDeployDeployCfgService;
import io.choerodon.devops.app.service.CiTplDeployDeployCfgService;
import io.choerodon.devops.infra.dto.CiDeployDeployCfgDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 14:47
 */
@Service
public class DeploymentDeployJobHandlerImpl extends AbstractJobHandler {

    @Autowired
    private CiDeployDeployCfgService ciDeployDeployCfgService;
    @Autowired
    private CiTplDeployDeployCfgService ciTplDeployDeployCfgService;


    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.DEPLOYMENT_DEPLOY;
    }

    @Override
    protected Long saveConfig(DevopsCiJobVO devopsCiJobVO) {
        CiDeployDeployCfgVO ciDeployDeployCfg = devopsCiJobVO.getCiDeployDeployCfg();
        CiDeployDeployCfgDTO ciDeployDeployCfgDTO = ConvertUtils.convertObject(ciDeployDeployCfg, CiDeployDeployCfgDTO.class);
        ciDeployDeployCfgDTO.setId(null);

        ciDeployDeployCfgService.baseCreate(ciDeployDeployCfgDTO);
        return ciDeployDeployCfgDTO.getId();
    }

    @Override
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setCiDeployDeployCfg(ciDeployDeployCfgService.queryConfigVoById(devopsCiJobVO.getConfigId()));
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
}
