package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineVO;
import io.choerodon.devops.api.vo.iam.entity.PipelineE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineConvertor.java
import io.choerodon.devops.infra.dto.PipelineDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineConvertor.java
import io.choerodon.devops.infra.dataobject.PipelineDO;
=======
import io.choerodon.devops.infra.dto.DevopsPipelineDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/PipelineConvertor.java
>>>>>>> [IMP] 重构部分Repository
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:48 2019/4/4
 * Description:
 */
@Component
public class PipelineConvertor implements ConvertorI<PipelineE, DevopsPipelineDTO, PipelineVO> {
    @Override
    public PipelineE doToEntity(DevopsPipelineDTO pipelineDO) {
        PipelineE pipelineE = new PipelineE();
        BeanUtils.copyProperties(pipelineDO, pipelineE);
        return pipelineE;
    }

    @Override
    public DevopsPipelineDTO entityToDo(PipelineE pipelineE) {
        DevopsPipelineDTO pipelineDO = new DevopsPipelineDTO();
        BeanUtils.copyProperties(pipelineE, pipelineDO);
        return pipelineDO;
    }

    @Override
    public PipelineVO entityToDto(PipelineE pipelineE) {
        PipelineVO pipelineDTO = new PipelineVO();
        BeanUtils.copyProperties(pipelineE, pipelineDTO);
        return pipelineDTO;
    }
}
