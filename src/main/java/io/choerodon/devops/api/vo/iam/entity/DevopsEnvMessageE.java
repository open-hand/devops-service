package io.choerodon.devops.api.vo.iam.entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author lizongwei
 * @date 2019/7/2
 */
@Component
@Scope("prototype")
public class DevopsEnvMessageE {

    private String resourceName;

    private String detail;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
