package io.choerodon.devops.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.AppExternalConfigDTO;
import io.choerodon.devops.infra.enums.ExternalAppAuthTypeEnum;
import io.choerodon.devops.infra.mapper.AppExternalConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/28 10:57
 */
@Service
public class AppExternalConfigServiceImpl implements AppExternalConfigService {

    private static final String ERROR_INVALID_APP_AUTH_TYPE = "error.invalid.app.auth.type";
    private static final String ERROR_SAVE_APP_CONFIG_FAILED = "error.save.app.config.failed";

    @Autowired
    private AppExternalConfigMapper appExternalConfigMapper;

    @Override
    public AppExternalConfigDTO queryByAppSeviceId(Long appServiceId) {
        AppExternalConfigDTO appExternalConfigDTO = new AppExternalConfigDTO();
        appExternalConfigDTO.setAppServiceId(appServiceId);
        return appExternalConfigMapper.selectOne(appExternalConfigDTO);
    }

    @Override
    @Transactional
    public void baseSave(AppExternalConfigDTO appExternalConfigDTO) {
        if (ExternalAppAuthTypeEnum.ACCESS_TOKEN.getValue().equals(appExternalConfigDTO.getAuthType())) {
            appExternalConfigDTO.setUsername(null);
            appExternalConfigDTO.setPassword(null);
        } else if (ExternalAppAuthTypeEnum.USERNAME_PASSWORD.getValue().equals(appExternalConfigDTO.getAuthType())) {
            appExternalConfigDTO.setAccessToken(null);
        } else {
            throw new CommonException(ERROR_INVALID_APP_AUTH_TYPE);
        }
        MapperUtil.resultJudgedInsertSelective(appExternalConfigMapper, appExternalConfigDTO, ERROR_SAVE_APP_CONFIG_FAILED);

    }
}
