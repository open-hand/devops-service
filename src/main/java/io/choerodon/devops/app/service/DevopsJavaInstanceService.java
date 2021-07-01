package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.deploy.JarDeployVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
public interface DevopsJavaInstanceService {
    /**
     * 部署java应用
     * @param projectId
     * @param jarDeployVO
     */
    void deployJavaInstance(Long projectId, JarDeployVO jarDeployVO);
}
