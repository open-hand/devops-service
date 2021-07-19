package io.choerodon.devops.app.eventhandler.host;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.vo.host.CommandResultVO;
import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.api.vo.host.JavaProcessInfoVO;
import io.choerodon.devops.app.service.DevopsDockerInstanceService;
import io.choerodon.devops.app.service.DevopsHostCommandService;
import io.choerodon.devops.app.service.DevopsNormalInstanceService;
import io.choerodon.devops.app.service.HostCommandHandler;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.DevopsNormalInstanceDTO;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * 〈功能简述〉
 * 〈同步devops command执行结果〉
 *
 * @author wanghao
 * @Date 2021/6/28 21:48
 */
@Component
public class CommandResultHandler implements HostMsgHandler {

    private Map<String, HostCommandHandler<DevopsHostCommandDTO, String>> resultHandlerMap = new HashMap<>();

    @Autowired
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    private DevopsNormalInstanceService devopsNormalInstanceService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    void init() {
        resultHandlerMap.put(HostCommandEnum.KILL_JAR.value(), (devopsHostCommandDTO, payload) -> {
            JavaProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, JavaProcessInfoVO.class);
            devopsNormalInstanceService.baseDelete(processInfoVO.getInstanceId());
        });
        resultHandlerMap.put(HostCommandEnum.DEPLOY_JAR.value(), (devopsHostCommandDTO, payload) -> {
            JavaProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, JavaProcessInfoVO.class);
            DevopsNormalInstanceDTO devopsNormalInstanceDTO = devopsNormalInstanceService.baseQuery(processInfoVO.getInstanceId());
            devopsNormalInstanceDTO.setStatus(processInfoVO.getStatus());
            devopsNormalInstanceDTO.setPid(processInfoVO.getPid());
            devopsNormalInstanceDTO.setPort(processInfoVO.getPort());
            devopsNormalInstanceService.baseUpdate(devopsNormalInstanceDTO);
        });
        resultHandlerMap.put(HostCommandEnum.REMOVE_DOCKER.value(), (devopsHostCommandDTO, payload) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            devopsDockerInstanceService.baseDelete(Long.valueOf(processInfoVO.getInstanceId()));
        });
        HostCommandHandler<DevopsHostCommandDTO, String> dockerUpdateConsumer = (devopsHostCommandDTO, payload) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            // 更新状态和容器id
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            devopsDockerInstanceDTO.setContainerId(processInfoVO.getContainerId());
            devopsDockerInstanceDTO.setStatus(processInfoVO.getStatus());
            devopsDockerInstanceService.baseUpdate(devopsDockerInstanceDTO);
        };
        resultHandlerMap.put(HostCommandEnum.STOP_DOCKER.value(), dockerUpdateConsumer);
        resultHandlerMap.put(HostCommandEnum.START_DOCKER.value(), (devopsHostCommandDTO, payload) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            // 更新状态和容器id
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            devopsDockerInstanceDTO.setContainerId(processInfoVO.getContainerId());
            devopsDockerInstanceDTO.setStatus(processInfoVO.getStatus());
            devopsDockerInstanceService.baseUpdate(devopsDockerInstanceDTO);
        });
        resultHandlerMap.put(HostCommandEnum.RESTART_DOCKER.value(), dockerUpdateConsumer);

        resultHandlerMap.put(HostCommandEnum.DEPLOY_DOCKER.value(), dockerUpdateConsumer);
    }


    @Override
    public void handler(String hostId, Long commandId, String payload) {
        DevopsHostCommandDTO devopsHostCommandDTO = devopsHostCommandService.baseQueryById(commandId);
        CommandResultVO commandResultVO = JsonHelper.unmarshalByJackson(payload, CommandResultVO.class);
        if (Boolean.TRUE.equals(commandResultVO.getSuccess())) {
            // 操作成功处理逻辑
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.SUCCESS.value());
            // 使用函数式接口 + 策略模式

            HostCommandHandler<DevopsHostCommandDTO, String> consumer = resultHandlerMap.get(devopsHostCommandDTO.getCommandType());
            if (consumer != null) {
                consumer.accept(devopsHostCommandDTO, commandResultVO.getPayload());
            }
        } else {
            // 操作失败处理逻辑
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
