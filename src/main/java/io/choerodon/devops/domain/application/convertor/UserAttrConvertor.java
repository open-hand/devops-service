package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.infra.dataobject.UserAttrDO;

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class UserAttrConvertor implements ConvertorI<UserAttrE, UserAttrDO, Object> {

    @Override
    public UserAttrE doToEntity(UserAttrDO userAttrDO) {
        UserAttrE userAttrE = new UserAttrE();
        BeanUtils.copyProperties(userAttrDO, userAttrE);
        return userAttrE;
    }

    @Override
    public UserAttrDO entityToDo(UserAttrE userAttrE) {
        UserAttrDO userAttrDO = new UserAttrDO();
        BeanUtils.copyProperties(userAttrE, userAttrDO);
        return userAttrDO;
    }
}
