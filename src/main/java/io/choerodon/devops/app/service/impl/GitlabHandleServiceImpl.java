package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.GitlabUserRequestVO;
import io.choerodon.devops.api.vo.OrgAdministratorVO;
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupPayload;
import io.choerodon.devops.app.eventhandler.payload.ProjectPayload;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.devops.app.service.GitlabHandleService;
import io.choerodon.devops.app.service.GitlabUserService;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;

/**
 * Created by wangxiang on 2020/12/29
 */
public class GitlabHandleServiceImpl implements GitlabHandleService {

    @Autowired
    private GitlabGroupService gitlabGroupService;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;

    @Autowired
    private GitlabUserService gitlabUserService;

    @Override
    public void handleProjectCategoryEvent(ProjectPayload projectPayload) {
        //1.在项目下创建三个组
        GitlabGroupPayload gitlabGroupPayload = new GitlabGroupPayload();
        gitlabGroupPayload.setOrganizationCode(projectPayload.getOrganizationCode());
        gitlabGroupPayload.setOrganizationName(projectPayload.getOrganizationName());
        gitlabGroupPayload.setProjectCode(projectPayload.getProjectCode());
        gitlabGroupPayload.setProjectId(projectPayload.getProjectId());
        gitlabGroupPayload.setProjectName(projectPayload.getProjectName());
        gitlabGroupPayload.setUserId(projectPayload.getUserId());
        gitlabGroupPayload.setUserName(projectPayload.getUserName());
        gitlabGroupService.createGroups(gitlabGroupPayload);

        //2.组织层创建gitlab用户
        //查询该项目下所有启用的用户带上他们的角色和gitlab标签 如果是组织管理员就同步为该项目三个组的owner
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectPayload.getProjectId());
        List<OrgAdministratorVO> orgAdministratorVOS = baseServiceClientOperator.listOrgAdministrator(projectDTO.getOrganizationId()).getContent();
        if (!CollectionUtils.isEmpty(orgAdministratorVOS)) {
            orgAdministratorVOS.forEach(orgAdministratorVO -> gitlabGroupMemberService.assignGitLabGroupMemberForOwner(projectDTO, orgAdministratorVO.getId()));
        }
        //剩下的创建gitlab用户，并分配角色
        List<Long> ids = orgAdministratorVOS.stream().map(OrgAdministratorVO::getId).collect(Collectors.toList());
        List<IamUserDTO> userDTOList = baseServiceClientOperator.queryUserByProjectId(projectDTO.getId());
        List<IamUserDTO> iamUserDTOS = userDTOList.stream().filter(iamUserDTO -> !ids.contains(iamUserDTO.getId())).collect(Collectors.toList());
        //剔除项目下的组织管理员 如果是项目下的成员则根据角色来创建并跟新gitlab用户
        iamUserDTOS.forEach(iamUserDTO -> {
            GitlabUserRequestVO gitlabUserReqDTO = new GitlabUserRequestVO();
            gitlabUserReqDTO.setProvider("oauth2_generic");
            gitlabUserReqDTO.setExternUid(String.valueOf(iamUserDTO.getId()));
            gitlabUserReqDTO.setSkipConfirmation(true);
            //userName 是user的loginName
            gitlabUserReqDTO.setUsername(iamUserDTO.getLoginName());
            gitlabUserReqDTO.setEmail(iamUserDTO.getEmail());
            //name是 user的realName
            gitlabUserReqDTO.setName(iamUserDTO.getRealName());
            if (iamUserDTO.getRealName() == null) {
                gitlabUserReqDTO.setName(iamUserDTO.getRealName());
            }
            gitlabUserReqDTO.setCanCreateGroup(true);
            gitlabUserReqDTO.setProjectsLimit(100);
            gitlabUserService.createGitlabUser(gitlabUserReqDTO);
            //如果是项目所有者，添加该项目下的三个组的owner权限
            gitlabGroupMemberService.assignGitLabGroupMemberForOwner(projectDTO, iamUserDTO.getId());

        });
        // TODO: 2020/12/29 停用的用户需要处理吗？
    }
}
