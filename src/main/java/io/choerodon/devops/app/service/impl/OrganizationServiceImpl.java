package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.eventhandler.payload.OrganizationEventPayload;
import io.choerodon.devops.app.service.OrganizationService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.enums.Visibility;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final String TEMPLATE = "template";

    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private UserAttrService userAttrService;

    @Override
    public void create(OrganizationEventPayload organizationEventPayload) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(organizationEventPayload.getUserId());
        if (userAttrDTO == null) {
            throw new CommonException("gitlab user not related to iam user");
        }
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setName(organizationEventPayload.getCode() + "_" + TEMPLATE);
        groupDTO.setPath(organizationEventPayload.getCode() + "_" + TEMPLATE);
        groupDTO.setVisibility(Visibility.PUBLIC);
        gitlabServiceClientOperator.createGroup(groupDTO, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
    }
}
