package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.AppUserPermissionRepDTO;
import io.choerodon.devops.api.vo.iam.entity.AppUserPermissionE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/AppUserPermissionConvertor.java
import io.choerodon.devops.infra.dto.AppUserPermissionDO;
=======
import io.choerodon.devops.infra.dataobject.AppUserPermissionDTO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/AppUserPermissionConvertor.java
=======
import io.choerodon.devops.infra.dto.ApplicationUserPermissionDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

>>>>>>> [IMP]重构后端代码

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:21
 * Description:
 */
@Component
public class AppUserPermissionConvertor implements ConvertorI<AppUserPermissionE, ApplicationUserPermissionDTO, AppUserPermissionRepDTO> {

    @Override
    public AppUserPermissionE doToEntity(ApplicationUserPermissionDTO applicationUserPermissionDTO) {
        AppUserPermissionE appUserPermissionE = new AppUserPermissionE();
        BeanUtils.copyProperties(applicationUserPermissionDTO, appUserPermissionE);
        return appUserPermissionE;
    }

    @Override
    public AppUserPermissionRepDTO entityToDto(AppUserPermissionE appUserPermissionE) {
        AppUserPermissionRepDTO appUserPermissionRepDTO = new AppUserPermissionRepDTO();
        BeanUtils.copyProperties(appUserPermissionE, appUserPermissionRepDTO);
        return appUserPermissionRepDTO;
    }
}
