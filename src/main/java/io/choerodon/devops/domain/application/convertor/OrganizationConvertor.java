package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO;

/**
 * Created by younger on 2018/4/3.
 */
@Component
public class OrganizationConvertor implements ConvertorI<OrganizationVO, OrganizationDO, Object> {

    @Override
    public OrganizationVO doToEntity(OrganizationDO organizationDO) {
        OrganizationVO organization = new OrganizationVO();
        BeanUtils.copyProperties(organizationDO, organization);
        return organization;
    }

}
