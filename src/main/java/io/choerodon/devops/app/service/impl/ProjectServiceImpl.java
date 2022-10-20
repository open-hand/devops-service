package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.DEVOPS_GITLAB_GROUP_ID_SELECT;
import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.DEVOPS_GROUP_NOT_SYNC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.ProjectService;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.mapper.DevopsProjectMapper;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/2
 * Time: 11:00
 * Description:
 */
@Component
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private DevopsProjectMapper devopsProjectMapper;

    @Override
    public DevopsProjectDTO queryById(Long projectId) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectMapper.selectByPrimaryKey(projectId);
        if (devopsProjectDTO == null) {
            throw new CommonException(DEVOPS_GROUP_NOT_SYNC);
        }
        if (devopsProjectDTO.getDevopsAppGroupId() == null || devopsProjectDTO.getDevopsEnvGroupId() == null) {
            throw new CommonException(DEVOPS_GITLAB_GROUP_ID_SELECT);
        }
        return devopsProjectDTO;
    }
}
