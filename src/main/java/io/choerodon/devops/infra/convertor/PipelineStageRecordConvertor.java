package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineStageRecordDTO;
import io.choerodon.devops.api.vo.iam.entity.PipelineStageRecordE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineStageRecordConvertor.java
import io.choerodon.devops.infra.dto.PipelineStageRecordDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineStageRecordDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineStageRecordConvertor.java
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:49 2019/4/14
 * Description:
 */
@Component
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

    @Override
    public PipelineStageRecordDTO entityToDto(PipelineStageRecordE stageRecordE) {
        PipelineStageRecordDTO stageRecordDTO = new PipelineStageRecordDTO();
        BeanUtils.copyProperties(stageRecordE, stageRecordDTO);
        return stageRecordDTO;
    }

    @Override
    public PipelineStageRecordDO entityToDo(PipelineStageRecordE stageRecordE) {
        PipelineStageRecordDO stageRecordDO = new PipelineStageRecordDO();
        BeanUtils.copyProperties(stageRecordE, stageRecordDO);
        return stageRecordDO;
    }
}