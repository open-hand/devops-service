package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.PipelineStageRecordDTO;
import io.choerodon.devops.domain.application.entity.PipelineStageRecordE;
import io.choerodon.devops.infra.dataobject.PipelineStageRecordDO;
import org.springframework.beans.BeanUtils;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:49 2019/4/14
 * Description:
 */
public class PipelineStageRecordConvertor implements ConvertorI<PipelineStageRecordE, PipelineStageRecordDO, PipelineStageRecordDTO> {
    @Override
    public PipelineStageRecordE dtoToEntity(PipelineStageRecordDTO stageRecordDTO) {
        PipelineStageRecordE stageRecordE = new PipelineStageRecordE();
        BeanUtils.copyProperties(stageRecordDTO, stageRecordE);
        return stageRecordE;
    }

    @Override
    public PipelineStageRecordE doToEntity(PipelineStageRecordDO stageRecordDO) {
        PipelineStageRecordE stageRecordE = new PipelineStageRecordE();
        BeanUtils.copyProperties(stageRecordDO, stageRecordE);
        return stageRecordE;
    }
}