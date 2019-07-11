package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineStageDTO;
import io.choerodon.devops.api.vo.iam.entity.PipelineStageE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineStageConvertor.java
import io.choerodon.devops.infra.dto.PipelineStageDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineStageDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineStageConvertor.java
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:28 2019/4/8
 * Description:
 */
@Component
public class PipelineStageConvertor implements ConvertorI<PipelineStageE, PipelineStageDO, PipelineStageDTO> {
    @Override
    public PipelineStageE dtoToEntity(PipelineStageDTO pipelineStageDTO) {
        PipelineStageE pipelineStageE = new PipelineStageE();
        BeanUtils.copyProperties(pipelineStageDTO, pipelineStageE);
        return pipelineStageE;
    }

    @Override
    public PipelineStageDTO entityToDto(PipelineStageE pipelineStageE) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        BeanUtils.copyProperties(pipelineStageE, pipelineStageDTO);
        return pipelineStageDTO;
    }

    @Override
    public PipelineStageDO entityToDo(PipelineStageE pipelineStageE) {
        PipelineStageDO pipelineStageDO = new PipelineStageDO();
        BeanUtils.copyProperties(pipelineStageE, pipelineStageDO);
        return pipelineStageDO;
    }

    @Override
    public PipelineStageE doToEntity(PipelineStageDO pipelineStageDO) {
        PipelineStageE pipelineStageE = new PipelineStageE();
        BeanUtils.copyProperties(pipelineStageDO, pipelineStageE);
        return pipelineStageE;
    }
}
