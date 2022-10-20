package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_UNSUPPORTED_STEP_TYPE;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 10:24
 */
@Component
public class DevopsCiStepOperator {


    @Autowired
    private List<AbstractDevopsCiStepHandler> devopsCiStepHandlerList;

    private Map<String, AbstractDevopsCiStepHandler> ciStepHandlerMap;

    @PostConstruct
    void init() {
        ciStepHandlerMap = devopsCiStepHandlerList.stream()
                .collect(Collectors.toMap(v -> v.getType().value(), Function.identity()));
    }

    public AbstractDevopsCiStepHandler getHandler(String type) {
        return ciStepHandlerMap.get(type);
    }

    public AbstractDevopsCiStepHandler getHandlerOrThrowE(String type) {
        AbstractDevopsCiStepHandler handler = getHandler(type);
        if (handler == null) {
            throw new CommonException(DEVOPS_UNSUPPORTED_STEP_TYPE, type);
        }
        return handler;
    }

}
