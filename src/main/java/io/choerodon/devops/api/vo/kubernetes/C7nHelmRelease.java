package io.choerodon.devops.api.vo.kubernetes;

import io.swagger.annotations.ApiModelProperty;

public class C7nHelmRelease {

    @ApiModelProperty("api版本")
    private String apiVersion;
    @ApiModelProperty("类型")
    private String kind;
    @ApiModelProperty("对象元数据")
    private Metadata metadata;
    @ApiModelProperty("对象配置内容")
    private Spec spec;

    /**
     * Constructor
     */
    public C7nHelmRelease() {
        this.apiVersion = "choerodon.io/v1alpha1";
        this.kind = "C7NHelmRelease";
        this.metadata = new Metadata();
        this.spec = new Spec();
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }
}
