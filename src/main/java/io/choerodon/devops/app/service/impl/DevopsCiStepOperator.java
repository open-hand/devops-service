package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;

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
        ciStepHandlerMap = devopsCiStepHandlerList.stream().collect(Collectors.toMap(AbstractDevopsCiStepHandler::getType, Function.identity()));
    }

    public AbstractDevopsCiStepHandler getHandler(String Type) {
        return ciStepHandlerMap.get(Type);
    }

    public AbstractDevopsCiStepHandler getHandlerOrThrowE(String Type) {
        AbstractDevopsCiStepHandler handler = getHandler(Type);
        if (handler == null) {
            throw new CommonException("error.unsupported.step.type");
        }
        return handler;
    }

}
