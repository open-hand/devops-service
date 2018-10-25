package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.devops.api.dto.RoleAssignmentSearchDTO;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO;
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;
import io.choerodon.devops.infra.dataobject.iam.UserDO;
import io.choerodon.devops.infra.feign.IamServiceClient;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class IamRepositoryImpl implements IamRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(IamRepositoryImpl.class);

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
        } catch (FeignException e) {
            LOGGER.error("get user by longin name {} error", userName);
            return null;
        }
    }

    @Override
    public List<ProjectE> listIamProjectByOrgId(Long organizationId, String name) {
        List<ProjectE> returnList = new ArrayList<>();
        int page = 0;
        int size = 200;
        ResponseEntity<Page<ProjectDO>> pageResponseEntity =
                iamServiceClient.queryProjectByOrgId(organizationId, page, size, name);
        Page<ProjectDO> projectDOPage = pageResponseEntity.getBody();
        List<ProjectE> projectEList = ConvertHelper.convertList(projectDOPage.getContent(), ProjectE.class);
        if (ConvertHelper.convertList(projectDOPage.getContent(), ProjectE.class) != null) {
            returnList.addAll(projectEList);
        }
        int totalPages = projectDOPage.getTotalPages();
        if (totalPages > 1) {
            for (int i = 1; i < totalPages; i++) {
                page = i;
                ResponseEntity<Page<ProjectDO>> entity = iamServiceClient
                        .queryProjectByOrgId(organizationId, page, size, name);
                if (entity != null) {
                    Page<ProjectDO> project = entity.getBody();
                    List<ProjectE> projectE = ConvertHelper.convertList(project.getContent(), ProjectE.class);
                    if (projectE != null) {
                        returnList.addAll(projectE);
                    }
                }
            }
        }
        return returnList;
    }

    @Override
    public UserE queryById(Long id) {
        try {
            ResponseEntity<UserDO> responseEntity = iamServiceClient.queryById(id);
            return ConvertHelper.convert(responseEntity.getBody(), UserE.class);
        } catch (FeignException e) {
            LOGGER.error("get user by user id {}", id);
            return null;
        }
    }

    @Override
    public UserE queryByProjectAndId(Long projectId, Long id) {
        try {
            ResponseEntity<Page<UserDO>> responseEntity = iamServiceClient.queryInProjectById(projectId, id);
            return ConvertHelper.convert(responseEntity.getBody().getContent().get(0), UserE.class);
        } catch (FeignException e) {
            LOGGER.error("get user by project id {} and user id {} error", projectId, id);
            return null;
        }
    }

    @Override
    public List<UserE> listUsersByIds(List<Long> ids) {
        List<UserE> userES = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            Long[] newIds = new Long[ids.size()];
            try {
                userES = ConvertHelper.convertList(iamServiceClient
                        .listUsersByIds(ids.toArray(newIds)).getBody(), UserE.class);
            } catch (Exception e) {
                throw new CommonException("error.users.get", e);
            }
        }
        return userES;
    }

    @Override
    public UserE queryUserByUserId(Long id) {
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        List<UserE> userES = this.listUsersByIds(ids);
        if (userES != null && !userES.isEmpty()) {
            return userES.get(0);
        }
        return null;
    }

    @Override
    public UserE queryByEmail(Long projectId, String email) {
        try {
            ResponseEntity<Page<UserDO>> userDOResponseEntity = iamServiceClient.listUsersByEmail(projectId, 0, 10, email);
            return ConvertHelper.convert(userDOResponseEntity.getBody().getContent().get(0), UserE.class);

        } catch (FeignException e) {
            LOGGER.error("get user by email {} error", email);
            return null;
        }
    }

    @Override
    public Page<UserE> queryUserPermissionByProjectId(Long projectId, PageRequest pageRequest) {
        try {
            pageRequest.setPage(0);
            pageRequest.setSize(100);
            ResponseEntity<Page<UserDO>> userEPageResponseEntity = iamServiceClient.queryUserByProjectId(projectId,
                    pageRequest.getPage(), pageRequest.getSize(), new RoleAssignmentSearchDTO());
            return ConvertPageHelper.convertPage(userEPageResponseEntity.getBody(), UserE.class);
        } catch (FeignException e) {
            LOGGER.error("get user permission by project id {} error", projectId);
            return null;
        }
    }
}
