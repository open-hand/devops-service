package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.dto.GitlabGroupMemberDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.UserAttrE;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface GitlabGroupMemberService {

    void createGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList);

    void deleteGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList);

    void checkEnvProject(DevopsEnvironmentE devopsEnvironmentE, UserAttrE userAttrE);
}
