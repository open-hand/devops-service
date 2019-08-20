package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.ProjectPayload;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/2
 * Time: 10:59
 * Description:
 */
public interface ProjectService {
    /**
     * 查询项目在gitlab中组是否创建
     *
     * @param projectId 项目Id
     * @return gitlab group Ready
     */
    boolean queryProjectGitlabGroupReady(Long projectId);


    /**
     * 根据Id查询project
     *
     * @param projectId
     * @return
     */
    DevopsProjectDTO queryById(Long projectId);

}
