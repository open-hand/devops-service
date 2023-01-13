package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CiPipelineImageVO;
import io.choerodon.devops.api.vo.ImageRepoInfoVO;
import io.choerodon.devops.api.vo.NpmRepoInfoVO;
import io.choerodon.devops.infra.dto.CiPipelineImageDTO;

/**
 * @author scp
 * @date 2020/7/21
 * @description
 */
public interface CiPipelineImageService {

    void createOrUpdate(CiPipelineImageVO ciPipelineImageVO);

    CiPipelineImageDTO queryByGitlabPipelineId(Long appServiceId, Long gitlabPipelineId, String jobName);

    ImageRepoInfoVO queryRewriteRepoInfoScript(Long projectId, String token, String repoType, Long repoId);

    ImageRepoInfoVO queryImageRepoInfo(String token, Long gitlabPipelineId);

    CiPipelineImageDTO queryPipelineLatestImage(Long appServiceId, Long gitlabPipelineId);

    void deleteByAppServiceId(Long appServiceId);

    NpmRepoInfoVO queryNpmRepoInfo(String token, Long repoId);
}
