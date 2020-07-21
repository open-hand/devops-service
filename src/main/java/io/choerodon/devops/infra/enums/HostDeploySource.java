package io.choerodon.devops.infra.enums;

/**
 * @author scp
 * @date 2020/7/21
 * @description
 */
public enum HostDeploySource {
    /**
     * 匹配部署
     */
    MATCH_DEPLOY("matchDeploy"),
    /**
     * 流水线制品部署
     */
    PIPELINE_DEPLOY("pipelineDeploy");
    private String value;

    HostDeploySource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
