//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

@ApiModel(
    description = "HTTPIngressPath associates a path regex with a backend. Incoming urls matching the path are forwarded to the backend."
)
public class V1beta1HTTPIngressPath {
    @SerializedName("backend")
    private V1beta1IngressBackend backend = null;
    @SerializedName("path")
    private String path = null;

    public V1beta1HTTPIngressPath() {
    }

    public V1beta1HTTPIngressPath backend(V1beta1IngressBackend backend) {
        this.backend = backend;
        return this;
    }

    @ApiModelProperty(
        required = true,
        value = "Backend defines the referenced service endpoint to which the traffic will be forwarded to."
    )
    public V1beta1IngressBackend getBackend() {
        return this.backend;
    }

    public void setBackend(V1beta1IngressBackend backend) {
        this.backend = backend;
    }

    public V1beta1HTTPIngressPath path(String path) {
        this.path = path;
        return this;
    }

    @ApiModelProperty("Path is an extended POSIX regex as defined by IEEE Std 1003.1, (i.e this follows the egrep/unix syntax, not the perl syntax) matched against the path of an incoming request. Currently it can contain characters disallowed from the conventional \"path\" part of a URL as defined by RFC 3986. Paths must begin with a '/'. If unspecified, the path defaults to a catch all sending traffic to the backend.")
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1beta1HTTPIngressPath v1beta1HTTPIngressPath = (V1beta1HTTPIngressPath)o;
            return Objects.equals(this.backend, v1beta1HTTPIngressPath.backend) && Objects.equals(this.path, v1beta1HTTPIngressPath.path);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.backend, this.path});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1beta1HTTPIngressPath {\n");
        sb.append("    backend: ").append(this.toIndentedString(this.backend)).append("\n");
        sb.append("    path: ").append(this.toIndentedString(this.path)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
