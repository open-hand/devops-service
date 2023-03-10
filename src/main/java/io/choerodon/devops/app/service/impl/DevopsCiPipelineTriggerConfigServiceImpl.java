package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.pipeline.DevopsCiPipelineTriggerConfigVO;
import io.choerodon.devops.app.service.DevopsCiPipelineTriggerConfigService;
import io.choerodon.devops.app.service.DevopsCiPipelineTriggerConfigVariableService;
import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineTriggerConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsCiPipelineTriggerConfigServiceImpl implements DevopsCiPipelineTriggerConfigService {
    private static final Logger logger = LoggerFactory.getLogger(DevopsCiPipelineTriggerConfigServiceImpl.class);

    public static final String PIPELINE_TRIGGER_NAME_TEMPLATE = "choerodon-pipeline-trigger-for:%s";

    @Autowired
    private DevopsCiPipelineTriggerConfigMapper devopsCiPipelineTriggerConfigMapper;

    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Autowired
    private DevopsCiPipelineTriggerConfigVariableService ciPipelineTriggerConfigVariableService;

    @Override
    public DevopsCiPipelineTriggerConfigDTO baseCreate(DevopsCiPipelineTriggerConfigDTO devopsCiPipelineTriggerConfigDTO) {
        return MapperUtil.resultJudgedInsertSelective(devopsCiPipelineTriggerConfigMapper, devopsCiPipelineTriggerConfigDTO, "error.ci.job.pipeline.trigger.config.create");
    }

    @Override
    public DevopsCiPipelineTriggerConfigVO queryConfigVoById(Long configId) {
        return devopsCiPipelineTriggerConfigMapper.queryConfigVoByIdWithVariables(configId);
    }

    @Override
    public void deleteByJobIds(List<Long> jobIds) {
        List<DevopsCiPipelineTriggerConfigDTO> devopsCiPipelineTriggerConfigDTOList = devopsCiPipelineTriggerConfigMapper.listByJobIds(jobIds);
        if (CollectionUtils.isEmpty(devopsCiPipelineTriggerConfigDTOList)) {
            return;
        }
        devopsCiPipelineTriggerConfigDTOList.forEach(devopsCiPipelineTriggerConfigDTO -> {
            try {
                logger.info("try to delete pipeline trigger");
                gitlabServiceClientOperator.deletePipelineTrigger(devopsCiPipelineTriggerConfigDTO.getTriggeredPipelineGitlabProjectId().intValue(), DetailsHelper.getUserDetails().getUserId(), devopsCiPipelineTriggerConfigDTO.getPipelineTriggerId());
            } catch (Exception ignored) {
                // 尝试删除线触发器，如果删除失败，不处理异常，继续向下执行
                logger.info(ignored.getMessage());
            }
        });

        List<Long> configIds = devopsCiPipelineTriggerConfigDTOList.stream().map(DevopsCiPipelineTriggerConfigDTO::getId).collect(Collectors.toList());

        devopsCiPipelineTriggerConfigMapper.deleteByIds(configIds);
        ciPipelineTriggerConfigVariableService.deleteByPipelineTriggerConfigIds(configIds);
    }
}