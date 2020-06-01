package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.GitlabGroupMemberVO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface GitlabGroupMemberService {

    /**
     * 同步用户权限到gitlab
     *
     * @param gitlabGroupMemberVOList 用户当前角色信息
     * @param isCreateUser            是否是创建用户, 用于跳过会报错的逻辑, 保证用户创建成功
     */
    void createGitlabGroupMemberRole(List<GitlabGroupMemberVO> gitlabGroupMemberVOList, boolean isCreateUser);

    void deleteGitlabGroupMemberRole(List<GitlabGroupMemberVO> gitlabGroupMemberVOList);

    void checkEnvProject(DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO);

    MemberDTO queryByUserId(Integer groupId, Integer userId);

    void delete(Integer groupId, Integer userId);

    int create(Integer groupId, MemberDTO memberDTO);

    void update(Integer groupId, MemberDTO memberDTO);

    void assignGitLabGroupOwner(Long groupId, MemberDTO groupMemberDTO, MemberDTO memberDTO);

    void assignGitLabGroupMemberForOwner(ProjectDTO projectDTO, Long userId);
}
