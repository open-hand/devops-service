package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.PipelineAppDeployDTO;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployValueE;
import io.choerodon.devops.infra.dataobject.PipelineAppDeployValueDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:02 2019/4/8
 * Description:
 */
@Component
public class PipelineAppDeployValueConvertor implements ConvertorI<PipelineAppDeployValueE, PipelineAppDeployValueDO, PipelineAppDeployDTO> {
    @Override
    public PipelineAppDeployValueE dtoToEntity(PipelineAppDeployDTO pipelineAppDeployDTO) {
        PipelineAppDeployValueE appDeployValueE = new PipelineAppDeployValueE();
        BeanUtils.copyProperties(pipelineAppDeployDTO, appDeployValueE);
        appDeployValueE.setId(pipelineAppDeployDTO.getValueId());
        return appDeployValueE;
    }

    @Override
    public PipelineAppDeployValueDO entityToDo(PipelineAppDeployValueE appDeployValueE) {
        PipelineAppDeployValueDO pipelineAppDeployValueDO = new PipelineAppDeployValueDO();
        BeanUtils.copyProperties(appDeployValueE, pipelineAppDeployValueDO);
        return pipelineAppDeployValueDO;
    }

    @Override
    public PipelineAppDeployValueE doToEntity(PipelineAppDeployValueDO appDeployValueDO) {
        PipelineAppDeployValueE appDeployValueE = new PipelineAppDeployValueE();
        BeanUtils.copyProperties(appDeployValueDO, appDeployValueE);
        return appDeployValueE;
    }
}
