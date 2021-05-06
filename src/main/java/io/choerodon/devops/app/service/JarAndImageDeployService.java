package io.choerodon.devops.app.service;

import net.schmizz.sshj.SSHClient;

import io.choerodon.devops.api.vo.deploy.DeployConfigVO;
import io.choerodon.devops.infra.dto.repo.C7nImageDeployDTO;

/**
 * Created by wangxiang on 2021/5/6
 */
public interface JarAndImageDeployService {
    void jarAndImageDeploy(Long projectId, DeployConfigVO deployConfigVO, Long userId);

}
