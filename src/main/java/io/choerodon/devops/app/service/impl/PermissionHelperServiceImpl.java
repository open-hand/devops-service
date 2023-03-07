package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.AppServiceCode.DEVOPS_APP_SERVICE_NOT_EXIST;
import static io.choerodon.devops.infra.constant.ExceptionConstants.CertificationCode.DEVOPS_CERTIFICATION_NOT_EXIST_IN_DATABASE;
import static io.choerodon.devops.infra.constant.ExceptionConstants.ClusterCode.DEVOPS_CLUSTER_NOT_EXIST;
import static io.choerodon.devops.infra.constant.ExceptionConstants.EnvironmentCode.DEVOPS_ENV_ID_NOT_EXIST;
import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.DEVOPS_USER_NOT_GITLAB_OWNER;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.util.CommonExAssertUtil;

/**
 * @author zmf
 * @since 19-12-30
 */
@Service
public class PermissionHelperServiceImpl implements PermissionHelper {

    private static final String DEVOPS_DEPLOYMENT_WAY_NOT_ONLY = "devops.deployment.way.not.only";

    @Autowired
    @Lazy
    private UserAttrService userAttrService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsClusterMapper devopsClusterMapper;
    @Autowired
    private DevopsClusterProPermissionMapper devopsClusterProPermissionMapper;
    @Autowired
    private DevopsCertificationProRelMapper devopsCertificationProRelMapper;
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private AppServiceMapper appServiceMapper;

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
        return baseServiceClientOperator.checkIsOrgOrProjectGitlabOwner(userId, projectId);
    }

    @Override
    public void checkProjectOwnerOrGitlabAdmin(Long projectId, Long iamUserId) {
        if (!isGitlabProjectOwnerOrGitlabAdmin(projectId, iamUserId)) {
            throw new CommonException(DEVOPS_USER_NOT_GITLAB_OWNER);
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

    @Override
    public boolean projectPermittedToCluster(Long clusterId, Long projectId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(Objects.requireNonNull(clusterId));
        CommonExAssertUtil.assertTrue(devopsClusterDTO != null, DEVOPS_CLUSTER_NOT_EXIST, clusterId);

        if (Boolean.TRUE.equals(devopsClusterDTO.getSkipCheckProjectPermission())) {
            return true;
        }

        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
        devopsClusterProPermissionDTO.setClusterId(clusterId);
        devopsClusterProPermissionDTO.setProjectId(Objects.requireNonNull(projectId));
        return devopsClusterProPermissionMapper.selectCount(devopsClusterProPermissionDTO) > 0;
    }

    @Override
    public boolean projectPermittedToCert(Long certId, Long projectId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(Objects.requireNonNull(certId));
        CommonExAssertUtil.assertNotNull(certificationDTO, DEVOPS_CERTIFICATION_NOT_EXIST_IN_DATABASE, certId);
        if (Boolean.TRUE.equals(certificationDTO.getSkipCheckProjectPermission())) {
            return true;
        }

        DevopsCertificationProRelationshipDTO condition = new DevopsCertificationProRelationshipDTO();
        condition.setCertId(certId);
        condition.setProjectId(projectId);
        return devopsCertificationProRelMapper.selectCount(condition) > 0;
    }

    @Override
    public DevopsEnvironmentDTO checkEnvBelongToProject(Long projectId, Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.queryByIdWithClusterCode(envId);
        if (devopsEnvironmentDTO == null) {
            throw new CommonException(DEVOPS_ENV_ID_NOT_EXIST, envId);

        }
        CommonExAssertUtil.assertTrue(projectId.equals(devopsEnvironmentDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        return devopsEnvironmentDTO;
    }

    @Override
    public AppServiceDTO checkAppServiceBelongToProject(Long projectId, Long appServiceId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        if (appServiceDTO == null) {
            throw new CommonException(DEVOPS_APP_SERVICE_NOT_EXIST);
        }
        CommonExAssertUtil.assertTrue(projectId.equals(appServiceDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        return appServiceDTO;
    }


    @Override
    public void checkAppServicesBelongToProject(Long projectId, List<Long> appServiceIds) {
        List<Long> appServiceIdsBelongToProject = appServiceMapper.listByProjectId(projectId, null, null)
                .stream()
                .map(AppServiceDTO::getId)
                .collect(Collectors.toList());
        CommonExAssertUtil.assertTrue(appServiceIdsBelongToProject.containsAll(appServiceIds), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
    }

    @Override
    public void checkDeploymentWay(DevopsServiceReqVO devopsServiceReqVO) {
        int count = 0;
        if (devopsServiceReqVO.getTargetAppServiceId() != null) count++;
        if (devopsServiceReqVO.getTargetDeploymentId() != null) count++;
        if (StringUtils.hasText(devopsServiceReqVO.getTargetInstanceCode())) count++;
        if (!ArrayUtil.isEmpty(devopsServiceReqVO.getEndPoints())) count++;
        if (!ArrayUtil.isEmpty(devopsServiceReqVO.getSelectors())) count++;


        if (count != 1) {
            throw new CommonException(DEVOPS_DEPLOYMENT_WAY_NOT_ONLY);
        }
    }
}
