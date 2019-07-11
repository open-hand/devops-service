package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.gitlab.BranchE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/BranchConvertor.java
import io.choerodon.devops.infra.dto.gitlab.BranchDO;
=======
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/BranchConvertor.java

/**
 * Created by Zenger on 2018/4/14.
 */
@Component
public class BranchConvertor implements ConvertorI<BranchE, BranchDO, Object> {

    @Override
    public BranchE doToEntity(BranchDO dataObject) {
        BranchE branchE = new BranchE();
        BeanUtils.copyProperties(dataObject, branchE);
        return branchE;
    }
}
