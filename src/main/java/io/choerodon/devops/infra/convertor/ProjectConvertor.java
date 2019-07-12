package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/ProjectConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ProjectVO;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/ProjectConvertor.java
>>>>>>> [IMP]重构后端结构
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;

>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/ProjectConvertor.java
/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class ProjectConvertor implements ConvertorI<ProjectVO, ProjectDTO, Object> {

    @Override
    public ProjectVO doToEntity(ProjectDTO projectDTO) {
        ProjectVO projectE = new ProjectVO();
        BeanUtils.copyProperties(projectDTO, projectE);
        projectE.initOrganization(projectDTO.getOrganizationId());
        return projectE;
    }

    @Override
    public ProjectDTO entityToDo(ProjectVO projectE) {
        ProjectDTO projectDTO = new ProjectDTO();
        BeanUtils.copyProperties(projectE, projectDTO);
        projectDTO.setOrganizationId(projectE.getOrganization().getId());
        return projectDTO;
    }
}
