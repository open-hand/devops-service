package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineUserRelDTO;
import io.choerodon.devops.api.vo.iam.entity.PipelineUserRelE;
import io.choerodon.devops.infra.dataobject.PipelineUserRelDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:11 2019/4/8
 * Description:
 */
@Component
public class PipelineUserRelConvertor implements ConvertorI<PipelineUserRelE, PipelineUserRelDO, PipelineUserRelDTO> {
    @Override
    public PipelineUserRelE dtoToEntity(PipelineUserRelDTO userRelDTO) {
        PipelineUserRelE pipelineUserRelE = new PipelineUserRelE();
        BeanUtils.copyProperties(userRelDTO, pipelineUserRelE);
        return pipelineUserRelE;
    }

    @Override
    public PipelineUserRelDO entityToDo(PipelineUserRelE pipelineUserRelE) {
        PipelineUserRelDO pipelineUserRelDO = new PipelineUserRelDO();
        BeanUtils.copyProperties(pipelineUserRelE, pipelineUserRelDO);
        return pipelineUserRelDO;
    }

    @Override
    public PipelineUserRelE doToEntity(PipelineUserRelDO pipelineUserRelDO) {
        PipelineUserRelE pipelineUserRelE = new PipelineUserRelE();
        BeanUtils.copyProperties(pipelineUserRelDO, pipelineUserRelE);
        return pipelineUserRelE;
    }
}
