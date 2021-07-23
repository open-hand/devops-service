package io.choerodon.devops.app.eventhandler.host;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.vo.host.CommandResultVO;
import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.api.vo.host.JavaProcessInfoVO;
import io.choerodon.devops.api.vo.host.MiddlewareDeployVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.DevopsNormalInstanceDTO;
import io.choerodon.devops.infra.enums.CommandStatus;
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
    private DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    private DevopsNormalInstanceService devopsNormalInstanceService;
    @Autowired
    private DevopsCdPipelineService devopsCdPipelineService;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;

    @PostConstruct
    void init() {
        resultHandlerMap.put(HostCommandEnum.KILL_JAR.value(), (payload) -> {
            JavaProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, JavaProcessInfoVO.class);
            devopsNormalInstanceService.baseDelete(Long.valueOf(processInfoVO.getInstanceId()));
        });
        resultHandlerMap.put(HostCommandEnum.DEPLOY_JAR.value(), (payload) -> {
            JavaProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, JavaProcessInfoVO.class);
            DevopsNormalInstanceDTO devopsNormalInstanceDTO = devopsNormalInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            devopsNormalInstanceDTO.setStatus(processInfoVO.getStatus());
            devopsNormalInstanceDTO.setPid(processInfoVO.getPid());
            devopsNormalInstanceDTO.setPorts(processInfoVO.getPorts());
            devopsNormalInstanceService.baseUpdate(devopsNormalInstanceDTO);
        });
        resultHandlerMap.put(HostCommandEnum.REMOVE_DOCKER.value(), (payload) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            devopsDockerInstanceService.baseDelete(Long.valueOf(processInfoVO.getInstanceId()));
        });
        Consumer<String> dockerUpdateConsumer = (payload) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            // 更新状态和容器id
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            devopsDockerInstanceDTO.setContainerId(processInfoVO.getContainerId());
            devopsDockerInstanceDTO.setStatus(processInfoVO.getStatus());
            devopsDockerInstanceDTO.setPorts(processInfoVO.getPorts());
            devopsDockerInstanceService.baseUpdate(devopsDockerInstanceDTO);
        };
        resultHandlerMap.put(HostCommandEnum.STOP_DOCKER.value(), dockerUpdateConsumer);
        resultHandlerMap.put(HostCommandEnum.START_DOCKER.value(), (payload) -> {
            DockerProcessInfoVO processInfoVO = JsonHelper.unmarshalByJackson(payload, DockerProcessInfoVO.class);
            // 更新状态和容器id
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.baseQuery(Long.valueOf(processInfoVO.getInstanceId()));
            devopsDockerInstanceDTO.setContainerId(processInfoVO.getContainerId());
            devopsDockerInstanceDTO.setStatus(processInfoVO.getStatus());
            devopsDockerInstanceDTO.setPorts(processInfoVO.getPorts());
            devopsDockerInstanceService.baseUpdate(devopsDockerInstanceDTO);
        });
        resultHandlerMap.put(HostCommandEnum.RESTART_DOCKER.value(), dockerUpdateConsumer);

        resultHandlerMap.put(HostCommandEnum.DEPLOY_DOCKER.value(), dockerUpdateConsumer);
    }


    @Override
    public void handler(String hostId, Long commandId, String payload) {
        DevopsHostCommandDTO devopsHostCommandDTO = devopsHostCommandService.baseQueryById(commandId);
        CommandResultVO commandResultVO = JsonHelper.unmarshalByJackson(payload, CommandResultVO.class);
        // 中间件单独处理
        if (devopsHostCommandDTO.getCommandType().equals(HostCommandEnum.DEPLOY_MIDDLEWARE.value())) {
            MiddlewareDeployVO middlewareDeployVO = JsonHelper.unmarshalByJackson(commandResultVO.getPayload(), MiddlewareDeployVO.class);
            String status = Boolean.TRUE.equals(commandResultVO.getSuccess()) ? CommandStatus.SUCCESS.getStatus() : CommandStatus.FAILED.getStatus();
            devopsDeployRecordService.updateRecord(Long.valueOf(middlewareDeployVO.getRecordId()), status, commandResultVO.getErrorMsg());
        } else {
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
    }

    @Override
    public String getType() {
        return HostMsgEventEnum.SYNC_COMMAND_STATUS.value();
    }
}
