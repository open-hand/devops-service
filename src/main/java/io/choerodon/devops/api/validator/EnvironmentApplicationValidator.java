package io.choerodon.devops.api.validator;

import java.util.stream.Stream;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.mapper.ApplicationMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author zmf
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EnvironmentApplicationValidator {
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;

    @Autowired
    private ApplicationMapper applicationMapper;


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
            throw new CommonException("error.env.id.not.exist");
        }
    }


    /**
     * 校验应用id不为空且存在
     *
     * @param appIds 应用id
     */
    public void checkAppIdsExist(Long[] appIds) {
        if (appIds == null || appIds.length == 0) {
            throw new CommonException("error.app.ids.null");
        }

        Stream.of(appIds).forEach(id -> {
            if (applicationMapper.selectByPrimaryKey(id) == null) {
                throw new CommonException("error.app.id.not.exist", id);
            }
        });
    }
}
