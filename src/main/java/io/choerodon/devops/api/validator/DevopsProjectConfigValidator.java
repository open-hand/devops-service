package io.choerodon.devops.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsProjectConfigDTO;
import io.choerodon.devops.infra.common.util.enums.ProjectConfigType;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/12
 */
public class DevopsProjectConfigValidator {
    public static void checkConfigType(DevopsProjectConfigDTO configDTO) {
        ProjectConfigType type = ProjectConfigType.valueOf(configDTO.getType().toUpperCase());
        switch (type) {
            case HARBOR:
                break;
            case CHART:
                break;
            default:
                throw new CommonException("error.projectConfig.type");
        }
    }
}
