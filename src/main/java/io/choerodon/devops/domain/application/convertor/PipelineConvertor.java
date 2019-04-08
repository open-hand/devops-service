package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.PipelineDTO;
import io.choerodon.devops.domain.application.entity.PipelineE;
import io.choerodon.devops.infra.dataobject.PipelineDO;
import org.springframework.beans.BeanUtils;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:48 2019/4/4
 * Description:
 */
public class PipelineConvertor implements ConvertorI<PipelineE, PipelineDO, PipelineDTO> {
    @Override
    public PipelineE doToEntity(PipelineDO pipelineDO) {
        PipelineE pipelineE = new PipelineE();
        BeanUtils.copyProperties(pipelineDO, pipelineE);
        return pipelineE;
    }
}
