package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.gitlab.BranchE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/BranchConvertor.java
import io.choerodon.devops.infra.dto.gitlab.BranchDO;
=======
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/BranchConvertor.java
=======
import io.choerodon.devops.infra.dto.gitlab.BranchDTO;
>>>>>>> [IMP]修改后端结构

/**
 * Created by Zenger on 2018/4/14.
 */
@Component
public class BranchConvertor implements ConvertorI<BranchE, BranchDTO, Object> {

    @Override
    public BranchE doToEntity(BranchDTO dataObject) {
        BranchE branchE = new BranchE();
        BeanUtils.copyProperties(dataObject, branchE);
        return branchE;
    }
}
