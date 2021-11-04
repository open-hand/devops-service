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
     * @param instanceId
     * @param deployObjectKey
     * @param hostId
     * @param instanceName
     * @param configSettingVOS
     * @return
     */
    List<DeployConfigDTO> saveConfigSetting(Long projectId,
                                            Long devopsDeployRecordId,
                                            Long instanceId,
                                            String deployObjectKey,
                                            Long hostId,
                                            String instanceName,
                                            List<ConfigSettingVO> configSettingVOS);

    /**
     * 构造发送到agent的配置
     *
     * @return
     * @param projectId
     */
    String doCreateConfigSettings(Long projectId);

    /**
     * 构造发送到agent的配置
     *
     * @param hostId
     * @param instanceId
     * @param instanceName
     * @param configSettingVOS
     * @return
     */
    String doCreateConfigSettings(Long hostId,
                                  Long instanceId,
                                  String instanceName,
                                  List<ConfigSettingVO> configSettingVOS);

    /**
     * 查询配置文件信息
     *
     * @param projectId
     * @param recordId
     * @param instanceId
     * @return
     */
    List<ConfigSettingVO> queryDeployConfig(Long projectId, Long recordId, Long instanceId);
}
