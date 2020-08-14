package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CiPipelineImageVO;
import io.choerodon.devops.infra.dto.CiPipelineImageDTO;

/**
 * @author scp
 * @date 2020/7/21
 * @description
 */
public interface CiPipelineImageService {

    void createOrUpdate(CiPipelineImageVO ciPipelineImageVO);

    CiPipelineImageDTO queryByGitlabPipelineId(Long gitlabPipelineId, String jobName);

}
