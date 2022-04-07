package io.choerodon.devops.app.eventhandler.host;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.vo.host.CommandResultVO;
import io.choerodon.devops.api.vo.host.DockerComposeInfoVO;
import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.api.vo.host.InstanceProcessInfoVO;
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

    private Map<String, Consumer<String>> resultHandlerMap = new HashMap<>();

    @Autowired
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private DevopsHostAppService devopsHostAppService;
    @Autowired
    private DevopsCdPipelineService devopsCdPipelineService;
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
        resultHandlerMap.put(HostCommandEnum.KILL_INSTANCE.value(), payload -> {
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
        Consumer<String> deploy_instance = (payload) -> {
            InstanceProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, InstanceProcessInfoVO.class);
            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            devopsHostAppInstanceDTO.setReady(processInfoVO.getReady());
            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
        };
        resultHandlerMap.put(HostCommandEnum.OPERATE_INSTANCE.value(), deploy_instance);
        resultHandlerMap.put(HostCommandEnum.DEPLOY_MIDDLEWARE.value(), deploy_instance);

        resultHandlerMap.put(HostCommandEnum.REMOVE_DOCKER.value(), payload -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            devopsDockerInstanceService.baseDelete(Long.valueOf(processInfoVO.getInstanceId()));
            devopsHostAppService.baseDelete(devopsDockerInstanceDTO.getAppId());

        });
        Consumer<String> dockerUpdateConsumer = payload -> {
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

        resultHandlerMap.put(HostCommandEnum.KILL_DOCKER_COMPOSE.value(), payload -> {
            DockerComposeInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerComposeInfoVO.class);
            // 删除docker-compose应用数据
            dockerComposeService.deleteAppData(Long.valueOf(processInfoVO.getInstanceId()));
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
            // 使用函数式接口 + 策略模式
            Consumer<String> consumer = resultHandlerMap.get(devopsHostCommandDTO.getCommandType());
            if (consumer != null) {
                consumer.accept(commandResultVO.getPayload());
            }
        } else {
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.FAILED.value());
            devopsHostCommandDTO.setError(commandResultVO.getErrorMsg());
        }
        devopsHostCommandService.baseUpdate(devopsHostCommandDTO);
        if (devopsHostCommandDTO.getCdJobRecordId() != null) {
            devopsCdPipelineService.hostDeployStatusUpdate(devopsHostCommandDTO.getCdJobRecordId(), commandResultVO.getSuccess(), commandResultVO.getErrorMsg());
        }
    }

    @Override
    public String getType() {
        return HostMsgEventEnum.SYNC_COMMAND_STATUS.value();
    }
}
