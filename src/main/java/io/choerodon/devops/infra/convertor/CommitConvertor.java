package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.CommitDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.CommitE;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO;
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.infra.dto.gitlab.CommitDO;

@Component
public class CommitConvertor implements ConvertorI<CommitE, CommitDO, CommitDTO> {

    @Override
    public CommitDTO doToDto(CommitDO dataObject) {
        CommitDTO commitDTO = new CommitDTO();
        BeanUtils.copyProperties(dataObject, commitDTO);
        return commitDTO;
    }
}
