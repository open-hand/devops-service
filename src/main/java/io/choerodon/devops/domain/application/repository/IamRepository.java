package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.valueobject.Organization;

/**
 * Created by younger on 2018/3/29.
 */
public interface IamRepository {

    ProjectE queryIamProject(Long projectId);

    Organization queryOrganization();

    Organization queryOrganizationById(Long organizationId);

    UserE queryByLoginName(String userName);

    List<ProjectE> listIamProjectByOrgId(Long organizationId);

    UserE queryById(Long id);

    UserE queryByProjectAndId(Long projectId, Long id);

    List<UserE> listUsersByIds(List<Long> ids);

    UserE queryUserByUserId(Long id);
}
