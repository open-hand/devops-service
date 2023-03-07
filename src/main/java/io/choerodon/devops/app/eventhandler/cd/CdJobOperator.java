package io.choerodon.devops.app.eventhandler.cd;

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
public class CdJobOperator {

    @Autowired
    private List<AbstractCdJobHandler> abstractJobHandlerList;

    private Map<String, AbstractCdJobHandler> jobHandlerMap;

    @PostConstruct
    void init() {
        jobHandlerMap = abstractJobHandlerList.stream().collect(Collectors.toMap(v -> v.getType().value(), Function.identity()));
    }

    public AbstractCdJobHandler getHandler(String type) {
        return jobHandlerMap.get(type);
    }

    public AbstractCdJobHandler getHandlerOrThrowE(String type) {
        AbstractCdJobHandler handler = jobHandlerMap.get(type);
        if (handler == null) {
            throw new CommonException(DEVOPS_UNSUPPORTED_JOB_TYPE, type);
        }
        return handler;
    }

}
