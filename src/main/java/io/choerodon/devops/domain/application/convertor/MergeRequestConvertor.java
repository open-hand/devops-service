package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.MergeRequestDTO;
import io.choerodon.devops.domain.application.entity.gitlab.MergeRequestE;
import io.choerodon.devops.infra.dataobject.gitlab.MergeRequestDO;

@Component
public class MergeRequestConvertor implements ConvertorI<MergeRequestE, MergeRequestDO, MergeRequestDTO> {

    @Override
    public MergeRequestDTO doToDto(MergeRequestDO dataObject) {
        MergeRequestDTO mergeRequestDTO = new MergeRequestDTO();
        BeanUtils.copyProperties(dataObject, mergeRequestDTO);
        return mergeRequestDTO;
    }
}
