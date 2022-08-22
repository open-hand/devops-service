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
    description = "IngressRule represents the rules mapping the paths under a specified host to the related backend services. Incoming requests are first evaluated for a host match, then routed to the backend associated with the matching IngressRuleValue."
)
public class V1beta1IngressRule {
    @SerializedName("host")
    private String host = null;
    @SerializedName("http")
    private V1beta1HTTPIngressRuleValue http = null;

    public V1beta1IngressRule() {
    }

    public V1beta1IngressRule host(String host) {
        this.host = host;
        return this;
    }

    @ApiModelProperty("Host is the fully qualified domain name of a network host, as defined by RFC 3986. Note the following deviations from the \"host\" part of the URI as defined in the RFC: 1. IPs are not allowed. Currently an IngressRuleValue can only apply to the    IP in the Spec of the parent Ingress. 2. The `:` delimiter is not respected because ports are not allowed.    Currently the port of an Ingress is implicitly :80 for http and    :443 for https. Both these may change in the future. Incoming requests are matched against the host before the IngressRuleValue. If the host is unspecified, the Ingress routes all traffic based on the specified IngressRuleValue.")
    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public V1beta1IngressRule http(V1beta1HTTPIngressRuleValue http) {
        this.http = http;
        return this;
    }

    @ApiModelProperty("")
    public V1beta1HTTPIngressRuleValue getHttp() {
        return this.http;
    }

    public void setHttp(V1beta1HTTPIngressRuleValue http) {
        this.http = http;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1beta1IngressRule v1beta1IngressRule = (V1beta1IngressRule)o;
            return Objects.equals(this.host, v1beta1IngressRule.host) && Objects.equals(this.http, v1beta1IngressRule.http);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.host, this.http});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1beta1IngressRule {\n");
        sb.append("    host: ").append(this.toIndentedString(this.host)).append("\n");
        sb.append("    http: ").append(this.toIndentedString(this.http)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
