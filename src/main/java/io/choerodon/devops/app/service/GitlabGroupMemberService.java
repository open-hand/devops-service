package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.GitlabGroupMemberDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.api.vo.iam.entity.UserAttrE;
import io.choerodon.devops.infra.dataobject.gitlab.MemberDTO;
import io.choerodon.devops.infra.dataobject.gitlab.RequestMemberDO;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface GitlabGroupMemberService {

    void createGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList);

    void deleteGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList);

    void checkEnvProject(DevopsEnvironmentE devopsEnvironmentE, UserAttrE userAttrE);

    MemberDTO queryByUserId(Integer groupId, Integer userId);

    void delete(Integer groupId, Integer userId);

    int create(Integer groupId, RequestMemberDO member);

    void update(Integer groupId, RequestMemberDO member);
}
