package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;

@Component
public class DevopsMergeRequestConvertor implements ConvertorI<DevopsMergeRequestE, DevopsMergeRequestDO, DevopsMergeRequestDTO> {

    @Override
    public DevopsMergeRequestDTO entityToDto(DevopsMergeRequestE entity) {
        DevopsMergeRequestDTO devopsMergeRequestDTO = new DevopsMergeRequestDTO();
        BeanUtils.copyProperties(entity, devopsMergeRequestDTO);
        return devopsMergeRequestDTO;
    }

    @Override
    public DevopsMergeRequestDO entityToDo(DevopsMergeRequestE entity) {
        DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO();
        BeanUtils.copyProperties(entity, devopsMergeRequestDO);
        return devopsMergeRequestDO;
    }
}
