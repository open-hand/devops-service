package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsDeployValueDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsDeployValueE;
import io.choerodon.devops.infra.dto.DevopsDeployValueDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:45 2019/4/10
 * Description:
 */
@Component
public class DevopsDeployValueConvertor implements ConvertorI<DevopsDeployValueE, DevopsDeployValueDO, DevopsDeployValueDTO> {
    @Override
    public DevopsDeployValueE doToEntity(DevopsDeployValueDO pipelineValueDO) {
        DevopsDeployValueE pipelineValueE = new DevopsDeployValueE();
        BeanUtils.copyProperties(pipelineValueDO, pipelineValueE);
        return pipelineValueE;
    }

    @Override
    public DevopsDeployValueDO entityToDo(DevopsDeployValueE pipelineValueE) {
        DevopsDeployValueDO pipelineValueDO = new DevopsDeployValueDO();
        BeanUtils.copyProperties(pipelineValueE, pipelineValueDO);
        return pipelineValueDO;
    }


    @Override
    public DevopsDeployValueE dtoToEntity(DevopsDeployValueDTO pipelineValueDTO) {
        DevopsDeployValueE pipelineValueE = new DevopsDeployValueE();
        BeanUtils.copyProperties(pipelineValueDTO, pipelineValueE);
        return pipelineValueE;
    }

    @Override
    public DevopsDeployValueDTO entityToDto(DevopsDeployValueE pipelineValueE) {
        DevopsDeployValueDTO pipelineValueDTO = new DevopsDeployValueDTO();
        BeanUtils.copyProperties(pipelineValueE, pipelineValueDTO);
        return pipelineValueDTO;
    }
}
