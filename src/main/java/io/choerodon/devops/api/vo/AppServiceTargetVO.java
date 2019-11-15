package io.choerodon.devops.api.vo;

public class AppServiceTargetVO {
    private Long targetAppServiceId;
    private String targetAppServiceCode;
    private String status;

    public Long getTargetAppServiceId() {
        return targetAppServiceId;
    }

    public void setTargetAppServiceId(Long targetAppServiceId) {
        this.targetAppServiceId = targetAppServiceId;
    }

    public String getTargetAppServiceCode() {
        return targetAppServiceCode;
    }

    public void setTargetAppServiceCode(String targetAppServiceCode) {
        this.targetAppServiceCode = targetAppServiceCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
