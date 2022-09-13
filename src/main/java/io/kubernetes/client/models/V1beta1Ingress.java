//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(
        description = "Ingress is a collection of rules that allow inbound connections to reach the endpoints defined by a backend. An Ingress can be configured to give services externally-reachable urls, load balance traffic, terminate SSL, offer name based virtual hosting etc."
)
public class V1beta1Ingress implements KubernetesObject {
    @SerializedName("apiVersion")
    private String apiVersion = null;
    @SerializedName("kind")
    private String kind = null;
    @SerializedName("metadata")
    private V1ObjectMeta metadata = null;
    @SerializedName("spec")
    private V1beta1IngressSpec spec = null;
    @SerializedName("status")
    private V1beta1IngressStatus status = null;

    public V1beta1Ingress() {
    }

    public V1beta1Ingress apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @ApiModelProperty("APIVersion defines the versioned schema of this representation of an object. Servers should convert recognized schemas to the latest internal value, and may reject unrecognized values. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#resources")
    public String getApiVersion() {
        return this.apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public V1beta1Ingress kind(String kind) {
        this.kind = kind;
        return this;
    }

    @ApiModelProperty("Kind is a string value representing the REST resource this object represents. Servers may infer this from the endpoint the client submits requests to. Cannot be updated. In CamelCase. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#types-kinds")
    public String getKind() {
        return this.kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public V1beta1Ingress metadata(V1ObjectMeta metadata) {
        this.metadata = metadata;
        return this;
    }

    @ApiModelProperty("Standard object's metadata. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#metadata")
    public V1ObjectMeta getMetadata() {
        return this.metadata;
    }

    public void setMetadata(V1ObjectMeta metadata) {
        this.metadata = metadata;
    }

    public V1beta1Ingress spec(V1beta1IngressSpec spec) {
        this.spec = spec;
        return this;
    }

    @ApiModelProperty("Spec is the desired state of the Ingress. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#spec-and-status")
    public V1beta1IngressSpec getSpec() {
        return this.spec;
    }

    public void setSpec(V1beta1IngressSpec spec) {
        this.spec = spec;
    }

    public V1beta1Ingress status(V1beta1IngressStatus status) {
        this.status = status;
        return this;
    }

    @ApiModelProperty("Status is the current state of the Ingress. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#spec-and-status")
    public V1beta1IngressStatus getStatus() {
        return this.status;
    }

    public void setStatus(V1beta1IngressStatus status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1beta1Ingress v1beta1Ingress = (V1beta1Ingress) o;
            return Objects.equals(this.apiVersion, v1beta1Ingress.apiVersion) && Objects.equals(this.kind, v1beta1Ingress.kind) && Objects.equals(this.metadata, v1beta1Ingress.metadata) && Objects.equals(this.spec, v1beta1Ingress.spec) && Objects.equals(this.status, v1beta1Ingress.status);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.apiVersion, this.kind, this.metadata, this.spec, this.status});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1beta1Ingress {\n");
        sb.append("    apiVersion: ").append(this.toIndentedString(this.apiVersion)).append("\n");
        sb.append("    kind: ").append(this.toIndentedString(this.kind)).append("\n");
        sb.append("    metadata: ").append(this.toIndentedString(this.metadata)).append("\n");
        sb.append("    spec: ").append(this.toIndentedString(this.spec)).append("\n");
        sb.append("    status: ").append(this.toIndentedString(this.status)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
