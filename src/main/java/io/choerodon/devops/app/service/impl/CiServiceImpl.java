package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ConfigFileVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.CiService;
import io.choerodon.devops.app.service.ConfigFileService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/2/15 10:35
 */
@Service
public class CiServiceImpl implements CiService {
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private ConfigFileService configFileService;

    @Override
    public String queryConfigFileById(String token, Long configFileId) {
        try {
            AppServiceDTO appServiceDTO = appServiceService.queryByTokenOrThrowE(token);

            ConfigFileVO configFileVO = configFileService.queryByIdWithDetail(configFileId);
            if (ResourceLevel.PROJECT.value().equals(configFileVO.getSourceType()) && !configFileVO.getSourceId().equals(appServiceDTO.getProjectId())) {
                throw new CommonException(MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_TEAM);
            }
            return configFileVO.getMessage();
        } catch (CommonException e) {
            throw new DevopsCiInvalidException(e.getCode(), e, e.getParameters());
        }
    }
}
