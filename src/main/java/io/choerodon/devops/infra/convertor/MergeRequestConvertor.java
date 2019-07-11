package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.AuthorDTO;
import io.choerodon.devops.api.vo.MergeRequestDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.MergeRequestE;
import io.choerodon.devops.infra.dto.gitlab.MergeRequestDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class MergeRequestConvertor implements ConvertorI<MergeRequestE, MergeRequestDO, MergeRequestDTO> {

    @Override
    public MergeRequestDTO doToDto(MergeRequestDO dataObject) {
        MergeRequestDTO mergeRequestDTO = new MergeRequestDTO();
        BeanUtils.copyProperties(dataObject, mergeRequestDTO);
        mergeRequestDTO.setAuthor(ConvertHelper.convert(dataObject.getAuthor(), AuthorDTO.class));
        return mergeRequestDTO;
    }
}
