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

    @Override
    public List<DeployConfigDTO> saveConfigSetting(Long projectId, Long devopsDeployRecordId, String deployObjectKey, JarDeployVO jarDeployVO) {
        List<ConfigSettingVO> configSettingVOS = jarDeployVO.getConfigSettingVOS();
        if (CollectionUtils.isEmpty(configSettingVOS)) {
            return Collections.EMPTY_LIST;
        }
        List<DeployConfigDTO> deployConfigDTOS = new ArrayList<>();
        configSettingVOS.forEach(configSetting -> {
            DeployConfigDTO deployConfigDTO = new DeployConfigDTO()
                    .setProjectId(projectId)
                    .setDeployRecordId(devopsDeployRecordId)
                    .setHostId(jarDeployVO.getHostId())
                    .setDeployObjectKey(deployObjectKey)
                    .setInstanceName(jarDeployVO.getName())
                    .setMountPath(configSetting.getMountPath())
                    .setConfigId(configSetting.getConfigId())
                    .setConfigGroup(configSetting.getConfigGroup())
                    .setConfigCode(configSetting.getConfigCode());
            deployConfigMapper.insertSelective(deployConfigDTO);
            deployConfigDTOS.add(deployConfigDTO);
        });
        return deployConfigDTOS;
    }

    @Override
    public String doCreateConfigSetting(Long projectId, JarDeployVO jarDeployVO) {
        List<ConfigSettingVO> configSettingVOS = jarDeployVO.getConfigSettingVOS();
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
            nacosListenConfig.setInstanceName(jarDeployVO.getName());
        });
        return JsonHelper.marshalByJackson(nacosListenConfigs);
    }
}
