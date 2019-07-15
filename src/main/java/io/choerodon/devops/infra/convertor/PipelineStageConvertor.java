package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineStageVO;
import io.choerodon.devops.api.vo.iam.entity.PipelineStageE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineStageConvertor.java
import io.choerodon.devops.infra.dto.PipelineStageDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineStageDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineStageConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineStageConvertor.java
import io.choerodon.devops.infra.dataobject.PipelineStageDO;
=======
import io.choerodon.devops.infra.dto.PipelineStageDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/PipelineStageConvertor.java
>>>>>>> [IMP]修改后端结构
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:28 2019/4/8
 * Description:
 */
@Component
public class PipelineStageConvertor implements ConvertorI<PipelineStageE, PipelineStageDTO, PipelineStageVO> {
    @Override
    public PipelineStageE dtoToEntity(PipelineStageVO pipelineStageVO) {
        PipelineStageE pipelineStageE = new PipelineStageE();
        BeanUtils.copyProperties(pipelineStageVO, pipelineStageE);
        return pipelineStageE;
    }

    @Override
    public PipelineStageVO entityToDto(PipelineStageE pipelineStageE) {
        PipelineStageVO pipelineStageVO = new PipelineStageVO();
        BeanUtils.copyProperties(pipelineStageE, pipelineStageVO);
        return pipelineStageVO;
    }

    @Override
    public PipelineStageDTO entityToDo(PipelineStageE pipelineStageE) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        BeanUtils.copyProperties(pipelineStageE, pipelineStageDTO);
        return pipelineStageDTO;
    }

    @Override
    public PipelineStageE doToEntity(PipelineStageDTO pipelineStageDTO) {
        PipelineStageE pipelineStageE = new PipelineStageE();
        BeanUtils.copyProperties(pipelineStageDTO, pipelineStageE);
        return pipelineStageE;
    }
}
