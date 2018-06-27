package io.choerodon.devops.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.eventhandler.SocketMessageHandler;
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

    private static final Logger logger = LoggerFactory.getLogger(GitlabGroupServiceImpl.class);

    @Autowired
    private GitlabServiceClient gitlabServiceClient;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;


    @Override
    public void createGroup(GitlabGroupPayload gitlabGroupPayload) {

        //创建gitlab group
        GroupDO group = new GroupDO();
        // name: orgName-projectName
        group.setName(gitlabGroupPayload.getOrganizationName() + "-" + gitlabGroupPayload.getProjectName());
        // path: orgCode-projectCode
        group.setPath(gitlabGroupPayload.getOrganizationCode() + "-" + gitlabGroupPayload.getProjectCode());
        UserAttrE userAttrE = userAttrRepository.queryById(gitlabGroupPayload.getUserId());
        ResponseEntity<GroupDO> responseEntity =
                gitlabServiceClient.createGroup(group, TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        logger.info(responseEntity.getBody().toString());
        group = responseEntity.getBody();
        if (group != null) {
            DevopsProjectDO devopsProjectDO = new DevopsProjectDO(gitlabGroupPayload.getProjectId());
            devopsProjectDO.setGitlabGroupId(group.getId());
            devopsProjectRepository.updateProjectAttr(devopsProjectDO);
        }
    }


}
