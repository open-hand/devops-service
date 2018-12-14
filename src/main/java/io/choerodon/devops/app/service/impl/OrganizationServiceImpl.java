package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.GitlabUserRequestDTO;
import io.choerodon.devops.api.dto.RegisterOrganizationDTO;
import io.choerodon.devops.app.service.GitlabUserService;
import io.choerodon.devops.app.service.OrganizationService;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.event.OrganizationEventPayload;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.Visibility;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final String TEMPLATE = "template";

    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabUserService gitlabUserService;

    @Override
    public void create(OrganizationEventPayload organizationEventPayload) {
        UserAttrE userAttrE = userAttrRepository.queryById(organizationEventPayload.getUserId());
        if (userAttrE == null) {
            throw new CommonException("gitlab user not related to iam user");
        }
        GitlabGroupE gitlabGroupENew = new GitlabGroupE();
        gitlabGroupENew.initName(organizationEventPayload.getCode() + "_" + TEMPLATE);
        gitlabGroupENew.initPath(organizationEventPayload.getCode() + "_" + TEMPLATE);
        gitlabGroupENew.initVisibility(Visibility.PUBLIC);
        gitlabRepository.createGroup(gitlabGroupENew, TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
    }

    @Override
    public void registerOrganization(RegisterOrganizationDTO registerOrganizationDTO) {
        GitlabUserRequestDTO gitlabUserReqDTO = new GitlabUserRequestDTO();
        gitlabUserReqDTO.setProvider("oauth2_generic");
        gitlabUserReqDTO.setExternUid(TypeUtil.objToString(registerOrganizationDTO.getUserId()));
        gitlabUserReqDTO.setSkipConfirmation(true);
        gitlabUserReqDTO.setUsername(registerOrganizationDTO.getLoginName());
        gitlabUserReqDTO.setEmail(registerOrganizationDTO.getEmail());
        gitlabUserReqDTO.setName(registerOrganizationDTO.getRealName());
        gitlabUserReqDTO.setCanCreateGroup(true);
        gitlabUserReqDTO.setProjectsLimit(100);
        gitlabUserService.createGitlabUser(gitlabUserReqDTO);
        UserAttrE userAttrE = userAttrRepository.queryById(registerOrganizationDTO.getUserId());
        if (userAttrE == null) {
            throw new CommonException("gitlab user not related to iam user");
        }
        GitlabGroupE gitlabGroupENew = new GitlabGroupE();
        gitlabGroupENew.initName(registerOrganizationDTO.getOrganizationCode() + "_" + TEMPLATE);
        gitlabGroupENew.initPath(registerOrganizationDTO.getOrganizationCode() + "_" + TEMPLATE);
        gitlabGroupENew.initVisibility(Visibility.PUBLIC);
        gitlabRepository.createGroup(gitlabGroupENew, TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
    }
}
