package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineRecordDTO;
import io.choerodon.devops.api.vo.iam.entity.PipelineRecordE;
import io.choerodon.devops.infra.dataobject.PipelineRecordDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:45 2019/4/16
 * Description:
 */
@Component
public class PipelineRecordConvertor implements ConvertorI<PipelineRecordE, PipelineRecordDO, PipelineRecordDTO> {
    @Override
    public PipelineRecordE doToEntity(PipelineRecordDO pipelineRecordDO) {
        PipelineRecordE pipelineRecordE = new PipelineRecordE();
        BeanUtils.copyProperties(pipelineRecordDO, pipelineRecordE);
        return pipelineRecordE;
    }

    @Override
    public PipelineRecordDO entityToDo(PipelineRecordE pipelineRecordE) {
        PipelineRecordDO pipelineRecordDO = new PipelineRecordDO();
        BeanUtils.copyProperties(pipelineRecordE, pipelineRecordDO);
        return pipelineRecordDO;
    }

    @Override
    public PipelineRecordDTO entityToDto(PipelineRecordE pipelineRecordE) {
        PipelineRecordDTO pipelineRecordDTO = new PipelineRecordDTO();
        BeanUtils.copyProperties(pipelineRecordE, pipelineRecordDTO);
        return pipelineRecordDTO;
    }
}
