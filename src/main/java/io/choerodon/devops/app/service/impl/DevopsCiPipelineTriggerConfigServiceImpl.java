package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.pipeline.DevopsCiPipelineTriggerConfigVO;
import io.choerodon.devops.app.service.DevopsCiPipelineTriggerConfigService;
import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineTriggerConfigMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsCiPipelineTriggerConfigServiceImpl implements DevopsCiPipelineTriggerConfigService {

    @Autowired
    DevopsCiPipelineTriggerConfigMapper devopsCiPipelineTriggerConfigMapper;

    @Autowired
    GitlabServiceClientOperator gitlabServiceClientOperator;

    @Override
    public DevopsCiPipelineTriggerConfigDTO baseCreate(DevopsCiPipelineTriggerConfigDTO devopsCiPipelineTriggerConfigDTO) {
        return MapperUtil.resultJudgedInsertSelective(devopsCiPipelineTriggerConfigMapper, devopsCiPipelineTriggerConfigDTO, "error.ci.job.pipeline.trigger.config.create");
    }

    @Override
    public DevopsCiPipelineTriggerConfigVO queryConfigVoById(Long configId) {
        return ConvertUtils.convertObject(devopsCiPipelineTriggerConfigMapper.selectByPrimaryKey(configId), DevopsCiPipelineTriggerConfigVO.class);
    }

    @Override
    public void deleteByJobIds(List<Long> jobIds) {
        List<DevopsCiPipelineTriggerConfigDTO> devopsCiPipelineTriggerConfigDTOList = devopsCiPipelineTriggerConfigMapper.listByJobIds(jobIds);
        if (CollectionUtils.isEmpty(devopsCiPipelineTriggerConfigDTOList)) {
            return;
        }
        devopsCiPipelineTriggerConfigDTOList.forEach(devopsCiPipelineTriggerConfigDTO -> {
            gitlabServiceClientOperator.deletePipelineTrigger(devopsCiPipelineTriggerConfigDTO.getTriggeredPipelineGitlabProjectId().intValue(), DetailsHelper.getUserDetails().getUserId(), devopsCiPipelineTriggerConfigDTO.getPipelineTriggerId());
        });
        devopsCiPipelineTriggerConfigMapper.deleteByIds(devopsCiPipelineTriggerConfigDTOList.stream().map(DevopsCiPipelineTriggerConfigDTO::getId).collect(Collectors.toList()));
    }
}
