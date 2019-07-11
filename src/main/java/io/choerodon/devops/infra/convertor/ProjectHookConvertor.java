package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.ProjectHookE;
import io.choerodon.devops.infra.dto.gitlab.ProjectHookDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/ProjectHookConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.ProjectHookE;
import io.choerodon.devops.infra.dataobject.gitlab.ProjectHookDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/ProjectHookConvertor.java

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
