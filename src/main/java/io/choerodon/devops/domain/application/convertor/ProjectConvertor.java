package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class ProjectConvertor implements ConvertorI<ProjectVO, ProjectDO, Object> {

    @Override
    public ProjectVO doToEntity(ProjectDO projectDO) {
        ProjectVO projectE = new ProjectVO();
        BeanUtils.copyProperties(projectDO, projectE);
        projectE.initOrganization(projectDO.getOrganizationId());
        return projectE;
    }

    @Override
    public ProjectDO entityToDo(ProjectVO projectE) {
        ProjectDO projectDO = new ProjectDO();
        BeanUtils.copyProperties(projectE, projectDO);
        projectDO.setOrganizationId(projectE.getOrganization().getId());
        return projectDO;
    }
}
