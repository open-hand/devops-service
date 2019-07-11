package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/3/28.
 */
@Component
public class GitlabGroupMemberRepositoryImpl implements GitlabGroupMemberRepository {

    private GitlabServiceClient gitlabServiceClient;

    public GitlabGroupMemberRepositoryImpl(GitlabServiceClient gitlabServiceClient) {
        this.gitlabServiceClient = gitlabServiceClient;
    }

    @Override
    public GitlabMemberE getUserMemberByUserId(Integer groupId, Integer userId) {
        return ConvertHelper.convert(gitlabServiceClient.getUserMemberByUserId(
                    groupId, userId).getBody(), GitlabMemberE.class);
    }

    @Override
    public ResponseEntity deleteMember(Integer groupId, Integer userId) {
        return gitlabServiceClient.deleteMember(groupId, userId);
    }

    @Override
    public int insertMember(Integer groupId, RequestMemberDO member) {
            return gitlabServiceClient.insertMember(groupId, member).getStatusCodeValue();
    }

    @Override
    public ResponseEntity updateMember(Integer groupId, RequestMemberDO member) {
            return gitlabServiceClient.updateMember(groupId, member);
    }

}
