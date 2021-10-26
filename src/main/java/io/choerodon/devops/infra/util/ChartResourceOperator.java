package io.choerodon.devops.infra.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.ChartResourceOperatorService;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/9 17:11
 */
@Component
public class ChartResourceOperator {

    private final Map<String, ChartResourceOperatorService> chartResourceOperatorServiceMap = new HashMap<>();
    @Autowired
    private List<ChartResourceOperatorService> chartResourceOperatorServices;

    @PostConstruct
    private void initialize() {
        chartResourceOperatorServices.forEach(chartResourceOperatorService -> chartResourceOperatorServiceMap.put(chartResourceOperatorService.getType().getType(), chartResourceOperatorService));
    }

    public Map<String, ChartResourceOperatorService> getOperatorMap() {
        return this.chartResourceOperatorServiceMap;
    }
}
