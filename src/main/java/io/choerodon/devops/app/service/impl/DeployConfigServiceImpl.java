package io.choerodon.devops.app.service.impl;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.deploy.ConfigSettingVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.infra.dto.DeployConfigDTO;
import io.choerodon.devops.infra.dto.governance.NacosListenConfigDTO;
import io.choerodon.devops.infra.feign.operator.GovernanceServiceClientOperator;
import io.choerodon.devops.infra.mapper.DeployConfigMapper;
import io.choerodon.devops.infra.util.JsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.platform.encrypt.EncryptClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.choerodon.devops.app.service.DeployConfigService;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 主机部署文件配置表应用服务默认实现
 *
 * @author jian.zhang02@hand-china.com 2021-08-19 15:43:01
 */
@Service
public class DeployConfigServiceImpl implements DeployConfigService {

    @Autowired
    private DeployConfigMapper deployConfigMapper;

    @Autowired
    private GovernanceServiceClientOperator governanceServiceClientOperator;

    @Autowired
    private EncryptClient encryptClient;

    @Override
    public List<DeployConfigDTO> saveConfigSetting(Long projectId,
                                                   Long devopsDeployRecordId,
                                                   Long instanceId,
                                                   String deployObjectKey,
                                                   Long hostId,
                                                   String instanceName,
                                                   List<ConfigSettingVO> configSettingVOS) {
        if (CollectionUtils.isEmpty(configSettingVOS)) {
            return Collections.emptyList();
        }
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        Date creationDate = new Date();
        List<DeployConfigDTO> deployConfigDTOS = new ArrayList<>();
        configSettingVOS.forEach(configSetting -> {
            DeployConfigDTO deployConfigDTO = new DeployConfigDTO()
                    .setId(null)
                    .setOrganizationId(customUserDetails.getTenantId())
                    .setProjectId(projectId)
                    .setDeployRecordId(devopsDeployRecordId)
                    .setHostId(hostId)
                    .setDeployObjectKey(deployObjectKey)
                    .setInstanceId(instanceId)
                    .setInstanceName(instanceName)
                    .setMountPath(configSetting.getMountPath())
                    .setConfigId(configSetting.getConfigId())
                    .setConfigGroup(configSetting.getConfigGroup())
                    .setConfigCode(configSetting.getConfigCode());
            deployConfigDTO.setCreationDate(creationDate);
            deployConfigMapper.insertSelective(deployConfigDTO);
            deployConfigDTOS.add(deployConfigDTO);
        });
        return deployConfigDTOS;
    }

    @Override
    public String doCreateConfigSettings(Long projectId,
                                         Long instanceId,
                                         String instanceName,
                                         List<ConfigSettingVO> configSettingVOS) {
        if (CollectionUtils.isEmpty(configSettingVOS)) {
            return null;
        }
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        Map<Long, Set<String>> configMountPathMap = configSettingVOS.stream().collect(
                Collectors.groupingBy(ConfigSettingVO::getConfigId, Collectors.mapping(ConfigSettingVO::getMountPath,
                        Collectors.toSet()))
        );
        List<NacosListenConfigDTO> nacosListenConfigs = governanceServiceClientOperator
                .batchQueryListenConfig(customUserDetails.getTenantId(), projectId, configMountPathMap.keySet());

        nacosListenConfigs.forEach(nacosListenConfig -> {
            nacosListenConfig.setMountPaths(configMountPathMap.get(nacosListenConfig.getConfigId()));
            nacosListenConfig.setInstanceName(instanceName);
            nacosListenConfig.setInstanceId(String.valueOf(instanceId));
            nacosListenConfig.setPassword(encryptClient.decrypt(nacosListenConfig.getPassword()));
        });
        return JsonHelper.marshalByJackson(nacosListenConfigs);
    }

    @Override
    public String doCreateConfigSettings(Long hostId) {
        List<DeployConfigDTO> configSettings = deployConfigMapper.queryConfigsByHostId(hostId);
        if (CollectionUtils.isEmpty(configSettings)) {
            return null;
        }
        Map<Long, Map<String, Set<String>>> configSettingsMap =
                configSettings.stream().collect(Collectors.groupingBy(DeployConfigDTO::getConfigId,
                        Collectors.groupingBy(deployConfigDTO -> String.valueOf(deployConfigDTO.getInstanceId()),
                                Collectors.mapping(DeployConfigDTO::getMountPath,
                                Collectors.toSet()))));

        List<NacosListenConfigDTO> nacosListenConfigs = governanceServiceClientOperator.batchQueryListenConfig(
                configSettings.get(0).getOrganizationId(),
                configSettings.get(0).getProjectId(),
                configSettingsMap.keySet());

        nacosListenConfigs.forEach(nacosListenConfig -> {
            nacosListenConfig.setInstanceMountPaths(configSettingsMap.get(nacosListenConfig.getConfigId()));
            nacosListenConfig.setPassword(encryptClient.decrypt(nacosListenConfig.getPassword()));
        });
        return JsonHelper.marshalByJackson(nacosListenConfigs);
    }

    @Override
    public List<ConfigSettingVO> queryDeployConfig(Long projectId, Long recordId, Long instanceId) {
        if (Objects.isNull(recordId) && Objects.isNull(instanceId)) {
            return null;
        }
        List<DeployConfigDTO> deployConfigs = deployConfigMapper.queryDeployConfig(projectId, recordId, instanceId);
        List<ConfigSettingVO> configSettingVOS = new ArrayList<>();
        deployConfigs.forEach(deployConfig -> {
            configSettingVOS.add(new ConfigSettingVO()
                    .setMountPath(deployConfig.getMountPath())
                    .setConfigGroup(deployConfig.getConfigGroup())
                    .setConfigCode(deployConfig.getConfigCode()));
        });
        return configSettingVOS;
    }
}
