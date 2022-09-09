//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(
    description = "IngressSpec describes the Ingress the user wishes to exist."
)
public class V1beta1IngressSpec {
    @SerializedName("backend")
    private V1beta1IngressBackend backend = null;
    @SerializedName("rules")
    private List<V1beta1IngressRule> rules = null;
    @SerializedName("tls")
    private List<V1beta1IngressTLS> tls = null;

    public V1beta1IngressSpec() {
    }

    public V1beta1IngressSpec backend(V1beta1IngressBackend backend) {
        this.backend = backend;
        return this;
    }

    @ApiModelProperty("A default backend capable of servicing requests that don't match any rule. At least one of 'backend' or 'rules' must be specified. This field is optional to allow the loadbalancer controller or defaulting logic to specify a global default.")
    public V1beta1IngressBackend getBackend() {
        return this.backend;
    }

    public void setBackend(V1beta1IngressBackend backend) {
        this.backend = backend;
    }

    public V1beta1IngressSpec rules(List<V1beta1IngressRule> rules) {
        this.rules = rules;
        return this;
    }

    public V1beta1IngressSpec addRulesItem(V1beta1IngressRule rulesItem) {
        if (this.rules == null) {
            this.rules = new ArrayList();
        }

        this.rules.add(rulesItem);
        return this;
    }

    @ApiModelProperty("A list of host rules used to configure the Ingress. If unspecified, or no rule matches, all traffic is sent to the default backend.")
    public List<V1beta1IngressRule> getRules() {
        return this.rules;
    }

    public void setRules(List<V1beta1IngressRule> rules) {
        this.rules = rules;
    }

    public V1beta1IngressSpec tls(List<V1beta1IngressTLS> tls) {
        this.tls = tls;
        return this;
    }

    public V1beta1IngressSpec addTlsItem(V1beta1IngressTLS tlsItem) {
        if (this.tls == null) {
            this.tls = new ArrayList();
        }

        this.tls.add(tlsItem);
        return this;
    }

    @ApiModelProperty("TLS configuration. Currently the Ingress only supports a single TLS port, 443. If multiple members of this list specify different hosts, they will be multiplexed on the same port according to the hostname specified through the SNI TLS extension, if the ingress controller fulfilling the ingress supports SNI.")
    public List<V1beta1IngressTLS> getTls() {
        return this.tls;
    }

    public void setTls(List<V1beta1IngressTLS> tls) {
        this.tls = tls;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1beta1IngressSpec v1beta1IngressSpec = (V1beta1IngressSpec)o;
            return Objects.equals(this.backend, v1beta1IngressSpec.backend) && Objects.equals(this.rules, v1beta1IngressSpec.rules) && Objects.equals(this.tls, v1beta1IngressSpec.tls);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.backend, this.rules, this.tls});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1beta1IngressSpec {\n");
        sb.append("    backend: ").append(this.toIndentedString(this.backend)).append("\n");
        sb.append("    rules: ").append(this.toIndentedString(this.rules)).append("\n");
        sb.append("    tls: ").append(this.toIndentedString(this.tls)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
