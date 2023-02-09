package io.choerodon.devops.infra.enums;

/**
 * @author lihao
 */
public enum ApprovalTypeEnum {
    /**
     * 应用流水线类型
     */
    CI_PIPELINE("ci_pipeline"),
    /**
     * 持续部署流水线类型
     */
    CD_PIPELINE("cd_pipeline"),
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
