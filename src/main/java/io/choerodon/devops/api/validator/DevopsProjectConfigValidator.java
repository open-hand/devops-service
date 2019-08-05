package io.choerodon.devops.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsProjectConfigVO;
import io.choerodon.devops.api.vo.ProjectConfigVO;
import io.choerodon.devops.app.service.ApplicationSevriceService;
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
    ApplicationSevriceService applicationService;

    public void checkConfigType(DevopsProjectConfigVO devopsProjectConfigVO) {
        ProjectConfigType type = ProjectConfigType.valueOf(devopsProjectConfigVO.getType().toUpperCase());
        ProjectConfigVO configDTO = devopsProjectConfigVO.getConfig();
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
