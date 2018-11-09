package io.choerodon.devops.infra.persistence.impl;

import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository;
import io.choerodon.devops.infra.dataobject.gitlab.RequestMemberDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

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
        try {
            return ConvertHelper.convert(gitlabServiceClient.getUserMemberByUserId(
                    groupId, userId).getBody(), GitlabMemberE.class);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public ResponseEntity deleteMember(Integer groupId, Integer userId) {
        try {
            return gitlabServiceClient.deleteMember(groupId, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public int insertMember(Integer groupId, RequestMemberDO member) {
        try {
            return gitlabServiceClient.insertMember(groupId, member).getStatusCodeValue();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public ResponseEntity updateMember(Integer groupId, RequestMemberDO member) {
        try {
            return gitlabServiceClient.updateMember(groupId, member);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public void initMockService(GitlabServiceClient gitlabServiceClient) {
        this.gitlabServiceClient = gitlabServiceClient;
    }
}
