package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.GitlabGroupMemberDTO;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.domain.application.event.GitlabGroupPayload;
import io.choerodon.devops.domain.application.event.KanbanProjectPayload;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO;
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
                                  GitlabGroupMemberService gitlabGroupMemberService) {
        this.gitlabServiceClient = gitlabServiceClient;
        this.devopsProjectRepository = devopsProjectRepository;
        this.eventProducerTemplate = eventProducerTemplate;
        this.gitlabGroupMemberService = gitlabGroupMemberService;
    }

    @Override
    public void createGroup(GitlabGroupPayload gitlabGroupPayload) {

        //创建gitlab group
        GroupDO group = new GroupDO();
        // name: orgName-projectName
        group.setName(gitlabGroupPayload.getOrganizationName() + "-" + gitlabGroupPayload.getProjectName());
        // path: orgCode-projectCode
        group.setPath(gitlabGroupPayload.getOrganizationCode() + "-" + gitlabGroupPayload.getProjectCode());
        ResponseEntity<GroupDO> responseEntity =
                gitlabServiceClient.createGroup(group, gitlabGroupPayload.getUserName());
        group = responseEntity.getBody();
        if (group != null) {
            DevopsProjectDO devopsProjectDO = new DevopsProjectDO(gitlabGroupPayload.getProjectId());
            devopsProjectDO.setGitlabGroupId(group.getId());
            devopsProjectRepository.updateProjectAttr(devopsProjectDO);
            setProjectMemberEvent(gitlabGroupPayload);

            List<GitlabProjectDO> projectList =
                    gitlabServiceClient.listProjects(group.getId(), gitlabGroupPayload.getUserName()).getBody();
            if (!projectList.isEmpty()) {
                KanbanProjectPayload kanbanProjectPayload = new KanbanProjectPayload();
                kanbanProjectPayload.setGitlabGroupId(group.getId());
                kanbanProjectPayload.setProjectId(gitlabGroupPayload.getProjectId());
                kanbanProjectPayload.setGitlabProjectId(projectList.get(0).getId());
                kanbanProjectPayload.setProjectName(gitlabGroupPayload.getProjectName());
                Exception exception = eventProducerTemplate.execute("createProject", "devops-service", kanbanProjectPayload,
                        (String uuid) -> {
                        });
                if (exception != null) {
                    throw new CommonException(exception.getMessage());
                }
            }
        }
    }


    private void setProjectMemberEvent(GitlabGroupPayload gitlabGroupPayload) {
        GitlabGroupMemberDTO gitlabGroupMemberDTO = new GitlabGroupMemberDTO();
        gitlabGroupMemberDTO.setResourceId(gitlabGroupPayload.getProjectId());
        gitlabGroupMemberDTO.setResourceType("project");
        gitlabGroupMemberDTO.setRoleLabels(gitlabGroupPayload.getRoleLabels());
        gitlabGroupMemberDTO.setUsername(gitlabGroupPayload.getUserName());
        List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList = new ArrayList<>();
        gitlabGroupMemberDTOList.add(gitlabGroupMemberDTO);
        gitlabGroupMemberService.createGitlabGroupMemberRole(gitlabGroupMemberDTOList);
    }
}
