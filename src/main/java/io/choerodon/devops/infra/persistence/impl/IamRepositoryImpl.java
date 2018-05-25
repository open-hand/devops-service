package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO;
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;
import io.choerodon.devops.infra.dataobject.iam.UserDO;
import io.choerodon.devops.infra.feign.IamServiceClient;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class IamRepositoryImpl implements IamRepository {

    private IamServiceClient iamServiceClient;

    public IamRepositoryImpl(IamServiceClient iamServiceClient) {
        this.iamServiceClient = iamServiceClient;
    }

    @Override
    public ProjectE queryIamProject(Long projectId) {
        ResponseEntity<ProjectDO> projectDO = iamServiceClient.queryIamProject(projectId);
        if (!projectDO.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.project.get");
        }
        return ConvertHelper.convert(projectDO.getBody(), ProjectE.class);
    }

    @Override
    public Organization queryOrganization() {
        ResponseEntity<OrganizationDO> organization = iamServiceClient.queryOrganization();
        if (organization.getStatusCode().is2xxSuccessful()) {
            return ConvertHelper.convert(organization.getBody(), Organization.class);
        } else {
            throw new CommonException("error.organization.get");
        }
    }

    @Override
    public Organization queryOrganizationById(Long organizationId) {
        ResponseEntity<OrganizationDO> organization = iamServiceClient.queryOrganizationById(organizationId);
        if (organization.getStatusCode().is2xxSuccessful()) {
            return ConvertHelper.convert(organization.getBody(), Organization.class);
        } else {
            throw new CommonException("error.organization.get");
        }
    }

    @Override
    public UserE queryByLoginName(String userName) {
        try {
            ResponseEntity<UserDO> responseEntity = iamServiceClient.queryByLoginName(userName);
            return ConvertHelper.convert(responseEntity.getBody(), UserE.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<ProjectE> listIamProjectByOrgId(Long organizationId) {
        List<ProjectE> returnList = new ArrayList<>();
        int page = 0;
        int size = 20;
        ResponseEntity<Page<ProjectDO>> pageResponseEntity =
                iamServiceClient.queryProjectByOrgId(organizationId, page, size);
        if (pageResponseEntity != null) {
            Page<ProjectDO> projectDOPage = pageResponseEntity.getBody();
            List<ProjectE> projectEList = ConvertHelper.convertList(projectDOPage.getContent(), ProjectE.class);
            if (projectEList != null) {
                returnList.addAll(projectEList);
            }
            int totalPages = projectDOPage.getTotalPages();
            if (totalPages > 1) {
                for (int i = 1; i < totalPages; i++) {
                    page = i;
                    ResponseEntity<Page<ProjectDO>> entity = iamServiceClient
                            .queryProjectByOrgId(organizationId, page, size);
                    if (entity != null) {
                        Page<ProjectDO> project = entity.getBody();
                        List<ProjectE> projectE = ConvertHelper.convertList(project.getContent(), ProjectE.class);
                        if (projectE != null) {
                            returnList.addAll(projectE);
                        }
                    }
                }
            }

        }
        return returnList;
    }
}
