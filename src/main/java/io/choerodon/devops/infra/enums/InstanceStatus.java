package io.choerodon.devops.infra.enums;

/**
 * Creator: Runge
 * Date: 2018/4/26
 * Time: 15:38
 * Description:
 */
public enum InstanceStatus {
    /**
     * 处理中
     */
    OPERATING("operating"),
    /**
     * 运行中
     */
    RUNNING("running"),
    /**
     * 失败
     */
    FAILED("failed"),
    /**
     * 停用
     */
    STOPPED("stopped"),
    /**
     * 删除
     */
    DELETED("deleted");


    private String status;

    InstanceStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
