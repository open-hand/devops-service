package io.choerodon.devops.api.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsEnvGroupService;

/**
 * Creator: Runge
 * Date: 2018/9/4
 * Time: 15:15
 * Description:
 */
@Component
public class DevopsEnvGroupValidator {
    @Autowired
    private DevopsEnvGroupService devopsEnvGroupService;

    /**
     * check whether env group name exist or not
     *
     * @param id        env group id
     * @param name      env group name
     * @param projectId env project id
     */
    public void checkNameUnique(Long id, String name, Long projectId) {
        if (!devopsEnvGroupService.baseCheckUniqueInProject(id, name, projectId)) {
            throw new CommonException("error.envGroupName.exist");
        }
    }
}
