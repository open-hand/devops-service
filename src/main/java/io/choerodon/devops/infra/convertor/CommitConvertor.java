package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/CommitConvertor.java
import io.choerodon.devops.api.vo.CommitDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.CommitE;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO;
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.infra.dto.gitlab.CommitDO;
=======
import io.choerodon.devops.api.vo.CommitVO;
<<<<<<< HEAD
import io.choerodon.devops.api.vo.iam.entity.gitlab.CommitE;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDTO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/CommitConvertor.java
=======
>>>>>>> [IMP]修改后端结构

@Component
public class CommitConvertor implements ConvertorI<CommitDTO, io.choerodon.devops.infra.dataobject.gitlab.CommitDTO, CommitVO> {

    @Override
    public CommitVO doToDto(io.choerodon.devops.infra.dataobject.gitlab.CommitDTO dataObject) {
        CommitVO commitDTO = new CommitVO();
        BeanUtils.copyProperties(dataObject, commitDTO);
        return commitDTO;
    }
}
