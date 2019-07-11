package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.infra.dto.iam.ProjectDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/ProjectConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;

>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/ProjectConvertor.java
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
