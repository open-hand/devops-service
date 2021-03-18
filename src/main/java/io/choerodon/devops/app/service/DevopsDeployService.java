package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.deploy.DeployConfigVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/19 16:04
 */
public interface DevopsDeployService {

    /**
     * 主机部署
     *
     * @param projectId
     * @param deployConfigVO
     */
    void hostDeploy(Long projectId, DeployConfigVO deployConfigVO);
}
