package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.devops.domain.application.repository.OrgRepository;
import io.choerodon.devops.domain.application.valueobject.ProjectCategoryEDTO;
import io.choerodon.devops.infra.feign.OrgServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:18 2019/6/24
 * Description:
 */
@Component
public class OrgRepositoryImpl implements OrgRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrgRepositoryImpl.class);

    private OrgServiceClient orgServiceClient;

    public OrgRepositoryImpl(OrgServiceClient orgServiceClient) {
        this.orgServiceClient = orgServiceClient;
    }

    @Override
    public ProjectCategoryEDTO createProjectCategory(Long organizationId, ProjectCategoryEDTO createDTO) {
            ResponseEntity<ProjectCategoryEDTO> simplifyDTOs = orgServiceClient
                    .createProjectCategory(organizationId, createDTO);
            return simplifyDTOs.getBody();
    }
}
