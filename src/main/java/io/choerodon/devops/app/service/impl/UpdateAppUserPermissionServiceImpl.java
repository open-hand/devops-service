package io.choerodon.devops.app.service.impl;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 16:08
 * Description:
 */
public class UpdateAppUserPermissionServiceImpl extends UpdateUserPermissionService {

    private BaseServiceClientOperator baseServiceClientOperator;
    private UserAttrService userAttrService;

    public UpdateAppUserPermissionServiceImpl() {
        this.baseServiceClientOperator = ApplicationContextHelper.getSpringFactory().getBean(BaseServiceClientOperator.class);
        this.userAttrService = ApplicationContextHelper.getSpringFactory().getBean(UserAttrService.class);
    }
}
