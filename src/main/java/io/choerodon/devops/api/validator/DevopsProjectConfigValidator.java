package io.choerodon.devops.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.infra.enums.ProjectConfigType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/12
 */
@Component
@Scope("singleton")
public class DevopsProjectConfigValidator {

    @Autowired
    AppServiceService applicationService;

    public void checkConfigType(DevopsConfigVO devopsConfigVO) {
        ProjectConfigType type = ProjectConfigType.valueOf(devopsConfigVO.getType().toUpperCase());
        ConfigVO configDTO = devopsConfigVO.getConfig();
        switch (type) {
            case HARBOR:
                applicationService.checkHarbor(configDTO.getUrl(),configDTO.getUserName(),configDTO.getPassword(),configDTO.getProject(),configDTO.getEmail());
                break;
            case CHART:
                applicationService.checkChart(configDTO.getUrl());
                break;
            default:
                throw new CommonException("error.project.config.type");
        }
    }
}
