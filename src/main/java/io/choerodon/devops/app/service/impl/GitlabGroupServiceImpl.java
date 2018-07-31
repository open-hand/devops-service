package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.event.GitlabGroupPayload;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:05
 * Description:
 */
@Component
public class GitlabGroupServiceImpl implements GitlabGroupService {

    @Autowired
    private GitlabServiceClient gitlabServiceClient;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;


    @Override
    public void createGroup(GitlabGroupPayload gitlabGroupPayload, String groupCodeSuffix) {

        //创建gitlab group
        GroupDO group = new GroupDO();
        // name: orgName-projectName
        group.setName(String.format("%s-%s%s",
                gitlabGroupPayload.getOrganizationName(),
                gitlabGroupPayload.getProjectName(),
                groupCodeSuffix));
        // path: orgCode-projectCode
        group.setPath(String.format("%s-%s%s",
                gitlabGroupPayload.getOrganizationCode(),
                gitlabGroupPayload.getProjectCode(),
                groupCodeSuffix));
        UserAttrE userAttrE = userAttrRepository.queryById(gitlabGroupPayload.getUserId());
        ResponseEntity<GroupDO> responseEntity =
                gitlabServiceClient.createGroup(group, TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        if (responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
            group = responseEntity.getBody();
            DevopsProjectDO devopsProjectDO = new DevopsProjectDO(gitlabGroupPayload.getProjectId());
            if (groupCodeSuffix.isEmpty()) {
                devopsProjectDO.setGitlabGroupId(group.getId());
            } else if ("-gitops".equals(groupCodeSuffix)) {
                devopsProjectDO.setEnvGroupId(group.getId());
            }
            devopsProjectRepository.updateProjectAttr(devopsProjectDO);
        }
    }


}
