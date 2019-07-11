package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/UserAttrConvertor.java
<<<<<<< HEAD
=======
<<<<<<< HEAD
import io.choerodon.devops.api.vo.UserAttrVO;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.domain.application.entity.UserAttrE;
=======
>>>>>>> 99504a39d606d3005354e0b1bdcb50530cde6afd
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/UserAttrConvertor.java
=======
import io.choerodon.devops.api.vo.UserAttrVO;
>>>>>>> [IMP]修复后端结构:src/main/java/io/choerodon/devops/infra/convertor/UserAttrConvertor.java
import io.choerodon.devops.api.vo.iam.entity.UserAttrE;
>>>>>>> [IMP] 修改AppControler重构
=======
import io.choerodon.devops.api.vo.iam.entity.UserAttrE;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/UserAttrConvertor.java
import io.choerodon.devops.infra.dataobject.UserAttrDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class UserAttrConvertor implements ConvertorI<UserAttrE, UserAttrDTO, UserAttrVO> {

    @Override
    public UserAttrE doToEntity(UserAttrDTO userAttrDO) {
        UserAttrE userAttrE = new UserAttrE();
        BeanUtils.copyProperties(userAttrDO, userAttrE);
        return userAttrE;
    }

    @Override
    public UserAttrDTO entityToDo(UserAttrE userAttrE) {
        UserAttrDTO userAttrDO = new UserAttrDTO();
        BeanUtils.copyProperties(userAttrE, userAttrDO);
        return userAttrDO;
    }

    @Override
    public UserAttrVO entityToDto(UserAttrE userAttrE) {
        UserAttrVO userAttrDTO = new UserAttrVO();
        BeanUtils.copyProperties(userAttrE, userAttrDTO);
        return userAttrDTO;
    }
}
