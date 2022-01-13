package io.choerodon.devops.app.task;


import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.DevopsRegistrySecretDTO;
import io.choerodon.devops.infra.mapper.DevopsRegistrySecretMapper;
import io.choerodon.devops.infra.util.GenerateUUID;

/**
 * Created by wangxiang on 2022/1/13
 */
@Component
public class RefreshImageAuthenticationTask {
    private static final Logger logger = LoggerFactory.getLogger(RefreshImageAuthenticationTask.class);

    private static final String REFRESH_IMAGE_AUTH = "refreshImageAuth";

    private static final Gson gson = new Gson();

    @Autowired
    private DevopsRegistrySecretMapper devopsRegistrySecretMapper;

    @Autowired
    private HarborService harborService;

    @Autowired
    private AgentCommandService agentCommandService;


    @JobTask(maxRetryCount = 3, code = REFRESH_IMAGE_AUTH, description = "定时刷新镜像认证")
    public void refreshImageAuth(Map<String, Object> map) {
        try {
            //1.查询默认仓库的secret
            DevopsRegistrySecretDTO recordDevopsRegistrySecretDTO = new DevopsRegistrySecretDTO();
            recordDevopsRegistrySecretDTO.setRepoType(HarborRepoConstants.DEFAULT_REPO);
            List<DevopsRegistrySecretDTO> devopsRegistrySecretDTOS = devopsRegistrySecretMapper.select(recordDevopsRegistrySecretDTO);
            if (CollectionUtils.isEmpty(devopsRegistrySecretDTOS)) {
                return;
            }
            List<List<DevopsRegistrySecretDTO>> lists = ListUtils.partition(devopsRegistrySecretDTOS, BaseConstants.PAGE_SIZE);
            lists.forEach(devopsRegistrySecretDTOS1 -> {
                //2.根据harborconfigId 查询仓库配置（查询的及有效的）
                Set<Long> harborConfigIds = devopsRegistrySecretDTOS1.stream().map(DevopsRegistrySecretDTO::getConfigId).collect(Collectors.toSet());

                List<DevopsConfigDTO> devopsConfigDTOS = harborService.queryHarborConfigByHarborConfigIds(harborConfigIds);
                if (CollectionUtils.isEmpty(devopsConfigDTOS)) {
                    return;
                }
                Map<Long, DevopsConfigDTO> devopsConfigDTOMap = devopsConfigDTOS.stream().collect(Collectors.toMap(DevopsConfigDTO::getId, Function.identity()));
                devopsRegistrySecretDTOS1.forEach(devopsRegistrySecretDTO -> {
                    DevopsConfigDTO devopsConfigDTO = devopsConfigDTOMap.get(devopsRegistrySecretDTO.getConfigId());
                    if (devopsConfigDTO == null) {
                        return;
                    }
                    //3.跟新进数据库
                    String secretCode = String.format("%s%s", "secret-", GenerateUUID.generateUUID().substring(0, 20));
                    ConfigVO configVO = gson.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class);
                    devopsRegistrySecretDTO.setSecretCode(secretCode);
                    devopsRegistrySecretDTO.setSecretDetail(gson.toJson(configVO));
                    devopsRegistrySecretMapper.updateByPrimaryKey(devopsRegistrySecretDTO);
                    //4.跟新到agent
                    agentCommandService.operateSecret(devopsRegistrySecretDTO.getClusterId(), devopsRegistrySecretDTO.getNamespace(), secretCode, configVO);
                });

            });


        } catch (Exception e) {
            logger.error("error.refresh.image.auth", e);
        }
    }


}
