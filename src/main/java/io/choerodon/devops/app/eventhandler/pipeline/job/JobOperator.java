package io.choerodon.devops.app.eventhandler.pipeline.job;

import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_UNSUPPORTED_JOB_TYPE;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 9:13
 */
@Component
public class JobOperator {

    @Autowired
    private List<AbstractJobHandler> abstractJobHandlerList;

    private Map<String, AbstractJobHandler> jobHandlerMap;

    @PostConstruct
    void init() {
        jobHandlerMap = abstractJobHandlerList.stream().collect(Collectors.toMap(v -> v.getType().value(), Function.identity()));
    }

    public AbstractJobHandler getHandler(String type) {
        return jobHandlerMap.get(type);
    }

    public AbstractJobHandler getHandlerOrThrowE(String type) {
        AbstractJobHandler handler = jobHandlerMap.get(type);
        if (handler == null) {
            throw new CommonException(DEVOPS_UNSUPPORTED_JOB_TYPE, type);
        }
        return handler;
    }

}
