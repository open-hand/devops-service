package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiChartPublishConfigService;
import io.choerodon.devops.app.service.CiTplChartPublishConfigService;
import io.choerodon.devops.infra.dto.CiChartPublishConfigDTO;
import io.choerodon.devops.infra.dto.CiTplChartPublishConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 17:33
 */
@Service
public class DevopsChartStepHandler extends AbstractDevopsCiStepHandler {

    private static final String CHART_BUILD_CMD = "chart_build %s";

    private static final String CHART_BUILD_CMD_DEFAULT = "chart_build";

    @Autowired
    private CiChartPublishConfigService ciChartPublishConfigService;
    @Autowired
    private CiTplChartPublishConfigService ciTplChartPublishConfigService;

    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
        ciTemplateStepVO.setChartPublishConfig(ciTplChartPublishConfigService.queryByStepId(ciTemplateStepVO.getId()));
    }


    @Override
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiChartPublishConfigDTO ciChartPublishConfigDTO = ciChartPublishConfigService.queryByStepId(devopsCiStepVO.getId());
        devopsCiStepVO.setChartPublishConfig(ciChartPublishConfigDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        CiTplChartPublishConfigDTO ciTplChartPublishConfigDTO = ciTemplateStepVO.getChartPublishConfig();
        ciTplChartPublishConfigDTO.setCiTemplateStepId(ciTemplateStepVO.getId());
        ciTplChartPublishConfigService.baseCreate(ciTplChartPublishConfigDTO);
    }

    @Override
    public void deleteTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        ciTplChartPublishConfigService.deleteByTemplateStepId(ciTemplateStepVO.getId());
    }

    @Override
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiChartPublishConfigDTO ciChartPublishConfigDTO = ciChartPublishConfigService.queryByStepId(devopsCiStepVO.getId());
        devopsCiStepVO.setChartPublishConfig(ciChartPublishConfigDTO);
    }

    @Override
    @Transactional
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        // 保存任务配置
        CiChartPublishConfigDTO chartPublishConfig = devopsCiStepVO.getChartPublishConfig();
        chartPublishConfig.setId(null);
        chartPublishConfig.setStepId(stepId);
        ciChartPublishConfigService.baseCreate(chartPublishConfig);
    }

    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        List<String> cmds = new ArrayList<>();

        CiChartPublishConfigDTO ciChartPublishConfigDTO = ciChartPublishConfigService.queryByStepId(devopsCiStepDTO.getId());
        if (ciChartPublishConfigDTO == null || ciChartPublishConfigDTO.getUseDefaultRepo()) {
            cmds.add(CHART_BUILD_CMD_DEFAULT);
        } else {
            cmds.add(String.format(CHART_BUILD_CMD, ciChartPublishConfigDTO.getRepoId()));
        }
        return cmds;
    }

    @Override
    @Transactional
    public void batchDeleteConfig(Set<Long> stepIds) {
        ciChartPublishConfigService.batchDeleteByStepIds(stepIds);
    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.UPLOAD_CHART;
    }

    @Override
    public Boolean isComplete(DevopsCiStepVO devopsCiStepVO) {
        CiChartPublishConfigDTO chartPublishConfig = devopsCiStepVO.getChartPublishConfig();
        if (chartPublishConfig == null) {
            return false;
        }
        if (Boolean.FALSE.equals(chartPublishConfig.getUseDefaultRepo()) && chartPublishConfig.getRepoId() == null) {
            return false;
        }
        return true;
    }
}
