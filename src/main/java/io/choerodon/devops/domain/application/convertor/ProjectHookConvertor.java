package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.ProjectHookE;
import io.choerodon.devops.infra.dataobject.gitlab.ProjectHookDO;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class ProjectHookConvertor implements ConvertorI<ProjectHookE, ProjectHookDO, Object> {

    @Override
    public ProjectHookDO entityToDo(ProjectHookE projectHookE) {
        ProjectHookDO projectHookDO = new ProjectHookDO();
        BeanUtils.copyProperties(projectHookE, projectHookDO);
        return projectHookDO;
    }

}
