package io.choerodon.devops.domain.application.convertor;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.AppInstanceCodeDTO;
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE;

/**
 * Created by Zenger on 2018/4/18.
 */
@Component
public class AppInstanceCodeConvertor implements ConvertorI<ApplicationInstanceE, Object, AppInstanceCodeDTO> {

    @Override
    public AppInstanceCodeDTO entityToDto(ApplicationInstanceE entity) {
        AppInstanceCodeDTO appInstanceCodeDTO = new AppInstanceCodeDTO();
        appInstanceCodeDTO.setId(entity.getId().toString());
        appInstanceCodeDTO.setCode(entity.getCode());
        appInstanceCodeDTO.setIsEnabled(entity.getIsEnabled());
        if (entity.getApplicationVersionE() != null) {
            appInstanceCodeDTO.setAppVersion(entity.getApplicationVersionE().getVersion());
        }
        return appInstanceCodeDTO;
    }
}
