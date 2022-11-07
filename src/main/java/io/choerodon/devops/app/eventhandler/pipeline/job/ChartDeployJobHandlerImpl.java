package io.choerodon.devops.app.eventhandler.pipeline.job;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.CiChartDeployConfigVO;
import io.choerodon.devops.app.service.CiChartDeployConfigService;
import io.choerodon.devops.app.service.CiTplChartDeployCfgService;
import io.choerodon.devops.infra.dto.CiChartDeployConfigDTO;
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
public class ChartDeployJobHandlerImpl extends AbstractJobHandler {

    @Autowired
    private CiChartDeployConfigService ciChartDeployConfigService;
    @Autowired
    private CiTplChartDeployCfgService ciTplChartDeployCfgService;

    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.CHART_DEPLOY;
    }

    @Override
    protected Long saveConfig(DevopsCiJobVO devopsCiJobVO) {
        CiChartDeployConfigVO ciChartDeployConfig = devopsCiJobVO.getCiChartDeployConfig();
        CiChartDeployConfigDTO ciChartDeployConfigDTO = ConvertUtils.convertObject(ciChartDeployConfig, CiChartDeployConfigDTO.class);
        ciChartDeployConfigDTO.setId(null);

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
}
