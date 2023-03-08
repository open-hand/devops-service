package io.choerodon.devops.app.service.impl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.pipeline.DevopsCiTemplatePipelineTriggerConfigVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.DevopsCiTemplatePipelineTriggerConfigService;
import io.choerodon.devops.app.service.DevopsCiTemplatePipelineTriggerConfigVariableService;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiTemplatePipelineTriggerConfigDTO;
import io.choerodon.devops.infra.mapper.CiTemplateJobMapper;
import io.choerodon.devops.infra.mapper.DevopsCiTemplatePipelineTriggerConfigMapper;

@Service
public class PipelineTriggerTemplateJobConfigService extends TemplateJobConfigService {

    @Autowired
    private DevopsCiTemplatePipelineTriggerConfigMapper devopsCiTemplatePipelineTriggerConfigMapper;
    @Autowired
    private DevopsCiTemplatePipelineTriggerConfigVariableService devopsCiTemplatePipelineTriggerConfigVariableService;

    @Autowired
    private DevopsCiTemplatePipelineTriggerConfigService devopsCiTemplatePipelineTriggerConfigService;

    @Autowired
    private CiTemplateJobMapper ciTemplateJobMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseInsert(CiTemplateJobVO ciTemplateJobVO) {
        DevopsCiTemplatePipelineTriggerConfigDTO devopsCiTemplatePipelineTriggerConfigDTO
                = ConvertUtils.convertObject(ciTemplateJobVO.getDevopsCiPipelineTriggerConfigVO(), DevopsCiTemplatePipelineTriggerConfigDTO.class);
        if (devopsCiTemplatePipelineTriggerConfigDTO == null) {
            return null;
        }
        devopsCiTemplatePipelineTriggerConfigDTO.setId(null);
        devopsCiTemplatePipelineTriggerConfigMapper.insertSelective(devopsCiTemplatePipelineTriggerConfigDTO);

        // 插入环境变量
        // 保存变量
        if (!ObjectUtils.isEmpty(ciTemplateJobVO.getDevopsCiPipelineTriggerConfigVO().getDevopsCiPipelineVariables())) {
            ciTemplateJobVO.getDevopsCiPipelineTriggerConfigVO().getDevopsCiPipelineVariables().forEach(devopsCiPipelineVariableDTO -> {
                devopsCiPipelineVariableDTO.setId(null);
                devopsCiPipelineVariableDTO.setPipelineTriggerTemplateConfigId(devopsCiTemplatePipelineTriggerConfigDTO.getId());
                devopsCiTemplatePipelineTriggerConfigVariableService.baseCreate(devopsCiPipelineVariableDTO);
            });
        }

        return devopsCiTemplatePipelineTriggerConfigDTO.getId();
    }

    @Override
    public void fillCdJobConfig(CiTemplateJobVO ciTemplateJobVO) {
        DevopsCiTemplatePipelineTriggerConfigVO devopsCiTemplatePipelineTriggerConfigVO = devopsCiTemplatePipelineTriggerConfigService.queryConfigVoById(ciTemplateJobVO.getConfigId());
        ciTemplateJobVO.setDevopsCiPipelineTriggerConfigVO(devopsCiTemplatePipelineTriggerConfigVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(CiTemplateJobVO ciTemplateJobVO) {
        CiTemplateJobDTO templateJobDTO = ciTemplateJobMapper.selectByPrimaryKey(ciTemplateJobVO.getId());
        if (templateJobDTO.getConfigId() == null) {
            return;
        }
        devopsCiTemplatePipelineTriggerConfigService.deleteById(templateJobDTO.getConfigId());
    }
}
