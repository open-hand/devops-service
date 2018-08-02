package io.choerodon.devops.domain.application.valueobject;

public class C7nHelmRelease {

    private String apiVersion;
    private String kind;
    private Metadata metadata;
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
