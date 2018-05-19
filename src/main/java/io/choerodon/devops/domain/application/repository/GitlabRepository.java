package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;

/**
 * Created by younger on 2018/3/29.
 */
public interface GitlabRepository {

    void addVariable(Integer gitlabProjectId, String key, String value, Boolean protecteds, String userName);

    List<String> listTokenByUserName(Integer gitlabProjectId, String name, String userName);

    String createToken(Integer gitlabProjectId, String name, String userName);

    GitlabGroupE queryGroupByName(String groupName);

    GitlabGroupE createGroup(GitlabGroupE gitlabGroupE);

    Boolean createFile(Integer projectId, String userName);

    void createProtectBranch(Integer projectId, String name, String mergeAccessLevel,
                             String pushAccessLevel, String userName);

    void deleteProject(Integer projectId);

    void updateProject(Integer projectId,String userName);
}
