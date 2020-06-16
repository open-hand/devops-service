package io.choerodon.devops.app.service.impl;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;

/**
 * @author zmf
 * @since 19-12-30
 */
@Service
public class PermissionHelperServiceImpl implements PermissionHelper {
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    public boolean isGitlabAdmin(Long userId) {
        UserAttrDTO result = userAttrService.baseQueryById(userId);
        return result != null && result.getGitlabAdmin();
    }

    @Override
    public boolean isGitlabAdmin() {
        if (DetailsHelper.getUserDetails() == null || DetailsHelper.getUserDetails().getUserId() == null) {
            return false;
        }
        return isGitlabAdmin(DetailsHelper.getUserDetails().getUserId());
    }

    @Override
    public boolean isGitlabProjectOwnerOrGitlabAdmin(Long projectId) {
        Long iamUserId = DetailsHelper.getUserDetails().getUserId();
        return isGitlabAdmin(iamUserId) || isGitlabProjectOwnerOrgitlabOrganizationOwner(iamUserId, projectId);
    }

    @Override
    public boolean isGitlabProjectOwnerOrGitlabAdmin(Long projectId, Long iamUserId) {
        return isGitlabAdmin(iamUserId) || isGitlabProjectOwnerOrgitlabOrganizationOwner(iamUserId, projectId);
    }

    @Override
    public boolean isGitlabProjectOwnerOrGitlabAdmin(Long projectId, @Nullable UserAttrDTO userAttrDTO) {
        if (userAttrDTO == null || userAttrDTO.getIamUserId() == null) {
            return false;
        }
        return userAttrDTO.getGitlabAdmin() || isGitlabProjectOwnerOrgitlabOrganizationOwner(userAttrDTO.getIamUserId(), projectId);
    }

    private boolean isGitlabProjectOwnerOrgitlabOrganizationOwner(Long userId, Long projectId) {
        return baseServiceClientOperator.isGitlabProjectOwner(userId, projectId) || baseServiceClientOperator.isGitLabOrgOwner(userId, projectId);
    }

    @Override
    public void checkProjectOwnerOrGitlabAdmin(Long projectId, Long iamUserId) {
        if (!isGitlabProjectOwnerOrGitlabAdmin(projectId, iamUserId)) {
            throw new CommonException("error.user.not.gitlab.owner");
        }
    }

    @Override
    public void checkProjectOwnerOrGitlabAdmin(Long projectId, @Nullable UserAttrDTO userAttrDTO) {
        if (!isGitlabProjectOwnerOrGitlabAdmin(projectId, userAttrDTO)) {
            throw new CommonException("error.user.not.owner");
        }
    }

    @Override
    public Boolean isRoot(Long userId) {
        return baseServiceClientOperator.isRoot(userId);
    }

    @Override
    public Boolean isOrganizationRoot(Long userId, Long organizationId) {
        return baseServiceClientOperator.isOrganzationRoot(userId, organizationId);
    }

    @Override
    public Boolean isProjectOwner(Long userId, Long projectId) {
        return baseServiceClientOperator.isProjectOwner(userId, projectId);
    }

    @Override
    public Boolean isGitlabProjectOwner(Long userId, Long projectId) {
        return baseServiceClientOperator.isGitlabProjectOwner(userId, projectId);
    }
}
