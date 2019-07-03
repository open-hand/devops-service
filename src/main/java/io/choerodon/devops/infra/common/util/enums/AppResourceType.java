package io.choerodon.devops.infra.common.util.enums;

import io.choerodon.core.exception.CommonException;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
public enum AppResourceType {
    SERVICE("service"),
    SECRET("secret"),
    CONFIGMAP("configMap"),
    INGRESS("ingress");

    private String resourceType;

    AppResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceType() {
        return resourceType;
    }
}
