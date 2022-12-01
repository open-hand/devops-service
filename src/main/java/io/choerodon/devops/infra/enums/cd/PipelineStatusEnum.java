package io.choerodon.devops.infra.enums.cd;

public enum PipelineStatusEnum {
    /**
     * 已创建
     */
    CREATED("created", 10, false),
    /**
     * 待执行
     */
    PENDING("pending", 20, false),
    /**
     * 成功
     */
    SUCCESS("success", 40, true),
    /**
     * 已取消
     */
    CANCELED("canceled", 50, true),
    /**
     * 失败
     */
    FAILED("failed", 60, true),
    /**
     * 已终止
     */
    STOP("stop", 70, true),

    /**
     * 运行中
     */
    RUNNING("running", 80, false),

    /**
     * 待审核
     */
    NOT_AUDIT("not_audit", 100, false);
//    /**
//     * 已跳过
//     */
//    SKIPPED("skipped");

    private final String value;
    private final Integer priority;
    private Boolean finalStatus;

    PipelineStatusEnum(String value, Integer priority, Boolean finalStatus) {
        this.value = value;
        this.priority = priority;
        this.finalStatus = finalStatus;
    }

    public static Integer getPriorityByValue(String value) {
        Integer priority = 0;
        for (PipelineStatusEnum pipelineStatusEnum : PipelineStatusEnum.values()) {
            if (pipelineStatusEnum.value.equals(value)) {
                priority = pipelineStatusEnum.priority;
                break;
            }
        }
        return priority;
    }

    public static Boolean isFinalStatus(String value) {
        Boolean finalStatus = false;
        for (PipelineStatusEnum pipelineStatusEnum : PipelineStatusEnum.values()) {
            if (pipelineStatusEnum.value.equals(value)) {
                finalStatus = pipelineStatusEnum.finalStatus;
                break;
            }
        }
        return finalStatus;
    }


    public String value() {
        return value;
    }

}

