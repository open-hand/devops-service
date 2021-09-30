package io.choerodon.devops.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.AppExternalConfigDTO;
import io.choerodon.devops.infra.enums.ExternalAppAuthTypeEnum;
import io.choerodon.devops.infra.mapper.AppExternalConfigMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
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
    private static final String ERROR_UPDATE_APP_CONFIG_FAILED = "error.update.app.config.failed";

    @Autowired
    private AppExternalConfigMapper appExternalConfigMapper;

    @Override
    public AppExternalConfigDTO baseQuery(Long id) {
        return appExternalConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void baseSave(AppExternalConfigDTO appExternalConfigDTO) {
        handlerAppExternalConfigDTO(appExternalConfigDTO);
        MapperUtil.resultJudgedInsertSelective(appExternalConfigMapper, appExternalConfigDTO, ERROR_SAVE_APP_CONFIG_FAILED);

    }

    @Override
    @Transactional
    public void update(Long projectId, Long id, AppExternalConfigDTO appExternalConfigDTO) {
        AppExternalConfigDTO appExternalConfigDTORecord = appExternalConfigMapper.selectByPrimaryKey(id);
        CommonExAssertUtil.assertTrue(projectId.equals(appExternalConfigDTORecord.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        appExternalConfigDTO.setId(id);
        appExternalConfigDTO.setProjectId(projectId);

        handlerAppExternalConfigDTO(appExternalConfigDTO);
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(appExternalConfigMapper, appExternalConfigDTO, ERROR_UPDATE_APP_CONFIG_FAILED);
    }

    @Override
    @Transactional
    public void baseDelete(Long externalConfigId) {
        appExternalConfigMapper.deleteByPrimaryKey(externalConfigId);
    }

    protected void handlerAppExternalConfigDTO (AppExternalConfigDTO appExternalConfigDTO) {
        if (ExternalAppAuthTypeEnum.ACCESS_TOKEN.getValue().equals(appExternalConfigDTO.getAuthType())) {
            appExternalConfigDTO.setUsername(null);
            appExternalConfigDTO.setPassword(null);
        } else if (ExternalAppAuthTypeEnum.USERNAME_PASSWORD.getValue().equals(appExternalConfigDTO.getAuthType())) {
            appExternalConfigDTO.setAccessToken(null);
        } else {
            throw new CommonException(ERROR_INVALID_APP_AUTH_TYPE);
        }
    }
}
