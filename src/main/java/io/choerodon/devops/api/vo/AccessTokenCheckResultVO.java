package io.choerodon.devops.api.vo;

/**
 * access token 校验结果
 */
public class AccessTokenCheckResultVO {
    private Boolean failed;
    private String failedType;

    public Boolean getFailed() {
        return failed;
    }

    public void setFailed(Boolean failed) {
        this.failed = failed;
    }

    public String getFailedType() {
        return failedType;
    }

    public void setFailedType(String failedType) {
        this.failedType = failedType;
    }
}
