package io.choerodon.devops.infra.feign.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.valueobject.ProjectCategoryEDTO;
import io.choerodon.devops.infra.feign.OrgServiceClient;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:53 2019/6/24
 * Description:
 */
@Component
public class OrgServiceClientFallBack implements OrgServiceClient {
    @Override
    public ResponseEntity<ProjectCategoryEDTO> createProjectCategory(Long organizationId, ProjectCategoryEDTO createDTO) {
        return new ResponseEntity("error.project.category.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
