package io.choerodon.devops.api.validator;

import static io.choerodon.devops.infra.constant.ExceptionConstants.AppServiceCode.DEVOPS_APP_ID_NOT_EXIST;
import static io.choerodon.devops.infra.constant.ExceptionConstants.EnvironmentCode.DEVOPS_ENV_ID_NOT_EXIST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.infra.dto.DevopsEnvAppServiceDTO;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvAppServiceMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;

/**
 * @author zmf
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EnvironmentApplicationValidator {
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;

    @Autowired
    private PermissionHelper permissionHelper;

    @Autowired
    private AppServiceMapper appServiceMapper;

    @Autowired
    private DevopsEnvAppServiceMapper devopsEnvAppServiceMapper;


    /**
     * 校验环境id存在
     *
     * @param envId 环境id
     */
    public void checkEnvIdExist(Long envId) {
        if (envId == null) {
            throw new CommonException("devops.env.id.null");
        }

        if (devopsEnvironmentMapper.selectByPrimaryKey(envId) == null) {
            throw new CommonException(DEVOPS_ENV_ID_NOT_EXIST, envId);
        }
    }


    /**
     * 校验应用id不为空且存在
     *
     * @param appServiceIds 应用id
     */
    public void checkAppIdsExist(List<Long> appServiceIds) {
        if (appServiceIds == null || appServiceIds.size() == 0) {
            throw new CommonException("devops.app.ids.null");
        }

        appServiceIds.forEach(id -> {
            if (appServiceMapper.selectByPrimaryKey(id) == null) {
                throw new CommonException(DEVOPS_APP_ID_NOT_EXIST, id);
            }
        });
    }

    /**
     * 校验环境id和应用id存在关联
     */
    public void checkEnvIdAndAppIdsExist(Long projectId, Long envId, Long appServiceId) {
        if (envId == null) {
            throw new CommonException("devops.env.id.null");
        }
        if (appServiceId == null) {
            throw new CommonException("devops.app.id.null");
        }
        permissionHelper.checkEnvBelongToProject(projectId, envId);
        DevopsEnvAppServiceDTO devopsEnvAppServiceDTO = new DevopsEnvAppServiceDTO(appServiceId, envId);
        if (devopsEnvAppServiceMapper.selectOne(devopsEnvAppServiceDTO) == null) {
            throw new CommonException("devops.envAndApp.not.exist", envId, appServiceId);
        }
    }
}
