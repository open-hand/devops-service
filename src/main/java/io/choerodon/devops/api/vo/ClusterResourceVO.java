package io.choerodon.devops.api.vo;

/**
 * @author: 25499
 * @date: 2019/10/30 13:59
 * @description:
 */
public class ClusterResourceVO {
    private  String status;

    private String message;

    public ClusterResourceVO() {
    }

    public ClusterResourceVO(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
