package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.deploy.ConfigSettingVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.infra.dto.DeployConfigDTO;

import java.util.List;


/**
 * 主机部署文件配置表应用服务
 *
 * @author jian.zhang02@hand-china.com 2021-08-19 15:43:01
 */
public interface DeployConfigService {
    /**
     * 保存部署配置文件信息
     *
     * @param projectId
     * @param devopsDeployRecordId
     * @param deployObjectKey
     * @param jarDeployVO
     * @return
     */
    List<DeployConfigDTO> saveConfigSetting(Long projectId, Long devopsDeployRecordId, String deployObjectKey, JarDeployVO jarDeployVO);

    /**
     * 构造发送到agent的配置
     *
     * @return
     * @param projectId
     * @param jarDeployVO
     */
    String doCreateConfigSetting(Long projectId, JarDeployVO jarDeployVO);
}
