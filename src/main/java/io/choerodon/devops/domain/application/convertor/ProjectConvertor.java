package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class ProjectConvertor implements ConvertorI<ProjectE, ProjectDO, Object> {

    @Override
    public ProjectE doToEntity(ProjectDO projectDO) {
        ProjectE projectE = new ProjectE();
        BeanUtils.copyProperties(projectDO, projectE);
        projectE.initOrganization(projectDO.getOrganizationId());
        return projectE;
    }

    @Override
    public ProjectDO entityToDo(ProjectE projectE) {
        ProjectDO projectDO = new ProjectDO();
        BeanUtils.copyProperties(projectE, projectDO);
        projectDO.setOrganizationId(projectE.getOrganization().getId());
        return projectDO;
    }
}
