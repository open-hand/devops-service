package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/OrganizationConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/OrganizationConvertor.java
>>>>>>> [IMP]重构后端结构
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/OrganizationConvertor.java

/**
 * Created by younger on 2018/4/3.
 */
@Component
public class OrganizationConvertor implements ConvertorI<OrganizationVO, OrganizationDTO, Object> {

    @Override
    public OrganizationVO doToEntity(OrganizationDTO organizationDTO) {
        OrganizationVO organization = new OrganizationVO();
        BeanUtils.copyProperties(organizationDTO, organization);
        return organization;
    }

}
