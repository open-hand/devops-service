package io.choerodon.devops.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.valueobject.ProjectCategoryEDTO;
import io.choerodon.devops.infra.feign.OrgServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:53 2019/6/24
 * Description:
 */
@Component
public class OrgServiceClientFallBack implements OrgServiceClient {
    @Override
    public ResponseEntity<ProjectCategoryEDTO> createProjectCategory(Long organizationId, ProjectCategoryEDTO createDTO) {
        throw new CommonException("error.project.category.create");
    }
}
