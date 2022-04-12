package io.choerodon.devops.app.eventhandler.host;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.host.DockerComposeUpdatePayload;
import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.app.service.DevopsDockerInstanceService;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/8 11:36
 */
@Service
public class DockerComposeProcessHandler implements HostMsgHandler {

    @Autowired
    private DevopsDockerInstanceService devopsDockerInstanceService;

    @Override
    public void handler(String hostId, Long commandId, String payload) {
        DockerComposeUpdatePayload dockerProcessUpdatePayload = JsonHelper.unmarshalByJackson(payload, DockerComposeUpdatePayload.class);

        if (CollectionUtils.isEmpty(dockerProcessUpdatePayload.getUpdateProcessInfos())) {
            return;
        }
        dockerProcessUpdatePayload.getUpdateProcessInfos().forEach(processPayload -> {
            Long appId = processPayload.getInstanceId();

            List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOList = devopsDockerInstanceService.listByHostId(appId);

            Map<String, DevopsDockerInstanceDTO> instanceDTOMap = devopsDockerInstanceDTOList.stream().collect(Collectors.toMap(DevopsDockerInstanceDTO::getName, Function.identity()));

            // 处理更新的数据
            List<DockerProcessInfoVO> updateProcessInfos = processPayload.getUpdateProcessInfos();
            if (!CollectionUtils.isEmpty(updateProcessInfos)) {
                updateProcessInfos.forEach(addProcessInfo -> {
                    DevopsDockerInstanceDTO devopsDockerInstanceDTO = instanceDTOMap.get(addProcessInfo.getContainerName());
                    if (devopsDockerInstanceDTO != null) {
                        devopsDockerInstanceDTO.setStatus(addProcessInfo.getStatus());
                        devopsDockerInstanceDTO.setName(addProcessInfo.getContainerName());
                        devopsDockerInstanceDTO.setPorts(addProcessInfo.getPorts());
                        devopsDockerInstanceService.baseUpdate(devopsDockerInstanceDTO);
                    } else {
                        devopsDockerInstanceDTO = ConvertUtils.convertObject(addProcessInfo, DevopsDockerInstanceDTO.class);
                        devopsDockerInstanceDTO.setAppId(appId);
                        devopsDockerInstanceDTO.setName(addProcessInfo.getContainerName());
                        devopsDockerInstanceDTO.setHostId(Long.valueOf(hostId));
                        devopsDockerInstanceDTO.setSourceType(AppSourceType.CUSTOM.getValue());
                        devopsDockerInstanceService.baseCreate(devopsDockerInstanceDTO);
                    }
                });
            }

        });

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.DOCKER_COMPOSE_PROCESS_UPDATE.value();
    }
}
