package io.choerodon.devops.domain.application.repository;

import org.springframework.http.ResponseEntity;

import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.infra.dataobject.gitlab.RequestMemberDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Created by Zenger on 2018/3/28.
 */
public interface GitlabGroupMemberRepository {

    GitlabMemberE getUserMemberByUserId(Integer groupId, Integer userId);

    ResponseEntity deleteMember(Integer groupId, Integer userId);

    int insertMember(Integer groupId, RequestMemberDO member);

    ResponseEntity updateMember(Integer groupId, RequestMemberDO member);

    void initMockService(GitlabServiceClient gitlabServiceClient);
}
