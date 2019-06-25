package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.valueobject.ProjectCategoryEDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:17 2019/6/24
 * Description:
 */
public interface OrgRepository {
    ProjectCategoryEDTO createProjectCategory(Long organizationId, ProjectCategoryEDTO createDTO);
}
