package io.choerodon.devops.app.eventhandler.host;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.host.DockerComposeUpdatePayload;
import io.choerodon.devops.app.service.DevopsDockerInstanceService;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
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
            devopsDockerInstanceService.createOrUpdate(hostId, processPayload);
        });

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.DOCKER_COMPOSE_PROCESS_UPDATE.value();
    }
}
