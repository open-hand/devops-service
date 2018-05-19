package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.dto.GitlabGroupMemberDTO;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface GitlabGroupMemberService {

    void createGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList);

    void deleteGitlabGroupMemberRole(List<GitlabGroupMemberDTO> gitlabGroupMemberDTOList);
}
