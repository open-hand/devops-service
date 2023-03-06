package io.choerodon.devops.app.eventhandler.pipeline.job;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiPipelineTriggerConfigVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.app.service.DevopsCiPipelineTriggerConfigService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigDTO;
import io.choerodon.devops.infra.dto.gitlab.PipelineTrigger;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 14:47
 */
@Service
public class PipelineTriggerHandlerImpl extends AbstractJobHandler {
    private static final String PIPELINE_TRIGGER_NAME_TEMPLATE = "choerodon-pipeline-trigger-for:%s";

    @Autowired
    private DevopsCiPipelineService devopsCiPipelineService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsCiPipelineTriggerConfigService devopsCiPipelineTriggerService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.PIPELINE_TRIGGER;
    }

    /**
     * 校验任务配置信息
     *
     * @param projectId
     * @param devopsCiJobVO
     */
    @Override
    protected void checkConfigInfo(Long projectId, DevopsCiJobVO devopsCiJobVO) {
        // 检查用户拥有被触发流水线的权限
    }

    @Override
    protected Long saveConfig(Long ciPipelineId, DevopsCiJobVO devopsCiJobVO) {
        DevopsCiPipelineTriggerConfigDTO devopsCiPipelineTriggerConfigDTO = ConvertUtils.convertObject(devopsCiJobVO.getDevopsCiPipelineTriggerConfigVO(), DevopsCiPipelineTriggerConfigDTO.class);

        AppServiceDTO appServiceDTO = appServiceService.queryByPipelineId(devopsCiPipelineTriggerConfigDTO.getTriggeredPipelineId());
        devopsCiPipelineTriggerConfigDTO.setTriggeredPipelineGitlabProjectId(appServiceDTO.getGitlabProjectId().longValue());
        devopsCiPipelineTriggerConfigDTO.setPipelineId(ciPipelineId);

        String pipelineTriggerName = String.format(PIPELINE_TRIGGER_NAME_TEMPLATE, appServiceDTO.getCode());

        List<PipelineTrigger> pipelineTriggers = gitlabServiceClientOperator.listPipelineTrigger(appServiceDTO.getGitlabProjectId(), DetailsHelper.getUserDetails().getUserId());
        PipelineTrigger pipelineTrigger = pipelineTriggers.stream().filter(t -> t.getDescription().equals(pipelineTriggerName)).findAny().orElseGet(null);
        if (pipelineTrigger != null) {
            devopsCiPipelineTriggerConfigDTO.setPipelineTriggerId(pipelineTrigger.getId().longValue());
        } else {
            PipelineTrigger createdPipelineTrigger = gitlabServiceClientOperator.createPipelineTrigger(appServiceDTO.getGitlabProjectId(), DetailsHelper.getUserDetails().getUserId(), pipelineTriggerName);
            devopsCiPipelineTriggerConfigDTO.setPipelineTriggerId(createdPipelineTrigger.getId().longValue());
        }

        devopsCiPipelineTriggerService.baseCreate(devopsCiPipelineTriggerConfigDTO);
        return devopsCiPipelineTriggerConfigDTO.getId();
    }

    @Override
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setDevopsCiPipelineTriggerConfigVO(devopsCiPipelineTriggerService.queryConfigVoById(devopsCiJobVO.getConfigId()));
    }

    @Override
    public void fillJobTemplateConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setDevopsCiPipelineTriggerConfigVO(devopsCiPipelineTriggerService.queryConfigVoById(devopsCiJobVO.getConfigId()));
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        DevopsCiPipelineTriggerConfigVO devopsCiPipelineTriggerConfigVO = devopsCiPipelineTriggerService.queryConfigVoById(devopsCiJobDTO.getConfigId());
        List<String> cmds = new ArrayList<>();
        cmds.add(String.format("pipeline_trigger %s %s %s %s", devopsCiPipelineTriggerConfigVO.getId(), devopsCiPipelineTriggerConfigVO.getRefName(), devopsCiPipelineTriggerConfigVO.getTriggeredPipelineProjectId(), devopsCiPipelineTriggerConfigVO.getToken()));
        return cmds;
    }
}
