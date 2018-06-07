package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.OrganizationService;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.event.OrganizationEventPayload;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.Visibility;

@Service
public class OrganizatonServiceImpl implements OrganizationService {

    private static final String TEMPLATE = "template";

    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;

    @Override
    public void create(OrganizationEventPayload organizationEventPayload) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(organizationEventPayload.getUserId()));
        GitlabGroupE gitlabGroupENew = new GitlabGroupE();
        gitlabGroupENew.initName(organizationEventPayload.getCode() + "_" + TEMPLATE);
        gitlabGroupENew.initPath(organizationEventPayload.getCode() + "_" + TEMPLATE);
        gitlabGroupENew.initVisibility(Visibility.PUBLIC);
        gitlabRepository.createGroup(gitlabGroupENew, TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
    }
}
