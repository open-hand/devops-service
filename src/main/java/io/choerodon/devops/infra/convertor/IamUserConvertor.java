package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.IamUserDTO;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/IamUserConvertor.java
import io.choerodon.devops.infra.dto.iam.UserDO;
=======
import io.choerodon.devops.infra.dataobject.iam.UserDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/IamUserConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/IamUserConvertor.java
import io.choerodon.devops.infra.dataobject.iam.UserDO;
=======
import io.choerodon.devops.infra.dto.iam.UserDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/IamUserConvertor.java
>>>>>>> [IMP]重构后端结构
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * Created by Zenger on 2018/4/16.
 */
@Service
public class IamUserConvertor implements ConvertorI<UserE, UserDTO, IamUserDTO> {

    @Override
    public UserE doToEntity(UserDTO dataObject) {
        UserE userE = new UserE();
        BeanUtils.copyProperties(dataObject, userE);
        return userE;
    }

    @Override
    public IamUserDTO entityToDto(UserE userE) {
        IamUserDTO userDTO = new IamUserDTO();
        BeanUtils.copyProperties(userE, userDTO);
        return userDTO;
    }
}
