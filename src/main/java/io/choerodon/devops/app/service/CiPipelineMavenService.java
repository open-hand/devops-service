package io.choerodon.devops.app.service;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.infra.dto.CiPipelineMavenDTO;

/**
 * @author scp
 * @date 2020/7/22
 */
public interface CiPipelineMavenService {

    void createOrUpdate(CiPipelineMavenDTO ciPipelineMavenDTO);

    /**
     * 存储jar包元数据
     *
     * @param nexusRepoId      制品库id
     * @param mvnSettingsId    mvnSettingsId
     * @param sequence         job的顺序
     * @param gitlabPipelineId gitlab流水线id
     * @param jobName          job名称
     * @param token            应用服务token
     * @param file             pom文件
     * @param mavenRepoUrl
     * @param username
     * @param password
     * @param version
     * @param groupId
     * @param artifactId
     * @param jarVersion
     * @param packaging
     */
    void createOrUpdateJarInfo(Long nexusRepoId,
                               Long sequence,
                               Long gitlabPipelineId,
                               String jobName,
                               String token,
                               MultipartFile file,
                               String mavenRepoUrl,
                               String username,
                               String password,
                               String version,
                               String groupId,
                               String artifactId,
                               String jarVersion,
                               String packaging);

    CiPipelineMavenDTO queryByGitlabPipelineId(Long appServiceId, Long gitlabPipelineId, String jobName);

    CiPipelineMavenDTO queryPipelineLatestImage(Long appServiceId, Long gitlabPipelineId);

    void deleteByAppServiceId(Long appServiceId);
}
