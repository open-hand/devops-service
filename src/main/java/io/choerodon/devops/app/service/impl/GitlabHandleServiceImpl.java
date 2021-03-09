package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import io.choerodon.devops.infra.dto.iam.RoleDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by wangxiang on 2020/12/29
 */
@Service
public class GitlabHandleServiceImpl implements GitlabHandleService {

    private static final String PROJECT_ADMIN = "project-admin";

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

        //2.同步项目下gitlab用户角色
        //2.1 同步root用户的角色
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectPayload.getProjectId());
        List<OrgAdministratorVO> orgAdministratorVOS = baseServiceClientOperator.listOrgAdministrator(projectDTO.getOrganizationId()).getContent();
        if (!CollectionUtils.isEmpty(orgAdministratorVOS)) {
            orgAdministratorVOS.forEach(orgAdministratorVO -> gitlabGroupMemberService.assignGitLabGroupMemberForOwner(projectDTO, orgAdministratorVO.getId()));
        }
        //2.2 同步组织管理员角色
        List<Long> rootIds = baseServiceClientOperator.queryRoot().stream().map(IamUserDTO::getId).collect(Collectors.toList());
        gitlabUserService.assignAdmins(rootIds);


        //2.3 同步项目所有者角色
        List<Long> ids = orgAdministratorVOS.stream().map(OrgAdministratorVO::getId).collect(Collectors.toList());
        ids.addAll(rootIds);
        List<IamUserDTO> userDTOList = baseServiceClientOperator.queryUserByProjectId(projectDTO.getId());
        List<IamUserDTO> iamUserDTOS = userDTOList.stream().filter(iamUserDTO -> !ids.contains(iamUserDTO.getId()) && iamUserDTO.getEnabled()).collect(Collectors.toList());
        //如果是项目所有者，添加该项目下的三个组的owner权限
        iamUserDTOS.forEach(iamUserDTO -> {
            if (iamUserDTO.getRoles().stream().map(RoleDTO::getCode).collect(Collectors.toList()).contains("project-admin")) {
                gitlabGroupMemberService.assignGitLabGroupMemberForOwner(projectDTO, iamUserDTO.getId());
            }
        });

        //2.4 同步停用用户的角色
        List<IamUserDTO> disanbleUser = userDTOList.stream().filter(iamUserDTO -> !iamUserDTO.getEnabled()).collect(Collectors.toList());
        disanbleUser.forEach(iamUserDTO -> {
            gitlabUserService.disEnabledGitlabUser(TypeUtil.objToLong(iamUserDTO.getId()));
        });
    }
}
