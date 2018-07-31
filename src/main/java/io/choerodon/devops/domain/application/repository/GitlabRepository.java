package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO;

/**
 * Created by younger on 2018/3/29.
 */
public interface GitlabRepository {

    void addVariable(Integer gitlabProjectId, String key, String value, Boolean protecteds, Integer userId);

    List<String> listTokenByUserId(Integer gitlabProjectId, String name, Integer userId);

    String createToken(Integer gitlabProjectId, String name, Integer userId);

    GitlabGroupE queryGroupByName(String groupName, Integer userId);

    GitlabGroupE createGroup(GitlabGroupE gitlabGroupE, Integer userId);

    Boolean createFile(Integer projectId, Integer userId);

    void createProtectBranch(Integer projectId, String name, String mergeAccessLevel,
                             String pushAccessLevel, Integer userId);

    void deleteProject(Integer projectId, Integer userId);

    String updateProject(Integer projectId, Integer userId);

    ProjectHook createWebHook(Integer projectId, Integer userId, ProjectHook projectHook);

    GitlabProjectDO createProject(Integer groupId, String projectName, Integer userId, boolean visibility);

    void createDeployKey(Integer projectId, String title, String key, boolean canPush, Integer userId);

}
