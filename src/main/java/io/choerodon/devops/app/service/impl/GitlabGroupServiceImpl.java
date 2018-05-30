package io.choerodon.devops.app.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.event.GitlabGroupPayload;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.event.producer.execute.EventProducerTemplate;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:05
 * Description:
 */
@Component
public class GitlabGroupServiceImpl implements GitlabGroupService {


    private GitlabServiceClient gitlabServiceClient;
    private DevopsProjectRepository devopsProjectRepository;
    private EventProducerTemplate eventProducerTemplate;
    private GitlabGroupMemberService gitlabGroupMemberService;
    private UserAttrRepository userAttrRepository;

    /**
     * GitLab group 创建事件服务
     *
     * @param gitlabServiceClient     GitLab 平台接口
     * @param devopsProjectRepository DevOps 项目 仓储类
     * @param eventProducerTemplate   事件发送
     */
    public GitlabGroupServiceImpl(GitlabServiceClient gitlabServiceClient,
                                  DevopsProjectRepository devopsProjectRepository,
                                  EventProducerTemplate eventProducerTemplate,
                                  GitlabGroupMemberService gitlabGroupMemberService,
                                  UserAttrRepository userAttrRepository) {
        this.gitlabServiceClient = gitlabServiceClient;
        this.devopsProjectRepository = devopsProjectRepository;
        this.eventProducerTemplate = eventProducerTemplate;
        this.gitlabGroupMemberService = gitlabGroupMemberService;
        this.userAttrRepository = userAttrRepository;
    }

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
        group = responseEntity.getBody();
        if (group != null) {
            DevopsProjectDO devopsProjectDO = new DevopsProjectDO(gitlabGroupPayload.getProjectId());
            devopsProjectDO.setGitlabGroupId(group.getId());
            devopsProjectRepository.updateProjectAttr(devopsProjectDO);
        }
    }


}
