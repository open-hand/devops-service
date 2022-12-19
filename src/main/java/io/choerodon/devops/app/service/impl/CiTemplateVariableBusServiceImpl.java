package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.template.CiTemplateVariableVO;
import io.choerodon.devops.app.service.CiTemplateVariableBusService;
import io.choerodon.devops.infra.dto.CiTemplateVariableDTO;
import io.choerodon.devops.infra.mapper.CiTemplateVariableBusMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * Created by wangxiang on 2021/12/22
 */
@Service
public class CiTemplateVariableBusServiceImpl implements CiTemplateVariableBusService {

    @Autowired
    private CiTemplateVariableBusMapper ciTemplateVariableBusMapper;

    @Override
    public List<CiTemplateVariableVO> queryCiVariableByPipelineTemplateId(Long sourceId, Long ciPipelineTemplateId) {
        CiTemplateVariableDTO ciTemplateVariableDTO = new CiTemplateVariableDTO();
        ciTemplateVariableDTO.setPipelineTemplateId(ciPipelineTemplateId);
        List<CiTemplateVariableDTO> ciTemplateVariableDTOS = ciTemplateVariableBusMapper.select(ciTemplateVariableDTO);
        return ConvertUtils.convertList(ciTemplateVariableDTOS, CiTemplateVariableVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplatePipelineId(Long ciTemplatePipelineId) {
        CiTemplateVariableDTO ciTemplateVariable = new CiTemplateVariableDTO();
        ciTemplateVariable.setPipelineTemplateId(ciTemplatePipelineId);
        ciTemplateVariableBusMapper.delete(ciTemplateVariable);
    }
}
