package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.AppUserPermissionRepDTO;
import io.choerodon.devops.api.vo.iam.entity.AppUserPermissionE;
import io.choerodon.devops.infra.dataobject.AppUserPermissionDO;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:21
 * Description:
 */
@Component
public class AppUserPermissionConvertor implements ConvertorI<AppUserPermissionE, AppUserPermissionDO, AppUserPermissionRepDTO> {

    @Override
    public AppUserPermissionE doToEntity(AppUserPermissionDO appUserPermissionDO) {
        AppUserPermissionE appUserPermissionE = new AppUserPermissionE();
        BeanUtils.copyProperties(appUserPermissionDO, appUserPermissionE);
        return appUserPermissionE;
    }

    @Override
    public AppUserPermissionRepDTO entityToDto(AppUserPermissionE appUserPermissionE) {
        AppUserPermissionRepDTO appUserPermissionRepDTO = new AppUserPermissionRepDTO();
        BeanUtils.copyProperties(appUserPermissionE, appUserPermissionRepDTO);
        return appUserPermissionRepDTO;
    }
}
