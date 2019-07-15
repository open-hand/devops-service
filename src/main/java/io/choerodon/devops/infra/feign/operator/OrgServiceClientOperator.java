package io.choerodon.devops.infra.feign.operator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.valueobject.ProjectCategoryEDTO;
import io.choerodon.devops.infra.feign.OrgServiceClient;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:46 2019/7/15
 * Description:
 */
@Component
public class OrgServiceClientOperator {
    @Autowired
    private OrgServiceClient orgServiceClient;

    public ProjectCategoryEDTO baseCreate(Long organizationId, ProjectCategoryEDTO createDTO) {
        ResponseEntity<ProjectCategoryEDTO> simplifyDTOs = orgServiceClient
                .createProjectCategory(organizationId, createDTO);
        return simplifyDTOs.getBody();
    }
}
