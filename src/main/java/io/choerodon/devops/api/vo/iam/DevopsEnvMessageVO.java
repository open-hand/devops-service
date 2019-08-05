package io.choerodon.devops.api.vo.iam;

/**
 * @author lizongwei
 * @date 2019/7/2
 */
public class DevopsEnvMessageVO {

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
