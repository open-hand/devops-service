package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.jenkins.*;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/3 9:24
 */
public interface JenkinsJobService {
    List<JenkinsJobVO> listAll(Long projectId);

    void build(Long projectId,
               Long serverId,
               String folder,
               String name,
               List<PropertyVO> properties);

    List<PropertyVO> listProperty(Long projectId, Long serverId, String folder, String name);

    List<JenkinsBuildInfo> listBuildHistory(Long projectId, Long serverId, String folder, String name);

    void stopBuild(Long projectId, Long serverId, String folder, String name, Integer buildId);

    void retryBuild(Long projectId, Long serverId, String folder, String name, Integer buildId);

    void auditPass(Long projectId, Long serverId, String folder, String name, Integer buildId, String inputId, List<PropertyVO> properties);

    void auditRefuse(Long projectId, Long serverId, String folder, String name, Integer buildId, String inputId);

    JenkinsBuildInfo queryBuildInfo(Long projectId, Long serverId, String folder, String name, Integer buildId);

    String queryLog(Long projectId, Long serverId, String folder, String name, Integer buildId);

    List<JenkinsStageVO> listStage(Long projectId, Long serverId, String folder, String name, Integer buildId);

    List<JenkinsNodeVO> listNode(Long projectId, Long serverId, String folder, String name, Integer buildId, Integer stageId);

    String queryNodeLog(Long projectId, Long serverId, String folder, String name, Integer buildId, Integer stageId, Integer nodeId);

    List<String> listBranch(Long projectId, Long serverId, String folder, String name);
}
