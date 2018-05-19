package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.gitlab.BranchE;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;

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
