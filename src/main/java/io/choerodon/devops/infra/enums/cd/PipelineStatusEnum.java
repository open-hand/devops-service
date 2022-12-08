package io.choerodon.devops.infra.enums.cd;

public enum PipelineStatusEnum {
    /**
     * 已跳过
     */
    SKIPPED("skipped", 10, true),
    /**
     * 成功
     */
    SUCCESS("success", 20, true),


    /**
     * 已创建
     */
    CREATED("created", 30, false),
    /**
     * 待执行
     */
    PENDING("pending", 40, false),


    /**
     * 失败
     */
    FAILED("failed", 50, true),
    /**
     * 已取消
     */
    CANCELED("canceled", 60, true),
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

    public static boolean isFinalStatus(String value) {
        boolean finalStatus = false;
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

