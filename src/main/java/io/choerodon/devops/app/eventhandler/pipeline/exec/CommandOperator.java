package io.choerodon.devops.app.eventhandler.pipeline.exec;

import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_UNSUPPORTED_JOB_TYPE;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.pipeline.CiResponseVO;
import io.choerodon.devops.app.service.DevopsHostCommandService;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 9:13
 */
@Component
public class CommandOperator {

    @Value("${devops.ci.host-deploy-timeout}")
    private Integer deployTimeout;

    @Autowired
    private List<AbstractCiCommandHandler> abstractCiCommandHandlers;

    @Autowired
    private DevopsHostCommandService devopsHostCommandService;

    private Map<String, AbstractCiCommandHandler> commandHandlerMap;

    @PostConstruct
    void init() {
        commandHandlerMap = abstractCiCommandHandlers.stream().collect(Collectors.toMap(v -> v.getType().value(), Function.identity()));
    }

    public AbstractCiCommandHandler getHandler(String type) {
        return commandHandlerMap.get(type);
    }

    public AbstractCiCommandHandler getHandlerOrThrowE(String type) {
        AbstractCiCommandHandler handler = commandHandlerMap.get(type);
        if (handler == null) {
            throw new CommonException(DEVOPS_UNSUPPORTED_JOB_TYPE, type);
        }
        return handler;
    }

    public CiResponseVO executeCommandByType(String token, Long gitlabPipelineId, Long gitlabJobId, Long configId, String commandType) {
        AbstractCiCommandHandler handler = getHandlerOrThrowE(commandType);
        return handler.executeCommand(token, gitlabPipelineId, gitlabJobId, configId);
    }

    public CiResponseVO getHostCommandStatus(String token, Long gitlabPipelineId, Long commandId) {
        CiResponseVO ciResponseVO = new CiResponseVO();
        DevopsHostCommandDTO devopsHostCommandDTO = devopsHostCommandService.baseQueryById(commandId);
        Map<String, Object> result = new HashMap<>();
        if (devopsHostCommandDTO != null && devopsHostCommandDTO.getCiPipelineRecordId().equals(gitlabPipelineId)) {
            result.put("status", devopsHostCommandDTO.getStatus());
            switch (HostCommandStatusEnum.valueOf(devopsHostCommandDTO.getStatus().toUpperCase())) {
                case OPERATING:
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(devopsHostCommandDTO.getCreationDate());
                    calendar.add(Calendar.MINUTE, deployTimeout);
                    if (calendar.getTime().before(new Date())) {
                        ciResponseVO.setFailed(true);
                        ciResponseVO.setMessage("Timeout");
                    } else {
                        ciResponseVO.setFailed(false);
                    }
                    break;
                case FAILED:
                    ciResponseVO.setFailed(true);
                    result.put("errorMsg", devopsHostCommandDTO.getError());
                    break;
                case SUCCESS:
                    ciResponseVO.setFailed(false);
                    result.put("errorMsg", devopsHostCommandDTO.getError());
                    break;
            }
        } else {
            ciResponseVO.setFailed(true);
            ciResponseVO.setMessage(String.format("Mismatched pipeline and host command,gitlabPipelineId:%s commandId:%s", gitlabPipelineId, commandId));
        }
        ciResponseVO.setContent(result);
        return ciResponseVO;
    }
}
