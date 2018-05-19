package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO;
import io.choerodon.devops.infra.dataobject.gitlab.ImpersonationTokenDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabRepositoryImpl implements GitlabRepository {

    private GitlabServiceClient gitlabServiceClient;
    private GitUtil gitUtil;

    public GitlabRepositoryImpl(GitlabServiceClient gitlabServiceClient, GitUtil gitUtil) {
        this.gitlabServiceClient = gitlabServiceClient;
        this.gitUtil = gitUtil;
    }

    @Override
    public void addVariable(Integer gitlabProjectId, String key, String value, Boolean protecteds, String userName) {
        gitlabServiceClient.addVariable(gitlabProjectId, key, value, protecteds, userName);
    }

    @Override
    public List<String> listTokenByUserName(Integer gitlabProjectId, String name, String userName) {
        ResponseEntity<List<ImpersonationTokenDO>> impersonationTokens = gitlabServiceClient
                .listTokenByUserName(userName);
        if (!impersonationTokens.getStatusCode().is2xxSuccessful()) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProject(gitlabProjectId, userName);
        }
        List<String> tokens = new ArrayList<>();
        impersonationTokens.getBody().parallelStream().forEach(impersonationToken ->
                tokens.add(impersonationToken.getToken())
        );
        return tokens;
    }

    @Override
    public String createToken(Integer gitlabProjectId, String name, String userName) {
        ResponseEntity<ImpersonationTokenDO> impersonationToken = gitlabServiceClient.createToken(userName);
        if (!impersonationToken.getStatusCode().is2xxSuccessful()) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProject(gitlabProjectId, userName);
        }
        return impersonationToken.getBody().getToken();
    }

    @Override
    public GitlabGroupE queryGroupByName(String groupName) {
        ResponseEntity<GroupDO> groupDO = gitlabServiceClient.queryGroupByName(groupName);
        if (groupDO != null) {
            return ConvertHelper.convert(groupDO.getBody(), GitlabGroupE.class);
        } else {
            return null;
        }
    }

    @Override
    public GitlabGroupE createGroup(GitlabGroupE gitlabGroupE) {
        ResponseEntity<GroupDO> groupDO = gitlabServiceClient.createGroup(ConvertHelper.convert(
                gitlabGroupE, GroupDO.class));
        if (groupDO.getStatusCode().is2xxSuccessful()) {
            return ConvertHelper.convert(groupDO.getBody(), GitlabGroupE.class);
        } else {
            throw new CommonException("error.group.create");
        }
    }

    @Override
    public Boolean createFile(Integer projectId, String userName) {
        ResponseEntity<Boolean> result = gitlabServiceClient.createFile(projectId, userName);
        if (result.getStatusCode().is2xxSuccessful()) {
            return result.getBody();
        } else {
            throw new CommonException("error.file.create");
        }
    }

    @Override
    public void createProtectBranch(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel, String userName) {
        ResponseEntity<Map<String, Object>> branch = gitlabServiceClient.createProtectedBranches(
                projectId, name, mergeAccessLevel, pushAccessLevel, userName);
        if (!branch.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.branch.create");
        }
    }

    @Override
    public void deleteProject(Integer projectId) {
        gitlabServiceClient.deleteProject(projectId, null);
    }


    @Override
    public void updateProject(Integer projectId,String userName) {
         gitlabServiceClient.updateProject(projectId,userName);
    }

}
