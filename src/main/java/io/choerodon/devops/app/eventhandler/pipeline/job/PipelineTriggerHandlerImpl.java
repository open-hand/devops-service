package io.choerodon.devops.app.eventhandler.pipeline.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static io.choerodon.devops.app.service.impl.DevopsCiPipelineTriggerConfigServiceImpl.PIPELINE_TRIGGER_NAME_TEMPLATE;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiPipelineTriggerConfigVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiTemplatePipelineTriggerConfigVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiPipelineTriggerConfigService;
import io.choerodon.devops.app.service.DevopsCiPipelineTriggerConfigVariableService;
import io.choerodon.devops.app.service.DevopsCiTemplatePipelineTriggerConfigService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigVariableDTO;
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

    private static final String VARIABLE_TEMPLATE = "variables[%s]=%s";
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsCiPipelineTriggerConfigService devopsCiPipelineTriggerService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsCiPipelineTriggerConfigVariableService devopsCiPipelineTriggerConfigVariableService;

    @Autowired
    private DevopsCiTemplatePipelineTriggerConfigService devopsCiTemplatePipelineTriggerConfigService;

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
    }

    @Override
    protected Long saveConfig(Long ciPipelineId, DevopsCiJobVO devopsCiJobVO) {
        DevopsCiPipelineTriggerConfigDTO devopsCiPipelineTriggerConfigDTO = ConvertUtils.convertObject(devopsCiJobVO.getDevopsCiPipelineTriggerConfigVO(), DevopsCiPipelineTriggerConfigDTO.class);

        AppServiceDTO currentAppServiceDTO = appServiceService.queryByPipelineId(ciPipelineId);

        AppServiceDTO targetAppServiceDTO = appServiceService.queryByPipelineId(devopsCiPipelineTriggerConfigDTO.getTriggeredPipelineId());
        devopsCiPipelineTriggerConfigDTO.setTriggeredPipelineGitlabProjectId(targetAppServiceDTO.getGitlabProjectId().longValue());
        devopsCiPipelineTriggerConfigDTO.setTriggeredPipelineProjectId(targetAppServiceDTO.getProjectId());
        devopsCiPipelineTriggerConfigDTO.setPipelineId(ciPipelineId);

        String pipelineTriggerName = String.format(PIPELINE_TRIGGER_NAME_TEMPLATE, currentAppServiceDTO.getCode());

        List<PipelineTrigger> pipelineTriggers = gitlabServiceClientOperator.listPipelineTrigger(targetAppServiceDTO.getGitlabProjectId(), DetailsHelper.getUserDetails().getUserId());
        PipelineTrigger pipelineTrigger = pipelineTriggers.stream().filter(t -> t.getDescription().equals(pipelineTriggerName)).findAny().orElse(null);
        if (pipelineTrigger != null) {
            devopsCiPipelineTriggerConfigDTO.setToken(pipelineTrigger.getToken());
            devopsCiPipelineTriggerConfigDTO.setPipelineTriggerId(pipelineTrigger.getId().longValue());
        } else {
            PipelineTrigger createdPipelineTrigger = gitlabServiceClientOperator.createPipelineTrigger(targetAppServiceDTO.getGitlabProjectId(), DetailsHelper.getUserDetails().getUserId(), pipelineTriggerName);
            if (createdPipelineTrigger == null) {
                throw new CommonException("error.ci.job.pipeline.trigger.config.create");
            }
            devopsCiPipelineTriggerConfigDTO.setToken(createdPipelineTrigger.getToken());
            devopsCiPipelineTriggerConfigDTO.setPipelineTriggerId(createdPipelineTrigger.getId().longValue());
        }

        devopsCiPipelineTriggerService.baseCreate(devopsCiPipelineTriggerConfigDTO);

        // 保存变量
        if (!ObjectUtils.isEmpty(devopsCiJobVO.getDevopsCiPipelineTriggerConfigVO().getDevopsCiPipelineVariables())) {
            devopsCiJobVO.getDevopsCiPipelineTriggerConfigVO().getDevopsCiPipelineVariables().forEach(devopsCiPipelineVariableDTO -> {
                devopsCiPipelineVariableDTO.setId(null);
                devopsCiPipelineVariableDTO.setPipelineTriggerConfigId(devopsCiPipelineTriggerConfigDTO.getId());
                devopsCiPipelineTriggerConfigVariableService.baseCreate(devopsCiPipelineVariableDTO);
            });
        }

        return devopsCiPipelineTriggerConfigDTO.getId();
    }

    @Override
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        DevopsCiPipelineTriggerConfigVO devopsCiPipelineTriggerConfigVO = devopsCiPipelineTriggerService.queryConfigVoById(devopsCiJobVO.getConfigId());
        devopsCiPipelineTriggerConfigVO.setToken(null);
        devopsCiJobVO.setDevopsCiPipelineTriggerConfigVO(devopsCiPipelineTriggerConfigVO);
    }

    @Override
    public void fillJobTemplateConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        DevopsCiTemplatePipelineTriggerConfigVO devopsCiTemplatePipelineTriggerConfigVO = devopsCiTemplatePipelineTriggerConfigService.queryConfigVoById(devopsCiJobVO.getConfigId());
        if (devopsCiTemplatePipelineTriggerConfigVO != null) {
            DevopsCiPipelineTriggerConfigVO devopsCiPipelineTriggerConfigVO = ConvertUtils.convertObject(devopsCiTemplatePipelineTriggerConfigVO, DevopsCiPipelineTriggerConfigVO.class);
            devopsCiPipelineTriggerConfigVO.setDevopsCiPipelineVariables(ConvertUtils.convertList(devopsCiTemplatePipelineTriggerConfigVO.getDevopsCiPipelineVariables(), DevopsCiPipelineTriggerConfigVariableDTO.class));
            devopsCiJobVO.setDevopsCiPipelineTriggerConfigVO(devopsCiPipelineTriggerConfigVO);
        }
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        DevopsCiPipelineTriggerConfigVO devopsCiPipelineTriggerConfigVO = devopsCiPipelineTriggerService.queryConfigVoById(devopsCiJobDTO.getConfigId());
        String variable = "";
        if (!ObjectUtils.isEmpty(devopsCiPipelineTriggerConfigVO.getDevopsCiPipelineVariables())) {
            List<String> variables = new ArrayList<>();

            devopsCiPipelineTriggerConfigVO.getDevopsCiPipelineVariables().forEach(v -> {
                String urlEncodedValue;
                try {
                    urlEncodedValue = URLEncoder.encode(v.getVariableValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                variables.add(String.format(VARIABLE_TEMPLATE, v.getVariableKey(), urlEncodedValue));
            });
            variable = String.join("&", variables);
        }

        List<String> cmds = new ArrayList<>();
        cmds.add(String.format("pipeline_trigger %s %s %s %s %s", devopsCiPipelineTriggerConfigVO.getId(), devopsCiPipelineTriggerConfigVO.getRefName(), devopsCiPipelineTriggerConfigVO.getTriggeredPipelineProjectId(), devopsCiPipelineTriggerConfigVO.getToken(), variable));
        return cmds;
    }
}
