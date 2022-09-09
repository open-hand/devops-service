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
    description = "IngressTLS describes the transport layer security associated with an Ingress."
)
public class V1beta1IngressTLS {
    @SerializedName("hosts")
    private List<String> hosts = null;
    @SerializedName("secretName")
    private String secretName = null;

    public V1beta1IngressTLS() {
    }

    public V1beta1IngressTLS hosts(List<String> hosts) {
        this.hosts = hosts;
        return this;
    }

    public V1beta1IngressTLS addHostsItem(String hostsItem) {
        if (this.hosts == null) {
            this.hosts = new ArrayList();
        }

        this.hosts.add(hostsItem);
        return this;
    }

    @ApiModelProperty("Hosts are a list of hosts included in the TLS certificate. The values in this list must match the name/s used in the tlsSecret. Defaults to the wildcard host setting for the loadbalancer controller fulfilling this Ingress, if left unspecified.")
    public List<String> getHosts() {
        return this.hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public V1beta1IngressTLS secretName(String secretName) {
        this.secretName = secretName;
        return this;
    }

    @ApiModelProperty("SecretName is the name of the secret used to terminate SSL traffic on 443. Field is left optional to allow SSL routing based on SNI hostname alone. If the SNI host in a listener conflicts with the \"Host\" header field used by an IngressRule, the SNI host is used for termination and value of the Host header is used for routing.")
    public String getSecretName() {
        return this.secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1beta1IngressTLS v1beta1IngressTLS = (V1beta1IngressTLS)o;
            return Objects.equals(this.hosts, v1beta1IngressTLS.hosts) && Objects.equals(this.secretName, v1beta1IngressTLS.secretName);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.hosts, this.secretName});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1beta1IngressTLS {\n");
        sb.append("    hosts: ").append(this.toIndentedString(this.hosts)).append("\n");
        sb.append("    secretName: ").append(this.toIndentedString(this.secretName)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
