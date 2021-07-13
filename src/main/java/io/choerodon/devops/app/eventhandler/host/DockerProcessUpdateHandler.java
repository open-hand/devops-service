package io.choerodon.devops.app.eventhandler.host;

import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.api.vo.host.DockerProcessUpdatePayload;
import io.choerodon.devops.app.service.DevopsDockerInstanceService;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.JsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/27 20:35
 */
@Component
public class DockerProcessUpdateHandler implements HostMsgHandler{

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DevopsDockerInstanceService devopsDockerInstanceService;

    @Override
    public void handler(String hostId, Long commandId, String payload) {
        DockerProcessUpdatePayload dockerProcessUpdatePayload = JsonHelper.unmarshalByJackson(payload, DockerProcessUpdatePayload.class);

        List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOList = devopsDockerInstanceService.listByHostId(Long.valueOf(hostId));
        // 为空不处理
        if (CollectionUtils.isEmpty(devopsDockerInstanceDTOList)) {
            return;
        }
        Map<Long, DevopsDockerInstanceDTO> instanceDTOMap = devopsDockerInstanceDTOList.stream().collect(Collectors.toMap(DevopsDockerInstanceDTO::getId, Function.identity()));

        // 处理更新的数据
        List<DockerProcessInfoVO> updateProcessInfos = dockerProcessUpdatePayload.getUpdateProcessInfos();
        updateProcessInfos.forEach(addProcessInfo -> {
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = instanceDTOMap.get(addProcessInfo.getInstanceId());
            if (devopsDockerInstanceDTO != null) {
                devopsDockerInstanceDTO.setStatus(addProcessInfo.getStatus());
                devopsDockerInstanceService.baseUpdate(devopsDockerInstanceDTO);
            }
        });

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.DOCKER_PROCESS_UPDATE.value();
    }
}
