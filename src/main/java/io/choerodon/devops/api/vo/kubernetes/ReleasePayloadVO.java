package io.choerodon.devops.api.vo.kubernetes;


import io.swagger.annotations.ApiModelProperty;

public class ReleasePayloadVO {
    @ApiModelProperty("helm release的名称")
    private String name;

    @ApiModelProperty("helm release的revision")
    private Long revision;

    @ApiModelProperty("命名空间")
    private String namespace;

    @ApiModelProperty("release的状态")
    private String status;

    @ApiModelProperty("release的chart名称")
    private String chartName;

    @ApiModelProperty("release的chart版本")
    private String chartVersion;

    private String config;

    // 这个字段没有在返回的消息中看见
    private String hooks;

    private String resources;

    @ApiModelProperty("实例对应的commandId")
    private Long command;

    @ApiModelProperty("C7nHelmRelease的Annotation中的commit sha值")
    private String commit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(String chartVersion) {
        this.chartVersion = chartVersion;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getHooks() {
        return hooks;
    }

    public void setHooks(String hooks) {
        this.hooks = hooks;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public Long getCommand() {
        return command;
    }

    public void setCommand(Long command) {
        this.command = command;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }
}
