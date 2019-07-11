package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineAppDeployDTO;
import io.choerodon.devops.domain.application.entity.PipelineAppDeployE;
import io.choerodon.devops.infra.dataobject.PipelineAppDeployDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:31 2019/4/8
 * Description:
 */
@Component
public class PipelineAppDeployConvertor implements ConvertorI<PipelineAppDeployE, PipelineAppDeployDO, PipelineAppDeployDTO> {

    @Override
    public PipelineAppDeployE dtoToEntity(PipelineAppDeployDTO pipelineAppDeployDTO) {
        PipelineAppDeployE pipelineAppDeployE = new PipelineAppDeployE();
        BeanUtils.copyProperties(pipelineAppDeployDTO, pipelineAppDeployE);
        if (pipelineAppDeployDTO.getTriggerVersion() != null) {
            pipelineAppDeployE.setTriggerVersion(pipelineAppDeployDTO.getTriggerVersion().stream().collect(Collectors.joining(",")));
        }
        return pipelineAppDeployE;
    }

    @Override
    public PipelineAppDeployDTO entityToDto(PipelineAppDeployE pipelineAppDeployE) {
        PipelineAppDeployDTO pipelineAppDeployDTO = new PipelineAppDeployDTO();
        BeanUtils.copyProperties(pipelineAppDeployE, pipelineAppDeployDTO);
        if (pipelineAppDeployE.getTriggerVersion() != null && !pipelineAppDeployE.getTriggerVersion().isEmpty()) {
            pipelineAppDeployDTO.setTriggerVersion(Arrays.asList(pipelineAppDeployE.getTriggerVersion().split(",")));
        } else {
            pipelineAppDeployDTO.setTriggerVersion(new ArrayList<>());
        }
        return pipelineAppDeployDTO;
    }


    @Override
    public PipelineAppDeployDO entityToDo(PipelineAppDeployE pipelineAppDeployE) {
        PipelineAppDeployDO pipelineAppDeployDO = new PipelineAppDeployDO();
        BeanUtils.copyProperties(pipelineAppDeployE, pipelineAppDeployDO);
        return pipelineAppDeployDO;
    }

    @Override
    public PipelineAppDeployE doToEntity(PipelineAppDeployDO pipelineAppDeployDO) {
        PipelineAppDeployE pipelineAppDeployE = new PipelineAppDeployE();
        BeanUtils.copyProperties(pipelineAppDeployDO, pipelineAppDeployE);
        return pipelineAppDeployE;
    }
}
