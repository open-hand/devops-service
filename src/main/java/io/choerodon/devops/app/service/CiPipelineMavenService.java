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
     * @param gitlabPipelineId gitlab流水线id
     * @param jobId ci job 的id
     * @param sequence job的顺序
     * @param jobName          job名称
     * @param token            应用服务token
     * @param file             pom文件
     */
    void createOrUpdate(Long nexusRepoId,
                        Long jobId,
                        Long sequence,
                        Long gitlabPipelineId,
                        String jobName,
                        String token,
                        MultipartFile file);

    CiPipelineMavenDTO queryByGitlabPipelineId(Long gitlabPipelineId, String jobName);
}
