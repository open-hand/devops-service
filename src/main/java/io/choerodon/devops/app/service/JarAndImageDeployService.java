package io.choerodon.devops.app.service;


import org.springframework.security.core.Authentication;

import io.choerodon.devops.api.vo.deploy.DeployConfigVO;

/**
 * Created by wangxiang on 2021/5/6
 */
public interface JarAndImageDeployService {
    void jarAndImageDeploy(Long projectId, DeployConfigVO deployConfigVO, Authentication authentication);

}
