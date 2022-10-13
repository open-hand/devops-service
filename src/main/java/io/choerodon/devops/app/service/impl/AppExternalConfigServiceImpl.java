package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.AppExternalConfigServiceCode.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AppExternalConfigService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.AppExternalConfigDTO;
import io.choerodon.devops.infra.enums.ExternalAppAuthTypeEnum;
import io.choerodon.devops.infra.mapper.AppExternalConfigMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.DESEncryptUtil;
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

    @Autowired
    private AppExternalConfigMapper appExternalConfigMapper;

    @Override
    public AppExternalConfigDTO baseQueryWithPassword(Long id) {
        AppExternalConfigDTO appExternalConfigDTO = appExternalConfigMapper.selectByPrimaryKey(id);
        if (appExternalConfigDTO != null
                && ExternalAppAuthTypeEnum.USERNAME_PASSWORD.getValue().equals(appExternalConfigDTO.getAuthType())) {
            appExternalConfigDTO.setPassword(DESEncryptUtil.decode(appExternalConfigDTO.getPassword()));
        }
        return appExternalConfigDTO;
    }

    @Override
    public AppExternalConfigDTO baseQueryWithoutPasswordAndToken(Long id) {
        AppExternalConfigDTO appExternalConfigDTO = appExternalConfigMapper.selectByPrimaryKey(id);
        appExternalConfigDTO.setPassword(null);
        appExternalConfigDTO.setAccessToken(null);
        return appExternalConfigDTO;
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
        MapperUtil.resultJudgedUpdateByPrimaryKey(appExternalConfigMapper, appExternalConfigDTO, ERROR_UPDATE_APP_CONFIG_FAILED);
    }

    @Override
    @Transactional
    public void baseDelete(Long externalConfigId) {
        appExternalConfigMapper.deleteByPrimaryKey(externalConfigId);
    }

    @Override
    public boolean checkRepositoryUrlUnique(String repositoryUrl) {
        AppExternalConfigDTO appExternalConfigDTO = new AppExternalConfigDTO();
        appExternalConfigDTO.setRepositoryUrl(repositoryUrl);
        return appExternalConfigMapper.selectCount(appExternalConfigDTO) < 1;
    }

    protected void handlerAppExternalConfigDTO (AppExternalConfigDTO appExternalConfigDTO) {
        if (ExternalAppAuthTypeEnum.ACCESS_TOKEN.getValue().equals(appExternalConfigDTO.getAuthType())) {
            appExternalConfigDTO.setUsername(null);
            appExternalConfigDTO.setPassword(null);
        } else if (ExternalAppAuthTypeEnum.USERNAME_PASSWORD.getValue().equals(appExternalConfigDTO.getAuthType())) {
            appExternalConfigDTO.setAccessToken(null);
            appExternalConfigDTO.setPassword(DESEncryptUtil.encode(appExternalConfigDTO.getPassword()));
        } else {
            throw new CommonException(ERROR_INVALID_APP_AUTH_TYPE);
        }
    }
}
