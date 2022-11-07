package io.choerodon.devops.app.eventhandler.pipeline.exec;

import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_UNSUPPORTED_JOB_TYPE;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.pipeline.CiResponseVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 9:13
 */
@Component
public class CommandOperator {

    @Autowired
    private List<AbstractCiCommandHandler> abstractCiCommandHandlers;

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
}
