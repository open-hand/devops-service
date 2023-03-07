package io.choerodon.devops.app.eventhandler.host;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.vo.host.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsHostAppInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
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

    protected Map<String, BiConsumer<String, String>> resultHandlerMap = new HashMap<>();

    @Autowired
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private DevopsHostAppService devopsHostAppService;
    @Autowired
    private DevopsHostAppInstanceService devopsHostAppInstanceService;
    @Autowired
    private DevopsMiddlewareService devopsMiddlewareService;
    @Autowired
    private DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    private DockerComposeService dockerComposeService;


    @PostConstruct
    void init() {
        resultHandlerMap.put(HostCommandEnum.KILL_INSTANCE.value(), (hostId, payload) -> {
            InstanceProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, InstanceProcessInfoVO.class);
            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            if (devopsHostAppInstanceDTO != null) {
                devopsHostAppInstanceService.baseDelete(Long.valueOf(processInfoVO.getInstanceId()));
                devopsHostAppService.baseDelete(devopsHostAppInstanceDTO.getAppId());
                if (AppSourceType.MIDDLEWARE.getValue().equals(devopsHostAppInstanceDTO.getSourceType())) {
                    devopsMiddlewareService.deleteByInstanceId(Long.valueOf(processInfoVO.getInstanceId()));
                }
            }
        });
        BiConsumer<String, String> deployInstance = (hostId, payload) -> {
            InstanceProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, InstanceProcessInfoVO.class);
            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            devopsHostAppInstanceDTO.setReady(processInfoVO.getReady());
            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
        };
        resultHandlerMap.put(HostCommandEnum.OPERATE_INSTANCE.value(), deployInstance);
        resultHandlerMap.put(HostCommandEnum.DEPLOY_MIDDLEWARE.value(), deployInstance);

        resultHandlerMap.put(HostCommandEnum.REMOVE_DOCKER.value(), (hostId, payload) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            devopsDockerInstanceService.baseDelete(Long.valueOf(processInfoVO.getInstanceId()));
            devopsHostAppService.baseDelete(devopsDockerInstanceDTO.getAppId());

        });
        BiConsumer<String, String> dockerUpdateConsumer = (hostId, payload) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            // 更新状态和容器id
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            devopsDockerInstanceDTO.setContainerId(processInfoVO.getContainerId());
            devopsDockerInstanceDTO.setStatus(processInfoVO.getStatus());
            devopsDockerInstanceDTO.setPorts(processInfoVO.getPorts());
            devopsDockerInstanceService.baseUpdate(devopsDockerInstanceDTO);
        };

        resultHandlerMap.put(HostCommandEnum.STOP_DOCKER.value(), dockerUpdateConsumer);
        resultHandlerMap.put(HostCommandEnum.START_DOCKER.value(), dockerUpdateConsumer);
        resultHandlerMap.put(HostCommandEnum.RESTART_DOCKER.value(), dockerUpdateConsumer);
        resultHandlerMap.put(HostCommandEnum.DEPLOY_DOCKER.value(), dockerUpdateConsumer);


        BiConsumer<String, String> dockerInComposeUpdateConsumer = (hostId, payload) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            // 更新状态和容器id
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));

            if (devopsDockerInstanceDTO != null) {
                devopsDockerInstanceDTO.setStatus(processInfoVO.getStatus());
                devopsDockerInstanceDTO.setPorts(processInfoVO.getPorts());
                devopsDockerInstanceDTO.setContainerId(processInfoVO.getContainerId());
                devopsDockerInstanceDTO.setImage(processInfoVO.getImage());
                devopsDockerInstanceService.baseUpdate(devopsDockerInstanceDTO);
            }

        };

        resultHandlerMap.put(HostCommandEnum.DEPLOY_DOCKER_COMPOSE.value(), (hostId, payload) -> {
            DockerProcessUpdatePayload dockerProcessUpdatePayload = JsonHelper.unmarshalByJackson(payload, DockerProcessUpdatePayload.class);
            // 删除docker-compose应用数据
            devopsDockerInstanceService.createOrUpdate(hostId, dockerProcessUpdatePayload);
        });

        resultHandlerMap.put(HostCommandEnum.KILL_DOCKER_COMPOSE.value(), (hostId, payload) -> {
            DockerComposeInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerComposeInfoVO.class);
            // 删除docker-compose应用数据
            dockerComposeService.deleteAppData(Long.valueOf(processInfoVO.getInstanceId()));
        });
        resultHandlerMap.put(HostCommandEnum.START_DOCKER_IN_COMPOSE.value(), dockerInComposeUpdateConsumer);
        resultHandlerMap.put(HostCommandEnum.RESTART_DOCKER_IN_COMPOSE.value(), dockerInComposeUpdateConsumer);
        resultHandlerMap.put(HostCommandEnum.STOP_DOCKER_IN_COMPOSE.value(), dockerInComposeUpdateConsumer);
        resultHandlerMap.put(HostCommandEnum.REMOVE_DOCKER_IN_COMPOSE.value(), (hostId, payload) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            if (devopsDockerInstanceDTO != null) {
                devopsDockerInstanceService.baseDelete(devopsDockerInstanceDTO.getId());
            }
        });
    }


    @Override
    public void handler(String hostId, Long commandId, String payload) {
        CommandResultVO commandResultVO = JsonHelper.unmarshalByJackson(payload, CommandResultVO.class);
        handler(hostId, commandId, commandResultVO);
    }

    public void handler(String hostId, Long commandId, CommandResultVO commandResultVO) {
        DevopsHostCommandDTO devopsHostCommandDTO = devopsHostCommandService.baseQueryById(commandId);

        if (Boolean.TRUE.equals(commandResultVO.getSuccess())) {
            // 操作成功处理逻辑
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.SUCCESS.value());
            devopsHostCommandDTO.setError(commandResultVO.getErrorMsg());
            // 使用函数式接口 + 策略模式
            BiConsumer<String, String> consumer = resultHandlerMap.get(devopsHostCommandDTO.getCommandType());
            if (consumer != null) {
                consumer.accept(hostId, commandResultVO.getPayload());
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
