package io.choerodon.devops.app.eventhandler.pipeline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/24 14:38
 */
@Component
public class CiUnitTestReportOperator {
    @Autowired
    private List<CiUnitTestReportHandler> ciUnitTestReportHandlers;

    private Map<String, CiUnitTestReportHandler> handlerMap = new HashMap<>();

    @PostConstruct
    void init() {
        ciUnitTestReportHandlers.forEach(v -> handlerMap.put(v.getType().value(), v));
    }

    public CiUnitTestReportHandler getHandlerByType(String type) {
        return handlerMap.get(type);
    }
}
