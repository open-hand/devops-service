package io.choerodon.devops.infra.enums;

/**
 * @author lihao
 */
public enum ApprovalTypeEnum {
    /**
     * ci_pipeline
     */
    CI_PIPELINE("ci_pipeline"),
    /**
     * 流水线类型
     */
    PIPELINE("pipeline"),
    /**
     * 分支合并类型
     */
    MERGE_REQUEST("merge_request");
    private String type;

    ApprovalTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
