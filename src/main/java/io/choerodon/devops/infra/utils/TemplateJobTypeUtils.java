package io.choerodon.devops.infra.utils;

import java.util.HashMap;
import java.util.Map;

import io.choerodon.devops.infra.enums.CiJobTypeEnum;

public final class TemplateJobTypeUtils {
    public static Map<String, String> stringStringMap = new HashMap<>(5);
    private static final String CHART = "chart";
    private static final String API_TEST = "apiTest";
    private static final String AUDIT = "audit";
    private static final String DEPLOYMENT = "deployment";
    private static final String HOST = "host";

    static {
        stringStringMap.put(CiJobTypeEnum.CHART_DEPLOY.value(), CHART);
        stringStringMap.put(CiJobTypeEnum.API_TEST.value(), API_TEST);
        stringStringMap.put(CiJobTypeEnum.AUDIT.value(), AUDIT);
        stringStringMap.put(CiJobTypeEnum.DEPLOYMENT_DEPLOY.value(), DEPLOYMENT);
        stringStringMap.put(CiJobTypeEnum.HOST_DEPLOY.value(), HOST);
    }
}
