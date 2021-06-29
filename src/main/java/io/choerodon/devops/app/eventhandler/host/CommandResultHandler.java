package io.choerodon.devops.app.eventhandler.host;

import io.choerodon.devops.api.vo.host.CommandResultVO;
import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.api.vo.host.JavaProcessInfoVO;
import io.choerodon.devops.app.service.DevopsHostCommandService;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.JsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ObjLongConsumer;

/**
 * 〈功能简述〉
 * 〈同步devops command执行结果〉
 *
 * @author wanghao
 * @Date 2021/6/28 21:48
 */
@Component
public class CommandResultHandler implements HostMsgHandler {

    private Map<String, ObjLongConsumer<String>> resultHandlerMap = new HashMap<>();

    @Autowired
    private DevopsHostCommandService devopsHostCommandService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    void init() {
        resultHandlerMap.put(HostCommandEnum.KILL_JAR.value(), (payload, hostId) -> {
            JavaProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, JavaProcessInfoVO.class);
            redisTemplate.opsForHash().delete(String.format(DevopsHostConstants.HOST_JAVA_PROCESS_INFO_KEY, hostId), processInfoVO.getPid());
        });
        resultHandlerMap.put(HostCommandEnum.REMOVE_DOCKER.value(), (payload, hostId) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            redisTemplate.opsForHash().delete(String.format(DevopsHostConstants.HOST_DOCKER_PROCESS_INFO_KEY, hostId), processInfoVO.getContainerId());
        });
        ObjLongConsumer<String> dockerUpdateConsumer = (payload, hostId) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            redisTemplate.opsForHash().put(String.format(DevopsHostConstants.HOST_DOCKER_PROCESS_INFO_KEY, hostId), processInfoVO.getContainerId(), processInfoVO);
        };
        resultHandlerMap.put(HostCommandEnum.STOP_DOCKER.value(), dockerUpdateConsumer);
        resultHandlerMap.put(HostCommandEnum.START_DOCKER.value(), dockerUpdateConsumer);
        resultHandlerMap.put(HostCommandEnum.RESTART_DOCKER.value(), dockerUpdateConsumer);
    }


    @Override
    public void handler(Long hostId, Long commandId, String payload) {
        DevopsHostCommandDTO devopsHostCommandDTO = devopsHostCommandService.baseQueryById(commandId);
        CommandResultVO commandResultVO = JsonHelper.unmarshalByJackson(payload, CommandResultVO.class);
        if (Boolean.TRUE.equals(commandResultVO.getSuccess())) {
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.SUCCESS.value());
            ObjLongConsumer<String> consumer = resultHandlerMap.get(devopsHostCommandDTO.getCommandType());
            if (consumer != null) {
                consumer.accept(commandResultVO.getPayload(), hostId);
            }
        } else {
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.FAILED.value());
            devopsHostCommandDTO.setError(commandResultVO.getErrorMsg());
        }
        devopsHostCommandService.baseUpdate(devopsHostCommandDTO);
    }

    @Override
    public String getType() {
        return HostMsgEventEnum.SYNC_COMMAND_STATUS.value();
    }
}
