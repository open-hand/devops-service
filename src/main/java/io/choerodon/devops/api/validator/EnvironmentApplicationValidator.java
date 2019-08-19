package io.choerodon.devops.api.validator;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsEnvAppServiceVO;
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
            throw new CommonException("error.env.id.null");
        }

        if (devopsEnvironmentMapper.selectByPrimaryKey(envId) == null) {
            throw new CommonException("error.env.id.not.exist", envId);
        }
    }


    /**
     * 校验应用id不为空且存在
     *
     * @param appServiceIds 应用id
     */
    public void checkAppIdsExist(Long[] appServiceIds) {
        if (appServiceIds == null || appServiceIds.length == 0) {
            throw new CommonException("error.app.ids.null");
        }

        Stream.of(appServiceIds).forEach(id -> {
            if (appServiceMapper.selectByPrimaryKey(id) == null) {
                throw new CommonException("error.app.id.not.exist", id);
            }
        });
    }

    /**
     * 校验环境id和应用id存在关联
     */
    public void checkEnvIdAndAppIdsExist(Long envId, Long appServiceId) {
        if (envId == null) {
            throw new CommonException("error.env.id.null");
        }
        if (appServiceId == null) {
            throw new CommonException("error.app.id.null");
        }
        DevopsEnvAppServiceDTO devopsEnvAppServiceDTO = new DevopsEnvAppServiceDTO(appServiceId, envId);
        if (devopsEnvAppServiceMapper.selectOne(devopsEnvAppServiceDTO) == null) {
            throw new CommonException("error.envAndApp.not.exist", envId, appServiceId);
        }
    }
}
