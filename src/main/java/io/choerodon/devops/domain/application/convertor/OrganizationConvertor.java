package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO;

/**
 * Created by younger on 2018/4/3.
 */
@Component
public class OrganizationConvertor implements ConvertorI<Organization, OrganizationDO, Object> {

    @Override
    public Organization doToEntity(OrganizationDO organizationDO) {
        Organization organization = new Organization();
        BeanUtils.copyProperties(organizationDO, organization);
        return organization;
    }

}
