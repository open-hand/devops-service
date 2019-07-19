package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.AuthorDTO;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/MergeRequestConvertor.java
import io.choerodon.devops.api.vo.MergeRequestDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.MergeRequestE;
import io.choerodon.devops.infra.dto.gitlab.MergeRequestDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
=======
import io.choerodon.devops.api.vo.iam.entity.gitlab.MergeRequestE;
import io.choerodon.devops.infra.dataobject.gitlab.MergeRequestDTO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/MergeRequestConvertor.java
=======
=======
import io.choerodon.devops.api.vo.MergeRequestVO;
>>>>>>> [IMP]修改后端结构
import io.choerodon.devops.api.vo.iam.entity.gitlab.MergeRequestE;
import io.choerodon.devops.infra.dataobject.gitlab.MergeRequestDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

>>>>>>> [IMP]修改后端结构

@Component
public class MergeRequestConvertor implements ConvertorI<MergeRequestE, MergeRequestDTO, MergeRequestVO> {

    @Override
    public MergeRequestVO doToDto(MergeRequestDTO dataObject) {
        MergeRequestVO mergeRequestVO = new MergeRequestVO();
        BeanUtils.copyProperties(dataObject, mergeRequestVO);
        mergeRequestVO.setAuthor(ConvertHelper.convert(dataObject.getAuthor(), AuthorDTO.class));
        return mergeRequestVO;
    }
}
