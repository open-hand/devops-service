package io.choerodon.devops.app.service.impl;

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
            throw new CommonException("error.group.not.sync");
        }
        if (devopsProjectDTO.getDevopsAppGroupId() == null || devopsProjectDTO.getDevopsEnvGroupId() == null) {
            throw new CommonException("error.gitlab.groupId.select");
        }
        return devopsProjectDTO;
    }
}
