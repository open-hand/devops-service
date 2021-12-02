package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiTemplateStageService;
import io.choerodon.devops.infra.dto.CiTemplateStageDTO;
import io.choerodon.devops.infra.mapper.CiTemplateStageMapper;

/**
 * 流水线模阶段(CiTemplateStage)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:20
 */
@Service
public class CiTemplateStageServiceImpl implements CiTemplateStageService {
    @Autowired
    private CiTemplateStageMapper ciTemplateStageMapper;


    @Override
    public List<CiTemplateStageDTO> listByPipelineTemplateIds(Set<Long> pipelineTemplateIds) {
        return ciTemplateStageMapper.listByPipelineTemplateIds(pipelineTemplateIds);
    }

    @Override
    public List<CiTemplateStageDTO> listByPipelineTemplateId(Long pipelineTemplateId) {
        Assert.notNull(pipelineTemplateId, "error.pipelineTemplateId.is.null");

        CiTemplateStageDTO ciTemplateStageDTO = new CiTemplateStageDTO();
        ciTemplateStageDTO.setPipelineTemplateId(pipelineTemplateId);

        return ciTemplateStageMapper.select(ciTemplateStageDTO);
    }
}

